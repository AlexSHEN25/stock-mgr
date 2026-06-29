package co.handk.backend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@MapperScan("co.handk.backend.mapper")
@EnableScheduling
@SpringBootApplication
public class StockBackendApplication {
    private static final String TOKYO_TIME_ZONE = "Asia/Tokyo";

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone(TOKYO_TIME_ZONE));
        SpringApplication.run(StockBackendApplication.class, args);
    }

}
