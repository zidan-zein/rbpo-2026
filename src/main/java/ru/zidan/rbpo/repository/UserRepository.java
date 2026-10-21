package ru.zidan.rbpo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.zidan.rbpo.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    // если не найден, вернет null
    User findByUsername(String username);

}
