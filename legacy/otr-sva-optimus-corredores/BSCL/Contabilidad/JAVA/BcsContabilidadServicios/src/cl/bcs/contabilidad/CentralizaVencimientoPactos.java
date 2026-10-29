package cl.bcs.contabilidad;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;

import cl.bcs.contabilidad.pojo.TabDatosMovimiento;
import cl.bcs.contabilidad.pojo.TabEventos;
import cl.bcs.contabilidad.pojo.TabMovimientos;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior.InObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior.OutObtenerDiaAnterior;

public class CentralizaVencimientoPactos implements BcsCentralizacion {

	private final String SQL_SELECT_MOVIMIENTOS = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaVencimientoPactos_SelectMovimientos.sql");
	private final String SQL_SELECT_EVENTOS_SUBYACENTE = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaVencimientoPactos_SelectEventosSubyacente.sql");
	private final String SQL_SELECT_EVENTOS_OPERACION = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaVencimientoPactos_SelectEventosOperacion.sql");
	private final String SQL_SELECT_DATOS_MOVIMIENTOS = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaVencimientoPactos_SelectDatosMovimiento.sql");
	private final String SQL_SELECT_EVENTOS_SUBYACENTE_DETALLE = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaVencimientoPactos_SelectEventosSubyacenteDetalle.sql");
	
	private final Log log = new Log("CON.CentralizaVencimientoPactos");
	
	public static class MovimientoPasoVencimientoPactos{
		public Long IdMovimiento;        
		public String DocumentoIdentidad;
		public String NombreCliente;
		public String Portafolio;
		public String Moneda;
		public String Producto;
		public String ClaseInstrumento;
		public String TipoInstrumento;
		public String Nemo;
		public String Boveda;
		public String IdCartera;
		public String Emisor;
		public String TipoReajuste;
		public String TipoEmisor;
		public String CodigoIFRS;
		public String TipoCartera;
		public String ClaseEmisor;
		public String Agente;
		public String NombreAgente;
		public String SucursalAgente;
		public String NombreSucursalAgente;
		public String CentroCostoAgente;
		public String Relacion; 
		public String CompraVenta;
		public String Mesa;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public SalidaCentralizacion centralizar(Connection con, EntradaCentralizacion entrada, EntradaCentralizacionInfoAdicional infoAdicional) throws Exception {
		log.write("INICIO::CentralizaVencimientoPactos");
		SQL sql = new SQL();
		SalidaCentralizacion out = new SalidaCentralizacion();		
		
		ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
		InformacionEmpresa empresa = consultarEmpresaService.logica(con, entrada.p_IdEmpresa);
	    Long idPaisSistema = empresa.IdPais;
	    
	    ObtenerDiaAnterior obtenerDiaAnterior = new ObtenerDiaAnterior();
	    InObtenerDiaAnterior inObtenerDiaAnterior = new InObtenerDiaAnterior();
	    inObtenerDiaAnterior.idPais = idPaisSistema;
	    inObtenerDiaAnterior.diaHabil = "S";
	    inObtenerDiaAnterior.soloCaeDiaHabil = "S";
	    InObtenerDiaAnterior.FechasIn fechaInicial = new InObtenerDiaAnterior.FechasIn();
	    fechaInicial.fechaInicial = entrada.p_FechaContable;
	    fechaInicial.numeroDias = 1L;
	    inObtenerDiaAnterior.fechas.add(fechaInicial);
	    OutObtenerDiaAnterior outObtenerDiaAnterior = obtenerDiaAnterior.logica(con, inObtenerDiaAnterior);
	    String fechaAnterior = outObtenerDiaAnterior.fechas.get(0).fechaCalculada;

		//MOVIMIENTOS
		HashMap<String,Object> params = new HashMap<>();
		params.put("p_IdEmpresa", entrada.p_IdEmpresa);
		params.put("p_FechaContable", entrada.p_FechaContable);
		params.put("p_IdTipoCentralizacion", entrada.p_IdTipoCentralizacion);
		out.P_TABMOVIMIENTOS = sql.query(con, SQL_SELECT_MOVIMIENTOS, params, TabMovimientos.class);
		params.clear();
		
		//EVENTOS
		String DetalleEventoCentralizacionVP;
		try
		{
			DetalleEventoCentralizacionVP = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "CONTABILIDAD", "DetalleEventoCentralizacionVP");
		}	
		catch (Exception e)
		{
			DetalleEventoCentralizacionVP = "N";
		}
		params.put("p_IdEmpresa", entrada.p_IdEmpresa);
		params.put("p_FechaContable", entrada.p_FechaContable);
		params.put("p_IdTipoCentralizacion", entrada.p_IdTipoCentralizacion);
		params.put("p_FechaAnterior", fechaAnterior);
		
		if(DetalleEventoCentralizacionVP.equalsIgnoreCase("S")) {
			out.P_TABEVENTOS.addAll(sql.query(con, SQL_SELECT_EVENTOS_SUBYACENTE_DETALLE, params, TabEventos.class));
		}else {
			out.P_TABEVENTOS.addAll(sql.query(con, SQL_SELECT_EVENTOS_SUBYACENTE, params, TabEventos.class));
		}
		params.remove("p_FechaAnterior");
		out.P_TABEVENTOS.addAll(sql.query(con, SQL_SELECT_EVENTOS_OPERACION, params, TabEventos.class));
		params.clear();
		
		//MOVIMIENTO DATOS OK
		params.put("p_IdEmpresa", entrada.p_IdEmpresa);
		params.put("p_FechaContable", entrada.p_FechaContable);
		List<MovimientoPasoVencimientoPactos> tabDatosMovimientoPaso = sql.query(con, SQL_SELECT_DATOS_MOVIMIENTOS, params, MovimientoPasoVencimientoPactos.class);
		params.clear();
		
		tabDatosMovimientoPaso.forEach(mov ->{
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "IdMovimiento", String.valueOf(mov.IdMovimiento), BigDecimal.ZERO));        
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "DocumentoIdentidad", mov.DocumentoIdentidad, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "NombreCliente", mov.NombreCliente, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "PortafolioCliente", mov.Portafolio, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Moneda", mov.Moneda, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Producto", mov.Producto, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "ClaseInstrumento", mov.ClaseInstrumento, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "TipoInstrumento", mov.TipoInstrumento, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Nemo", mov.Nemo, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Boveda", mov.Boveda, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "IdCartera", mov.IdCartera, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Emisor", mov.Emisor, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "IdTipoReajuste", mov.TipoReajuste, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "TipoEmisor", mov.TipoEmisor, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "CodigoIFRS", mov.CodigoIFRS, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "TipoCartera", mov.TipoCartera, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "ClaseEmisor", mov.ClaseEmisor, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Agente", mov.Agente, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "NombreAgente", mov.NombreAgente, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "SucursalAgente", mov.SucursalAgente, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "NombreSucursalAgente", mov.NombreSucursalAgente, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "CentroCostoAgente", mov.CentroCostoAgente, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Relacion", mov.Relacion, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "CompraVenta", mov.CompraVenta, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Mesa", mov.Mesa, BigDecimal.ZERO));
		});
		
		out.P_OUT_RESULTADO = llenarResultado(out.P_TABMOVIMIENTOS);
		
		return out;
	}

}
