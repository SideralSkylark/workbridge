// package com.workbridge.workbridge_app.auth;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.Mockito.*;

// import java.time.LocalDateTime;
// import java.util.List;
// import java.util.Optional;
// import java.util.Set;

// import com.workbridge.workbridge_app.auth.dto.*;
// import com.workbridge.workbridge_app.auth.exception.InvalidCredentialsException;
// import com.workbridge.workbridge_app.auth.exception.UserAlreadyExistsException;
// import com.workbridge.workbridge_app.auth.service.AuthenticationService;
// import com.workbridge.workbridge_app.auth.service.VerificationService;
// import com.workbridge.workbridge_app.security.JwtService;
// import com.workbridge.workbridge_app.user.entity.ApplicationUser;
// import com.workbridge.workbridge_app.user.entity.UserRole;
// import com.workbridge.workbridge_app.user.entity.UserRoleEntity;
// import com.workbridge.workbridge_app.user.exception.UserNotFoundException;
// import com.workbridge.workbridge_app.user.repository.UserRepository;
// import com.workbridge.workbridge_app.user.repository.UserRoleRepository;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Nested;
// import org.junit.jupiter.api.Test;
// import org.mockito.*;

// import org.springframework.security.crypto.password.PasswordEncoder;

// public class AuthenticationServiceTest {

//     @Mock private UserRepository userRepository;
//     @Mock private UserRoleRepository roleRepository;
//     @Mock private PasswordEncoder passwordEncoder;
//     @Mock private JwtService jwtService;
//     @Mock private VerificationService verificationService;

//     @InjectMocks private AuthenticationService authenticationService;

//     @Captor private ArgumentCaptor<ApplicationUser> userCaptor;

//     private final String username = "User1";
//     private final String email = "user@gmail.com";
//     private final String password = "user1234";
//     private final List<String> roles = List.of("SERVICE_SEEKER");

//     @BeforeEach
//     void setUp() {
//         MockitoAnnotations.openMocks(this);
//     }

//     private RegisterRequestDTO buildRegisterRequest() {
//         RegisterRequestDTO dto = new RegisterRequestDTO();
//         dto.setUsername(username);
//         dto.setEmail(email);
//         dto.setPassword(password);
//         dto.setRoles(roles);
//         dto.setStatus("INACTIVE");
//         return dto;
//     }

//     private ApplicationUser buildUser(boolean enabled) {
//         ApplicationUser user = new ApplicationUser();
//         user.setUsername(username);
//         user.setEmail(email);
//         user.setPassword("hashed");
//         user.setEnabled(enabled);
//         user.setUpdatedAt(LocalDateTime.now());
//         user.setRoles(Set.of(new UserRoleEntity(UserRole.SERVICE_SEEKER)));
//         return user;
//     }

//     @Nested
//     class RegisterTests {

//         @Test
//         void shouldRegisterUserSuccessfully() {
//             RegisterRequestDTO request = buildRegisterRequest();
//             UserRoleEntity roleEntity = new UserRoleEntity();
//             roleEntity.setRole(UserRole.SERVICE_SEEKER);

//             when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
//             when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
//             when(passwordEncoder.encode(password)).thenReturn("hashed");
//             when(roleRepository.findByRole(UserRole.SERVICE_SEEKER)).thenReturn(Optional.of(roleEntity));

//             RegisterResponseDTO response = authenticationService.register(request);

//             verify(userRepository).save(userCaptor.capture());
//             ApplicationUser savedUser = userCaptor.getValue();

//             assertEquals(email, savedUser.getEmail());
//             assertFalse(savedUser.isEnabled());
//             assertEquals(email, response.getEmail());
//             verify(verificationService).createAndSendVerificationToken(savedUser);
//         }

//         @Test
//         void shouldThrowExceptionWhenUsernameExists() {
//             RegisterRequestDTO request = buildRegisterRequest();
//             when(userRepository.existsByUsername(username)).thenReturn(true);
//             assertThrows(UserAlreadyExistsException.class, () -> authenticationService.register(request));
//         }

