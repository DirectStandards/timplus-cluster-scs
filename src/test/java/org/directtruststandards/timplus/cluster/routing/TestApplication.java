package org.directtruststandards.timplus.cluster.routing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(TestChannelBinderConfiguration.class)
@ComponentScan({"org.directtruststandards.timplus.cluster.routing"})
@EnableAutoConfiguration
public class TestApplication 
{
	public static void main(String[] args) 
	{
		SpringApplication.run(TestApplication.class, args);
	}	
}
