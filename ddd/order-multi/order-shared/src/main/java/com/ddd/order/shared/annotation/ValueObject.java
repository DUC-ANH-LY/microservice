package com.ddd.order.shared.annotation;

import java.lang.annotation.*;

/**
 * Marker annotation indicating the annotated class is a DDD Value Object.
 * Value Objects are immutable and defined entirely by their attributes.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ValueObject {
}
