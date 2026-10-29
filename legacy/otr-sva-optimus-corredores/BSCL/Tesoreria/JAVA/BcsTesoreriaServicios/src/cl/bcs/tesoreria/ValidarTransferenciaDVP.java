package cl.bcs.tesoreria;

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

import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ValidarTransferenciaDVP implements JavaCall {
	
	private final String SQL_SELECT_VALIDAR_NOMINA_TRANSFERENCIA_DVP = loadResourceToString("cl/bcs/tesoreria/sql/ValidarNominaTransferenciaDVP.sql");
	private final String SQL_SELECT_VALIDAR_TRANSFERENCIA_DVP = loadResourceToString("cl/bcs/tesoreria/sql/ValidarTransferenciaDVP.sql");

    //-------------------------------------------------------------------------------------------
	// SERVICIO No. 700.146
	//-------------------------------------------------------------------------------------------
	
	//-------------------------------------------------------------------------------------------
	// CLASES DE ENTRADA
	//-------------------------------------------------------------------------------------------
	public static class ValidarTransferenciaDVP_Entrada {	
		public Long		p_IdEmpresa;  				 
		public Long		p_IdUsuario; 				   
		public String 	p_IdModulo; 				
		public String 	p_UsaReglasNegocio;			
	    public String 	p_Fecha; 			// Corresponde a Fecha Sistema	
	    
	    // Gestion Nomina o transferencia a Liquidar
	    public List<NominaTransferencia> p_NominaTransferencia;  
	    
		public String toString() {
			return Utils.printJson(this, false);
		}
	}	
	
	public class NominaTransferencia {
		public Long IdNominaTransferencia;
		public String IdMedioDePago;
		public BigDecimal Monto;
	}
		
    //-------------------------------------------------------------------------------------------
	// CLASES DE SALIDA
	//-------------------------------------------------------------------------------------------
	public static class ValidarTransferenciaDVP_Salida 
	{	
		public List<MensajeSalida>				P_OUT_MENSAJES;
		public Long								P_OUT_ERRORESCONTADOR;
		public String 							P_OUT_VALIDO;
		
		public ValidarTransferenciaDVP_Salida() {
			P_OUT_MENSAJES 				= new ArrayList<MensajeSalida>();
			P_OUT_ERRORESCONTADOR       = 0L;
		};
	    
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
    
	public static class TransferenciaDCV {
		public String IdTipoOperacion;
		public String Folio;		
		public BigDecimal MontoNominaTransferencia;
		public BigDecimal MontoCustodia;
		public String ClaveDCV;
		public String ClaveCustodia;
		public String IdEstado;
	}
	
	public static class NominaTransferenciaDCV {
		public String ClaveDCV;
		public String IdTipoOperacion;		
		public String IdEstado;
	}

	public ValidarTransferenciaDVP_Salida logica(Connection conn, ValidarTransferenciaDVP_Entrada in) throws Exception {
		ValidarTransferenciaDVP_Salida salida = new ValidarTransferenciaDVP_Salida();
		salida.P_OUT_VALIDO = "S";
		
		for (NominaTransferencia n: in.p_NominaTransferencia) {		// Registros de NominaTransferencia de entrada
			
			if (n.IdMedioDePago.equals("TDP")) {
				SQL sql = new SQL();
				HashMap<String, Object> paramsN = new HashMap<String, Object>();
				paramsN.put("IdNominaTransferencia", n.IdNominaTransferencia);
				List<NominaTransferenciaDCV> nominaTransferenciaDCV = sql.query(conn, SQL_SELECT_VALIDAR_NOMINA_TRANSFERENCIA_DVP, paramsN, NominaTransferenciaDCV.class);
				
				if (nominaTransferenciaDCV.get(0).IdEstado.equals("ING")) {
					
					if (nominaTransferenciaDCV.get(0).IdTipoOperacion==null || nominaTransferenciaDCV.get(0).IdTipoOperacion.trim().equals("")) {
						salida.P_OUT_VALIDO = "S";	// Se considera que la nomina o transferencia se realizo a partir de un egreso manual, no aplica la validacion
						
					} else {
						if (nominaTransferenciaDCV.get(0).ClaveDCV==null || nominaTransferenciaDCV.get(0).ClaveDCV.trim().equals("")) {
							salida.P_OUT_VALIDO = "N";			// Clave de nomina o transferencia DCV vacia
							salida.P_OUT_ERRORESCONTADOR= 1L;
							String msg= Diccionario.getMensaje(conn, in.p_IdEmpresa, "UI_DEBE_AGREGAR_CLAVE_DCV")
									.msg();					
							salida.P_OUT_MENSAJES.add(new MensajeSalida(1L, MensajeSalida.MENSAJE_ERROR, "UI_DEBE_AGREGAR_CLAVE_DCV", msg));

						} else {
							HashMap<String, Object> params = new HashMap<String, Object>();
							params.put("IdNominaTransferencia", n.IdNominaTransferencia);
							params.put("ClaveDCV", nominaTransferenciaDCV.get(0).ClaveDCV);					
							
							List<TransferenciaDCV> transferenciaDCV = sql.query(conn, SQL_SELECT_VALIDAR_TRANSFERENCIA_DVP, params, TransferenciaDCV.class);
						
							if (transferenciaDCV.isEmpty()) {	// NO coinciden las claves
								salida.P_OUT_VALIDO = "N";
								salida.P_OUT_ERRORESCONTADOR= 1L;
								String msg= Diccionario.getMensaje(conn, in.p_IdEmpresa, "UI_CLAVE_DCV_NO_COINCIDE_CON_CLAVE_TRANSFERENCIA_TITULOS_DEPOSITO_VALORES")
										.msg();
								salida.P_OUT_MENSAJES.add(new MensajeSalida(1L, MensajeSalida.MENSAJE_ERROR, "UI_CLAVE_DCV_NO_COINCIDE_CON_CLAVE_TRANSFERENCIA_TITULOS_DEPOSITO_VALORES", msg));					

							} else {
								salida.P_OUT_VALIDO = "S";		// Coinciden las claves
							}
						}
					}
					
				} else if (nominaTransferenciaDCV.get(0).IdEstado.equals("RCP")) {		// Nomina o transferencia recepcionada
					salida.P_OUT_VALIDO = "N";
					salida.P_OUT_ERRORESCONTADOR= 1L;
					String msg= Diccionario.getMensaje(conn, in.p_IdEmpresa, "NOMINA_TRANSFERENCIA_RECEPCIONADO")
							.msg();					
					salida.P_OUT_MENSAJES.add(new MensajeSalida(1L, MensajeSalida.MENSAJE_ERROR, "NOMINA_TRANSFERENCIA_RECEPCIONADO", msg));
					
				} else if (nominaTransferenciaDCV.get(0).IdEstado.equals("ANU")) {		// Nomina o transferencia anulada
					salida.P_OUT_VALIDO = "N";
					salida.P_OUT_ERRORESCONTADOR= 1L;
					String msg= Diccionario.getMensaje(conn, in.p_IdEmpresa, "NOMINA_TRANSFERENCIA_ANULADO")
							.msg();					
					salida.P_OUT_MENSAJES.add(new MensajeSalida(1L, MensajeSalida.MENSAJE_ERROR, "NOMINA_TRANSFERENCIA_ANULADO", msg));
					break;	
					
				}
				
			} else {	// No es transferencia TDP, no aplica la validacion
				salida.P_OUT_VALIDO = "S";
			}
		}
		return salida;
	}

	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
                					 .serializeNulls().create();
		ValidarTransferenciaDVP_Entrada in = gson.fromJson(jin.toString(), ValidarTransferenciaDVP_Entrada.class);
		ValidarTransferenciaDVP_Salida out = logica(conn, in);
		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	
	public static void main(String[] args) throws SQLException {
		Connection conn = null;
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
                					 .serializeNulls().create();
		try {
			System.out.println("== INICIO ==");
			conn = PlataformaUtils.getQAConnection();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":109,\"p_IdModulo\":\"TES\",\"p_UsaReglasNegocio\":\"S\",\"p_Fecha\":\"2020-01-10\",\"p_NominaTransferencia\":[{\"IdNominaTransferencia\":51,\"IdMedioDePago\":\"TDP\",\"Monto\":333390}]}";
																																
			ValidarTransferenciaDVP_Entrada entrada = gson.fromJson(json, ValidarTransferenciaDVP_Entrada.class);
			ValidarTransferenciaDVP demo = new ValidarTransferenciaDVP();
			ValidarTransferenciaDVP_Salida ObjOut = demo.logica(conn, entrada);
			System.out.println(ObjOut.toString());
			
		} catch (Exception e) {
			e.printStackTrace();
			
		} finally {
			conn.close();
		}
		System.out.println("== FIN ==");
	}
}
