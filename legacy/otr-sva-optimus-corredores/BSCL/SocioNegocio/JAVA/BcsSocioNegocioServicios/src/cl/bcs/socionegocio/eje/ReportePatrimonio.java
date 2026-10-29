package cl.bcs.socionegocio.eje;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.Set;

public class ReportePatrimonio implements JavaCall {

	private final String SQL_CONSULTA_DATOS   = loadResourceToString("cl/bcs/socionegocio/eje/sql/ReportePatrimonio.sql");
	
	public static class ReportePatrimonio_IN {
		public Date p_FechaConsulta;		
		public long p_IdEmpresa;
		public long p_IdAgente;
		public long p_IdUsuario;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class Cartolahistorica {
		public Long	  		IdCartolaHistorica;
		public Long   		IdCliente;
		public String 		NombreCliente;
		public Long   		IdAgente;
		public String 		NombreAgente;
		public Long   		IdMercadoCartola;
		public String 		NombreMercadoCartola;
		public String 		IndiceMercado;
		public BigDecimal 	MontoTotalMercado;
		public BigDecimal 	PorcentajeTotalMercado;
		public Integer suma;
		public static Object stream() {
			// TODO Auto-generated method stub
			return null;
		}
		public Long getIdCliente() {
		    return IdCliente;
		 }
		public String getNameCliente() {
		    return NombreCliente;
		 }
		public String getNombreMercadoCartola() {
		    return NombreMercadoCartola;
		 }
		public Long getIdMercadoCartola() {
		    return IdMercadoCartola;
		 }
		
		public BigDecimal getSumaMercado() {
		    return  MontoTotalMercado;
		 }
		public TablaMercados getMercado(){
			TablaMercados tabla =new TablaMercados();
			tabla.MontoTotalMercado = MontoTotalMercado;
			tabla.NombreMercadoCartola = NombreMercadoCartola;
			return tabla;
		}
	}
	
	public static class Mercado{
		 public Long       IdMercadoCartola;
		 public String     NombreMercadoCartola;  
		 public String     IdNombreMercadoCartola;
	}
	
	public static class ReportePatrimonio_OUT {

		public List<TablaSalida>    P_OUT_TBL;
		public List<Mercado>        P_OUT_MERCADOS;
		public Long                 P_OUT_ERRORESCONTADOR = 0L;
		
