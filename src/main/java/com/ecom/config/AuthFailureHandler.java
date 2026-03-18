package com.ecom.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import com.ecom.model.UserDtls;
import com.ecom.repository.UserRepository;
import com.ecom.service.UserService;
import com.ecom.util.AppConstant;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
@Component
public class AuthFailureHandler extends SimpleUrlAuthenticationFailureHandler {
	@Autowired
	private UserService service;
	@Autowired
	private UserRepository repository;
	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException, ServletException {
			String email = request.getParameter("username");
			UserDtls userDtls = repository.findByEmail(email);
			if(userDtls != null) {
			
			
			
			if(userDtls.getIsEnable()) {
				if(userDtls.getAccountNonLocked()) {
					if(userDtls.getFailedAttempt()<=AppConstant.ATTEMPT_TIME) {
						service.increaseFailedAttempt(userDtls);
					}else {
						service.userAccountLock(userDtls);
						exception = new LockedException("your account is locked!! failed attempte 3");
					}
					
				}else {
					if(service.unlockAccountTimeExpired(userDtls)) {
						exception = new LockedException("your account is unlocked !!please try again");	
						
					}else {
					exception = new LockedException("your account is locked !! try after sometime");}
				}
			}else {
				exception = new LockedException("your account is locked");}}
						
						
			else {
	
						
						
				exception = new LockedException("Email & password invalid");
						
						
						
						
			}
		super.setDefaultFailureUrl("/signin?error");
		super.onAuthenticationFailure(request, response, exception);
	}

}
