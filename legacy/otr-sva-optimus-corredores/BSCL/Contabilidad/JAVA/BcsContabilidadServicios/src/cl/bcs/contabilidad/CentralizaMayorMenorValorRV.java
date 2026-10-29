package cl.bcs.contabilidad;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import cl.bcs.contabilidad.pojo.TabDatosMovimiento;
import cl.bcs.contabilidad.pojo.TabEventos;
import cl.bcs.contabilidad.pojo.TabMovimientos;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.Log;
import cl.bcs.utils.Utils;
import cl.bcs.utils.SQL;


public class CentralizaMayorMenorValorRV implements BcsCentralizacion {
	private final Log log = new Log("CON.CentralizaMayorMenorValor");
	
	public static class MovimientosMayorMenorValor{		
		public Long IdMovimiento;
		public Long FolioOperacion;
		public String DocumentoIdentidad;
		public String Portafolio;
		public String EsCuentaPropia;
		public String EsRelacionado;
		public String TipoCartera;
		public Date FechaOperacion;
		public Date FechaVencimientoOperacion;
		public Date FechaLiquidacion;
		public String ClaseInstrumento;
		public String TipoInstrumento;
		public String Nemo;
		public String Boveda;
		public String Emisor;
		public String TipoEmisor;
		public String TipoReajuste;
		public Date FechaVencimientoInstrumento;
		public String CodigoIFRS;
		public String Moneda;
		public String ClaseEmisor;		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	private final String SQL_SELECT_MOVIMIENTOS = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaMayorMenorValorRV_SelectMovimientos.sql");
	private final String SQL_SELECT_EVENTOS = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaMayorMenorValorRV_SelectEventos.sql");
	private final String SQL_SELECT_DATOS_MOVIMIENTOS = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaMayorMenorValorRV_SelectDatosMovimiento.sql");
	
	@Override
	public SalidaCentralizacion centralizar(Connection con, EntradaCentralizacion entrada, EntradaCentralizacionInfoAdicional infoAdicional) throws Exception {
		log.write("INICIO::CentralizaMayorMenorValorRV");
		SQL sql = new SQL();
		SalidaCentralizacion out = new SalidaCentralizacion();		
		
		//MOVIMIENTOS
		HashMap<String,Object> params = new HashMap<String,Object>(5);
		params.put("p_IdEmpresa", entrada.p_IdEmpresa);
		params.put("p_FechaContable", entrada.p_FechaContable);
		params.put("p_IdTipoCentralizacion", entrada.p_IdTipoCentralizacion);
		params.put("p_IdInforme", entrada.p_IdInforme);
		out.P_TABMOVIMIENTOS = sql.query(con, SQL_SELECT_MOVIMIENTOS, params, TabMovimientos.class);
		params.clear();
		
		//EVENTOS
		params.put("p_IdEmpresa", entrada.p_IdEmpresa);
		params.put("p_IdTipoCentralizacion", entrada.p_IdTipoCentralizacion);
		params.put("p_IdInforme", entrada.p_IdInforme);
		out.P_TABEVENTOS = sql.query(con, SQL_SELECT_EVENTOS, params, TabEventos.class);
		params.clear();
		
		//MOVIMIENTO DATOS
		params.put("p_IdEmpresa", entrada.p_IdEmpresa);
		params.put("p_DocumentoIdentidad", infoAdicional.p_Empresa.DocumentoIdentidad);
		params.put("p_IdInforme", entrada.p_IdInforme);
		List<MovimientosMayorMenorValor> tabDatosMovimientoPaso = sql.query(con, SQL_SELECT_DATOS_MOVIMIENTOS, params, MovimientosMayorMenorValor.class);
		params.clear();
		
		tabDatosMovimientoPaso.forEach(mov ->{
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "DocumentoIdentidad", mov.DocumentoIdentidad, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Portafolio", mov.Portafolio, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "EsCuentaPropia", String.valueOf(mov.EsCuentaPropia), BigDecimal.ZERO)); 
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "EsRelacionado", String.valueOf(mov.EsRelacionado), BigDecimal.ZERO)); 
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "TipoCartera", mov.TipoCartera, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "FechaOperacion", Fecha.stringToSqlDate(mov.FechaOperacion), BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "FechaVencimientoOperacion", mov.FechaVencimientoOperacion.toString(), BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "FechaLiquidacion", Fecha.stringToSqlDate(mov.FechaLiquidacion), BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "FolioOperacion", String.valueOf(mov.FolioOperacion), BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "ClaseInstrumento", mov.ClaseInstrumento, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "TipoInstrumento", mov.TipoInstrumento, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Nemotecnico", mov.Nemo, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Boveda", mov.Boveda, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Emisor", mov.Emisor, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "TipoEmisor", mov.TipoEmisor, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "IdTipoReajuste", mov.TipoReajuste, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "FechaVencimientoInstrumento", mov.FechaVencimientoInstrumento.toString(), BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "CodigoIFRS", mov.CodigoIFRS, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Moneda", mov.Moneda, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "ClaseEmisor", mov.ClaseEmisor.toString(), BigDecimal.ZERO));
		});
		
		out.P_OUT_RESULTADO = llenarResultado(out.P_TABMOVIMIENTOS);
		log.write("FIN::CentralizaMayorMenorValorRV");
		return out;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getMASTER01Connection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();			
			
			String Json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdModulo\":\"CON\",\"p_IdTipoCentralizacion\":406,\"p_FechaContable\":\"2024-04-10\",\"p_Ejercicio\":2024,\"p_SimulaOperacion\":\"N\"}";
			
			EntradaCentralizacion Objin = gson.fromJson(Json, EntradaCentralizacion.class);
			CentralizaMayorMenorValorRV  demo=new CentralizaMayorMenorValorRV();				
			try {
				SalidaCentralizacion ObjOut=demo.centralizar(con, Objin, null);
				con.commit();
				System.out.println(ObjOut.toString());
			}
			catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}
						
    	}
		System.out.println("== END ==");
	}
}
