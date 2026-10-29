package cl.bcs.plataforma.services;

import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import cl.bcs.plataforma.services.config.pojo.RegistroConfiguracion;

public class RegistrosConfiguracionCache {

	private Cache<String, RegistroConfiguracion> cache;
	
	private RegistrosConfiguracionCache() {
		cache = CacheBuilder.newBuilder()
				.expireAfterWrite(1, TimeUnit.MINUTES)
				.concurrencyLevel(4)
				.build();
		
	}
	private static class LazyCache{
		static final RegistrosConfiguracionCache instance = new RegistrosConfiguracionCache();
	}
	
	public static RegistrosConfiguracionCache getInstance() {
		return LazyCache.instance;
	}
	
	public void put(String key, RegistroConfiguracion reg) {
		cache.put(key, reg);
	}
	
	public RegistroConfiguracion get(String key){
		return cache.getIfPresent(key);
	}
	
	public Long size() {
		return (long) cache.asMap().size();
	}
	

}
