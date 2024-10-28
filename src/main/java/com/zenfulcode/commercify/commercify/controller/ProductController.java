package com.zenfulcode.commercify.commercify.controller;


import com.zenfulcode.commercify.commercify.api.requests.CreateProductRequest;
import com.zenfulcode.commercify.commercify.api.responses.DeleteProductResponse;
import com.zenfulcode.commercify.commercify.dto.ProductDTO;
import com.zenfulcode.commercify.commercify.service.ProductService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@AllArgsConstructor
public class ProductController {
    private final ProductService productService;
    private final PagedResourcesAssembler<ProductDTO> pagedResourcesAssembler;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<ProductDTO>>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "productId") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
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
        Sort.Direction direction = Sort.Direction.fromString(sortDirection.toUpperCase());
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<ProductDTO> products = productService.getActiveProducts(pageRequest);

        return ResponseEntity.ok(pagedResourcesAssembler.toModel(products));
    }

    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@RequestBody CreateProductRequest request) {
        ProductDTO product = productService.saveProduct(request);
        return ResponseEntity.ok(product);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        ProductDTO product = productService.getProductById(id);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(product);
    }

    @PostMapping("/batch")
    public ResponseEntity<List<ProductDTO>> createBatchProducts(@RequestBody List<CreateProductRequest> request) {
        List<ProductDTO> products = productService.saveProducts(request);
        return ResponseEntity.ok(products);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<DeleteProductResponse> deleteProduct(@PathVariable Long id) {
        try {
            boolean deleted = productService.deleteProduct(id);
            return ResponseEntity.ok(new DeleteProductResponse(deleted));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new DeleteProductResponse(false, e.getMessage()));
        }
    }

    @DeleteMapping("/deactivate/{id}")
    public ResponseEntity<String> deactivateProduct(@PathVariable Long id) {
        try {
            productService.deactivateProduct(id);
            return ResponseEntity.ok("Product deactivated");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
