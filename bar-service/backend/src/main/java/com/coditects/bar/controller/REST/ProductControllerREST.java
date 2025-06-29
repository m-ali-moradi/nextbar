package com.coditects.bar.controller.REST;

import java.util.List;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.coditects.bar.model.Product;
import com.coditects.bar.repository.ProductRepository;

/**
 * ProductControllerREST is a REST controller for managing products.
 * It provides endpoints for creating, retrieving, updating, and deleting products.
 * This class is part of the bar management system.
 */
@RestController
@RequestMapping("/bars/products")
//@CrossOrigin(origins = "http://localhost:5173") // Allow cross-origin requests from the frontend application
public class ProductControllerREST {

    private final ProductRepository productRepository;

    public ProductControllerREST(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // getAllProducts
   @GetMapping
   public List<Product> getProducts() {
       return productRepository.findAll();
   }

}
