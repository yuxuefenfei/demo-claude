package cn.wangwenzhu.claude;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
//@EnableScheduling
public class DemoClaudeApplication {

    public static void main(String[] args) {
        // 启动Spring Boot应用（非Web模式，避免阻塞）
        var app = new SpringApplication(DemoClaudeApplication.class);
        app.setWebApplicationType(WebApplicationType.NONE);

        // 启动应用上下文，CommandLineRunner会自动执行
        app.run(args);
    }
}
