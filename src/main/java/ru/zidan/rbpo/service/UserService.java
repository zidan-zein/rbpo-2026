package ru.zidan.rbpo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.zidan.rbpo.security.PasswordRules;
import ru.zidan.rbpo.dto.UserAccessRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private final PasswordEncoder passwordEncoder;
    private final ExhibitionRepository exhibitionRepository;

    public List<User> getAll() {
        return userRepository.findAll();
    }

    public User getById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public HttpStatus checkCreate(User data) {
        if (!PasswordRules.valid(data.getPassword())) {
            return HttpStatus.BAD_REQUEST;
        }
        if (data.getUsername() == null || data.getUsername().isBlank()) {
            return HttpStatus.BAD_REQUEST;
        }
        if (data.getFullName() == null || data.getFullName().isBlank()) {
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
        if (data.getFullName() != null && data.getFullName().isBlank()) {
            return HttpStatus.BAD_REQUEST;
        }
        return null;
    }

    // нельзя удалить куратора открытой выставки, у открытой выставки куратор должен быть
    public HttpStatus checkDelete(Long id) {
        List<Exhibition> list = exhibitionRepository.findByCuratorId(id);
        for (Exhibition ex : list) {
            if (ex.getStatus().equals("OPEN")) {
                return HttpStatus.CONFLICT;
            }
        }
        return null;
    }

    public User create(User data) {
        // делаю новый объект и переношу только нужные поля,
        // id и время из запроса не беру, их ставит сервер
        User user = new User();
        user.setPassword(passwordEncoder.encode(data.getPassword()));
        user.setRole("USER");
        user.setEnabled(true);
        user.setUsername(data.getUsername());
        user.setFullName(data.getFullName());
        user.setEmail(data.getEmail());
        user.setCreatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    public User update(Long id, User data) {
        User user = userRepository.findById(id).orElse(null);

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
        return userRepository.save(user);
    }

    public User changeAccess(Long id, UserAccessRequest data) {
        User user = userRepository.findById(id).orElseThrow();
        if (data.getRole() != null) {
            user.setRole(data.getRole());
        }
        if (data.getEnabled() != null) {
            user.setEnabled(data.getEnabled());
        }
        return userRepository.save(user);
    }

    @Transactional
    public void delete(Long id) {
        // у выставок curator_id станет null сам, это ON DELETE SET NULL в базе
        userRepository.deleteById(id);
    }
}
