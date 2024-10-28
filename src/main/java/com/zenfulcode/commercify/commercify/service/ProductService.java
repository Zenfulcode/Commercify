package com.zenfulcode.commercify.commercify.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Product;
import com.stripe.param.ProductCreateParams;
import com.zenfulcode.commercify.commercify.api.requests.CreateProductRequest;
import com.zenfulcode.commercify.commercify.dto.ProductDTO;
import com.zenfulcode.commercify.commercify.dto.mapper.ProductDTOMapper;
import com.zenfulcode.commercify.commercify.entity.ProductEntity;
import com.zenfulcode.commercify.commercify.repository.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@AllArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductDTOMapper mapper;

    public Page<ProductDTO> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable).map(mapper);
    }

    public Page<ProductDTO> getActiveProducts(Pageable pageable) {
        return productRepository.queryAllByActiveTrue(pageable).map(mapper);
    }

    public ProductDTO getProductById(Long id) {
        return productRepository.findById(id).map(mapper).orElseThrow(() -> new NoSuchElementException("Product not found"));
    }

    public ProductDTO saveProduct(CreateProductRequest request) {
        ProductEntity productEntity = ProductEntity.builder()
                .name(request.name())
                .description(request.description())
                .currency(request.currency())
                .unitPrice(request.unitPrice())
                .stock(request.stock())
                .active(true)
                .build();

        if (!Stripe.apiKey.isBlank()) {
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
        }

        ProductEntity savedProduct = productRepository.save(productEntity);

        return mapper.apply(savedProduct);
    }

    public boolean deleteProduct(Long id) throws RuntimeException {
        ProductEntity productEnt = productRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Product not found"));

        if (!Stripe.apiKey.isBlank()) {
            deleteProductFromStripe(productEnt);
        }

        if (Stripe.apiKey.isBlank() && productEnt.getStripeId() != null) {
            throw new RuntimeException("Cant delete product from stripe without stripe key");
        }

        productRepository.deleteById(id);
        return true;
    }

    private void deleteProductFromStripe(ProductEntity productEnt) {
        try {
            Product stripeProduct = Product.retrieve(productEnt.getStripeId());
            stripeProduct.delete();
        } catch (StripeException e) {
            throw new RuntimeException("Stripe error", e);
        }
    }

    public List<ProductDTO> saveProducts(List<CreateProductRequest> request) {
        return request.stream().map(this::saveProduct).toList();
    }

    public void deactivateProduct(Long id) {
        ProductEntity productEnt = productRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Product not found"));

        if (Stripe.apiKey.isBlank() && productEnt.getStripeId() != null) {
            throw new RuntimeException("Cant delete product from stripe without stripe key");
        }

        if (productEnt.getStripeId() != null) {
            try {
                Product stripeProduct = Product.retrieve(productEnt.getStripeId());
                stripeProduct.delete();
            } catch (StripeException e) {
                throw new RuntimeException("Stripe error", e);
            }
        }

        productEnt.setActive(false);
        productRepository.save(productEnt);
    }
}
