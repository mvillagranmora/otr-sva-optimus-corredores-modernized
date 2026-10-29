package cl.bcs.utils;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.ObjectUtils;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class JavaViewCache {
	
	protected static LoadingCache<String, Object> cache = CacheBuilder.newBuilder()
			.maximumSize(100)
			.expireAfterAccess(1, TimeUnit.MINUTES)
			.build(new CacheLoader<String, Object>(){
				@Override
				public Object load(String arg0) throws Exception {
					//no cargar nada al inicio
					return null;
				}
				
			});

	public static <T> T getCache(String key, Class<T> type) throws ExecutionException {
		Object obj = cache.getIfPresent(key);
		return (obj!=null) ? ObjectUtils.cloneIfPossible(type.cast(obj)) : null;
	}
	
	public static void setCache(String key, Object json) {
		cache.put(key, ObjectUtils.cloneIfPossible(json));
	}
	

}
