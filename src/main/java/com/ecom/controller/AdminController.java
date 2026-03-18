package com.ecom.controller;



import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.ecom.model.Category;
import com.ecom.model.Product;
import com.ecom.model.ProductOrder;
import com.ecom.model.UserDtls;
import com.ecom.service.CartService;
import com.ecom.service.CategoryService;
import com.ecom.service.FileService;
import com.ecom.service.OrderService;
import com.ecom.service.ProductService;
import com.ecom.service.UserService;

import com.ecom.util.CommonUtil;
import com.ecom.util.OrderStatus;


import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;


@Controller
@RequestMapping("/admin")
public class AdminController {
	
	@Autowired
    private final CommonUtil commonUtil;
	@Autowired
	private CategoryService categoryService;
	@Autowired
	private ProductService productService;
	@Autowired
	private UserService userService;
	@Autowired
	private CartService cartService;
	@Autowired
	private OrderService orderService;
	@Autowired
	private PasswordEncoder encoder;
	
	@Autowired
	private FileService fileService;
    AdminController(CommonUtil commonUtil) {
        this.commonUtil = commonUtil;
    }
	@ModelAttribute
	public void getUserDetails(Principal p, Model m) {
		
		if(p!=null) {
			
			String email = p.getName();
			UserDtls userDtls = userService.getUserByEmail(email);
			m.addAttribute("user",userDtls );
			Integer countCart = cartService.getCountCart(userDtls.getId());
			
			m.addAttribute("countCart",countCart);
		}
		List<Category> Allcategory = categoryService.getAllActiveCategory();
		m.addAttribute("Allcategory",Allcategory);	
	
	}
	
	@GetMapping("/index")
	public String index() {
		
		return "admin/index";
	}
	
	@GetMapping("/loadAddProduct")
	public String loadAddProduct(org.springframework.ui.Model m) {
		List<Category> categories = categoryService.getAllcategory();
		m.addAttribute("categories",categories);
		
		return "admin/add_product";
	}
	
	@GetMapping("/category")
	public String category(Model m, @RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
			@RequestParam(name = "pageSize", defaultValue = "5") Integer pageSize) {
		// m.addAttribute("categorys", categoryService.getAllCategory());
		Page<Category> page = categoryService.getAllCategorPagination(pageNo, pageSize);
		List<Category> categorys = page.getContent();
		m.addAttribute("categorys", categorys);

		m.addAttribute("pageNo", page.getNumber());
		m.addAttribute("pageSize", pageSize);
		m.addAttribute("totalElements", page.getTotalElements());
		m.addAttribute("totalPages", page.getTotalPages());
		m.addAttribute("isFirst", page.isFirst());
		m.addAttribute("isLast", page.isLast());

		return "admin/category";
	}
	@PostMapping("/saveCategory")
	public String saveCategory(@ModelAttribute Category category,@RequestParam("file") MultipartFile file, HttpSession session) throws IOException {
		
		//String imageName= file != null ? file.getOriginalFilename():"default.jpg";
		//https://shoppingkart-category.s3.us-east-1.amazonaws.com/pant.png
		String imageUrl = commonUtil.getImageUrl(file, 1);
		category.setImgName(imageUrl);
		Boolean existCategory = categoryService.existCategory(category.getName());
		
		if(existCategory) {
		session.setAttribute("errorMsg", "Category Name Already exists");
		
		}else 
		{
		com.ecom.model.Category saveCategory = categoryService.saveCategory(category);
		if(ObjectUtils.isEmpty(saveCategory)) {
			session.setAttribute("errorMsg", "Not saved : Internal error");
		}else {
		//this is for mysql database upload	
		/*
		 * File saveFile = new ClassPathResource("static/img").getFile(); Path path =
		 * Paths.get(saveFile.getAbsolutePath()+File.separator+"category_img"+File.
		 * separator+file.getOriginalFilename()); System.out.println(path);
		 * Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
		 */	
			fileService.uploadFileS3(file, 1);
			
			session.setAttribute("succMsg", "Saved Successfully");
		 
		}
	}
	
	
		return "redirect:/admin/category";
	}
	
	
	@GetMapping("/deleteCategory/{id}")
	public String deleteCategory(@PathVariable int id, HttpSession session) {
		
		Boolean deleteCategory = categoryService.deleteCategory(id);
		
		if(deleteCategory) {
			session.setAttribute("succMsg", "category deleted succsessfully");
		}else {
			session.setAttribute("errorMsg", "Something went wrong");
		}
		return "redirect:/admin/category";
	}
	@GetMapping("/loadEditCategory/{id}")
	public String loadEditCategory(@PathVariable int id, Model m) {
		
		m.addAttribute("category",categoryService.getCategoryById(id));
		return "admin/edit_category";
	}
	
