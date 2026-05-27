package com.TicketApp.orders_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        OpenAPI openAPI = new OpenAPI();
        openAPI.setOpenapi("3.0.3");
        
        Info info = new Info();
        info.setTitle("TicketApp Orders API");
        info.setVersion("1.0.0");
        info.setDescription("API para gestión de órdenes de tickets");
        
        Contact contact = new Contact();
        contact.setName("TicketApp Team");
        contact.setEmail("support@ticketapp.com");
        info.setContact(contact);
        
        openAPI.setInfo(info);
        
        Server server = new Server();
        server.setUrl("http://localhost:8080");
        server.setDescription("Local development server");
        openAPI.setServers(List.of(server));
        
        return openAPI;
    }
}
