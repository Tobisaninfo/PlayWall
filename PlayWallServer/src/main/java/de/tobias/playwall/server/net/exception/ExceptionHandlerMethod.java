package de.tobias.playwall.server.net.exception;

import java.lang.reflect.Method;

record ExceptionHandlerMethod(Object bean, Method method)
{
}
