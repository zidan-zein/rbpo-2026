package ru.zidan.rbpo.repository;

import org.springframework.stereotype.Repository;
import ru.zidan.rbpo.entity.User;

import java.util.ArrayList;
import java.util.List;

// базы данных пока нет, поэтому храню все просто в списке
@Repository
public class UserRepository {

    private List<User> users = new ArrayList<>();
    private long nextId = 1;

    public List<User> findAll() {
        // отдаю копию списка, чтобы снаружи его не поменяли
        return new ArrayList<>(users);
    }

    public User findById(Long id) {
        for (User u : users) {
            if (u.getId().equals(id)) {
                return u;
            }
        }
        return null;
    }

    public User findByUsername(String username) {
        for (User u : users) {
            if (u.getUsername().equals(username)) {
                return u;
            }
        }
        return null;
    }

    public User add(User user) {
        user.setId(nextId);
        nextId++;
        users.add(user);
        return user;
    }

    public void deleteById(Long id) {
        User u = findById(id);
        if (u != null) {
            users.remove(u);
        }
    }
}
