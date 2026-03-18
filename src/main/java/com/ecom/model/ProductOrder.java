package com.ecom.model;

import java.sql.Date;
import java.time.LocalDate;

import org.hibernate.annotations.ManyToAny;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ProductOrder {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	private String orderId;
	
	private LocalDate orderDate;
	@ManyToOne
	private Product product;
	
	private Double price;
	private Integer quantity;
	@ManyToOne
	private UserDtls user;
	private String status;
	private String paymentType;
	@OneToOne(cascade = CascadeType.ALL,fetch = FetchType.EAGER)
	private OrderAddress orderAddress;
}