	@PostMapping("/updateCategory")
	public String updateCategory(@ModelAttribute Category category1, @RequestParam("file") MultipartFile file, HttpSession session) throws IOException {
		System.out.println("Received ID: " + category1.getId());
		System.out.println("Received Status: " + category1.getIsActive());
		Category oldCategory = categoryService.getCategoryById(category1.getId());
		//String imageName = file.isEmpty() ? oldCategory.getImgName() : file.getOriginalFilename();
		String imageUrl = commonUtil.getImageUrl(file, 1);
		if (oldCategory != null) {

			oldCategory.setName(category1.getName());
			oldCategory.setIsActive(category1.getIsActive());
			oldCategory.setImgName(imageUrl);
		}

		Category updateCategory = categoryService.saveCategory(oldCategory);

		if (!ObjectUtils.isEmpty(updateCategory)) {

			if (!file.isEmpty()) {
				/*
				 * File saveFile = new ClassPathResource("static/img").getFile();
				 * 
				 * Path path = Paths.get(saveFile.getAbsolutePath() + File.separator +
				 * "category_img" + File.separator + file.getOriginalFilename());
				 * 
				 * // System.out.println(path); Files.copy(file.getInputStream(), path,
				 * StandardCopyOption.REPLACE_EXISTING);
				 */
				fileService.uploadFileS3(file, 1);
			}

			session.setAttribute("succMsg", "Category update success");
		} else {
			session.setAttribute("errorMsg", "something wrong on server");
		}

		return "redirect:/admin/loadEditCategory/" + category1.getId();
	}
	
	@PostMapping("/saveProduct")
	public String saveProduct(@ModelAttribute Product product,@RequestParam("file") MultipartFile image, HttpSession session) throws IOException {
		
		//String imageName =	image.isEmpty() ? "default.jpg" : image.getOriginalFilename();
		String imageUrl = commonUtil.getImageUrl(image, 2);
		
		product.setImage(imageUrl);
		product.setDiscount(0);
		product.setDiscountPrice(product.getPrice());
	
		Product saveProduct = productService.saveProduct(product);
		if(!ObjectUtils.isEmpty(saveProduct)) {
			
			//File saveFile =	new ClassPathResource("static/img").getFile();
			//Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+"product_img"+File.separator+image.getOriginalFilename());	
			//System.out.println(path);
			//Files.copy(image.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
			fileService.uploadFileS3(image, 2);
			session.setAttribute("succMsg", "Product Saved Succussfully");
		}
		else {
			session.setAttribute("errorMsg", "Something Went Wrong");

			
		}
		
		return "redirect:/admin/loadAddProduct";
	}
	
	@GetMapping("/products")
	public String loadViewProduct(Model m, @RequestParam(defaultValue = "") String ch,
			@RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
			@RequestParam(name = "pageSize", defaultValue = "5") Integer pageSize) {

//		List<Product> products = null;
//		if (ch != null && ch.length() > 0) {
//			products = productService.searchProduct(ch);
//		} else {
//			products = productService.getAllProducts();
//		}
//		m.addAttribute("products", products);

		Page<Product> page = null;
		if (ch != null && ch.length() > 0) {
			page = productService.searchProductPagination(pageNo, pageSize, ch);
		} else {
			page = productService.getAllProductsPagination(pageNo, pageSize);
		}
		m.addAttribute("products", page.getContent());

		m.addAttribute("pageNo", page.getNumber());
		m.addAttribute("pageSize", pageSize);
		m.addAttribute("totalElements", page.getTotalElements());
		m.addAttribute("totalPages", page.getTotalPages());
		m.addAttribute("isFirst", page.isFirst());
		m.addAttribute("isLast", page.isLast());

		return "admin/products";
	}
	
