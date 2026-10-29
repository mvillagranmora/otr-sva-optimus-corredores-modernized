package cl.bcs.socionegocio.eje;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.dbcp2.BasicDataSource;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class RecarterizacionClientes implements JavaCall {	
	private final Log log = new Log("EJE_RecarterizacionClientes");
	//Mensajes de validaciÃ³n salida
	private final String Validacion_ClienteBloqueado="Cliente bloqueado";
	private final String Validacion_PortafolioBloqueado="Portafolio bloqueado";
	private final String Validacion_clienteActualizado="Cliente actualizado";
	private final String Validacion_clienteSinActualizar="Cliente sin actualizar";
	private final String Validacion_SinDiferencias="No hay diferencias para actualizar";
	private final String Error_ClienteNoExiste="El cliente no existe";
	private final String Error_PortafolioNoExiste="El portafolio no existe";
	private final String Error_EjecutivoClienteNoExiste="El ejecutivo configurado para el cliente no existe";
	private final String Error_EjecutivoPortafolioNoExiste="El ejecutivo configurado para el(los) portafolios no existe";
	private final String Error_EjecutivoSinSucursalDefinida="El ejecutivo no tiene sucursal definida";
	private final String Error_MismaSucursal="Nueva sucursal es la misma que la sucursal anterior";
	private final String Error_SucursalNoExiste="Sucursal no existe";
	
	//QUERIES
	private final String SQL_SELECT_EJECUTIVOS = "select \"IdEmpresa\",\"IdEmpleadoEjecutivo\",\"IdPersonaEjecutivo\",\"NombreCompleto\",\"CodigoEmpleado\","
			+ "\"IdSucursal\",\"NombreSucursal\",\"CodigoSucursal\",\"IdEstado\",\"NombreEstado\",\"IdEmpleadoSupervisor\",\"NombreSupervisor\","
			+ "\"IdEmpleadoEjecutivoReemplaza\",\"NombreCompletoReemplaza\",\"FechaInicioReemplazo\",\"FechaFinReemplazo\" "
			+ "from \"BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo\" where \"CodigoEmpleado\" in (:ListaCodigoEmpleado) and \"IdEmpresa\"=:IdEmpresa";
	private final String SQL_SELECT_SUCURSALES = "select \"IdSucursal\",\"IdEmpresa\",\"CodigoSucursal\",\"NombreSucursal\",\"EstadoSucursal\",\"IdDivisionPais\",\"Calle\",\"Numero\",\"Depto\",\"Telefono\" "
			+ "from \"BSCL.Plataforma.BD::PLA.Sucursal\" where \"IdEmpresa\"=:IdEmpresa";
	private final String SQL_SELECT_CLIENTES = "select \"IdDocumentoIdentidad\",\"Tipo\",\"NombreCompleto\",\"Nombres\",\"ApellidoPaterno\",\"ApellidoMaterno\","
			+ "\"RazonSocial\",\"IdGiroPrincipal\",\"IdPaisNacimiento\",\"IdPaisResidencia\",\"IdPaisNacionalidadPrincipal\",\"FechaNacimiento\",\"FechaDefuncion\","
			+ "\"Sexo\",\"EsExtranjero\",\"EstadoCivil\",\"RegimenConyugal\",\"Cargo\",\"Empleador\",\"IdDireccionFisicaPrincipal\",\"IdDireccionElectronicaPrincipal\","
			+ "\"IdTelefonoPrincipal\",\"IdProfesion\",\"DocumentoIdentidad\",\"TipoDocumentoIdentidad\",\"DocumentoIdentidadAdicional\",\"TipoDocumentoIdentidadAdicional\","
			+ "\"IdPersonaConyuge\",\"IdPersonaRepresentante\",\"IdCliente\",\"IdPersona\",\"IdEmpresa\",\"IdSegmentoCliente\",\"NombreSegmentoCliente\","
			+ "\"DescripcionSegmentoCliente\",\"DescripcionSegmento\",\"IdCategoriaInversionista\",\"NombreCategoriaInversionista\",\"IdPersonaEjecutivo\","
			+ "\"IdEmpleadoEjecutivo\",\"IdSucursalEjecutivo\",\"CodigoEmpleadoEjecutivo\",\"CodigoEjecutivoExterno\",\"IdTipoCliente\",\"IdTipoBloqueo\",\"FechaBloqueo\","
			+ "\"ComentarioBloqueo\",\"IdUsuarioCreador\",\"FechaIngreso\",\"FechaCreacion\",\"FechaModificacion\",\"EsEnrolado\",\"IdEstado\",\"IdEstadoPersona\","
			+ "\"IdEstadoTabla\",\"IdPlantillaLimiteExtra\",\"NombreTipoPersona\",\"CodigoAdicionalHolding\",\"IdSucursalPreferencia\",\"IdPersonaBrokerExtranjero\",\"NombreEstado\" "
			+ "from \"BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente\" where \"TipoDocumentoIdentidad\"=:TipoDocumentoIdentidad and \"DocumentoIdentidad\" in (:ListaDocumentoIdentidad)";
	private final String SQL_SELECT_PORTAFOLIOS = "select \"IdPortafolio\",\"CodigoPortafolio\",\"DescripcionPortafolio\",\"Proposito\",\"IdCliente\",\"IdEstado\","
			+ "\"Estado\",\"FechaBloqueo\",\"ComentarioBloqueo\",\"IdTipoBloqueo\",\"IdPerfilComercialBaseOrigen\",\"IdPerfilProductos\",\"IdPersonaBrokerExtranjero\","
			+ "\"IdPerfilProductosExtra\",\"IdPerfilCobros\",\"IdPerfilCobrosExtra\",\"IdPerfilLimites\",\"IdPerfilLimitesExtra\",\"IdPerfilCondicionesOperativas\","
			+ "\"IdPerfilCondicionesOperativasExtra\",\"IdEstadoAutorizacion\",\"FechaIngreso\",\"FechaCreacion\",\"FechaModificacion\",\"IdDireccionEnvioCorrespondencia\","
			+ "\"IdTelefonoContacto\",\"IdCarga\",\"IdEmpleadoEjecutivo\",\"EsEjecutivoExclusivo\",\"CodigoPropositoPortafolio\",\"NumeroFicha\",\"IdNombreFantasia\","
			+ "\"Alias\",\"EsCuentaPropia\",\"IdEmpresaCuentaPropia\"from \"BSCL.Portafolio.BD.Vistas::ConsultarPortafolio\" "
			+ "where \"IdCliente\" in (:ListaIdClientes)";	
	
	private final String SQL_UPDATE_CLIENTE = "update \"BSCL.SocioNegocio.BD::CLI.Cliente\" set \"IdPersonaEjecutivo\"=coalesce(:IdPersonaEjecutivo,\"IdPersonaEjecutivo\"), \"IdSucursalPreferencia\"=coalesce(:IdSucursalPreferencia,\"IdSucursalPreferencia\") where \"IdCliente\"=:IdCliente";
	private final String SQL_UPDATE_PORTAFOLIO = "update \"BSCL.Portafolio.BD::POR.Portafolio\" set \"IdEmpleadoEjecutivo\"=coalesce(:IdEmpleadoEjecutivo,\"IdEmpleadoEjecutivo\") where \"IdPortafolio\"=:IdPortafolio";
	/*******************
	 * pojos
	 *******************/
	public static class Ejecutivo {
		public Long					IdEmpresa; 
		public Long					IdEmpleadoEjecutivo; 
		public Long					IdPersonaEjecutivo; 
		public String				NombreCompleto; 
		public String				CodigoEmpleado; 
		public Long					IdSucursal; 
		public String				NombreSucursal; 
		public String				CodigoSucursal; 
		public String				IdEstado; 
		public String				NombreEstado; 
		public Long					IdEmpleadoSupervisor; 
		public String				NombreSupervisor; 
		public Long					IdEmpleadoEjecutivoReemplaza; 
		public String				NombreCompletoReemplaza; 
		public java.sql.Timestamp	FechaInicioReemplazo; 
		public String				FechaInicioReemplazoStr; 
		public java.sql.Timestamp	FechaFinReemplazo; 
		public String				FechaFinReemplazoStr; 
	}
	public static class Sucursal {
		public Long					IdSucursal; 
		public Long					IdEmpresa; 
		public String				CodigoSucursal; 
		public String				NombreSucursal; 
		public String				EstadoSucursal; 
		public Long					IdDivisionPais; 
		public String				Calle; 
		public String				Numero; 
		public String				Depto; 
		public String				Telefono; 
	}
	public static class Cliente {
		public Long					IdDocumentoIdentidad; 
		public String				Tipo;//tipoPersona 
		public String				NombreCompleto; 
		public String				Nombres; 
		public String				ApellidoPaterno; 
		public String				ApellidoMaterno; 
		public String				RazonSocial; 
		public Long					IdGiroPrincipal; 
		public Long					IdPaisNacimiento; 
		public Long					IdPaisResidencia; 
		public Long					IdPaisNacionalidadPrincipal; 
		public java.sql.Date		FechaNacimiento; 
		public java.sql.Date		FechaDefuncion; 
		public String				Sexo; 
		public String				EsExtranjero; 
		public String				EstadoCivil; 
		public String				RegimenConyugal; 
		public String				Cargo; 
		public String				Empleador; 
		public Long					IdDireccionFisicaPrincipal; 
		public Long					IdDireccionElectronicaPrincipal; 
		public Long					IdTelefonoPrincipal; 
		public Long					IdProfesion; 
		public String				DocumentoIdentidad; 
		public String				TipoDocumentoIdentidad; 
		public String				DocumentoIdentidadAdicional; 
		public String				TipoDocumentoIdentidadAdicional; 
		public Long					IdPersonaConyuge; 
		public Long					IdPersonaRepresentante; 
		public Long					IdCliente; 
		public Long					IdPersona; 
		public Long					IdEmpresa; 
		public Long					IdSegmentoCliente; 
		public String				NombreSegmentoCliente; 
		public String				DescripcionSegmentoCliente; 
		public String				DescripcionSegmento; 
		public String				IdCategoriaInversionista; 
		public String				NombreCategoriaInversionista; 
		public Long					IdPersonaEjecutivo; 
		public Long					IdEmpleadoEjecutivo; 
		public Long					IdSucursalEjecutivo; 
		public String				CodigoEmpleadoEjecutivo; 
		public String				CodigoEjecutivoExterno; 
		public String				IdTipoCliente; 
		public String				IdTipoBloqueo; 
		public java.sql.Timestamp	FechaBloqueo; 
		public String				FechaBloqueoStr; 
		public String				ComentarioBloqueo; 
		public Long					IdUsuarioCreador; 
		public java.sql.Timestamp	FechaIngreso; 
		public String				FechaIngresoStr; 
		public java.sql.Timestamp	FechaCreacion; 
		public String				FechaCreacionStr; 
		public java.sql.Timestamp	FechaModificacion; 
		public String				FechaModificacionStr; 
		public String				EsEnrolado; 
		public String				IdEstado; 
		public String				IdEstadoPersona; 
		public String				IdEstadoTabla; 
		public String				IdPlantillaLimiteExtra; 
		public String				NombreTipoPersona; 
		public String				CodigoAdicionalHolding; 
		public Long					IdSucursalPreferencia; 
		public Long					IdPersonaBrokerExtranjero; 
		public String				NombreEstado; 
	}
	public static class Portafolio {
		public Long					IdPortafolio; 
		public String				CodigoPortafolio; 
		public String				DescripcionPortafolio; 
		public String				Proposito; 
		public Long					IdCliente; 
		public String				IdEstado; 
		public String				Estado; 
		public java.sql.Timestamp	FechaBloqueo; 
		public String				FechaBloqueoStr; 
		public String				ComentarioBloqueo; 
		public String				IdTipoBloqueo; 
		public String				IdPerfilComercialBaseOrigen; 
		public String				IdPerfilProductos; 
		public Long					IdPersonaBrokerExtranjero; 
		public String				IdPerfilProductosExtra; 
		public String				IdPerfilCobros; 
		public String				IdPerfilCobrosExtra; 
		public String				IdPerfilLimites; 
		public String				IdPerfilLimitesExtra; 
		public String				IdPerfilCondicionesOperativas; 
		public String				IdPerfilCondicionesOperativasExtra; 
		public String				IdEstadoAutorizacion; 
		public java.sql.Timestamp	FechaIngreso; 
		public String				FechaIngresoStr; 
		public java.sql.Timestamp	FechaCreacion; 
		public String				FechaCreacionStr; 
		public java.sql.Timestamp	FechaModificacion; 
		public String				FechaModificacionStr; 
		public Long					IdDireccionEnvioCorrespondencia; 
		public Long					IdTelefonoContacto; 
		public Long					IdCarga; 
		public Long					IdEmpleadoEjecutivo; 
		public String				EsEjecutivoExclusivo; 
		public String				CodigoPropositoPortafolio; 
		public String				NumeroFicha; 
		public Long					IdNombreFantasia; 
		public String				Alias; 
		public String				EsCuentaPropia; 
		public Long					IdEmpresaCuentaPropia; 
	}
	public static class CodigoEmpleado{
		public String CodigoEmpleado;
	}
	public static class DocumentoIdentidad{
		public String DocumentoIdentidad;
	}
	public static class Id{
		public Long	IdRegistro;
	}
	/*******************
	 * clases in/out
	 *******************/
	public static class InRecarterizacionClientes {
		public Long 		p_IdEmpresa;
		public Long 		p_IdUsuario;
		public String 		p_SimularOperacion;
		public String		p_TipoDocumentoIdentidad;
		public String		p_OmiteValidarSucursal; //"S"-> no valida que sean sucursales iguales, asigna al cliente sucursal del nuevo Ejecutivo.
		public List<Registro> p_Registros;
		public static class Registro{
			public Long		LineaArchivo;
			public String	TipoPersona;
			public String	DocumentoIdentidad;
			public String	SucursalCliente;
			public String	EjecutivoCliente;
			public String	CodigoPortafolio;
			public String	EjecutivoPortafolio;
		}
		public InRecarterizacionClientes(){
			p_Registros = new ArrayList<Registro>();
		}
	}
	
	public static class OutRecarterizacionClientes{
		public Long 		P_OUT_ERRORESCONTADOR;
		public String		P_OUT_DESCRIPCION;
		public List<Registro> p_Registros;
		public static class Registro{
			//se devuelven los datos de entrada:
			public Long		LineaArchivo;
			public String	TipoPersona;
			public String	DocumentoIdentidad;
			public String	SucursalCliente;
			public String	EjecutivoCliente;
			public String	CodigoPortafolio;
			public String	EjecutivoPortafolio;
			//variables de procesamiento (interno)
			public boolean	procExisteCliente;
			public Long		procIdCliente;
			public boolean	procExisteSucursal;
			public boolean	procUsaSucursalCliente;
			public Long		procIdSucursalCliente;
			public Long		procIdSucursalClienteAntes;
			public boolean	procExisteEjecutivoCliente;
			public boolean	procUsaEjecutivoCliente;
			public Long		procIdEmpleadoEjecutivoCliente;
			public Long		procIdPersonaEjecutivoCliente;
			public Long		procIdPersonaEjecutivoClienteAntes;
			public boolean 	procUsaEjecutivoPortafolio;
			public boolean 	procExisteEjecutivoPortafolio; 
			public Long		procIdEmpleadoEjecutivoPortafolio;
			public Long		procIdPersonaEjecutivoPortafolio;
			public Long		procIdEmpleadoEjecutivoPortafolioAntes;
			public boolean 	procProcesaTodosPortafolios;
			public boolean	procExistePortafolio;
			public Long		procIdPortafolio;
			public String	procIdEstadoCliente;
			public String	procIdEstadoPortafolio;
			public String	procNombreCliente;
			public boolean	procError;
			//resultadoProceso pÃºblico:
			public String	SucursalClienteAnterior;
			public String	EjecutivoClienteAnterior;
			public String	EjecutivoPortafolioAnterior;
			public String	MensajeValidacion;
			public String	MensajeError;
		}
		public OutRecarterizacionClientes(){
			p_Registros = new ArrayList<Registro>();
		}
	}	
	
	/*******************
	 * mÃ©todo principal
	 *******************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = new Gson();		
		InRecarterizacionClientes in=gson.fromJson(jin.toString() ,InRecarterizacionClientes.class);
		OutRecarterizacionClientes out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public static void main(String[] args) {
		
		//Settings by file
        String dbURL  = "jdbc:sap://bcshanadev:30215/?";
        String dbprop = "currentSchema=BSCL;";
        String dbdrv  = "com.sap.db.jdbc.Driver";
        String dbqry  = "select 1 from dummy";
        String user   = "JGARCES";
        String pass   = "Bolsa199";		
        //Datasource
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(dbdrv);
        ds.setUsername(user);
        ds.setPassword(pass);
        ds.setUrl(dbURL);
        ds.setValidationQuery(dbqry);
        ds.setConnectionProperties(dbprop);
        ds.setDefaultAutoCommit(false);
        //prueba de la clase
		try {
			Connection con = ds.getConnection();
			InRecarterizacionClientes entrada = new InRecarterizacionClientes();
			entrada.p_IdEmpresa=1L;
			entrada.p_IdUsuario=37L;
			entrada.p_SimularOperacion="N";
			entrada.p_TipoDocumentoIdentidad="R";
			entrada.p_OmiteValidarSucursal="S"; //"S"-> no valida que sean sucursales iguales, asigna al cliente sucursal del nuevo Ejecutivo.
			InRecarterizacionClientes.Registro item1 = new InRecarterizacionClientes.Registro();
			item1.LineaArchivo=1L;
			item1.TipoPersona="N";
			item1.DocumentoIdentidad="16929326-0";
			item1.SucursalCliente="SC01";//S002
			item1.EjecutivoCliente="0011";//JUANGUIT
			item1.CodigoPortafolio="";
			item1.EjecutivoPortafolio="0011";//JUANGUIT
			entrada.p_Registros.add(item1);
 			JsonParser parser 		= new JsonParser();
	        JsonObject dataJsonIn	= parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
	        RecarterizacionClientes demo=new RecarterizacionClientes();		
	    	System.out.println(demo.call(con, dataJsonIn).toString());	
	    	con.commit();
	    	con.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
        System.out.println("== END ==");
	}
	
	/**********************
	 * lÃ³gica del programa
	 **********************/
	public OutRecarterizacionClientes logica(Connection con, InRecarterizacionClientes entrada) throws Exception {
		boolean debug = true;
		log.write("^.^ [INI]PROCESAMIENTO RECARTERIZACION, DEBUG="+debug+" ^.^");
		OutRecarterizacionClientes salida = new OutRecarterizacionClientes();
		salida.P_OUT_ERRORESCONTADOR = new Long(1);
		SQL sql=new SQL();
		String fechaSistema = Utils.getFechaSistema(con,"BSCL",entrada.p_IdEmpresa);
		List<String> itemsCodigoEmpleado = new ArrayList<String>();
		List<String> itemsDocumentoIdentidad = new ArrayList<String>();
		List<Long> itemsIdCliente = new ArrayList<Long>();
		if(entrada.p_Registros.size()==0){
			log.write("^.^ [FIN]NO HAY DATOS PARA PROCESAR");
			return salida;
		}
		if(debug)log.write("OBTIENE DOCUMENTOS IDENTIDAD Y CODIGOS EJECUTIVOS UNICOS PARA OPTIMIZAR LLAMADAS A BD");
		for(InRecarterizacionClientes.Registro item : entrada.p_Registros){
			//------------------------------------------------------
			//Itera sobre entrada para determinar datos no repetidos
			//------------------------------------------------------
			//Codigos empleados
			boolean existeEjecutivoCliente = false;
			boolean existeEjecutivoPortafolio = false;
			for(String itemCod : itemsCodigoEmpleado){
				if(item.EjecutivoCliente.equals("")||itemCod.equals(item.EjecutivoCliente))existeEjecutivoCliente=true;
				if(item.EjecutivoPortafolio.equals("")||itemCod.equals(item.EjecutivoPortafolio))existeEjecutivoPortafolio=true;
				if(existeEjecutivoCliente&&existeEjecutivoPortafolio)break;
			}
			if(!existeEjecutivoCliente)itemsCodigoEmpleado.add(item.EjecutivoCliente);
			if(!existeEjecutivoPortafolio&&!item.EjecutivoCliente.equals(item.EjecutivoPortafolio))itemsCodigoEmpleado.add(item.EjecutivoPortafolio);
			//Documentos identidad
			boolean existeDocumentoIdentidad = false;
			for(String itemDocId : itemsDocumentoIdentidad){
				if(item.DocumentoIdentidad.equals("")||itemDocId.equals(item.DocumentoIdentidad)){
					existeDocumentoIdentidad=true;
					break;
				}
			}
			if(!existeDocumentoIdentidad)itemsDocumentoIdentidad.add(item.DocumentoIdentidad);
		}
		if(debug)log.write("OBTIENE LISTADO EJECUTIVOS PARA CODIGOS INGRESADOS");
		HashMap<String,Object> parmsInEjecutivos =new HashMap<String,Object>();
		parmsInEjecutivos.put("IdEmpresa", entrada.p_IdEmpresa);
		parmsInEjecutivos.put("ListaCodigoEmpleado", itemsCodigoEmpleado);
		List<Ejecutivo> ejecutivos = sql.query(con, SQL_SELECT_EJECUTIVOS,parmsInEjecutivos,Ejecutivo.class);
		
		if(debug)log.write("OBTIENE SUCURSALES DE LA CORREDORA");
		HashMap<String,Object> parmsInSucursales =new HashMap<String,Object>();
		parmsInSucursales.put("IdEmpresa", entrada.p_IdEmpresa);
		List<Sucursal> sucursales = sql.query(con, SQL_SELECT_SUCURSALES,parmsInSucursales,Sucursal.class);
		
		if(debug)log.write("OBTIENE LISTADO CLIENTES PARA DOCUMENTOS DE IDENTIDAD INGRESADOS (PRINCIPAL)");
		HashMap<String,Object> parmsInClientes =new HashMap<String,Object>();
		parmsInClientes.put("ListaDocumentoIdentidad", itemsDocumentoIdentidad);
		parmsInClientes.put("TipoDocumentoIdentidad", entrada.p_TipoDocumentoIdentidad);
		List<Cliente> clientesTodos = sql.query(con, SQL_SELECT_CLIENTES,parmsInClientes,Cliente.class);
		List<Cliente> clientes = new ArrayList<Cliente>();
		//filtra los clientes en base al tipo de persona ingresado (llave)
		for(InRecarterizacionClientes.Registro itementrada:entrada.p_Registros){
			if(itementrada.DocumentoIdentidad.equals(""))continue;
			for(Cliente item : clientesTodos){
				if(item.DocumentoIdentidad.equals(itementrada.DocumentoIdentidad)&&item.Tipo.equals(itementrada.TipoPersona)){
					clientes.add(item);
					break;
				}
			}
		}
		if(debug){log.writeAsJson("Listado de Clientes Validos",clientes);}
		
		if(debug){log.write("OBTENER DATOS PORTAFOLIOS EN BASE A CLIENTES OBTENIDOS");}
		List<Portafolio> portafolios=null;
		if(clientes.size()>0){
			for(Cliente item : clientes)itemsIdCliente.add(item.IdCliente);
			HashMap<String,Object> parmsInPortafolios =new HashMap<String,Object>();
			parmsInPortafolios.put("ListaIdClientes", itemsIdCliente);
			portafolios = sql.query(con, SQL_SELECT_PORTAFOLIOS,parmsInPortafolios,Portafolio.class);
			if(debug)log.writeAsJson("Listado de Portafolios",portafolios);
		}else{if(debug)log.write("No se obtuvo ningun cliente->no se realiza query a portafolios.");}
		

		if(debug)log.write("COMIENZA EL PROCESAMIENTO");
		for(InRecarterizacionClientes.Registro itemin:entrada.p_Registros){
			if(debug)log.write("###### Procesamiento linea archivo=>"+itemin.LineaArchivo+" ######");
			OutRecarterizacionClientes.Registro itemout=new OutRecarterizacionClientes.Registro();
			fnInRegistroToOutRegistro(itemin,itemout);
			
			if(debug)log.write(">VERIFICACION DEL CLIENTE"+":\""+itemin.DocumentoIdentidad+"\"");
			itemout.procExisteCliente=false;
			itemout.procIdCliente=0L;
			for(Cliente itemcli:clientes){
				if(itemin.DocumentoIdentidad.equals(itemcli.DocumentoIdentidad)){
					itemout.procExisteCliente=true;
					itemout.procIdCliente=itemcli.IdCliente;
					itemout.procNombreCliente=itemcli.NombreCompleto;
					itemout.procIdEstadoCliente=itemcli.IdEstado;
					itemout.procIdPersonaEjecutivoClienteAntes=itemcli.IdPersonaEjecutivo;
					itemout.procIdSucursalClienteAntes=itemcli.IdSucursalPreferencia;//TODO:VER SI ES SUCURSALEJECUTIVO
					break;
				}
			}
			if(itemout.procExisteCliente){if(debug)log.write("El cliente existe!");}
			else{if(debug)log.write(Error_ClienteNoExiste);}
			
			if(debug)log.write(">VALIDA SUCURSAL"+":\""+itemin.SucursalCliente+"\"");
			itemout.procExisteSucursal=false;
			itemout.procUsaSucursalCliente=false;
			itemout.procIdSucursalCliente=0L;
			if(itemin.SucursalCliente!=null&&!itemin.SucursalCliente.equals("")){
				itemout.procUsaSucursalCliente=true;
				for(Sucursal itemsuc:sucursales){
					if(itemin.SucursalCliente.equals(itemsuc.NombreSucursal)){
						itemout.procExisteSucursal=true;
						itemout.procIdSucursalCliente=itemsuc.IdSucursal;
						break;
					}
				}
				if(itemout.procExisteSucursal){if(debug)log.write("Sucursal existe!");}
				else{if(debug)log.write(Error_SucursalNoExiste);}
			}else{if(debug)log.write("No se configura cambio de sucursal");}
			
			if(debug)log.write(">VALIDA EJECUTIVO DEL CLIENTE"+":\""+itemin.EjecutivoCliente+"\"");
			itemout.procExisteEjecutivoCliente=false;
			itemout.procUsaEjecutivoCliente=false;
			itemout.procIdEmpleadoEjecutivoCliente=0L;
			if(itemin.EjecutivoCliente!=null&&!itemin.EjecutivoCliente.equals("")){
				itemout.procUsaEjecutivoCliente=true;
				for(Ejecutivo itemeje:ejecutivos){
					if(itemeje.CodigoEmpleado.equals(itemin.EjecutivoCliente)){
						itemout.procExisteEjecutivoCliente=true;
						itemout.procIdEmpleadoEjecutivoCliente=itemeje.IdEmpleadoEjecutivo;
						itemout.procIdPersonaEjecutivoCliente=itemeje.IdPersonaEjecutivo;
						break;
					}
				}
				if(itemout.procExisteEjecutivoCliente){if(debug)log.write("El ejecutivo definido para cliente existe!");}
				else{if(debug)log.write(Error_EjecutivoClienteNoExiste);}
			}else{if(debug)log.write("No se configura cambio de ejecutivo");}
			
			if(debug)log.write(">VALIDA EJECUTIVO PORTAFOLIO"+":\""+itemin.EjecutivoPortafolio+"\"");
			itemout.procUsaEjecutivoPortafolio = false; 
			itemout.procExisteEjecutivoPortafolio = false; 
			itemout.procIdEmpleadoEjecutivoPortafolio=0L;
			if(itemin.EjecutivoPortafolio!=null&&!itemin.EjecutivoPortafolio.equals("")){
				itemout.procUsaEjecutivoPortafolio=true;
				for(Ejecutivo itemeje:ejecutivos){
					if(itemeje.CodigoEmpleado.equals(itemin.EjecutivoPortafolio)){
						itemout.procExisteEjecutivoPortafolio=true;
						itemout.procIdEmpleadoEjecutivoPortafolio=itemeje.IdEmpleadoEjecutivo;
						itemout.procIdPersonaEjecutivoPortafolio=itemeje.IdPersonaEjecutivo;
						break;
					}
				}
				if(itemout.procExisteEjecutivoPortafolio){if(debug)log.write("El ejecutivo definido para portafolio existe!");}
				else{if(debug)log.write(Error_EjecutivoPortafolioNoExiste);}
			}else{
				if(itemout.procUsaEjecutivoCliente){//ES VACIO HEREDA EL EJEUTIVO DEL CLIENTE
					itemout.procExisteEjecutivoPortafolio=itemout.procExisteEjecutivoCliente;
					itemout.procIdEmpleadoEjecutivoPortafolio=itemout.procIdEmpleadoEjecutivoCliente;
					itemout.procUsaEjecutivoPortafolio=itemout.procUsaEjecutivoCliente;
					if(debug){log.write("No se define ejecutivo de portafolio se usara ejecutivo de cliente");}
				}else{if(debug)log.write("No se define ejecutivo de portafolio ni ejecutivo de cliente");}
			}
			
			if(debug)log.write(">VALIDA PORTAFOLIO DEL CLIENTE"+":\""+itemin.CodigoPortafolio+"\"");
			itemout.procProcesaTodosPortafolios=true;
			if(itemin.CodigoPortafolio!=null&&!itemin.CodigoPortafolio.equals("")){
				itemout.procProcesaTodosPortafolios=false;
				itemout.procExistePortafolio=false;
				if(itemout.procIdCliente>0){
					for(Portafolio itempor:portafolios){ //se asume que el cliente tiene al menos 1 portafolio!
						if(itempor.IdCliente.equals(itemout.procIdCliente) &&itempor.CodigoPortafolio.equals(itemin.CodigoPortafolio)) {
							itemout.procExistePortafolio = true;
							itemout.procIdPortafolio = itempor.IdPortafolio;
							itemout.procIdEmpleadoEjecutivoPortafolioAntes=itempor.IdEmpleadoEjecutivo;
							itemout.procIdEstadoPortafolio=itempor.IdEstado;
							break;
						}
					}
				}else{if(debug)log.write("Al no encontrar cliente, se omite recorrer portafolios");}
				if(itemout.procExistePortafolio){if(debug)log.write("El portafolio definido existe!");}
				else{if(debug)log.write("Error_PortafolioNoExiste");}
				if(debug)log.write("Agrega el regristro de procesamiento a la salida");
				fnGeneraMensajesItem(itemout);
				salida.p_Registros.add(itemout);
			}else {
				if(debug)log.write(">(SE PROCESARAN TODOS LOS PORTAFOLIOS)");
				if(itemout.procIdCliente>0){
					for(Portafolio itempor:portafolios){
						if(itempor.IdCliente.equals(itemout.procIdCliente)) {
							if(debug)log.write(">>Portafolio"+":\""+itempor.CodigoPortafolio+"\"");
							OutRecarterizacionClientes.Registro itemportout=new OutRecarterizacionClientes.Registro();
							fnInRegistroToOutRegistro(itemin,itemportout);
							itemportout.procExisteCliente = itemout.procExisteCliente;
							itemportout.procIdCliente = itemout.procIdCliente;
							itemportout.procNombreCliente=itemout.procNombreCliente;
							itemportout.procExisteSucursal = itemout.procExisteSucursal;
							itemportout.procUsaSucursalCliente = itemout.procUsaSucursalCliente;
							itemportout.procIdSucursalCliente = itemout.procIdSucursalCliente;
							itemportout.procIdSucursalClienteAntes = itemout.procIdSucursalClienteAntes;
							itemportout.procExisteEjecutivoCliente = itemout.procExisteEjecutivoCliente;
							itemportout.procUsaEjecutivoCliente = itemout.procUsaEjecutivoCliente;
							itemportout.procIdEmpleadoEjecutivoCliente = itemout.procIdEmpleadoEjecutivoCliente;
							itemportout.procIdPersonaEjecutivoCliente = itemout.procIdPersonaEjecutivoCliente;
							itemportout.procIdPersonaEjecutivoClienteAntes = itemout.procIdPersonaEjecutivoClienteAntes;
							itemportout.procUsaEjecutivoPortafolio = itemout.procUsaEjecutivoPortafolio;
							itemportout.procExisteEjecutivoPortafolio = itemout.procExisteEjecutivoPortafolio;
							itemportout.procIdEmpleadoEjecutivoPortafolio = itemout.procIdEmpleadoEjecutivoPortafolio;
							itemportout.procIdPersonaEjecutivoPortafolio = itemout.procIdPersonaEjecutivoPortafolio;
							itemportout.procProcesaTodosPortafolios = itemout.procProcesaTodosPortafolios;
							//Datos obtenidos del portafolio
							itemportout.procExistePortafolio = true;
							itemportout.procIdPortafolio = itempor.IdPortafolio;
							itemportout.procIdEmpleadoEjecutivoPortafolioAntes=itempor.IdEmpleadoEjecutivo;
							itemportout.procIdEstadoPortafolio=itempor.IdEstado;
							itemportout.CodigoPortafolio=itempor.CodigoPortafolio;//repasa el codigo portafolio.
							fnGeneraMensajesItem(itemportout);
							salida.p_Registros.add(itemportout);
						}
					}
				}else{
					if(debug)log.write("Al no encontrar cliente, se omite recorrer portafolios");
					salida.p_Registros.add(itemout);
				}
			}
			if(debug){log.write(">VALIDACIONES Y APERTURA DE REGISTROS REALIZADA. SE PREPARA EL UPDATE.");}
		}//for
		if(salida.p_Registros.size()==0){
			
		}
		if(debug){log.write("SECCION ACTUALIZAR");}
		List<Cliente> listaUpdateCliente=new ArrayList<Cliente>();
		List<Portafolio> listaUpdatePortafolio=new ArrayList<Portafolio>();
		AgregarLog logcambios=new AgregarLog();
		AgregarLog.InAgregarLog inlogcambios=new AgregarLog.InAgregarLog();
		inlogcambios.p_IdTrabajo=1313L;
		Long idCabeceraLog=1L;
		for(int vm=0;vm<salida.p_Registros.size();vm++){
			OutRecarterizacionClientes.Registro item=salida.p_Registros.get(vm);
			if(item.procError){
				if(debug){log.write(">Hay errores para item:"+vm+",linea archivo:"+item.LineaArchivo+". Se omite en update");}
				continue;
			}
			//Update cliente
			Cliente ic=new Cliente();
			ic.IdCliente=item.procIdCliente;
			boolean cambiarEjecutivoCliente=false;
			boolean cambiarSucursalCliente=false;
			if(item.procUsaEjecutivoCliente){
				if(debug){log.write("IdPersonaEjecutivo Antes="+item.procIdPersonaEjecutivoClienteAntes+",IdPersonaEjecutivo Despues="+item.procIdPersonaEjecutivoCliente);}
				if(item.procIdPersonaEjecutivoClienteAntes==null&&item.procIdPersonaEjecutivoCliente!=null){
					if(debug){log.write("cliente no tenia ejecutivo previamente asignado!");}
					cambiarEjecutivoCliente=true;
				}
				else if(item.procIdPersonaEjecutivoCliente!=null&&!item.procIdPersonaEjecutivoClienteAntes.equals(item.procIdPersonaEjecutivoCliente)){
					cambiarEjecutivoCliente=true;
				}else{if(debug){log.write("Mismo ejecutivo para el cliente!");}}
			}
			if(item.procUsaSucursalCliente){
				if(debug){log.write("IdSucursalCliente Antes="+item.procIdSucursalClienteAntes+",IdSucursalCliente Despues="+item.procIdSucursalCliente);}
				if(item.procIdSucursalClienteAntes==null&&item.procIdSucursalCliente!=null){
					if(debug){log.write("cliente no tenÃ­a sucursal previamente asignada!");}
					cambiarSucursalCliente=true;
				}
				else if(item.procIdSucursalCliente!=null&&!item.procIdSucursalClienteAntes.equals(item.procIdSucursalCliente)){
					cambiarSucursalCliente=true;
				}else{if(debug)log.write("Misma sucursal para el cliente!");}
			}
			if(cambiarEjecutivoCliente){
				ic.IdPersonaEjecutivo=item.procIdPersonaEjecutivoCliente;
				AgregarLog.InAgregarLog.DetalleLog detLog=new AgregarLog.InAgregarLog.DetalleLog();
				detLog.IdTrabajo=inlogcambios.p_IdTrabajo;
				detLog.IdCabeceraLog=idCabeceraLog;
				detLog.SubRegistro="CLIENTE";
				detLog.CampoRegistro="IdPersonaEjecutivo";
				detLog.ValorAntiguo=(item.procIdPersonaEjecutivoClienteAntes!=null)?item.procIdPersonaEjecutivoClienteAntes.toString():"";
				detLog.ValorNuevo=item.procIdPersonaEjecutivoCliente.toString();
				inlogcambios.p_LogDetalle.add(detLog);
			}
			if(cambiarSucursalCliente){
				ic.IdSucursalPreferencia=item.procIdSucursalCliente;
				AgregarLog.InAgregarLog.DetalleLog detLog=new AgregarLog.InAgregarLog.DetalleLog();
				detLog.IdTrabajo=inlogcambios.p_IdTrabajo;
				detLog.IdCabeceraLog=idCabeceraLog;
				detLog.SubRegistro="CLIENTE";
				detLog.CampoRegistro="IdSucursalPreferencia";
				detLog.ValorAntiguo=(item.procIdSucursalClienteAntes!=null)?item.procIdSucursalClienteAntes.toString():"";
				detLog.ValorNuevo=item.procIdSucursalCliente.toString();
				inlogcambios.p_LogDetalle.add(detLog);
			}
			if(cambiarEjecutivoCliente||cambiarSucursalCliente){
				listaUpdateCliente.add(ic);
				item.MensajeValidacion=Validacion_clienteActualizado;
				AgregarLog.InAgregarLog.Log cabLog=new AgregarLog.InAgregarLog.Log();
				cabLog.IdTrabajo=inlogcambios.p_IdTrabajo;
				cabLog.IdCabeceraLog=idCabeceraLog;
				cabLog.IdTipoLog="MODIFICACION";
				cabLog.IdModulo="SOC";
				cabLog.IdUsuario=entrada.p_IdUsuario;
				cabLog.IdEmpresa=entrada.p_IdEmpresa;
				cabLog.FechaSistemaLog=fechaSistema;
				cabLog.IdUsuarioAtendido=entrada.p_IdUsuario;
				cabLog.IdTipoRegistro="CLIENTE";
				cabLog.NombreProceso="RECARTERIZACION_CLIENTES";
				cabLog.IdRegistro=item.procIdCliente.toString();
				cabLog.EstadoProceso="AUT";
				cabLog.EstadoRegistro="AUT";
				cabLog.EstadoCliente=item.procIdEstadoCliente;
				cabLog.IdCliente=item.procIdCliente;
				cabLog.NombreCliente=item.procNombreCliente+"-"+item.DocumentoIdentidad;
				cabLog.IdTipoDocumentoIdentidad=entrada.p_TipoDocumentoIdentidad;
				inlogcambios.p_Log.add(cabLog);
				idCabeceraLog++;
			}
			//update portafolio
			Portafolio ip=new Portafolio();
			ip.IdPortafolio=item.procIdPortafolio;
			boolean cambiarEjecutivoPortafolio=false;
			if(item.procUsaEjecutivoPortafolio){
				if(debug){log.write("IdEmpleadoEjecutivoPortafolio Antes="+item.procIdEmpleadoEjecutivoPortafolioAntes+",IdEmpleadoEjecutivoPortafolio Despues="+item.procIdEmpleadoEjecutivoPortafolio);}
				if(item.procIdEmpleadoEjecutivoPortafolioAntes==null&&item.procIdEmpleadoEjecutivoPortafolio!=null){
					if(debug){log.write("portafolio no tenÃ­a ejecutivo previamente asignado!");}
					cambiarEjecutivoPortafolio=true;
				}
				else if(item.procIdEmpleadoEjecutivoPortafolio!=null&&!item.procIdEmpleadoEjecutivoPortafolioAntes.equals(item.procIdEmpleadoEjecutivoPortafolio)){
					cambiarEjecutivoPortafolio=true;
				}else{if(debug)log.write("Mismo ejecutivo para el portafolio!");}
			}
			if(cambiarEjecutivoPortafolio){
				ip.IdEmpleadoEjecutivo=item.procIdEmpleadoEjecutivoPortafolio;
				listaUpdatePortafolio.add(ip);
				item.MensajeValidacion=Validacion_clienteActualizado;
				ic.IdPersonaEjecutivo=item.procIdPersonaEjecutivoCliente;
				AgregarLog.InAgregarLog.DetalleLog detLog=new AgregarLog.InAgregarLog.DetalleLog();
				detLog.IdTrabajo=inlogcambios.p_IdTrabajo;
				detLog.IdCabeceraLog=idCabeceraLog;
				detLog.SubRegistro="PORTAFOLIO";
				detLog.CampoRegistro="IdEmpleadoEjecutivo";
				detLog.ValorAntiguo=(item.procIdEmpleadoEjecutivoPortafolioAntes!=null)?item.procIdEmpleadoEjecutivoPortafolioAntes.toString():"";
				detLog.ValorNuevo=item.procIdEmpleadoEjecutivoPortafolio.toString();
				inlogcambios.p_LogDetalle.add(detLog);
				AgregarLog.InAgregarLog.Log cabLog=new AgregarLog.InAgregarLog.Log();
				cabLog.IdTrabajo=inlogcambios.p_IdTrabajo;
				cabLog.IdCabeceraLog=idCabeceraLog;
				cabLog.IdTipoLog="MODIFICACION";
				cabLog.IdModulo="SOC";
				cabLog.IdUsuario=entrada.p_IdUsuario;
				cabLog.IdEmpresa=entrada.p_IdEmpresa;
				cabLog.FechaSistemaLog=fechaSistema;
				cabLog.IdUsuarioAtendido=entrada.p_IdUsuario;
				cabLog.IdTipoRegistro="PORTAFOLIO";
				cabLog.NombreProceso="RECARTERIZACION_CLIENTES";
				cabLog.IdRegistro=item.procIdPortafolio.toString();
				cabLog.EstadoProceso="AUT";
				cabLog.EstadoRegistro="AUT";
				cabLog.EstadoCliente=item.procIdEstadoCliente;
				cabLog.EstadoPortafolio=item.procIdEstadoPortafolio;
				cabLog.IdCliente=item.procIdCliente;
				cabLog.IdPortafolio=item.procIdPortafolio;
				cabLog.NombreCliente=item.procNombreCliente+"-"+item.DocumentoIdentidad;
				cabLog.IdTipoDocumentoIdentidad=entrada.p_TipoDocumentoIdentidad;
				inlogcambios.p_Log.add(cabLog);
				idCabeceraLog++;
			}
			if(!cambiarEjecutivoCliente&&!cambiarSucursalCliente&&!cambiarEjecutivoPortafolio){
				if(debug){log.write("No hay diferencias para actualizar en item="+vm);}
				item.MensajeValidacion=item.MensajeValidacion=Validacion_SinDiferencias;
			}
		}//for
		if(entrada.p_SimularOperacion.equals("N"))if(debug){log.write("DATOS LISTOS PARA REALIZAR UPDATE");}
		else {if(debug){log.write("SE SIMULA LA OPERACION");}}
		if(debug){log.write("SE REALIZA UPDATE");}
		if(listaUpdateCliente.size()>0){
			if(entrada.p_SimularOperacion.equals("N"))
				sql.batch(con,SQL_UPDATE_CLIENTE, listaUpdateCliente,Cliente.class);
		}
		else{if(debug)log.write("No hay datos de cliente para actualizar");}
		if(listaUpdatePortafolio.size()>0){
			if(entrada.p_SimularOperacion.equals("N")) 
				sql.batch(con,SQL_UPDATE_PORTAFOLIO, listaUpdatePortafolio,Portafolio.class);
		}
		else{if(debug)log.write("No hay datos de portafolio para actualizar");}
		salida.P_OUT_ERRORESCONTADOR = 0L;
		if(debug){log.write("AGREGAR LOG DE MODIFICACION DE EJECUTIVOS(pendiente insert)");};
		if(inlogcambios.p_Log.size()>0){
			if(debug){log.writeAsJson("Datos LOG",inlogcambios);}
			if(entrada.p_SimularOperacion.equals("N")){
				AgregarLog.OutAgregarLog outlogcambios=logcambios.logica(con, inlogcambios);
				if(outlogcambios.P_OUT_ERRORESCONTADOR>0){
					log.write("Error al agregar log");
					salida.P_OUT_ERRORESCONTADOR++;
				} else log.write("Log agregado correctamente");
			}			
		}else{if(debug){log.write("No hay datos para agregar log.");}}
		//log.writeAsJson("SALIDA:",salida);
		
		//SP pÃºblico debe realizar el commit de la operacion.
		/*if(salida.P_OUT_ERRORESCONTADOR==0)con.commit();
		else con.rollback();*/
		
		return salida;
	}
	
	public void fnInRegistroToOutRegistro(InRecarterizacionClientes.Registro itemin, OutRecarterizacionClientes.Registro itemout){
		itemout.LineaArchivo=itemin.LineaArchivo;
		itemout.TipoPersona=itemin.TipoPersona;
		itemout.DocumentoIdentidad=itemin.DocumentoIdentidad;
		itemout.SucursalCliente=itemin.SucursalCliente;
		itemout.EjecutivoCliente=itemin.EjecutivoCliente;
		itemout.CodigoPortafolio=itemin.CodigoPortafolio;
		itemout.EjecutivoPortafolio=itemin.EjecutivoPortafolio;
	}
	public void fnGeneraMensajesItem(OutRecarterizacionClientes.Registro item){
		//VALIDACIONES SEGUN RQ PERU
		item.MensajeValidacion="";item.MensajeError="";
		//ADVERTENCIAS
		String mensajeAdvertencia="";
		if("BLO".equals(item.procIdEstadoCliente)){mensajeAdvertencia=Validacion_ClienteBloqueado;}
		else{if("BLO".equals(item.procIdEstadoPortafolio))mensajeAdvertencia=Validacion_PortafolioBloqueado;}
		//ERRORES
		//VALIDAR QUE AGENTES NO SEAN LOS MISMOS!
		item.procError=true;
		item.MensajeValidacion=Validacion_clienteSinActualizar;
		if(!item.procExisteCliente){
			item.MensajeError=(item.MensajeError.equals(""))?Error_ClienteNoExiste:item.MensajeError+";"+Error_ClienteNoExiste;
			return;
		}
		if(item.procUsaSucursalCliente&&!item.procExisteSucursal){
			item.MensajeError=(item.MensajeError.equals(""))?Error_SucursalNoExiste:item.MensajeError+";"+Error_SucursalNoExiste;
			return;
		}
		if(item.procUsaEjecutivoCliente&&!item.procExisteEjecutivoCliente){
			item.MensajeError=(item.MensajeError.equals(""))?Error_EjecutivoClienteNoExiste:item.MensajeError+";"+Error_EjecutivoClienteNoExiste;
			return;
		}
		if(item.procUsaEjecutivoPortafolio&&!item.procExisteEjecutivoPortafolio){
			item.MensajeError=(item.MensajeError.equals(""))?Error_EjecutivoPortafolioNoExiste:item.MensajeError+";"+Error_EjecutivoPortafolioNoExiste;
			return;
		}
		item.procError=false;
	}
}
