package account.service;

import account.entity.User;
import org.springframework.stereotype.Service;

@Service
public interface UserService {

    User signUp(User user);

}
