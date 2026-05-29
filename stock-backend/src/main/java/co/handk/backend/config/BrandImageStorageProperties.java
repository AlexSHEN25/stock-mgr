package co.handk.backend.config;

import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

@Setter
@Component
@ConfigurationProperties(prefix = "app.brand-image")
public class BrandImageStorageProperties {

    private String dir = "./data/upload/brand";

    public Path getRootDir() {
        return Paths.get(dir).toAbsolutePath().normalize();
    }
}

