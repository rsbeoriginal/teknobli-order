package com.tecknobli.order;

import com.tecknobli.order.dto.ProductDTO;
import com.tecknobli.order.entity.UserOrder;
import com.tecknobli.order.merchantmicroservice.dto.MerchantOrderDTO;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class OrderApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrderApplication.class, args);
	}

//	@Override
//	public void run(String... strings) throws Exception {
//
//
//	}

}

