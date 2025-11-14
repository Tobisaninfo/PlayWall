package de.tobias.playwall.client.appcontext;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Service
{
	Class<?> superclass() default Object.class;

	boolean singleton() default true;
}
