package co.handk.backend.config;

import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

@Setter
@Component
@ConfigurationProperties(prefix = "app.avatar")
public class AvatarStorageProperties {

    private String dir = "./data/upload/avatar";

    public Path getRootDir() {
        return Paths.get(dir).toAbsolutePath().normalize();
    }

    public Path getUploadDir() {
        return getRootDir().resolve("upload").normalize();
    }
}
