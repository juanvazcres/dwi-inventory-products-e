package com.unir.products.service;

import com.unir.products.model.db.Product;
import com.unir.products.model.request.CreateProductRequest;
import com.unir.products.model.response.ProductsQueryResponse;

public interface ProductsService {

	ProductsQueryResponse getProducts(String title, String category, String description, Boolean aggregate);
	
	Product getProduct(String productId);
	
	Boolean removeProduct(String productId);
	
	Product createProduct(CreateProductRequest request);

}
