package com.identicum.identity.demoapi.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.identicum.identity.demoapi.models.Product;
import com.identicum.identity.demoapi.repositories.ProductRepository;

@RestController
@RequestMapping("/api/v1")
public class ProductController {

	private final ProductRepository productRepository;

	public ProductController(ProductRepository productRepository) {
		this.productRepository = productRepository;
	}

	@GetMapping("/products")
	public Iterable<Product> index() {
		return this.productRepository.findAll();
	}

	@GetMapping("/products/{id}")
	public ResponseEntity<Product> findById(@PathVariable("id") Long id) {
		return ResponseEntity.of(this.productRepository.findById(id));
	}

}
