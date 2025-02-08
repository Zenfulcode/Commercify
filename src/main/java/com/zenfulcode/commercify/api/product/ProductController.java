package com.zenfulcode.commercify.api.product;

import com.zenfulcode.commercify.api.product.dto.request.*;
import com.zenfulcode.commercify.api.product.dto.response.CreateProductResponse;
import com.zenfulcode.commercify.api.product.dto.response.PagedProductResponse;
import com.zenfulcode.commercify.api.product.dto.response.ProductDetailResponse;
import com.zenfulcode.commercify.api.product.dto.response.UpdateProductResponse;
import com.zenfulcode.commercify.api.product.mapper.ProductDtoMapper;
import com.zenfulcode.commercify.api.product.mapper.ProductResponseMapper;
import com.zenfulcode.commercify.product.application.command.*;
import com.zenfulcode.commercify.product.application.query.ProductQuery;
import com.zenfulcode.commercify.product.application.service.ProductApplicationService;
import com.zenfulcode.commercify.product.domain.model.Product;
import com.zenfulcode.commercify.product.domain.valueobject.ProductId;
import com.zenfulcode.commercify.shared.interfaces.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v2/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductApplicationService productApplicationService;
    private final ProductDtoMapper dtoMapper;
    private final ProductResponseMapper responseMapper;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CreateProductResponse>> createProduct(
            @Validated @RequestBody CreateProductRequest request) {

        // Map request to command
        CreateProductCommand command = dtoMapper.toCommand(request);

        // Execute use case
        ProductId productId = productApplicationService.createProduct(command);

        // Return response
        CreateProductResponse response = new CreateProductResponse(
                productId.getId(),
                "Product created successfully"
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getProduct(
            @PathVariable String productId) {

        Product product = productApplicationService.getProduct(ProductId.of(productId));
        ProductDetailResponse response = dtoMapper.toDetailResponse(product);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PagedProductResponse>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        Sort.Direction direction = Sort.Direction.fromString(sortDirection.toUpperCase());
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<Product> products = productApplicationService.findProducts(
                ProductQuery.all(),
                pageRequest
        );

        PagedProductResponse response = responseMapper.toPagedResponse(products);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<PagedProductResponse>> getActiveProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        Sort.Direction direction = Sort.Direction.fromString(sortDirection.toUpperCase());
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<Product> products = productApplicationService.findProducts(
                ProductQuery.active(),
                pageRequest
        );

        PagedProductResponse response = responseMapper.toPagedResponse(products);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UpdateProductResponse>> updateProduct(
            @PathVariable String productId,
            @RequestBody UpdateProductRequest request) {

        UpdateProductCommand command = dtoMapper.toCommand(ProductId.of(productId), request);
        productApplicationService.updateProduct(command);

        UpdateProductResponse response = new UpdateProductResponse(
                "Product updated successfully"
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{productId}/inventory")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> adjustInventory(
            @PathVariable String productId,
            @RequestBody AdjustInventoryRequest request) {

        AdjustInventoryCommand command = dtoMapper.toCommand(ProductId.of(productId), request);
        productApplicationService.adjustInventory(command);

        return ResponseEntity.ok(ApiResponse.success("Inventory adjusted successfully"));
    }

    @PostMapping("/{productId}/variants")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> addVariants(
            @PathVariable String productId,
            @RequestBody AddVariantsRequest request) {

        AddProductVariantsCommand command = dtoMapper.toCommand(ProductId.of(productId), request);
        productApplicationService.addProductVariants(command);

        return ResponseEntity.ok(ApiResponse.success("Variants added successfully"));
    }

    @PutMapping("/{productId}/variants/prices")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> updateVariantPrices(
            @PathVariable String productId,
            @RequestBody UpdateVariantPricesRequest request) {

        UpdateVariantPricesCommand command = dtoMapper.toCommand(ProductId.of(productId), request);
        productApplicationService.updateVariantPrices(command);

        return ResponseEntity.ok(ApiResponse.success("Variant prices updated successfully"));
    }

    @PostMapping("/{productId}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deactivateProduct(@PathVariable String productId) {
        DeactivateProductCommand command = new DeactivateProductCommand(ProductId.of(productId));
        productApplicationService.deactivateProduct(command);

        return ResponseEntity.ok(ApiResponse.success("Product deactivated successfully"));
    }

    @DeleteMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteProduct(@PathVariable String productId) {
        DeleteProductCommand command = new DeleteProductCommand(ProductId.of(productId));
        productApplicationService.deleteProduct(command);

        return ResponseEntity.ok(ApiResponse.success("Product deleted successfully"));
    }
}