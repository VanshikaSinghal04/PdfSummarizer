package com.vanshika.pdf_summarizer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import com.vanshika.pdf_summarizer.service.UserService;
import org.springframework.security.config.Customizer;
@Configuration  //this tells Spring that this class contains configuration settings for the application and should be processed during startup when the index html page is loaded.treat this file as a set of rules/settings for the application
@EnableWebSecurity//this tells Spring to enable web security for the application, which means that it will apply security measures to incoming HTTP requests and protect the application from unauthorized access.
public class SecurityConfig {
    @Autowired
    private UserService userService;

    @Bean  //this tells Spring Boot, Run this method automatically when the app starts, take the security system it creates, and keep it active in memory.
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())//csrf stands for Cross-Site Request Forgery. It is a security feature that stops unauthorized websites from sending data to your backend. so disabling helps as whn you click on button it will not take you to someother page
            .authorizeHttpRequests(auth -> auth //it sees  who is allowed to visit which URLs on your website.
                .requestMatchers(
                    "/",
                    "/index.html",
                    "/login",
                    "/error",
                    "/oauth2/**",
                    "/login/oauth2/**"
                ).permitAll()//this means that anyone can access these URLs without needing to log in or be authenticated. These are public pages that don't require any special permissions.
                .anyRequest().authenticated()//this means that any other request that is not listed above will require the user to be authenticated (logged in) before they can access it.
            )
            .oauth2Login(oauth2 -> oauth2 //oath stands for Open Authorization, which is a way for users to log in using their existing accounts from other services (like Google, Facebook, etc.) instead of creating a new account specifically for your application.
                .defaultSuccessUrl("/upload-page", true)//this means that after a user successfully logs in using OAuth2 (like Google login), they will be redirected to the "/upload-page" URL. The 'true' parameter ensures that this redirection happens every time they log in, even if they were trying to access a different page before logging in.
                .failureUrl("/login?error")
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(oAuth2UserRequest -> {  //this is a lambda expression that defines what happens when the application needs to load user information after a successful OAuth2 login. It takes an oAuth2UserRequest as input and returns an OAuth2User.
                        var delegate = new org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService();
                        var oauth2User = delegate.loadUser(oAuth2UserRequest);
                        userService.saveOrUpdateUser(oauth2User);
                        return oauth2User;
                    })
                )
            )
            .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(Customizer.withDefaults())
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/")
                .permitAll() //this means that anyone can log out of the application without needing to be authenticated. The logout page is accessible to all users.
            );

        return http.build();// This takes the complete set of rules you just defined, builds them into a finished security system, and passes it back to Spring Boot to activate i
    }
}