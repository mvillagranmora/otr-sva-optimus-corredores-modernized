package cl.bcs.plataforma.pla;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class GuardarConfiguracionGrillaPredeterminado implements JavaCall {
	
	private final String SQL_UPDATE_CONFIGURACION= loadResourceToString("cl/bcs/plataforma/pla/sql/GuardarConfiguracionGrillaPredeterminado_Update.sql");
	private final String SQL_INSERT_CONFIGURACION = loadResourceToString("cl/bcs/plataforma/pla/sql/GuardarConfiguracionGrillaPredeterminado_Insert.sql");
	private final String SQL_SELECT_CONFIGURACION = loadResourceToString("cl/bcs/plataforma/pla/sql/GuardarConfiguracionGrillaPredeterminado_Select.sql");
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = new Gson();		
		Parametros_Entrada in = gson.fromJson(jin.toString() ,Parametros_Entrada.class);
		Parametros_Salida out = logica(conn,in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_ERRORESDETALLE);
		
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public static class Parametros_Entrada{
		public Long p_IdEmpresa;
		public String p_IdModulo;
		public List<ObjConfiguracion> p_Configuracion;
	}
	
	public static class Parametros_Salida{
		public String P_OUT_RESULTADO;
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_ERRORESDETALLE;		
		
		public Parametros_Salida(){
			P_OUT_ERRORESDETALLE = new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ObjConfiguracion{
		public String Llave;
		public String Valor;
	}
	
	public static class EntidadConfiguracion{
		public Long IdConfiguracionGrilla;
		public Long p_IdEmpresa;
		public String p_Llave;	
		public String p_Valor;
	}
	
	public Parametros_Salida logica(Connection conn, Parametros_Entrada entrada) throws Exception {
		SQL sql = new SQL();
		Parametros_Salida salida = new Parametros_Salida();
		
		HashMap<String,Object> datosBusqueda = new HashMap<String,Object>();
		datosBusqueda.put("p_IdEmpresa", entrada.p_IdEmpresa);
		datosBusqueda.put("p_Llave", entrada.p_Configuracion.get(0).Llave);
	    
		List<EntidadConfiguracion> existe = sql.query(conn, SQL_SELECT_CONFIGURACION, datosBusqueda, EntidadConfiguracion.class);
	    
		EntidadConfiguracion datosActualizacion = new EntidadConfiguracion();
		datosActualizacion.p_IdEmpresa = entrada.p_IdEmpresa;
		datosActualizacion.p_Llave = entrada.p_Configuracion.get(0).Llave;
		datosActualizacion.p_Valor = entrada.p_Configuracion.get(0).Valor;
		List<EntidadConfiguracion> lstEntidadConfiguracion = new ArrayList<EntidadConfiguracion>();
		lstEntidadConfiguracion.add(datosActualizacion);
		
		if(existe.size() > 0){
			sql.batch(conn, SQL_UPDATE_CONFIGURACION, lstEntidadConfiguracion,EntidadConfiguracion.class);
		}else{ 
			sql.batch(conn, SQL_INSERT_CONFIGURACION, lstEntidadConfiguracion, EntidadConfiguracion.class);		
		}
	    
	    salida.P_OUT_ERRORESCONTADOR = 0L;
	    salida.P_OUT_ERRORESDETALLE.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_OK, "UI_INFO", "Se guardo correctamente la configuración"));
		   
		return salida;
	}
	

	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
		    String json = "{\"p_IdEmpresa\":1,\"p_IdModulo\":\"PLA\",\"p_Configuracion\":[{\"Llave\":\"1::DEFAULT-GRID_gridEventoCapitalCliente\",\"Valor\":\"{\\\"columns\\\":[{\\\"level\\\":0,\\\"title\\\":\\\"Tipo Evento Capital Cliente\\\",\\\"field\\\":\\\"IdEventoCapitalCliente\\\",\\\"visible\\\":true,\\\"locked\\\":false,\\\"width\\\":40},{\\\"level\\\":0,\\\"title\\\":\\\"Tipo Evento\\\",\\\"field\\\":\\\"IdTipoEventoCapital\\\",\\\"visible\\\":true,\\\"locked\\\":false,\\\"width\\\":90},{\\\"level\\\":0,\\\"title\\\":\\\"Número Dividendo\\\",\\\"field\\\":\\\"NumeroDividendo\\\",\\\"visible\\\":true,\\\"locked\\\":false,\\\"width\\\":50},{\\\"level\\\":0,\\\"title\\\":\\\"Secuencia\\\",\\\"field\\\":\\\"Secuencia\\\",\\\"visible\\\":true,\\\"locked\\\":false,\\\"width\\\":60},{\\\"level\\\":0,\\\"title\\\":\\\"Codigo Portafolio\\\",\\\"field\\\":\\\"CodigoPortafolio\\\",\\\"visible\\\":true,\\\"locked\\\":false,\\\"width\\\":70},{\\\"level\\\":0,\\\"title\\\":\\\"Código Clasificación Tipo Instrumento\\\",\\\"field\\\":\\\"IdClasificacionTipoInstrumento\\\",\\\"visible\\\":true,\\\"locked\\\":false,\\\"width\\\":40},{\\\"level\\\":0,\\\"title\\\":\\\"Nemo\\\",\\\"field\\\":\\\"Nemo\\\",\\\"visible\\\":true,\\\"locked\\\":false,\\\"width\\\":90},{\\\"level\\\":0,\\\"title\\\":\\\"ISIN\\\",\\\"field\\\":\\\"ISIN\\\",\\\"visible\\\":true,\\\"locked\\\":false,\\\"width\\\":80},{\\\"level\\\":0,\\\"title\\\":\\\"Nemo Nuevo\\\",\\\"field\\\":\\\"NemoNuevo\\\",\\\"visible\\\":true,\\\"locked\\\":false,\\\"width\\\":90},{\\\"level\\\":0,\\\"title\\\":\\\"Concepto Garantía\\\",\\\"field\\\":\\\"IdConceptoGarantia\\\",\\\"visible\\\":true,\\\"locked\\\":false,\\\"width\\\":90},{\\\"level\\\":0,\\\"title\\\":\\\"Tipo Cartera\\\",\\\"field\\\":\\\"TipoCartera\\\",\\\"visible\\\":true,\\\"locked\\\":false,\\\"width\\\":80},{\\\"level\\\":0,\\\"title\\\":\\\"Cliente\\\",\\\"field\\\":\\\"DocumentoIdentidad\\\",\\\"visible\\\":true,\\\"locked\\\":false,\\\"width\\\":70},{\\\"level\\\":0,\\\"title\\\":\\\"Nombre Cliente\\\",\\\"field\\\":\\\"NombreCliente\\\",\\\"visible\\\":true,\\\"locked\\\":false,\\\"width\\\":230},{\\\"level\\\":0,\\\"title\\\":\\\"Preferencias Notificaciones\\\",\\\"field\\\":\\\"ConfiguracionNotificacion\\\",\\\"visible\\\":true,\\\"locked\\\":false,\\\"width\\\":80},{\\\"level\\\":0,\\\"title\\\":\\\"Preferencia de Pago\\\",\\\"field\\\":\\\"TienePreferenciaDePago\\\",\\\"visible\\\":true,\\\"locked\\\":false,\\\"width\\\":80},{\\\"level\\\":0,\\\"title\\\":\\\"Monto Capital\\\",\\\"field\\\":\\\"MontoCapital\\\",\\\"visible\\\":true,\\\"locked\\\":false,\\\"width\\\":80},{\\\"level\\\":0,\\\"title\\\":\\\"Precio\\\",\\\"field\\\":\\\"Precio\\\",\\\"visible\\\":true,\\\"locked\\\":false,\\\"width\\\":80},{\\\"level\\\":0,\\\"title\\\":\\\"Factor\\\",\\\"field\\\":\\\"Factor\\\",\\\"visible\\\":true,\\\"locked\\\":false,\\\"width\\\":80},{\\\"level\\\":0,\\\"title\\\":\\\"Precio Nuevo\\\",\\\"field\\\":\\\"PrecioNuevo\\\",\\\"visible\\\":true,\\\"locked\\\":false,\\\"width\\\":80},{\\\"level\\\":0,\\\"title\\\":\\\"Cantidad Nueva\\\",\\\"field\\\":\\\"CantidadNueva\\\",\\\"visible\\\":true,\\\"locked\\\":false,\\\"width\\\":60},{\\\"level\\\":0,\\\"title\\\":\\\"Monto Interés\\\",\\\"field\\\":\\\"MontoInteres\\\",\\\"visible\\\":true,\\\"locked\\\":false,\\\"width\\\":80},{\\\"level\\\":0,\\\"title\\\":\\\"Monto Reajuste\\\",\\\"field\\\":\\\"MontoReajuste\\\",\\\"visible\\\":true,\\\"locked\\\":false,\\\"width\\\":80},{\\\"level\\\":0,\\\"title\\\":\\\"Cantidad con Derecho\\\",\\\"field\\\":\\\"CantidadDerecho\\\",\\\"visible\\\":true,\\\"locked\\\":false,\\\"width\\\":100},{\\\"level\\\":0,\\\"title\\\":\\\"Total\\\",\\\"field\\\":\\\"Total\\\",\\\"visible\\\":true,\\\"locked\\\":false,\\\"width\\\":100},{\\\"level\\\":0,\\\"title\\\":\\\"UI_MONTO_RETENIDO\\\",\\\"field\\\":\\\"MontoRetencion\\\",\\\"visible\\\":true,\\\"locked\\\":false,\\\"width\\\":100},{\\\"level\\\":0,\\\"title\\\":\\\"Cantidad derecho archivo\\\",\\\"field\\\":\\\"CantidadDerechoArchivo\\\",\\\"visible\\\":true,\\\"locked\\\":false,\\\"width\\\":100},{\\\"level\\\":0,\\\"title\\\":\\\"Cantidad nueva archivo\\\",\\\"field\\\":\\\"AccionesDisponiblesArchivo\\\",\\\"visible\\\":true,\\\"locked\\\":false,\\\"width\\\":100},{\\\"level\\\":0,\\\"title\\\":\\\"Diferencia cantidad derecho\\\",\\\"field\\\":\\\"DiferenciaCantidadDerecho\\\",\\\"visible\\\":true,\\\"locked\\\":false,\\\"width\\\":100},{\\\"level\\\":0,\\\"title\\\":\\\"Diferencia cantidad nueva\\\",\\\"field\\\":\\\"DiferenciaAccionesCantidadNueva\\\",\\\"visible\\\":true,\\\"locked\\\":false,\\\"width\\\":100},{\\\"level\\\":0,\\\"title\\\":\\\"Monto beneficio archivo\\\",\\\"field\\\":\\\"MontoBeneficioArchivo\\\",\\\"visible\\\":true,\\\"locked\\\":false,\\\"width\\\":100},{\\\"level\\\":0,\\\"title\\\":\\\"Diferencia monto beneficio\\\",\\\"field\\\":\\\"DiferenciaMontoBeneficio\\\",\\\"visible\\\":true,\\\"locked\\\":false,\\\"width\\\":100},{\\\"level\\\":0,\\\"title\\\":\\\"Monto interés archivo\\\",\\\"field\\\":\\\"MontoInteresArchivo\\\",\\\"visible\\\":true,\\\"locked\\\":false,\\\"width\\\":100},{\\\"level\\\":0,\\\"title\\\":\\\"Monto capital archivo\\\",\\\"field\\\":\\\"MontoCapitalArchivo\\\",\\\"visible\\\":true,\\\"locked\\\":false,\\\"width\\\":100},{\\\"level\\\":0,\\\"title\\\":\\\"Diferencia monto interés\\\",\\\"field\\\":\\\"DiferenciaMontoInteres\\\",\\\"visible\\\":true,\\\"locked\\\":false,\\\"width\\\":100},{\\\"level\\\":0,\\\"title\\\":\\\"Diferencia monto capital\\\",\\\"field\\\":\\\"DiferenciaMontoCapital\\\",\\\"visible\\\":true,\\\"locked\\\":false,\\\"width\\\":100},{\\\"level\\\":0,\\\"title\\\":\\\"UI_MONTO_RETENIDO_ARCHIVO\\\",\\\"field\\\":\\\"MontoRetencionArchivo\\\",\\\"visible\\\":true,\\\"locked\\\":false,\\\"width\\\":100},{\\\"level\\\":0,\\\"title\\\":\\\"UI_DIFERENCIA_MONTO_RETENIDO\\\",\\\"field\\\":\\\"DiferenciaMontoRetencion\\\",\\\"visible\\\":true,\\\"locked\\\":false,\\\"width\\\":100},{\\\"level\\\":0,\\\"title\\\":\\\"Moneda\\\",\\\"field\\\":\\\"IdMoneda\\\",\\\"visible\\\":true,\\\"locked\\\":false,\\\"width\\\":60},{\\\"level\\\":0,\\\"title\\\":\\\"Fecha Pago\\\",\\\"field\\\":\\\"FechaPago\\\",\\\"visible\\\":true,\\\"locked\\\":false,\\\"width\\\":80},{\\\"level\\\":0,\\\"title\\\":\\\"Agente\\\",\\\"field\\\":\\\"CodigoUsuarioAgente\\\",\\\"visible\\\":true,\\\"locked\\\":false,\\\"width\\\":60},{\\\"level\\\":0,\\\"title\\\":\\\"Nombre\\\",\\\"field\\\":\\\"NombreAgente\\\",\\\"visible\\\":true,\\\"locked\\\":false,\\\"width\\\":180},{\\\"level\\\":0,\\\"title\\\":\\\"Bóveda\\\",\\\"field\\\":\\\"IdBoveda\\\",\\\"visible\\\":true,\\\"locked\\\":false,\\\"width\\\":60},{\\\"level\\\":0,\\\"title\\\":\\\"Estado\\\",\\\"field\\\":\\\"IdEstado\\\",\\\"visible\\\":true,\\\"locked\\\":false,\\\"width\\\":60},{\\\"level\\\":0,\\\"title\\\":\\\"Origen\\\",\\\"field\\\":\\\"Origen\\\",\\\"visible\\\":true,\\\"locked\\\":false,\\\"width\\\":60},{\\\"level\\\":0,\\\"title\\\":\\\"Notificación\\\",\\\"field\\\":\\\"EstadoNotificacion\\\",\\\"visible\\\":true,\\\"locked\\\":false,\\\"width\\\":90},{\\\"level\\\":0,\\\"title\\\":\\\"Medio Pago\\\",\\\"field\\\":\\\"NombreMedioDePago\\\",\\\"visible\\\":true,\\\"locked\\\":false,\\\"width\\\":120},{\\\"level\\\":0,\\\"title\\\":\\\"Tipo Cuenta Bóveda\\\",\\\"field\\\":\\\"TipoCuentaBoveda\\\",\\\"visible\\\":true,\\\"locked\\\":false,\\\"width\\\":120},{\\\"level\\\":0,\\\"title\\\":\\\"N° Cuenta Bóveda\\\",\\\"field\\\":\\\"NumeroCuentaBoveda\\\",\\\"visible\\\":true,\\\"locked\\\":false,\\\"width\\\":120},{\\\"level\\\":0,\\\"title\\\":\\\"Paga\\\",\\\"field\\\":\\\"PagaClienteCorredor\\\",\\\"visible\\\":true,\\\"locked\\\":false,\\\"width\\\":80},{\\\"level\\\":0,\\\"title\\\":\\\"Tipo Reajuste\\\",\\\"field\\\":\\\"IdTipoReajuste\\\",\\\"visible\\\":true,\\\"locked\\\":false,\\\"width\\\":90},{\\\"level\\\":0,\\\"title\\\":\\\"Monto Interés\\\",\\\"field\\\":\\\"INGMontoInteresCorteCupon\\\",\\\"visible\\\":true,\\\"locked\\\":false,\\\"width\\\":80},{\\\"level\\\":0,\\\"title\\\":\\\"Amortización\\\",\\\"field\\\":\\\"Amortizacion\\\",\\\"visible\\\":true,\\\"locked\\\":false,\\\"width\\\":80},{\\\"level\\\":0,\\\"title\\\":\\\"Afecto a Clearing\\\",\\\"field\\\":\\\"AfectoaClearing\\\",\\\"visible\\\":true,\\\"locked\\\":false,\\\"width\\\":60},{\\\"level\\\":0,\\\"title\\\":\\\"Procesadi Clearing\\\",\\\"field\\\":\\\"ProcesadoClearing\\\",\\\"visible\\\":true,\\\"locked\\\":false,\\\"width\\\":60}]}\"}]}";
			
		    Parametros_Entrada ent = gson.fromJson(json, Parametros_Entrada.class);

		    GuardarConfiguracionGrillaPredeterminado demo = new GuardarConfiguracionGrillaPredeterminado();
			System.out.println("ENTRADA");
			System.out.println(ent.toString());
			System.out.println("SALIDA");
			try {
				Parametros_Salida ObjOut = demo.logica(con, ent);
				System.out.println(ObjOut.toString());
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
		    con.commit();

		}

		System.out.println("== END ==");
	}
	
}