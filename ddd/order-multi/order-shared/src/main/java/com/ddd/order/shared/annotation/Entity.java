package com.ddd.order.shared.annotation;

import java.lang.annotation.*;

/**
 * Marker annotation indicating the annotated class is a DDD Entity.
 * Entities are distinguished by their identity, not their attributes.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Entity {
}
