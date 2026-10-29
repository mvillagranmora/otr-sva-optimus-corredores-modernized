package cl.bcs.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class BcsGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(BcsGatewayApplication.class, args);
    }
}
