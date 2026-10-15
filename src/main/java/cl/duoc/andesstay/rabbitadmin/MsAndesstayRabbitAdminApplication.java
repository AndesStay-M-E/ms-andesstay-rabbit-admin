package cl.duoc.andesstay.rabbitadmin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MsAndesstayRabbitAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsAndesstayRabbitAdminApplication.class, args);
    }
}
