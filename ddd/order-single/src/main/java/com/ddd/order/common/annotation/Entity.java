package com.ddd.order.common.annotation;

import java.lang.annotation.*;

/**
 * Marker annotation indicating the annotated class is a DDD Entity.
 * An Entity is an object with a distinct identity that runs through time
 * and different representations. Entities are distinguished by their identity,
 * not their attributes.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Entity {
}