	@GetMapping("/deleteProduct/{id}")
	public String deleteProduct(@PathVariable int id, HttpSession session) {
		
		Boolean deleteproduct = productService.deleteProduct(id);
		
		if(deleteproduct) {
			session.setAttribute("succMsg", "Product Deleted Successfully");
		}else {
			session.setAttribute("errorMsg", "Something Went Wrong");

			
		}
		
		return "redirect:admin/products";
	}
	
	@GetMapping("/editProduct/{id}")
	public String editProduct(@PathVariable int id, org.springframework.ui.Model m) {
		m.addAttribute("product", productService.getProductById(id));
		m.addAttribute("categories", categoryService.getAllcategory() );

		return "admin/edit_product";
	}
	
	@PostMapping("/updateProduct")
	public String updateProduct( org.springframework.ui.Model m, HttpSession session, @ModelAttribute Product product, @RequestParam("file") MultipartFile image) {
		
		if(product.getDiscount() < 0 || product.getDiscount()>100) {
			session.setAttribute("errorMsg", "Invalid Discount");

		}
		else {
		Product updatedProduct = productService.updateProduct(product, image);
		if(!ObjectUtils.isEmpty(updatedProduct)) {
			
			
			session.setAttribute("succMsg", "Product updated Succussfully");
		}
		else {
			session.setAttribute("errorMsg", "Something Went Wrong");

			
		}}
		
		
		return "redirect:/admin/editProduct/"+product.getId();
	}
	@GetMapping("/users")
	public String getAllUsers(org.springframework.ui.Model m) {
		
		
		//List<UserDtls> users = userService.getUsers("Role_USER");
		List<UserDtls> users = userService.getUsers();
		m.addAttribute("users",users);
		return "admin/users";
	}
	
	@GetMapping("/updateStatus")
	public String updateUserAccountStatus(@RequestParam Boolean status, @RequestParam Integer id, HttpSession session) {
		Boolean f = userService.updateAccountStatus(id, status);
		
		
		if(f) {
			
			session.setAttribute("succMsg", "Account Status Updated");
		}
		else {
			session.setAttribute("errorMsg", "Something Went Wrong");

			
		}
			
		
		return "redirect:/admin/users";
	}
	
	@GetMapping("/orders")
	public String getAllOrders(Model m,@RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
			@RequestParam(name = "pageSize", defaultValue = "5") Integer pageSize) {
//		List<ProductOrder> allOrders = orderService.getAllOrders();
//		m.addAttribute("orders", allOrders);
//		m.addAttribute("srch", false);
		
		Page<ProductOrder> page = orderService.getAllOrdersPagination(pageNo, pageSize);
		m.addAttribute("orders", page.getContent());
		m.addAttribute("srch", false);
	
		m.addAttribute("pageNo", page.getNumber());
		m.addAttribute("pageSize", pageSize);
		m.addAttribute("totalElements", page.getTotalElements());
		m.addAttribute("totalPages", page.getTotalPages());
		m.addAttribute("isFirst", page.isFirst());
		m.addAttribute("isLast", page.isLast());
		
		return "admin/orders";
	}
	
	
	@PostMapping("/update-order-status")
	public String updateOrderStatus(@RequestParam Integer id,@RequestParam Integer st, HttpSession session ) throws UnsupportedEncodingException, MessagingException {
		OrderStatus[] values = OrderStatus.values();
		String status = null;
		for(OrderStatus orderSt : values) {
			
			if(orderSt.getId().equals(st)) {
				
				status = orderSt.getName();
			}
			
		}
		ProductOrder updateOrder= orderService.updateOrderStatus(id, status);
		commonUtil.sendMailforProductOrder(updateOrder, status);

		if(updateOrder == null) {
			session.setAttribute("errorMsg", "Something went wrong");
			
		}else {
			
			session.setAttribute("succMsg", "Order Status Updated");

			
		}
		return "redirect:/admin/orders";
	}
	@GetMapping("/search-order")
	public String searchProduct(@RequestParam String orderId, Model m, HttpSession session,@RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
			@RequestParam(name = "pageSize", defaultValue = "5") Integer pageSize) {
		if (orderId != null && orderId.length() > 0) {

			ProductOrder order = orderService.getOrdersByOrderId(orderId.trim());

			if (ObjectUtils.isEmpty(order)) {
				session.setAttribute("errorMsg", "Incorrect orderId");
				m.addAttribute("orderDtls", null);
			} else {
				m.addAttribute("orderDtls", order);
			}

			m.addAttribute("srch", true);
		} else {
//			List<ProductOrder> allOrders = orderService.getAllOrders();
//			m.addAttribute("orders", allOrders);
//			m.addAttribute("srch", false);
			
			Page<ProductOrder> page = orderService.getAllOrdersPagination(pageNo, pageSize);
			m.addAttribute("orders", page);
			m.addAttribute("srch", false);
			
			m.addAttribute("pageNo", page.getNumber());
			m.addAttribute("pageSize", pageSize);
			m.addAttribute("totalElements", page.getTotalElements());
			m.addAttribute("totalPages", page.getTotalPages());
			m.addAttribute("isFirst", page.isFirst());
			m.addAttribute("isLast", page.isLast());
			
		}
		return "admin/orders";

	}
	
