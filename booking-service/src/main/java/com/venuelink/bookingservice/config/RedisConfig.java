package com.venuelink.bookingservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

@Configuration
public class RedisConfig {

    @Bean
    StringRedisTemplate redisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }

    @Bean
    RedisScript<Long> seatReservationScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(SEAT_RESERVATION_LUA);
        script.setResultType(Long.class);
        return script;
    }

    private static final String SEAT_RESERVATION_LUA =
            "local currentSeats = tonumber(redis.call('GET', KEYS[1])) " +
            "if currentSeats == nil then return -2 end " +
            "local requestedSeats = tonumber(ARGV[1]) " +
            "if currentSeats < requestedSeats then return -1 end " +
            "redis.call('DECRBY', KEYS[1], requestedSeats) " +
            "return currentSeats - requestedSeats";
}