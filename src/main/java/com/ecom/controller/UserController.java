package com.ecom.controller;

import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.ecom.model.Cart;
import com.ecom.model.Category;
import com.ecom.model.OrderRequest;
import com.ecom.model.ProductOrder;
import com.ecom.model.UserDtls;
import com.ecom.service.CartService;
import com.ecom.service.CategoryService;
import com.ecom.service.OrderService;
import com.ecom.service.UserService;
import com.ecom.util.CommonUtil;
import com.ecom.util.OrderStatus;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {
	@Autowired
    private final CommonUtil commonUtil;
	
	@Autowired
	private OrderService orderService;
	@Autowired
	private CategoryService categoryService;
	@Autowired
	private CartService cartService;
	@Autowired
	private UserService userService;
	@Autowired
	private PasswordEncoder encoder;

    UserController(CommonUtil commonUtil) {
        this.commonUtil = commonUtil;
    }
	@ModelAttribute
	public void getUserDetails(Principal p, org.springframework.ui.Model m) {
		
		if(p!=null) {
			
			String email = p.getName();
			UserDtls userDtls = userService.getUserByEmail(email);
			m.addAttribute("user",userDtls );
			Integer countCart = cartService.getCountCart(userDtls.getId());
			m.addAttribute("countCart",countCart);
			List<Cart> cartByUser = cartService.getCartByUser(userDtls.getId());
			m.addAttribute("carts", cartByUser);
			if(countCart > 0) {
			Double totalOrderPrice = cartByUser.get(cartByUser.size()-1).getTotalOrderPrice();
			m.addAttribute("totalOrderPrice", totalOrderPrice);}
		}
		List<Category> category = categoryService.getAllActiveCategory();
		m.addAttribute("category",category);
	
	}
	
	
	
	@GetMapping("/home")
	public String home() {
		
		return "user/home";
		
	}
	
	@GetMapping("/addCart")
	public String addToCart(@RequestParam Integer pid, @RequestParam Integer uid, HttpSession session) {
		
		Cart saveCart = cartService.saveCart(pid, uid);
		
		if(saveCart == null) {
			session.setAttribute("errorMsg", "Product add to cart failed");
			
		}else {
			
			session.setAttribute("succMsg", "Product added to cart");

			
		}
		return "redirect:/product/"+pid;
	}
	
	@GetMapping("/cart")
	public String loadCartPage() {
		
		
		return "user/cart";
	}
	@GetMapping("/cartQuantityUpdate")
	public String updateCartQuantity(@RequestParam String sy, @RequestParam Integer cid) {
		
		cartService.updateQuantity(sy, cid);
		return "redirect:/user/cart";
	}
	@GetMapping("/orders")
	public String orderPage() {
		
		
		return "user/order";
	}
	private UserDtls getLoggedInUserDetails(Principal p) {
		
		String email = p.getName();
		UserDtls userDtls = userService.getUserByEmail(email);
		return userDtls;
	}
	
	@PostMapping("/save-order")
	public String saveOrder(@ModelAttribute OrderRequest orderRequest, Principal p) throws UnsupportedEncodingException, MessagingException {
		UserDtls user = getLoggedInUserDetails(p);
		orderService.saveOrder(user.getId(), orderRequest);
		
		return "user/success";
	}
	
	@GetMapping("/success")
	public String loadSuccess() {
		
		return "user/success";
	}
	
	@GetMapping("/user-orders")
	public String myOrders(Model m, Principal p) {
		UserDtls user = getLoggedInUserDetails(p);
	    List<ProductOrder> orders = orderService.getOrderByUser(user.getId());
	    m.addAttribute("orders", orders);
		
		return "user/myorder";
	}
	
	@GetMapping("/update-status")
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
			
			session.setAttribute("succMsg", "Order cancelled");

			
		}
		return "redirect:/user/user-orders";
	}
	@GetMapping("/profile")
	public String profile() {
		
		
		return "user/profile";
	}
	@PostMapping("/update-profile")
	public String updateProfile(@ModelAttribute UserDtls user, @RequestParam MultipartFile img, HttpSession session) {
		UserDtls updateUserProfile = userService.updateUserProfile(user, img);
		if(updateUserProfile == null) {
			
      session.setAttribute("errorMsg", "Something went wrong");
			
		}else {
			
			session.setAttribute("succMsg", "Profile Updated Successfully");

			
		}
		return "redirect:/user/profile";
	}
	
	@PostMapping("change-password")
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
					
					
				
		return "redirect:/user/profile";
	}
	
}
