package dev.kaooot.debugger.api.command.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.cloudburstmc.protocol.bedrock.data.command.CommandParamOption;
import org.cloudburstmc.protocol.bedrock.data.command.CommandParamType;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Parameter {

    String name();

    boolean optional() default false;

    CommandEnumData enumData() default @CommandEnumData(name = "");

    CommandParamType type();

    String postfix() default "";

    CommandParamOption[] options() default {};
}