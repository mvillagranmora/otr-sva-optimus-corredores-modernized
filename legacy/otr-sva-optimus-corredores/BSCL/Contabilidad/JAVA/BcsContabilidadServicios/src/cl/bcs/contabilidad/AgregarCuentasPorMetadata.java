package cl.bcs.contabilidad;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class AgregarCuentasPorMetadata implements JavaCall {
	
	private final Log log = new Log("CON.AgregarCuentasPorMetadata");
	
	private final String SQL_CUENTAS_FILTROS = loadResourceToString("cl/bcs/contabilidad/sql/AgregarCuentasPorMetadata_SelectCuentaFiltros.sql");
	private final String SQL_ID_DATOS_CUENTA_CONTABLE = loadResourceToString("cl/bcs/contabilidad/sql/AgregarCuentasPorMetadata_SelectIdCuentaContable.sql");
	private final String SQL_SELECT_ULTIMO_ORDEN = loadResourceToString("cl/bcs/contabilidad/sql/AgregarCuentasPorMetadata_SelectUltimoOrden.sql");
	private final String SQL_INSERT_CONTABILIDAD_DEFINICION_ASIENTO = loadResourceToString("cl/bcs/contabilidad/sql/AgregarCuentasPorMetadata_InsertContabilidadDefinicionAsiento.sql");
	private final String SQL_INSERT_CONTABILIDAD_DEFINICION_TIPO_EVENTO = loadResourceToString("cl/bcs/contabilidad/sql/AgregarCuentasPorMetadata_InsertContabilidadDefinicionTipoEvento.sql");
	private final String SQL_INSERT_CONTABILIDAD_TIPO_ASIENTO_CONDICION = loadResourceToString("cl/bcs/contabilidad/sql/AgregarCuentasPorMetadata_InsertContabilidadTipoAsientoCondicion.sql");
	
	public static class InAgregarCuentasPorMetadata{
		public Long 	p_IdEmpresa;
		public Long 	p_IdUsuario;
		public String 	p_IdModulo;
		public Long 	p_IdMetadataCuentaContable;
		public List<Evento> p_Eventos;
		public String p_DebeHaber;
		public String p_Ejercicio;
		public String p_Formato;
		public Long p_IdTipoContabilidad;
		public List<CuentasFiltros> p_CuentasFiltros;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class OutAgregarCuentasPorMetadata{
		public String P_OUT_MENSAJE;
		public Long P_OUT_ERRORESCONTADOR;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CuentasFiltros{
		public String CuentaOptimus;
		public String IdTipoDatoEvento;
		public String Condicion;
		public String Valor;
	}
		
	public static class CuentasContableInsert{
		public Long IdDefinicionAsiento;
		public Long IdTipoContabilidad;
		public String IdCuentaContable;
		public String Ejercicio;
		public Long Orden;
		public String DebeHaber;
		public String TipoEvento;
		public String Condicionado;
		public String OmiteCondicionCmbte;
		public String Estado;
		public String Formato;
		public Long IdMetadataCuentaContable;
	}
	
	public static class Evento{
		public String IdTipoEvento;
		public String Operador;
	}
	
	public static class EventoInsert{
		public String IdTipoEvento;
		public Long IdDefinicionAsiento;
		public String Operador;
		public String Estado;
	}
	
	public static class Condicion{
		public String IdTipoDatoEvento;
		public String CuentaOptimus;
		public String Valor;
		public String Condicion;
		public String EsTodos;
	}
	
	public static class CondicionInsert{
		public Long IdDefinicionAsiento;
		public Long IdTipoContabilidad;
		public String IdTipoDatoEvento;
		public String ValorVarchar;
		public BigDecimal ValorDecimal;
		public String Condicion;
		public String Conector;
		public String TipoCondicion;
		public String Estado;
		public String OmiteCondicionCmbte;
	}
	
	public static class Orden{
		public Long Orden;
		public Long Index;
	}
	
	public static class Id{
		public Long Id;
	}
	
	public static class Max{
		public Long Max = 0L;
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();	
		InAgregarCuentasPorMetadata in=gson.fromJson(jin.toString() ,InAgregarCuentasPorMetadata.class);
		OutAgregarCuentasPorMetadata  out= logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	}
	
	/**********************
	 * lógica del programa
	 **********************/
	public  OutAgregarCuentasPorMetadata logica(Connection con, InAgregarCuentasPorMetadata entrada) throws Exception {
		OutAgregarCuentasPorMetadata out = new OutAgregarCuentasPorMetadata();
		out.P_OUT_ERRORESCONTADOR = 0L;
		try{
			SQL sql=new SQL();
			HashMap<String,Object> params = new HashMap<String,Object>();
			List<CuentasFiltros> cuentasFiltros = entrada.p_CuentasFiltros;
			if(cuentasFiltros == null || cuentasFiltros.size() == 0){
				params.put("IdMetadataCuentaContable", entrada.p_IdMetadataCuentaContable);
				cuentasFiltros = sql.query(con,SQL_CUENTAS_FILTROS,params,CuentasFiltros.class);
			}
			params.clear();
			params.put("IdTipoContabilidad", entrada.p_IdTipoContabilidad);
			List<Max> selectOrden = sql.query(con,SQL_SELECT_ULTIMO_ORDEN,params,Max.class);
			Long maxOrden = selectOrden.size() == 0 || selectOrden.get(0).Max == null ? 0L : selectOrden.get(0).Max;
			List<CuentasContableInsert> cuentasContableInsert = new ArrayList<CuentasContableInsert>();
			List<String> cuentas = new ArrayList<String>();
			int index = 1;
			for(CuentasFiltros cuentaFiltros : cuentasFiltros){
				if(!cuentas.contains(cuentaFiltros.CuentaOptimus)){
					CuentasContableInsert cuentaContable = new CuentasContableInsert();
					params.clear();
					List<Id> selectId = sql.query(con,SQL_ID_DATOS_CUENTA_CONTABLE,params,Id.class);
					cuentaContable.IdDefinicionAsiento = selectId.get(0).Id;
					cuentaContable.IdTipoContabilidad = entrada.p_IdTipoContabilidad;
					cuentaContable.IdCuentaContable = cuentaFiltros.CuentaOptimus;
					cuentaContable.Ejercicio = entrada.p_Ejercicio;
					cuentaContable.Orden = maxOrden + index;
					cuentaContable.DebeHaber = entrada.p_DebeHaber;
					cuentaContable.TipoEvento = entrada.p_Eventos.size() > 1 ? "C" : "S";
					cuentaContable.Condicionado = "S";
					cuentaContable.OmiteCondicionCmbte = "N";
					cuentaContable.Estado = "V";
					cuentaContable.Formato = entrada.p_Formato;
					cuentaContable.IdMetadataCuentaContable = entrada.p_IdMetadataCuentaContable;
					cuentasContableInsert.add(cuentaContable);
					cuentas.add(cuentaFiltros.CuentaOptimus);
					index++;
				}
			}
			
			List<EventoInsert> eventos = new ArrayList<EventoInsert>();
			for(Evento evento : entrada.p_Eventos){
				for(CuentasContableInsert cuenta : cuentasContableInsert){
					EventoInsert eventoInsert = new EventoInsert();
					eventoInsert.IdDefinicionAsiento = cuenta.IdDefinicionAsiento;
					eventoInsert.IdTipoEvento = evento.IdTipoEvento;
					eventoInsert.Operador = evento.Operador;
					eventoInsert.Estado = "V";
					eventos.add(eventoInsert);
				}
			}
			
			List<CondicionInsert> condicionesInsert = new ArrayList<CondicionInsert>();
			
			for(CuentasContableInsert cuenta : cuentasContableInsert){
				for(CuentasFiltros cuentaFiltros : cuentasFiltros){
					if(cuentaFiltros.CuentaOptimus.equalsIgnoreCase(cuenta.IdCuentaContable)){
						CondicionInsert condicionInsert = new CondicionInsert();
						condicionInsert.IdDefinicionAsiento = cuenta.IdDefinicionAsiento;
						condicionInsert.IdTipoContabilidad = 0L;
						condicionInsert.IdTipoDatoEvento = cuentaFiltros.IdTipoDatoEvento.trim();
						condicionInsert.ValorVarchar = cuentaFiltros.Valor.trim();
						try{
							condicionInsert.ValorDecimal = new BigDecimal(cuentaFiltros.Valor);
						}catch(Exception e){
							condicionInsert.ValorDecimal = BigDecimal.ZERO;
						}
						condicionInsert.Condicion = cuentaFiltros.Condicion;
						condicionInsert.Conector = "AND";
						condicionInsert.TipoCondicion = "A";
						condicionInsert.Estado = "V";
						condicionInsert.OmiteCondicionCmbte = "";
						condicionesInsert.add(condicionInsert);
					}
				}
			}
			
		
			sql.batch(con, SQL_INSERT_CONTABILIDAD_DEFINICION_ASIENTO, cuentasContableInsert, CuentasContableInsert.class);
			sql.batch(con, SQL_INSERT_CONTABILIDAD_DEFINICION_TIPO_EVENTO, eventos, EventoInsert.class);
			sql.batch(con, SQL_INSERT_CONTABILIDAD_TIPO_ASIENTO_CONDICION, condicionesInsert, CondicionInsert.class);
			
			out.P_OUT_MENSAJE = "UI_EXITO";
		} catch(Exception e){
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJE = "UI_ERROR";
			log.write("Error",e);
		}
		return out;
	}
	
	
	public static void main (String [] args) throws Exception {
  		try (Connection con = PlataformaUtils.getDESAConnection()){
  				Gson gson = new GsonBuilder().create();
  				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"CON\",\"p_IdMetadataCuentaContable\":13,\"p_Eventos\":[{\"IdTipoEvento\":\"MONTO_COBRO\",\"Operador\":\"+\"}],\"p_DebeHaber\":\"D\",\"p_Ejercicio\":\"2018\",\"p_Formato\":\"D\",\"p_IdTipoContabilidad\":326}";
  				InAgregarCuentasPorMetadata entrada = gson.fromJson(json, InAgregarCuentasPorMetadata.class);
				JsonParser parser 		= new JsonParser();
				JsonObject dataJsonIn	= parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
				AgregarCuentasPorMetadata demo= new AgregarCuentasPorMetadata();
				System.out.println(demo.call(con, dataJsonIn).toString());
				con.commit();
  			}
	}

	
}
