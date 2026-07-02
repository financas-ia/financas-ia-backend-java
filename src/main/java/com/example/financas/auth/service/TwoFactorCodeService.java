package com.example.financas.auth.service;

import com.example.financas.auth.entity.entity.TwoFactorCode;
import com.example.financas.auth.repository.TwoFactorCodeRepository;
import com.example.financas.exceptions.NotFoundException;
import com.example.financas.mail.MailService;
import com.example.financas.user.domain.entity.User;
import com.example.financas.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@Service
public class TwoFactorCodeService {

    private final UserRepository userRepository;
    private final MailService mailService;
    private final TwoFactorCodeRepository twoFactorCodeRepository;

    public TwoFactorCodeService(UserRepository userRepository, MailService mailService, TwoFactorCodeRepository twoFactorCodeRepository) {
        this.userRepository = userRepository;
        this.mailService = mailService;
        this.twoFactorCodeRepository = twoFactorCodeRepository;
    }

    public String genareteTwoFactorCode(){
        SecureRandom random = new SecureRandom();
        int number = random.nextInt(1000000);
        return String.format("%06d", number);
    }

    public void sendTwoFactorCode(String email, String code) {
        this.mailService.sendTwoFactorCode(email, code);
    }

    public void SaveTwoFactorCode(User user, String code) {
        List<TwoFactorCode>existingCodes = this.twoFactorCodeRepository.findByUserAndValidTrue(user);
        existingCodes.forEach(c -> c.setValid(false));
        this.twoFactorCodeRepository.saveAll(existingCodes);

        TwoFactorCode twoFactorCode = new TwoFactorCode();
        twoFactorCode.setUser(user);
        twoFactorCode.setCode(code);
        twoFactorCode.setExpirationTime(genExpirationTime());
        twoFactorCode.setValid(true);

        this.twoFactorCodeRepository.save(twoFactorCode);
    }

    public boolean validTwoFactorCode(User user, String code) {
        Optional<TwoFactorCode>twoFactorCode = this.twoFactorCodeRepository.findByCodeAndUserAndValidTrue(code, user);
        if (twoFactorCode.isEmpty()) {
            throw new NotFoundException("Not Found or Invalid Two Factor Code");
        }

        if (twoFactorCode.get().getExpirationTime().isBefore(Instant.now())) {
            throw new NotFoundException("Two Factor Code Expired");
        }

        if (!twoFactorCode.get().isValid()) {
            throw new NotFoundException("Two Factor Code Invalid");
        }

        twoFactorCode.get().setValid(false);
        this.twoFactorCodeRepository.save(twoFactorCode.get());
        return true;
    }

    public Instant genExpirationTime() {
        return Instant.now().plus(10, java.time.temporal.ChronoUnit.MINUTES);
    }
}
