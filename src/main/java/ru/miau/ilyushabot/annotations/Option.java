package ru.miau.ilyushabot.annotations;

import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(HasOptions.class)
public @interface Option {
    OptionType optionType() default OptionType.INTEGER;
    String name();
    String description();
    boolean isRequired() default true;

}