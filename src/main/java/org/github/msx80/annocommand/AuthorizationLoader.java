package org.github.msx80.annocommand;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public interface AuthorizationLoader<C> {

	/**
	 * Loads authorizations associated to a context object.
	 * Authorization tokens take the form of Annotation classes.
	 * @param context
	 * @return
	 */
	Collection<Class<? extends Annotation>> getAuths(C context);
	
	/**
	 * Decorate an AuthorizationLoader adding a cache so the actual loading
	 * is done only once per context object.
	 * @param <T> the type of the context
	 * @param src a source loader to be decorated
	 * @return a loader with an internal cache
	 */
	public static <T> AuthorizationLoader<T> withCache(AuthorizationLoader<T> src)
	{
		Map<T, Collection<Class<? extends Annotation>>> cache = new HashMap<>();
		return u -> cache.computeIfAbsent(u, src::getAuths);
	}
}
