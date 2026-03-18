package com.ecom.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecom.model.Cart;
import com.ecom.model.Product;
import com.ecom.model.UserDtls;
import com.ecom.repository.CartServiceRepository;
import com.ecom.repository.ProductRepository;
import com.ecom.repository.UserRepository;
import com.ecom.service.CartService;

@Service
public class CartServiceImpl implements CartService{

    
	
	@Autowired
	private CartServiceRepository cartrepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ProductRepository productRepository;

    
	
	@Override
	public Cart saveCart(Integer productId, Integer userId) {
		// TODO Auto-generated method stub
		
		UserDtls userDtls = userRepository.findById(userId).get();
		
		Product product = productRepository.findById(productId).get();
		Cart cartStatus = cartrepository.findByProductIdAndUserId(productId,  userId);
		Cart cart = null;
		
		if(cartStatus == null) {
			
			cart = new Cart();
			cart.setProduct(product);
			cart.setUser(userDtls);
			cart.setQuantity(1);
			cart.setTotalPrice(1 * product.getDiscountPrice());
		}else {
			
			cart= cartStatus;
			cart.setQuantity(cart.getQuantity()+1);
			cart.setTotalPrice(cart.getQuantity()* cart.getProduct().getDiscountPrice());
		}
		Cart saveCart = cartrepository.save(cart);
		return saveCart;
	}

	@Override
	public List<Cart> getCartByUser(Integer userId) {
		List<Cart> carts = cartrepository.findByUserId(userId);
		Double totalOrderPrice = 0.0;
		List<Cart> updateCarts = new ArrayList<>();
		for(Cart c : carts) {
			
			Double totalPrice = (c.getProduct().getDiscountPrice()*c.getQuantity());
			
			c.setTotalPrice(totalPrice);
			totalOrderPrice +=totalPrice;
			updateCarts.add(c);
			c.setTotalOrderPrice(totalOrderPrice);
		}
		
		return updateCarts;
	}

	@Override
	public Integer getCountCart(Integer userId) {
		
		
		
		return cartrepository.countByUserId(userId);
		
		
	}

	@Override
	public void updateQuantity(String sy, Integer id) {
		Cart cart = cartrepository.findById(id).get();
		Integer updateQuantity;
		if(sy.equalsIgnoreCase("de")) {
			updateQuantity = cart.getQuantity()-1;
			
			if(updateQuantity <= 0) {
				cartrepository.deleteById(id);
				return;
			
			}
			}else {
				updateQuantity = cart.getQuantity()+1;
				
			}
		cart.setQuantity(updateQuantity);
		cartrepository.save(cart); 
		
		
		
	}

}
