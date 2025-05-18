package com.ttn.nexuscart.security.config;

import com.ttn.nexuscart.entity.Role;
import com.ttn.nexuscart.entity.users.User;
import com.ttn.nexuscart.repositories.RoleRepository;
import com.ttn.nexuscart.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Component
public class AdminBootstrap implements ApplicationRunner {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    @Value("${admin.email}")
    private String email;
    @Value("${admin.password}")
    private String password;

    @Autowired
    public AdminBootstrap(UserRepository userRepository, RoleRepository roleRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Optional<User> existingAdmin = userRepository.findByEmail(email);

        if (existingAdmin.isEmpty()) {
            User admin = new User();
            admin.setFirstName("Deepika");
            admin.setMiddleName("Rani");
            admin.setLastName("Bhagat");
            admin.setEmail(email);
            admin.setPassword(bCryptPasswordEncoder.encode(password));
            admin.setIsLocked(false);
            admin.setIsActive(true);
            Role role = roleRepository.findByAuthority("ADMIN");
            Set<Role> roles = new HashSet<>();
            roles.add(role);
            admin.setRoles(roles);

            userRepository.save(admin);
        }
    }
}