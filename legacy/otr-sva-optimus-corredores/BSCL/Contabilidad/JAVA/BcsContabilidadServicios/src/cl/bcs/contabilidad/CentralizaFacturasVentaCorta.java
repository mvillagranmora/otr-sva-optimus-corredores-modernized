package cl.bcs.contabilidad;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;

import cl.bcs.contabilidad.pojo.TabDatosMovimiento;
import cl.bcs.contabilidad.pojo.TabEventos;
import cl.bcs.contabilidad.pojo.TabMovimientos;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CentralizaFacturasVentaCorta implements BcsCentralizacion  {
	
	private final String SQL_SELECT_MOVIMIENTOS = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaFacturaVentaCorta_SelectMovimientos.sql");
	private final String SQL_SELECT_EVENTOS = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaFacturaVentaCorta_SelectEventos.sql");;
	private final String SQL_SELECT_DATOS_MOVIMIENTOS = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaFacturaVentaCorta_SelectMovimientosDatos.sql");
	private final String SQL_MERCADO_CENTRALIZACION = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaFacturaVentaCorta_SelectMercadoCentralizacion.sql");
	
	private final Log log = new Log("CON.CentralizaFacturasVentaCorta");
	
	public static class TabDatosMovimientoPaso{
		public Long IdMovimiento;
        public String TipoDocumento;
        public Long Folio;
        public String FechaDocumento;
        public String CompraVenta;
        public String Operacion;
        public String Producto;
        public String ClaseInstrumento;
        public String TipoInstrumento;
        public String CondicionLiquidacion;
        public String MonedaComprobante;
        public String Bolsa;
        public String DocumentoIdentidad;
        public String NombreCliente;
        public String Portafolio;
        public String Concepto;
        public String NombreConcepto;
        public String Agente;
        public String NombreAgente;
        public String SucursalAgente;
        public String NombreSucursalAgente;
        public String CentroCostoAgente;
        public String EstadoSII;
        public String EstadoSUNAT;
        public String EsExento;
        public String Moneda;
        public String MonedaLiquidacion;
        public String EsContraparte;
        public String Relacion;
        public String TipoPersona;
        public String TipoInstrumentoVenta;
        public String TipoInstrumentoCompra;
        public String ClaseEmisor;
        public String Emisor;
        public Long TipoEmisor;
        public String Nemotecnico;
        public String TipoBonoIRF;
        public String CodigoIFRS;
        public String OrigenInstrumento;
        public String IndicadorRelacionadoGrupo;
        public String Plaza;
        public String FormadorMercado;
        public String PresenciaBursatil;
        public String CodigoTipoComprobante;
        public Date FechaLiquidacion;
        public String TipoComprobante;
        public Timestamp FechaComprobante;
        public String EstadoComprobante;
        public String EsOtrosConceptos;
        public Long FolioPoliza;
        public String EsAPlazo;
        public String DocumentoIdentidadCAVALI;
        public String EsVentaCorta;
        public String EsArbitraje;
        public BigDecimal CantidadTotal;
        public BigDecimal Precio;
        public BigDecimal Tasa;
        public Timestamp FechaVencimiento;
        
        public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ClasificacionTipoInstrumento{
		public String ClasificacionTipoInstrumento;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public SalidaCentralizacion centralizar(Connection con, EntradaCentralizacion entrada, EntradaCentralizacionInfoAdicional infoAdicional) throws Exception {
		log.write("INICIO::CentralizaFacturasVentaCorta");
		SQL sql = new SQL();
		SalidaCentralizacion out = new SalidaCentralizacion();
		List<TabDatosMovimientoPaso> tabDatosMovimientoPaso = new ArrayList<TabDatosMovimientoPaso>();
		InformacionEmpresa empresa = infoAdicional.p_Empresa;
		
		String documentoIdentidadContraparte = "";
		String nombreContraparte = "";
		
		try {
			documentoIdentidadContraparte = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "CON.DocumentoIdentidadContraparte");
			nombreContraparte = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "CON.NombreContraparte");
		}catch(Exception e) {
			log.write("No existen parametros configurados",e);
		}
		
		
		//MOVIMIENTOS
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdTipoCentralizacion", entrada.p_IdTipoCentralizacion);
		List<ClasificacionTipoInstrumento> clasificacion = sql.query(con, SQL_MERCADO_CENTRALIZACION, params, ClasificacionTipoInstrumento.class);
		params.clear();
		
		params.put("FechaContable", entrada.p_FechaContable);
		params.put("IdTipoCentralizacion", entrada.p_IdTipoCentralizacion);
		params.put("DocumentoIdentidad", empresa.DocumentoIdentidad);
		params.put("IdClasificacionTipoInstrumento", clasificacion.get(0).ClasificacionTipoInstrumento);
		
		out.P_TABMOVIMIENTOS = sql.query(con, SQL_SELECT_MOVIMIENTOS, params, TabMovimientos.class);
		out.P_TABEVENTOS = sql.query(con, SQL_SELECT_EVENTOS, params, TabEventos.class);
		params.put("IdEmpresa", empresa.IdEmpresa);
		params.remove("IdTipoCentralizacion");
		tabDatosMovimientoPaso = sql.query(con, SQL_SELECT_DATOS_MOVIMIENTOS, params, TabDatosMovimientoPaso.class);

		for (TabDatosMovimientoPaso mov : tabDatosMovimientoPaso) {
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "TipoDocumento", mov.TipoDocumento, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "FechaDocumento", mov.FechaDocumento, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "CompraVenta", mov.CompraVenta, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Operacion", mov.Operacion, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Producto", mov.Producto, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "ClaseInstrumento", mov.ClaseInstrumento, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "TipoInstrumento", mov.TipoInstrumento, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "CondicionLiquidacion", mov.CondicionLiquidacion, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "MonedaComprobante", mov.MonedaComprobante, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Bolsa", mov.Bolsa, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "DocumentoIdentidad", mov.DocumentoIdentidad, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "NombreCliente", mov.NombreCliente, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Portafolio", mov.Portafolio, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Concepto", mov.Concepto, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "NombreConcepto", mov.NombreConcepto, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Agente", mov.Agente, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "NombreAgente", mov.NombreAgente, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "SucursalAgente", mov.SucursalAgente, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "NombreSucursalAgente", mov.NombreSucursalAgente, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "FolioPoliza", String.valueOf(mov.FolioPoliza), BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Folio", String.valueOf(mov.Folio), BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "EsExento", mov.EsExento, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Moneda", mov.Moneda, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "MonedaLiquidacion", mov.MonedaLiquidacion, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "EsContraparte", mov.EsContraparte, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Relacion", mov.Relacion, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Emisor", mov.Emisor, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Nemotecnico", mov.Nemotecnico, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "TipoPersona", mov.TipoPersona, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "CodigoTipoComprobante", mov.CodigoTipoComprobante, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "FechaLiquidacion", Fecha.stringToSqlDate(mov.FechaLiquidacion), BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "FechaComprobante", Fecha.toStringDatetime(mov.FechaComprobante), BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "EstadoComprobante", mov.EstadoComprobante, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "EsOtrosConceptos", mov.EsOtrosConceptos, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "EsAPlazo", mov.EsAPlazo, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "EsVentaCorta", mov.EsVentaCorta, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "EsArbitraje", mov.EsArbitraje, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "DocumentoIdentidadContraparte", documentoIdentidadContraparte, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "NombreContraparte", nombreContraparte, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "EstadoSII", mov.EstadoSII, BigDecimal.ZERO));
		}
		
		out.P_OUT_RESULTADO = llenarResultado(out.P_TABMOVIMIENTOS);

		log.write("FIN::CentralizaFacturasVentaCorta");
		return out;
	}
	
	public static void main(String[] args) {		
		System.out.println("== INICIO PRUEBA LOCAL ==");

		try {
			Connection con = PlataformaUtils.getPropertiesConnection("desa2");			
			EntradaCentralizacion entrada =  new EntradaCentralizacion();
			entrada.p_IdEmpresa = 1L;
			entrada.p_FechaContable = "2024-02-19";
			entrada.p_IdTipoCentralizacion = 408L;
			
			EntradaCentralizacionInfoAdicional adicional = new EntradaCentralizacionInfoAdicional();
			InformacionEmpresa emp = new InformacionEmpresa();
			emp.IdEmpresa= 1L;
			adicional.p_Empresa = emp;
			
			CentralizaFacturasVentaCorta demo = new CentralizaFacturasVentaCorta();
			
			try {
				SalidaCentralizacion ObjOut = demo.centralizar(con, entrada, adicional);
 				System.out.println(ObjOut.toString());
 				con.commit();	
			} catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}
		} catch (Exception e) {
			e.printStackTrace();			
		}
		
	}
}
