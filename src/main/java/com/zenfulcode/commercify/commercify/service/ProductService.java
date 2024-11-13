package com.zenfulcode.commercify.commercify.service;

import com.stripe.Stripe;
import com.zenfulcode.commercify.commercify.OrderStatus;
import com.zenfulcode.commercify.commercify.api.requests.products.CreateVariantOptionRequest;
import com.zenfulcode.commercify.commercify.api.requests.products.PriceRequest;
import com.zenfulcode.commercify.commercify.api.requests.products.ProductRequest;
import com.zenfulcode.commercify.commercify.api.requests.products.ProductVariantRequest;
import com.zenfulcode.commercify.commercify.dto.*;
import com.zenfulcode.commercify.commercify.dto.mapper.OrderMapper;
import com.zenfulcode.commercify.commercify.dto.mapper.ProductMapper;
import com.zenfulcode.commercify.commercify.dto.mapper.ProductVariantMapper;
import com.zenfulcode.commercify.commercify.entity.OrderEntity;
import com.zenfulcode.commercify.commercify.entity.ProductEntity;
import com.zenfulcode.commercify.commercify.entity.ProductVariantEntity;
import com.zenfulcode.commercify.commercify.entity.VariantOptionEntity;
import com.zenfulcode.commercify.commercify.exception.ProductDeletionException;
import com.zenfulcode.commercify.commercify.exception.ProductNotFoundException;
import com.zenfulcode.commercify.commercify.exception.ProductValidationException;
import com.zenfulcode.commercify.commercify.factory.ProductFactory;
import com.zenfulcode.commercify.commercify.repository.OrderLineRepository;
import com.zenfulcode.commercify.commercify.repository.ProductRepository;
import com.zenfulcode.commercify.commercify.repository.ProductVariantRepository;
import com.zenfulcode.commercify.commercify.service.stripe.StripeProductService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final OrderLineRepository orderLineRepository;
    private final StripeProductService stripeProductService;
    private final ProductMapper productMapper;
    private final ProductVariantMapper variantMapper;
    private final ProductFactory productFactory;
    private final OrderMapper orderMapper;

    @Transactional
    public ProductDTO saveProduct(ProductRequest request) {
        validateProductRequest(request);

        ProductEntity product = productFactory.createFromRequest(request);

        // Create variants if provided
        if (request.variants() != null && !request.variants().isEmpty()) {
            Set<ProductVariantEntity> variants = createVariantsFromRequest(request.variants(), product);
            product.setVariants(variants);
        }

        ProductEntity savedProduct = productRepository.save(product);
        return productMapper.apply(savedProduct);
    }

    @Transactional
    public ProductDTO addVariantToProduct(Long productId, ProductVariantRequest request) {
        validateVariantRequest(request);

        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        ProductVariantEntity variant = createVariantFromRequest(request, product);

        product.addVariant(variant);
        ProductEntity savedProduct = productRepository.save(product);

        return productMapper.apply(savedProduct);
    }

    @Transactional
    public ProductDTO updateVariant(Long productId, Long variantId, ProductVariantRequest request) {
        validateVariantRequest(request);

        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        ProductVariantEntity variant = product.getVariants().stream()
                .filter(v -> v.getId().equals(variantId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Variant not found"));

        updateVariantFromRequest(variant, request);

        ProductEntity savedProduct = productRepository.save(product);
        return productMapper.apply(savedProduct);
    }

    @Transactional
    public void deleteVariant(Long productId, Long variantId) {
        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        ProductVariantEntity variant = product.getVariants().stream()
                .filter(v -> v.getId().equals(variantId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Variant not found"));

        validateVariantDeletion(variant);

        product.getVariants().remove(variant);
        productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public Page<ProductVariantEntityDto> getProductVariants(Long productId, PageRequest pageRequest) {
        productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        return variantRepository.findByProductId(productId, pageRequest)
                .map(variantMapper);
    }

    @Transactional(readOnly = true)
    public ProductVariantEntityDto getProductVariant(Long productId, Long variantId) {
        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        return product.getVariants().stream()
                .filter(v -> v.getId().equals(variantId))
                .findFirst()
                .map(variantMapper)
                .orElse(null);
    }

    @Transactional
    public ProductDTO updateVariantStock(Long productId, Long variantId, Integer stock) {
        if (stock < 0) {
            throw new IllegalArgumentException("Stock cannot be negative");
        }

        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        ProductVariantEntity variant = product.getVariants().stream()
                .filter(v -> v.getId().equals(variantId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Variant not found"));

        variant.setStock(stock);

        ProductEntity savedProduct = productRepository.save(product);
        return productMapper.apply(savedProduct);
    }

    // Helper methods
    private Set<ProductVariantEntity> createVariantsFromRequest(List<ProductVariantRequest> variantRequests, ProductEntity product) {
        return variantRequests.stream()
                .map(request -> createVariantFromRequest(request, product))
                .collect(Collectors.toSet());
    }

    private ProductVariantEntity createVariantFromRequest(ProductVariantRequest request, ProductEntity product) {
        ProductVariantEntity variant = ProductVariantEntity.builder()
                .sku(request.sku())
                .stock(request.stock())
                .imageUrl(request.imageUrl())
                .price(request.price().amount())
                .currency(request.price().currency())
                .product(product)
                .options(new HashSet<>())
                .build();

        // Create variant options
        for (CreateVariantOptionRequest optionRequest : request.options()) {
            VariantOptionEntity option = VariantOptionEntity.builder()
                    .name(optionRequest.name())
                    .value(optionRequest.value())
                    .productVariant(variant)
                    .build();
            variant.addOption(option);
        }

        return variant;
    }

    private void updateVariantFromRequest(ProductVariantEntity variant, ProductVariantRequest request) {
        variant.setSku(request.sku());
        variant.setStock(request.stock());
        variant.setImageUrl(request.imageUrl());
        variant.setPrice(request.price().amount());
        variant.setCurrency(request.price().currency());

        // Update options
        variant.getOptions().clear();
        for (CreateVariantOptionRequest optionRequest : request.options()) {
            VariantOptionEntity option = VariantOptionEntity.builder()
                    .name(optionRequest.name())
                    .value(optionRequest.value())
                    .productVariant(variant)
                    .build();
            variant.addOption(option);
        }
    }

    private void validateVariantRequest(ProductVariantRequest request) {
        List<String> errors = new ArrayList<>();

        if (request.sku() == null || request.sku().isBlank()) {
            errors.add("SKU is required");
        }

        if (request.stock() == null || request.stock() < 0) {
            errors.add("Stock must be non-negative");
        }

        if (request.price() == null || request.price().amount() == null || request.price().amount() < 0) {
            errors.add("Price is required and must be non-negative");
        }

        if (request.options() == null || request.options().isEmpty()) {
            errors.add("At least one variant option is required");
        }

        if (!errors.isEmpty()) {
            throw new ProductValidationException(errors);
        }
    }

    private void validateVariantDeletion(ProductVariantEntity variant) {
        // Check for active orders using this variant
        Set<OrderEntity> activeOrders = orderLineRepository.findActiveOrdersForVariant(
                variant.getId(),
                List.of(OrderStatus.PENDING, OrderStatus.CONFIRMED, OrderStatus.SHIPPED)
        );

        if (!activeOrders.isEmpty()) {
            List<OrderDTO> activeOrderDTOs = activeOrders.stream()
                    .map(orderMapper)
                    .collect(Collectors.toList());

            throw new ProductDeletionException(
                    "Cannot delete variant with active orders",
                    List.of("Variant has active orders"),
                    activeOrderDTOs
            );
        }
    }

    private boolean stripeEnabled() {
        return Stripe.apiKey != null && !Stripe.apiKey.isBlank();
    }

    private void validateProductRequest(ProductRequest request) {
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
    public ProductUpdateResult updateProduct(Long id, ProductRequest request) {
        List<String> warnings = new ArrayList<>();

        ProductEntity product = productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));

        product.setName(request.name() != null ? request.name() : product.getName());
        product.setDescription(request.description() != null ? request.description() : product.getDescription());
        product.setStock(request.stock() != null ? request.stock() : product.getStock());
        product.setActive(request.active() != null ? request.active() : product.getActive());
        product.setImageUrl(request.imageUrl() != null ? request.imageUrl() : product.getImageUrl());

        updateProductPrice(product, request.price());

        ProductEntity savedProduct = productRepository.save(product);
        return ProductUpdateResult.withWarnings(productMapper.apply(savedProduct), warnings);
    }

    private void updateProductPrice(ProductEntity product, PriceRequest request) {
        validatePriceRequest(request);

        product.setUnitPrice(request.amount());
        product.setCurrency(request.currency());

        ProductEntity savedProduct = productRepository.save(product);
        productMapper.apply(savedProduct);
    }

    /**
     * Checks if a product can be safely deleted
     *
     * @param product The product to check
     * @return A validation result containing any issues found
     */
    public ProductDeletionValidationResult validateProductDeletion(ProductEntity product) {
        // Get sample of active orders for reference
        List<OrderDTO> activeOrders = orderLineRepository.findActiveOrdersForProduct(product.getId(), List.of(OrderStatus.PENDING, OrderStatus.CONFIRMED, OrderStatus.SHIPPED)).stream().map(orderMapper).collect(Collectors.toList());

        List<String> issues = new ArrayList<>();
        if (!activeOrders.isEmpty()) {
            issues.add(String.format("Product has %d active orders", activeOrders.size()));
        }

        return new ProductDeletionValidationResult(issues.isEmpty(), issues, activeOrders);
    }

    @Transactional
    public void deleteProduct(Long id) throws RuntimeException {
        ProductEntity productEnt = productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));

        ProductDeletionValidationResult validationResult = validateProductDeletion(productEnt);
        if (!validationResult.canDelete()) {
            throw new ProductDeletionException("Cannot delete product", validationResult.getIssues(), validationResult.getActiveOrders());
        }

        productRepository.deleteById(id);
    }

    @Transactional
    public void reactivateProduct(Long id) throws RuntimeException {
        ProductEntity productEnt = productRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Product not found"));
        productEnt.setActive(true);
        productRepository.save(productEnt);
    }

    @Transactional
    public void deactivateProduct(Long id) throws RuntimeException {
        ProductEntity productEnt = productRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Product not found"));
        productEnt.setActive(false);
        productRepository.save(productEnt);
    }

    private void validatePriceRequest(PriceRequest request) {
        List<String> errors = new ArrayList<>();

        if (request.amount() != null && request.amount() < 0) {
            errors.add("Price amount must be greater than zero");
        }

        if (!errors.isEmpty()) {
            throw new ProductValidationException(errors);
        }
    }

    public Page<ProductDTO> getAllProducts(PageRequest pageRequest) {
        return productRepository.findAll(pageRequest).map(productMapper);
    }

    public ProductDTO getProductById(Long id) {
        return productRepository.findById(id).map(productMapper).orElse(null);
    }

    public Page<ProductDTO> getActiveProducts(PageRequest pageRequest) {
        return productRepository.queryAllByActiveTrue(pageRequest).map(productMapper);
    }
}
