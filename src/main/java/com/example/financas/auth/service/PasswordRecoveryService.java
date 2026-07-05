package com.example.financas.auth.service;


import com.example.financas.auth.entity.entity.PasswordRecovery;
import com.example.financas.auth.repository.PasswordRecoveryRepository;
import com.example.financas.exceptions.NotFoundException;
import com.example.financas.exceptions.dto.ForbiddenException;
import com.example.financas.mail.MailService;
import com.example.financas.user.domain.entity.User;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;


import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class PasswordRecoveryService {

    private final PasswordRecoveryRepository passwordRecoveryRepository;
    private final MailService mailService;

    public PasswordRecoveryService(PasswordRecoveryRepository passwordRecoveryRepository, MailService mailService) {
        this.passwordRecoveryRepository = passwordRecoveryRepository;
        this.mailService = mailService;
    }

    public String generateRecoveryToken(User user) {

        String token = UUID.randomUUID().toString();

        String hash = DigestUtils.sha256Hex(token);

        PasswordRecovery passwordRecovery = new PasswordRecovery();
        passwordRecovery.setUser(user);
        passwordRecovery.setToken(hash);
        passwordRecovery.setExpirationDate(Instant.now().plus(10, java.time.temporal.ChronoUnit.MINUTES));
        passwordRecovery.setValid(true);

        this.passwordRecoveryRepository.save(passwordRecovery);
        return token;
    }

    public void sendRecoveryEmail(User user, String token) {
        this.mailService.sendResetPasswordEmail(user.getEmail(), token);
    }

    public void invalidAllTokens(User user) {
        List<PasswordRecovery> listPasswordRecovery = this.passwordRecoveryRepository.findByUserAndValidTrue(user);
        listPasswordRecovery.forEach(c -> {
            c.setValid(false);
        });
    }

    public PasswordRecovery validateAndGetToken(String token) {
        String hashToken = DigestUtils.sha256Hex(token);
        PasswordRecovery passwordRecovery = this.passwordRecoveryRepository.findByTokenAndValidTrue(hashToken)
                .orElseThrow(() -> new NotFoundException("Invalid or expired password recovery token"));

        if (passwordRecovery.getExpirationDate().isBefore(Instant.now())) {
            passwordRecovery.setValid(false);
            passwordRecoveryRepository.save(passwordRecovery);
            throw new ForbiddenException("Token has expired");
        }

        return passwordRecovery;
    }

    public void completeRecovery(PasswordRecovery passwordRecovery) {
        passwordRecovery.setValid(false);
        passwordRecovery.setUsedAt(Instant.now());
        passwordRecoveryRepository.save(passwordRecovery);
    }
}
