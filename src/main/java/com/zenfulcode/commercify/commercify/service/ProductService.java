package com.zenfulcode.commercify.commercify.service;

import com.stripe.Stripe;
import com.zenfulcode.commercify.commercify.OrderStatus;
import com.zenfulcode.commercify.commercify.api.requests.products.CreatePriceRequest;
import com.zenfulcode.commercify.commercify.api.requests.products.CreateProductRequest;
import com.zenfulcode.commercify.commercify.api.requests.products.UpdatePriceRequest;
import com.zenfulcode.commercify.commercify.api.requests.products.UpdateProductRequest;
import com.zenfulcode.commercify.commercify.dto.OrderDTO;
import com.zenfulcode.commercify.commercify.dto.ProductDTO;
import com.zenfulcode.commercify.commercify.dto.ProductDeletionValidationResult;
import com.zenfulcode.commercify.commercify.dto.ProductUpdateResult;
import com.zenfulcode.commercify.commercify.dto.mapper.OrderMapper;
import com.zenfulcode.commercify.commercify.dto.mapper.ProductMapper;
import com.zenfulcode.commercify.commercify.entity.ProductEntity;
import com.zenfulcode.commercify.commercify.exception.ProductDeletionException;
import com.zenfulcode.commercify.commercify.exception.ProductNotFoundException;
import com.zenfulcode.commercify.commercify.exception.ProductValidationException;
import com.zenfulcode.commercify.commercify.factory.ProductFactory;
import com.zenfulcode.commercify.commercify.repository.OrderLineRepository;
import com.zenfulcode.commercify.commercify.repository.ProductRepository;
import com.zenfulcode.commercify.commercify.service.stripe.StripeProductService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class ProductService {
    private final ProductRepository productRepository;
    private final StripeProductService stripeProductService;
    private final ProductMapper mapper;
    private final ProductFactory productFactory;
    private final OrderLineRepository orderLineRepository;
    private final OrderMapper orderMapper;

    @Transactional
    public ProductDTO saveProduct(CreateProductRequest request) {
        validateProductRequest(request);

        ProductEntity product = productFactory.createFromRequest(request);

        if (Stripe.apiKey != null && !Stripe.apiKey.isBlank()) {
            String stripeId = stripeProductService.createStripeProduct(product);
            product.setStripeId(stripeId);
        }

        ProductEntity savedProduct = productRepository.save(product);

        CreatePriceRequest priceRequest = new CreatePriceRequest(
                product.getCurrency(),
                product.getUnitPrice()
        );

        // Create prices after product is saved
        stripeProductService.createStripePrice(savedProduct, priceRequest);

        return mapper.apply(savedProduct);
    }

    private void validateProductRequest(CreateProductRequest request) {
        List<String> errors = new ArrayList<>();

        if (request.name() == null || request.name().isBlank()) {
            errors.add("Product name is required");
        }

        if (request.price() == null || request.price().amount() == null || request.price().amount() < 0) {
            errors.add("At least one price is required");
        }

        if (request.stock() != null && request.stock() < 0) {
            errors.add("Stock cannot be negative");
        }

        if (!errors.isEmpty()) {
            throw new ProductValidationException(errors);
        }
    }

    @Transactional
    public ProductUpdateResult updateProduct(Long id, UpdateProductRequest request) {
        List<String> warnings = new ArrayList<>();

        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        product.setName(request.name() != null ? request.name() : product.getName());
        product.setDescription(request.description() != null ? request.description() : product.getDescription());
        product.setStock(request.stock() != null ? request.stock() : product.getStock());
        product.setActive(request.active() != null ? request.active() : product.getActive());
        product.setImageUrl(request.imageUrl() != null ? request.imageUrl() : product.getImageUrl());
        product.setCurrency(request.price().currency() != null ? request.price().currency() : product.getCurrency());
        product.setUnitPrice(request.price().amount() != null ? request.price().amount() : product.getUnitPrice());

        updateProductPrice(product, request.price());

        if (product.getStripeId() != null && Stripe.apiKey != null) {
            try {
                stripeProductService.updateStripeProduct(product.getStripeId(), product);
            } catch (Exception e) {
                warnings.add("Stripe update failed: " + e.getMessage());
                log.error("Stripe update failed", e);
            }
        }

        ProductEntity savedProduct = productRepository.save(product);
        return ProductUpdateResult.withWarnings(mapper.apply(savedProduct), warnings);
    }

    private void updateProductPrice(ProductEntity product, UpdatePriceRequest request) {
        validatePriceRequest(request);

        if (product.getStripePriceId() != null) {
            stripeProductService.updateStripePrice(product, request);
        } else {
            CreatePriceRequest createRequest = new CreatePriceRequest(
                    request.currency(),
                    request.amount()
            );
            stripeProductService.createStripePrice(product, createRequest);
        }

        ProductEntity savedProduct = productRepository.save(product);
        mapper.apply(savedProduct);
    }

    /**
     * Checks if a product can be safely deleted
     *
     * @param product The product to check
     * @return A validation result containing any issues found
     */
    public ProductDeletionValidationResult validateProductDeletion(ProductEntity product) {
        // Get sample of active orders for reference
        List<OrderDTO> activeOrders = orderLineRepository.findActiveOrdersForProduct(product.getId(),
                List.of(OrderStatus.PENDING, OrderStatus.CONFIRMED, OrderStatus.SHIPPED)
        ).stream().map(orderMapper).collect(Collectors.toList());

        List<String> issues = new ArrayList<>();
        if (!activeOrders.isEmpty()) {
            issues.add(String.format(
                    "Product has %d active orders",
                    activeOrders.size()
            ));
        }

        return new ProductDeletionValidationResult(
                issues.isEmpty(),
                issues,
                activeOrders
        );
    }

    @Transactional
    public void deleteProduct(Long id) throws RuntimeException {
        ProductEntity productEnt = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        ProductDeletionValidationResult validationResult = validateProductDeletion(productEnt);
        if (!validationResult.canDelete()) {
            throw new ProductDeletionException(
                    "Cannot delete product",
                    validationResult.getIssues(),
                    validationResult.getActiveOrders()
            );
        }

        try {
            stripeProductService.deactivateProduct(productEnt);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        productRepository.deleteById(id);
    }

    @Transactional
    public void reactivateProduct(Long id) throws RuntimeException {
        ProductEntity productEnt = productRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Product not found"));

        try {
            stripeProductService.reactivateProduct(productEnt);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        productEnt.setActive(true);
        productRepository.save(productEnt);
    }

    @Transactional
    public void deactivateProduct(Long id) throws RuntimeException {
        ProductEntity productEnt = productRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Product not found"));

        try {
            stripeProductService.deactivateProduct(productEnt);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        productEnt.setActive(false);
        productRepository.save(productEnt);
    }


    private void validatePriceRequest(UpdatePriceRequest request) {
        List<String> errors = new ArrayList<>();

        if (request.amount() != null && request.amount() < 0) {
            errors.add("Price amount must be greater than zero");
        }

        if (!errors.isEmpty()) {
            throw new ProductValidationException(errors);
        }
    }

    public Page<ProductDTO> getAllProducts(PageRequest pageRequest) {
        return productRepository.findAll(pageRequest)
                .map(mapper);
    }

    public ProductDTO getProductById(Long id) {
        return productRepository.findById(id)
                .map(mapper)
                .orElse(null);
    }

    public Page<ProductDTO> getActiveProducts(PageRequest pageRequest) {
        return productRepository.queryAllByActiveTrue(pageRequest)
                .map(mapper);
    }
}
