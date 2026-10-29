package cl.bcs.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.timgroup.statsd.Event;
import com.timgroup.statsd.Event.AlertType;
import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.NonBlockingStatsDClientBuilder;
import com.timgroup.statsd.StatsDClientErrorHandler;
import com.timgroup.statsd.StatsDClientException;


public class StatsD {
	
	public static final String DEFAULT_PROPERTIES_FILE = "statsd.properties";
	
	public static final Integer FLUSH_INTERVAL_MS = 5000;
	
	public static final String PROP_PREFIX = "STATSD_PREFIX";
	public static final String PROP_HOSTNAME = "STATSD_HOSTNAME";
	public static final String PROP_PORT = "STATSD_PORT";
	
	
	public static final Integer DEFAULT_PORT = 8125;
	public static final String DEFAULT_PREFIX = "cl.bcs.optimus";
	public static final String DEFAULT_HOSTNAME = "localhost";
	
	private static final Logger logger = LogManager.getLogger(StatsD.class);
	
	private static NonBlockingStatsDClient singleton;

	static {
		Properties prop = new Properties();
		InputStream input = null;
		try {

			String hostname = DEFAULT_HOSTNAME;
			String prefix 	= DEFAULT_PREFIX;
			Integer port 	= DEFAULT_PORT;
			
			File file = new File(DEFAULT_PROPERTIES_FILE); 
			
			if(file.exists()) {

				input = new FileInputStream(DEFAULT_PROPERTIES_FILE);
				prop.load(input);
				
				if(prop.getProperty(PROP_PREFIX) != null && prop.getProperty(PROP_PREFIX).length() > 0)
					prefix  	= prop.getProperty(PROP_PREFIX);
				if(prop.getProperty(PROP_HOSTNAME) != null && prop.getProperty(PROP_HOSTNAME).length() > 0)
					hostname   	= prop.getProperty(PROP_HOSTNAME);
				if(prop.getProperty(PROP_PORT) != null && prop.getProperty(PROP_PORT).length() > 0)
					port   		= Integer.parseInt(prop.getProperty(PROP_PORT));
				
		        input.close();
				
			}
			else {
				
				if(System.getProperty(PROP_HOSTNAME) != null)
					hostname = System.getProperty(PROP_HOSTNAME);

				if(System.getProperty(PROP_PREFIX) != null)
					prefix = System.getProperty(PROP_PREFIX);

				if(System.getProperty(PROP_PORT) != null)
					port = Integer.parseInt(System.getProperty(PROP_PORT));

				
			}
			
			singleton = new NonBlockingStatsDClientBuilder()
	                .prefix(prefix)
	                .hostname(hostname)
	                .port(port)
	                .aggregationFlushInterval(FLUSH_INTERVAL_MS)
	                .telemetryFlushInterval(FLUSH_INTERVAL_MS)
	                .build();
			
			logger.info("Init OK, prefix=" + prefix + ", hostname=" + hostname + ", port=" + port); 
			
		}catch(Exception e){
			
			logger.error("Error al inicializar statsD: " + e.getMessage(), e);
			
			logger.info("Inicializado con valores por defecto");
			
			singleton = new NonBlockingStatsDClientBuilder()
	                .prefix(DEFAULT_PREFIX)
	                .hostname(DEFAULT_HOSTNAME)
	                .port(DEFAULT_PORT)
	                .aggregationFlushInterval(FLUSH_INTERVAL_MS)
	                .telemetryFlushInterval(FLUSH_INTERVAL_MS)	                
	                .build();
		}
	};
	
	public static void reportCount(String name, long value, String... tags) throws StatsDClientException {
		
		if(logger.isDebugEnabled())
			logger.debug("reportCount " + name + "=" + value + " TAGS: " + Arrays.toString(tags));
		
		singleton.count(name, value, tags);
	};

	public static void reportCount(String name, double value, String... tags) throws StatsDClientException {
		
		if(logger.isDebugEnabled())
			logger.debug("reportCount " + name + "=" + value + " TAGS: " + Arrays.toString(tags));
		
		singleton.count(name, value, tags);
	};	
	
	public static void reportIncrement(String name, String... tags) throws StatsDClientException {
		
		if(logger.isDebugEnabled())
			logger.debug("reportIncrement " + name + "=1 TAGS: " + Arrays.toString(tags));
		
		singleton.incrementCounter(name, tags);
	};

