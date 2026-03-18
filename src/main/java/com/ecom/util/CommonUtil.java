package com.ecom.util;

import java.io.UnsupportedEncodingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.ecom.model.ProductOrder;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class  CommonUtil {
	
	@Autowired
	public AmazonS3 amazonS3;
	@Value("${aws.s3.bucket.category}")
	private String categoryBucket;
	@Value("${aws.s3.bucket.product}")
	private String productBucket;
	@Value("${aws.s3.bucket.profile}")
	private String profileBucket;
	@Autowired
	private  JavaMailSender mailSender;
	public  Boolean sendMail(String url, String email) throws UnsupportedEncodingException, MessagingException {
		
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);
		
		helper.setFrom("soumyachurheofficial@gamil.com", "Shopping Cart");
		helper.setTo(email);
		String content = "reset the password using below link" + "<p> <a href=\"" + url +"\"> Change my password </a> </p>";
		helper.setSubject("password reset");
		helper.setText(content, true);
		mailSender.send(message);
		return true;
	}

	public static String generateUrl(HttpServletRequest request) {
		
		
		
		 String siteUrl = request.getRequestURL().toString();
		 return siteUrl.replace(request.getServletPath(), "");

	}
	 
			
	public Boolean sendMailforProductOrder(ProductOrder order, String status) throws UnsupportedEncodingException, MessagingException {
		String	msg="<p>Hello [[name]],</p>"
				+ "<p>Thank you order <b>[[orderStatus]]</b>.</p>"
				+ "<p><b>Product Details:</b></p>"
				+ "<p>Name : [[productName]]</p>"
				+ "<p>Category : [[category]]</p>"
				+ "<p>Quantity : [[quantity]]</p>"
				+ "<p>Price : [[price]]</p>"
				+ "<p>Payment Type : [[paymentType]]</p>";
		
		
		
		
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);

		helper.setFrom("soumyachurheofficial@gamil.com", "Shopping Cart");
		helper.setTo(order.getOrderAddress().getEmail());

		msg=msg.replace("[[name]]",order.getOrderAddress().getFirstName());
		msg=msg.replace("[[orderStatus]]",status);
		msg=msg.replace("[[productName]]", order.getProduct().getTitle());
		msg=msg.replace("[[category]]", order.getProduct().getCategory());
		msg=msg.replace("[[quantity]]", order.getQuantity().toString());
		msg=msg.replace("[[price]]", order.getPrice().toString());
		msg=msg.replace("[[paymentType]]", order.getPaymentType());
		
		helper.setSubject("Product Order Status");
		helper.setText(msg, true);
		mailSender.send(message);
		return true;
		
	}
	public String getImageUrl(MultipartFile file, Integer bucketType) {
		String bucketName= null;
		
		if(bucketType == 1) {
			
			bucketName = categoryBucket;
		}
		else if(bucketType ==2) {
			
			bucketName = productBucket;
		}else {
			bucketName = profileBucket;
		}
		String imageName= file != null ? file.getOriginalFilename():"default.jpg";

		//https://shoppingkart-category.s3.us-east-1.amazonaws.com/pant.png
		String url = "https://" + bucketName +".s3.us-east-1.amazonaws.com/" + imageName;
		
		return url;
	}
}
