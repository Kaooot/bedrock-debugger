package dev.kaooot.debugger.api.command.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.cloudburstmc.protocol.bedrock.data.command.CommandEnumConstraint;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandEnumValue {

    String name();

    CommandEnumConstraint[] constraints() default {};
}