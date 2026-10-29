package cl.bcs.scheduler;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.Connection; 

import javax.sql.DataSource;

import cl.bcs.scheduler.pojo.Agenda;
import cl.bcs.scheduler.pojo.Programa;
import cl.bcs.scheduler.sql.TransactionManager;
import cl.bcs.utils.Utils;

public abstract class BcsJobManager implements Runnable {
    protected Date statsDate = new Date();
    private final ExecutorService executor;
    private final ExecutorService managerService = Executors.newSingleThreadExecutor();
    private final int pollIntervalMilliSeconds;
    public BcsJobListener jobListener = null;
    private DataSource datasourceJobs;
    private DataSource datasourceJobManager;
    private final TransactionManager transacction = new TransactionManager();
    private static boolean firstExecutionDiary = false;
    private static final long HEARTBIT_CONTROL = 10000L; //MSEG
    private static final long REBOOT_CONTROL   = 5000L; //MSEG
    private static final int  ZOMBIE_CONTROL   = 30; //SECS


    public BcsJobManager(int concurrentJobs, int pollIntervalMilliSeconds) {
        this.executor = Executors.newFixedThreadPool(concurrentJobs);
        this.pollIntervalMilliSeconds = pollIntervalMilliSeconds;
        this.managerService.submit(this);
    }

    public void setDataSource(DataSource datasourceJobs, DataSource datasourceJobManager) {
        this.datasourceJobs = datasourceJobs;
        this.datasourceJobManager = datasourceJobManager;
    }

    public void setBcsJobListener(BcsJobListener jobListener) {
        this.jobListener = jobListener;
    }

    public Date getStatsSinceDate() {
        return new Date(statsDate.getTime());
    }

    protected List<BcsJob> poolJobs(String node, String ptaskType) {
        List<BcsJob> listJobs = new ArrayList<BcsJob>();
       try(Connection connection = datasourceJobManager.getConnection();) {       	
           List<Programa> programmList = transacction.readProgramm(datasourceJobManager, node, ptaskType);
           for (Programa programm : programmList) {
                 long programId = -1;
               try {
                   programId = programm.getIdPrograma();
                   BcsJob job;
                   String programClassName = programm.getNombreClasePrograma();
                   int attempt = programm.getIntento()+1;
                   transacction.updateIntent(programId,attempt,connection); 
                   try {
                       Map<String, String> parameters = transacction.paramsProgram(programId,datasourceJobManager);                       
                       Class<?> obj = Class.forName(programClassName);
                       job = (BcsJob) obj.newInstance();
                       boolean isDaemon=transacction.isDaemaon(datasourceJobManager,programId);
                       job.setParameters(programId,parameters == null ? new HashMap<String, String>() : parameters,System.currentTimeMillis(), isDaemon, datasourceJobs,programm.getMinuto()); 
                       listJobs.add(job);
                       transacction.updateProgramm(programId, 0, "Programa Encolado", datasourceJobManager);
                   } catch (ClassNotFoundException e) {
                      transacction.updateProgramm(programId, -1,"No encuentro la clase : "+ e.getMessage(), datasourceJobManager);                                           
                      Logger.getLogger(BcsJobManager.class.getName()).log(Level.SEVERE,e.getMessage(), e.getCause());
                  }catch (ClassCastException e) {
                     transacction.updateProgramm(programId, -1, "No puedo castear job  "+e.getMessage(), datasourceJobManager);                                         
                     Logger.getLogger(BcsJobManager.class.getName()).log(Level.SEVERE,e.getMessage(), e.getCause());
                 }catch (Throwable e) {
                    transacction.updateProgramm(programId, -1, e.getMessage(), datasourceJobManager);                                         
                    Logger.getLogger(BcsJobManager.class.getName()).log(Level.SEVERE,e.getMessage(), e.getCause());
                }
               } catch (Exception e) {                	 
                   transacction.updateProgramm(programId, -1, e.getMessage(), datasourceJobManager);                   
                   Logger.getLogger(BcsJobManager.class.getName()).log(Level.SEVERE,e.getMessage(), e.getCause());
               }
           }
       } catch (Exception e) {
            Logger.getLogger(BcsJobManager.class.getName()).log(Level.SEVERE, e.getMessage(), e.getCause());
       }
        return listJobs;
   }

