package net.minecraftforge.fml.common.eventhandler.legacy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Legacy Forge annotation kept as a compile-time compatibility shim.
 * The actual event wiring is handled by NEU's own event system on Fabric.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SubscribeEventJava {
	boolean receiveCanceled() default false;
}

