package io.firebolt.jdbi;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.jdbi.v3.sqlobject.Handler;
import org.jdbi.v3.sqlobject.HandlerDecorator;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Arrays;

public class CachingHandlerDecorator implements HandlerDecorator  {

    private final Cache<String, Object> cache;

    public CachingHandlerDecorator() {
        this.cache = Caffeine.newBuilder()
                // divide this roughly by the number of queries per users to find out how much user data we can keep in
                // memory in parallel
                .maximumSize(10_000)
                .expireAfterWrite(Duration.ofMinutes(20))
                .build();
    }

    @Override
    public Handler decorateHandler(final Handler base, final Class<?> sqlObjectType, final Method method) {
        return (target, args, handle) -> {
            // this could be optimized a little to reduce the cache key by not included the dao name or a hash representation?
            String cacheKey = method.getDeclaringClass().getSimpleName() + "|" + method.getName() + "|" + method.getParameterCount() + "|" + Arrays.asList(args);
            Object response = cache.getIfPresent(cacheKey);
            if (response == null) {
                response = base.invoke(target, args, handle);
                cache.put(cacheKey, response);
            }

            return response;
        };
    }
}
