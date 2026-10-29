package cl.bcs.plataforma.pla;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ActualizarConfiguracionFlujoCustodia implements JavaCall {

	private final Log log = new Log("PLA.ActualizarConfiguracionFlujoCustodia");
	
	private final String SQL_SELECT_CONFIGURACION = loadResourceToString("cl/bcs/plataforma/pla/sql/ActualizarConfiguracionFlujoCustodia_SelectConfiguracionFlujoCustodia.sql");//Obtiene datos de la vista ConfiguracionFlujoCustodia
	private final String SQL_INSERT_UPDATE_CONFIGURACION = loadResourceToString("cl/bcs/plataforma/pla/sql/ActualizarConfiguracionFlujoCustodia_UpsertConfiguracionFlujoCustodia.sql");// Actualiza la tabla BSCL.Custodia.BD::CUS.ItemsFlujoCustodia
	
	
	public static class ActualizarConfiguracionFlujoCustodia_Entrada {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;//PLA: Plataforma
		public List<ConfiguracionFlujoCustodia> p_ConfiguracionFlujoCustodia;

		public ActualizarConfiguracionFlujoCustodia_Entrada() {
			p_ConfiguracionFlujoCustodia = new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ConfiguracionFlujoCustodia {
		public Long IdUsoFlujoCustodia;
		public String Item;
		public String FuncionOperador;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class ActualizarConfiguracionFlujoCustodia_Salida {
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;

		public ActualizarConfiguracionFlujoCustodia_Salida() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ConfiguracionFlujoCustodia_Proceso {
		public Long IdUsoFlujoCustodia;
		public Long IdEmpresa;
		public String UsoTipoOperacion;
		public String IdClasificacionTipoInstrumento;
		public String IdTipoInstrumento;
		public Long IdInstrumento;
		public Long IdCliente;
		public Long IdPortafolio;
		public String BusquedaEspecifica;
		public java.sql.Timestamp FechaIngresoRegistro;
		public Long IdUsuario;
		public String IdEstadoUso;
		public String ItemCalculo;
		public String ItemOrigen;
		public String Descripcion;
		public String FuncionOperador;
		public String IdEstadoItem;
		public String EstadoUso;
		public String EstadoItem;
		public String NombreTipoOperacion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		ActualizarConfiguracionFlujoCustodia_Entrada in = gson.fromJson(jin.toString(), ActualizarConfiguracionFlujoCustodia_Entrada.class);
		ActualizarConfiguracionFlujoCustodia_Salida out = logica(conn, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	private ActualizarConfiguracionFlujoCustodia_Salida logica(Connection conn, ActualizarConfiguracionFlujoCustodia_Entrada in) throws Exception {
		SQL sql = new SQL();
		ActualizarConfiguracionFlujoCustodia_Salida salida = new ActualizarConfiguracionFlujoCustodia_Salida();
		salida.P_OUT_ERRORESCONTADOR = 0L;
		Long Id = 0L;
		HashMap<String,String> mapMsg = new HashMap<>();
		
		log.write("INICIO :: PLA.ActualizarConfiguracionFlujoCustodia");
		
		try {
			if(in.p_ConfiguracionFlujoCustodia.size() == 0){
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_ENVIARON_REGISTROS",mapMsg,conn,in.p_IdEmpresa));
				Id++;
				return salida;
			}
			
			String fechaHoraSistemaSecondate = FechaDB.ObtenerFechaHoraSistemaUTC(conn,in.p_IdEmpresa);
			
			HashMap<String, Object> params = new HashMap<>();
			params.put("IdEmpresa", in.p_IdEmpresa);
			List<ConfiguracionFlujoCustodia_Proceso> configuraciones = sql.query(conn, SQL_SELECT_CONFIGURACION, params, ConfiguracionFlujoCustodia_Proceso.class);
			
			HashMap<String, String> CalculoOrigen = new HashMap<>();// relaciona cada ItemCalculo con un ItemOrigen
			HashMap<Long, String> UsoTipoOperacion = new HashMap<>();// relaciona cada IdUsoFlujoCustodia con un UsoTipoOperacion
			HashMap<Long, String> NombreTipoOperacion = new HashMap<>();// relaciona cada IdUsoFlujoCustodia con un NombreTipoOperacion
			HashMap<Long, String> BusquedaEspecifica = new HashMap<>();// relaciona cada IdUsoFlujoCustodia con una BusquedaEspecifica
			
			for (ConfiguracionFlujoCustodia_Proceso o : configuraciones) {
				if (!(CalculoOrigen.containsKey(o.ItemCalculo))) {
					CalculoOrigen.put(o.ItemCalculo, o.ItemOrigen);
				}
				if (!(UsoTipoOperacion.containsKey(o.IdUsoFlujoCustodia))) {
					UsoTipoOperacion.put(o.IdUsoFlujoCustodia, o.UsoTipoOperacion);
				}
				if (!(NombreTipoOperacion.containsKey(o.IdUsoFlujoCustodia))) {
					NombreTipoOperacion.put(o.IdUsoFlujoCustodia, o.NombreTipoOperacion);
				}
				if (!(BusquedaEspecifica.containsKey(o.IdUsoFlujoCustodia))) {
					BusquedaEspecifica.put(o.IdUsoFlujoCustodia, o.BusquedaEspecifica);
				}
			}

			// Aqui se ve si hay discrepancias de los datos originales de la vista "ConsultarConfiguracionFlujoCustodia"
			// con los datos modificados que ingresaron como "in.p_ConfiguracionFlujoCustodia" desde la grilla en la interfaz web
			// Se actualizaran los datos en "configuraciones" y los datos no existentes se insertaran en lista.
			List<ConfiguracionFlujoCustodia_Proceso> lista = new ArrayList<ConfiguracionFlujoCustodia_Proceso>();
			long a = 0;
			for (int i = 0; i < in.p_ConfiguracionFlujoCustodia.size(); i++) {
				a = 0;
				for (int j = 0; j < configuraciones.size(); j++) {
					if (configuraciones.get(j).IdUsoFlujoCustodia.equals(in.p_ConfiguracionFlujoCustodia.get(i).IdUsoFlujoCustodia) && configuraciones.get(j).ItemCalculo.equals(in.p_ConfiguracionFlujoCustodia.get(i).Item)) {
						ConfiguracionFlujoCustodia_Proceso temp = new ConfiguracionFlujoCustodia_Proceso();
						temp.IdUsoFlujoCustodia = configuraciones.get(j).IdUsoFlujoCustodia;
						temp.IdEmpresa = configuraciones.get(j).IdEmpresa;
						temp.UsoTipoOperacion = configuraciones.get(j).UsoTipoOperacion;
						temp.IdClasificacionTipoInstrumento = configuraciones.get(j).IdClasificacionTipoInstrumento;
						temp.IdTipoInstrumento = configuraciones.get(j).IdTipoInstrumento;
						temp.IdInstrumento = configuraciones.get(j).IdInstrumento;
						temp.IdCliente = configuraciones.get(j).IdCliente;
						temp.IdPortafolio = configuraciones.get(j).IdPortafolio;
						temp.BusquedaEspecifica = configuraciones.get(j).BusquedaEspecifica;
						temp.FechaIngresoRegistro = configuraciones.get(j).FechaIngresoRegistro;
						temp.IdUsuario = configuraciones.get(j).IdUsuario;
						temp.IdEstadoUso = configuraciones.get(j).IdEstadoUso;
						temp.ItemCalculo = configuraciones.get(j).ItemCalculo;
						temp.ItemOrigen = configuraciones.get(j).ItemOrigen;
						temp.Descripcion = configuraciones.get(j).Descripcion;
						temp.FuncionOperador = in.p_ConfiguracionFlujoCustodia.get(i).FuncionOperador;
						temp.IdEstadoItem = configuraciones.get(j).IdEstadoItem;
						temp.EstadoUso = configuraciones.get(j).EstadoUso;
						temp.EstadoItem = configuraciones.get(j).EstadoItem;
						temp.NombreTipoOperacion = configuraciones.get(j).NombreTipoOperacion;
						configuraciones.set(j, temp);
						a = 1;
					}
					if (a == 0 && j == (configuraciones.size()) - 1) {
						ConfiguracionFlujoCustodia_Proceso temp = new ConfiguracionFlujoCustodia_Proceso();
						temp.IdUsoFlujoCustodia = in.p_ConfiguracionFlujoCustodia.get(i).IdUsoFlujoCustodia;
						temp.IdEmpresa = in.p_IdEmpresa;
						temp.UsoTipoOperacion = UsoTipoOperacion.get(in.p_ConfiguracionFlujoCustodia.get(i).IdUsoFlujoCustodia);
						temp.IdClasificacionTipoInstrumento = "";
						temp.IdTipoInstrumento = "";
						temp.IdInstrumento = 0L;
						temp.IdCliente = 0L;
						temp.IdPortafolio = 0L;
						temp.BusquedaEspecifica = BusquedaEspecifica.get(in.p_ConfiguracionFlujoCustodia.get(i).IdUsoFlujoCustodia);
						temp.FechaIngresoRegistro = Fecha.toDateTime(fechaHoraSistemaSecondate);
						temp.IdUsuario = in.p_IdUsuario;
						temp.IdEstadoUso = "ACT";
						temp.ItemCalculo = in.p_ConfiguracionFlujoCustodia.get(i).Item;
						temp.ItemOrigen = CalculoOrigen.get(in.p_ConfiguracionFlujoCustodia.get(i).Item);
						temp.Descripcion = "";
						temp.FuncionOperador = in.p_ConfiguracionFlujoCustodia.get(i).FuncionOperador;
						temp.IdEstadoItem = "ACT";
						temp.EstadoUso = "ACTIVO";
						temp.EstadoItem = "ACTIVO";
						temp.NombreTipoOperacion = NombreTipoOperacion.get(in.p_ConfiguracionFlujoCustodia.get(i).IdUsoFlujoCustodia);
						lista.add(temp);
					}
				}
			}
			
			// Actualiza la tabla BSCL.Custodia.BD::CUS.ItemsFlujoCustodia
			List<ConfiguracionFlujoCustodia_Proceso> datosItemsFlujoCustodia = new ArrayList<ConfiguracionFlujoCustodia_Proceso>();
			datosItemsFlujoCustodia.addAll(configuraciones);
			datosItemsFlujoCustodia.addAll(lista);
			
			sql.batch(conn, SQL_INSERT_UPDATE_CONFIGURACION, datosItemsFlujoCustodia, ConfiguracionFlujoCustodia_Proceso.class);
			
		}
		catch (Exception e) {
			salida.P_OUT_ERRORESCONTADOR = 1L;
			log.write(e);
			throw e;
		}
		
		return salida;
	}

	// TEST DE PRUEBA, EXCLUSIVAMENTE PARA PROBAR.
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":33,\"p_IdModulo\":\"PLA\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"N\",\"p_ConfiguracionFlujoCustodia\":[{\"Item\":\"INFORMACION_CARTERA\",\"IdUsoFlujoCustodia\":1,\"FuncionOperador\":\"+\"},{\"Item\":\"CARTERA_TRANSITO_COMPRA\",\"IdUsoFlujoCustodia\":1,\"FuncionOperador\":\"+\"},{\"Item\":\"CARTERA_TRANSITO_COMPRA\",\"IdUsoFlujoCustodia\":2,\"FuncionOperador\":\"0\"}]}";
			ActualizarConfiguracionFlujoCustodia_Entrada Objin = gson.fromJson(json, ActualizarConfiguracionFlujoCustodia_Entrada.class);
			
			ActualizarConfiguracionFlujoCustodia demo = new ActualizarConfiguracionFlujoCustodia();
			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			try {
 				ActualizarConfiguracionFlujoCustodia_Salida ObjOut = demo.logica(con, Objin);
 				//con.commit();
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				//er.printStackTrace();	
				con.rollback();
			}
 			//con.commit();
			finally {
				con.close();
			}
		}
		System.out.println("== END ==");
	}
}
