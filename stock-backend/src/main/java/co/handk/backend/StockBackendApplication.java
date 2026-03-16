package co.handk.backend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@MapperScan("co.handk.backend.mapper")
@SpringBootApplication
public class StockBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(StockBackendApplication.class, args);
    }

}
