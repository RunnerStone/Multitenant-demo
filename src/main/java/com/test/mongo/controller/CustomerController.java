package com.test.mongo.controller;

import static org.springframework.http.HttpStatus.NO_CONTENT;

import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.sap.hcp.cf.logging.common.customfields.CustomField;
import com.test.mongo.models.Customer;
import com.test.mongo.models.CustomerRepository;
import com.test.mongo.services.UserServiceClient;

@RestController
@RequestMapping(path = CustomerController.PATH)
@RequestScope
@Validated
public class CustomerController {
    public static final String PATH = "/api/v1/customer";
    private CustomerRepository customerRepository;
    private MongoTemplate mongoTemplate;
    
    private static final Marker TECHNICAL = MarkerFactory.getMarker("TECHNICAL");
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    private UserServiceClient userServiceClient;
    
    @Autowired
    HttpServletRequest request;

    @Autowired
    public CustomerController(CustomerRepository customerRepository, UserServiceClient userServiceClient) {
        this.customerRepository = customerRepository;
        this.userServiceClient = userServiceClient;
    }

    @RequestMapping(path = "", method = RequestMethod.GET)
    public Iterable<Customer> customers() {
        return customerRepository.findAll();
    }

    @RequestMapping(value = "{id}", method = RequestMethod.GET)
    public Customer personById(@PathVariable("id") String id) {

        MDC.put("endpoint", "GET: " + PATH + "/" + id);

        logger.info("demonstration of custom fields, not part of message",
                CustomField.customField("example-key", "example-value"));
        logger.info("demonstration of custom fields, part of message: {}",
                CustomField.customField("example-key", "example-value"));
        logger.info("method entry, GET: {}/{}", PATH, id);
        return customerRepository.findOne(id);
    }

    @RequestMapping(value = "lastname/{name}", method = RequestMethod.GET)
    public Iterable<Customer> PersonByName(@PathVariable("name") String name) {
        return customerRepository.findByLastName(name);
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Customer> add(@RequestBody @Valid Customer Person, UriComponentsBuilder uriComponentsBuilder) throws URISyntaxException  {
    	
    	if(userServiceClient.isPremiumUser("42")) {
    		
    		Customer savedPerson = customerRepository.save(Person);
        	logger.info(TECHNICAL, "created ad with id {}", savedPerson.id);
            UriComponents uriComponents = uriComponentsBuilder.path(PATH + "/{{id}").buildAndExpand(savedPerson.getId());
            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(uriComponents.toUri());
            return new ResponseEntity<>(savedPerson, headers, HttpStatus.CREATED);
    	} else {
    		String message = "You need to be a premium user to create an advertisement";
    		logger.warn(message);
    		throw new NotAuthorizedException(message);
    	}
    	
    }

    @DeleteMapping
    @ResponseStatus(NO_CONTENT)
    public void deteAll() {
    	customerRepository.deleteAll();
    }

    @DeleteMapping("{id}")
    @ResponseStatus(NO_CONTENT)
    public void deleteById(@PathVariable("id") String id) {
    	customerRepository.delete(id);
    }

    @RequestMapping(value = "{id}", method = RequestMethod.PUT)
    public Customer update(@PathVariable("id") String id, @RequestBody Customer updatePerson) {
        return customerRepository.save(updatePerson);
    }
}
