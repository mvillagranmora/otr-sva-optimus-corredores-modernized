package cl.bcs.utils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List; 
/**
 * 
 * Small plugin manager to load JAR  
 * 
 * @author kaiser  
 */
public class PluginLoader {
	private static URLClassLoader context=null;
	private final static List<URL> hUrls=new ArrayList<>();
	private final static HashMap<String,Long> hLmod=new HashMap<>();
	
	/**
	 * Unload all classes
	 * @throws IOException
	 */
	public synchronized static void UnloadPlugins() throws IOException {
		if (context==null){
			System.out.println("Kaiser Plugin is empty");
		}else {
			System.out.println("Kaiser Plugin unloading");
			context.close();
			context=null;
		}			
	}
	
	/**
	 * 
	 * @param fjar
	 * @param className
	 * @return
	 * @throws MalformedURLException
	 * @throws ClassNotFoundException
	 * @throws IOException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public synchronized static Object getObject(String pathBase,String fjar,String className) throws MalformedURLException, ClassNotFoundException, IOException, InstantiationException, IllegalAccessException {
		File fJar=new File(pathBase+"/"+fjar);	
		File rJar=new File(pathBase+"/_"+fjar);
		//first time
		if (context==null) {
			hUrls.clear();			
			System.out.println("Kaiser Plugin loading jar from pathBase");
			File pbase = new File(pathBase);
			for (File f:pbase.listFiles()) {
				if (f.isFile() && f.exists() && !f.getName().startsWith("_") && f.getName().endsWith(".jar")) {
					System.out.println("  "+f.getCanonicalPath());
					File xJar=new File(pathBase+"/_"+f.getName());					
					Files.copy(f.toPath(), xJar.toPath(), StandardCopyOption.REPLACE_EXISTING);
					hUrls.add(new URL("file:"+xJar.getCanonicalPath()));
					hLmod.put(f.getName(),f.lastModified());
				}
			}		
		}
		//new or updated
		 if (context==null || !hLmod.containsKey(fjar)  ||  fJar.lastModified()>hLmod.get(fjar)) {
			System.out.println("Kaiser Plugin New "+fjar+"  "+fJar.lastModified()+" Running:"+rJar.lastModified());
			if (context!=null) context.close();
			Files.copy(fJar.toPath(), rJar.toPath(), StandardCopyOption.REPLACE_EXISTING);	
			if (!hLmod.containsKey(fjar)) hUrls.add(new URL("file:"+rJar.getCanonicalPath()));
			context = new URLClassLoader (hUrls.toArray(new URL[hUrls.size()]),PluginLoader.class.getClassLoader());
			Class<?> tclass=Class.forName (className, true, context);
			hLmod.put(fjar,fJar.lastModified());
			return tclass.newInstance();					
		}
		//new instance		
		Class<?> tclass=Class.forName (className, true, context);
		return tclass.newInstance();		
	}
}
