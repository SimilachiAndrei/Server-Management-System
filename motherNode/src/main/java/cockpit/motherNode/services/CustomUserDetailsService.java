package cockpit.motherNode.services;

import cockpit.motherNode.entities.User;
import cockpit.motherNode.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        // Attempt to find user by email
        Optional<User> userOpt = userRepository.findByEmail(identifier);

        if (userOpt.isPresent()) {
            return userOpt.get();
        }

        // If not found by email, attempt to find by username
        userOpt = userRepository.findByUsername(identifier);

        if (userOpt.isPresent()) {
            return userOpt.get();
        }

        throw new UsernameNotFoundException("User not found with email or username: " + identifier);
    }

}
