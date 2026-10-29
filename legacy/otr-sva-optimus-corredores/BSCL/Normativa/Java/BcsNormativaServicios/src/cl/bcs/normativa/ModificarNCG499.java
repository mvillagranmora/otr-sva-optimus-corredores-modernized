package cl.bcs.normativa;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.FileLoader;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarNCG499 implements JavaCall {

	private static final Logger logger = LogManager.getLogger(ModificarNCG499.class);

    public static class ObjDetalleParidad {

        @Override
		public String toString() {
            return Utils.printJson(this, true);
        }
    }

    public static class ModificarNCG499_Entrada {
        public List<NCG499> NCG499_in;
        public String Accion;
        public Long	p_IdEmpresa;
        public Long p_IdUsuario;
    }

    public static class NCG499 extends BcsPojo{
    	public Date Fecha;
    	public String IdClasificacionTipoInstrumento;
    	public String Nemotecnico;
    	public String IdEmisor;
        public Long IdPortafolio;
        public String ISIN;
        public Long IdCliente;
        public String IdTipoInstrumento = "";
        public String IdTipoInstrumentoCMF;//desde el mantenedor siempre se enviara el codigo de la cmf (creado para cuando no existe el tipo instrumento en bolsa)
        public Date FechaVencimiento;
        public String ClasificacionRiesgoInstrumento;
        public String IdMonedaPrincipal;
        public String IdMonedaInstrReajuste;
        public BigDecimal ValorMercadoCustodia;
        public BigDecimal MontoMercadoCustodia;
        public BigDecimal PrecioMercadoCustodia;
        public BigDecimal PrecioCompraCustodia;
        public BigDecimal TasaMercadoCustodia;
        public BigDecimal CantidadTotal;
        public BigDecimal ValorCompra;
        public BigDecimal MontoCompra;
        public BigDecimal CantidadGarantia;
        public String IdBoveda;
        public BigDecimal CantidadCustodia;
        public String TendenciaDeRiesgo;
        public BigDecimal BaseTasa;
        public BigDecimal Duration;
        public String CodigoPais;
        public String CodigoPaisEmisionInstrumento;
        public String FuentePrecios;
        public BigDecimal CantidadPrestamo;
        public String FuentePreciosGarantia;
        public String FuentePreciosPrestamo;
        public int IdCircularCartera;
        public String IdEstado;
        public String IdOrigen;
        public String SituacionInstrumento;//clasifica la suma de cantidad dependiendo de si se considera custodia(NR), garantia(GT) o prestamo(PR)
        public BigDecimal CantidadUnidades;//suma de cantidades (custodia, garantia o prestamos dependiendo de fecha-nemo-IdEmisor)
        public BigDecimal Tir;
        public String CodigoValorizacion;
        
        public String SituacionInstrumentoAux;
        
        public String FuentePreciosPlazo;
        public BigDecimal CantidadPlazoCompra;
        public BigDecimal CantidadPlazoVenta;
        public BigDecimal ValorizacionGarantia;
        public BigDecimal ValorizacionPrestamo;
        public BigDecimal ValorizacionPlazoCompra;
        public BigDecimal ValorizacionPlazoVenta;
        
        public String ClasificacionInstrumentoCustodia;
        public String ClasificacionInstrumentoGarantia;
        public String ClasificacionInstrumentoPrestamo;
        public String ClasificacionInstrumentoPlazo;
        
        public String IdTipoInstrumentoPlazoCompra;
        public String IdTipoInstrumentoPlazoVenta;
        
        public Long IdInstrumento;
        
    }

    public static class Log {
        public String IdTipoLog;
        public String IdTipoRegistro;
        public Long IdUsuario;
        public Long IdEmpresa;
    }

    public static class Homologacion{
    	public String Tipo;
    	public String Descripcion;
    	public String ValorOrigen;
    	public String ValorDestino;
    }
    
    //SALIDAS
  	public static class ResultadoProceso_Salida {
  		public String Campo;
  		public Long Id;
  		public String Servicio;
  		public String Resultado;
  		
  		public String toString() {
  			return Utils.printJson(this, false);
  		}
  	}

    public static class ModificarNCG499_Salida extends BcsPojo{
    	public List<MensajeSalida>					P_OUT_MENSAJES;
		public Long									P_OUT_ERRORESCONTADOR;
		public List<ResultadoProceso_Salida>		P_OUT_RESULTADOPROCESO;		
        
        public ModificarNCG499_Salida (){
			P_OUT_MENSAJES	= new ArrayList<>();
			P_OUT_RESULTADOPROCESO = new ArrayList<>();
			P_OUT_ERRORESCONTADOR = 0L;
		}
    }

    private static final FileLoader LOADER = new FileLoader();
    private static final String SQL_UPDATE_NCG499 = LOADER.loadResourceToString("cl/bcs/normativa/sql/ModificarNCG499_UpdateNCG499.sql");
    private static final String SQL_UPDATE_ESTADO = LOADER.loadResourceToString("cl/bcs/normativa/sql/ModificarNCG499_UpdateEstado.sql");
    private static final String SQL_INSERT_NCG499 = LOADER.loadResourceToString("cl/bcs/normativa/sql/ModificarNCG499_InsertNCG499.sql");
    private static final String SQL_SELECT_AGRUPADO = LOADER.loadResourceToString("cl/bcs/normativa/sql/ModificarNCG499_ObtenerDatosAgrupados.sql");
    private static final String SQL_SELECT_HOMOLOGACION = LOADER.loadResourceToString("cl/bcs/normativa/sql/ModificarNCG499_ObtenerHomologacionCMF.sql");

    @Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
        // Configura Gson con el formato de fecha específico "yyyy-MM-dd"
        Gson gson = new GsonBuilder()
                    .setDateFormat("yyyy-MM-dd")
                    .serializeNulls()
                    .create();

        ModificarNCG499_Entrada in = gson.fromJson(jin.toString(), ModificarNCG499_Entrada.class);
        ModificarNCG499_Salida out = logica(con, in);

        // Convierte el objeto de salida a JSON utilizando el mismo objeto Gson
        String jsonOut = gson.toJson(out);
        JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(jsonOut).getAsJsonObject();

        return dataout;
    }

    public ModificarNCG499_Salida logica(Connection con, ModificarNCG499_Entrada entrada) throws Exception {
    	ModificarNCG499_Salida out = new ModificarNCG499_Salida();
    	String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");       
        String idTipoLog= "";
        String idTipoRegistro= "";
        BigDecimal numLimite = new BigDecimal("999999999999999.9999");
        BigDecimal cantUnidadesLog = BigDecimal.ZERO;
        String fuentePrecioLog = "";

        try {
            SQL sql = new SQL();
            List<NCG499> ncgUpdateEstado = new ArrayList<>();
            List<NCG499> ncgNuevo = new ArrayList<>();
            for (NCG499 ncg499 : entrada.NCG499_in)
            {
            	if((ncg499.Tir.compareTo((numLimite).multiply(new BigDecimal(-1), MathContext.DECIMAL128)) == -1) || (ncg499.Tir.compareTo(numLimite) == 1)) {     
            		String Codigo = "Tir Fuera de Rango";
            		out.P_OUT_ERRORESCONTADOR ++;
            		out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,Codigo,"Tir, Valor par o precio fuera de rango debe ser entre 15 enteros (positivo o negativo) y 4 decimales."));
        			
            	}
            	if((ncg499.MontoMercadoCustodia.compareTo((numLimite).multiply(new BigDecimal(-1), MathContext.DECIMAL128)) == -1) || (ncg499.MontoMercadoCustodia.compareTo(numLimite) == 1)) {     
            		String Codigo = "Valorizacion al cierre Fuera de Rango";
            		out.P_OUT_ERRORESCONTADOR ++;
            		out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,Codigo,"Valorizacion al cierre fuera de rango debe ser entre 15 enteros (positivo o negativo) y 4 decimales."));
        			
            	}
            	if((ncg499.Duration.compareTo((numLimite).multiply(new BigDecimal(-1), MathContext.DECIMAL128)) == -1) || (ncg499.Duration.compareTo(numLimite) == 1)) {     
            		String Codigo = "Duration Fuera de Rango";
            		out.P_OUT_ERRORESCONTADOR ++;
            		out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,Codigo,"Duration fuera de rango debe ser entre 15 enteros (positivo o negativo) y 4 decimales."));
        			
            	}
            	//retorna errores si es que hay más de uno
            	if(out.P_OUT_ERRORESCONTADOR > 0) {
            		return out;
            	}
            	
            	HashMap<String,Object> filtroNcg = new HashMap<>(5);
            	filtroNcg.put("Fecha",ncg499.Fecha);
            	filtroNcg.put("Nemotecnico",ncg499.Nemotecnico);
            	filtroNcg.put("IdEmisor",ncg499.IdEmisor);
            	//filtroNcg.put("IdEstado", ncg499.IdOrigen == "PAUTA" ? "ACT" : ncg499.IdEstado);
            	filtroNcg.put("IdOrigen",ncg499.IdOrigen);
            	filtroNcg.put("IdInstrumento",ncg499.IdInstrumento);

            	NCG499 ncg499Original = sql.query(con, SQL_SELECT_AGRUPADO,filtroNcg , NCG499.class).get(0);
            	NCG499 ncg499Obj = new NCG499();
            	ncg499Obj = ncg499Original;

                if ("MODIFICAR".equals(entrada.Accion)) {

                	ncg499Obj.SituacionInstrumento = ncg499.SituacionInstrumento;
                	ncg499Obj.ISIN = ncg499.ISIN;
            		ncg499Obj.FechaVencimiento = ncg499.FechaVencimiento;
            		ncg499Obj.ClasificacionRiesgoInstrumento = ncg499.ClasificacionRiesgoInstrumento;
            		ncg499Obj.TendenciaDeRiesgo = ncg499.TendenciaDeRiesgo;
            		ncg499Obj.BaseTasa = ncg499.BaseTasa;
            		ncg499Obj.Duration = ncg499.Duration.setScale(4, RoundingMode.DOWN);
            		ncg499Obj.CodigoPais = ncg499.CodigoPais;
            		ncg499Obj.CodigoPaisEmisionInstrumento = ncg499.CodigoPaisEmisionInstrumento;
            		ncg499Obj.CodigoValorizacion = ncg499.CodigoValorizacion;            		
            		
            		//Cantidad de unidades y fuente de precio va separado
            		if("NR".equals(ncg499.SituacionInstrumentoAux)) {
            			ncg499Obj.MontoMercadoCustodia = ncg499.MontoMercadoCustodia.setScale(4, RoundingMode.DOWN);
            			cantUnidadesLog = ncg499Obj.CantidadCustodia;
            			fuentePrecioLog = ncg499Obj.FuentePrecios == null ? "" : ncg499Obj.FuentePrecios;
            			ncg499Obj.CantidadCustodia = ncg499.CantidadUnidades.setScale(4, RoundingMode.DOWN);
            			ncg499Obj.FuentePrecios = ncg499.FuentePrecios == null ? "" :ncg499.FuentePrecios;
            		}
            		if("GT".equals(ncg499.SituacionInstrumentoAux)) {
            			ncg499Obj.ValorizacionGarantia = ncg499.MontoMercadoCustodia.setScale(4, RoundingMode.DOWN);
            			cantUnidadesLog = ncg499Obj.CantidadGarantia;
            			fuentePrecioLog = ncg499Obj.FuentePreciosGarantia == null ? "" : ncg499Obj.FuentePreciosGarantia ;
            			ncg499Obj.CantidadGarantia = ncg499.CantidadUnidades.setScale(4, RoundingMode.DOWN);
            			ncg499Obj.FuentePreciosGarantia = ncg499.FuentePrecios == null ? "" :ncg499.FuentePrecios;
            		}
            		if("PR".equals(ncg499.SituacionInstrumentoAux)) {
            			ncg499Obj.ValorizacionPrestamo = ncg499.MontoMercadoCustodia.setScale(4, RoundingMode.DOWN);
            			cantUnidadesLog = ncg499Obj.CantidadPrestamo;
            			fuentePrecioLog = ncg499Obj.FuentePreciosPrestamo == null ? "" : ncg499Obj.FuentePreciosPrestamo;
            			ncg499Obj.CantidadPrestamo = ncg499.CantidadUnidades.setScale(4, RoundingMode.DOWN);
            			ncg499Obj.FuentePreciosPrestamo = ncg499.FuentePrecios == null ? "" :ncg499.FuentePrecios;
            		}
            		if("OP".equals(ncg499.SituacionInstrumentoAux)) {

            			
            			if(ncg499.IdTipoInstrumentoCMF.equals("PCCV")) {
            				ncg499Obj.ValorizacionPlazoCompra = ncg499.MontoMercadoCustodia.setScale(4, RoundingMode.DOWN);
            				cantUnidadesLog = ncg499Obj.CantidadPlazoCompra;
            				ncg499Obj.CantidadPlazoCompra = ncg499.CantidadUnidades.setScale(4, RoundingMode.DOWN);
            			}
            			
            			if(ncg499.IdTipoInstrumentoCMF.equals("PVCC")) {
            				ncg499Obj.ValorizacionPlazoVenta = ncg499.MontoMercadoCustodia.setScale(4, RoundingMode.DOWN);
            				cantUnidadesLog = ncg499Obj.CantidadPlazoVenta;
            				ncg499Obj.CantidadPlazoVenta = ncg499.CantidadUnidades.setScale(4, RoundingMode.DOWN);
            			}
            			fuentePrecioLog = ncg499Obj.FuentePreciosPlazo == null ? "" : ncg499Obj.FuentePreciosPlazo;
            			ncg499Obj.FuentePreciosPlazo = ncg499.FuentePrecios == null ? "" :ncg499.FuentePrecios;
            			
            		}
            		
            		//TIR, valor par o precio (variable comun "Tir")
            		if("RV".equals(ncg499Original.IdClasificacionTipoInstrumento) || "FM".equals(ncg499Original.IdClasificacionTipoInstrumento)){
            			ncg499Obj.Tir = ncg499Original.PrecioMercadoCustodia.setScale(4, RoundingMode.DOWN);
            			ncg499Obj.PrecioMercadoCustodia = ncg499.Tir.setScale(4, RoundingMode.DOWN);
            		}
            		if("RF".equals(ncg499Original.IdClasificacionTipoInstrumento) || "IF".equals(ncg499Original.IdClasificacionTipoInstrumento)){
            			ncg499Obj.Tir = ncg499Original.TasaMercadoCustodia.setScale(4, RoundingMode.DOWN);
            			ncg499Obj.TasaMercadoCustodia = ncg499.Tir.setScale(4, RoundingMode.DOWN);
            		}

            		//Tipo Instrumento llega siempre el codigo de la CMF
            		HashMap<String,Object> filtroHomologa = new HashMap<>(3);
            		filtroHomologa.put("Tipo","INSTRUMENTOCMF");
            		filtroHomologa.put("ValorOrigen","");
            		filtroHomologa.put("ValorDestino",ncg499.IdTipoInstrumentoCMF);
            		Homologacion homologa = sql.query(con, SQL_SELECT_HOMOLOGACION,filtroHomologa , Homologacion.class).get(0);
            		
            		if(ncg499.IdTipoInstrumentoCMF.equals("PCCV") || ncg499.IdTipoInstrumentoCMF.equals("PCCO")) {
            			ncg499Obj.IdTipoInstrumentoPlazoCompra = ncg499.IdTipoInstrumentoCMF;
            			ncg499Obj.IdTipoInstrumentoCMF = ncg499.IdTipoInstrumentoCMF;
                		ncg499Obj.IdTipoInstrumento =  ncg499Original.IdTipoInstrumento;
            		} else if(ncg499.IdTipoInstrumentoCMF.equals("PVCC") || ncg499.IdTipoInstrumentoCMF.equals("PCV")) {
            			ncg499Obj.IdTipoInstrumentoPlazoVenta = ncg499.IdTipoInstrumentoCMF;
            			ncg499Obj.IdTipoInstrumentoCMF = ncg499.IdTipoInstrumentoCMF;
                		ncg499Obj.IdTipoInstrumento =  ncg499Original.IdTipoInstrumento;
            		} else {
            			ncg499Obj.IdTipoInstrumentoCMF = ncg499.IdTipoInstrumentoCMF;
                		ncg499Obj.IdTipoInstrumento =  !homologa.ValorOrigen.trim().isEmpty() ? homologa.ValorOrigen : ncg499Original.IdTipoInstrumento;
            		}
            		
            		/*La primera vez que entra un registro para modificar se actualiza el estado a inactivo y se inserta un nuevo registro de origen MANT (mantenedor)
            		con el estado ACT para ser mostrado en la vista, este registro nuevo viene con los montos sumados para que solo sea un registro y no muchos como cuando se respalda en la pauta*/
            		if("PAUTA".equalsIgnoreCase(ncg499.IdOrigen)) {
                		ncg499Obj.IdEstado = "INA";
                		ncgUpdateEstado.add(ncg499Obj);
                		sql.batch(con, SQL_UPDATE_ESTADO, ncgUpdateEstado, NCG499.class);
						if(ncg499.CantidadUnidades.compareTo(BigDecimal.ZERO) < 0){
							ncg499.IdEstado = "NEG";
						}
                		ncg499Obj.IdEstado = ncg499.IdEstado;
                		ncg499Obj.IdOrigen = "MANT";
                		ncgNuevo.add(ncg499Obj);
                		sql.batch(con, SQL_INSERT_NCG499, ncgNuevo, NCG499.class);
                	}
                	if("MANT".equalsIgnoreCase(ncg499.IdOrigen)) {
                		ncg499Obj.IdEstado = ncg499.IdEstado;
                		ncgNuevo.add(ncg499Obj);
                		sql.batch(con, SQL_UPDATE_NCG499, ncgNuevo, NCG499.class);
                	}
                	//Anula
                	if("ANU".equalsIgnoreCase(ncg499.IdEstado)) {
                		ncg499Obj.IdEstado = ncg499.IdEstado;
                		ncgUpdateEstado.add(ncg499Obj);
                		sql.batch(con, SQL_UPDATE_ESTADO, ncgUpdateEstado, NCG499.class);
                	}
                	String codigo = "UI_NCG499_OK";
					String desc = Diccionario.getMensaje(con, entrada.p_IdEmpresa, codigo).add("Nemotecnico", ncg499Obj.Nemotecnico).msg();
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_INFORMACION,codigo,desc));
                    idTipoLog = "MODIFICACION";
                    idTipoRegistro="NCG499";
                }

                // AGREGAR LOG
                AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
                entradaAgregarLog.p_IdEmpresa = entrada.p_IdEmpresa;
                entradaAgregarLog.p_IdUsuario = entrada.p_IdUsuario;
                entradaAgregarLog.p_IdModulo = "NOR";

                AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();

                log.IdCabeceraLog = (long) ncg499.Nemotecnico.hashCode();
                log.IdTipoLog = idTipoLog;
                log.IdTipoRegistro = idTipoRegistro;
                log.IdUsuario = entrada.p_IdUsuario;
                log.IdEmpresa = entrada.p_IdEmpresa;
                log.IdModulo = "NOR";
                log.FechaSistemaLog = fechaSistema;
                entradaAgregarLog.p_Log.add(log);

                // Detalle del log
                if(!ncg499Obj.ISIN.equals(ncg499.ISIN ))  {
    	            AgregarLog.InAgregarLog.DetalleLog isin = new AgregarLog.InAgregarLog.DetalleLog();
    	            isin.SubRegistro = ncg499Obj.Nemotecnico;
    	            isin.CampoRegistro = "ISIN";
    	            isin.IdCabeceraLog = (long) ncg499.Nemotecnico.hashCode();
    	            isin.ValorAntiguo = ncg499.ISIN != null ? ncg499.ISIN : "";
    	            isin.ValorNuevo = ncg499Obj.ISIN;
    	            entradaAgregarLog.p_LogDetalle.add(isin);
                }
                if(!ncg499Obj.FechaVencimiento.equals(ncg499.FechaVencimiento)) {
                	AgregarLog.InAgregarLog.DetalleLog fechavencimiento = new AgregarLog.InAgregarLog.DetalleLog();
                	fechavencimiento.SubRegistro = ncg499Obj.Nemotecnico;
                	fechavencimiento.CampoRegistro = "Fecha Vencimiento";
                	fechavencimiento.IdCabeceraLog = (long) ncg499.Nemotecnico.hashCode();
                	fechavencimiento.ValorAntiguo = ncg499.FechaVencimiento != null ? ncg499.FechaVencimiento.toString() : "";
                	fechavencimiento.ValorNuevo = ncg499Obj.FechaVencimiento.toString();
                    entradaAgregarLog.p_LogDetalle.add(fechavencimiento);
                }
                if(!ncg499Obj.ClasificacionRiesgoInstrumento.equals(ncg499.ClasificacionRiesgoInstrumento)) {
                	 AgregarLog.InAgregarLog.DetalleLog clasificaionderiesgo = new AgregarLog.InAgregarLog.DetalleLog();
                	 clasificaionderiesgo.SubRegistro = ncg499Obj.Nemotecnico;
                	 clasificaionderiesgo.CampoRegistro = "Clasificacion de Riesgo";
                	 clasificaionderiesgo.IdCabeceraLog = (long) ncg499.Nemotecnico.hashCode();
                	 clasificaionderiesgo.ValorAntiguo = ncg499.ClasificacionRiesgoInstrumento != null ? ncg499.ClasificacionRiesgoInstrumento.toString() : "";
                	 clasificaionderiesgo.ValorNuevo = ncg499Obj.ClasificacionRiesgoInstrumento.toString();
                     entradaAgregarLog.p_LogDetalle.add(clasificaionderiesgo);
                }
                if(!ncg499Obj.TendenciaDeRiesgo.equals(ncg499.TendenciaDeRiesgo)){
               	 AgregarLog.InAgregarLog.DetalleLog tendenciaderiesgo = new AgregarLog.InAgregarLog.DetalleLog();
               	tendenciaderiesgo.SubRegistro = ncg499Obj.Nemotecnico;
               	tendenciaderiesgo.CampoRegistro = "Tendencia de Riesgo";
               	tendenciaderiesgo.IdCabeceraLog = (long) ncg499.Nemotecnico.hashCode();
               	tendenciaderiesgo.ValorAntiguo = ncg499.TendenciaDeRiesgo != null ? ncg499.TendenciaDeRiesgo.toString() : "";
               	tendenciaderiesgo.ValorNuevo = ncg499Obj.TendenciaDeRiesgo.toString();
                    entradaAgregarLog.p_LogDetalle.add(tendenciaderiesgo);
               }
                if(!ncg499Obj.BaseTasa.equals(ncg499.BaseTasa)) {
               	 AgregarLog.InAgregarLog.DetalleLog basetasa = new AgregarLog.InAgregarLog.DetalleLog();
               	basetasa.SubRegistro = ncg499Obj.Nemotecnico;
               	basetasa.CampoRegistro = "Base Tasa";
               	basetasa.IdCabeceraLog = (long) ncg499.Nemotecnico.hashCode();
               	basetasa.ValorAntiguo = ncg499.BaseTasa != null ? ncg499.BaseTasa.toString() : "";
               	basetasa.ValorNuevo = ncg499Obj.BaseTasa.toString();
                    entradaAgregarLog.p_LogDetalle.add(basetasa);
               }
                if(!ncg499Obj.Duration.equals(ncg499.Duration)){
                  	 AgregarLog.InAgregarLog.DetalleLog duracion = new AgregarLog.InAgregarLog.DetalleLog();
                  	duracion.SubRegistro = ncg499Obj.Nemotecnico;
                  	duracion.CampoRegistro = "Duration";
                  	duracion.IdCabeceraLog = (long) ncg499.Nemotecnico.hashCode();
                  	duracion.ValorAntiguo = ncg499.Duration != null ? ncg499.Duration.toString() : "";
                  	duracion.ValorNuevo = ncg499Obj.Duration.toString();
                       entradaAgregarLog.p_LogDetalle.add(duracion);
                  }
                if(!ncg499Obj.CodigoPais.equals(ncg499.CodigoPais)){
                 	 AgregarLog.InAgregarLog.DetalleLog codigoPais = new AgregarLog.InAgregarLog.DetalleLog();
                 	codigoPais.SubRegistro = ncg499Obj.Nemotecnico;
                 	codigoPais.CampoRegistro = "Codigo Pais";
                 	codigoPais.IdCabeceraLog = (long) ncg499.Nemotecnico.hashCode();
                 	codigoPais.ValorAntiguo = ncg499.CodigoPais != null ? ncg499.CodigoPais : "";
                 	codigoPais.ValorNuevo = ncg499Obj.CodigoPais.toString();
                      entradaAgregarLog.p_LogDetalle.add(codigoPais);
                 }
                   if(!ncg499Obj.CodigoPaisEmisionInstrumento.equals(ncg499.CodigoPaisEmisionInstrumento)){
                	 AgregarLog.InAgregarLog.DetalleLog codigopaisinstrumento = new AgregarLog.InAgregarLog.DetalleLog();
                	codigopaisinstrumento.SubRegistro = ncg499Obj.Nemotecnico;
                	codigopaisinstrumento.CampoRegistro = "Codigo Pais Instrumento";
                	codigopaisinstrumento.IdCabeceraLog = (long) ncg499.Nemotecnico.hashCode();
                	codigopaisinstrumento.ValorAntiguo = ncg499.CodigoPaisEmisionInstrumento != null ? ncg499.CodigoPaisEmisionInstrumento : "";
                	codigopaisinstrumento.ValorNuevo = ncg499Obj.CodigoPaisEmisionInstrumento.toString();
                     entradaAgregarLog.p_LogDetalle.add(codigopaisinstrumento);
                }
                   	if(!fuentePrecioLog.equals(ncg499.FuentePrecios)){
                    	 AgregarLog.InAgregarLog.DetalleLog fuentePrecioMod = new AgregarLog.InAgregarLog.DetalleLog();
                    	 fuentePrecioMod.SubRegistro = ncg499.Nemotecnico;
                    	 fuentePrecioMod.CampoRegistro = "Fuente de Precios";
                    	 fuentePrecioMod.IdCabeceraLog = (long) ncg499.Nemotecnico.hashCode();
                    	 fuentePrecioMod.ValorAntiguo = fuentePrecioLog;
                    	 fuentePrecioMod.ValorNuevo = ncg499.FuentePrecios;
                        entradaAgregarLog.p_LogDetalle.add(fuentePrecioMod);
                }
	               	if(!ncg499Obj.IdTipoInstrumento.equals(ncg499.IdTipoInstrumentoCMF)){
	               	 AgregarLog.InAgregarLog.DetalleLog tipoInstrumentoMod = new AgregarLog.InAgregarLog.DetalleLog();
	               	tipoInstrumentoMod.SubRegistro = ncg499.Nemotecnico;
	               	tipoInstrumentoMod.CampoRegistro = "Id Tipo Instrumento";
	               	tipoInstrumentoMod.IdCabeceraLog = (long) ncg499.Nemotecnico.hashCode();
	               	tipoInstrumentoMod.ValorAntiguo = ncg499.IdTipoInstrumento.toString();
	               	tipoInstrumentoMod.ValorNuevo = ncg499Obj.IdTipoInstrumento.toString();
	                   entradaAgregarLog.p_LogDetalle.add(tipoInstrumentoMod);
               }
	               if(!ncg499Obj.Tir.equals(ncg499.Tir)) {
	                  	AgregarLog.InAgregarLog.DetalleLog tirMod = new AgregarLog.InAgregarLog.DetalleLog();
	                  	tirMod.SubRegistro = ncg499Obj.Nemotecnico;
	                  	tirMod.CampoRegistro = "TIR";
	                  	tirMod.IdCabeceraLog = (long) ncg499.Nemotecnico.hashCode();
	                  	tirMod.ValorAntiguo = ncg499.Tir != null ? ncg499.Tir.toString() : "";
	                  	tirMod.ValorNuevo = ncg499Obj.Tir.toString();
	                      entradaAgregarLog.p_LogDetalle.add(tirMod);
	                  }
	             if(!cantUnidadesLog.equals(ncg499.CantidadUnidades)) {
	                  	AgregarLog.InAgregarLog.DetalleLog canUniMod = new AgregarLog.InAgregarLog.DetalleLog();
	                  	canUniMod.SubRegistro = ncg499Obj.Nemotecnico;
	                  	canUniMod.CampoRegistro = "Cantidad Unidades";
	                  	canUniMod.IdCabeceraLog = (long) ncg499.Nemotecnico.hashCode();
	                  	canUniMod.ValorAntiguo = cantUnidadesLog.toString();
	                  	canUniMod.ValorNuevo = ncg499.CantidadUnidades.toString();
	                      entradaAgregarLog.p_LogDetalle.add(canUniMod);
	                  }
	             if(!ncg499Obj.CodigoValorizacion.equals(ncg499.CodigoValorizacion))  {
	    	            AgregarLog.InAgregarLog.DetalleLog codVal = new AgregarLog.InAgregarLog.DetalleLog();
	    	            codVal.SubRegistro = ncg499Obj.Nemotecnico;
	    	            codVal.CampoRegistro = "Codigo Valorizacion";
	    	            codVal.IdCabeceraLog = (long) ncg499.Nemotecnico.hashCode();
	    	            codVal.ValorAntiguo = ncg499.CodigoValorizacion != null ? ncg499.CodigoValorizacion : "";
	    	            codVal.ValorNuevo = ncg499Obj.CodigoValorizacion;
	    	            entradaAgregarLog.p_LogDetalle.add(codVal);
	                } 
	             if(!ncg499Obj.MontoMercadoCustodia.equals(ncg499.MontoMercadoCustodia)) {
	                  	AgregarLog.InAgregarLog.DetalleLog valorizaMod = new AgregarLog.InAgregarLog.DetalleLog();
	                  	valorizaMod.SubRegistro = ncg499Obj.Nemotecnico;
	                  	valorizaMod.CampoRegistro = "Valorizacion al Cierre";
	                  	valorizaMod.IdCabeceraLog = (long) ncg499.Nemotecnico.hashCode();
	                  	valorizaMod.ValorAntiguo = ncg499Original.MontoMercadoCustodia != null ? ncg499Original.MontoMercadoCustodia.toString() : "";
	                  	valorizaMod.ValorNuevo = ncg499Obj.MontoMercadoCustodia.toString();
	                      entradaAgregarLog.p_LogDetalle.add(valorizaMod);
	                  }
	           if(!entradaAgregarLog.p_LogDetalle.isEmpty()) {
	        	   logger.info("Agregando log para NCG499 {} con acción {}", ncg499.Nemotecnico.toString(), entrada.Accion);	                
	                AgregarLog agregarLogService = new AgregarLog();
	                agregarLogService.logica(con, entradaAgregarLog);
	           }                
            }

            con.commit();
            return out;
        } catch (Exception e) {
            con.rollback();
            throw e;
        }
    }
    
    

    public static void main(String[] args) throws Exception {
        // Prueba de la clase
        try {
            Connection con = PlataformaUtils.getPropertiesConnection("desa02", "connections.properties");
            try {
                Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
                String jsonModificar = "{\"NCG499_in\":[{\"Fecha\":\"2025-02-11T03:00:00.000Z\",\"IdClasificacionTipoInstrumento\":\"110211\",\"Nemotecnico\":\"COPEC\",\"ISIN\":\"\",\"SituacionInstrumento\":\"NR\",\"IdEmisor\":\"COPEC\",\"CodigoPais\":\"CL\",\"IdTipoInstrumentoCMF\":\"CFM\",\"FechaVencimiento\":\"0001-01-01T04:42:45.000Z\",\"ClasificacionRiesgoInstrumento\":\"\",\"TendenciaDeRiesgo\":\"\",\"CantidadUnidades\":\"57\",\"Tir\":\"1795.56\",\"CodigoValorizacion\":\"PR\",\"BaseTasa\":\"0\",\"MontoMercadoCustodia\":\"102346.92\",\"CodigoPaisEmisionInstrumento\":\"CL\",\"FuentePrecios\":\"PRECIOSAGF\",\"Duration\":\"0\",\"IdOrigen\":\"PAUTA\",\"IdEstado\":\"ACT\",\"SituacionInstrumentoAux\":\"NR\",\"IdInstrumento\":267751}],\"Accion\":\"MODIFICAR\",\"p_IdEmpresa\":1,\"p_IdUsuario\":97}";
                ModificarNCG499_Entrada entradaModificar = gson.fromJson(jsonModificar, ModificarNCG499_Entrada.class);
                ModificarNCG499 demo = new ModificarNCG499();
                ModificarNCG499_Salida salidaModificar = demo.logica(con, entradaModificar);
                System.out.println(salidaModificar.toString());
                // con.rollback();
                //con.commit();
            } catch (Exception e) {
                con.rollback();
                e.printStackTrace();
            } finally {
                con.close();
            }
           } catch (Exception e) {
                e.printStackTrace();
            }

            System.out.println("== END ==");
        }
    }




