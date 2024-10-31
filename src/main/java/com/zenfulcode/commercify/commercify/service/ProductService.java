package com.zenfulcode.commercify.commercify.service;

import com.stripe.Stripe;
import com.zenfulcode.commercify.commercify.OrderStatus;
import com.zenfulcode.commercify.commercify.api.requests.CreatePriceRequest;
import com.zenfulcode.commercify.commercify.api.requests.CreateProductRequest;
import com.zenfulcode.commercify.commercify.api.requests.UpdatePriceRequest;
import com.zenfulcode.commercify.commercify.api.requests.UpdateProductRequest;
import com.zenfulcode.commercify.commercify.dto.ActiveOrderDTO;
import com.zenfulcode.commercify.commercify.dto.ProductDTO;
import com.zenfulcode.commercify.commercify.dto.ProductDeletionValidationResult;
import com.zenfulcode.commercify.commercify.dto.ProductUpdateResult;
import com.zenfulcode.commercify.commercify.dto.mapper.ProductDTOMapper;
import com.zenfulcode.commercify.commercify.entity.PriceEntity;
import com.zenfulcode.commercify.commercify.entity.ProductEntity;
import com.zenfulcode.commercify.commercify.exception.*;
import com.zenfulcode.commercify.commercify.factory.ProductFactory;
import com.zenfulcode.commercify.commercify.repository.OrderLineRepository;
import com.zenfulcode.commercify.commercify.repository.OrderRepository;
import com.zenfulcode.commercify.commercify.repository.ProductRepository;
import com.zenfulcode.commercify.commercify.service.stripe.StripeProductService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class ProductService {
    private final ProductRepository productRepository;
    private final PriceService priceService;
    private final StripeProductService stripeProductService;
    private final ProductDTOMapper mapper;
    private final ProductFactory productFactory;
    private final OrderLineRepository orderLineRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public ProductDTO saveProduct(CreateProductRequest request) {
        validateProductRequest(request);

        ProductEntity product = productFactory.createFromRequest(request);

        if (!Stripe.apiKey.isBlank()) {
            String stripeId = stripeProductService.createStripeProduct(product);
            product.setStripeId(stripeId);
        }

        ProductEntity savedProduct = productRepository.save(product);

        // Create prices after product is saved
        request.prices().forEach(priceRequest ->
                priceService.createPrice(priceRequest, savedProduct));

        return mapper.apply(savedProduct);
    }

    private void validateProductRequest(CreateProductRequest request) {
        List<String> errors = new ArrayList<>();

        if (request.name() == null || request.name().isBlank()) {
            errors.add("Product name is required");
        }

        if (request.prices() == null || request.prices().isEmpty()) {
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

        updateProductFields(product, request);

        if (!Stripe.apiKey.isBlank() && product.getStripeId() != null) {
            try {
                stripeProductService.updateStripeProduct(product.getStripeId(), product);
                handlePriceUpdates(product, request.prices());
            } catch (Exception e) {
                warnings.add("Stripe update failed: " + e.getMessage());
                log.error("Stripe update failed", e);
            }
        }

        ProductEntity savedProduct = productRepository.save(product);
        return ProductUpdateResult.withWarnings(mapper.apply(savedProduct), warnings);
    }

    @Transactional
    public ProductDTO updateProductPrice(Long productId, UpdatePriceRequest request) {
        validatePriceRequest(request);

        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        if (request.priceId() != null) {
            // Update existing price
            PriceEntity existingPrice = product.getPrices().stream()
                    .filter(p -> p.getPriceId().equals(request.priceId()))
                    .findFirst()
                    .orElseThrow(() -> new PriceNotFoundException(request.priceId()));

            // Check if this is the only active default price
            if (!request.active() && existingPrice.getIsDefault() &&
                    product.getPrices().stream()
                            .filter(p -> !p.getPriceId().equals(request.priceId()))
                            .filter(PriceEntity::getActive)
                            .noneMatch(PriceEntity::getIsDefault)) {
                throw new ProductValidationException(List.of("Cannot deactivate the only default price"));
            }

            priceService.updatePrice(existingPrice, request);
        } else {
            CreatePriceRequest createRequest = new CreatePriceRequest(
                    request.currency(),
                    request.amount(),
                    request.isDefault(),
                    request.active()
            );
            priceService.createPrice(createRequest, product);

            // If this is a new default price, update other prices
            if (request.isDefault()) {
                product.getPrices().stream()
                        .filter(p -> !p.getCurrency().equals(request.currency()))
                        .forEach(p -> p.setIsDefault(false));
            }
        }

        ProductEntity savedProduct = productRepository.save(product);
        return mapper.apply(savedProduct);
    }

    /**
     * Checks if a product can be safely deleted
     *
     * @param product The product to check
     * @return A validation result containing any issues found
     */
    public ProductDeletionValidationResult validateProductDeletion(ProductEntity product) {
        List<String> issues = new ArrayList<>();

        // Check for active orders
        long activeOrderCount = orderLineRepository.countActiveOrdersForProduct(
                product.getProductId(),
                ACTIVE_ORDER_STATUSES
        );

        if (activeOrderCount > 0) {
            issues.add(String.format(
                    "Product has %d active orders",
                    activeOrderCount
            ));
        }

        // Get sample of active orders for reference
        List<ActiveOrderDTO> activeOrders = orderLineRepository.findActiveOrdersForProduct(
                product.getProductId(),
                ACTIVE_ORDER_STATUSES,
                PageRequest.of(0, 5, Sort.by("createdAt").descending())
        );

        return new ProductDeletionValidationResult(
                issues.isEmpty(),
                issues,
                activeOrders
        );
    }

    @Transactional
    public void deleteProduct(Long id) {
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        // Check if product can be deleted
        ProductDeletionValidationResult validationResult = validateProductDeletion(product);
        if (!validationResult.canDelete()) {
            throw new ProductDeletionException(
                    "Cannot delete product",
                    validationResult.getIssues(),
                    validationResult.getActiveOrders()
            );
        }

        // Handle Stripe deletion if necessary
        if (!Stripe.apiKey.isBlank() && product.getStripeId() != null) {
            try {
                stripeProductService.deleteStripeProduct(product.getStripeId());
            } catch (Exception e) {
                log.error("Failed to delete product from Stripe", e);
                throw new StripeOperationException("Failed to delete product from Stripe", e);
            }
        }

        // Deactivate all prices first
        product.getPrices().forEach(price -> {
            price.setActive(false);
            if (price.getStripePriceId() != null) {
                priceService.deactivatePrice(price);
            }
        });

        // Delete the product
        productRepository.delete(product);
    }

    @Transactional
    public ProductDTO duplicateProduct(Long id) {
        ProductEntity originalProduct = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        ProductEntity duplicateProduct = productFactory.duplicate(originalProduct);

        ProductEntity savedDuplicate = productRepository.save(duplicateProduct);

        if (!Stripe.apiKey.isBlank()) {
            try {
                String stripeId = stripeProductService.createStripeProduct(savedDuplicate);
                savedDuplicate.setStripeId(stripeId);
            } catch (Exception e) {
                log.error("Failed to create Stripe product for duplicate", e);
                throw new StripeOperationException("Failed to create Stripe product for duplicate", e);
            }
        }

        originalProduct.getPrices().forEach(originalPrice -> {
            CreatePriceRequest priceRequest = new CreatePriceRequest(
                    originalPrice.getCurrency(),
                    originalPrice.getAmount(),
                    originalPrice.getIsDefault(),
                    false  // Start with inactive prices
            );
            priceService.createPrice(priceRequest, savedDuplicate);
        });

        return mapper.apply(productRepository.save(savedDuplicate));
    }

    private void validatePriceRequest(UpdatePriceRequest request) {
        List<String> errors = new ArrayList<>();

        if (request.amount() != null && request.amount() <= 0) {
            errors.add("Price amount must be greater than zero");
        }

        if (request.currency() != null && !isSupportedCurrency(request.currency())) {
            errors.add("Unsupported currency: " + request.currency());
        }

        if (!errors.isEmpty()) {
            throw new ProductValidationException(errors);
        }
    }

    private boolean isSupportedCurrency(String currency) {
        // Add your supported currencies here
        Set<String> supportedCurrencies = Set.of("USD", "EUR", "GBP");
        return supportedCurrencies.contains(currency.toUpperCase());
    }

    private boolean hasActiveOrders(ProductEntity product) {
        return orderLineRepository.existsActiveOrdersForProduct(
                product.getProductId(),
                ACTIVE_ORDER_STATUSES
        );
    }

    private static final Set<OrderStatus> ACTIVE_ORDER_STATUSES = Set.of(
            OrderStatus.PENDING,
            OrderStatus.CONFIRMED,
            OrderStatus.SHIPPED
    );

    private void updateProductFields(ProductEntity product, UpdateProductRequest request) {
        if (request.name() != null) product.setName(request.name());
        if (request.description() != null) product.setDescription(request.description());
        if (request.stock() != null) product.setStock(request.stock());
        if (request.imageUrl() != null) product.setImageUrl(request.imageUrl());
        if (request.active() != null) product.setActive(request.active());
    }

    private void handlePriceUpdates(ProductEntity product, List<UpdatePriceRequest> priceUpdates) {
        if (priceUpdates == null || priceUpdates.isEmpty()) {
            return;
        }

        Map<Long, PriceEntity> existingPrices = product.getPrices().stream()
                .collect(Collectors.toMap(PriceEntity::getPriceId, price -> price));

        priceUpdates.forEach(priceUpdate -> {
            if (priceUpdate.priceId() == null) {
                priceService.createPrice(
                        new CreatePriceRequest(
                                priceUpdate.currency(),
                                priceUpdate.amount(),
                                priceUpdate.isDefault(),
                                priceUpdate.active()
                        ),
                        product
                );
            } else {
                PriceEntity existingPrice = existingPrices.get(priceUpdate.priceId());
                if (existingPrice != null) {
                    priceService.updatePrice(existingPrice, priceUpdate);
                }
            }
        });

        // Deactivate prices not in update request
        Set<Long> updatedPriceIds = priceUpdates.stream()
                .map(UpdatePriceRequest::priceId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        existingPrices.forEach((id, price) -> {
            if (!updatedPriceIds.contains(id)) {
                priceService.deactivatePrice(price);
            }
        });
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
