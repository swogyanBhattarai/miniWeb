package org.example.justdeepfried.dto;

import java.lang.reflect.Method;

public record Records (
        Object classInstance,
        Method method,
        String path
) {
}
