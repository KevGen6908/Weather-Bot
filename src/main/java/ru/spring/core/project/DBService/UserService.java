package ru.spring.core.project.DBService;

import org.springframework.stereotype.Service;
import ru.spring.core.project.entity.User;

import java.util.List;

@Service
public interface UserService {
    User addUser(User user);
    void deleteUserById(Long id);
    void deleteUserByChatId(Long chatId);
    List<User> getAll();
    List<User> getUsersByChatId(Long chatId);


}
