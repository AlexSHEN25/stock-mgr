package co.handk.schema.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 启用配置
 */
@Configuration
@EnableConfigurationProperties(LowCodeProperties.class)
public class LowCodeAutoConfig {
}