package com.nextbar.bar.model.dto;

/**
 * DTO representing total servings per product.
 * Matches frontend expectation: { name, total }.
 */
public record TotalServedDto(String name, int total) {}