	@GetMapping("add-admin")
	public String loadadminAdd() {
		
		
		return "admin/add_admin";
	}

	
	
	@PostMapping("/saveAdmin")
	public String saveAdmin(@ModelAttribute UserDtls user, @RequestParam("img") MultipartFile file, HttpSession session) throws IOException {
		
		
		//String imageName = file.isEmpty() ? "default.jpg" : file.getOriginalFilename();
		String imageUrl = commonUtil.getImageUrl(file, 3);

		user.setProfileImage(imageUrl);
		UserDtls savedUser = userService.saveAdmin(user);
		if(!ObjectUtils.isEmpty(savedUser)) {
			
			if(!file.isEmpty()) {
				/*
				 * File saveFile = new ClassPathResource("static/img").getFile(); Path path =
				 * Paths.get(saveFile.getAbsolutePath()+File.separator+"profile_img"+File.
				 * separator+file.getOriginalFilename()); System.out.println(path);
				 * Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
				 */
				fileService.uploadFileS3(file, 3);
			}
			session.setAttribute("succMsg", "Registered Succussfully");

		}else {
			
			session.setAttribute("errorMsg", "Something went wrong");
			
		}
		
		
		return "redirect:/admin/add-admin";
		
	}
	
	
	@GetMapping("/profile")
	public String profile() {
		
		
		return "admin/profile";
	}
	@PostMapping("/update-profile")
	public String updateProfile(@ModelAttribute UserDtls user, @RequestParam MultipartFile img, HttpSession session) {
		UserDtls updateUserProfile = userService.updateUserProfile(user, img);
		if(updateUserProfile == null) {
			
      session.setAttribute("errorMsg", "Something went wrong");
			
		}else {
			
			session.setAttribute("succMsg", "Profile Updated Successfully");

			
		}
		return "redirect:/admin/profile";
	}
	private UserDtls getLoggedInUserDetails(Principal p) {
		
		String email = p.getName();
		UserDtls userDtls = userService.getUserByEmail(email);
		return userDtls;
	}
	@PostMapping("/change-password")
	public String changePassword(@RequestParam String newPassword, @RequestParam String currentPassword, Principal p, HttpSession session) {
		
		UserDtls loggedInUserDetails = getLoggedInUserDetails(p);
		boolean matches = encoder.matches(currentPassword, loggedInUserDetails.getPassword());
		if(!matches) {
			
		      session.setAttribute("errorMsg", "Incorrect current password");
					
				}else {
					@Nullable
					String encodePassword = encoder.encode(newPassword);
					loggedInUserDetails.setPassword(encodePassword);
					UserDtls updateUser = userService.updateUser(loggedInUserDetails);
					if(updateUser == null) {
						
						session.setAttribute("errorMsg", "Password not updated successfully");
					}else {
					
					session.setAttribute("succMsg", "Password Updated Successfully");

					
				}
				}
					
					
				
		return "redirect:/admin/profile";
	}
}



