package account.controller;


import account.entity.User;
import account.entity.UserDetailsImpl;
import account.exception.ChangePasswordException;
import account.exception.ExistingPasswordException;
import account.exception.ExistingUserException;
import account.exception.HackersPasswordException;
import account.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.util.*;


@RestController()
@Validated
public class AccountController {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private BCryptPasswordEncoder encoder;


    @PostMapping("/api/auth/signup")
    public ResponseEntity<?> authSignUp(@Valid @RequestBody User detail){
        
        String name = detail.getName();
        String lastname = detail.getLastname();
        String email = detail.getEmail();
        String password = detail.getPassword();
        String role = "ROLE_USER";

        if(Objects.isNull(name) || name.isEmpty()
                || (Objects.isNull(lastname) || lastname.isEmpty())
                || (Objects.isNull(email) || email.isEmpty())
                || (Objects.isNull(password) || password.isEmpty())
                || !email.endsWith("@acme.com")
        ) return ResponseEntity.badRequest().build();

        email = email.toLowerCase();

        if(userDetailsService.findUser(email))
            throw new ExistingUserException();

        if(getBreachedPasswords().contains(password)){
            throw new HackersPasswordException();
        }

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


    /**
     * Method for Password  change
     *
     * @param userPassword The new password for authentication
     * throws ExistingPassword Exception if the new password equals
     *  the old password.
     * throws HackersPasswordException if new password is contained in
     * likely breached passwords
     * */
    @PostMapping("/api/auth/changepass")
    public ResponseEntity<?> changePassword(@RequestBody Map<String,String> userPassword){

        if(userPassword == null)  return ResponseEntity.badRequest().build();

        String new_password = userPassword.get("new_password");

        if(new_password.length() <12){
            throw new ChangePasswordException();
        }


        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if(auth == null ||new_password == null
        || new_password.isEmpty()) return ResponseEntity.badRequest().build();

        User user = ((UserDetailsImpl)auth.getPrincipal()).getUser();
        String password = user.getPassword();

        if(getBreachedPasswords().contains(new_password)){
            throw new HackersPasswordException();
        }

        if(encoder.matches(new_password, password)){
            throw new ExistingPasswordException();
        }

        //Encode Password with PasswordEncoder object
        user.setPassword(encoder.encode(new_password));
        userDetailsService.updateUser(user);
        Map<String, String> map = new HashMap<>();
        map.put("email", user.getEmail());
        map.put("status", "The password has been updated successfully");
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
