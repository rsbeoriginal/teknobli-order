package com.tecknobli.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OrderApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrderApplication.class, args);
	}

//	@Override
//	public void run(String... strings) throws Exception {
//
//		@Autowired
//		EmailService emailService = new EmailServiceImpl();
//
//		((EmailServiceImpl) emailService).sendSimpleMessage("rishi.sharma@coviam.com","Test Subject",
//								"This is the body of the test mail");
//	}
}

