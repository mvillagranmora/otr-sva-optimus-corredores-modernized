package cl.bcs.portafolio;

import java.sql.Connection;
import java.sql.SQLException;
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

public class ModificaPreferenciaPagoPortafolio implements JavaCall {

	
	//Clases utilitarias
	//entrada
	public static class ModificaPreferenciaPagoPortafolio_Entrada {
		public Long									p_IdEmpresa											;
		public Long									p_IdUsuario											; 
        public String								p_IdModulo						=	"SOC"			;
        public String								p_UsaReglasNegocio				=	"N"				;
        public List<PreferenciaPagoPortafolioMod>	p_PreferenciaPagoPortafolioMod	= new ArrayList<>()	;
        
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}

	}
	
	public static class PreferenciaPagoPortafolioMod {
		public Long IdPreferenciaPagoPortafolio;
		public List<RegistroCambio> RegistrosCambio = new ArrayList<>();
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}

	}
	
	public static class RegistroCambio {
		public String CampoModificado;
		public String ValorNuevo;
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	//Salida
	public static class ModificaPreferenciaPagoPortafolio_Salida { 
		public List<MensajeSalida>					P_OUT_MENSAJES;
		public Long									P_OUT_ERRORESCONTADOR = 0L;
		public List<Resultado>   	     			P_OUT_RESULTADOPROCESO ;
		public Long									P_OUT_ADVERTENCIASCONTADOR = 0L;
		public List<ObjADVERTENCIASDETALLE>			P_OUT_ADVERTENCIASDETALLE;
		public List<ObjERRORESDETALLE>				P_OUT_ERRORESDETALLE;

		public ModificaPreferenciaPagoPortafolio_Salida()
		{	
			this.P_OUT_MENSAJES 			 = new ArrayList<MensajeSalida>();
			this.P_OUT_RESULTADOPROCESO		 = new ArrayList<Resultado>();
			this.P_OUT_ADVERTENCIASDETALLE   = new ArrayList<ObjADVERTENCIASDETALLE>();
			this.P_OUT_ERRORESDETALLE        = new ArrayList<ObjERRORESDETALLE>();
		}
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	
	}
	public static class Resultado {
		public Long IdPreferenciaPagoPortafolio;
		public long RefId = 0L;
		
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}

	}
	public static class ObjADVERTENCIASDETALLE {
		public Long IdPreferenciaPagoPortafolio;
		public String	Codigo;
		public String	RequiereAutorizacion;
		public String	Descripcion;
	}

	public static class ObjERRORESDETALLE {
		public Long IdPreferenciaPagoPortafolio;
		public String	Codigo;
		public String	Descripcion;
	}

	
	//-------------------------------------------------
	// LOGICA DEL SERVICIO 
	//-------------------------------------------------
	public ModificaPreferenciaPagoPortafolio_Salida logica(Connection conn, ModificaPreferenciaPagoPortafolio_Entrada in) throws Exception {
		//String IdContrato;
		//Long IdEmpresa;
		//Resultado objResultado = new Resultado();
		ModificaPreferenciaPagoPortafolio_Salida out = new ModificaPreferenciaPagoPortafolio_Salida();
		SQL sql = new SQL();
		if(in.p_PreferenciaPagoPortafolioMod.size()>0) {

			try {
					
				// ------------------------------
				// Crea las tablas temporales
	
				SQL.exec(conn,
						"CREATE LOCAL TEMPORARY TABLE #ModificaPPPMasivo_P_REGISTROCAMBIO (\"CampoModificado\" NVARCHAR(255),\"ValorNuevo\" NVARCHAR(255))"); 
				
				for(int i=0;i<in.p_PreferenciaPagoPortafolioMod.size();i++) {
		
					List<RegistroCambio> objLstRegistroCambio = in.p_PreferenciaPagoPortafolioMod.get(i).RegistrosCambio;
					int[] rows = sql.batch(conn,
							"INSERT INTO #ModificaPPPMasivo_P_REGISTROCAMBIO (\"CampoModificado\",\"ValorNuevo\") VALUES (:CampoModificado,:ValorNuevo)",
							objLstRegistroCambio, RegistroCambio.class);
				
/* rescata el valor que se cargo en la tabla temporal **solo para pruebas
					List<RegistroCambio> objLstRegistroCambioOut = new ArrayList<>();
					HashMap<String,Object> parametroEntrada = new HashMap<String,Object>();
					parametroEntrada.put("p_valor", 0);
					objLstRegistroCambioOut = sql.query(conn,
							"SELECT \"CampoModificado\",\"ValorNuevo\" FROM #ModificaPPPMasivo_P_REGISTROCAMBIO WHERE :p_valor = 0",
							parametroEntrada, RegistroCambio.class);
*/
					
					// ------------------------------
					// Ejecuta el SP
					HashMap<String, Object> parmsIN = new HashMap<String, Object>();
		
					parmsIN.put("P_IDEMPRESA", in.p_IdEmpresa);
					parmsIN.put("P_IDUSUARIO", in.p_IdUsuario);
					parmsIN.put("P_IDMODULO", in.p_IdModulo);
					parmsIN.put("P_USAREGLASNEGOCIO", in.p_UsaReglasNegocio);
					parmsIN.put("P_IDPREFERENCIAPAGOPORTAFOLIO", in.p_PreferenciaPagoPortafolioMod.get(i).IdPreferenciaPagoPortafolio);
					parmsIN.put("P_REGISTROCAMBIO", objLstRegistroCambio);
		
					HashMap<String, Class> parmsOUT = new HashMap<String, Class>();
		
					parmsOUT.put("P_OUT_REFID", Long.class);
					parmsOUT.put("P_OUT_ADVERTENCIASCONTADOR", Long.class);
					parmsOUT.put("P_OUT_ADVERTENCIASDETALLE", ObjADVERTENCIASDETALLE.class);
					parmsOUT.put("P_OUT_ERRORESCONTADOR", Long.class);
					parmsOUT.put("P_OUT_ERRORESDETALLE", ObjERRORESDETALLE.class); 
		
					String queryCall = "call \"BSCL.Portafolio.BD.Servicios::ModificarPreferenciaPago\" (:P_IDEMPRESA,:P_IDUSUARIO,:P_IDMODULO,:P_USAREGLASNEGOCIO,:P_IDPREFERENCIAPAGOPORTAFOLIO,#ModificaPPPMasivo_P_REGISTROCAMBIO, @P_OUT_REFID,@P_OUT_ADVERTENCIASCONTADOR,$P_OUT_ADVERTENCIASDETALLE,@P_OUT_ERRORESCONTADOR,$P_OUT_ERRORESDETALLE)";
					HashMap<String, Object> rpt = sql.call(conn, queryCall, parmsIN, parmsOUT);
		
					// ------------------------------
					// Lee scalar
		
					out.P_OUT_ADVERTENCIASCONTADOR += (Long) rpt.get("P_OUT_ADVERTENCIASCONTADOR");
					out.P_OUT_ERRORESCONTADOR += (Long) rpt.get("P_OUT_ERRORESCONTADOR");
		
					// ------------------------------
		
					Resultado objResultado = new Resultado();
					objResultado.RefId = (Long) rpt.get("P_OUT_REFID");
					objResultado.IdPreferenciaPagoPortafolio = in.p_PreferenciaPagoPortafolioMod.get(i).IdPreferenciaPagoPortafolio;
					out.P_OUT_RESULTADOPROCESO.add(objResultado);
					List<ObjADVERTENCIASDETALLE> lstDetalleAdvPaso = (List<ObjADVERTENCIASDETALLE>) rpt.get("P_OUT_ADVERTENCIASDETALLE");
					List<ObjERRORESDETALLE> lstDetalleErrPaso = (List<ObjERRORESDETALLE>) rpt.get("P_OUT_ERRORESDETALLE");
					
					//agrego las advertencias a las advertencias de salida
					for(int ii=0;ii<lstDetalleAdvPaso.size();ii++) {
						//asigno la advertencia al registro de preferencia de pago al que pertenece
						lstDetalleAdvPaso.get(ii).IdPreferenciaPagoPortafolio = in.p_PreferenciaPagoPortafolioMod.get(i).IdPreferenciaPagoPortafolio;
						out.P_OUT_ADVERTENCIASDETALLE.add(lstDetalleAdvPaso.get(ii));
					};
					
					//agrego los errores a los errores de salida
					for(int ii=0;ii<lstDetalleErrPaso.size();ii++) {
						//asigno el error al registro de preferencia de pago al que pertenece
						lstDetalleErrPaso.get(ii).IdPreferenciaPagoPortafolio = in.p_PreferenciaPagoPortafolioMod.get(i).IdPreferenciaPagoPortafolio;
						out.P_OUT_ERRORESDETALLE.add(lstDetalleErrPaso.get(ii));
					};
					
					//out.P_OUT_ADVERTENCIASDETALLE = (List<ObjADVERTENCIASDETALLE>) rpt.get("P_OUT_ADVERTENCIASDETALLE");
					//out.P_OUT_ERRORESDETALLE = (List<ObjERRORESDETALLE>) rpt.get("P_OUT_ERRORESDETALLE");
					
					//limpia los valores de registro de preferencia de portafolio que se cambiaron
					try {
						SQL.exec(conn, "DELETE FROM #ModificaPPPMasivo_P_REGISTROCAMBIO");
					} catch (Exception er) {
					}

				}	
			}
			catch (Exception er) {
				er.printStackTrace();
				System.out.println(er.toString());
			}
			finally {
				// Elimina tablas temporales locales
	
				try {
					SQL.exec(conn, "DROP TABLE #ModificaPPPMasivo_P_REGISTROCAMBIO");
				} catch (Exception er) {
				}
	
			}
		}		
		
		return out; 	
	}
	//-------------------------------------------------------------------------------------------
	// OBJETO DE TRANSPORTE - CALL
	//-------------------------------------------------------------------------------------------
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception
	{
		Gson gson = Utils.getGsonBuilder();
		ModificaPreferenciaPagoPortafolio_Entrada in = gson.fromJson(jin.toString(), ModificaPreferenciaPagoPortafolio_Entrada.class);
		ModificaPreferenciaPagoPortafolio_Salida out = logica(conn, in);
		
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	//-------------------------------------------------
	// METODO PARA PRUEBAS LOCALES 
	//-------------------------------------------------
	public static void main(String[] args)
	{
		System.out.println("== INICIO PRUEBA LOCAL ==");
		
		try (Connection con = PlataformaUtils.getDESAConnection())
		{
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			
			//String jsonCL = "{\"p_ConsultarEnrolamiento\":[{\"IdEnrolamiento\":0,\"IdEmpresa\":1,\"IdUsuario\":3,\"IdTipoEntidad\":\"CLIENTE\",\"IdEntidadEnrolada\":0,\"IdTipoDocumentoIdentidad\":\"R\",\"IdTipoDocumentoIdentidadAsociado\":\"R\",\"DocumentoIdentidad\":\"68370463-7\",\"IdTipoDocumentoIdentidadAdicional\":\"R\",\"DocumentoIdentidadAdicional\":\"\",\"Tipo\":\"N\",\"Nombres\":\"TEST\",\"ApellidoPaterno\":\"ALTA\",\"ApellidoMaterno\":\"CLIENTE\",\"RazonSocial\":\"\",\"IdPaisNacimiento\":1,\"FechaNacimiento\":\"2010-01-01T03:00:00.000Z\",\"Sexo\":\"M\",\"EstadoCivil\":\"SO\",\"IdCiudad\":173,\"IdDivisionPais\":343,\"IdTipoTelefono\":\"FIJO\",\"Telefono\":\"+56225485952\",\"Email\":\"CHERNANDEZ@BOLSADESANTIAGO.COM\",\"DireccionCalle\":\"ASD\",\"DireccionNumero\":\"123\",\"DireccionDepartamento\":\"\",\"FechaCreacion\":\"2018-11-06T17:21:10.190Z\",\"FechaModificacion\":\"2018-11-06T17:21:10.190Z\",\"IdPersonaConyuge\":0,\"RegimenConyugal\":\"\",\"CLI_IdCategoriaInversionista\":\"ARRIESGADO\",\"CLI_IdTipoCliente\":\"FAMILYOFFICE\",\"CLI_IdClaseCliente\":\"\",\"CLI_IdEmpleadoEjecutivo\":7298,\"CLI_CodigoAdicionalHolding\":\"\",\"CLI_ClienteFinal\":\"N\",\"POR_IdPerfilComercial\":\"PERFIL ARRIESGADO BCS\",\"POR_IdPersonaBrokerExtranjero\":0,\"EMP_CodigoEmpleado\":\"\",\"EMP_CodigoTipoCargoEmpleado\":\"\",\"EMP_IdEmpleadoSupervisor\":0,\"EMP_IdSucursal\":0,\"EMP_FechaInicioCargo\":\"0001-01-01\",\"EMP_FechaTerminoCargo\":\"0001-01-01\",\"CNT_CodigoInscripcion\":\"\",\"CNT_Emisor\":\"\",\"CNT_Depositos\":\"\",\"CNT_Bolsa\":\"\",\"CNT_Corredora\":\"\",\"CNT_CamaraDeCompensacion\":\"\",\"CNT_Banco\":\"\",\"CNT_Boveda\":\"\",\"CNT_Otros\":\"\",\"PRO_IdAreaProveedor\":0,\"EMP_CargoIndefinido\":\"\",\"EsRutInterno\":\"N\"}],\"p_IdEmpresa\":1,\"p_IdUsuario\":3,\"p_IdModulo\":\"SOC\"}";
			
			String jsonCL = "{\r\n" + 
					"	\"p_IdEmpresa\": 1,\r\n" + 
					"	\"p_IdUsuario\": 6,\r\n" + 
					"	\"p_IdModulo\": \"SOC\",\r\n" + 
					"	\"p_UsaReglasNegocio\": \"N\",\r\n" + 
					"	\"p_PreferenciaPagoPortafolioMod\": [\r\n" + 
					"	{\r\n" + 
					"		\"IdPreferenciaPagoPortafolio\": 23570,\r\n" + 
					"		\"RegistrosCambio\": [{\r\n" + 
					"			\"CampoModificado\": \"IdEstado\",\r\n" + 
					"			\"ValorNuevo\": \"ACT\"\r\n" + 
					"		},\r\n" + 
					"		{\r\n" + 
					"			\"CampoModificado\": \"IdCuentaBancaria\",\r\n" + 
					"			\"ValorNuevo\": \"271\"\r\n" + 
					"		}]\r\n" + 
					"	},\r\n" + 
					"	{\r\n" + 
					"		\"IdPreferenciaPagoPortafolio\": 23706,\r\n" + 
					"		\"RegistrosCambio\": [{\r\n" + 
					"			\"CampoModificado\": \"IdEstado\",\r\n" + 
					"			\"ValorNuevo\": \"ACT\"\r\n" + 
					"		},\r\n" + 
					"		{\r\n" + 
					"			\"CampoModificado\": \"IdCuentaBancaria\",\r\n" + 
					"			\"ValorNuevo\": \"271\"\r\n" + "}," + 
					"		{\r\n" + 
					"			\"CampoModificado\": \"PorDefecto\",\r\n" + 
					"			\"ValorNuevo\": \"S\"\r\n" + 
					"		}]\r\n" + 
					"	}\r\n" + 
					"	]\r\n" + 
					"}"; 
					
			
			ModificaPreferenciaPagoPortafolio_Entrada Objin = gson.fromJson(jsonCL, ModificaPreferenciaPagoPortafolio_Entrada.class);

			ModificaPreferenciaPagoPortafolio demo = new ModificaPreferenciaPagoPortafolio();
			System.out.println("ENTRADA");
			System.out.println(Objin.toString());
			
			try {
				ModificaPreferenciaPagoPortafolio_Salida ObjOut = demo.logica(con, Objin);
				//con.rollback();
				con.commit();
				System.out.println("SALIDA");
				System.out.println(ObjOut.toString());
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
			//con.rollback();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		System.out.println("== FIN PRUEBA LOCAL ==");
	}
/*	private void GeneraElementosError(Connection con, Long idEmpresa, String codMsg, List<MensajeSalida> P_OUT_MENSAJES) 
			throws Exception {
		
		String msg	=	Diccionario.getMensaje(con,idEmpresa,codMsg)
						.msg();
		
		P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,codMsg,msg));
		
	}
*/
}
