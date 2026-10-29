package cl.bcs.socionegocio.blo;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.dbcp2.BasicDataSource;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class BloqueoAutomaticoClientesPorConfiguracion implements JavaCall {	
	private final Log log = new Log("SOC_BLOQUEOAUTOMATICOCLIENTE_PORCONFIGURACION");

	// QUERIES
	private final String SQL_SELECT_DATOSPROCESAR = loadResourceToString("cl/bcs/socionegocio/blo/sql/BloqueoAutomaticoClientesPorConfiguracion_SelectDatosProcesar.sql");
	private final String SQL_UPDATE_CLIENTE_BLO = loadResourceToString("cl/bcs/socionegocio/blo/sql/BloqueoAutomaticoClientesPorConfiguracion_UpdateClienteBlo.sql");
			
	
	/*******************
	 * pojos
	 *******************/
	public static class DatosConsulta{
		public Long 				IdCliente;
		public String				IdEstadoCliente;
		public String				IdTipoDocumento;
		public Long					IdDocumento;
		public Long 				IdUltimoAnexo;
		public String				IdEstadoAnexo;
		public java.sql.Timestamp	FechaVigencia;
	}
	
	public static class DatosActualizar{
		public Long 	IdCliente;
		public String 	IdTipoBloqueo;
		public String	ComentarioBloqueo;
	}
	
	/*******************
	 * clases in/out
	 *******************/
	public static class InBloqueoAutomaticoClientesPorConfiguracion {
		public Long 		p_IdEmpresa;
		public Long 		p_IdUsuario;
		public String 		p_IdModulo;
		public String 		p_SimularOperacion;
	}
	
	public static class OutBloqueoAutomaticoClientesPorConfiguracion{
		public Long 			P_OUT_ERRORESCONTADOR;
		public String			P_OUT_DESCRIPCION;
		public List<Registro> 	P_OUT_LISTA_CLIENTES;	
		
		
		public OutBloqueoAutomaticoClientesPorConfiguracion() {
	        P_OUT_LISTA_CLIENTES = new ArrayList<Registro>();
	    }		

		public static class Registro{
			public Long 	IdCliente;
			public Registro(){}
			public Registro(Long IdCliente){
				this.IdCliente = IdCliente;
			}
		}
	}	
	
	/*******************
	 * método principal
	 *******************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = new Gson();		
		InBloqueoAutomaticoClientesPorConfiguracion in=gson.fromJson(jin.toString() ,InBloqueoAutomaticoClientesPorConfiguracion.class);
		OutBloqueoAutomaticoClientesPorConfiguracion out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}	

	public static void main(String[] args) throws Exception {

    	try (Connection con = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {

			InBloqueoAutomaticoClientesPorConfiguracion entrada =
				new InBloqueoAutomaticoClientesPorConfiguracion();

			entrada.p_IdEmpresa = 1L;
			entrada.p_IdUsuario = 37L;
			entrada.p_IdModulo = "SOC";
			entrada.p_SimularOperacion = "N";

			JsonParser parser = new JsonParser();
			JsonObject dataJsonIn =
				parser.parse(new Gson().toJson(entrada)).getAsJsonObject();

			BloqueoAutomaticoClientesPorConfiguracion demo =
				new BloqueoAutomaticoClientesPorConfiguracion();

			System.out.println(
				demo.call(con, dataJsonIn).toString()
			);

			// con.commit();
			con.rollback();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("== END ==");
	}	

	/**********************
	 * lógica del programa
	 **********************/
	public OutBloqueoAutomaticoClientesPorConfiguracion logica(Connection con, InBloqueoAutomaticoClientesPorConfiguracion entrada) throws Exception {
		boolean debug = true;
		log.write("[INI]PROCESAMIENTO CAMBIO ESTADO CLIENTE/PORTAFOLIO, DEBUG="+debug);
		OutBloqueoAutomaticoClientesPorConfiguracion salida = new OutBloqueoAutomaticoClientesPorConfiguracion();
		salida.P_OUT_ERRORESCONTADOR = 0L;
		SQL sql=new SQL();
		String fechaHoraSistemaStr = Utils.getFechaHoraSistema(con, "BSCL", entrada.p_IdEmpresa);
		java.sql.Timestamp fechaHoraSistemaTs = Fecha.toTimeStamp(fechaHoraSistemaStr);
		String fechaSistema = Utils.getFechaSistema(con,"BSCL",entrada.p_IdEmpresa);
		List<String> itemsCodigoEmpleado = new ArrayList<String>();
		List<String> itemsDocumentoIdentidad = new ArrayList<String>();
		List<Long> itemsIdCliente = new ArrayList<Long>();
		
		HashMap<String,Object> parmsconsulta =new HashMap<String,Object>();
		parmsconsulta.put("IdEmpresa", entrada.p_IdEmpresa);
		List<DatosConsulta> datosConsulta = sql.query(con, SQL_SELECT_DATOSPROCESAR,parmsconsulta,DatosConsulta.class);
		log.write("prepara set de datos de salida y datos para el update");
		List<DatosActualizar> dacliBLO=new ArrayList<DatosActualizar>();
		//procesa datos de entrada para determinar salida.
		for(DatosConsulta itemcons:datosConsulta){
			if(  itemcons.IdDocumento==null
			  || itemcons.IdEstadoAnexo==null
			  || itemcons.IdEstadoAnexo.equals("VCD")
			  || itemcons.FechaVigencia.before(fechaHoraSistemaTs)
			){
				boolean clienteEnLista=false;
				for(DatosActualizar itemact:dacliBLO){
					if(itemact.IdCliente == itemcons.IdCliente){
						clienteEnLista=true;
						break;
					}
				}
				if(!clienteEnLista){
					DatosActualizar aux = new DatosActualizar();
					aux.IdCliente=itemcons.IdCliente;
					aux.IdTipoBloqueo="BAUTOM"; //Crear Tipo de Bloqueo!
					aux.ComentarioBloqueo="Cliente bloqueado automaticamente";
					dacliBLO.add(aux);
				}
			}
		}
		// realiza update
		if(dacliBLO.size()>0){
			if(entrada.p_SimularOperacion.equalsIgnoreCase("N")){
				sql.batch(con,SQL_UPDATE_CLIENTE_BLO, dacliBLO,DatosActualizar.class);
			}
			//realizado el update, devolver listado IdClientes bloqueados.
			for(DatosActualizar item:dacliBLO){
				salida.P_OUT_LISTA_CLIENTES.add(new OutBloqueoAutomaticoClientesPorConfiguracion.Registro(item.IdCliente));
			}
		}else{
			//no hay datos para bloquear.
		}
		log.writeAsJson("ENTRADA:",entrada);
		log.writeAsJson("SALIDA:",salida);
		log.write("****************** FIN ****************");
		return salida;
	}
}