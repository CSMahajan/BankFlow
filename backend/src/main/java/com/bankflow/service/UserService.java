package com.bankflow.service;

import com.bankflow.dto.*;
import com.bankflow.entity.AuditAction;
import com.bankflow.entity.User;
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
        log.info("Customer registered successfully. User ID: [{}], Email: [{}]", savedUser.getId(), savedUser.getEmail());
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

        // 3. Generate JWT token
        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());

        log.info("User [{}] authenticated successfully with role [{}]", user.getEmail(), user.getRole());

        //****IMPORTANT---> DO NOT REMOVE BELOW COMMENT
        //Below is special case for auditing because before logging in we don't have security context of authenticated user
        auditLogService.log(
                user,
                AuditAction.LOGIN,
                "User logged in successfully"
        );
        // 4. Return token and user metadata
        return new AuthResponse(token, user.getEmail(), user.getRole().name(), user.getFullName());
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
        log.info("Updating profile details for user [{}]. New Name: [{}]", currentUser.getEmail(), request.fullName());

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
