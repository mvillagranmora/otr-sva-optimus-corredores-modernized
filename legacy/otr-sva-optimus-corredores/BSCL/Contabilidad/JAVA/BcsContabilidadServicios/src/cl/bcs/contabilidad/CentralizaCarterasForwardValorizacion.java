package cl.bcs.contabilidad;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cl.bcs.contabilidad.pojo.TabDatosMovimiento;
import cl.bcs.contabilidad.pojo.TabEventos;
import cl.bcs.contabilidad.pojo.TabMovimientos;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior.InObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior.OutObtenerDiaAnterior;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CentralizaCarterasForwardValorizacion implements BcsCentralizacion{
	
	private final Log log = new Log("CON.CentralizaCarterasForwardValorizacion");

	private final String SQL_SELECT_MOVIMIENTOS = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaCarterasForwardValorizacion_SelectMovimientos.sql");
	private final String SQL_SELECT_MOVIMIENTOS_HIS = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaCarterasForwardValorizacion_SelectMovimientosHis.sql");
	private final String SQL_SELECT_MONTO_EVENTOS = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaCarterasForwardValorizacion_SelectMontoEventos.sql");
	private final String SQL_SELECT_MONTO_EVENTOS_HIS = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaCarterasForwardValorizacion_SelectMontoEventosHis.sql");
	private final String SQL_SELECT_EVENTOS = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaCarterasForwardValorizacion_SelectEventos.sql");
	private final String SQL_SELECT_EVENTOS_HIS = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaCarterasForwardValorizacion_SelectEventosHis.sql");
	private final String SQL_SELECT_DATOS_MOVIMIENTOS = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaCarterasForwardValorizacion_SelectDatosMovimiento.sql");
	private final String SQL_SELECT_DATOS_MOVIMIENTOS_HIS = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaCarterasForwardValorizacion_SelectDatosMovimientoHis.sql");
	
	public static class TabDatosMovimientoPaso{
		public Long IdMovimiento;
		public String DocumentoIdentidad;
		public String NombreCliente;
		public String Portafolio;
		public String MonedaPrincipal;
		public String MonedaSecundaria;
//		public String EsRelacionado;
		public String ModalidaPago;
//		public String EsCuentaPropia;
		public String Producto;
		public String ClaseInstrumento;
		public String TipoInstrumento;
		public String TipoOperacion;
		public String Nemo;
		public String Agente;
		public String NombreAgente;
		public String SucursalAgente;
		public String NombreSucursalAgente;
		public String CentroCostoAgente;
		public String Relacion;
		public String Bolsa;
		public BigDecimal TasaCambio;
		public String TipoPersona;
		public String Concepto;
		public Date FechaLiquidacion;
		public Date FechaOperacion;
		public String CompraVenta;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class MontoEventos{
		public Long IdCartera;
		public String IdMonedaPrincipal;
		public BigDecimal RazonableActivo;
		public BigDecimal RazonablePasivo;
		public BigDecimal RazonableNeto;
		public BigDecimal RazonableActivoAnterior;
		public BigDecimal RazonablePasivoAnterior;
		public BigDecimal RazonableNetoAnterior;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public SalidaCentralizacion centralizar(Connection con, EntradaCentralizacion entrada, EntradaCentralizacionInfoAdicional infoAdicional) throws Exception {
		log.write("INICIO::CentralizaCarterasForwardValorizacion");
		
		SQL sql = new SQL();
		SalidaCentralizacion out = new SalidaCentralizacion();
		
		String idMonedaSistema = infoAdicional.p_IdMonedaSistema;
		String fechaSistema = infoAdicional.p_FechaSistema;
		InformacionEmpresa empresa = infoAdicional.p_Empresa;
		Long idPaisEmpresa = empresa.IdPais;
		
		InObtenerDiaAnterior inObtenerDiaAnterior = new InObtenerDiaAnterior();
		ObtenerDiaAnterior obtenerDiaAnterior = new ObtenerDiaAnterior();
		InObtenerDiaAnterior.FechasIn fechasIn = new InObtenerDiaAnterior.FechasIn();
		fechasIn.fechaInicial = entrada.p_FechaContable;
		fechasIn.numeroDias = 1L;
		inObtenerDiaAnterior.fechas.add(fechasIn);
		inObtenerDiaAnterior.idPais = idPaisEmpresa;
		inObtenerDiaAnterior.diaHabil = "S";
		
		List<MontoEventos> montoEventos = new ArrayList<MontoEventos>();
		List<TabDatosMovimientoPaso> tabDatosMovimientoPaso = new ArrayList<TabDatosMovimientoPaso>();
		
		OutObtenerDiaAnterior outObtenerDiaAnterior = obtenerDiaAnterior.logica(con, inObtenerDiaAnterior);
		
		String fechaAnterior = outObtenerDiaAnterior.fechas.get(0).fechaCalculada;
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		params.put("IdTipoCentralizacion", entrada.p_IdTipoCentralizacion);
		params.put("FechaContable", entrada.p_FechaContable);

		if(fechaSistema.equals(entrada.p_FechaContable)) {
			out.P_TABMOVIMIENTOS.addAll(sql.query(con, SQL_SELECT_MOVIMIENTOS, params, TabMovimientos.class));
		}else {
			out.P_TABMOVIMIENTOS.addAll(sql.query(con, SQL_SELECT_MOVIMIENTOS_HIS, params, TabMovimientos.class));
		}
		params.clear();
		
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		params.put("IdTipoCentralizacion", entrada.p_IdTipoCentralizacion);
		params.put("FechaContable", entrada.p_FechaContable);
		params.put("IdMonedaSistema", idMonedaSistema);
		if(fechaSistema.equals(entrada.p_FechaContable)) {
			out.P_TABEVENTOS.addAll(sql.query(con, SQL_SELECT_EVENTOS, params, TabEventos.class));
		}else {
			out.P_TABEVENTOS.addAll(sql.query(con, SQL_SELECT_EVENTOS_HIS, params, TabEventos.class));
		}
		params.clear();
		
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		params.put("FechaContable", entrada.p_FechaContable);
		params.put("FechaAnteriorSistema", fechaAnterior);
		if(fechaSistema.equals(entrada.p_FechaContable)) {
			montoEventos = sql.query(con, SQL_SELECT_MONTO_EVENTOS, params, MontoEventos.class);
		}else {
			montoEventos = sql.query(con, SQL_SELECT_MONTO_EVENTOS_HIS, params, MontoEventos.class);
		}
		params.clear();
		
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		params.put("FechaContable", entrada.p_FechaContable);
		if(fechaSistema.equals(entrada.p_FechaContable)) {
			tabDatosMovimientoPaso =  sql.query(con, SQL_SELECT_DATOS_MOVIMIENTOS, params, TabDatosMovimientoPaso.class);
		}else {
			tabDatosMovimientoPaso =  sql.query(con, SQL_SELECT_DATOS_MOVIMIENTOS_HIS, params, TabDatosMovimientoPaso.class);
		}
		
		for(MontoEventos mvto : montoEventos){
			out.P_TABEVENTOS.add(new TabEventos(entrada.p_IdTipoCentralizacion, mvto.IdCartera, "VALOR_RAZONABLE_ACTIVO", "Valor a Razonable Activo", mvto.RazonableActivo, idMonedaSistema));
			out.P_TABEVENTOS.add(new TabEventos(entrada.p_IdTipoCentralizacion, mvto.IdCartera, "VALOR_RAZONABLE_PASIVO", "Valor a Razonable Pasivo", mvto.RazonablePasivo, idMonedaSistema));
			if(BigDecimal.ZERO.compareTo(mvto.RazonableNeto) == 1){
				out.P_TABEVENTOS.add(new TabEventos(entrada.p_IdTipoCentralizacion, mvto.IdCartera, "VALOR_RAZONABLE_PERDIDA", "Valor a Razonable Perdida", mvto.RazonableNeto.abs(MathContext.DECIMAL128), idMonedaSistema));
			}
			if(BigDecimal.ZERO.compareTo(mvto.RazonableNeto) == -1){
				out.P_TABEVENTOS.add(new TabEventos(entrada.p_IdTipoCentralizacion, mvto.IdCartera, "VALOR_RAZONABLE_UTILIDAD", "Valor a Razonable Utilidad", mvto.RazonableNeto.abs(MathContext.DECIMAL128), idMonedaSistema));
			}

			out.P_TABEVENTOS.add(new TabEventos(entrada.p_IdTipoCentralizacion, mvto.IdCartera, "VALOR_RAZONABLE_ACTIVO_ANTERIOR", "Valor a Razonable Activo Anterior", mvto.RazonableActivoAnterior, idMonedaSistema));
			out.P_TABEVENTOS.add(new TabEventos(entrada.p_IdTipoCentralizacion, mvto.IdCartera, "VALOR_RAZONABLE_PASIVO_ANTERIOR", "Valor a Razonable Pasivo Anterior", mvto.RazonablePasivoAnterior, idMonedaSistema));
			if(BigDecimal.ZERO.compareTo(mvto.RazonableNetoAnterior) == 1){
				out.P_TABEVENTOS.add(new TabEventos(entrada.p_IdTipoCentralizacion, mvto.IdCartera, "VALOR_RAZONABLE_PERDIDA_ANTERIOR", "Valor a Razonable Perdida Anterior", mvto.RazonableNetoAnterior.abs(MathContext.DECIMAL128), idMonedaSistema));
			}
			if(BigDecimal.ZERO.compareTo(mvto.RazonableNetoAnterior) == -1){
				out.P_TABEVENTOS.add(new TabEventos(entrada.p_IdTipoCentralizacion, mvto.IdCartera, "VALOR_RAZONABLE_UTILIDAD_ANTERIOR", "Valor a Razonable Utilidad Anterior", mvto.RazonableNetoAnterior.abs(MathContext.DECIMAL128), idMonedaSistema));
			}
			
		}
		
		for(TabDatosMovimientoPaso mov : tabDatosMovimientoPaso){
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "DocumentoIdentidad", mov.DocumentoIdentidad, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "NombreCliente", mov.NombreCliente, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "PortafolioCliente", mov.Portafolio, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "MonedaPrincipal", mov.MonedaPrincipal, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "MonedaSecundaria", mov.MonedaSecundaria, BigDecimal.ZERO));
//			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "EsRelacionado", mov.EsRelacionado, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Modalidad", String.valueOf(mov.ModalidaPago), BigDecimal.ZERO));
//			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "EsCuentaPropia", mov.EsCuentaPropia, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Producto", mov.Producto, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "ClaseInstrumento", mov.ClaseInstrumento, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "TipoInstrumento", mov.TipoInstrumento, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "TipoOperacion", mov.TipoOperacion, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Nemo", mov.Nemo, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Agente", mov.Agente, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "NombreAgente", mov.NombreAgente, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "SucursalAgente", mov.SucursalAgente, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "NombreSucursalAgente", mov.NombreSucursalAgente, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "CentroCostoAgente", mov.CentroCostoAgente, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Relacion", mov.Relacion, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Bolsa", mov.Bolsa, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "TasaCambio", mov.TasaCambio.toPlainString(), BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "TipoPersona", mov.TipoPersona, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Concepto", mov.Concepto, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "CompraVenta", mov.CompraVenta, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "FechaLiquidacion", Fecha.toStringDate(mov.FechaLiquidacion), BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "FechaOperacion", Fecha.toStringDate(mov.FechaOperacion), BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "CostingCode", mov.CentroCostoAgente, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "CostingCode2", mov.Producto, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "CostingCode3", mov.Relacion, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "CostingCode4", mov.Bolsa, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "CostingCode5", "", BigDecimal.ZERO));
		}
		out.P_OUT_RESULTADO = llenarResultado(out.P_TABMOVIMIENTOS);
		log.write("FIN::CentralizaCarterasForwardValorizacion");
		return out;
	}
	
}
