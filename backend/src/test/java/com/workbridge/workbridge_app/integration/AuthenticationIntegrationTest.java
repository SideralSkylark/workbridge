// package com.workbridge.workbridge_app.integration;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.context.annotation.Import;
// import org.springframework.http.MediaType;
// import org.springframework.test.web.servlet.MockMvc;
// import org.springframework.test.web.servlet.ResultActions;

// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.workbridge.workbridge_app.dto.AuthenticationResponseDTO;
// import com.workbridge.workbridge_app.dto.EmailVerificationDTO;
// import com.workbridge.workbridge_app.dto.LoginRequestDTO;
// import com.workbridge.workbridge_app.dto.RegisterRequestDTO;
// import com.workbridge.workbridge_app.dto.RegisterResponseDTO;
// import com.workbridge.workbridge_app.entity.ApplicationUser;
// import com.workbridge.workbridge_app.repository.BookingRepository;
// import com.workbridge.workbridge_app.repository.ChatMessageRepository;
// import com.workbridge.workbridge_app.repository.ProviderRequestRepository;
// import com.workbridge.workbridge_app.repository.ServiceRepository;
// import com.workbridge.workbridge_app.repository.UserRepository;
// import com.workbridge.workbridge_app.service.AuthenticationService;

// @SpringBootTest
// @AutoConfigureMockMvc
// @Import(TestConfig.class)
// public class AuthenticationIntegrationTest {

//     @Autowired
//     private MockMvc mockMvc;

//     @Autowired
//     private ObjectMapper objectMapper;

//     @Autowired
//     private AuthenticationService authenticationService;

//     @Autowired
//     private UserRepository userRepository;

//     @Autowired
//     private ProviderRequestRepository providerRequestRepository;

//     @Autowired
//     private BookingRepository bookingRepository;

//     @Autowired
//     private ChatMessageRepository chatMessageRepository;

//     @Autowired
//     private ServiceRepository serviceRepository;

//     private RegisterRequestDTO registerRequest;
//     private LoginRequestDTO loginRequest;
//     private String verificationCode;
//     private String accessToken;

//     @BeforeEach
//     void setUp() {
//         // Clear the database before each test
//         providerRequestRepository.deleteAll();
//         bookingRepository.deleteAll();
//         serviceRepository.deleteAll();
//         chatMessageRepository.deleteAll();
//         userRepository.deleteAll();

//         // Setup test data
//         registerRequest = new RegisterRequestDTO();
//         registerRequest.setUsername("testuser");
//         registerRequest.setEmail("test@example.com");
//         registerRequest.setPassword("Test123!@#");
//         registerRequest.setStatus("PENDING");

//         loginRequest = LoginRequestDTO.builder()
//                 .email("test@example.com")
//                 .password("Test123!@#")
//                 .build();
//     }

//     @Test
//     void completeRegistrationFlow_ShouldSucceed() throws Exception {
//         // Step 1: Register
//         ResultActions registerResult = mockMvc.perform(post("/api/v1/auth/register")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(objectMapper.writeValueAsString(registerRequest)))
//                 .andExpect(status().isCreated());

//         RegisterResponseDTO registerResponse = objectMapper.readValue(
//                 registerResult.andReturn().getResponse().getContentAsString(),
//                 RegisterResponseDTO.class);
//         verificationCode = "123456"; // This should be obtained from the email service in a real scenario

//         // Verify user is created but not enabled
//         ApplicationUser user = userRepository.findByEmail(registerRequest.getEmail()).orElseThrow();
//         assertFalse(user.isEnabled());

//         // Step 2: Verify Email
//         EmailVerificationDTO verificationDTO = new EmailVerificationDTO(
//                 registerRequest.getEmail(),
//                 verificationCode
//         );

//         ResultActions verifyResult = mockMvc.perform(post("/api/v1/auth/verify")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(objectMapper.writeValueAsString(verificationDTO)))
//                 .andExpect(status().isOk());

//         AuthenticationResponseDTO verifyResponse = objectMapper.readValue(
//                 verifyResult.andReturn().getResponse().getContentAsString(),
//                 AuthenticationResponseDTO.class);
//         accessToken = verifyResponse.getToken();

//         // Verify user is now enabled
//         user = userRepository.findByEmail(registerRequest.getEmail()).orElseThrow();
//         assertTrue(user.isEnabled());

//         // Step 3: Login
//         ResultActions loginResult = mockMvc.perform(post("/api/v1/auth/login")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(objectMapper.writeValueAsString(loginRequest)))
//                 .andExpect(status().isOk());

//         AuthenticationResponseDTO loginResponse = objectMapper.readValue(
//                 loginResult.andReturn().getResponse().getContentAsString(),
//                 AuthenticationResponseDTO.class);
//         assertNotNull(loginResponse.getToken());
//     }

//     @Test
//     void passwordResetFlow_ShouldSucceed() throws Exception {
//         // First register and verify a user
//         completeRegistrationFlow_ShouldSucceed();

