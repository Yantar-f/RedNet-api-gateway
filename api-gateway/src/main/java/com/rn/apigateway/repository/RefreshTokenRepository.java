package com.rn.apigateway.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class RefreshTokenRepository {
    private final ReactiveRedisTemplate<String, String> template;




    @Autowired
    public RefreshTokenRepository(ReactiveRedisTemplate<String, String> template) {
        this.template = template;
    }



    public Mono<Boolean> save(String userId, String refreshTokenEntity) {
        return template.opsForValue().set(userId, refreshTokenEntity);
    }

    public Mono<String> getAndSave(String userId, String refreshTokenEntity) {
        return template.opsForValue().getAndSet(userId, refreshTokenEntity);
    }

    public Mono<Boolean> deleteById(String userId) {
        return template.opsForValue().delete(userId);
    }

    public Mono<String> findById(String userId) {
        return template.opsForValue().get(userId);
    }

    public Mono<String> getAndDeleteById(String userId) {
        return template.opsForValue().getAndDelete(userId);
    }
}
