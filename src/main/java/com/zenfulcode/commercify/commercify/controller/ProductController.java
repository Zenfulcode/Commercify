package com.zenfulcode.commercify.commercify.controller;


import com.zenfulcode.commercify.commercify.api.requests.products.ProductRequest;
import com.zenfulcode.commercify.commercify.api.requests.products.ProductVariantRequest;
import com.zenfulcode.commercify.commercify.api.responses.ErrorResponse;
import com.zenfulcode.commercify.commercify.api.responses.ValidationErrorResponse;
import com.zenfulcode.commercify.commercify.api.responses.products.ProductDeletionErrorResponse;
import com.zenfulcode.commercify.commercify.api.responses.products.ProductUpdateResponse;
import com.zenfulcode.commercify.commercify.dto.ProductDTO;
import com.zenfulcode.commercify.commercify.dto.ProductUpdateResult;
import com.zenfulcode.commercify.commercify.dto.ProductVariantEntityDto;
import com.zenfulcode.commercify.commercify.exception.InvalidSortFieldException;
import com.zenfulcode.commercify.commercify.exception.ProductDeletionException;
import com.zenfulcode.commercify.commercify.exception.ProductNotFoundException;
import com.zenfulcode.commercify.commercify.exception.ProductValidationException;
import com.zenfulcode.commercify.commercify.service.ProductService;
import com.zenfulcode.commercify.commercify.viewmodel.ProductVariantViewModel;
import com.zenfulcode.commercify.commercify.viewmodel.ProductViewModel;
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
    private final PagedResourcesAssembler<ProductViewModel> pagedProductResourceAssembler;
    private final PagedResourcesAssembler<ProductVariantViewModel> pageVariantResourceAssembler;

    private static final Set<String> VALID_SORT_FIELDS = Set.of(
            "id", "name", "stock", "createdAt", "updatedAt"
    );

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<ProductViewModel>>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        validateSortField(sortBy);
        Sort.Direction direction = Sort.Direction.fromString(sortDirection.toUpperCase());
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<ProductViewModel> products = productService.getAllProducts(pageRequest).map(ProductViewModel::fromDTO);

        return ResponseEntity.ok(pagedProductResourceAssembler.toModel(products));
    }

    @GetMapping("/active")
    public ResponseEntity<PagedModel<EntityModel<ProductViewModel>>> getActiveProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        validateSortField(sortBy);
        Sort.Direction direction = Sort.Direction.fromString(sortDirection.toUpperCase());
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<ProductViewModel> products = productService.getActiveProducts(pageRequest).map(ProductViewModel::fromDTO);
        return ResponseEntity.ok(pagedProductResourceAssembler.toModel(products));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable Long id) {
        try {
            ProductDTO product = productService.getProductById(id);
            if (product == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(ProductViewModel.fromDTO(product));
        } catch (Exception e) {
            log.error("Error retrieving product", e);
            return ResponseEntity.internalServerError()
                    .body(new ErrorResponse("Error retrieving product: " + e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<?> createProduct(@Validated @RequestBody ProductRequest request) {
        System.out.println("CreateProductRequest: " + request);

        try {
            ProductDTO product = productService.saveProduct(request);
            return ResponseEntity.ok(ProductViewModel.fromDTO(product));
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
            @Validated @RequestBody ProductRequest request
    ) {
        System.out.println("UpdateProductRequest: " + request);

        try {
            ProductUpdateResult result = productService.updateProduct(id, request);
            if (!result.getWarnings().isEmpty()) {
                return ResponseEntity.ok()
                        .body(new ProductUpdateResponse(
                                ProductViewModel.fromDTO(
                                        result.getProduct()
                                ),
                                "Product updated with warnings",
                                result.getWarnings()
                        ));
            }
            return ResponseEntity.ok(ProductViewModel.fromDTO(result.getProduct()));
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
    @PostMapping("/{id}/reactivate")
    public ResponseEntity<?> reactivateProduct(@PathVariable Long id) {
        try {
            productService.reactivateProduct(id);
            return ResponseEntity.ok("Product reactivated");
        } catch (ProductNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error updating product", e);
            return ResponseEntity.internalServerError()
                    .body(new ErrorResponse("Error updating product: " + e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivateProduct(@PathVariable Long id) {
        try {
            productService.deactivateProduct(id);
            return ResponseEntity.ok("Product deactivated");
        } catch (ProductNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error updating product", e);
            return ResponseEntity.internalServerError()
                    .body(new ErrorResponse("Error updating product: " + e.getMessage()));
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
    @PostMapping("/{productId}/variants")
    public ResponseEntity<?> addVariantToProduct(
            @PathVariable Long productId,
            @Validated @RequestBody ProductVariantRequest request
    ) {
        try {
            ProductDTO product = productService.addVariantToProduct(productId, request);
            return ResponseEntity.ok(ProductViewModel.fromDTO(product));
        } catch (ProductNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (ProductValidationException e) {
            return ResponseEntity.badRequest().body(new ValidationErrorResponse(e.getErrors()));
        } catch (Exception e) {
            log.error("Error adding variant to product", e);
            return ResponseEntity.internalServerError()
                    .body(new ErrorResponse("Error adding variant: " + e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{productId}/variants/{variantId}")
    public ResponseEntity<?> updateProductVariant(
            @PathVariable Long productId,
            @PathVariable Long variantId,
            @Validated @RequestBody ProductVariantRequest request
    ) {
        try {
            ProductDTO product = productService.updateVariant(productId, variantId, request);
            return ResponseEntity.ok(ProductViewModel.fromDTO(product));
        } catch (ProductNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (ProductValidationException e) {
            return ResponseEntity.badRequest().body(new ValidationErrorResponse(e.getErrors()));
        } catch (Exception e) {
            log.error("Error updating product variant", e);
            return ResponseEntity.internalServerError()
                    .body(new ErrorResponse("Error updating variant: " + e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{productId}/variants/{variantId}")
    public ResponseEntity<?> deleteProductVariant(
            @PathVariable Long productId,
            @PathVariable Long variantId
    ) {
        try {
            productService.deleteVariant(productId, variantId);
            return ResponseEntity.ok().build();
        } catch (ProductNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error deleting product variant", e);
            return ResponseEntity.internalServerError()
                    .body(new ErrorResponse("Error deleting variant: " + e.getMessage()));
        }
    }

    @GetMapping("/{productId}/variants")
    public ResponseEntity<?> getProductVariants(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        try {
            validateSortField(sortBy);
            Sort.Direction direction = Sort.Direction.fromString(sortDirection.toUpperCase());
            PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortBy));

            Page<ProductVariantViewModel> variants = productService.getProductVariants(productId, pageRequest)
                    .map(ProductVariantViewModel::fromDTO);

            return ResponseEntity.ok(pageVariantResourceAssembler.toModel(variants));
        } catch (ProductNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error retrieving product variants", e);
            return ResponseEntity.internalServerError()
                    .body(new ErrorResponse("Error retrieving variants: " + e.getMessage()));
        }
    }

    @GetMapping("/{productId}/variants/{variantId}")
    public ResponseEntity<?> getProductVariant(
            @PathVariable Long productId,
            @PathVariable Long variantId
    ) {
        try {
            ProductVariantEntityDto variant = productService.getProductVariant(productId, variantId);
            return ResponseEntity.ok(ProductVariantViewModel.fromDTO(variant));
        } catch (ProductNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error retrieving product variant", e);
            return ResponseEntity.internalServerError()
                    .body(new ErrorResponse("Error retrieving variant: " + e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{productId}/variants/{variantId}/stock")
    public ResponseEntity<?> updateVariantStock(
            @PathVariable Long productId,
            @PathVariable Long variantId,
            @RequestBody Integer stock
    ) {
        try {
            ProductDTO product = productService.updateVariantStock(productId, variantId, stock);
            return ResponseEntity.ok(ProductViewModel.fromDTO(product));
        } catch (ProductNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error updating variant stock", e);
            return ResponseEntity.internalServerError()
                    .body(new ErrorResponse("Error updating variant stock: " + e.getMessage()));
        }
    }

    private void validateSortField(String sortBy) {
        if (!VALID_SORT_FIELDS.contains(sortBy)) {
            throw new InvalidSortFieldException("Invalid sort field: " + sortBy);
        }
    }
}