//         // Step 1: Request Password Reset
//         ResultActions resetRequestResult = mockMvc.perform(post("/api/v1/auth/reset-password-request")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content("{\"email\": \"" + registerRequest.getEmail() + "\"}"))
//                 .andExpect(status().isOk());

//         // Get the reset code from the response
//         String resetCode = objectMapper.readValue(
//                 resetRequestResult.andReturn().getResponse().getContentAsString(),
//                 String.class);

//         // Step 2: Reset Password
//         String newPassword = "NewTest123!@#";
//         ResultActions resetPasswordResult = mockMvc.perform(post("/api/v1/auth/reset-password")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content("{\"email\": \"" + registerRequest.getEmail() + 
//                         "\", \"code\": \"" + resetCode + 
//                         "\", \"newPassword\": \"" + newPassword + "\"}"))
//                 .andExpect(status().isOk());

//         // Step 3: Try logging in with new password
//         loginRequest.setPassword(newPassword);
//         mockMvc.perform(post("/api/v1/auth/login")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(objectMapper.writeValueAsString(loginRequest)))
//                 .andExpect(status().isOk());
//     }

//     @Test
//     void tokenValidationAndRefresh_ShouldSucceed() throws Exception {
//         // First register and verify a user
//         completeRegistrationFlow_ShouldSucceed();

//         // Step 1: Try accessing protected endpoint with access token
//         mockMvc.perform(get("/api/v1/users/me")
//                 .header("Authorization", "Bearer " + accessToken))
//                 .andExpect(status().isOk());

//         // Step 2: Try accessing with expired token (simulated)
//         String expiredToken = "expired.token.here";
//         mockMvc.perform(get("/api/v1/users/me")
//                 .header("Authorization", "Bearer " + expiredToken))
//                 .andExpect(status().isUnauthorized());

//         // Step 3: Refresh token
//         ResultActions refreshResult = mockMvc.perform(post("/api/v1/auth/refresh")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content("{\"token\": \"" + accessToken + "\"}"))
//                 .andExpect(status().isOk());

//         AuthenticationResponseDTO refreshResponse = objectMapper.readValue(
//                 refreshResult.andReturn().getResponse().getContentAsString(),
//                 AuthenticationResponseDTO.class);
//         String newAccessToken = refreshResponse.getToken();

//         // Step 4: Try accessing protected endpoint with new access token
//         mockMvc.perform(get("/api/v1/users/me")
//                 .header("Authorization", "Bearer " + newAccessToken))
//                 .andExpect(status().isOk());
//     }

//     @Test
//     void roleBasedAccessControl_ShouldEnforcePermissions() throws Exception {
//         // First register and verify a user (will be a SERVICE_SEEKER by default)
//         completeRegistrationFlow_ShouldSucceed();

//         // Step 1: Try accessing SERVICE_PROVIDER endpoint (should fail)
//         mockMvc.perform(get("/api/v1/services/me")
//                 .header("Authorization", "Bearer " + accessToken))
//                 .andExpect(status().isForbidden());

//         // Step 2: Request to become a provider
//         mockMvc.perform(post("/api/v1/users/me/request-to-become-provider")
//                 .header("Authorization", "Bearer " + accessToken))
//                 .andExpect(status().isOk());

//         // Step 3: Login as admin and approve the request
//         LoginRequestDTO adminLogin = LoginRequestDTO.builder()
//                 .email("admin@workbridge.com")
//                 .password("admin123!@#")
//                 .build();

//         ResultActions adminLoginResult = mockMvc.perform(post("/api/v1/auth/login")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(objectMapper.writeValueAsString(adminLogin)))
//                 .andExpect(status().isOk());

//         AuthenticationResponseDTO adminResponse = objectMapper.readValue(
//                 adminLoginResult.andReturn().getResponse().getContentAsString(),
//                 AuthenticationResponseDTO.class);

//         // Approve provider request
//         mockMvc.perform(put("/api/v1/admins/approve-provider/1")
//                 .header("Authorization", "Bearer " + adminResponse.getToken()))
//                 .andExpect(status().isOk());

//         // Step 4: Login as the user again to get new tokens with updated roles
//         ResultActions newLoginResult = mockMvc.perform(post("/api/v1/auth/login")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(objectMapper.writeValueAsString(loginRequest)))
//                 .andExpect(status().isOk());

//         AuthenticationResponseDTO newLoginResponse = objectMapper.readValue(
//                 newLoginResult.andReturn().getResponse().getContentAsString(),
//                 AuthenticationResponseDTO.class);

//         // Step 5: Try accessing SERVICE_PROVIDER endpoint (should succeed)
//         mockMvc.perform(get("/api/v1/services/me")
//                 .header("Authorization", "Bearer " + newLoginResponse.getToken()))
//                 .andExpect(status().isOk());
//     }
// } 