package com.websementic.fmp.security.service;

import com.websementic.fmp.configuration.CustomRedisCacheConfigurations;
import com.websementic.fmp.exeption.BadArgumentException;
import com.websementic.fmp.exeption.NotFoundException;
import com.websementic.fmp.user.model.Session;
import com.websementic.fmp.user.model.User;
import com.websementic.fmp.user.repository.SessionRepository;
import jakarta.validation.constraints.NotNull;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class SessionService {

    private final SessionRepository sessionRepository;
    private final CacheManager cacheManager;
    //TODO: implement a scheduled to check if the redis cache is reachable
    private Cache sessionCache;

    public SessionService(SessionRepository sessionRepository, CacheManager cacheManager) {
        this.sessionRepository = sessionRepository;
        this.cacheManager = cacheManager;
        this.sessionCache = cacheManager.getCache(CustomRedisCacheConfigurations.SESSION_IDS_CACHE_NAME);
    }

    public Session create(User user, String userAgent) {
        return sessionRepository.save(new Session(user, userAgent, LocalDateTime.now().plusDays(7)));
    }

    public Session findById(String string) throws NotFoundException {
        return sessionRepository.findById(string).orElseThrow(() -> new NotFoundException("Session not found"));
    }

    public void terminate(Session session) {
        session.setExpiredAt(LocalDateTime.now());
        sessionRepository.save(session);
        try {
            if (sessionCache != null) {
                sessionCache.put(session.getId(), session.getExpiredAt()
                        .toEpochSecond(ZoneOffset.systemDefault().getRules().getOffset(Instant.now())));
            }
        } catch (Exception ignored) {
        }


    }

    public void extendSessionExpiration(@NotNull Session session, long seconds) throws BadArgumentException {
        try {
            Assert.notNull(session, "Session must not be null");
            Assert.isTrue(session.getExpiredAt().isAfter(LocalDateTime.now()), "Session is expired");
            Assert.isTrue(seconds >= 0, "You must provide a valid expiration");
        } catch (Exception e) {
            throw new BadArgumentException(e);
        }
        session.setExpiredAt(LocalDateTime.now().plusSeconds(seconds));
        sessionRepository.save(session);
    }
}
