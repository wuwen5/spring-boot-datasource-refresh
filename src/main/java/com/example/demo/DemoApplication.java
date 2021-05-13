package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}



	@Configuration
	@ConfigurationProperties(prefix = "spring.datasource")
	public class DataSourceConfiguration {

		private Class<DataSource> type;

		@Bean
		@RefreshScope
        @ConfigurationProperties(prefix = "spring.datasource.hikari")
		DataSource dataSource() {
            System.out.println("create RefreshScope DataSource.");
			return DataSourceBuilder.create().type(type).build();
		}

        public void setType(Class<DataSource> type) {
            this.type = type;
        }
    }

}
