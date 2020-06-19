package com.identicum.iam.identity.repo;

import java.util.Optional;

import com.identicum.iam.identity.models.Product;

import org.springframework.data.repository.CrudRepository;

public interface ProductRepository extends CrudRepository<Product, Long> {

	@Override
	Iterable<Product> findAll();

	@Override
	Optional<Product> findById(Long id);
}
