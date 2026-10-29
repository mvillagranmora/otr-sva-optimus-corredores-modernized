package cl.bcs.portafolio;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class IngresaPreferenciaPagoPortafolio implements JavaCall {
	static final Logger logger = LogManager.getLogger(IngresaPreferenciaPagoPortafolio.class);
	//Clases utilitarias
	//entrada
	public static class IngresaPreferenciaPagoPortafolio_Entrada {
		public Long									p_IdEmpresa												;
		public Long									p_IdUsuario												; 
        public String								p_IdModulo						=	"SOC"				;
        public String								p_UsaReglasNegocio				=	"N"					;
        public List<PreferenciaPagoPortafolio> 		p_PreferenciaPagoPortafolioIng	= new ArrayList<>()		;
        
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}

	}
	public static class PreferenciaPagoPortafolio {
        public Long		 IdPortafolio		= 0L	;
        public String	 IdPreferenciaPago	= ""	;;
        public Long		 IdCuentaBancaria	= 0L	;
        public Long		 IdTarjetaBancaria	= 0L	;
        public String	 IdEstado			= "ACT"	;
        public String	 PorDefecto			= "N"	;
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}

	}

	//Salida
	public static class IngresaPreferenciaPagoPortafolio_Salida { 
		public List<MensajeSalida>					P_OUT_MENSAJES;
		public Long									P_OUT_ERRORESCONTADOR = 0L;
		public List<Resultado>   	     			P_OUT_RESULTADOPROCESO ;
		public Long									P_OUT_ADVERTENCIASCONTADOR = 0L;
		public List<ObjADVERTENCIASDETALLE>			P_OUT_ADVERTENCIASDETALLE;
		public List<ObjERRORESDETALLE>				P_OUT_ERRORESDETALLE;

		public IngresaPreferenciaPagoPortafolio_Salida()
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
	public IngresaPreferenciaPagoPortafolio_Salida logica(Connection conn, IngresaPreferenciaPagoPortafolio_Entrada in) throws Exception {
		//String IdContrato;
		//Long IdEmpresa;
		//Resultado objResultado = new Resultado();
		logger.info("INICIO::IngresaPreferenciaPagoPortafolio");
		IngresaPreferenciaPagoPortafolio_Salida out = new IngresaPreferenciaPagoPortafolio_Salida();
		SQL sql = new SQL();
		if(in.p_PreferenciaPagoPortafolioIng.size()>0) {

			try { 
					
				for( PreferenciaPagoPortafolio item : in.p_PreferenciaPagoPortafolioIng ) {

					// ------------------------------
					// Ejecuta el SP
					HashMap<String, Object> parmsIN = new HashMap<String, Object>();
		
					parmsIN.put("P_IDEMPRESA", in.p_IdEmpresa);
					parmsIN.put("P_IDUSUARIO", in.p_IdUsuario);
					parmsIN.put("P_IDMODULO", in.p_IdModulo);
					parmsIN.put("P_USAREGLASNEGOCIO", in.p_UsaReglasNegocio);
					parmsIN.put("P_IDPORTAFOLIO", item.IdPortafolio);
					parmsIN.put("P_IDPREFERENCIAPAGO", item.IdPreferenciaPago);
					parmsIN.put("P_IDCUENTABANCARIA", item.IdCuentaBancaria);
					parmsIN.put("P_IDTARJETABANCARIA", item.IdTarjetaBancaria);
					parmsIN.put("P_IDESTADO", item.IdEstado);
					parmsIN.put("P_PORDEFECTO", item.PorDefecto);
					
		
					HashMap<String, Class> parmsOUT = new HashMap<String, Class>();
		
					parmsOUT.put("P_OUT_REFID", Long.class);
					parmsOUT.put("P_OUT_ADVERTENCIASCONTADOR", Long.class);
					parmsOUT.put("P_OUT_ADVERTENCIASDETALLE", ObjADVERTENCIASDETALLE.class);
					parmsOUT.put("P_OUT_ERRORESCONTADOR", Long.class);
					parmsOUT.put("P_OUT_ERRORESDETALLE", ObjERRORESDETALLE.class); 
		
					String queryCall = "call \"BSCL.Portafolio.BD.Servicios::CrearPreferenciaPago\" (:P_IDEMPRESA,:P_IDUSUARIO,:P_IDMODULO,:P_USAREGLASNEGOCIO,:P_IDPORTAFOLIO,:P_IDPREFERENCIAPAGO,:P_IDCUENTABANCARIA,:P_IDTARJETABANCARIA,:P_IDESTADO,:P_PORDEFECTO,@P_OUT_REFID,@P_OUT_ADVERTENCIASCONTADOR,$P_OUT_ADVERTENCIASDETALLE,@P_OUT_ERRORESCONTADOR,$P_OUT_ERRORESDETALLE)";
					HashMap<String, Object> rpt = sql.call(conn, queryCall, parmsIN, parmsOUT);
		
					// ------------------------------
					// Lee scalar
		
					out.P_OUT_ADVERTENCIASCONTADOR += (Long) rpt.get("P_OUT_ADVERTENCIASCONTADOR");
					out.P_OUT_ERRORESCONTADOR += (Long) rpt.get("P_OUT_ERRORESCONTADOR");
		
					// ------------------------------
		
					Resultado objResultado = new Resultado();
					objResultado.RefId = (Long) rpt.get("P_OUT_REFID");
					//objResultado.IdPreferenciaPagoPortafolio = in.p_PreferenciaPagoPortafolioMod.get(i).IdPreferenciaPagoPortafolio;
					out.P_OUT_RESULTADOPROCESO.add(objResultado);
					List<ObjADVERTENCIASDETALLE> lstDetalleAdvPaso = (List<ObjADVERTENCIASDETALLE>) rpt.get("P_OUT_ADVERTENCIASDETALLE");
					List<ObjERRORESDETALLE> lstDetalleErrPaso = (List<ObjERRORESDETALLE>) rpt.get("P_OUT_ERRORESDETALLE");
					
					//agrego las advertencias a las advertencias de salida
					for(int ii=0;ii<lstDetalleAdvPaso.size();ii++) {
						//asigno la advertencia al registro de preferencia de pago al que pertenece
						//lstDetalleAdvPaso.get(ii).IdPreferenciaPagoPortafolio = in.p_PreferenciaPagoPortafolioMod.get(i).IdPreferenciaPagoPortafolio;
						out.P_OUT_ADVERTENCIASDETALLE.add(lstDetalleAdvPaso.get(ii));
					};
					
					//agrego los errores a los errores de salida
					for(int ii=0;ii<lstDetalleErrPaso.size();ii++) {
						//asigno el error al registro de preferencia de pago al que pertenece
						//lstDetalleErrPaso.get(ii).IdPreferenciaPagoPortafolio = in.p_PreferenciaPagoPortafolioMod.get(i).IdPreferenciaPagoPortafolio;
						out.P_OUT_ERRORESDETALLE.add(lstDetalleErrPaso.get(ii));
					};

				}
				
				
				
				
			}
			catch (Exception er) {
				er.printStackTrace();
				++out.P_OUT_ERRORESCONTADOR;
				ObjERRORESDETALLE objError = new ObjERRORESDETALLE();
				objError.Codigo ="ERROR";
				objError.Descripcion = er.toString();
				out.P_OUT_ERRORESDETALLE.add(objError);
				System.out.println(er.toString());
			}
		}
		logger.info("FIN::IngresaPreferenciaPagoPortafolio");
		return out;
	}
	
	
	//-------------------------------------------------------------------------------------------
	// OBJETO DE TRANSPORTE - CALL
	//-------------------------------------------------------------------------------------------
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception
	{
		Gson gson = Utils.getGsonBuilder();
		IngresaPreferenciaPagoPortafolio_Entrada in = gson.fromJson(jin.toString(), IngresaPreferenciaPagoPortafolio_Entrada.class);
		IngresaPreferenciaPagoPortafolio_Salida out = logica(conn, in);
		
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
/*			
			String jsonCL = "{\r\n" + 
					"	\"p_IdEmpresa\": 1,\r\n" + 
					"	\"p_IdUsuario\": 6,\r\n" + 
					"	\"p_IdModulo\": \"SOC\",\r\n" + 
					"	\"p_UsaReglasNegocio\": \"N\",\r\n" + 
					"	\"p_PreferenciaPagoPortafolioIng\": [\r\n" + 
					"	{\r\n" + 
					"		\"IdPortafolio\" : 1451574,\r\n" + 
					"		\"IdPreferenciaPago\" : \"VENCFM_TRA_CLP\",\r\n" + 
					"		\"IdCuentaBancaria\" : 270,\r\n" + 
					"		\"IdTarjetaBancaria\" : 0,\r\n" + 
					"		\"IdEstado\" : \"ACT\",\r\n" + 
					"		\"PorDefecto\" : \"N\"\r\n" + 
					"	}\r\n" + 
					"	]\r\n" + 
					"}"; 
					
*/
			String jsonCL = "{\"p_IdEmpresa\":1,\"p_IdModulo\":\"SOC\",\"p_IdUsuario\":6,\"p_UsaReglasNegocio\":\"N\",\"p_PreferenciaPagoPortafolioIng\":[{\"IdPortafolio\":1451574,\"IdPreferenciaPago\":\"VENCFM_TRA_CLP\",\"IdCuentaBancaria\":270,\"IdEstado\":\"ACT\",\"PorDefecto\":\"N\"}]}";
			
			
			
			
			IngresaPreferenciaPagoPortafolio_Entrada Objin = gson.fromJson(jsonCL, IngresaPreferenciaPagoPortafolio_Entrada.class);

			IngresaPreferenciaPagoPortafolio demo = new IngresaPreferenciaPagoPortafolio();
			System.out.println("ENTRADA");
			System.out.println(Objin.toString());
			
			try {
				IngresaPreferenciaPagoPortafolio_Salida ObjOut = demo.logica(con, Objin);
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
	
}
