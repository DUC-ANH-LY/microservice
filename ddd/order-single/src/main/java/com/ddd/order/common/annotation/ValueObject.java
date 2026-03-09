package com.ddd.order.common.annotation;

import java.lang.annotation.*;

/**
 * Marker annotation indicating the annotated class is a DDD Value Object.
 * A Value Object has no conceptual identity and is defined entirely by its attributes.
 * Value Objects should be immutable.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ValueObject {
}
