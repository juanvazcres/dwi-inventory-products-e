package com.unir.products.model.db;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Document(indexName = "products", createIndex = true)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class Product {
	
	@Id
	private String id;

	@Field(type = FieldType.Text, name = "title")
	private String title;

	@Field(type = FieldType.Double, name = "price")
	private Double price;

	@Field(type = FieldType.Text, name = "description")
	private String description;

	@Field(type = FieldType.Text, name = "category")
	private String category;

	@Field(type = FieldType.Text, name = "image")
	private String image;

}
