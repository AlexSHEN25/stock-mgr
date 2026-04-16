package co.handk.schema.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * 低代码配置
 */
@Data
@ConfigurationProperties(prefix = "lowcode")
public class LowCodeProperties {

    /**
     * 扫描包列表
     */
    private List<String> scanPackages;
}