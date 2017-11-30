package com.test.mongo.util;

import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
//import org.springframework.security.jwt.JwtHelper;
import org.springframework.stereotype.Component;

import com.sap.xs2.security.container.SecurityContext;
import com.sap.xs2.security.container.UserInfo;
import com.sap.xs2.security.container.UserInfoException;



@Configuration
@Component("tenantProvider")
public class TenantProvider {
	
	private static final String forwardedTenantId = "X-TENANT-ID";
	private static final String defaultTenant = "Defaul";
	
	private static final Logger log = LoggerFactory.getLogger(TenantProvider.class);
	
	private static ThreadLocal<String> tenantId = new ThreadLocal<>();

//	@Context
//	HttpServletRequest request;
	@Bean
	public static TenantProvider GetTenantProvider() {
		return new TenantProvider();
	}

	public String getTenantId() {
		System.out.println(tenantId);
		return tenantId.get() != null ? tenantId.get() : defaultTenant;
	}

	public static void parseTenantIdFromRequest(HttpServletRequest request) throws IllegalArgumentException, UnsupportedEncodingException {
//		String BEARER = "Bearer";
//		String authorization = request.getHeader("Authorization");
//		if(!authorization.isEmpty() && authorization.startsWith(BEARER)){
//            String tokenContent = authorization.replaceFirst(BEARER, "").trim();
//            //Decode JWT Token
//            Jwt decodedJwt = JwtHelper.decode(tokenContent);
//            String auth = decodedJwt.getClaims();
//            int indexOfZid = auth.indexOf("\"zid\":\"") + 7;
    		String zid = null;
            try {

                UserInfo userInfo = SecurityContext.getUserInfo();
                zid = userInfo.getIdentityZone();
                log.info("Read tenantId from JWT token: {}", zid);

            } catch (UserInfoException e) {
                log.error("UserInfoException, no tenant could be determined for this request.", e);
            }
//            String zid = auth.substring(indexOfZid, indexOfZid + 36);
            tenantId.set(zid);
        
	}

	public static void clearTenantId() {
		tenantId.remove();
	}
}
