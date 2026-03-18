package com.ecom.model;


import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
public class UserDtls {

	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	@NotBlank(message = "Name cannot be empty !!")
	private String name;
	@NotBlank(message = "Phone Number cannot be empty !!")
	private String mobileNumber;
	@NotBlank(message = "email cannot be empty !!")
	@Email(message = "Invalid Email !!")
	private String email;
	@NotBlank(message = "Address cannot be empty !!")
	private String address;
	@NotBlank(message = "City cannot be empty !!")
	private String city;
	@NotBlank(message = "State cannot be empty !!")
	private String state;
	@NotBlank(message = "Pincode cannot be empty !!")
	private String pincode;
	@NotBlank(message = "Password cannot be empty !!")
	private String password;
	
	private String profileImage;
	private String role;
	
	private Boolean isEnable;
	private Boolean accountNonLocked;
	private Integer failedAttempt;
	private Date lockTime;
	private String resetToken;
	
}
