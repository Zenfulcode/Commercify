package com.zenfulcode.commercify.commercify.controller;


import com.zenfulcode.commercify.commercify.api.requests.CreateProductRequest;
import com.zenfulcode.commercify.commercify.api.requests.UpdatePriceRequest;
import com.zenfulcode.commercify.commercify.api.requests.UpdateProductRequest;
import com.zenfulcode.commercify.commercify.api.responses.ErrorResponse;
import com.zenfulcode.commercify.commercify.api.responses.ProductDeletionErrorResponse;
import com.zenfulcode.commercify.commercify.api.responses.ProductUpdateResponse;
import com.zenfulcode.commercify.commercify.api.responses.ValidationErrorResponse;
import com.zenfulcode.commercify.commercify.dto.ProductDTO;
import com.zenfulcode.commercify.commercify.dto.ProductUpdateResult;
import com.zenfulcode.commercify.commercify.exception.*;
import com.zenfulcode.commercify.commercify.service.PriceService;
import com.zenfulcode.commercify.commercify.service.ProductService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/v1/products")
@AllArgsConstructor
@Slf4j
public class ProductController {
    private final ProductService productService;
    private final PriceService priceService;
    private final PagedResourcesAssembler<ProductDTO> pagedResourcesAssembler;

    private static final Set<String> VALID_SORT_FIELDS = Set.of(
            "productId", "name", "stock", "createdAt", "updatedAt"
    );

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<ProductDTO>>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "productId") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        validateSortField(sortBy);
        Sort.Direction direction = Sort.Direction.fromString(sortDirection.toUpperCase());
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<ProductDTO> products = productService.getAllProducts(pageRequest);
        return ResponseEntity.ok(pagedResourcesAssembler.toModel(products));
    }

    @GetMapping("/active")
    public ResponseEntity<PagedModel<EntityModel<ProductDTO>>> getActiveProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "productId") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        validateSortField(sortBy);
        Sort.Direction direction = Sort.Direction.fromString(sortDirection.toUpperCase());
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<ProductDTO> products = productService.getActiveProducts(pageRequest);
        return ResponseEntity.ok(pagedResourcesAssembler.toModel(products));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable Long id) {
        try {
            ProductDTO product = productService.getProductById(id);
            if (product == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(product);
        } catch (Exception e) {
            log.error("Error retrieving product", e);
            return ResponseEntity.internalServerError()
                    .body(new ErrorResponse("Error retrieving product: " + e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<?> createProduct(@Validated @RequestBody CreateProductRequest request) {
        try {
            ProductDTO product = productService.saveProduct(request);
            return ResponseEntity.ok(product);
        } catch (ProductValidationException e) {
            return ResponseEntity.badRequest().body(new ValidationErrorResponse(e.getErrors()));
        } catch (Exception e) {
            log.error("Error creating product", e);
            return ResponseEntity.internalServerError()
                    .body(new ErrorResponse("Error creating product: " + e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(
            @PathVariable Long id,
            @Validated @RequestBody UpdateProductRequest request
    ) {
        try {
            ProductUpdateResult result = productService.updateProduct(id, request);
            if (!result.getWarnings().isEmpty()) {
                return ResponseEntity.ok()
                        .body(new ProductUpdateResponse(
                                result.getProduct(),
                                "Product updated with warnings",
                                result.getWarnings()
                        ));
            }
            return ResponseEntity.ok(result.getProduct());
        } catch (ProductNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (ProductValidationException e) {
            return ResponseEntity.badRequest().body(new ValidationErrorResponse(e.getErrors()));
        } catch (Exception e) {
            log.error("Error updating product", e);
            return ResponseEntity.internalServerError()
                    .body(new ErrorResponse("Error updating product: " + e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/price")
    public ResponseEntity<?> updateProductPrice(
            @PathVariable Long id,
            @Validated @RequestBody UpdatePriceRequest request
    ) {
        try {
            ProductDTO updatedProduct = productService.updateProductPrice(id, request);
            return ResponseEntity.ok(updatedProduct);
        } catch (ProductNotFoundException | PriceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (ProductValidationException e) {
            return ResponseEntity.badRequest().body(new ValidationErrorResponse(e.getErrors()));
        } catch (Exception e) {
            log.error("Error updating product price", e);
            return ResponseEntity.internalServerError()
                    .body(new ErrorResponse("Error updating product price: " + e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.ok().build();
        } catch (ProductNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (ProductDeletionException e) {
            return ResponseEntity.badRequest().body(new ProductDeletionErrorResponse(
                    "Cannot delete product",
                    e.getIssues(),
                    e.getActiveOrders()
            ));
        } catch (Exception e) {
            log.error("Error deleting product", e);
            return ResponseEntity.internalServerError()
                    .body(new ErrorResponse("Error deleting product: " + e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/duplicate")
    public ResponseEntity<?> duplicateProduct(@PathVariable Long id) {
        try {
            ProductDTO duplicatedProduct = productService.duplicateProduct(id);
            return ResponseEntity.ok(duplicatedProduct);
        } catch (ProductNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error duplicating product", e);
            return ResponseEntity.internalServerError()
                    .body(new ErrorResponse("Error duplicating product: " + e.getMessage()));
        }
    }

    private void validateSortField(String sortBy) {
        if (!VALID_SORT_FIELDS.contains(sortBy)) {
            throw new InvalidSortFieldException("Invalid sort field: " + sortBy);
        }
    }


    // Get Product By ID
    // Delete Product
    // Update Product
    // Update product price
    // Get active products
}
