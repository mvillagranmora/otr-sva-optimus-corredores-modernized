package cl.bcs.normativa.jobs;
  

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.text.SimpleDateFormat;
import java.util.Date;

import java.util.HashMap;

import cl.bcs.fondomutuo.ActualizarCostoUF_MLT;

import cl.bcs.fondomutuo.ActualizarCostoUF_MLT.EntradaActualizarCostoUF;
import cl.bcs.fondomutuo.ActualizarCostoUF_MLT.SalidaActualizarCostoUF;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;

import javax.sql.DataSource;

import cl.bcs.scheduler.BcsJob;

import cl.bcs.utils.JavaCall;

import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BcsJobProcesarCostos extends BcsJob implements JavaCall{
	
	public static class RegistroValorCuota{
		public String 	FechaValorCuota;
		
		public RegistroValorCuota() {
			this.FechaValorCuota = "";
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class RegistroPortafolioFecha{
		public Long IdPortafolio;
		public String FechaValorCuota;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class UpdMLT {
		
		public Long IdEmpresa;
	    
		
	}
	AtomicBoolean running=new AtomicBoolean(false); 
	
	private final Logger log = LogManager.getLogger(BcsJobProcesarCostos.class);
	
	SQL sql=new SQL();
	private final String SQL_SELECT_ULT_MLT = loadResourceToString("cl/bcs/normativa/jobs/sql/ProcesarCostos_SelectUltMLT.sql");
	private final String SQL_UPDATE_ULT_MLT = loadResourceToString("cl/bcs/normativa/jobs/sql/ProcesarCostos_UpdateUltMLT.sql");
	private final String SQL_SELECT_PORTAFOLIOS_ULT_MLT = loadResourceToString("cl/bcs/normativa/jobs/sql/ProcesarCostos_SelecPortafoliostUltMLT.sql");
	
	private List<String> ArrFechaIni  = new ArrayList<String>();	
	private List<String> ArrFechaFin  = new ArrayList<String>();
	private List<Long> ArrPortafolio  = new ArrayList<Long>();
	
	public String 	FechaMinima;
	
	
	public boolean transaction(Connection dbcon) throws InterruptedException, Throwable {
		log.info("INICIO ProcesarCostosUF ");
		
		running.set(true);
        DataSource datasource = super.getDataSource();
    	
		
        Connection dbConnection = null;
	
		try {
			dbConnection = datasource.getConnection();
			final long idEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
			final long IdUsuario=Long.parseLong(this.getParam("IdUsuario"));
			
			//---- para debug ------
			//dbConnection=dbcon;
			//final long idEmpresa =1L;	
			//final long IdUsuario =1L;
			//---- para debug ------
			
			

        	HashMap<String,Object> params = new HashMap<String,Object>();
    		params.put("IdEmpresa", idEmpresa);
    		
    		//List<RegistroValorCuota> fechaValor = sql.query(dbcon, SQL_SELECT_ULT_MLT, params, RegistroValorCuota.class);
    		
    		List<RegistroPortafolioFecha> Portafolio = sql.query(dbcon, SQL_SELECT_PORTAFOLIOS_ULT_MLT, params, RegistroPortafolioFecha.class);
    		
    		//if(fechaValor.size() != 0){
    			
  	         
    			//for(RegistroValorCuota in : fechaValor){
    				//FechaMinima =  in.FechaValorCuota;
    				
    			//}
    			
    		//}
			String confFechaSistema = ConfiguracionSistema.getParametroConfiguracionString(dbConnection,idEmpresa,"PLATAFORMA","PLA.FechaSistema");
	
			
			for(int i= 0; i< Portafolio.size(); i++){
				getFechas(Portafolio.get(i).FechaValorCuota,confFechaSistema,Portafolio.get(i).IdPortafolio);
			}
			
        	try{
    			
        		ActualizarCostoUF_MLT actualiza = new ActualizarCostoUF_MLT();
				EntradaActualizarCostoUF entradaActualizarCostoUF = new EntradaActualizarCostoUF();
				entradaActualizarCostoUF.p_IdEmpresa = idEmpresa;
				entradaActualizarCostoUF.p_IdUsuario = IdUsuario;
				entradaActualizarCostoUF.p_IdModulo = "NOR";
				

				log.info("====== INICIO DE PROCESO ===========");
				log.debug("NUMERO DE REGISTROS A PROCESAR:" + Portafolio.size());
				
				for(int i= 0; i<ArrFechaIni.size(); i++){
					//entradaActualizarCostoUF.p_FechaDesde = "2020-01-01";
					
					entradaActualizarCostoUF.p_FechaDesde =  ArrFechaIni.get(i);
					entradaActualizarCostoUF.p_FechaHasta = ArrFechaFin.get(i);
					entradaActualizarCostoUF.p_Portafolio = ArrPortafolio.get(i);
					
					SalidaActualizarCostoUF salida = actualiza.logica(dbConnection, entradaActualizarCostoUF);
					log.debug(salida);
				}
				 List<UpdMLT> upd = new ArrayList<UpdMLT>();
				 UpdMLT updn = new UpdMLT();
		         updn.IdEmpresa=idEmpresa;upd.add(updn);
			      
				
				sql.batch(dbcon, SQL_UPDATE_ULT_MLT, upd, UpdMLT.class);
				
				log.info("====== TERMINO DE PROCESO ===========");
				dbConnection.commit();
        	
        		
        	}catch(Exception ex){
        		dbConnection.rollback();
        		ex.printStackTrace(); 
        	}
        	
        	
		} catch (Exception e) {
			dbConnection.rollback();
    		e.printStackTrace(); 
		}
	
		return true;
	}

	public static int getDistanceMonth(String dateStr1,String dateStr2) throws Exception {
        // Analiza el objeto de fecha
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date1 = sdf.parse(dateStr1);
        Date date2 = sdf.parse(dateStr2);
        // Usa el objeto Calendario para manipular el objeto de fecha
        Calendar c1 = Calendar.getInstance();
        Calendar c2= Calendar.getInstance();
        c1.setTime(date1);
        c2.setTime(date2);

        long time1 = date1.getTime();
        long time2 = date2.getTime();
        // Restar anios
        int yearSubtract = c2.get(Calendar.YEAR) - c1.get(Calendar.YEAR);
        // Restar meses
        int monthSubtract = 0;
        // Aplica diferentes formulas segun la secuencia de fechas
        if (time1 < time2) {
            monthSubtract =c2.get(Calendar.MONTH) - c1.get(Calendar.MONTH) + 1;
        } else {
            monthSubtract =c2.get(Calendar.MONTH) - c1.get(Calendar.MONTH) - 1;
        }

        return (int)Math.abs(yearSubtract * 12 + monthSubtract);


    }
	
	public  void getFechas(String FechaINI,String FechaFIN, Long Portafolio) throws Exception {
	//	 FechaINI = "2009-01-01";
	//     FechaFIN = "2022-01-10";
	     String FechaFIN_NEW = "";
	     Calendar c = Calendar.getInstance();
	     
	     int Intervalo=6; //meses a considerar
	     int dif =getDistanceMonth(FechaINI,FechaFIN);
	     
	     log.debug("Meses encontrados entre rango de fechas: " + dif + " meses");
	     log.debug("fecha minima: " + FechaINI);
	     log.debug("fecha maxima: " + FechaFIN);
	     
	     /*
		 int cliclo =dif /6;
		 int remainder = dif % 6;
		 System.out.println(cliclo);
		 System.out.println(remainder);
		 */
		 SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd"); 
		
		 
		 if (dif <=Intervalo)
		 {
			 //inscribir fecha
			 ArrFechaIni.add(FechaINI); ArrFechaFin.add(FechaFIN);
			 ArrPortafolio.add(Portafolio);
		 };
		 while (dif >Intervalo)
		 {
			  
			 c.setTime(formato.parse(FechaINI));
			 c.add(Calendar.MONTH, + (Intervalo + 1)); //agrega meses 
			 c.add(Calendar.DATE, -1);
			 c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
			 Date endDate = (Date)formato.parse(c.get(Calendar.YEAR) + "-" + String.format("%02d", c.get(Calendar.MONTH)) + "-" + String.format("%02d", c.get(Calendar.DAY_OF_MONTH)));
			 Date ayer = new Date(endDate.getTime() - (1000 * 60 * 60 * 24));
			 FechaFIN_NEW=formato.format(ayer);
			 //System.out.println("fecha :"+ FechaFIN_NEW);

			 //inscribir fecha
			 ArrFechaIni.add(FechaINI); ArrFechaFin.add(FechaFIN_NEW);
			 ArrPortafolio.add(Portafolio);
			 
			 //nuevo periodo
			 Date tomorrow = new Date(ayer.getTime() + (1000 * 60 * 60 * 24));
			 FechaINI=formato.format(tomorrow);
			 //System.out.println("fecha ini new:"+ FechaINI);

			 
			 //comparar nuevas fechas
			 dif =getDistanceMonth(FechaFIN_NEW,FechaFIN);//Compara ultima fecha versus fecha final
			 if (dif <=Intervalo && ((Date)formato.parse(FechaFIN)).after(tomorrow))
			 {
				 //inscribir fecha
				 ArrFechaIni.add(FechaINI); ArrFechaFin.add(FechaFIN);
				 ArrPortafolio.add(Portafolio);
			 };

			 
			 
		 };
		 log.debug("fecha ini :"+ ArrFechaIni.toString());
		 log.debug("fecha fin :"+ ArrFechaFin.toString());
		 


    }
	
	@Override
    public void onError(Throwable t) {
        System.out.println(":BcsJobProcesarServ:  ****** ERROR  *******");  
        t.printStackTrace();
    }

	@Override
	public void pause() {

	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		Connection con = PlataformaUtils.getDESAConnection();
		BcsJobProcesarCostos a = new BcsJobProcesarCostos();
		try {
			boolean  b= a.transaction(con);
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
}
