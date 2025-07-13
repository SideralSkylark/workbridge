package com.workbridge.workbridge_app.user;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.data.domain.*;

import com.workbridge.workbridge_app.booking.repository.BookingRepository;
import com.workbridge.workbridge_app.review.repository.ReviewRepository;
import com.workbridge.workbridge_app.service.repository.ServiceRepository;
import com.workbridge.workbridge_app.user.dto.ProviderRequestDTO;
import com.workbridge.workbridge_app.user.dto.UpdateUserProfileDTO;
import com.workbridge.workbridge_app.user.dto.UserResponseDTO;
import com.workbridge.workbridge_app.user.entity.*;
import com.workbridge.workbridge_app.user.exception.ProviderRequestNotFoundException;
import com.workbridge.workbridge_app.user.exception.UserNotFoundException;
import com.workbridge.workbridge_app.user.mapper.UserMapper;
import com.workbridge.workbridge_app.user.repository.*;
import com.workbridge.workbridge_app.user.service.UserService;

class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private UserRoleRepository userRoleRepository;
    @Mock private ProviderRequestRepository providerRequestRepository;
    @Mock private ReviewRepository reviewRepository;
    @Mock private BookingRepository bookingRepository;
    @Mock private ServiceRepository serviceRepository;
    @Mock private UserMapper userMapper;

    @InjectMocks private UserService userService;

    private ApplicationUser user;
    private ProviderRequest providerRequest;
    private UserResponseDTO userResponseDTO;
    private ProviderRequestDTO providerRequestDTO;
    private UpdateUserProfileDTO updateUserProfileDTO;

    private static final String USERNAME = "testuser";
    private static final String EMAIL = "testuser@example.com";
    private static final Long USER_ID = 123L;
    private static final Long PROVIDER_REQUEST_ID = 10L;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new ApplicationUser();
        user.setId(USER_ID);
        user.setUsername(USERNAME);
        user.setEmail(EMAIL);
        user.setEnabled(true);
        user.setDeleted(false);

        providerRequest = new ProviderRequest(user);
        providerRequest.setId(PROVIDER_REQUEST_ID);
        providerRequest.setApproved(false);

        userResponseDTO = new UserResponseDTO();
        userResponseDTO.setUsername(USERNAME);
        userResponseDTO.setEmail(EMAIL);

        providerRequestDTO = new ProviderRequestDTO();
        providerRequestDTO.setId(PROVIDER_REQUEST_ID);
        providerRequestDTO.setUsername(USERNAME);

        updateUserProfileDTO = new UpdateUserProfileDTO();
        updateUserProfileDTO.setUsername("updatedUser");
        updateUserProfileDTO.setEmail("updated@example.com");
        updateUserProfileDTO.setEnabled(false);
    }

    @Test
    void getAllUsers_shouldReturnPagedUsers() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<ApplicationUser> usersPage = new PageImpl<>(List.of(user));
        when(userRepository.findAll(pageable)).thenReturn(usersPage);
        when(userMapper.toDTO(user)).thenReturn(userResponseDTO);

        Page<UserResponseDTO> result = userService.getAllUsers(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUsername()).isEqualTo(USERNAME);
        verify(userRepository).findAll(pageable);
        verify(userMapper).toDTO(user);
    }

    @Test
    void getAllNonAdminUsers_shouldReturnList() {
        List<ApplicationUser> nonAdminUsers = List.of(user);
        when(userRepository.findAllNonAdminUsers()).thenReturn(nonAdminUsers);
        when(userMapper.toDTO(user)).thenReturn(userResponseDTO);

        List<UserResponseDTO> result = userService.getAllNonAdminUsers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUsername()).isEqualTo(USERNAME);
        verify(userRepository).findAllNonAdminUsers();
    }

    @Test
    void getUsersByRole_shouldReturnList() {
        when(userRepository.findAllByRole(UserRole.SERVICE_SEEKER)).thenReturn(List.of(user));
        when(userMapper.toDTO(user)).thenReturn(userResponseDTO);

        List<UserResponseDTO> result = userService.getUsersByRole(UserRole.SERVICE_SEEKER);

        assertThat(result).hasSize(1);
        verify(userRepository).findAllByRole(UserRole.SERVICE_SEEKER);
    }

    @Test
    void getAllProviderRequestNotApproved_shouldReturnPagedRequests() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<ProviderRequest> page = new PageImpl<>(List.of(providerRequest));
        when(providerRequestRepository.findByApprovedFalse(pageable)).thenReturn(page);
        when(userMapper.toDTO(providerRequest)).thenReturn(providerRequestDTO);

        Page<ProviderRequestDTO> result = userService.getAllProviderRequestNotApproved(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(PROVIDER_REQUEST_ID);
        verify(providerRequestRepository).findByApprovedFalse(pageable);
    }

    @Test
    void updateUser_shouldUpdateAndSaveUser() {
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ApplicationUser updated = userService.updateUser(USERNAME, updateUserProfileDTO);

        assertThat(updated.getUsername()).isEqualTo("updatedUser");
        assertThat(updated.getEmail()).isEqualTo("updated@example.com");
        assertThat(updated.isEnabled()).isFalse();
        verify(userRepository).save(user);
    }

    @Test
    void updateUser_shouldThrowIfUserNotFound() {
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(USERNAME, updateUserProfileDTO))
            .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void deleteByUsername_shouldSoftDeleteUserAndRelatedEntities() {
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        try (var mockedSecurity = Mockito.mockStatic(com.workbridge.workbridge_app.security.SecurityUtil.class)) {
            mockedSecurity.when(com.workbridge.workbridge_app.security.SecurityUtil::getAuthenticatedId).thenReturn(999L);

            userService.deleteByUsername(USERNAME);

            assertThat(user.isDeleted()).isTrue();
            assertThat(user.getDeletedByUserId()).isEqualTo(999L);
            assertThat(user.getDeletedAt()).isNotNull();
            verify(reviewRepository).softDeleteByUser(USER_ID, 999L);
            verify(bookingRepository).softDeleteBySeeker(USER_ID, 999L);
            verify(serviceRepository).softDeleteByProvider(USER_ID, 999L);
            verify(userRepository).save(user);
        }
    }

    @Test
    void deleteByUsername_shouldSkipIfAlreadyDeleted() {
        user.setDeleted(true);
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));

        userService.deleteByUsername(USERNAME);

        verify(reviewRepository, never()).softDeleteByUser(anyLong(), anyLong());
        verify(userRepository, never()).save(any());
    }

    @Test
    void requestToBecomeProvider_shouldSaveRequest() {
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
        user.getRoles().clear(); // make sure user is not a provider already
        when(providerRequestRepository.save(any())).thenReturn(providerRequest);

        userService.requestToBecomeProvider(USERNAME);

        verify(providerRequestRepository).save(any(ProviderRequest.class));
    }

    @Test
    void requestToBecomeProvider_shouldThrowIfAlreadyProvider() {
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
        user.addRole(new UserRoleEntity(UserRole.SERVICE_PROVIDER));

        assertThatThrownBy(() -> userService.requestToBecomeProvider(USERNAME))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("already a service provider");
    }

    @Test
    void approveProviderRequest_shouldApproveAndSave() {
        when(providerRequestRepository.findById(PROVIDER_REQUEST_ID)).thenReturn(Optional.of(providerRequest));
        when(userRoleRepository.findByRole(UserRole.SERVICE_PROVIDER)).thenReturn(Optional.of(new UserRoleEntity(UserRole.SERVICE_PROVIDER)));
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(providerRequestRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        userService.approveProviderRequest(PROVIDER_REQUEST_ID);

        assertThat(providerRequest.isApproved()).isTrue();
        verify(userRepository).save(user);
        verify(providerRequestRepository).save(providerRequest);
    }

    @Test
    void approveProviderRequest_shouldThrowIfRequestNotFound() {
        when(providerRequestRepository.findById(PROVIDER_REQUEST_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.approveProviderRequest(PROVIDER_REQUEST_ID))
            .isInstanceOf(ProviderRequestNotFoundException.class);
    }

    @Test
    void approveProviderRequest_shouldThrowIfAlreadyApproved() {
        providerRequest.setApproved(true);
        when(providerRequestRepository.findById(PROVIDER_REQUEST_ID)).thenReturn(Optional.of(providerRequest));

        assertThatThrownBy(() -> userService.approveProviderRequest(PROVIDER_REQUEST_ID))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("already approved");
    }

    @Test
    void isServiceProvider_shouldReturnTrueIfUserHasRole() {
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
        user.addRole(new UserRoleEntity(UserRole.SERVICE_PROVIDER));

        assertThat(userService.isServiceProvider(USERNAME)).isTrue();
    }

    @Test
    void isServiceProvider_shouldReturnFalseIfUserHasNotRole() {
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));

        assertThat(userService.isServiceProvider(USERNAME)).isFalse();
    }

    @Test
    void hasPendingProviderRequest_shouldReturnTrueIfExists() {
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
        when(providerRequestRepository.existsByUserAndApprovedFalse(user)).thenReturn(true);

        assertThat(userService.hasPendingProviderRequest(USERNAME)).isTrue();
    }

    @Test
    void hasPendingProviderRequest_shouldReturnFalseIfNotExists() {
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
        when(providerRequestRepository.existsByUserAndApprovedFalse(user)).thenReturn(false);

        assertThat(userService.hasPendingProviderRequest(USERNAME)).isFalse();
    }

    @Test
    void enableAccount_shouldEnableIfDisabled() {
        user.setEnabled(false);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        boolean changed = userService.enableAccount(EMAIL);

        assertThat(changed).isTrue();
        assertThat(user.isEnabled()).isTrue();
    }

    @Test
    void enableAccount_shouldReturnFalseIfAlreadyEnabled() {
        user.setEnabled(true);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        boolean changed = userService.enableAccount(EMAIL);

        assertThat(changed).isFalse();
        verify(userRepository, never()).save(any());
    }

    @Test
    void disableAccount_shouldDisableIfEnabled() {
        user.setEnabled(true);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        boolean changed = userService.disableAccount(EMAIL);

        assertThat(changed).isTrue();
        assertThat(user.isEnabled()).isFalse();
    }

    @Test
    void disableAccount_shouldReturnFalseIfAlreadyDisabled() {
        user.setEnabled(false);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        boolean changed = userService.disableAccount(EMAIL);

        assertThat(changed).isFalse();
        verify(userRepository, never()).save(any());
    }
}
