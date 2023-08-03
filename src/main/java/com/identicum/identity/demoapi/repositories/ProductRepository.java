package com.identicum.identity.demoapi.repositories;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.identicum.identity.demoapi.models.Product;

public interface ProductRepository extends CrudRepository<Product, Long>  {
    
    @Override
	Iterable<Product> findAll();

	@Override
	Optional<Product> findById(Long id);
}



