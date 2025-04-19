package com.zenfulcode.commercify.product.application.service;

import com.zenfulcode.commercify.product.application.command.*;
import com.zenfulcode.commercify.product.application.query.CountNewProductsInPeriodQuery;
import com.zenfulcode.commercify.product.application.query.ProductQuery;
import com.zenfulcode.commercify.product.domain.exception.ProductDeletionException;
import com.zenfulcode.commercify.product.domain.exception.ProductNotFoundException;
import com.zenfulcode.commercify.product.domain.model.Product;
import com.zenfulcode.commercify.product.domain.model.ProductVariant;
import com.zenfulcode.commercify.product.domain.repository.ProductRepository;
import com.zenfulcode.commercify.product.domain.service.ProductDomainService;
import com.zenfulcode.commercify.product.domain.valueobject.*;
import com.zenfulcode.commercify.shared.domain.event.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductApplicationService {
    private final ProductDomainService productDomainService;
    private final DomainEventPublisher eventPublisher;
    private final ProductRepository productRepository;

    /**
     * Creates a new product
     */
    @Transactional
    public ProductId createProduct(CreateProductCommand command) {
        // Convert command to domain specification
        ProductSpecification spec = new ProductSpecification(
                command.name(),
                command.description(),
                command.imageUrl(),
                command.initialStock(),
                command.price(),
                command.variantSpecs()
        );

        // Use domain service to create product
        Product product = productDomainService.createProduct(spec);

        // Persist the product
        Product savedProduct = productRepository.save(product);

        // Publish domain events
        eventPublisher.publish(product.getDomainEvents());

        return savedProduct.getId();
    }

    /**
     * Updates an existing product
     */
    @Transactional
    public void updateProduct(UpdateProductCommand command) {
        // Retrieve product
        Product product = productRepository.findById(command.productId())
                .orElseThrow(() -> new ProductNotFoundException(command.productId()));

        // Update product through domain service
        productDomainService.updateProduct(product, command.updateSpec());

        // Save changes
        productRepository.save(product);

        // Publish events
        eventPublisher.publish(product.getDomainEvents());
    }

    /**
     * Handles product inventory adjustments
     */
    @Transactional
    public void adjustInventory(AdjustInventoryCommand command) {
        Product product = productRepository.findById(command.productId())
                .orElseThrow(() -> new ProductNotFoundException(command.productId()));

        InventoryAdjustment adjustment = new InventoryAdjustment(
                command.adjustmentType(),
                command.quantity(),
                command.reason()
        );

        productDomainService.adjustInventory(product, adjustment);
        productRepository.save(product);
        eventPublisher.publish(product.getDomainEvents());
    }

    /**
     * Updates variant prices
     */
    @Transactional
    public void updateVariantPrices(UpdateVariantPricesCommand command) {
        Product product = productRepository.findById(command.productId())
                .orElseThrow(() -> new ProductNotFoundException(command.productId()));

        productDomainService.updateVariantPrices(product, command.priceUpdates());
        productRepository.save(product);
        eventPublisher.publish(product.getDomainEvents());
    }

    /**
     * Adds variants to a product
     */
    @Transactional
    public void addProductVariants(AddProductVariantsCommand command) {
        Product product = productRepository.findById(command.productId())
                .orElseThrow(() -> new ProductNotFoundException(command.productId()));

        productDomainService.createProductVariants(product, command.variantSpecs());
        productRepository.save(product);
        eventPublisher.publish(product.getDomainEvents());
    }

    /**
     * Deactivates a product
     */
    @Transactional
    public void deactivateProduct(DeactivateProductCommand command) {
        Product product = productRepository.findById(command.productId())
                .orElseThrow(() -> new ProductNotFoundException(command.productId()));

        product.deactivate();
        productRepository.save(product);
        eventPublisher.publish(product.getDomainEvents());
    }

    /**
     * Activates a product
     */
    @Transactional
    public void activateProduct(ActivateProductCommand command) {
        Product product = productRepository.findById(command.productId())
                .orElseThrow(() -> new ProductNotFoundException(command.productId()));

        product.activate();
        productRepository.save(product);
        eventPublisher.publish(product.getDomainEvents());
    }

    /**
     * Deletes a product
     */
    @Transactional
    public void deleteProduct(DeleteProductCommand command) {
        Product product = productRepository.findById(command.productId())
                .orElseThrow(() -> new ProductNotFoundException(command.productId()));

        ProductDeletionValidation validation = productDomainService.validateProductDeletion(product);

        if (!validation.canDelete()) {
            throw new ProductDeletionException(
                    "Cannot delete product",
                    validation.issues()
            );
        }

        productRepository.delete(product);
        eventPublisher.publish(product.getDomainEvents());
    }

    @Transactional(readOnly = true)
    public List<Product> findAllProducts(Collection<ProductId> productIds) {
        return productDomainService.getAllProductsById(productIds);
    }

    /**
     * Queries for products
     */
    @Transactional(readOnly = true)
    public Page<Product> findProducts(ProductQuery query, Pageable pageable) {
        return switch (query.type()) {
            case ALL -> productRepository.findAll(pageable);
            case ACTIVE -> productRepository.findByActiveTrue(pageable);
            case BY_CATEGORY -> productRepository.findByCategory(query.categoryId(), pageable);
            case LOW_STOCK -> productRepository.findByStockLessThan(query.threshold(), pageable);
        };
    }

    @Transactional(readOnly = true)
    public Product getProductById(ProductId productId) {
        return productDomainService.getProductById(productId);
    }

    @Transactional(readOnly = true)
    public List<ProductVariant> findVariantsByIds(List<VariantId> variantIds) {
        return productRepository.findVariantsByIds(variantIds);
    }

    public int countNewProductsInPeriod(CountNewProductsInPeriodQuery query) {
        return productDomainService.countNewProductsInPeriod(query.startDate(), query.endDate());
    }
}