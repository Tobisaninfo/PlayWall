package de.tobias.playwall.server.net;

import de.tobias.playwall.common.net.RequestMessage;
import org.springframework.stereotype.Service;

import java.lang.annotation.*;

@Inherited
@Service
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RequestHandlerTyped
{
	Class<? extends RequestMessage> value();
}
