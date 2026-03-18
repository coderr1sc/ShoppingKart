package com.ecom.service.impl;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import com.ecom.model.Product;
import com.ecom.repository.ProductRepository;
import com.ecom.service.FileService;
import com.ecom.service.ProductService;
import com.ecom.util.CommonUtil;
@Service
public class ProductServiceImpl implements ProductService {
    
	@Autowired
	private CommonUtil commonUtil;
	@Autowired
	private ProductRepository productRepository;
	@Autowired
	private FileService fileService;
	@Override
	public Product saveProduct(Product product) {
		// TODO Auto-generated method stub
		
		return productRepository.save(product);
	}

	@Override
	public List<Product> getAllProducts() {
		// TODO Auto-generated method stub
		return productRepository.findAll();
	}

	@Override
	public Boolean deleteProduct(Integer id) {
		// TODO Auto-generated method stub
		Product product = productRepository.findById(id).orElse(null);
		if(!ObjectUtils.isEmpty(product)) {
			productRepository.delete(product);
			return true;
		}
		
		return false;
	}

	@Override
	public Product getProductById(Integer id) {
		
		Product product = productRepository.findById(id).orElse(null);
		return product;
	}

	@Override
	public Product updateProduct(Product product, MultipartFile image) {
		
		
		Product dbProduct = this.getProductById(product.getId());
		//String imageName = image.isEmpty() ? dbProduct.getImage():image.getOriginalFilename();
		String imageUrl = commonUtil.getImageUrl(image, 2);

		

		dbProduct.setTitle(product.getTitle());
		dbProduct.setDescription(product.getDescription());
		dbProduct.setCategory(product.getCategory());
		dbProduct.setPrice(product.getPrice());
		dbProduct.setStock(product.getStock());
		dbProduct.setImage(imageUrl);
		dbProduct.setIsActive(product.getIsActive());
		dbProduct.setDiscount(product.getDiscount());
		Double discount = product.getPrice()*(product.getDiscount()/100.0);
		Double discountPrice = product.getPrice()-discount;
		dbProduct.setDiscountPrice(discountPrice);
		Product updateProduct = productRepository.save(dbProduct);
		
		if(!ObjectUtils.isEmpty(updateProduct)) {
			if(!image.isEmpty()) {
			try {
				/*
				 * File saveFile = new ClassPathResource("static/img").getFile(); Path path =
				 * Paths.get(saveFile.getAbsolutePath()+File.separator+"product_img"+File.
				 * separator+image.getOriginalFilename()); System.out.println(path);
				 * Files.copy(image.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
				 */
				fileService.uploadFileS3(image, 2);	
			}catch(Exception e) {
				e.printStackTrace();
			}
			}
			return product;
		}
		return null;
	}

	@Override
	public List<Product> getAllActiveProducts(String category) {
		// TODO Auto-generated method stub
		List<Product> products = null;
		
		if(ObjectUtils.isEmpty(category)) {
			products = productRepository.findByIsActiveTrue();}
		else {
			products = productRepository.findByCategory(category);
		}
		return products;
	}

	@Override
	public List<Product> searchProduct(String ch) {
		// TODO Auto-generated method stub
		
		return productRepository.findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(ch, ch);
	}

	@Override
	public Page<Product> getAllActiveProductPagination(Integer pageNo, Integer pageSize, String category) {

		Pageable pageable = PageRequest.of(pageNo, pageSize);
		Page<Product> pageProduct = null;

		if (ObjectUtils.isEmpty(category)) {
			pageProduct = productRepository.findByIsActiveTrue(pageable);
		} else {
			pageProduct = productRepository.findByCategory(pageable, category);
		}
		return pageProduct;
	}

	@Override
	public Page<Product> searchProductPagination(Integer pageNo, Integer pageSize, String ch) {

		Pageable pageable = PageRequest.of(pageNo, pageSize);
		return productRepository.findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(ch, ch, pageable);
		
	}

	@Override
	public Page<Product> getAllProductsPagination(Integer pageNo, Integer pageSize) {
		
		Pageable pageable = PageRequest.of(pageNo, pageSize);// TODO Auto-generated method stub
		return productRepository.findAll(pageable);
	}

}
