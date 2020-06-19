package com.identicum.iam.identity.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.identicum.iam.identity.models.Product;
import com.identicum.iam.identity.repo.ProductRepository;

@CrossOrigin
@RestController
@RequestMapping("/api/v1")
public class ProductController
{

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	ProductRepository productRepository;
	
	@GetMapping(value = {"/products"})
	public Iterable<Product> index()
	{
		log.debug("Searching products");
		return this.productRepository.findAll();
	}

}
