package cn.edu.xmu.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {
    @Bean
    public RestTemplate restTemplate() {
        // 我不熟悉RestTemplate的配置，就这样简单的搞一下好了。
        return new RestTemplate();
    }
}
