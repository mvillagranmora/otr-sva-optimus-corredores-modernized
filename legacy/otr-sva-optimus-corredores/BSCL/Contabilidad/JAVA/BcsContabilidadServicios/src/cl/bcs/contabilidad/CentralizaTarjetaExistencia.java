package cl.bcs.contabilidad;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;

import cl.bcs.contabilidad.pojo.TabDatosMovimiento;
import cl.bcs.contabilidad.pojo.TabEventos;
import cl.bcs.contabilidad.pojo.TabMovimientos;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CentralizaTarjetaExistencia implements BcsCentralizacion {

	private final String SQL_SELECT_MOVIMIENTOS = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaTarjetaExistencia_SelectMovimientos.sql");
	private final String SQL_SELECT_EVENTOS = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaTarjetaExistencia_SelectEventos.sql");
	private final String SQL_SELECT_DATOS_MOVIMIENTOS = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaTarjetaExistencia_SelectDatosMovimiento.sql");
	
	private final Log log = new Log("CON.CentralizaTarjetaExistencia");
	
	public static class MovimientoPasoAnticipoPacto{
		public Long IdMovimiento;
		public String DocumentoIdentidad;
		public String NombreCliente;
		public String PortafolioCliente;
		public String Moneda;
		public String Producto;
		public String ClaseInstrumento;
		public String TipoInstrumento;
		public String Nemo;
		//public String Boveda;
		public Long IdCartera;
		public String IdEmisor;
		public String IdTipoReajuste;
		//public String TipoEmisor;
		public String CodigoIFRS;
		public String TipoCartera;
		public String ClaseEmisor;
		public String Agente;
		public String NombreAgente;
		public String SucursalAgente;
		public String NombreSucursalAgente;
		public String CentroCostoAgente;
		public String Relacion;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		
	}
	
	@Override
	public SalidaCentralizacion centralizar(Connection con, EntradaCentralizacion entrada, EntradaCentralizacionInfoAdicional infoAdicional) throws Exception {
		log.write("INICIO::CentralizaTarjetaExistencia");
		SQL sql = new SQL();
		SalidaCentralizacion out = new SalidaCentralizacion();

		//MOVIMIENTOS
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		params.put("FechaContable", entrada.p_FechaContable);
		params.put("IdTipoCentralizacion", entrada.p_IdTipoCentralizacion);
		out.P_TABMOVIMIENTOS = sql.query(con, SQL_SELECT_MOVIMIENTOS, params, TabMovimientos.class);
		params.clear();
		
		//EVENTOS
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		params.put("FechaContable", entrada.p_FechaContable);
		params.put("IdTipoCentralizacion", entrada.p_IdTipoCentralizacion);
		out.P_TABEVENTOS = sql.query(con, SQL_SELECT_EVENTOS, params, TabEventos.class);
		params.clear();
		
		//MOVIMIENTO DATOS
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		params.put("FechaContable", entrada.p_FechaContable);
		List<MovimientoPasoAnticipoPacto> tabDatosMovimientoPaso = sql.query(con, SQL_SELECT_DATOS_MOVIMIENTOS, params, MovimientoPasoAnticipoPacto.class);
		params.clear();
		
		tabDatosMovimientoPaso.forEach(mov ->{
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "IdMovimiento", String.valueOf(mov.IdMovimiento), BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "DocumentoIdentidad", mov.DocumentoIdentidad, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "NombreCliente", mov.NombreCliente, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "PortafolioCliente", mov.PortafolioCliente, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Moneda", mov.Moneda, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Relacion", mov.Relacion, BigDecimal.ZERO));
//			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Producto", mov.Producto, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "ClaseInstrumento", mov.ClaseInstrumento, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "TipoInstrumento", mov.TipoInstrumento, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Nemo", mov.Nemo, BigDecimal.ZERO));
//			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Boveda", mov.Boveda, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "IdCartera", "", BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "IdEmisor", mov.IdEmisor, BigDecimal.ZERO));
//			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "IdTipoReajuste", mov.IdTipoReajuste, BigDecimal.ZERO));
//			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "TipoEmisor", mov.TipoEmisor, BigDecimal.ZERO));
//			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "CodigoIFRS", mov.CodigoIFRS, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "TipoCartera", mov.TipoCartera, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "ClaseEmisor", mov.ClaseEmisor, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Agente", mov.Agente, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "NombreAgente", mov.NombreAgente, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "SucursalAgente", mov.SucursalAgente, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "NombreSucursalAgente", mov.NombreSucursalAgente, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "CentroCostoAgente", mov.CentroCostoAgente, BigDecimal.ZERO));
		});
		
		out.P_OUT_RESULTADO = llenarResultado(out.P_TABMOVIMIENTOS);
		
		return out;
	}

}
