package com.identicum.identity.demoapi.controllers;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.identicum.identity.demoapi.configuration.SecurityConfiguration;
import com.identicum.identity.demoapi.models.Product;
import com.identicum.identity.demoapi.repositories.ProductRepository;

@WebMvcTest(ProductController.class)
@Import(SecurityConfiguration.class)
class ProductControllerTest {

	private static final String READ_SCOPE = "SCOPE_api.identicum.com/products:read";

	@Autowired
	MockMvc mvc;

	@MockitoBean
	ProductRepository productRepository;

	@MockitoBean
	JwtDecoder jwtDecoder;

	@Test
	void productsWithoutTokenIsUnauthorized() throws Exception {
		mvc.perform(get("/api/v1/products")).andExpect(status().isUnauthorized());
	}

	@Test
	void productsWithoutScopeIsForbidden() throws Exception {
		mvc.perform(get("/api/v1/products").with(jwt())).andExpect(status().isForbidden());
	}

	@Test
	void productByIdWithoutScopeIsForbidden() throws Exception {
		mvc.perform(get("/api/v1/products/1").with(jwt())).andExpect(status().isForbidden());
	}

	@Test
	void productsWithScopeReturnsList() throws Exception {
		given(productRepository.findAll()).willReturn(List.of(product(1L, "Phone")));

		mvc.perform(get("/api/v1/products").with(readScope()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].name").value("Phone"));
	}

	@Test
	void productByIdWithScopeReturnsProduct() throws Exception {
		given(productRepository.findById(1L)).willReturn(Optional.of(product(1L, "Phone")));

		mvc.perform(get("/api/v1/products/1").with(readScope()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(1));
	}

	@Test
	void unknownProductIsNotFound() throws Exception {
		given(productRepository.findById(99L)).willReturn(Optional.empty());

		mvc.perform(get("/api/v1/products/99").with(readScope())).andExpect(status().isNotFound());
	}

	private static RequestPostProcessor readScope() {
		return jwt().authorities(new SimpleGrantedAuthority(READ_SCOPE));
	}

	private static Product product(Long id, String name) {
		Product product = new Product();
		product.setId(id);
		product.setName(name);
		return product;
	}
}
