package com.test.mongo.services;

import java.util.function.Supplier;

import static com.sap.hcp.cf.logging.common.LogContext.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.exception.HystrixBadRequestException;
import com.test.mongo.services.UserServiceClient.User;
import com.sap.hcp.cf.logging.common.LogContext;

public class GetUserCommand extends HystrixCommand<User> {
	
	//Hystrix uses a default timeout of 1000ms, increase in case you run into problems in remote locations
	private static final int DERAULT_TIMEOUT_MS = 1000;
    private Logger logger = LoggerFactory.getLogger(getClass());
	
	private String url;
	private RestTemplate restTemplate;
    private String correlationId;
	
	public GetUserCommand(String url, RestTemplate restTemplate) {
		super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("User"))
		        .andCommandKey(HystrixCommandKey.Factory.asKey("User.getById"))
		        .andCommandPropertiesDefaults(HystrixCommandProperties.Setter().withExecutionTimeoutInMilliseconds(1500)));
		this.url = url;
		this.restTemplate = restTemplate;

        this.correlationId = LogContext.getCorrelationId();
	}
	
	@Override
	protected User run() throws Exception {
		LogContext.initializeContext(this.correlationId);
		logger.info("sending request {}", url);
		
		ResponseEntity<User> responseEntity = sendRequest();
		
		HttpStatus statusCode = responseEntity.getStatusCode();
		if(statusCode.is4xxClientError()) {
			logger.error("received HTTP status code: {}", statusCode);
			throw new HystrixBadRequestException("Unsuccessful outgoing request");
		}
		else if(!statusCode.is2xxSuccessful()) {
			logger.warn("received HTTP status code: {}", statusCode);
			throw new IllegalStateException("Unsuccessful outgoing request");
		}
		logger.info("received response, status code: {}", statusCode);
		return responseEntity.getBody();
	}
	
	protected ResponseEntity<User> sendRequest(){
		HttpHeaders headers = new HttpHeaders();
		headers.add(HTTP_HEADER_CORRELATION_ID, correlationId);
		HttpEntity<User> request = new HttpEntity<>(headers);
		return restTemplate.exchange(url, HttpMethod.GET, request, User.class);
	}
	
	@Override
	protected User getFallback() {
		logger.info("enter fallback method");
		if(isResponseTimedOut()) {
			logger.error("execution timed out after {} ms (HystrixCommandKey:{})", getTimeoutInMs(),
                    this.getCommandKey().name());
		}
		if(isFailedExecution()) {
			logger.error("execution failed", getFailedExecutionException());
		}
		if (isResponseRejected()) {
            logger.warn("request was rejected");
        }
		return new User();
	}
	
	protected int getTimeoutInMs() {
		return this.properties.executionTimeoutInMilliseconds().get();
	}
}
