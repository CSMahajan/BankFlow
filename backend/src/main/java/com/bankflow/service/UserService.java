package com.bankflow.service;

import com.bankflow.dto.*;
import com.bankflow.entity.AuditAction;
import com.bankflow.entity.User;
import com.bankflow.entity.VerificationToken;
import com.bankflow.exception.EmailVerificationException;
import com.bankflow.exception.ResourceNotFoundException;
import com.bankflow.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final CardRepository cardRepository;
    private final LoanRepository loanRepository;
    private final FixedDepositRepository fixedDepositRepository;
    private final JwtService jwtService;
    private final VerificationTokenService verificationTokenService;
    private final EmailService emailService;
    private final AuditLogService auditLogService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void registerCustomer(RegisterRequest request) {
        log.info("Attempting customer registration for email [{}]", request.email());

        if (userRepository.existsByEmail(request.email())) {
            log.warn("Registration failed: Email [{}] is already registered", request.email());
            throw new IllegalArgumentException("Email is already registered");
        }

        String encodedPassword = passwordEncoder.encode(request.password());

        User user = User.builder()
                .fullName(request.fullName())
                .email(request.email())
                .password(encodedPassword)
                .role(User.Role.CUSTOMER)
                .build();

        User savedUser = userRepository.save(user);

        VerificationToken verificationToken = verificationTokenService.createVerificationToken(savedUser);

        emailService.sendVerificationEmail(savedUser, verificationToken.getToken());
        emailService.sendVerificationEmail(savedUser, verificationToken.getToken());

        log.info("Customer registered successfully. User ID: [{}], Email: [{}]",
                savedUser.getId(), savedUser.getEmail());
        auditLogService.log(
                savedUser,
                AuditAction.USER_REGISTERED,
                "Customer account registered"
        );
    }

    public AuthResponse login(LoginRequest request) {
        log.info("Attempting authentication for email [{}]", request.email());

        // 1. Fetch user by email or throw uniform error for security
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                    log.warn("Authentication failed for email [{}]: Account not found", request.email());
                    return new BadCredentialsException("Invalid email or password");
                });

        // 2. Compare plain password with stored BCrypt hash
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            log.warn("Authentication failed for email [{}]: Password mismatch", request.email());
            throw new BadCredentialsException("Invalid email or password");
        }

        if (!user.isEmailVerified()) {
            log.warn("Authentication failed for email [{}]: Email not verified",
                    request.email());

            throw new EmailVerificationException(
                    "Please verify your email address before logging in."
            );
        }

        // 3. Generate JWT token
        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());

        log.info("User [{}] authenticated successfully with role [{}]", user.getEmail(), user.getRole());

        auditLogService.log(
                user,
                AuditAction.LOGIN,
                "User logged in successfully"
        );
        // 4. Return token and user metadata
        return new AuthResponse(token, user.getEmail(), user.getRole().name(), user.getFullName());
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {

        User currentUser = getAuthenticatedUser();

        log.info("Password change requested by [{}]", currentUser.getEmail());

        if (!passwordEncoder.matches(request.currentPassword(), currentUser.getPassword())) {
            throw new BadCredentialsException("Current password is incorrect.");
        }

        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match.");
        }

        if (passwordEncoder.matches(request.newPassword(), currentUser.getPassword())) {
            throw new IllegalArgumentException(
                    "New password must be different from the current password.");
        }

        currentUser.setPassword(passwordEncoder.encode(request.newPassword()));

        userRepository.save(currentUser);

        auditLogService.log(AuditAction.PASSWORD_CHANGED, "Password changed successfully");

        log.info("Password changed successfully for [{}]", currentUser.getEmail());
    }

    @Transactional
    public void createAdminAccount(CreateAdminRequest request) {
        log.info("Attempting ADMIN account creation for email [{}]", request.email());

        if (userRepository.existsByEmail(request.email())) {
            log.warn("Admin account creation failed: Email [{}] is already registered", request.email());
            throw new IllegalArgumentException("Email is already registered");
        }

        String encodedPassword = passwordEncoder.encode(request.password());

        User adminUser = User.builder()
                .fullName(request.fullName())
                .email(request.email())
                .password(encodedPassword)
                .role(User.Role.ADMIN) // Explicitly assign ADMIN role
                .build();

        User savedAdmin = userRepository.save(adminUser);
        log.info("ADMIN account created successfully. User ID: [{}], Email: [{}]", savedAdmin.getId(), savedAdmin.getEmail());
        auditLogService.log(
                savedAdmin,
                AuditAction.USER_REGISTERED,
                "Administrator account created"
        );
    }

    @Transactional
    public void resendVerificationEmail(
            ResendVerificationRequest request) {

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No account found with this email address."
                        ));

        if (user.isEmailVerified()) {
            throw new EmailVerificationException(
                    "Your email address is already verified."
            );
        }

        VerificationToken verificationToken =
                verificationTokenService.createVerificationToken(user);

        emailService.sendVerificationEmail(
                user,
                verificationToken.getToken()
        );

        auditLogService.log(
                user,
                AuditAction.VERIFICATION_EMAIL_RESENT,
                "Verification email resent"
        );
    }

    @Transactional
    public void forgotPassword(
            ForgotPasswordRequest request) {

        log.info("Forgot password requested for email [{}]",
                request.email());

        User user = userRepository.findByEmail(request.email()).orElse(null);

        if (user == null) {
            log.info("Forgot password requested for non-existing email [{}]", request.email());
            return;
        }

        VerificationToken token = verificationTokenService.createPasswordResetToken(user);

        emailService.sendPasswordResetEmail(user, token.getToken());

        log.info("Password reset email sent to [{}]",
                user.getEmail());
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {

        log.info("Reset password requested");

        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new IllegalArgumentException(
                    "Passwords do not match.");
        }

        VerificationToken verificationToken =
                verificationTokenService
                        .validatePasswordResetToken(
                                request.token());

        User user = verificationToken.getUser();

        user.setPassword(
                passwordEncoder.encode(
                        request.newPassword()));

        // Our agreed design
        user.setEmailVerified(true);

        userRepository.save(user);

        verificationToken.setUsed(true);

        log.info("Password reset successful for user [{}]", user.getEmail());

        auditLogService.log(user, AuditAction.PASSWORD_CHANGED, "Password reset successfully"
        );
    }

    @Transactional(readOnly = true)
    public List<UserSummaryResponse> getAllUsers() {
        log.info("Fetching all users for admin");

        return userRepository.findAll()
                .stream()
                .map(user -> new UserSummaryResponse(
                        user.getId(),
                        user.getFullName(),
                        user.getEmail(),
                        user.getRole().name(),
                        user.getCreatedAt(),
                        accountRepository.countByUserId(user.getId())
                ))
                .toList();
    }

    @Transactional
    public void updateCustomerProfile(UpdateProfileRequest request) {
        User currentUser = getAuthenticatedUser();
        log.info("Updating profile details for user [{}]. New Name: [{}]",
                currentUser.getEmail(), request.fullName());

        currentUser.setFullName(request.fullName());
        userRepository.save(currentUser);

        log.info("Profile updated successfully for user [{}]", currentUser.getEmail());
        auditLogService.log(
                AuditAction.PROFILE_UPDATED,
                "Updated profile information"
        );
    }

    @Transactional(readOnly = true)
    public UserMeResponse getCurrentUser(String email) {
        log.info("Fetching profile for email [{}]", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Profile fetch failed: No user found for email [{}]", email);
                    return new ResourceNotFoundException("User not found for email: " + email);
                });

        return new UserMeResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole().name()
        );
    }

    @Transactional(readOnly = true)
    public UserDetailsResponse getUserDetails(Long userId) {

        log.info("Fetching details for user [{}]", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        return new UserDetailsResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole().name(),
                user.getCreatedAt(),

                accountRepository.countByUserId(userId),
                cardRepository.countByAccountUserId(userId),
                loanRepository.countByUserId(userId),
                fixedDepositRepository.countByUserId(userId),

                accountRepository.getTotalBalance(userId),
                loanRepository.getOutstandingLoanAmount(userId)
        );
    }

    @Transactional(readOnly = true)
    public List<AdminUserAccountResponse> getUserAccounts(Long userId) {

        log.info("Fetching accounts for user [{}]", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        return accountRepository.findByUserId(user.getId())
                .stream()
                .map(account -> new AdminUserAccountResponse(

                        account.getAccountNumber(),
                        account.getAccountType(),
                        account.getCurrentBalance(),
                        account.getAccountStatus(),
                        account.getBranchName(),
                        account.getCreatedAt()

                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AdminUserCardResponse> getUserCards(Long userId) {

        log.info("Fetching cards for user [{}]", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        return cardRepository.findByAccountUserId(user.getId())
                .stream()
                .map(card -> new AdminUserCardResponse(

                        card.getCardNumber(),
                        card.getCardType(),
                        card.getCardStatus(),
                        card.getDailyLimit(),
                        card.getExpiryDate()

                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AdminUserLoanResponse> getUserLoans(Long userId) {

        log.info("Fetching loans for user [{}]", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        return loanRepository.findByUserId(user.getId())
                .stream()
                .map(loan -> new AdminUserLoanResponse(

                        loan.getLoanNumber(),
                        loan.getLoanType(),
                        loan.getStatus(),
                        loan.getPrincipalAmount(),
                        loan.getRemainingBalance(),
                        loan.getMonthlyEmi(),
                        loan.getTenureMonths(),
                        loan.getNextDueDate()

                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AdminUserFixedDepositResponse> getUserFixedDeposits(Long userId) {

        log.info("Fetching fixed deposits for user [{}]", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        return fixedDepositRepository.findByUserId(user.getId())
                .stream()
                .map(fd -> new AdminUserFixedDepositResponse(

                        fd.getFdNumber(),
                        fd.getDepositAmount(),
                        fd.getInterestRate(),
                        fd.getMaturityDate(),
                        fd.getMaturityAmount(),
                        fd.getStatus()

                ))
                .toList();
    }

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> {
                    log.error("Authentication Context Error: User [{}] not found in database", auth.getName());
                    return new IllegalArgumentException("Authenticated user not found");
                });
    }
}