		public ReportePatrimonio_OUT(){
			this.P_OUT_TBL      =  new ArrayList<TablaSalida>();
			this.P_OUT_MERCADOS =  new ArrayList<Mercado>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class TablaMercados{
		public String    NombreMercadoCartola;
		public BigDecimal MontoTotalMercado;
		public String toString() {
			return Utils.printJson(this, false);
		}

	}
	public static class SumaTotalMercado{
		
		public BigDecimal MontoTotalMercado;
		public String toString() {
			return Utils.printJson(this, false);
		}
	
	}
	public static class TablaSalida{
		public Long       			IdCliente;
		public String     			NombreCliente;
		public String     			Agente;
		public List<TablaMercados> ListaMercadoCartola;
		public BigDecimal 			TotalGeneral;

		public String toString() {
			return Utils.printJson(this, false);
		}
	
	}

	public ReportePatrimonio_OUT logica(Connection conn, ReportePatrimonio_IN in) throws Exception {
		//**REPORTE PATRIMONIO**
		ReportePatrimonio_OUT out = new ReportePatrimonio_OUT();
		SQL sql = new SQL();
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.clear();
		System.out.println(in.p_IdAgente);
		params.put("p_IdAgente", in.p_IdAgente);
		params.put("p_FechaConsulta", in.p_FechaConsulta);
		List<Cartolahistorica> reportePatrimonio = sql.query(conn, SQL_CONSULTA_DATOS, params, Cartolahistorica.class);

		//--------------------------------------------------------------------------------------------------------------------------------------
		//Optiene los clientes
        Map<Long, Long> contClientes 		= reportePatrimonio.stream().collect(Collectors.groupingBy(Cartolahistorica::getIdCliente, Collectors.counting()));
        //suma los totales de mercados por clientes
		Map<Long, BigDecimal> sumaCliente 	= reportePatrimonio.stream().collect(Collectors.groupingBy(Cartolahistorica::getIdCliente, Collectors.reducing(BigDecimal.ZERO, Cartolahistorica::getSumaMercado, BigDecimal::add)));
		//Mapea los mercados por clientes 
		Map<Long, Set<TablaMercados>> mercadosColl 	= reportePatrimonio.stream().collect(Collectors.groupingBy(Cartolahistorica::getIdCliente, Collectors.mapping(Cartolahistorica::getMercado, Collectors.toSet())));
		//Optiene los diferentes mercados 
        Map<String, Long> contMercados 		= reportePatrimonio.stream().distinct().collect(Collectors.groupingBy(Cartolahistorica::getNombreMercadoCartola, Collectors.counting()));
        System.out.println("+Clientes--->"+contClientes);
        System.out.println("+--->"+sumaCliente);
        System.out.println("+--->"+mercadosColl);
        System.out.println("+--->"+contMercados);
        //--------------------------------------------------------------------------------------------------------------------------------------

  		Set<String> mapKeys = contMercados.keySet();
  		List<Mercado> mercado = new ArrayList<>();
  		for (String key : mapKeys) {
  			
  			Mercado ms = new Mercado();
  			ms.IdMercadoCartola = contMercados.get(key);
  			ms.NombreMercadoCartola = key.replaceAll(" ", "_");
  			ms.NombreMercadoCartola = ms.NombreMercadoCartola != null? "UI_".concat(ms.NombreMercadoCartola) : ms.NombreMercadoCartola;
  			mercado.add(ms);
        }
  		out.P_OUT_MERCADOS.addAll(mercado);
  		System.out.println("+--->"+mercado);
  		
      	//-------------------------------------------------------------------	
		Long contador = (long) 0;
		Long ultimoReg = (long) reportePatrimonio.size();
		
		List<TablaSalida> salida = new ArrayList<>();		
		for(Cartolahistorica datoCartola: reportePatrimonio){ //Recorre la data
			List<TablaMercados> listaMercados = new ArrayList<>();
			contador ++;
			TablaSalida salidaTabla = new TablaSalida();	
			salidaTabla.NombreCliente          = datoCartola.NombreCliente;              //parametro
			salidaTabla.IdCliente              = datoCartola.IdCliente;                  //parametro
			salidaTabla.Agente                 = datoCartola.NombreAgente;               //parametro
			salidaTabla.TotalGeneral 		   = sumaCliente.get(datoCartola.IdCliente); //parametro			

			if(contClientes.get(datoCartola.IdCliente).equals(contador) ||  (ultimoReg.equals(contador))) {	
				Set<TablaMercados> me =  mercadosColl.get(datoCartola.IdCliente);
				me.forEach(a ->{
					TablaMercados tablaMercado =  new TablaMercados();
					tablaMercado.NombreMercadoCartola = a.NombreMercadoCartola.replaceAll(" ", "_");
					tablaMercado.NombreMercadoCartola = tablaMercado.NombreMercadoCartola != null? "UI_".concat(tablaMercado.NombreMercadoCartola) : tablaMercado.NombreMercadoCartola;
					tablaMercado.MontoTotalMercado = a.MontoTotalMercado;
					listaMercados.add(tablaMercado);
				});
				salidaTabla.ListaMercadoCartola    = listaMercados;
				salida.add(salidaTabla);
				contador=(long) 0;
			}		
		}		
		out.P_OUT_TBL.addAll(salida);
		return out;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		ReportePatrimonio_IN jin = gson.fromJson(in.toString(), ReportePatrimonio_IN.class);
		ReportePatrimonio_OUT out = logica(conn, jin);
		JsonParser parser = new JsonParser();
		JsonObject dataOut = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataOut;
	};

	public static void main(String[] args) throws Exception, SQLException { 
		Connection conn = null;
		Gson gson = new Gson();
		try {
			System.out.println("== Inicio Reporte Patrimonio ==");
			conn = PlataformaUtils.getDESAConnection();
			String json = "{\"p_IdEmpresa\":1,\"p_IdAgente\":11496,\"p_IdUsuario\":109,\"p_FechaConsulta\":\"2019-08-23\"}";
			ReportePatrimonio_IN objIn = gson.fromJson(json, ReportePatrimonio_IN.class);
			ReportePatrimonio demo = new ReportePatrimonio();
			ReportePatrimonio_OUT ObjOut = demo.logica(conn, objIn);
			System.out.println(ObjOut.toString());
			conn.rollback();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			conn.close();
		}
		System.out.println("== Final Reporte Patrimonio ==");

	};
}
