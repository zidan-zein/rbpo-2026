package ru.zidan.rbpo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

// тут два разных GET запроса, как в задании просили
@RestController
public class HelloController {

    // первый эндпоинт - просто возвращает текст
    @GetMapping("/hello")
    public String hello() {
        return "Privet, eto pervoe zadanie po RBPO";
    }

    // второй эндпоинт - возвращает число
    // беру случайное число от 1 до 100, чтобы функция реально отличалась от первой
    @GetMapping("/random-number")
    public int randomNumber() {
        Random rnd = new Random();
        int num = rnd.nextInt(100) + 1;
        return num;
    }

}
