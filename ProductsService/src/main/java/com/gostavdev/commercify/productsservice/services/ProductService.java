package com.gostavdev.commercify.productsservice.services;

import com.gostavdev.commercify.productsservice.dto.ProductDTO;
import com.gostavdev.commercify.productsservice.dto.ProductDTOMapper;
import com.gostavdev.commercify.productsservice.entities.ProductEntity;
import com.gostavdev.commercify.productsservice.repositories.ProductRepository;
import com.gostavdev.commercify.productsservice.requests.ProductRequest;
import com.stripe.exception.StripeException;
import com.stripe.model.Product;
import com.stripe.param.ProductCreateParams;
import com.stripe.param.ProductUpdateParams;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductDTOMapper mapper;

    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll().stream().map(mapper).toList();
    }

    public List<ProductDTO> getActiveProducts() {
        return productRepository.queryAllByActiveTrue().stream().map(mapper).toList();
    }

    public ProductDTO getProductById(Long id) {
        return productRepository.findById(id).map(mapper).orElseThrow(() -> new NoSuchElementException("Product not found"));
    }

    public ProductDTO saveProduct(ProductRequest request) {
        ProductEntity productEntity = ProductEntity.builder()
                .name(request.name())
                .description(request.description())
                .currency(request.currency())
                .unitPrice(request.unitPrice())
                .stock(request.stock())
                .active(true)
                .build();

        try {
            long amountInCents = (long) (productEntity.getUnitPrice() * 100);

            ProductCreateParams.DefaultPriceData defaultPriceData = ProductCreateParams.DefaultPriceData.builder()
                    .setCurrency(productEntity.getCurrency())
                    .setUnitAmount(amountInCents).build();

            ProductCreateParams params =
                    ProductCreateParams.builder()
                            .setName(productEntity.getName())
                            .setDescription(productEntity.getDescription())
                            .setDefaultPriceData(defaultPriceData)
                            .build();
            Product stripeProduct = Product.create(params);

            productEntity.setStripeId(stripeProduct.getId());

        } catch (StripeException e) {
            throw new RuntimeException("Stripe error", e);
        }

        ProductEntity savedProduct = productRepository.save(productEntity);

        return mapper.apply(savedProduct);
    }

    public boolean deleteProduct(Long id, boolean forceDeletion) throws RuntimeException {
        ProductEntity productEnt = productRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Product not found"));

        Product resource;
        try {
            resource = Product.retrieve(productEnt.getStripeId());
        } catch (StripeException e) {
            productRepository.deleteById(id);
            return true;
        }

        if (forceDeletion) {
            productRepository.deleteById(id);
        } else {
            productEnt.setActive(false);
            productRepository.save(productEnt);
        }

        if (!resource.getActive()) {
            throw new RuntimeException("Product has already been deleted");
        }

        ProductUpdateParams params =
                ProductUpdateParams.builder().setActive(false).build();

        try {
            resource.update(params);
        } catch (StripeException e) {
            return false;
        }

        return true;
    }

    public List<ProductDTO> saveProducts(List<ProductRequest> request) {
        return request.stream().map(this::saveProduct).toList();
    }
}
