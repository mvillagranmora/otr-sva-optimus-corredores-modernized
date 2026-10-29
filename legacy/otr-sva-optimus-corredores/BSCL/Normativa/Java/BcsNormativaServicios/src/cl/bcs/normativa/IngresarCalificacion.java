package cl.bcs.normativa;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;

import cl.bcs.normativa.IngresarMLT.RegistroMLT;
import cl.bcs.plataforma.pla.utils.FileLoader;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class IngresarCalificacion implements JavaCall { 
	
	public static class ObjDetalleParidad {
        public String toString() {
            return Utils.printJson(this, true);
        }
    }
    
    public static class AgregarModificarCalificacion_Entrada {
        public List<Calificacion> Calificacion_in;
        public String Accion;
        
    }
    
    public static class Calificacion {
        public BigDecimal EJERCICIO;
		public String MERCADO;
		public String NEMO;
		public String FEC_PAGO;
		public BigDecimal SEC_EVE;
		public String DESCRIPCION;
		public BigDecimal FACTOR_06;
		public BigDecimal FACTOR_07;
		public BigDecimal FACTOR_08;
		public BigDecimal FACTOR_09;
		public BigDecimal FACTOR_10;
		public BigDecimal FACTOR_11;
		public BigDecimal FACTOR_12;
		public BigDecimal NUM_DIVDO;
		public String TIPO_SOC;
		public BigDecimal FACTOR_13;
		public BigDecimal FACTOR_14;
		public BigDecimal FACTOR_15;
		public BigDecimal FACTOR_16;
		public BigDecimal FACTOR_17;
		public BigDecimal FACTOR_18;
		public BigDecimal FACTOR_19;
		public BigDecimal FACTOR_20;
		public BigDecimal FACTOR_21;
		public BigDecimal FACTOR_22;
		public BigDecimal FACTOR_23;
		public BigDecimal FACTOR_24;
		public BigDecimal FACTORACT;
		public BigDecimal NUMCERTSOC;
		public BigDecimal FACTOR_25;
		public BigDecimal FACTOR_26;
		public BigDecimal FACTOR_27;
		public BigDecimal FACTOR_28;
		public BigDecimal FACTOR_29;
		public BigDecimal FACTOR_30;
		public BigDecimal FACTOR_31;
		public BigDecimal FACTOR_32;
		public BigDecimal FACTOR_33;
		public BigDecimal FACTOR_34;
		public BigDecimal FACTOR_35;
		public BigDecimal FACTOR_36;
		public BigDecimal FACTOR_37;
		public BigDecimal FACTOR_38;
		public String FechaUltimaMod;
		public String UsuarioUltimaMod;
		public String AcogidoISFUT;
		public String IDORIGEN;
    }
    
    public static class MantenedorCalificacion_Entrada {
        public List<Calificacion> Calificacion_in;
        public String Accion;
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
    }

    public static class MantenedorCalificacion_Salida {
        public Long errorCount;
        public String mensaje;
    }

    public static class ConsultarNemo {
        public Long EJERCICIO;
		public String NEMO;
		public String FEC_PAGO;
        public String IDORIGEN;
    }

    public static class ConsultarNemo_Entrada {
    	public Long EJERCICIO; 
    }

    public static class ConsultarNemo_Salida {
        public Long errorCount;
        public String mensaje;
		public List<ConsultarNemo>	P_OUT_NEMOS;

		public ConsultarNemo_Salida() {
			P_OUT_NEMOS = new ArrayList<>();
		}

		public String toString() {
			return Utils.printJson(this, true);
		}
    }

    private static final FileLoader LOADER = new FileLoader();

    private static final String SQL_INSERT_CALIFICACION = LOADER.loadResourceToString("cl/bcs/normativa/sql/Calificacion_InsertRegistro.sql");
    private static final String SQL_UPDATE_CALIFICACION = LOADER.loadResourceToString("cl/bcs/normativa/sql/Calificacion_UpdateRegistro.sql");
    private static final String SQL_DELETE_CALIFICACION = LOADER.loadResourceToString("cl/bcs/normativa/sql/Calificacion_DeleteRegistro.sql");
    private static final String SQL_CONSULTAR_NEMOS     = LOADER.loadResourceToString("cl/bcs/normativa/sql/Calificacion_ConsultarNemos.sql");


    public JsonObject call(Connection con, JsonObject jin) throws Exception {
    	Gson gson = new Gson();
    	JsonParser parser = new JsonParser();

        if (jin.has("Calificacion_in")) {
        	System.out.println("== Call calificacion ==");
        	
            MantenedorCalificacion_Entrada in = gson.fromJson(jin.toString(), MantenedorCalificacion_Entrada.class);
            MantenedorCalificacion_Salida out = logica(con, in);
            
            JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
            return dataout;
        	
        } else {
            System.out.println("== Call consultaNemo ==");
        	
            ConsultarNemo_Entrada in = gson.fromJson(jin.toString(), ConsultarNemo_Entrada.class);
            ConsultarNemo_Salida out = consultarNemo(con, in);
            
            JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
            return dataout;
        }
    }                

    public ConsultarNemo_Salida consultarNemo(Connection con, ConsultarNemo_Entrada entrada) throws Exception {
    	System.out.println("== Inicio ConsultarNemo ==");
    	
        ConsultarNemo_Salida out = new ConsultarNemo_Salida();
        out.errorCount = 0L;
        out.mensaje = "";
        HashMap<String,Object> params = new HashMap<>();

        try {
            SQL sql = new SQL();

            params.put("EJERCICIO", entrada.EJERCICIO);
            
            List<ConsultarNemo> nemos = sql.query(con, SQL_CONSULTAR_NEMOS, params, ConsultarNemo.class);
            
            System.out.println("Resultado de la consulta:");
            for (ConsultarNemo nemo : nemos) {
                System.out.println("NEMO: " + nemo.NEMO + ", FEC_PAGO: " + nemo.FEC_PAGO + ", IDORIGEN: " + nemo.IDORIGEN);
            }
            
            out.mensaje = "OK NEMOS";
            out.P_OUT_NEMOS = nemos;
            
            con.commit();
            return out;

        } catch (Exception e) {
            con.rollback();
            throw e;
        }
    }

    public MantenedorCalificacion_Salida logica(Connection con, MantenedorCalificacion_Entrada entrada) throws Exception {
    	System.out.println("== Inicio logica calificacion ==");
    	
    	
    	MantenedorCalificacion_Salida out = new MantenedorCalificacion_Salida();
        out.errorCount = 0L;
        out.mensaje = "";
               
        try {
            SQL sql = new SQL();

            switch(entrada.Accion) {
                case "CREAR":
                    sql.batch(con, SQL_INSERT_CALIFICACION, entrada.Calificacion_in, Calificacion.class);
                    break;
                case "UPDATE":
                    sql.batch(con, SQL_UPDATE_CALIFICACION, entrada.Calificacion_in, Calificacion.class);
                    break;
                case "DELETE":
                    sql.batch(con, SQL_DELETE_CALIFICACION, entrada.Calificacion_in, Calificacion.class);
                    break;
                default:
                    out.mensaje="Accion no reconocida: " + entrada.Accion;
                    return out;
            }
            
            //LOG AUDITORIA
            AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
            String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, 1L, "PLATAFORMA", "PLA.FechaSistema");
			Long idCabeceraLog = 159L;
            if (!entrada.Calificacion_in.isEmpty() && entrada.Calificacion_in != null) {
	            for (Calificacion datosLog: entrada.Calificacion_in) {
	            	//cabecera log
	            	AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
					log.IdCabeceraLog = idCabeceraLog;
					log.IdTipoLog = "";
					log.IdTipoRegistro = "";

					if("CREAR".equalsIgnoreCase(entrada.Accion)){
						log.IdTipoLog = "INGRESO";
						log.IdTipoRegistro = "INGRESAR_CALIFICACION";
					}
					if("UPDATE".equalsIgnoreCase(entrada.Accion)){
						log.IdTipoLog = "MODIFICACION";
						log.IdTipoRegistro = "MODIFICAR_CALIFICACION";
					}
					if("DELETE".equalsIgnoreCase(entrada.Accion)){
						log.IdTipoLog = "ANULACION";
						log.IdTipoRegistro = "ANULAR_CALIFICACION";
					}
					
					log.IdModulo = "NOR";
					log.IdUsuario = entrada.p_IdUsuario != null ? entrada.p_IdUsuario : 0L;
					log.IdEmpresa = entrada.p_IdEmpresa != null ? entrada.p_IdEmpresa : 0L;
					log.FechaSistemaLog = fechaSistema != null ? fechaSistema : "";
					log.IdUsuarioAtendido = 0L;
					log.IdCliente = 0L;
					log.EstadoCliente = "";
					log.NombreCliente = "";
					log.IdTipoDocumentoIdentidad = "";
					log.IdDocumentoIdentidad = "";
					log.IdPortafolio = 0L;
					log.EstadoPortafolio = "";
					log.EstadoRegistro = "";
					log.IdTipoOperacion = "";
					log.Folio = 0L;
					log.Cantidad = BigDecimal.ZERO;
					log.PrecioTasa = BigDecimal.ZERO;
					log.Monto = BigDecimal.ZERO;
					log.IdMonedaOrigen = "";
					log.IdMonedaLiquidacion = "";
					log.TipoCambio = "";
					log.Nemo = datosLog.NEMO != null ? datosLog.NEMO : "";
					log.IdProceso = 0L;
					log.EstadoProceso = "";
					log.NombreProceso = "IngresarCalificacion";						
					entradaAgregarLog.p_Log.add(log);
					
					//detalle log 
					AgregarLog.InAgregarLog.DetalleLog detLog = new AgregarLog.InAgregarLog.DetalleLog();
					detLog.IdCabeceraLog = idCabeceraLog;
					detLog.IdTrabajo = 0L;
					detLog.SubRegistro = log.IdTipoRegistro != null ? log.IdTipoRegistro : "";
					detLog.CampoRegistro = "Instrumento  ";
					detLog.ValorAntiguo = "";
					detLog.ValorNuevo = datosLog.NEMO != null ? datosLog.NEMO : "";
					entradaAgregarLog.p_LogDetalle.add(detLog);
					
					AgregarLog.InAgregarLog.DetalleLog detLog2 = new AgregarLog.InAgregarLog.DetalleLog();
					detLog2.IdCabeceraLog = idCabeceraLog;
					detLog2.IdTrabajo = 0L;
					detLog2.SubRegistro = log.IdTipoRegistro != null ? log.IdTipoRegistro : "";
					detLog2.CampoRegistro = "Descripcion ";
					detLog2.ValorAntiguo = "";
					detLog2.ValorNuevo = datosLog.DESCRIPCION != null ? datosLog.DESCRIPCION : "";
					entradaAgregarLog.p_LogDetalle.add(detLog2);
					
					AgregarLog.InAgregarLog.DetalleLog detLog3 = new AgregarLog.InAgregarLog.DetalleLog();
					detLog3.IdCabeceraLog = idCabeceraLog;
					detLog3.IdTrabajo = 0L;
					detLog3.SubRegistro = log.IdTipoRegistro != null ? log.IdTipoRegistro : "";
					detLog3.CampoRegistro = "Secuencia Evento ";
					detLog3.ValorAntiguo = "";
					if (datosLog != null && datosLog.SEC_EVE != null && datosLog.SEC_EVE.toString() != null) {
						detLog3.ValorNuevo = datosLog.SEC_EVE.toString();
					} else { 
						detLog3.ValorNuevo = "";
					}
					entradaAgregarLog.p_LogDetalle.add(detLog3);
					
					idCabeceraLog = idCabeceraLog + 1L;
				}
	            
	            AgregarLog agregarLogService = new AgregarLog();
				agregarLogService.logica(con, entradaAgregarLog);
			}
            out.mensaje = "OK";
            con.commit();            
            return out;

        } catch (Exception e) {
            con.rollback();
            throw e;
        }
    }
    
    public static void main(String[] args) throws Exception {
    	
    	System.out.println("== TEST ==");
    	
    	try {
    		
    		Connection con = PlataformaUtils.getDESAConnection();
    		
    		try {
    			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
            	
            	String objCalificacion = "{\"Accion\": \"CREAR\", \"Calificacion_in\": [{\"EJERCICIO\": 2022}]}";
            	String objNemo = "{\"EJERCICIO\": 2022}";
            	
            	String json = objCalificacion;
            	
            	MantenedorCalificacion_Entrada ObjIn_json = gson.fromJson(json, MantenedorCalificacion_Entrada.class);
            	//ConsultarNemo_Entrada ObjIn_json = gson.fromJson(json, ConsultarNemo_Entrada.class);
            	
    			IngresarCalificacion demo = new IngresarCalificacion();
    			
    			MantenedorCalificacion_Salida ObjOut = demo.logica(con, ObjIn_json);
    			//ConsultarNemo_Salida ObjOut = demo.consultarNemo(con, ObjIn_json);
    			
    			
    			System.out.println(ObjOut.toString());
    			
    			con.commit();
            	
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
