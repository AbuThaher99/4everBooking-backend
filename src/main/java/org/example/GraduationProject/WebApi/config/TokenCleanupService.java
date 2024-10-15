package org.example.GraduationProject.WebApi.config;

import org.example.GraduationProject.Common.Entities.Token;
import org.example.GraduationProject.Core.Repsitories.TokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TokenCleanupService {

    @Autowired
    private TokenRepository tokenRepository;

    @Scheduled(cron = "0 0 * * * ?") // every hour at the top of the hour
    public void cleanupExpiredTokens() {
        List<Token> expiredTokens = tokenRepository.findAllByExpiredTrue();
        for (Token token : expiredTokens) {
            tokenRepository.deleteById(token.getId());
        }
    }
}
