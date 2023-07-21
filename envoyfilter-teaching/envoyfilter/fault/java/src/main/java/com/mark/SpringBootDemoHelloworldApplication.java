package com.mark;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.RequestHeader;


@SpringBootApplication
@RestController
public class SpringBootDemoHelloworldApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringBootDemoHelloworldApplication.class, args);
	}


	@GetMapping("/auth")
	public String auth(@RequestHeader("userId") String userId,@RequestHeader("x-envoy-downstream-service-node") String node ) {
		//String userId=request.getHeader("UserId");
		if ("admin".equals(userId)){
			return "ok";
		}
		return node;
	}
}
