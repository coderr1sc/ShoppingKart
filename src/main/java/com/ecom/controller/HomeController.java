package com.ecom.controller;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;


import com.ecom.model.Category;
import com.ecom.model.Product;
import com.ecom.model.UserDtls;
import com.ecom.service.CartService;
import com.ecom.service.CategoryService;
import com.ecom.service.FileService;
import com.ecom.service.ProductService;
import com.ecom.service.UserService;
import com.ecom.util.CommonUtil;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class HomeController {
	@Autowired
	private CategoryService categoryService;
	@Autowired
	private ProductService productService;
	@Autowired
	private UserService userService;
	@Autowired
	private CommonUtil commonUtil;
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	@Autowired
	private FileService fileService;
	@Autowired
	private CartService cartService;
	@ModelAttribute
	public void getUserDetails(Principal p, Model m) {
		
		if(p!=null) {
			
			String email = p.getName();
			UserDtls userDtls = userService.getUserByEmail(email);
			m.addAttribute("user",userDtls);
			Integer countCart = cartService.getCountCart(userDtls.getId());
			
			m.addAttribute("countCart",countCart);
			
		}
		List<Category> category = categoryService.getAllActiveCategory();
		m.addAttribute("category",category);
		
		
	}
	
	
	
	@GetMapping("/home")
	public String index(Model m) {
		
		List<Category> allActiveCategory = categoryService.getAllActiveCategory().stream().limit(6).toList();
		List<Product> allActiveProducts = productService.getAllActiveProducts("").stream()
				.sorted(Comparator.comparing(Product::getId).reversed()).limit(8).toList();
		m.addAttribute("products",allActiveProducts);
		m.addAttribute("category",allActiveCategory);
		return "index1";
	}
	
	@GetMapping("/signin")
	public String login() {
		return "login";
	}
	
	@GetMapping("/register")
	public String register(Model model) {
	model.addAttribute("userDtls", new UserDtls());
		return "register";
	}
	
	
	@GetMapping("/products")
	public String products(Model m, @RequestParam(value = "category", defaultValue = "") String category,
			@RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
			@RequestParam(name = "pageSize", defaultValue = "8") Integer pageSize) {
		List<Category> categories = categoryService.getAllActiveCategory();
		m.addAttribute("paramValue", category);
		m.addAttribute("categories", categories);

//		List<Product> products = productService.getAllActiveProducts(category);
//		m.addAttribute("products", products);

		Page<Product> page = productService.getAllActiveProductPagination(pageNo, pageSize, category);
		List<Product> products = page.getContent();
		m.addAttribute("products", products);
		m.addAttribute("productsSize", products.size());

		m.addAttribute("pageNo", page.getNumber());
		m.addAttribute("pageSize", pageSize);
		m.addAttribute("totalElements", page.getTotalElements());
		m.addAttribute("totalPages", page.getTotalPages());
		m.addAttribute("isFirst", page.isFirst());
		m.addAttribute("isLast", page.isLast());

		return "products";
	}
	@GetMapping("/product/{id}")
	public String viewProduct(@PathVariable int id, Model m) {
		
		Product productById = productService.getProductById(id);
		m.addAttribute("product",productById);
		return "view_product";
		
	}
	
	@PostMapping("/saveUser")
	public String saveUser(@Valid @ModelAttribute UserDtls user, BindingResult result, @RequestParam("img") MultipartFile file, HttpSession session) throws IOException {
		if(result.hasErrors()) {return "register";
		}
		Boolean existEmail = userService.existEmail(user.getEmail());
		if(existEmail) {
			
			session.setAttribute("errorMsg", "User already exists");
		}else {
			
		//String imageName = file.isEmpty() ? "default.jpg" : file.getOriginalFilename();
			String imageUrl = commonUtil.getImageUrl(file, 3);

			user.setProfileImage(imageUrl);
			UserDtls savedUser = userService.save(user);
		
		if(!ObjectUtils.isEmpty(savedUser)) {
			
			if(!file.isEmpty()) {
				//THIS IS FOR UPLOADING IN MYSQL DB
				/* 
				 * File saveFile = new ClassPathResource("static/img").getFile(); Path path =
				 * Paths.get(saveFile.getAbsolutePath()+File.separator+"profile_img"+File.
				 * separator+file.getOriginalFilename()); System.out.println(path);
				 * Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
				 */
			// FOR UPLOAADING IN AWS CLOUD
				fileService.uploadFileS3(file, 3);
			}
			session.setAttribute("succMsg", "Registered Succussfully");

		}else {
			
			session.setAttribute("errorMsg", "Something went wrong");
			
		}
		}
		
		return "redirect:/register";
		
	}
	//forgot password
	
	@GetMapping("/forgot-password")
	public String showForgotPassword() {
		
		
		return "ForgotPassword";
	}
	@PostMapping("/forgot-password")
	public String processForgotPassword(@RequestParam String email,HttpSession session, HttpServletRequest request) throws UnsupportedEncodingException, MessagingException {
		
		UserDtls userByEmail = userService.getUserByEmail(email);
		if(ObjectUtils.isEmpty(userByEmail)) {
			
			
			session.setAttribute("errorMsg", "invalid email");

		}else {
			
			String resetToken = UUID.randomUUID().toString();
			userService.updateUserResetToken(email, resetToken);
			//generate uri = http://localhost:8080/reset-password?token=fsnfmsfnsdmnfdmfsmdnmds
			String url = CommonUtil.generateUrl(request)+"/reset-password?token="+resetToken;
		
			Boolean sendMail = commonUtil.sendMail(url, email);
			if(sendMail) {
			session.setAttribute("succMsg", "email sent, check your mail");
			}
			else {
				session.setAttribute("errorMsg", "something wrong on server, mail not sent");

				
			}
		}
		
		
		
		
		return "redirect:/forgot-password";
	}
	@GetMapping("/reset-password")
	public String resetPassword(@RequestParam String token, HttpSession session, Model m) {
		UserDtls userByToken = userService.getUserByToken(token);
		
		if(userByToken == null) {
			
			m.addAttribute("msg", "Your link is invalid or expired");
			return "message";
		}
		m.addAttribute("token", token);
		return "reset-password";
	}
	
	
	@PostMapping("/reset-password")
	public String resetPassword(@RequestParam String token, @RequestParam String password, HttpSession session, Model m) {
		UserDtls userByToken = userService.getUserByToken(token);
		if(userByToken == null) {
			
			m.addAttribute("errorMsg", "Your link is invalid or expired");
			return "message";
		}else {
			
			
			userByToken.setPassword(passwordEncoder.encode(password));
			userByToken.setResetToken(null);
			userService.updateUser(userByToken);
			session.setAttribute("succMsg", "Password changed successfully");
			m.addAttribute("msg", "password changed successfully");
			return "message";
		}
		
		
		
	}
	@GetMapping("/search")
	public String serachProduct(@RequestParam String ch, Model m) {
		List<Category> categories = categoryService.getAllcategory();
		
		m.addAttribute("categories", categories);
		List<Product> searchProduct = productService.searchProduct(ch);
		m.addAttribute("products",searchProduct);
		m.addAttribute("productsSize", searchProduct.size());
		return "products";
	}
	
	
}
