package com.ecom.service;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.springframework.data.domain.Page;

import com.ecom.model.OrderRequest;
import com.ecom.model.ProductOrder;

import jakarta.mail.MessagingException;

public interface OrderService {

	
	public void saveOrder(Integer userId, OrderRequest orderRequest) throws UnsupportedEncodingException, MessagingException;
	public ProductOrder updateOrderStatus(Integer id, String status);
	public List<ProductOrder> getOrderByUser(Integer userId);
	public List<ProductOrder> getAllOrders();
	public ProductOrder getOrdersByOrderId(String orderId);
	public Page<ProductOrder>getAllOrdersPagination(Integer PageNo, Integer pageSize);
}
