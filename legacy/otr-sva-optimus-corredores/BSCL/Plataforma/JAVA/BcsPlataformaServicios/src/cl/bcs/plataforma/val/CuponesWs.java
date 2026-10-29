package cl.bcs.plataforma.val;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.val.util.CuponesWsBCS;
import cl.bcs.plataforma.val.util.ValoUtils;
import cl.bcs.plataforma.val.util.ValoUtils.TareaValorizacion;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class CuponesWs implements JavaCall{	
	static final Logger logger = LogManager.getLogger(CuponesWs.class.getName());
	
	private static CuponesWsBCS cuponesbcs = null;
	//QUERIES

	private static String 				RutaArchivosConfiguracion;
	
	//CLASES
	public static class Cabecera{
		public String codigoEmisor;
		public String fechaEmision;
		public String FechaFin;
		public String FechaIni;
		public String fechaVencimiento;
		public String Nemo;
		public String Plazo;
		public String TipoMod;
		public String tipoUnidad;
		public String valorReferencial;
		public Cabecera(){}
		public Cabecera(String codigoEmisor, String fechaEmision, String FechaFin, String FechaIni, String fechaVencimiento, String Nemo, String Plazo, String TipoMod, String tipoUnidad, String valorReferencial) {
			this.codigoEmisor = codigoEmisor;
			this.fechaEmision = fechaEmision;
			this.FechaFin = FechaFin;
			this.FechaIni = FechaIni;
			this.fechaVencimiento = fechaVencimiento;
			this.Nemo = Nemo;
			this.Plazo = Plazo;
			this.TipoMod = TipoMod;
			this.tipoUnidad = tipoUnidad;
			this.valorReferencial = valorReferencial;
		}
	}
	
	public static class Detalle {
		public String cuponArrAmortizacion;
		public String cuponArrFactorAjuste;
		public String cuponArrFechaEfectiva;
		public String cuponArrFechaHabil;
		public String cuponArrFlujo;
		public String cuponArrIntereses;
		public String cuponArrSecuencia;
		public Detalle(){}
		public Detalle(String cuponArrAmortizacion, String cuponArrFactorAjuste, String cuponArrFechaEfectiva, String cuponArrFechaHabil, String cuponArrFlujo, String cuponArrIntereses, String cuponArrSecuencia) {
			this.cuponArrAmortizacion = cuponArrAmortizacion;
			this.cuponArrFactorAjuste = cuponArrFactorAjuste;
			this.cuponArrFechaEfectiva = cuponArrFechaEfectiva;
			this.cuponArrFechaHabil = cuponArrFechaHabil;
			this.cuponArrFlujo = cuponArrFlujo;
			this.cuponArrIntereses = cuponArrIntereses;
			this.cuponArrSecuencia = cuponArrSecuencia;
		}
	}
	
	public static class Contenedor {
		public Cabecera cabecera;
		public List<Detalle> detalle = new ArrayList<Detalle>();
	}
	
	public static class ResultCupon {
		public Contenedor contenedor;
		public String ID;
		public String SecuenciaMenor;
		public String Glosa;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	//ENTRADA/SALIDA SP
	public static class InCuponesWs{
		public Long idEmpresa;
		public Long idUsuario;
		public String idModulo;
		public Long idGrupo; // idGrupo>0: Es llamado desde el job debe realizar queries.
		public String rutinaValorizador;
		public List<ValoUtils.TareaValorizacion> cupones;
		public InCuponesWs(){
			cupones = new ArrayList<ValoUtils.TareaValorizacion>();
			idGrupo=new Long(0);
		};
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class OutCuponesWs {
		public Long erroresContador;
		public String descripcion;
		public List<ValoUtils.TareaValorizacion> cupones;
		public OutCuponesWs(){
			cupones = new ArrayList<ValoUtils.TareaValorizacion>();
		};
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}


	/**********************
	 * métodos básicos
	 **********************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = Utils.getGsonBuilder();
		InCuponesWs in=gson.fromJson(jin.toString() ,InCuponesWs.class);
		OutCuponesWs out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public JsonArray crearJsonCupon(ValoUtils.TareaValorizacion cupon) {
		JsonArray entradaCupon = new JsonArray();
		JsonObject detEntradaCupon = new JsonObject();
		detEntradaCupon.addProperty("ID", cupon.IdTarea.toString());
		detEntradaCupon.addProperty("Nemo", cupon.Nemo);
		detEntradaCupon.addProperty("Plazo", cupon.Plazo.toString());
		detEntradaCupon.addProperty("FechaIni", ValoUtils.dateToString(cupon.FechaIni));
		detEntradaCupon.addProperty("FechaFin", ValoUtils.dateToString(cupon.FechaFin));
		detEntradaCupon.addProperty("TipoMod", cupon.CodTipoMod);
		entradaCupon.add(detEntradaCupon);
		return entradaCupon;
	}
	
	@Trace
	public OutCuponesWs logica(Connection con, InCuponesWs entrada) throws Exception {
		boolean debug = true;
		OutCuponesWs salida = new OutCuponesWs();
		String msjaux = "";
		logger.info("Nueva Obtencion Cupones");
		List<ValoUtils.TareaValorizacion> cupones = null;
		salida.erroresContador = new Long(0);
		cupones = entrada.cupones;
		if(cupones.size()==0){
			salida.erroresContador++;
			salida.descripcion = "No se han enviado instrumentos";
			return salida;
		}
		if(entrada.rutinaValorizador.equals("BCS")){
			logger.debug("Antes de inicializar");
			inicializaCuponesWsBCS(con, entrada.idEmpresa);
			logger.debug("DESPUES de inicializar");
			cuponesbcs.depuracion = false;
			Gson gson = getGsonBuilder();
			int reintentos = 3;
			String reintenta= "N";
			List<ResultCupon[]> result = new ArrayList<CuponesWs.ResultCupon[]>();
			List<CompletableFuture<String>> futures = new ArrayList<CompletableFuture<String>>();
			for (int r = 0; r <= reintentos; r++) {	
				logger.info("CuponesWS - intento numero: "+r);
				logger.debug("CuponesWS - intento numero: "+r);
				ExecutorService executor = Executors.newFixedThreadPool(Math.min(entrada.cupones.size(), 5));
				futures = entrada.cupones.stream().parallel()
						.map(t -> crearJsonCupon(t))
						.map(t -> t.toString())
						.map(t -> CompletableFuture.supplyAsync(() -> cuponesbcs.obtenerCupon(t), executor))
						.collect(Collectors.toList());
				
				result = new ArrayList<CuponesWs.ResultCupon[]>();
				result = futures.stream().map(CompletableFuture::join)
						.map(t -> gson.fromJson(t, ResultCupon[].class))
						.collect(Collectors.toList());
				executor.shutdown();
				logger.debug("Despues de obtener info");
				logger.info("CuponesWS - fin intento numero: "+r);
				logger.debug("CuponesWS - fin intento numero: "+r);
				if(reintentos == r) {
					logger.info("CuponesWS -ERROR no hay mas intentos BOOOOOOOOOOOOOM: "+r);
					logger.debug("CuponesWS -ERROR no hay mas intentos BOOOOOOOOOOOOOM: "+r);
				}
				reintenta= "N";
				for(int e=0; e<result.size();e++){
					if(result.get(e) == null) {
						logger.info("CuponesWS - se requiere otro intento");
						logger.debug("CuponesWS - se requiere otro intento");
						reintenta = "S";
						break;
					}
				}
				if(reintenta == "N") {
					logger.info("CuponesWS - NO requiere otro intento");
					logger.debug("CuponesWS - NO requiere otro intento");
					break;
				}
			}
			
   			for(int i=0; i<cupones.size();i++){
   				for(int j=0; j<result.size();j++){
   					for(int k=0; k<result.get(j).length;k++){
   						
   						ResultCupon[] arr = result.get(j);
   						ResultCupon cupon = arr[k];
   						
   						if(cupones.get(i).IdTarea.toString().equals(cupon.ID)){
   	   						cupones.get(i).CodigoEmisor = cupon.contenedor.cabecera.codigoEmisor;
   	   						cupones.get(i).FechaEmision = ValoUtils.stringToSqlDate(cupon.contenedor.cabecera.fechaEmision);
   	   						cupones.get(i).FechaVencimiento = ValoUtils.stringToSqlDate(cupon.contenedor.cabecera.fechaVencimiento);
   	   						cupones.get(i).TipoMod = cupon.contenedor.cabecera.TipoMod;
   	   						cupones.get(i).TipoUnidad = cupon.contenedor.cabecera.tipoUnidad;
   	   						cupones.get(i).ValorReferencial = cupon.contenedor.cabecera.valorReferencial;
   	   						cupones.get(i).Glosa = cupon.Glosa;
   	   						if(cupones.get(i).Glosa == null || cupones.get(i).Glosa.length()==0) {
   	   							Integer secuenciaMenor = Integer.parseInt(cupon.SecuenciaMenor);
   	   							Integer orden = -1;
   		   						Integer cantCupones = cupon.contenedor.detalle.size();
   		   						for(int ll=0; ll<cantCupones;ll++){
   		   							Integer secuenciaNueva = Integer.parseInt(cupon.contenedor.detalle.get(ll).cuponArrSecuencia);
   		   							if(secuenciaMenor.compareTo(secuenciaNueva) == 0) {
   		   								orden = ll; break;
   		   							}
   		   						}
   		   						if(orden!=-1){
   		   							Detalle detalle = cupon.contenedor.detalle.get(orden);
   		   							cupones.get(i).CuponAmortizacion = new BigDecimal(detalle.cuponArrAmortizacion);
   		   							cupones.get(i).CuponFactorAjuste = new BigDecimal(detalle.cuponArrFactorAjuste);
   		   							cupones.get(i).CuponFechaEfectiva = ValoUtils.stringToSqlDate(detalle.cuponArrFechaEfectiva);
   		   							cupones.get(i).CuponFechaHabil = ValoUtils.stringToSqlDate(detalle.cuponArrFechaHabil);
   		   							cupones.get(i).CuponFlujo = new BigDecimal(detalle.cuponArrFlujo);
   		   							cupones.get(i).CuponIntereses = new BigDecimal(detalle.cuponArrIntereses);
   		   							cupones.get(i).CuponSecuencia = new Long(detalle.cuponArrSecuencia);
   		   							cupones.get(i).IdEstado ="TER";
   		   						}
   	   						}else {
   	   							cupones.get(i).IdEstado = "ERR";
   	   						}
   	   					}
   					}
   					
   			 	}
   			}
    		
			salida.cupones = cupones;
		}
		// else if(entrada.rutinaValorizador.equals("OTRA_RUTINA")){}
		else{
			msjaux = "Rutina "+entrada.rutinaValorizador+" no implementada";
			logger.debug(msjaux);
			salida.erroresContador++;
			salida.descripcion = msjaux;
			return salida;
		}
		if(debug){
			//log.writeAsJson("Entrada CuponWs",entrada);
			//log.writeAsJson("Salida CuponWs",salida);
		}
		return salida;
	}
	
	public static void main(String []args) throws Exception {
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			CuponesWs cupon = new CuponesWs();
			InCuponesWs entrada = new InCuponesWs();
			entrada.idEmpresa = 1L;
			entrada.idGrupo = 0L;
			entrada.idModulo = "PLA";
			entrada.idUsuario = 4L;
			entrada.rutinaValorizador = "BCS";
			List<TareaValorizacion> asdf2 = new ArrayList<>();
			TareaValorizacion arg0 = new TareaValorizacion();
			arg0.IdTarea = 0L;
			arg0.Nemo = "BCHIS-P12A";
			arg0.Plazo = 100L;
			arg0.FechaIni = Fecha.toDate("2018-07-07");
			arg0.FechaFin = Fecha.toDate("2019-07-07");
			arg0.CodTipoMod = "D";
			
			asdf2.add(arg0);
			entrada.cupones = asdf2 ;
			OutCuponesWs asdf = cupon.logica(con, entrada);
		}
	}
	
	/**********************
	 * métodos de la clase
	 **********************/	
	private static void inicializaCuponesWsBCS(Connection con, Long IdEmpresa) throws Exception{ 
		if(cuponesbcs == null){
			if(RutaArchivosConfiguracion==null)	
				RutaArchivosConfiguracion = Utils.getConfiguracion(con, "BSCL", IdEmpresa, "PLA.RutaArchivosConfiguracion");
			if(RutaArchivosConfiguracion==null)
				throw new Exception("iniValorizadorBCS: RutaArchivosConfiguracion no definida. Revise parametro PLA.RutaArchivosConfiguracion"); 
    		Properties prop = new Properties();
    		
    		InputStream input = null;
    		try {
    			input = new FileInputStream(RutaArchivosConfiguracion+"cupon.properties");
    		}catch(FileNotFoundException fnfe) {
    			input = new FileInputStream("Properties/cupon.properties");
    		}
    		logger.debug("configuracion cargada");
            prop.load(input);
            String Server=prop.getProperty("SERVER");
            String User=prop.getProperty("USER");
            String Pass=prop.getProperty("PASS");
            Boolean Depuracion=("Y".equalsIgnoreCase(prop.getProperty("DEPURATION"))?true:false); //Y|N
            int ConnTimeOut=Integer.valueOf(prop.getProperty("CONNTIMEOUT"));
            int ReadTimeOut=Integer.valueOf(prop.getProperty("READTIMEOUT"));
            input.close();
            
            cuponesbcs=new CuponesWsBCS(Server,User,Pass,Depuracion);
    	}
    }

}
