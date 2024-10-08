package com.unir.products.service;

import com.unir.products.model.response.ProductsQueryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.unir.products.data.DataAccessRepository;
import com.unir.products.model.db.Product;
import com.unir.products.model.request.CreateProductRequest;

@Service
@RequiredArgsConstructor
public class ProductsServiceImpl implements ProductsService {

	private final DataAccessRepository repository;

	@Override
	public ProductsQueryResponse getProducts(String title, String category, String description, Boolean aggregate) {
		//Ahora por defecto solo devolvera productos visibles
		return repository.findProducts(title, description, category, aggregate);
	}

	@Override
	public Product getProduct(String productId) {
		return repository.findById(productId).orElse(null);
	}

	@Override
	public Boolean removeProduct(String productId) {

		Product product = repository.findById(productId).orElse(null);
		if (product != null) {
			repository.delete(product);
			return Boolean.TRUE;
		} else {
			return Boolean.FALSE;
		}
	}

	@Override
	public Product createProduct(CreateProductRequest request) {

		if (request != null && StringUtils.hasLength(request.getTitle().trim())
				&& StringUtils.hasLength(request.getDescription().trim())
				&& StringUtils.hasLength(request.getCategory().trim())) {

			Product product = Product.builder().title(request.getTitle()).description(request.getDescription())
					.category(request.getCategory()).build();

			return repository.save(product);
		} else {
			return null;
		}
	}

}
