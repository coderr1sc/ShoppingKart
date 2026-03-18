package com.ecom.service.impl;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ecom.model.UserDtls;
import com.ecom.repository.UserRepository;
import com.ecom.service.FileService;
import com.ecom.service.UserService;
import com.ecom.util.AppConstant;
import com.ecom.util.CommonUtil;

@Service
public class UserServiceImpl implements UserService {
	@Autowired
    private CommonUtil commonUtil;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private PasswordEncoder passwordEncoder;
	@Autowired
	private FileService fileService;
   
	
	@Override
	public UserDtls save(UserDtls user) {
		user.setRole("ROLE_USER");
		user.setIsEnable(true);
		user.setAccountNonLocked(true);
		user.setFailedAttempt(0);
		user.setLockTime(null);
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		UserDtls saveUser = userRepository.save(user);
		return saveUser;
	}

	@Override
	public UserDtls getUserByEmail(String email) {
		
		return userRepository.findByEmail(email);
	}

	@Override
	public List<UserDtls> getUsers(String role) {
		
		return userRepository.findByRole(role);
	}

	@Override
	public Boolean updateAccountStatus(Integer id, Boolean status) {
		
		Optional<UserDtls> findByUser = userRepository.findById(id);
		if(findByUser.isPresent()) {
			
			
			UserDtls userDtls = findByUser.get();
			userDtls.setIsEnable(status);
			userRepository.save(userDtls);
			return true;
		}
		return false;
		
		
	}

	@Override
	public void increaseFailedAttempt(UserDtls user) {
			int attempt =user.getFailedAttempt()+1;
			user.setFailedAttempt(attempt);
			userRepository.save(user);
	}

	@Override
	public void userAccountLock(UserDtls user) {
		// TODO Auto-generated method stub
		user.setAccountNonLocked(false);
		user.setLockTime(new Date());
		userRepository.save(user);
	}

	@Override
	public boolean unlockAccountTimeExpired(UserDtls user) {
		
		long lockTime = user.getLockTime().getTime();
		long unlockTime = lockTime + AppConstant.UNLOCK_DURATION_TIME;
		long currentTime = System.currentTimeMillis();
		
		if(unlockTime < currentTime) {
			user.setAccountNonLocked(true);
			user.setFailedAttempt(0);
			user.setLockTime(null);
			userRepository.save(user);
			return true;
		}
		return false;
	}

	@Override
	public void resetAttempt(int userId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateUserResetToken(String email, String resetToken) {
		// TODO Auto-generated method stub
		
		UserDtls byEmail = userRepository.findByEmail(email);
		byEmail.setResetToken(resetToken);
		userRepository.save(byEmail);
	}

	@Override
	public UserDtls getUserByToken(String token) {
		// TODO Auto-generated method stub
		return userRepository.findByResetToken(token);
	}

	@Override
	public UserDtls updateUser(UserDtls user) {
		// TODO Auto-generated method stub
		return userRepository.save(user);
	}

	@Override
	public UserDtls updateUserProfile(UserDtls user, MultipartFile img) {
		// TODO Auto-generated method stub
		
		UserDtls dbUser = userRepository.findById(user.getId()).get();
		
		if(!img.isEmpty()) {
			String imageUrl = commonUtil.getImageUrl(img, 3);
			
			dbUser.setProfileImage(imageUrl);
		}
		if(dbUser != null)
		{
			dbUser.setName(user.getName());
			dbUser.setMobileNumber(user.getMobileNumber());
			dbUser.setAddress(user.getAddress());
			dbUser.setCity(user.getCity());
			dbUser.setState(user.getState());
			dbUser.setPincode(user.getPincode());
			dbUser.setState(user.getState());
			userRepository.save(dbUser);
		}
		try {
			if (!img.isEmpty()) {
				/*
				 * File saveFile = new ClassPathResource("static/img").getFile();
				 * 
				 * Path path = Paths.get(saveFile.getAbsolutePath() + File.separator +
				 * "profile_img" + File.separator + img.getOriginalFilename());
				 * 
				 * //System.out.println(path); Files.copy(img.getInputStream(), path,
				 * StandardCopyOption.REPLACE_EXISTING);
				 */
				fileService.uploadFileS3(img, 3);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dbUser;
	}

	@Override
	public UserDtls saveAdmin(UserDtls user) {
		// TODO Auto-generated method stub
		user.setRole("ROLE_ADMIN");
		user.setIsEnable(true);
		user.setAccountNonLocked(true);
		user.setFailedAttempt(0);
		user.setLockTime(null);
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		UserDtls saveUser = userRepository.save(user);
		return saveUser;
	}

	@Override
	public List<UserDtls> getUsers() {
		// TODO Auto-generated method stub
		return userRepository.findAll();
	}

	@Override
	public Boolean existEmail(String email) {
		// TODO Auto-generated method stub
		return userRepository.existsByEmail(email);
	}

}
