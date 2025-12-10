package si.fri.prpo.authservice.service;

import si.fri.prpo.authservice.dto.LoginRequest;
import si.fri.prpo.authservice.dto.LoginResponse;
import si.fri.prpo.authservice.dto.RegisterRequest;
import si.fri.prpo.authservice.dto.UserResponse;
import si.fri.prpo.authservice.entity.User;
import si.fri.prpo.authservice.repository.UserRepository;
import si.fri.prpo.authservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        // Preveri ali uporabnik že obstaja
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Ustvari novega uporabnika
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER");

        User savedUser = userRepository.save(user);

        return new UserResponse(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getRole(),
                savedUser.getCreatedAt());
    }

    public LoginResponse login(LoginRequest request) {
        // Najdi uporabnika
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        // Preveri geslo
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }

        // Generiraj JWT token
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());

        return new LoginResponse(
                token,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole());
    }
}
