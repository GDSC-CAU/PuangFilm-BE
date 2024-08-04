package gdsc.cau.puangbe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class PuangbeApplication {

	public static void main(String[] args) {
		SpringApplication.run(PuangbeApplication.class, args);
	}

}
