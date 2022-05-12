package account.service;

import account.entity.User;
import account.entity.UserDetailsImpl;
import account.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.Optional;

@Service
public class UserDetailsServiceImpl implements UserService, UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<User> userEntity = userRepository.findByEmail(email.toLowerCase());
        if(userEntity.isEmpty()) {
            throw new UsernameNotFoundException("No user found");
        }
        User user = userEntity.get();
        return new UserDetailsImpl(user);
    }

    @Override
    public User signUp(User user) {
        return userRepository.save(user);
    }

    public boolean findUser(String email){
        return (userRepository.findByEmail(email).isPresent());
    }

    public void updateUser(User user) {
        userRepository.updateUser(user.getPassword(),user.getId());
    }
}
