package ru.zidan.rbpo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.zidan.rbpo.entity.Exhibition;
import ru.zidan.rbpo.entity.User;
import ru.zidan.rbpo.repository.ExhibitionRepository;
import ru.zidan.rbpo.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    // репозитории передает спринг через конструктор (его делает lombok)
    private final UserRepository userRepository;
    private final ExhibitionRepository exhibitionRepository;

    public List<User> getAll() {
        return userRepository.findAll();
    }

    public User getById(Long id) {
        return userRepository.findById(id);
    }

    public HttpStatus checkCreate(User data) {
        if (data.getUsername() == null || data.getUsername().isBlank()) {
            return HttpStatus.BAD_REQUEST;
        }
        // такой username уже есть - это конфликт
        if (userRepository.findByUsername(data.getUsername()) != null) {
            return HttpStatus.CONFLICT;
        }
        return null;
    }

    public HttpStatus checkUpdate(Long id, User data) {
        if (data.getUsername() != null) {
            if (data.getUsername().isBlank()) {
                return HttpStatus.BAD_REQUEST;
            }
            User other = userRepository.findByUsername(data.getUsername());
            if (other != null && !other.getId().equals(id)) {
                return HttpStatus.CONFLICT;
            }
        }
        return null;
    }

    public User create(User data) {
        // делаю новый объект и переношу только нужные поля,
        // id и время из запроса не беру, их ставит сервер
        User user = new User();
        user.setUsername(data.getUsername());
        user.setFullName(data.getFullName());
        user.setEmail(data.getEmail());
        user.setCreatedAt(LocalDateTime.now());
        return userRepository.add(user);
    }

    public User update(Long id, User data) {
        User user = userRepository.findById(id);

        // PATCH: меняю только то что прислали, null значит поле не трогаем
        if (data.getUsername() != null) {
            user.setUsername(data.getUsername());
        }
        if (data.getFullName() != null) {
            user.setFullName(data.getFullName());
        }
        if (data.getEmail() != null) {
            user.setEmail(data.getEmail());
        }
        return user;
    }

    public void delete(Long id) {
        // пользователь мог быть куратором, тогда у выставки убираю ссылку на него,
        // чтобы не осталось id несуществующего пользователя
        List<Exhibition> list = exhibitionRepository.findByCuratorId(id);
        for (Exhibition ex : list) {
            ex.setCuratorId(null);
        }
        userRepository.deleteById(id);
    }
}
