package com.nextbar.bar.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nextbar.bar.model.Product;
import com.nextbar.bar.repository.ProductRepository;

import java.util.List;

/**
 * REST controller for managing products.
 * Provides endpoints for retrieving product information.
 */
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductRepository productRepository;

    /**
     * Get all products.
     *
     * @return list of all products
     */
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productRepository.findAll());
    }
}
