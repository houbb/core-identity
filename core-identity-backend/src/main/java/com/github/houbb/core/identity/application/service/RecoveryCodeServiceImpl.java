package com.github.houbb.core.identity.application.service;

import com.github.houbb.core.identity.application.domain.RecoveryCode;
import com.github.houbb.core.identity.application.domain.RecoveryCodeSet;
import com.github.houbb.core.identity.application.port.RecoveryCodeRepository;
import com.github.houbb.core.identity.application.port.RecoveryCodeSetRepository;
import com.github.houbb.core.identity.infrastructure.util.TokenUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

/**
 * Default implementation of RecoveryCodeService.
 */
public class RecoveryCodeServiceImpl implements RecoveryCodeService {

    private static final Logger log = LoggerFactory.getLogger(RecoveryCodeServiceImpl.class);

    private static final int DEFAULT_CODE_COUNT = 10;
    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // no 0/O/1/I for readability

    private final RecoveryCodeSetRepository codeSetRepo;
    private final RecoveryCodeRepository codeRepo;
    private final SecureRandom secureRandom = new SecureRandom();

    public RecoveryCodeServiceImpl(RecoveryCodeSetRepository codeSetRepo, RecoveryCodeRepository codeRepo) {
        this.codeSetRepo = codeSetRepo;
        this.codeRepo = codeRepo;
    }

    @Override
    @Transactional
    public List<String> generate(String userId) {
        // Revoke existing active set
        codeSetRepo.revokeByUserId(userId, System.currentTimeMillis());

        long now = System.currentTimeMillis();
        String setId = UUID.randomUUID().toString();
        List<String> plaintextCodes = new ArrayList<>();

        // Generate codes
        List<RecoveryCode> codes = new ArrayList<>();
        for (int i = 0; i < DEFAULT_CODE_COUNT; i++) {
            String plaintext = generateRecoveryCode();
            plaintextCodes.add(plaintext);

            RecoveryCode code = new RecoveryCode();
            code.setId(UUID.randomUUID().toString());
            code.setCodeSetId(setId);
            code.setCodeHash(hashCode(plaintext));
            code.setStatus("ACTIVE");
            code.setCreatedAt(now);
            codes.add(code);
        }

        // Save code set
        RecoveryCodeSet set = new RecoveryCodeSet();
        set.setId(setId);
        set.setUserId(userId);
        set.setStatus("ACTIVE");
        set.setTotalCount(DEFAULT_CODE_COUNT);
        set.setRemainingCount(DEFAULT_CODE_COUNT);
        set.setGeneratedAt(now);
        set.setVersion(1);
        codeSetRepo.save(set);

        // Batch save codes
        codeRepo.saveBatch(codes);

        log.info("Generated {} recovery codes for user {}", DEFAULT_CODE_COUNT, userId);
        return plaintextCodes;
    }

    @Override
    @Transactional
    public boolean verify(String userId, String code) {
        String codeHash = hashCode(code);
        RecoveryCode recoveryCode = codeRepo.findByCodeHash(codeHash);

        if (recoveryCode == null || !"ACTIVE".equals(recoveryCode.getStatus())) {
            return false;
        }

        // Verify the code belongs to the user's active set
        RecoveryCodeSet set = codeSetRepo.findByUserIdAndStatus(userId, "ACTIVE").orElse(null);
        if (set == null || !set.getId().equals(recoveryCode.getCodeSetId())) {
            return false;
        }

        // Mark as used
        long now = System.currentTimeMillis();
        codeRepo.markUsed(recoveryCode.getId(), now);
        codeSetRepo.decrementRemaining(set.getId(), set.getVersion());

        log.info("Recovery code used for user {}", userId);
        return true;
    }

    @Override
    public RecoveryCodeStatus getStatus(String userId) {
        RecoveryCodeSet set = codeSetRepo.findByUserIdAndStatus(userId, "ACTIVE").orElse(null);
        if (set == null) {
            return new RecoveryCodeStatus(0, 0, 0, false);
        }
        return new RecoveryCodeStatus(set.getRemainingCount(), set.getTotalCount(), set.getGeneratedAt(), true);
    }

    @Override
    @Transactional
    public List<String> regenerate(String userId) {
        // Revoke all codes in existing set
        codeSetRepo.revokeByUserId(userId, System.currentTimeMillis());

        // Generate new set
        return generate(userId);
    }

    private String generateRecoveryCode() {
        StringBuilder sb = new StringBuilder(20);
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 5; j++) {
                sb.append(CODE_CHARS.charAt(secureRandom.nextInt(CODE_CHARS.length())));
            }
            if (i < 3) {
                sb.append('-');
            }
        }
        return sb.toString();
    }

    private String hashCode(String plaintext) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(plaintext.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
