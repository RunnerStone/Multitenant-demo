package com.test.mongo.services;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.netflix.hystrix.exception.HystrixRuntimeException;

@Component
public class UserServiceClient {
	private static final String PATH = "api/v1.0/users";
	private final RestTemplate restTemplate;
	
	@Value("${USER_ROUTE}")
	private String userServiceRoute;
	
	private Logger logger;
	
	@Inject
	public UserServiceClient(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
		this.logger = LoggerFactory.getLogger(getClass());
	}
	
	public boolean isPremiumUser(String id) throws RuntimeException {
		String url = userServiceRoute + "/" + PATH + "/" + id;
		boolean isPremiumUser = false;
		
		try {
			User user = new GetUserCommand(url, restTemplate).execute();
			isPremiumUser = user.premiumUser;
		}catch(HystrixRuntimeException ex) {
			logger.warn("[HystrixFailure:" + ex.getFailureType().toString() + "] " + ex.getMessage());
		}
		
		return isPremiumUser;
	}
	
	public static class User{
		public boolean premiumUser;
	}

}
