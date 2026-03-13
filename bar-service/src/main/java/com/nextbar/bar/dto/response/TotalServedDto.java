package com.nextbar.bar.dto.response;

/**
 * DTO representing total servings per product.
 * Matches frontend expectation: { name, total }.
 */
public record TotalServedDto(String name, int total) {
}
