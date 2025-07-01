package com.workbridge.workbridge_app.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "security")
@Getter
@Setter
public class SecurityProperties {

    private List<String> publicUrls;
    private Cors cors = new Cors();

    @Getter
    @Setter
    public static class Cors {
        private List<String> allowedOrigins;
    }
}