   protected List<BcsJob> poolDiary() {

       List<BcsJob> listdiary = new ArrayList<BcsJob>();
       if(!firstExecutionDiary){
    	   //Corrige rezagados y Demonios
    	   firstExecutionDiary = transacction.firstExecution(datasourceJobManager);        	 
       }	
       try  {
            //Reintentos
          // transacction.updateTryProgramm(connection);
           
           //Agenda
           List<Agenda> diaryList = transacction.readDiary(datasourceJobManager);
           for (Agenda diary : diaryList) {
               long diaryId = -1;
               try {
                   diaryId = diary.getIdAgenda();
                   if (diary.getHabilitado().equalsIgnoreCase("S")) {
                       BcsJob job = null;
                       String taskType = diary.getIdTipoPrograma();
                       String programmClassName = diary.getNombreClasePrograma();
                       String nextExecution = diary.getSiguienteFechaEjecucion();
                       String lastExecution = diary.getUltimaFechaEjecucion();
                       int fecha = -1;
                       int idProgramm = -1;
                       Map<String, String> parameters = transacction.paramsDiary(diaryId, datasourceJobManager);
                       try {                       	                          	    
	                       	if( lastExecution!=null && nextExecution!=null) {
	                       		if (diary.getEsDemonio().equalsIgnoreCase("S")) {
	                       			//Demonio - Solo la primera vez se cumple ( firstExecution )
	                       			if(transacction.compararFechasConDate(nextExecution, lastExecution)) {
	                       				transacction.updateStartDaemon(datasourceJobManager,diaryId);
	                       				idProgramm = transacction.addProgramm((int) diaryId, taskType, 0 , parameters,datasourceJobManager);
	                       			}	                       			
	                       		}else {
	                       			//Programa - Si la Siguiente ejecucion es menor que la Ultima ejecucion se agrega un nuevo programa
		                       		if(transacction.compararFechasConDate(nextExecution, lastExecution)) { 
		                        	    fecha = transacction.getDateToProgramm(diaryId, datasourceJobManager);
		                        	    if(fecha>0){
		                        	    	transacction.updateNextExecutionDateDiary(datasourceJobManager,diaryId, fecha);
		                                    idProgramm = transacction.addProgramm((int) diaryId, taskType, fecha, parameters,datasourceJobManager);
		                        	    }		                                    
			                        }
	                       		}
	                       	}
                       } catch (Throwable e) {
                       		e.printStackTrace();
                           Logger.getLogger(BcsJobManager.class.getName()).log(Level.SEVERE, e.getMessage(),e.getCause());
                       }
                   }

               } catch (Exception e) {
            	   e.printStackTrace();
                   Logger.getLogger(BcsJobManager.class.getName()).log(Level.SEVERE,"Error en bd datos  " + e.getMessage(), e.getCause());
               }
           }

       } catch (Exception e) {
    	   e.printStackTrace();
           Logger.getLogger(BcsJobManager.class.getName()).log(Level.SEVERE, e.getMessage(), e.getCause());
       }
       return listdiary;
   }
    public final void runJob(String node, String taskType, String processId) { 	
    	boolean firsttime=true;
    	long heartbit=System.currentTimeMillis();
    	long reboot=System.currentTimeMillis();
        while (true) {
            try {
                Thread.sleep(pollIntervalMilliSeconds);
            } catch (InterruptedException e) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Job Manager interrumpido", e);
                throw new RuntimeException(e);
            }            
            
            try {
            	//Primea ejecucion
            	if (firsttime) {
            		transacction.addNodo(datasourceJobManager, node, taskType, Utils.getPidSystem(), processId);
            		firsttime=false;
            	}
            	//Heartbit- Actualiza Cycle
            	if ((System.currentTimeMillis() - heartbit)> HEARTBIT_CONTROL) {
            		heartbit=System.currentTimeMillis();
            		transacction.updateCycleNodo(datasourceJobManager,Utils.getPidSystem(), taskType, processId);
            	}            	
            	//Reboot Node
            	if ((System.currentTimeMillis() - reboot)> REBOOT_CONTROL) {
            		reboot=System.currentTimeMillis();
	            	if (transacction.isRebootNodo(datasourceJobManager,Utils.getPidSystem())) {
	            		Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Reboot solicitado Node:"+node+" Job:"+taskType+" Pid:"+Utils.getPidSystem());
	            		System.exit(0);
	            		break;
	            	}
            	}
            	//Programas nuevos
            	List<BcsJob> jobs = poolJobs(node, taskType);
                for (BcsJob j : jobs) {
                    j.setJobListener(new BcsJobListener() {
                        @Override
                        public void status(long jobId, boolean isError, String msg) {
                            if (jobListener != null) {
                                jobListener.status(jobId, isError, msg);
                            }
                        }

						@Override
						public void updateDaemon(long jobId) {
							if (jobListener != null) {
								jobListener.updateDaemon(jobId);
							}							
						}
                    });
                    executor.submit(j);
                    j.pause();
                }
            } catch (Throwable e) {
            	e.printStackTrace();
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Exception in : " + e.getCause(),e);
            }
        }
    }

    public final void run() {

    }

    public final void runDiary() {
    	long heartbit=System.currentTimeMillis();
        while (true) {
            try {
                Thread.sleep(pollIntervalMilliSeconds);
            } catch (InterruptedException e) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Job Manager interrumpido", e);
                throw new RuntimeException(e);
            }
            try {
            	//Heartbit- Reinicia nodos pegados
            	if ((System.currentTimeMillis() - heartbit)> HEARTBIT_CONTROL) {
            		heartbit=System.currentTimeMillis();
                	List<Integer> listzombie=transacction.getZombieNodo(datasourceJobManager,ZOMBIE_CONTROL,true);
                	for (int pid:listzombie) {
                		Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Job Manager zombie pid:"+pid+" killing...");
                		try {
                			Runtime rt = Runtime.getRuntime();
                			if (Utils.isWindowsOS()) {
                				 rt.exec("taskkill /pid "+pid);
                			} else {
                				 rt.exec("kill -9 " +pid);
                			}
                		}catch(Exception er) { }
                	}             		
            	}            	
            	//Agendas nuevas que deben agregar Programas
            	List<BcsJob> diary = poolDiary();
                for (BcsJob j : diary) {
                    executor.submit(j);
                }                
            } catch (Throwable e) {
            	e.printStackTrace();
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Exception in : " + e.getCause(),e);
            }
        }
    }
}
