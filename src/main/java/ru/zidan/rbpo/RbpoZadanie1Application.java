package ru.zidan.rbpo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// это главный класс, с него все начинается
// SpringApplication.run сам поднимает сервер и все настраивает
@SpringBootApplication
public class RbpoZadanie1Application {

    public static void main(String[] args) {
        SpringApplication.run(RbpoZadanie1Application.class, args);
    }

}
