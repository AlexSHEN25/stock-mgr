package co.handk.schema;

import co.handk.schema.annotation.Schema;
import co.handk.schema.config.LowCodeProperties;
import co.handk.schema.registry.SchemaRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Schema自动扫描注册（支持多包 + 插件化）
 */
@Component
@RequiredArgsConstructor
public class SchemaAutoRegister {

    private final SchemaRegistry registry;
    private final LowCodeProperties properties;

    @PostConstruct
    public void init() {

        if (properties.getScanPackages() == null || properties.getScanPackages().isEmpty()) {
            throw new RuntimeException("lowcode.scan-packages 未配置");
        }

        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);

        scanner.addIncludeFilter(new AnnotationTypeFilter(Schema.class));

        for (String basePackage : properties.getScanPackages()) {

            Set<org.springframework.beans.factory.config.BeanDefinition> beans =
                    scanner.findCandidateComponents(basePackage);

            for (var bean : beans) {
                try {
                    Class<?> clazz = Class.forName(bean.getBeanClassName());

                    Schema schema = clazz.getAnnotation(Schema.class);

                    String resource = schema.resource();

                    registry.register(resource, clazz);

                    System.out.println("Schema注册: " + resource + " -> " + clazz.getName());

                } catch (Exception e) {
                    throw new RuntimeException("Schema扫描失败: " + bean.getBeanClassName(), e);
                }
            }
        }
    }
}