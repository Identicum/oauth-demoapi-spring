package com.identicum.identity.demoapi.controllers;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.identicum.identity.demoapi.models.Product;
import com.identicum.identity.demoapi.repositories.ProductRepository;

@RestController
@RequestMapping("/api/v1")
public class ProductController {
    @Autowired
	ProductRepository productRepository;
	
    @GetMapping(value = {"/products"})
	public Iterable<Product> index(){
		return this.productRepository.findAll();
	}

	@GetMapping(value = {"/products/{id}"})
	public Optional<Product> findByid(@PathVariable("id") Long id){

		return this.productRepository.findById(id);
	}

}
