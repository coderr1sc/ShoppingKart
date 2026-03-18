package com.ecom.service;

import java.awt.print.Pageable;
import java.util.List;

import org.springframework.data.domain.Page;

import com.ecom.model.Category;

public interface CategoryService {
	
	
	public Category saveCategory(Category category);
	public Boolean existCategory(String name);
	public List<Category>getAllcategory();
	public Boolean deleteCategory(int id);
	public Category getCategoryById(int id);
	public Page<Category> getAllCategorPagination(Integer pageNo, Integer pageSize);
	public List<Category>getAllActiveCategory();
}
