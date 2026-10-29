package cl.bcs.scheduler.tools;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.scheduler.BcsJobListener;
import cl.bcs.scheduler.BcsJobManager;
import cl.bcs.scheduler.sql.TransactionManager;

public class BcsJobManagerSingleBean extends BcsJobManager {

    private DataSource datasourceJobManager;
    TransactionManager transacction = new TransactionManager();
    private static final Logger logger = LogManager.getLogger(BcsJobManagerSingleBean.class);

    public static void main(String[] args) throws IOException  {
    	logger.info("<File Config> <AGENDA|PROGRAMA> <NODO|-> <JOB|-]> <mseg #> <paralelismo #> <LOG TRUE|FALSE> <identificadorproceso #> ");
    	logger.info("New Logger enabled");
        for (int i=0;i<args.length;i++) {
        	logger.debug(i+"="+args[i]);
        }
        //Settings by cmdline
        Locale.setDefault(new Locale("en", "EN"));
        String file = args[0];
        String modo = args[1];
        String node = args[2];
        String jobname = args[3];
        int mseg = Integer.parseInt(args[4]);
        int concurrent = Integer.parseInt(args[5]);
        String log = args[6];
        String idproceso = ""; 

        if (args.length > 7) {
            idproceso = args[7];
        }
        
        if (node.equalsIgnoreCase("*")||node.equalsIgnoreCase("-")) {
            node = "";
        }
        if (jobname.equalsIgnoreCase("*")||jobname.equalsIgnoreCase("-")) {
            jobname = "";
        }
        
        //Settings by file
        Properties prop = new Properties();
        InputStream input = new FileInputStream(file);
        prop.load(input);
        String dbURL  = prop.getProperty("URL");
        String dbprop = prop.getProperty("PROP");
        String dbdrv  = prop.getProperty("DRIVER");
        String dbqry  = prop.getProperty("QVAL");
        String userjm = prop.getProperty("UserJobman");
        String passjm = prop.getProperty("PassJobman");
        String passjmcipher = prop.getProperty("PassJobmanCipher", "");
        String userjb = prop.getProperty("UserJob");
        String passjb = prop.getProperty("PassJob");
        String passjbcipher = prop.getProperty("PassJobCipher", "");
        
        if(passjmcipher.equalsIgnoreCase("Static3DES")) {
        	logger.info("Desencriptando password jobman..");
        	passjm = CypherEncryption.decrypt(passjm);
        }        

        if(passjbcipher.equalsIgnoreCase("Static3DES")) {
        	logger.info("Desencriptando password job..");
        	passjb = CypherEncryption.decrypt(passjb);
        }        
        
        //Job Manager       
        BasicDataSource dsJM = new BasicDataSource();
        dsJM.setDriverClassName(dbdrv);
        dsJM.setUsername(userjm);
        dsJM.setPassword(passjm);
        dsJM.setUrl(dbURL);
        dsJM.setValidationQuery(dbqry);
        dsJM.setConnectionProperties(dbprop);
        dsJM.setDefaultAutoCommit(false);

        //Job
        BasicDataSource jsJB = new BasicDataSource();
        jsJB.setDriverClassName(dbdrv);
        jsJB.setUsername(userjb);
        jsJB.setPassword(passjb);
        jsJB.setUrl(dbURL);
        jsJB.setValidationQuery(dbqry);
        jsJB.setConnectionProperties(dbprop);
        jsJB.setDefaultAutoCommit(false);
        
        //Wait valid connections to start
        while (true) {
        	TransactionManager tmg=new TransactionManager();
        	try {            	
        		logger.debug("PING DATABASE");  
            	if (tmg.pingDB(dsJM) && tmg.pingDB(jsJB)) {
            		logger.debug("CONEXION OK");        		
        			break;
            	}else {
            		logger.error("CONEXION ERROR - TRY NEXT 5 MINS.");        		
            		Thread.currentThread();
					Thread.sleep(5*60*1000);
            		continue;
            	}
        	}catch(Exception er) {
        		er.printStackTrace();
        	}finally {
        		tmg=null;
        	}
        }
        
        //Run Process
        BcsJobManagerSingleBean ejecutor = new BcsJobManagerSingleBean(concurrent,mseg);
        ejecutor.setDataSource(jsJB, dsJM);
        if (modo.equalsIgnoreCase("PROGRAMA")) {
            ejecutor.runJob(node, jobname, idproceso);
        } else if (modo.equalsIgnoreCase("AGENDA")) {
            ejecutor.runDiary();
        } else {
            System.out.println("ERROR PARAMETROS");
        }
        
    }

    public BcsJobManagerSingleBean() {
        this(250, 1);
        startUp();

    }

    public BcsJobManagerSingleBean(int concurrentJobs, int pollIntervalSeconds) {
        super(concurrentJobs, pollIntervalSeconds);                
        startUp();
    }

    @Override
    public void setDataSource(DataSource datasourceJobs, DataSource datasourceJobManager) {
        this.datasourceJobManager = datasourceJobManager;
        super.setDataSource(datasourceJobs, datasourceJobManager);
    }

    private void startUp() {
        super.setBcsJobListener(new BcsJobListener() {
            @Override
            public void status(long jobId, boolean isError, String msg) {
                try {
                     if (!isError) {
                        transacction.updateProgramm(jobId, 1, "Finalizado correctamente.", datasourceJobManager);
                    } else {
                        transacction.updateProgramm(jobId, -1, "Terminado con errores:" + msg, datasourceJobManager);
                    }
                 } catch (Exception e) {
                    logger.error(e);
                }
            }

			@Override
			public void updateDaemon(long jobId) {
                try {
                	transacction.updateCycleDaemon(datasourceJobManager,jobId);                	
                } catch (Exception e) {
                	logger.error(e);
               }
				
			}
        });
    }

}