	public static void reportGauge(String name, long value, String... tags) throws StatsDClientException {
		
		if(logger.isDebugEnabled())
			logger.debug("reportGauge " + name + "=" + value + " TAGS: " + Arrays.toString(tags));
		
		singleton.gauge(name, value, tags);
	};

	public static void reportGauge(String name, double value, String... tags) throws StatsDClientException {
		
		if(logger.isDebugEnabled())
			logger.debug("reportGauge " + name + "=" + value + " TAGS: " + Arrays.toString(tags));
		
		singleton.gauge(name, value, tags);
	};
				
	public static void reportExecutionTimeMilis(String name, long value, String... tags) throws StatsDClientException {
		
		if(logger.isDebugEnabled())
			logger.debug("reportExecutionTimeMilis " + name + "=" + value + " TAGS: " + Arrays.toString(tags));
		
		singleton.time(name, value, tags);
	};		
	

    public static void reportEvent(final Event event, final String... tags) {

        if(logger.isTraceEnabled())
            logger.trace("recordEvent: {} tags: {}", event, Arrays.toString(tags));

        singleton.recordEvent(event, tags);
    }

    public static void reportException(String message, Throwable exception, String... tags) {

        if(logger.isTraceEnabled())
            logger.trace("recordException: {} tags: {}", message, Arrays.toString(tags));

        // Get the exception stacktrace on a String using PrintWriter
        StringWriter sw = new StringWriter();        
        exception.printStackTrace(new PrintWriter(sw));        

        Event event = Event.builder()
            .withTitle(message)
            .withText(sw.toString())
            .withAlertType(AlertType.ERROR)            
            .build(); 
            
        singleton.recordEvent(event, tags);
		
	}
	

    public static void reportEventInfo(String message, String text, String... tags) {

        if(logger.isTraceEnabled())
            logger.trace("recordEventInfo: {} tags: {}", message, Arrays.toString(tags));

        Event event = Event.builder()
            .withTitle(message)
            .withText(text)
            .withAlertType(AlertType.INFO)            
            .build(); 
            
        singleton.recordEvent(event, tags);
    }

    public static void reportEventSuccess(String message, String text, String... tags) {

        if(logger.isTraceEnabled())
        	logger.trace("recordEventSuccess: {} tags: {}", message, Arrays.toString(tags));

        Event event = Event.builder()
            .withTitle(message)
            .withText(text)
            .withAlertType(AlertType.SUCCESS)            
            .build(); 
            
        singleton.recordEvent(event, tags);
    }

    public static void reportEventWarning(String message, String text, String... tags) {

        if(logger.isTraceEnabled())
        	logger.trace("recordEventWarning: {} tags: {}", message, Arrays.toString(tags));

        Event event = Event.builder()
            .withTitle(message)
            .withText(text)
            .withAlertType(AlertType.WARNING)            
            .build(); 
            
        singleton.recordEvent(event, tags);
    }	
    
    public static void close() {
    	singleton.close();
    }
    
    public static void reconfigure(String hostname, int port, String prefix, boolean blocking) {
    	
		logger.info("Reinicializando statsD: hostname=" + hostname + " port=" + port + " prefix=" + prefix + " blocking=" + (blocking?"TRUE":"FALSE"));
		
		singleton = new NonBlockingStatsDClientBuilder()
                .prefix(prefix)
                .hostname(hostname)
                .port(port)
                .blocking(blocking)
                .aggregationFlushInterval(FLUSH_INTERVAL_MS)
                .telemetryFlushInterval(FLUSH_INTERVAL_MS)
                .errorHandler(new StatsDClientErrorHandler() {
					
					@Override
					public void handle(Exception e) {
						logger.error("Error processing metric: " + e.getMessage(), e);
						
					}
				})
                .build();    
				
		
    }     	 
		
	public static void main(String[] args) throws Exception{
					
		// Los valores de configuración los toma del archivo stasd.properties que 
		// se encuentra en la raíz del workspace. En caso que no esté el archivo
		// se puede forzar la configuración manualmente con la siguiente línea. 
		// StatsD.reconfigure("161.238.15.26", DEFAULT_PORT, DEFAULT_PREFIX, false);
		
		for(int i = 0; i < 10; i++) {
			StatsD.reportIncrement("testMetric3", "tag1:valor1", "tag2:valor2");
			Thread.sleep(1000);
		}
		
		while(true) {
			
			double valor = Math.random() * 100 + 100;
			
			StatsD.reportGauge("testGauge", valor, "dev:jm");
			
			logger.info("Sent"); 
			
			Thread.sleep(1000);
		}


	};
}
