package com.ecom.service.impl;

import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.ecom.model.Cart;
import com.ecom.model.OrderAddress;
import com.ecom.model.OrderRequest;
import com.ecom.model.ProductOrder;
import com.ecom.repository.CartServiceRepository;
import com.ecom.repository.ProductOrderRepository;
import com.ecom.service.OrderService;
import com.ecom.util.CommonUtil;
import com.ecom.util.OrderStatus;

import jakarta.mail.MessagingException;

@Service
public class OrderServiceImpl implements OrderService{

    private final CommonUtil commonUtil;

	@Autowired
	private ProductOrderRepository orderRepository;
	@Autowired
	private CartServiceRepository cartRepository;

    OrderServiceImpl(CommonUtil commonUtil) {
        this.commonUtil = commonUtil;
    }
	@Override
	public void saveOrder(Integer userId, OrderRequest orderRequest) throws UnsupportedEncodingException, MessagingException {

		List<Cart> carts = cartRepository.findByUserId(userId);
		for(Cart cart : carts ) {
			
			ProductOrder order = new ProductOrder();

			order.setOrderId(UUID.randomUUID().toString());
			order.setOrderDate(LocalDate.now());

			order.setProduct(cart.getProduct());
			order.setPrice(cart.getProduct().getDiscountPrice());

			order.setQuantity(cart.getQuantity());
			order.setUser(cart.getUser());

			order.setStatus(OrderStatus.IN_PROGRESS.getName());
			order.setPaymentType(orderRequest.getPaymentType());

			OrderAddress address = new OrderAddress();
			address.setFirstName(orderRequest.getFirstName());
			address.setLastName(orderRequest.getLastName());
			address.setEmail(orderRequest.getEmail());
			address.setMobileNo(orderRequest.getMobileNo());
			address.setAddress(orderRequest.getAddress());
			address.setCity(orderRequest.getCity());
			address.setState(orderRequest.getState());
			address.setPincode(orderRequest.getPincode());

			order.setOrderAddress(address);

			ProductOrder saveOrder = orderRepository.save(order);
			//resetCart(cart.getUser());
			commonUtil.sendMailforProductOrder(saveOrder, "Success");
			
		}
		
		
	}
	@Override
	public List<ProductOrder> getOrderByUser(Integer userId) {
		List<ProductOrder> orders = orderRepository.findByUserId(userId);
		return orders;
	}
	@Override
	public ProductOrder updateOrderStatus(Integer id, String status) {
		// TODO Auto-generated method stub
		Optional<ProductOrder> findById = orderRepository.findById(id);
		if(findById.isPresent()) {
			
			ProductOrder productOrder = findById.get();
			productOrder.setStatus(status);
			ProductOrder updateOrder = orderRepository.save(productOrder);
			return updateOrder;
		}
		return null;
	}
	@Override
	public List<ProductOrder> getAllOrders() {
		
		// TODO Auto-generated method stub
		return orderRepository.findAll();
	}
	@Override
	public ProductOrder getOrdersByOrderId(String orderId) {
		// TODO Auto-generated method stub
		return orderRepository.findByOrderId(orderId);
	}
	@Override
	public Page<ProductOrder> getAllOrdersPagination(Integer PageNo, Integer pageSize) {
		// TODO Auto-generated method stub
Pageable pageable =	PageRequest.of(PageNo, pageSize);
		return orderRepository.findAll(pageable);
	}

}
