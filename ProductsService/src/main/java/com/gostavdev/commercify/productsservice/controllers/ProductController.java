package com.gostavdev.commercify.productsservice.controllers;

import com.gostavdev.commercify.productsservice.requests.ProductRequest;
import com.gostavdev.commercify.productsservice.responses.ProductDeleteResponse;
import com.gostavdev.commercify.productsservice.services.ProductService;
import com.gostavdev.commercify.productsservice.dto.ProductDTO;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@AllArgsConstructor
public class ProductController {
    private final ProductService productService;
    private final PagedResourcesAssembler<ProductDTO> pagedResourcesAssembler;

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
    public ResponseEntity<ProductDTO> createProduct(@RequestBody ProductRequest request) {
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
    public ResponseEntity<List<ProductDTO>> createBatchProducts(@RequestBody List<ProductRequest> request) {
        List<ProductDTO> products = productService.saveProducts(request);
        return ResponseEntity.ok(products);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ProductDeleteResponse> deleteProduct(@PathVariable Long id) {
        try {
            boolean deleted = productService.deleteProduct(id, false);
            return ResponseEntity.ok(new ProductDeleteResponse(deleted));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ProductDeleteResponse(false, e.getMessage()));
        }
    }

    @DeleteMapping("/{id}/force")
    public ResponseEntity<ProductDeleteResponse> forceDeleteProduct(@PathVariable Long id) {
        try {
            boolean deleted = productService.deleteProduct(id, true);
            return ResponseEntity.ok(new ProductDeleteResponse(deleted));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ProductDeleteResponse(false, e.getMessage()));
        }
    }
}
