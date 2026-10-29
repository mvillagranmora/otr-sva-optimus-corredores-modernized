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
import cl.bcs.socionegocio.bck.ConsultarEstadoContratoBlockchain.ConsultarEstadoContratoBlockchain_Entrada;
import cl.bcs.socionegocio.bck.ConsultarEstadoContratoBlockchain.ConsultarEstadoContratoBlockchain_Salida;
import cl.bcs.socionegocio.pojo.ConstantesBCK;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class ContratoEsBlockChainFirmado implements JavaCall  {
	private final String SQL_SELECT_ESCONTRATOBLOCKCHAIN				=	loadResourceToString("cl/bcs/socionegocio/bck/sql/ContratoEsBlockChainFirmado_SelectTipoDocumento.sql");
	
	//-------------------------------------------------
	// CLASES DE ENTRADA 
	//-------------------------------------------------
	public static class  ContratoEsBlockChainFirmado_Entrada
	{
		public Long 				p_IdEmpresa				;
		public Long					p_IdUsuario				; 
		public String				p_ClaveUsuario			;
		public ConsultaEstadoCntBC  p_ConsultaEstadoCntBC	; // json para llamar a servicio "ConsultaEstadoContratoBlockchain"
		
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}

	public static class ConsultaEstadoCntBC 
	{
		
		public	String	p_IdTipoDocumento;
		public	String	p_IdCliente;
		public	String	p_BrokerageRUT;
		public	String 	p_IdContrato;
	}
	
    //-------------------------------------------------------------------------------------------
	// CLASES UTILITARIAS QUE REPRESENTAN OBJETOS OUT	
	//-------------------------------------------------------------------------------------------
	
	public static class ContratoEsBlockChainFirmado_Salida {

		public List<MensajeSalida>					P_OUT_MENSAJES;
		public Long									P_OUT_ERRORESCONTADOR = 0L;
		public List<Resultado>   	     			P_OUT_RESULTADOPROCESO ;
		
		public ContratoEsBlockChainFirmado_Salida()
		{	
			this.P_OUT_MENSAJES 			 = new ArrayList<MensajeSalida>();
			this.P_OUT_RESULTADOPROCESO		 = new ArrayList<Resultado>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class TipoDocumento {
		Long IdClase = 0L;
	}
	public static class Resultado {
		public String EsBlockChain = "N";
		public String ContratoFirmado = "N";
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}

	}

	public class Documentoj {
		String IdTipoDocumento;
		String NombreDocumento;
		String Clasificacion;
	}
	public class ClienteBlockChain {
		public String DocumentoIdentidad;
	}
	//-------------------------------------------------
	// LOGICA DEL SERVICIO 
	//-------------------------------------------------

	public ContratoEsBlockChainFirmado_Salida logica(Connection conn, ContratoEsBlockChainFirmado_Entrada in) throws Exception {
		String IdContrato;
		Long IdEmpresa;
		Resultado objResultado = new Resultado();
		
		SQL sql = new SQL();
		ContratoEsBlockChainFirmado_Salida salida = new ContratoEsBlockChainFirmado_Salida();

		//Rescata de los datos de entrada el valor de IdEmpresa y otros
		IdEmpresa = in.p_IdEmpresa;
		IdContrato = in.p_ConsultaEstadoCntBC.p_IdContrato.equals("") || in.p_ConsultaEstadoCntBC.p_IdContrato == null ? ConstantesBCK.CONTRATOPRESTAMOANEXO42 : in.p_ConsultaEstadoCntBC.p_IdContrato;
	
		HashMap<String,Object> parametroEntrada = new HashMap<String,Object>();
		parametroEntrada.put("p_IdTipoDocumento", IdContrato);
		List<TipoDocumento> arrTipoDocumento = sql.query(conn, SQL_SELECT_ESCONTRATOBLOCKCHAIN, parametroEntrada, TipoDocumento.class);

		//si arrTipoDocumento tiene valores, entonces es que el contrato es de TipoDocumento BlockChain
		
		if(arrTipoDocumento.size()>0) {
			
			objResultado.EsBlockChain = "S";	
			
			//entonces va a consumir servicio que consulta estado de contrato BlockChain
			ConsultarEstadoContratoBlockchain			objConsultaFirma = new ConsultarEstadoContratoBlockchain();
			ConsultarEstadoContratoBlockchain_Entrada	objFirma_entrada = new ConsultarEstadoContratoBlockchain_Entrada();
			ConsultarEstadoContratoBlockchain_Salida	objFirma_salida;
			
			objFirma_entrada.p_IdTipoDocumento		=	in.p_ConsultaEstadoCntBC.p_IdContrato; // antes el servicio aceptaba tipos de contrato BlockChain, ahora son tipos de Contrato Optimus pero no se cambio el nombre del paramteo, in.p_ConsultaEstadoCntBC.p_TypeMSA;
			objFirma_entrada.p_IdCliente			=	in.p_ConsultaEstadoCntBC.p_IdCliente;
			objFirma_entrada.p_IdEmpresa	        =   in.p_IdEmpresa;
			objFirma_entrada.p_IdModulo	            =   "CLI";
			objFirma_entrada.p_IdUsuario	        =   in.p_IdUsuario;
			
			//ejecuta servicio Consulta estado contrato BlockChain del cliente
			objFirma_salida = objConsultaFirma.logica(conn, objFirma_entrada);
			
			// crea objeto cliente blockchain
			ClienteBlockChain clienteBlockChain = new ClienteBlockChain();
			clienteBlockChain.DocumentoIdentidad = objFirma_salida.DocumentoIdentidad;
			
			//no hay errores
			if (objFirma_salida.p_Out_ContadorError==0L) 
			{
				if(objFirma_salida.p_Out_Status.equals(ConstantesBCK.ESTADO_APROBADO_CONTRATO_BLOCKCHAIN)) {
					objResultado.ContratoFirmado="S";
				}
				else 
				{	 
					if(objFirma_salida.p_Out_Status.equals(ConstantesBCK.ESTADO_RECHAZADO_CONTRATO_BLOCKCHAIN)) {
						GeneraElementosError(conn, IdEmpresa, "UI_ERROR_CONTRATO_BLOCKCHAIN_RECHAZADO", salida.P_OUT_MENSAJES, clienteBlockChain);
					}else if(objFirma_salida.p_Out_Status.equals(ConstantesBCK.ESTADO_PENDIENTE_CONTRATO_BLOCKCHAIN)) {
						GeneraElementosError(conn, IdEmpresa, "UI_ERROR_CONTRATO_BLOCKCHAIN_PENDIENTE", salida.P_OUT_MENSAJES, clienteBlockChain);
					}else if(objFirma_salida.p_Out_Status.equals(ConstantesBCK.ESTADO_SIN_CONTRATO_BLOCKCHAIN)) {
						GeneraElementosError(conn, IdEmpresa, "UI_ERROR_SIN_CONTRATO_BLOCKCHAIN", salida.P_OUT_MENSAJES, clienteBlockChain);
					}else{
						GeneraElementosError(conn, IdEmpresa, "UI_ERROR_EN_SERVICIO_ESTADO_CONTRATO_BLOCKCHAIN", salida.P_OUT_MENSAJES, clienteBlockChain);
					}
				 ++salida.P_OUT_ERRORESCONTADOR;
				 
				}
				
			}
			else {
				//si hay errores los retorna en una pila de errores
				
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"",objFirma_salida.p_Out_MensajeError));
				++salida.P_OUT_ERRORESCONTADOR;
				
			}
			
		} 
		else 
		{
			objResultado.EsBlockChain = "N";	
		}
		
		salida.P_OUT_RESULTADOPROCESO.add(objResultado);

		return salida;
		
	}
	//-------------------------------------------------------------------------------------------
	// OBJETO DE TRANSPORTE - CALL
	//-------------------------------------------------------------------------------------------
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception
	{
		Gson gson = Utils.getGsonBuilder();
		ContratoEsBlockChainFirmado_Entrada in = gson.fromJson(jin.toString(), ContratoEsBlockChainFirmado_Entrada.class);
		ContratoEsBlockChainFirmado_Salida out = logica(conn, in);
		
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
					" \"p_IdEmpresa\":1,\r\n" + 
					" \"p_IdUsuario\":54,\r\n" + 
					" \"p_ClaveUsuario\": \"\",\r\n" + 
					" \"p_ConsultaEstadoCntBC\": \r\n" + 
					" {\r\n" + 
					"\"p_TypeMSA\": \"48\",\r\n" + 
					"\"p_IdCliente\": 524891,\r\n" + 
					"\"p_BrokerageRUT\": \"80993900-6\",\r\n" + 
					"\"p_IdContrato\": \"CONTRATOPRESTAMOANEXO48\"\r\n" + 	
					" }\r\n" + 
					" }";

			ContratoEsBlockChainFirmado_Entrada Objin = gson.fromJson(jsonCL, ContratoEsBlockChainFirmado_Entrada.class);

			ContratoEsBlockChainFirmado demo = new ContratoEsBlockChainFirmado();
			System.out.println("ENTRADA");
			System.out.println(Objin.toString());
			
			try {
				ContratoEsBlockChainFirmado_Salida ObjOut = demo.logica(con, Objin);
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
	private void GeneraElementosError(Connection con, Long idEmpresa, String codMsg, List<MensajeSalida> P_OUT_MENSAJES, ClienteBlockChain clienteBlockChain) 
			throws Exception {
		
		String msg	=	Diccionario.getMensaje(con,idEmpresa,codMsg)
				.add("DocumentoIdentidad", clienteBlockChain.DocumentoIdentidad)
				.msg();
		
		P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,codMsg,msg));
		
	}

}
