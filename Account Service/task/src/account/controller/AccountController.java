package account.controller;


import account.entity.User;
import account.entity.UserDetailsImpl;
import account.exception.ExistingUserException;
import account.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.util.*;


@RestController()
public class AccountController {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;


    @Autowired
    private PasswordEncoder encoder;


    @PostMapping("/api/auth/signup")
    public ResponseEntity<?> authSignUp(@RequestBody User detail){
        detail.setEmail(detail.getEmail().toLowerCase());

        if(userDetailsService.findUser(detail.getEmail()))
            throw new ExistingUserException();
        
        String name = detail.getName();
        String lastname = detail.getLastname();
        String email = detail.getEmail();
        String password = detail.getPassword();
        String role = "ROLE_USER";

        if(Objects.isNull(name) || name.isEmpty()
            || (Objects.isNull(lastname) || lastname.isEmpty()) ||
                (Objects.isNull(email) || email.isEmpty())
                || (Objects.isNull(password) || password.isEmpty()) ||
                !email.endsWith("@acme.com")
        )
            return ResponseEntity.badRequest().build();
            //return "Nothing";//new ResponseEntity(HttpStatus.BAD_REQUEST);
        
        User user = new User(name,lastname,email,password,role);
        user.setPassword(encoder.encode(password));
        user = userDetailsService.signUp(user);
        return ResponseEntity.ok().body(getMappedUser(userDetailsService.signUp(user)));
    }

    @GetMapping("/api/empl/payment")
    public  ResponseEntity<?> getAll(Authentication auth){
        User user = ((UserDetailsImpl)auth.getPrincipal()).getUser();

        return ResponseEntity.ok().body(getMappedUser(user));
    }

    @PostMapping("/api/auth/changepass")
    public ResponseEntity<?> changePassword(
            @Valid @RequestBody
            @Size(min = 12,message="The password length must be at least 12 chars!")
                    String newPassword, Authentication auth){

        User user = ((UserDetailsImpl)auth.getPrincipal()).getUser();
        String password = user.getPassword();

        if(encoder.matches(newPassword, password)){
            return ResponseEntity.badRequest().build();
        }

        if(getBreachedPasswords().contains(newPassword)){

        }

        user.setPassword(encoder.encode(newPassword));
        userDetailsService.updateUser(user);
        Map<String, String> map = new HashMap<>();
        map.put("email", user.getEmail());
        map.put("status", "The password has been updated");
        return ResponseEntity.ok().body(map);

    }

    private Map<String, Object> getMappedUser(User user){
        Map<String, Object> mappedUser = new HashMap<>();
        mappedUser.put("id", user.getId());
        mappedUser.put("name", user.getName());
        mappedUser.put("lastname", user.getLastname());
        mappedUser.put("email", user.getEmail());
        return mappedUser;
    }

    private List getBreachedPasswords(){
        return Arrays.asList("PasswordForJanuary", "PasswordForFebruary",
                "PasswordForMarch", "PasswordForApril", "PasswordForMay",
                "PasswordForJune", "PasswordForJuly", "PasswordForAugust",
                "PasswordForSeptember", "PasswordForOctober", "PasswordForNovember",
                "PasswordForDecember");
    }



}
