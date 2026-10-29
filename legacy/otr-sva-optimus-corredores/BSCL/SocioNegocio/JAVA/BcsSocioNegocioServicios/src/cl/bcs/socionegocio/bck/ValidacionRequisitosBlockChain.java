package cl.bcs.socionegocio.bck;

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
import cl.bcs.socionegocio.soc.CrearSocioNegocio.SelectIdPersona;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ValidacionRequisitosBlockChain implements JavaCall {
	private final String SQL_SELECT_IDPERSONA						= loadResourceToString("cl/bcs/socionegocio/soc/sql/CrearSocioNegocio_SelectIdPersona.sql");
	private final String SQL_SELECT_DIRECCIONELECTRONICA			= loadResourceToString("cl/bcs/socionegocio/bck/sql/ValidacionRequisitosBlockChain_DireccionElectronicaPreYActiva.sql");
	private final String SQL_SELECT_TELEFONOACTIVO					= loadResourceToString("cl/bcs/socionegocio/bck/sql/ValidacionRequisitosBlockChain_TelefonoActivo.sql");
	private final String SQL_SELECT_DIRECCIONFISICA					= loadResourceToString("cl/bcs/socionegocio/bck/sql/ValidacionRequisitosBlockChain_DireccionFisicaParYActiva.sql");

	//-------------------------------------------------
	// CLASES DE ENTRADA 
	//-------------------------------------------------
	public static class ValidacionRequisitosBlockChain_Entrada
	{
		public Long 				p_IdEmpresa;
		public Long					p_IdUsuario; // preguntar si es String o Long
		public String				p_ClaveUsuario;
		public Long					p_IdCliente = 0L; //Id del cliente que se va a consultar
		
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
    //-------------------------------------------------------------------------------------------
	// CLASES UTILITARIAS QUE REPRESENTAN OBJETOS OUT	
	//-------------------------------------------------------------------------------------------
	
	public static class ValidacionRequisitosBlockChain_Salida {

		public List<MensajeSalida>					P_OUT_MENSAJES;
		public Long									P_OUT_ERRORESCONTADOR = 0L;
		public List<Resultado>   	     			P_OUT_RESULTADOPROCESO;
		
		public ValidacionRequisitosBlockChain_Salida()
		{	
			this.P_OUT_MENSAJES 			 = new ArrayList<MensajeSalida>();
			this.P_OUT_RESULTADOPROCESO		 = new ArrayList<Resultado>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}	

	public static class Resultado {
		String res = "";
		
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}

	}
	
	public static class DireccionElectronica {
		Long IdPersona;
		String IdEstado;
		String Predeterminado;
		String CodigoProposito;
		String Descripcion;
		
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}

	}
	public static class TelefonoActivo {
		String	Numero			=	"";
		String	IdEstado		=	"";
		String	IdTipoTelefono	=	"";
	}
	
	public static class DireccionFisica {
		Long	IdPersona		;
		String	IdEstado		;
		String	Predeterminado	;
		String	CodigoProposito	;
		String	Descripcion		;
		
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}

	}
	
	
	//-------------------------------------------------
	// LOGICA DEL SERVICIO 
	//-------------------------------------------------
	public ValidacionRequisitosBlockChain_Salida logica(Connection conn, ValidacionRequisitosBlockChain_Entrada in) throws Exception
	{
		SQL sql = new SQL();
		
		ValidacionRequisitosBlockChain_Salida salida = new ValidacionRequisitosBlockChain_Salida();
		Long IdEmpresa = 0L; 
		Long IdPersona = 0L;
		List<DireccionElectronica> arrDireccionesElectronica = new ArrayList<>();
		List<TelefonoActivo> arrTelefono = new ArrayList<>();
		List<DireccionFisica> arrDireccionesFisica = new ArrayList<>();
		
		//Rescata de los datos de entrada el valor de IdEmpresa
		IdEmpresa = in.p_IdEmpresa;

        //va a buscar el IdPersona con el IdCliente que se recibe desde el objeto "entrada"
		
		if(in.p_IdCliente != 0L) {
			
			HashMap<String,Object> parametroEntrada = new HashMap<String,Object>();
			parametroEntrada.put("p_IdCliente", in.p_IdCliente);
			List<SelectIdPersona> SelectIdPersona = sql.query(conn, SQL_SELECT_IDPERSONA, parametroEntrada, SelectIdPersona.class);
			
			if(SelectIdPersona.size()>0) {
				
				IdPersona = SelectIdPersona.get(0).IdPersona;
				
				//va por los datos de Direccion Electronica
				parametroEntrada = new HashMap<String,Object>();
				parametroEntrada.put("p_IdPersona", IdPersona);
				arrDireccionesElectronica = sql.query(conn, SQL_SELECT_DIRECCIONELECTRONICA, parametroEntrada, DireccionElectronica.class);
				
				//No devolvio Direcciones Electronica
				if(arrDireccionesElectronica.size()==0) {
					//entonces genera mensaje de error alusivo a la falta de direccion electronica particular, predeterminada y activa
		        	GeneraElementosError(conn, IdEmpresa, "UI_NO_EXISTE_DIRECCION_ELECTRONICA_PAR_PRE_ACT", salida.P_OUT_MENSAJES );
		        	++salida.P_OUT_ERRORESCONTADOR;

				}
				
				//va por Telefonos Activos 
				parametroEntrada = new HashMap<String,Object>();
				parametroEntrada.put("p_IdPersona", IdPersona);
				arrTelefono = sql.query(conn, SQL_SELECT_TELEFONOACTIVO, parametroEntrada, TelefonoActivo.class);
				
				//No devolvio Telefonos
				if(arrTelefono.size()==0) {
					//entonces genera mensaje de error alusivo a la falta de Telefono, fijo o movil y activo
		        	GeneraElementosError(conn, IdEmpresa, "UI_NO_EXISTE_TELEFONO_ACT", salida.P_OUT_MENSAJES );
		        	++salida.P_OUT_ERRORESCONTADOR;
					
				}
				
				//va por los datos de Direccion Fisica
				parametroEntrada = new HashMap<String,Object>();
				parametroEntrada.put("p_IdPersona", IdPersona);
				arrDireccionesFisica = sql.query(conn, SQL_SELECT_DIRECCIONFISICA, parametroEntrada, DireccionFisica.class);
				
				//No devolvio Direcciones Fisica
				if(arrDireccionesFisica.size()==0) {
					//entonces genera mensaje de error alusivo a la falta de direccion electronica particular y activa
		        	GeneraElementosError(conn, IdEmpresa, "UI_NO_EXISTE_DIRECCION_FISICA_PAR_ACT", salida.P_OUT_MENSAJES );
		        	++salida.P_OUT_ERRORESCONTADOR;
					
				}
				
				
			}
			else {
				//TODO crear mensaje de error por no encontrar el IdPersona
	        	GeneraElementosError(conn, IdEmpresa, "PERSONA_NO_EXISTE", salida.P_OUT_MENSAJES );
	        	++salida.P_OUT_ERRORESCONTADOR;

				
			}

		}
		else {
			//TODO crear mensaje de error por no se envio IdCliente
        	GeneraElementosError(conn, IdEmpresa, "CLIENTE_INVALIDO", salida.P_OUT_MENSAJES );
        	++salida.P_OUT_ERRORESCONTADOR;
			
		}
		
		Resultado res = new Resultado();

		res.res = salida.P_OUT_ERRORESCONTADOR==0 ? "OK" : "NOOK";

		salida.P_OUT_RESULTADOPROCESO.add(res); 
		
		return salida;
				
	}
	//-------------------------------------------------------------------------------------------
	// OBJETO DE TRANSPORTE - CALL
	//-------------------------------------------------------------------------------------------
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception
	{
		Gson gson = Utils.getGsonBuilder();
		ValidacionRequisitosBlockChain_Entrada in = gson.fromJson(jin.toString(), ValidacionRequisitosBlockChain_Entrada.class);
		ValidacionRequisitosBlockChain_Salida out = logica(conn, in);
		
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
					"  \"p_IdEmpresa\": 1,\r\n" + 
					"  \"p_IdUsuario\": \"6\",\r\n" + 
					"  \"p_IdCliente\": 1421528\r\n" + 
					"}";
			
			ValidacionRequisitosBlockChain_Entrada Objin = gson.fromJson(jsonCL, ValidacionRequisitosBlockChain_Entrada.class);

			ValidacionRequisitosBlockChain demo = new ValidacionRequisitosBlockChain();
			System.out.println("ENTRADA");
			System.out.println(Objin.toString());
			
			try {
				ValidacionRequisitosBlockChain_Salida ObjOut = demo.logica(con, Objin);
				con.rollback();
				System.out.println("SALIDA");
				System.out.println(ObjOut.toString());
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
			con.rollback();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		System.out.println("== FIN PRUEBA LOCAL ==");
	}
	private void GeneraElementosError(Connection con, Long idEmpresa, String codMsg, List<MensajeSalida> P_OUT_MENSAJES) 
			throws Exception {
		
		String msg	=	Diccionario.getMensaje(con,idEmpresa,codMsg)
						.msg();
		
		P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,codMsg,msg));

		
		
	}

}