//         @Test
//         void shouldThrowExceptionWhenEmailExists() {
//             RegisterRequestDTO request = buildRegisterRequest();
//             when(userRepository.existsByEmail(email)).thenReturn(true);
//             assertThrows(UserAlreadyExistsException.class, () -> authenticationService.register(request));
//         }

//         @Test
//         void shouldThrowExceptionWhenRoleIsInvalid() {
//             RegisterRequestDTO request = buildRegisterRequest();
//             when(userRepository.existsByUsername(any())).thenReturn(false);
//             when(userRepository.existsByEmail(any())).thenReturn(false);
//             when(roleRepository.findByRole(UserRole.SERVICE_SEEKER)).thenThrow(IllegalArgumentException.class);
//             assertThrows(IllegalArgumentException.class, () -> authenticationService.register(request));
//         }
//     }

//     @Nested
//     class VerifyTests {

//         @Test
//         void shouldVerifyEmailSuccessfully() {
//             ApplicationUser user = buildUser(false);
//             when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
//             when(jwtService.generateToken(user)).thenReturn("jwt-token");

//             AuthenticationResponseDTO response = authenticationService.verify(new EmailVerificationDTO(email, "123456"));

//             assertTrue(user.isEnabled());
//             assertEquals("jwt-token", response.getAccessToken());
//             verify(verificationService).verifyToken(email, "123456");
//             verify(userRepository).save(user);
//         }
//     }

//     @Nested
//     class ResendVerificationCodeTests {

//         @Test
//         void shouldResendVerificationCodeIfNotVerified() {
//             ApplicationUser user = buildUser(false);
//             when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

//             RegisterResponseDTO response = authenticationService.resendVerificationCode(email);

//             assertEquals(email, response.getEmail());
//             verify(verificationService).deleteExistingToken(email);
//             verify(verificationService).createAndSendVerificationToken(user);
//         }

//         @Test
//         void shouldNotResendIfUserAlreadyVerified() {
//             ApplicationUser user = buildUser(true);
//             when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

//             RegisterResponseDTO response = authenticationService.resendVerificationCode(email);

//             assertEquals(email, response.getEmail());
//             verify(verificationService, never()).createAndSendVerificationToken(any());
//             verify(verificationService, never()).deleteExistingToken(any());
//         }
//     }

//     @Nested
//     class LoginTests {

//         @Test
//         void shouldLoginSuccessfully() {
//             ApplicationUser user = buildUser(true);
//             when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
//             when(passwordEncoder.matches("user1234", "hashed")).thenReturn(true);
//             when(jwtService.generateToken(user)).thenReturn("jwt-token");

//             AuthenticationResponseDTO response = authenticationService.login(new LoginRequestDTO(email, "user1234"));

//             assertEquals("jwt-token", response.getAccessToken());
//             assertEquals(email, response.getEmail());
//         }

//         @Test
//         void shouldThrowIfUserNotFound() {
//             when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
//             assertThrows(InvalidCredentialsException.class,
//                     () -> authenticationService.login(new LoginRequestDTO(email, "pass")));
//         }

//         @Test
//         void shouldThrowIfPasswordInvalid() {
//             ApplicationUser user = buildUser(true);
//             when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
//             when(passwordEncoder.matches("wrongpass", "hashed")).thenReturn(false);
//             assertThrows(InvalidCredentialsException.class,
//                     () -> authenticationService.login(new LoginRequestDTO(email, "wrongpass")));
//         }

//         @Test
//         void shouldThrowIfUserNotVerified() {
//             ApplicationUser user = buildUser(false);
//             when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
//             when(passwordEncoder.matches("user1234", "hashed")).thenReturn(true);
//             assertThrows(UserNotFoundException.class,
//                     () -> authenticationService.login(new LoginRequestDTO(email, "user1234")));
//         }
//     }
// }
