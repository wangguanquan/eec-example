package net.cua;

import com.alibaba.druid.pool.DruidDataSourceFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;
import java.util.HashMap;

/**
 * Create by guanquan.wang at 2018-10-13 13:56
 */
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    @ConfigurationProperties(prefix="spring.dataSource")
    public DataSource dataSource() throws Exception {
        return DruidDataSourceFactory.createDataSource(new HashMap());
    }
}
