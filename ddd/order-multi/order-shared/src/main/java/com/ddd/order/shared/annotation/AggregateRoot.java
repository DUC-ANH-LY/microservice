package com.ddd.order.shared.annotation;

import java.lang.annotation.*;

/**
 * Marker annotation indicating the annotated class is a DDD Aggregate Root.
 * An Aggregate Root is the only member of its aggregate that outside objects
 * are allowed to hold references to. It enforces all invariants of the aggregate.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AggregateRoot {
}
