package cl.bcs.facturacion;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import cl.bcs.custodia.AnularCarteraNegativa;
import cl.bcs.custodia.CertificarMovimientosEnTransito;
import cl.bcs.custodia.AnularCarteraNegativa.AnularCarteraNegativa_Entrada;
import cl.bcs.custodia.AnularCarteraNegativa.AnularCarteraNegativa_Salida;
import cl.bcs.custodia.AnularCarteraNegativa.CarteraNegativa_Entrada;
import cl.bcs.custodia.utils.ControlRetiroCustodia;
import cl.bcs.custodia.utils.ControlRetiroCustodia.ControlRetiroCustodiaEntrada;
import cl.bcs.custodia.utils.ControlRetiroCustodia.ControlRetiroObjDatosEntrada;
import cl.bcs.custodia.utils.GestionMovimientoCarteraEnTransito;
import cl.bcs.custodia.utils.InformacionConfiguracionCustodia;
import cl.bcs.custodia.utils.GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar;
import cl.bcs.custodia.utils.IngresoCustodia;
import cl.bcs.custodia.utils.IngresoCustodia.IngresoCustodiaSalida;
import cl.bcs.custodia.utils.RetiroCustodia;
import cl.bcs.custodia.utils.RetiroCustodia.ObjDatosEntrada;
import cl.bcs.custodia.utils.RetiroCustodia.RetiroCustodiaSalida;
import cl.bcs.facturacion.AnularComprobanteFinal.MovimientoCarteraEnTransito_Proceso;
import cl.bcs.facturacion.ProcesarFacturacionCustodia.GrupoOperacionCustodia;
import cl.bcs.facturacion.ProcesarFacturacionCustodia.MovimientoCustodiaSalida;
import cl.bcs.facturacion.ProcesarFacturacionCustodia.Paso;
import cl.bcs.facturacion.pojo.Comprobante;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class ReversarFacturacionCustodia implements JavaCall {
	private static final Logger logger = LogManager.getLogger(ReversarFacturacionCustodia.class);

	private final String SQL_SELECT_COMPROBANTE = loadResourceToString("cl/bcs/facturacion/sql/ReversarFacturacionCustodia_SelectComprobante.sql");
	private final String SQL_SELECT_CUSTODIA_TRANSITOS = loadResourceToString("cl/bcs/facturacion/sql/ReversarFacturacionCustodia_SelectTransitos.sql");
	private final String SQL_SELECT_CUSTODIA_CARTERAS_COMPRAS = loadResourceToString("cl/bcs/facturacion/sql/ReversarFacturacionCustodia_SelectCarterasCompra.sql");
	private final String SQL_SELECT_CUSTODIA_CARTERAS_VENTAS = loadResourceToString("cl/bcs/facturacion/sql/ReversarFacturacionCustodia_SelectCarterasVenta.sql");
	//apuntamos a mismo sql de anulacion para homologar (cualquier modificacion debe ir en los dos lados)
	private final String SQL_SELECT_CUSTODIA_CARTERAS_NEGATIVAS = loadResourceToString("cl/bcs/facturacion/sql/AnularComprobanteFinal_SelectCarterasNegativas.sql"); 
	private final String SQL_SELECT_CUSTODIA_SUBYACENTES = loadResourceToString("cl/bcs/facturacion/sql/ReversarFacturacionCustodia_SelectSubyacentes.sql");
	private final String SQL_SELECT_PRESTAMOS_VIGENTES = loadResourceToString("cl/bcs/facturacion/sql/ReversarFacturacionCustodia_SelectPrestamosVigentes.sql");
	private final String SQL_SELECT_CUSTODIA_GARANTIAS_COMPRAS = loadResourceToString("cl/bcs/facturacion/sql/AnularComprobanteFinal_SelectGarantiasCompra.sql");
	private final String SQL_SELECT_INDICADORES_PORTAFOLIO = loadResourceToString("cl/bcs/facturacion/sql/ReversarFacturacionCustodia_SelectIndicadoresPortafolio.sql");
	
	public static class ReversarFacturacionCustodia_Entrada {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public List<Long> p_Comprobantes;
		public List<CrearComprobante.Comprobante_Proceso> p_NotasCreditos;
		public List<Long> p_MovimientosEnTransito;	//si proviene desde Certificacion de movimiento en transito
		public String p_simularOperacion;
		public String p_ValorizacionAutomatica = "S";

		public ReversarFacturacionCustodia_Entrada() {
			this.p_Comprobantes = new ArrayList<>();
			this.p_MovimientosEnTransito = new ArrayList<>();
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class ReversarFacturacionCustodia_Salida {

		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR = 0L;
		public List<MovimientoCustodiaSalida> P_OUT_MOVIMIENTOS;

		public ReversarFacturacionCustodia_Salida() {
			this.P_OUT_MENSAJES = new ArrayList<>();
			this.P_OUT_MOVIMIENTOS = new ArrayList<>();
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class TransitoAnulacionCompraNormalCustodia {
	    public IngresoCustodia.ObjDatosEntrada pasoTransitoAnulacionCartera; //anulacion de cartera vigente
	    public Long FolioCartera;
	    public List<IngresoCustodia.ObjDatosEntrada> pasoTransitoRetiroGarantia;

	    public TransitoAnulacionCompraNormalCustodia(IngresoCustodia.ObjDatosEntrada transitoAnulacionCartera) {
	        this.pasoTransitoAnulacionCartera = transitoAnulacionCartera;
			this.FolioCartera = transitoAnulacionCartera.FolioCustodia;
	        this.pasoTransitoRetiroGarantia = new ArrayList<>();
	    }
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class TransitoAnulacionVentaNormalCustodia {
		public IngresoCustodia.ObjDatosEntrada pasoTransitoAnulacionCartera;	//anulacion cartera liquidada
		public IngresoCustodia.ObjDatosEntrada pasoTransitoIngresoCartera;		//reingreso cartera
		
		public TransitoAnulacionVentaNormalCustodia(IngresoCustodia.ObjDatosEntrada pasoTransitoAnulacionCartera, IngresoCustodia.ObjDatosEntrada pasoTransitoIngresoCartera) {
			this.pasoTransitoAnulacionCartera = pasoTransitoAnulacionCartera;
			this.pasoTransitoIngresoCartera =  pasoTransitoIngresoCartera;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class TransitoAnulacionVentaSimultanea {
		public Long FolioCartera;
		public IngresoCustodia.ObjDatosEntrada pasoTransitoAnulacionCabecera;	//anulacion cartera liquidada
		public List<IngresoCustodia.ObjDatosEntrada> pasoTransitoRetiroSubyacente;		//reingreso cartera
		
		public TransitoAnulacionVentaSimultanea(Long FolioCartera, IngresoCustodia.ObjDatosEntrada pasoTransitoAnulacionCabecera) {
			this.pasoTransitoAnulacionCabecera = pasoTransitoAnulacionCabecera;
			this.FolioCartera =  FolioCartera;
			this.pasoTransitoRetiroSubyacente = new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class MovimientosParaTransito extends ObjDatosEntrada {
		
		public Long FolioCartera;
		public Long IdGarantia;
		public BigDecimal INGCantidad;
		public Long IdCartera;
		public BigDecimal INGPrecioCompra;
		public BigDecimal INGMontoATasaCompraActual = BigDecimal.ZERO;
		public Long EGRFolioComprobante;
		public String IdClasificacionTipoInstrumento;
		public BigDecimal EGRCantidad;
		public BigDecimal EGRValorRescate;
		public Long	IdSubyacente;
		public Long	FolioSubyacente;
		public String IdTipoOperacionAnulacion;
		public BigDecimal INGValorRescate;
		public Long FolioCarteraRelacionada;
		public java.sql.Date FechaVencimientoOperacion;
		public Long IdCuentaCustodioPortafolio;
		public Long IdPortafolioRelacionado;
		public Long IdClienteRelacionado;
		public java.sql.Date INGFechaLiquidacion;
		public String IdTipoReajuste;
		public String EsVentaCorta;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class Comprobante_Proceso extends Comprobante {
		public long ok;
		public long errores;
		public long autorizaciones;
		public long advertencias;

		public List<MovimientoCarteraEnTransito_Proceso> movsCarteraTransito;
		public List<MovimientosParaTransito> carteras;
		public List<MovimientosParaTransito> subyacentes;
		public List<MovimientosParaTransito> garantias;

		public Comprobante_Proceso() {
			super();
			movsCarteraTransito = new ArrayList<>();
			carteras = new ArrayList<>();
			subyacentes = new ArrayList<>();
			garantias = new ArrayList<>();

		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class FoliosPres {
		public long FolioPrestamo;
		public BigDecimal CantidadAsignada;
	}
	
	public static class IndicadoresPortafolio{
		public Long IdPortafolio;
		public String SobregiroRV = "N";
		public String SobregiroRF = "N";
		public String SobregiroIF = "N";
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public ReversarFacturacionCustodia_Salida logica(Connection conn, ReversarFacturacionCustodia_Entrada entrada) throws Exception {
	    
	    if (entrada.p_MovimientosEnTransito != null && !entrada.p_MovimientosEnTransito.isEmpty()) {
	        return certificarMovimientosTransito(conn, entrada);
	    } 
	    
	    return crearMovimientosEnTransito(conn, entrada);
	}
	
	private ReversarFacturacionCustodia_Salida certificarMovimientosTransito(Connection conn, ReversarFacturacionCustodia_Entrada entrada) throws Exception {
	    ReversarFacturacionCustodia_Salida salida = new ReversarFacturacionCustodia_Salida();

	    List<GestionMovimientoCarteraEnTransito.ObjMovimientosEnTransito> movimientosTransito = 
	        GestionMovimientoCarteraEnTransito.ObtenerMovimientoEnTransito(conn, entrada.p_MovimientosEnTransito);

	    if (movimientosTransito == null || movimientosTransito.isEmpty()) {
	        return salida;
	    }
	    
		List<InformacionConfiguracionCustodia.ObjConfiguracionMovimientoCustodia> _configuracion = InformacionConfiguracionCustodia.informacionConfiguracion(conn, entrada.p_IdEmpresa);
		HashMap<String,Object> ConfigCUS = Utils.List2Hash(_configuracion, "IdTipoOperacion");
	    
	    List<GrupoOperacionCustodia> anularComprasCustodia = new ArrayList<>();
	    List<GrupoOperacionCustodia> anularVentasCustodia = new ArrayList<>();
	    List<GrupoOperacionCustodia> anularComprasSimultaneas = new ArrayList<>();
	    List<GrupoOperacionCustodia> anularVentasSimultaneas = new ArrayList<>();
	    
	    for (GestionMovimientoCarteraEnTransito.ObjMovimientosEnTransito t : movimientosTransito) {
			InformacionConfiguracionCustodia.ObjConfiguracionMovimientoCustodia conf = (InformacionConfiguracionCustodia.ObjConfiguracionMovimientoCustodia) ConfigCUS.get(t.IdTipoOperacion);	
			
			GrupoOperacionCustodia g = new GrupoOperacionCustodia(t.IdMovimientoCarteraEnTransito,conf,t,t);

			if (conf == null) {
				String codigo = "UI_ERROR_OPERACION_NO_CONFIGURADA";
				String descripcion = Diccionario.getMensaje(conn, entrada.p_IdEmpresa, codigo).add("Operacion", t.IdTipoOperacion).msg();
				salida.P_OUT_MENSAJES.add(new MensajeSalida(t.IdMovimientoCarteraEnTransito,MensajeSalida.MENSAJE_ERROR,codigo,descripcion));	
				salida.P_OUT_ERRORESCONTADOR++;
			}
	    				
			// simultaneas compra
			if ("ANCSIM".equalsIgnoreCase(t.IdTipoOperacion)) {
				anularComprasSimultaneas.add(g);
			}
			// Simultaneas venta
			else if ("ANVSIM".equalsIgnoreCase(t.IdTipoOperacion)) {
				anularVentasSimultaneas.add(g);
			}
			// OSA
	        else if ("ANSOSA".equalsIgnoreCase(t.IdTipoOperacion) && "OSA".equalsIgnoreCase(t.IdTipoInstrumento)) {
	        	anularVentasCustodia.add(g);
	        }
			// compra Custodia
			else if ("N".equalsIgnoreCase(t.EsNominal) && "N".equalsIgnoreCase(conf.PoseeInstrumentoSubyacente) && "ING".equalsIgnoreCase(conf.IdAccionOperacionRelacionada)) {
				anularComprasCustodia.add(g);
			} 
			else if (!"S".equalsIgnoreCase(t.EsVentaCorta) && "N".equalsIgnoreCase(t.EsNominal) && "N".equalsIgnoreCase(conf.PoseeInstrumentoSubyacente) && "RET".equalsIgnoreCase(conf.IdAccionOperacionRelacionada)) {
				anularVentasCustodia.add(g);
			}
			else if ("S".equalsIgnoreCase(t.EsVentaCorta) && "RET".equalsIgnoreCase(conf.IdAccionOperacionRelacionada)) {
				anularComprasCustodia.add(g);
			}
	    }
		List<ObjMovimientosAActualizar> transitosOK = new ArrayList<>();

	    if (anularComprasCustodia.size() > 0) {
	    	List<GrupoOperacionCustodia> salidasCompras = ProcesarComprasCustodia(conn, entrada, anularComprasCustodia);
	    	
	    	for (GrupoOperacionCustodia g : salidasCompras) {
				transitosOK.addAll(g.transitosActualizar);
				salida.P_OUT_MENSAJES.addAll(g.pasos.stream().flatMap(t -> t.mensajes.stream()).collect(Collectors.toList()));
			}	
	    }
	    
	    if (anularVentasCustodia.size() > 0) {
	    	List<GrupoOperacionCustodia> salidasVentas = ProcesarVentasCustodia(conn, entrada, anularVentasCustodia);
	    	
	    	for (GrupoOperacionCustodia g : salidasVentas) {
				transitosOK.addAll(g.transitosActualizar);
				salida.P_OUT_MENSAJES.addAll(g.pasos.stream().flatMap(t -> t.mensajes.stream()).collect(Collectors.toList()));
			}	
	    }
	    
	    if (anularComprasSimultaneas.size() > 0) {
	    	List<GrupoOperacionCustodia> salidaAnularSimultaneas = ProcesarComprasSimultaneas(conn, entrada, anularComprasSimultaneas);    	
	    	for (GrupoOperacionCustodia g : salidaAnularSimultaneas) {
				transitosOK.addAll(g.transitosActualizar);
				salida.P_OUT_MENSAJES.addAll(g.pasos.stream().flatMap(t -> t.mensajes.stream()).collect(Collectors.toList()));
			}	
	    }
	    
	    if (anularVentasSimultaneas.size() > 0) {
	    	List<GrupoOperacionCustodia> salidaAnularSimultaneas = ProcesarVentasSimultaneas(conn, entrada, anularVentasSimultaneas);
	    	for (GrupoOperacionCustodia g : salidaAnularSimultaneas) {
				transitosOK.addAll(g.transitosActualizar);
				salida.P_OUT_MENSAJES.addAll(g.pasos.stream().flatMap(t -> t.mensajes.stream()).collect(Collectors.toList()));
			}	
	    }
	    
	    if (transitosOK.size() > 0) {
			GestionMovimientoCarteraEnTransito.ActualizarMovimientoEnTransito(conn, transitosOK);
		}
	   
	    return salida;
	}
	
	public List<GrupoOperacionCustodia> ProcesarComprasCustodia(Connection conn, ReversarFacturacionCustodia_Entrada entrada, List<GrupoOperacionCustodia> grupos) throws Exception {
		SQL _sql = new SQL();
		ControlRetiroCustodiaEntrada entradaRetiroCustodia = new ControlRetiroCustodiaEntrada();
		configurarEntradaBase(entrada, entradaRetiroCustodia);
		
		HashMap<Long, IndicadoresPortafolio> indicadoresPortafolio = new HashMap<>();
		{
			HashMap<String,Object> paramsInIndicadorPortafolio = new HashMap<>();
			List<Long> idsPortafolio = grupos
					.stream().map(g -> g.TransitoPrincipal.IdPortafolio).collect(Collectors.toList());
			
			paramsInIndicadorPortafolio.put("idsPortafolio", idsPortafolio);
			List<IndicadoresPortafolio> _indicadoresPortafolio = _sql.query(conn, SQL_SELECT_INDICADORES_PORTAFOLIO, paramsInIndicadorPortafolio, IndicadoresPortafolio.class);
			HashMap<Object, Object>__indicadoresPortafolio = Utils.List2Hash(_indicadoresPortafolio, "IdPortafolio");
			for(Object key: __indicadoresPortafolio.keySet()) {
				indicadoresPortafolio.put((Long)key,(IndicadoresPortafolio) __indicadoresPortafolio.get(key));
			}
		}
		
		for (GrupoOperacionCustodia g: grupos) {
			 ControlRetiroObjDatosEntrada movIngreso = new ControlRetiroObjDatosEntrada();
			 IndicadoresPortafolio ind = indicadoresPortafolio.getOrDefault(g.TransitoPrincipal.IdPortafolio, new IndicadoresPortafolio());

			 mapearCamposComunes(g.TransitoPrincipal, movIngreso);
			 movIngreso.TablaAfectada = "CARTERA";
			 movIngreso.PermiteSobregiro = portafolioPermiteSobregiro(g.TransitoPrincipal.IdClasificacionTipoInstrumento, ind);
				
			 entradaRetiroCustodia.p_DatosEntrada.add(movIngreso);
			 
			 Paso p = new Paso("RETIROCARTERA", movIngreso, g);
			 g.pasos.add(p);
		}
		
	    if (entradaRetiroCustodia.p_DatosEntrada.isEmpty()) {
	    	 return new ArrayList<>();
	    }
	    
    	ControlRetiroCustodia servicioRetiro = new ControlRetiroCustodia();
        RetiroCustodiaSalida resRetiro = servicioRetiro.logica(conn, entradaRetiroCustodia);
        
        for (RetiroCustodia.RetiroCustodiaSalida.ObjDatosSalida datoSalida : resRetiro.P_OUT_DATOSSALIDA) {
			for (GrupoOperacionCustodia grupo : grupos) {
				
				if (!grupo.IdMovimientoInicial.equals(datoSalida.IdGenerarMovimientoCustodia)) {
					continue;
				}
				
				if (datoSalida.RegistroConProblemas.equalsIgnoreCase("N")) {
					grupo.pasos.get(0).estado = "OK";
					grupo.estado = "OK";
					
					if (datoSalida.TablaOrigen.equalsIgnoreCase("NEGATIVA")) {
						String codigo = "UI_CARTERA_NEGATIVA_INGRESADA";
						String nemo = grupo.TransitoPrincipal.IdClasificacionTipoInstrumento.equalsIgnoreCase("IF")?grupo.TransitoPrincipal.NemoSVS:grupo.TransitoPrincipal.Nemotecnico;
						String descripcion = Diccionario.getMensaje(conn, entrada.p_IdEmpresa, codigo).add("FolioCartera", datoSalida.IdCustodia).add("Nemotecnico",nemo).msg();
						grupo.pasos.get(0).mensajes.add(new MensajeSalida(datoSalida.IdGenerarMovimientoCustodia,MensajeSalida.MENSAJE_INFORMACION,codigo,descripcion));
					}
					else {
						String codigo = "UI_TIPO_CARTERA_RETIRADA_FOLIO";
						String descripcion = Diccionario.getMensaje(conn, entrada.p_IdEmpresa, codigo).add("FolioCartera", datoSalida.FolioCustodia).add("TipoCartera", datoSalida.TablaOrigen).msg();
						grupo.pasos.get(0).mensajes.add(new MensajeSalida(datoSalida.IdGenerarMovimientoCustodia,MensajeSalida.MENSAJE_INFORMACION,codigo,descripcion));	
					}
					
				}
				else {
					grupo.pasos.get(0).estado = "ERR";
					grupo.estado = "ERR";
					
					String codigo = "UI_ERROR_AL_RETIRAR_CARTERA"; 
					String descripcion = Diccionario.getMensaje(conn, entrada.p_IdEmpresa, codigo)
							.add("IdMovimientoComprobante", datoSalida.IdGenerarMovimientoCustodia).add("TipoCartera", datoSalida.TablaOrigen).msg();
					grupo.pasos.get(0).mensajes.add(new MensajeSalida(datoSalida.IdGenerarMovimientoCustodia,MensajeSalida.MENSAJE_ERROR,codigo,descripcion));
					
				}
			}
		}
        
        for (GrupoOperacionCustodia grupo : grupos) {
			if (grupo.estado.equalsIgnoreCase("OK")) {
				GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar MOV = new GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar();
				MOV.IdMovimientoCarteraEnTransito = grupo.TransitoPrincipal.IdMovimientoCarteraEnTransito;
				MOV.EsCertificacion = "S";
				MOV.IdUsuario = entrada.p_IdUsuario;
				grupo.transitosActualizar.add(MOV);
			}
		}
		
        return grupos;
	}
	
	public List<GrupoOperacionCustodia> ProcesarVentasCustodia(Connection conn, ReversarFacturacionCustodia_Entrada entrada, List<GrupoOperacionCustodia> grupos) throws Exception {
		List<MensajeSalida> salida = new ArrayList<>();
		IngresoCustodia.IngresoCustodiaEntrada entradaIngresos = new IngresoCustodia.IngresoCustodiaEntrada();
		configurarEntradaBase(entrada, entradaIngresos);
		List<CarteraNegativa_Entrada> cneg_list = new ArrayList<>();

		
		for (GrupoOperacionCustodia g: grupos) {
			if (!g.TransitoPrincipal.IdMovimientoCarteraEnTransitoRelacionado.equals(0L)) {
				CarteraNegativa_Entrada n = new CarteraNegativa_Entrada();
				n.IdCarteraNegativa = g.TransitoPrincipal.IdCustodiaOrigen;
				cneg_list.add(n);
				g.estado = "OK";
				continue;
			}
			
			 IngresoCustodia.ObjDatosEntrada movIngreso = new IngresoCustodia.ObjDatosEntrada();
			 mapearCamposComunes(g.TransitoPrincipal, movIngreso);
			 movIngreso.TablaAfectada = "CARTERA";
			 Paso p = new Paso("RETIROCARTERA", movIngreso, g);
			 g.pasos.add(p);
			 entradaIngresos.p_DatosEntrada.add(movIngreso);
		}
		
		if (cneg_list.size() > 0) {
			AnularCarteraNegativa anulacionCNeg = new AnularCarteraNegativa();
			AnularCarteraNegativa_Entrada cneg_in = new AnularCarteraNegativa_Entrada();
			cneg_in.p_IdEmpresa = entrada.p_IdEmpresa;
			cneg_in.p_IdModulo = entrada.p_IdModulo;
			cneg_in.p_IdUsuario = entrada.p_IdUsuario;
			cneg_in.p_simularOperacion = entrada.p_simularOperacion;
			cneg_in.p_CarterasNegativas = cneg_list;
			AnularCarteraNegativa_Salida cneg_out = anulacionCNeg.logica(conn, cneg_in);
			salida.addAll(cneg_out.P_OUT_MENSAJES);
		}
		
        IngresoCustodia servicioIngreso = new IngresoCustodia();
        IngresoCustodia.IngresoCustodiaSalida resIngreso = servicioIngreso.logica(conn, entradaIngresos);
        
        for (IngresoCustodiaSalida.ObjDatosSalida datoSalida : resIngreso.P_OUT_DATOSSALIDA) {
			for (GrupoOperacionCustodia grupo : grupos) {
				
				if (!grupo.IdMovimientoInicial.equals(datoSalida.IdGenerarMovimientoCustodia)) {
					continue;
				}
				
				grupo.pasos.get(0).resultado.add(datoSalida);
				
				if (datoSalida.RegistroConProblemas.equalsIgnoreCase("N")) {
					
					grupo.estado = "OK";
					grupo.pasos.get(0).estado = "OK";
					
					String codigo = "UI_CARTERA_CREADA_FOLIO";
					String descripcion = Diccionario.getMensaje(conn, entrada.p_IdEmpresa, codigo).add("FolioCartera", datoSalida.FolioCustodia).add("TipoCartera", datoSalida.TablaOrigen).msg();
					grupo.pasos.get(0).mensajes.add(new MensajeSalida(datoSalida.IdGenerarMovimientoCustodia,MensajeSalida.MENSAJE_INFORMACION,codigo,descripcion));
				}
				else {
					
					grupo.estado = "ERR";
					grupo.pasos.get(0).estado = "ERR";
					
					String codigo = "UI_ERROR_AL_CREAR_CARTERA_MOVIMIENTO";
					String descripcion = Diccionario.getMensaje(conn, entrada.p_IdEmpresa, codigo)
							.add("IdMovimientoComprobante", datoSalida.IdGenerarMovimientoCustodia).add("TipoCartera", datoSalida.TablaOrigen).msg();
					grupo.pasos.get(0).mensajes.add(new MensajeSalida(datoSalida.IdGenerarMovimientoCustodia,MensajeSalida.MENSAJE_ERROR,codigo,descripcion));
				}
			}
		}
        
        for (GrupoOperacionCustodia grupo : grupos) {
			if (grupo.estado.equalsIgnoreCase("OK")) {
				GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar MOV = new GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar();
				MOV.IdMovimientoCarteraEnTransito = grupo.TransitoPrincipal.IdMovimientoCarteraEnTransito;
				MOV.EsCertificacion = "S";
				MOV.IdUsuario = entrada.p_IdUsuario;
				grupo.transitosActualizar.add(MOV);
			}
		}
		
        return grupos;
	}
	
	public List<GrupoOperacionCustodia> ProcesarVentasSimultaneas(Connection conn, ReversarFacturacionCustodia_Entrada entrada, List<GrupoOperacionCustodia> grupos) throws Exception {		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa,"PLATAFORMA", "PLA.FechaSistema");
		Timestamp fechaHoraSistema = FechaDB.ObtenerFechaHoraSistemaTs(conn, entrada.p_IdEmpresa);
				
		RetiroCustodia.RetiroCustodiaEntrada entradaEntradaCustodia = new RetiroCustodia.RetiroCustodiaEntrada();
		configurarEntradaBase(entrada, entradaEntradaCustodia);
				
		for (GrupoOperacionCustodia g: grupos) {
			RetiroCustodia.ObjDatosEntrada movRetiroCustodia = new RetiroCustodia.ObjDatosEntrada();
			mapearCamposComunes(g.TransitoPrincipal, movRetiroCustodia);
			movRetiroCustodia.TablaAfectada = (!g.TransitoPrincipal.IdMovimientoCarteraEnTransitoRelacionado.equals(0L)) ? "SUBYACENTE" :  "CARTERA";
			movRetiroCustodia.FechaRetiroRegistro = fechaHoraSistema;
			movRetiroCustodia.FechaOperacion = Fecha.toDate(fechaSistema);
			
			entradaEntradaCustodia.p_DatosEntrada.add(movRetiroCustodia);
			 Paso p = new Paso("RETIROCARTERA", movRetiroCustodia, g);
			 g.pasos.add(p);
		}
		
	    if (entradaEntradaCustodia.p_DatosEntrada.isEmpty()) {
	    	 return new ArrayList<>();
	    }
	    
    	RetiroCustodia servicioRetiro = new RetiroCustodia();
        RetiroCustodiaSalida resRetiro = servicioRetiro.logica(conn, entradaEntradaCustodia);
        
        for (RetiroCustodia.RetiroCustodiaSalida.ObjDatosSalida datoSalida : resRetiro.P_OUT_DATOSSALIDA) {
			for (GrupoOperacionCustodia grupo : grupos) {
				
				if (!grupo.IdMovimientoInicial.equals(datoSalida.IdGenerarMovimientoCustodia)) {
					continue;
				}
				
				if (datoSalida.RegistroConProblemas.equalsIgnoreCase("N")) {
					grupo.pasos.get(0).estado = "OK";
					grupo.estado = "OK";
				
					String codigo = "UI_TIPO_CARTERA_RETIRADA_FOLIO";
					String descripcion = Diccionario.getMensaje(conn, entrada.p_IdEmpresa, codigo).add("FolioCartera", datoSalida.FolioCustodia).add("TipoCartera", datoSalida.TablaOrigen).msg();
					grupo.pasos.get(0).mensajes.add(new MensajeSalida(datoSalida.IdGenerarMovimientoCustodia,MensajeSalida.MENSAJE_INFORMACION,codigo,descripcion));	
				}
				else {
					grupo.pasos.get(0).estado = "ERR";
					grupo.estado = "ERR";
					
					String codigo = "UI_ERROR_AL_RETIRAR_CARTERA"; 
					String descripcion = Diccionario.getMensaje(conn, entrada.p_IdEmpresa, codigo)
							.add("IdMovimientoComprobante", datoSalida.IdGenerarMovimientoCustodia).add("TipoCartera", datoSalida.TablaOrigen).msg();
					grupo.pasos.get(0).mensajes.add(new MensajeSalida(datoSalida.IdGenerarMovimientoCustodia,MensajeSalida.MENSAJE_ERROR,codigo,descripcion));
					
				}
			}
		}
		
        for (GrupoOperacionCustodia grupo : grupos) {
			if (grupo.estado.equalsIgnoreCase("OK")) {
				GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar MOV = new GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar();
				MOV.IdMovimientoCarteraEnTransito = grupo.TransitoPrincipal.IdMovimientoCarteraEnTransito;
				MOV.EsCertificacion = "S";
				MOV.IdUsuario = entrada.p_IdUsuario;
				grupo.transitosActualizar.add(MOV);
			}
		}
		
		return grupos;
	}
	
	public List<GrupoOperacionCustodia> ProcesarComprasSimultaneas(Connection conn, ReversarFacturacionCustodia_Entrada entrada, List<GrupoOperacionCustodia> grupos) throws Exception {
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa,"PLATAFORMA", "PLA.FechaSistema");
		Timestamp fechaHoraSistema = FechaDB.ObtenerFechaHoraSistemaTs(conn, entrada.p_IdEmpresa);
		
		RetiroCustodia.RetiroCustodiaEntrada retiroCustodiaEntrada = new RetiroCustodia.RetiroCustodiaEntrada();

		configurarEntradaBase(entrada, retiroCustodiaEntrada);
		
		for (GrupoOperacionCustodia g: grupos) {
			RetiroCustodia.ObjDatosEntrada movRetiroCustodia = new RetiroCustodia.ObjDatosEntrada();
			mapearCamposComunes(g.TransitoPrincipal, movRetiroCustodia);
			movRetiroCustodia.TablaAfectada = (!g.TransitoPrincipal.FolioCarteraGarantizada.equals(0L)) ? "GARANTIA" : "CARTERA";
			movRetiroCustodia.AccionOperacion = (!g.TransitoPrincipal.FolioCarteraGarantizada.equals(0L)) ? "ANULACION" : movRetiroCustodia.AccionOperacion;
			movRetiroCustodia.FechaRetiroRegistro = fechaHoraSistema;
			movRetiroCustodia.FechaOperacion = Fecha.toDate(fechaSistema);
			
			retiroCustodiaEntrada.p_DatosEntrada.add(movRetiroCustodia);
			Paso p = new Paso("RETIROCARTERA", movRetiroCustodia, g);
		    g.pasos.add(p);
		}
	    
    	RetiroCustodia servicioRetiro = new RetiroCustodia();
        RetiroCustodiaSalida resRetiro = servicioRetiro.logica(conn, retiroCustodiaEntrada);
        
        for (RetiroCustodia.RetiroCustodiaSalida.ObjDatosSalida datoSalida : resRetiro.P_OUT_DATOSSALIDA) {
			for (GrupoOperacionCustodia grupo : grupos) {
				
				if (!grupo.IdMovimientoInicial.equals(datoSalida.IdGenerarMovimientoCustodia)) {
					continue;
				}
				
				if (datoSalida.RegistroConProblemas.equalsIgnoreCase("N")) {
					grupo.pasos.get(0).estado = "OK";
					grupo.estado = "OK";
					
					String codigo = "UI_TIPO_CARTERA_RETIRADA_FOLIO";
					String descripcion = Diccionario.getMensaje(conn, entrada.p_IdEmpresa, codigo).add("FolioCartera", datoSalida.FolioCustodia).add("TipoCartera", datoSalida.TablaOrigen).msg();
					grupo.pasos.get(0).mensajes.add(new MensajeSalida(datoSalida.IdGenerarMovimientoCustodia,MensajeSalida.MENSAJE_INFORMACION,codigo,descripcion));	

					
				}
				else {
					grupo.pasos.get(0).estado = "ERR";
					grupo.estado = "ERR";
					
					String codigo = "UI_ERROR_AL_RETIRAR_CARTERA"; 
					String descripcion = Diccionario.getMensaje(conn, entrada.p_IdEmpresa, codigo)
							.add("IdMovimientoComprobante", datoSalida.IdGenerarMovimientoCustodia).add("TipoCartera", datoSalida.TablaOrigen).msg();
					grupo.pasos.get(0).mensajes.add(new MensajeSalida(datoSalida.IdGenerarMovimientoCustodia,MensajeSalida.MENSAJE_ERROR,codigo,descripcion));
					
				}
			}
		}
		
        for (GrupoOperacionCustodia grupo : grupos) {
			if (grupo.estado.equalsIgnoreCase("OK")) {
				GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar MOV = new GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar();
				MOV.IdMovimientoCarteraEnTransito = grupo.TransitoPrincipal.IdMovimientoCarteraEnTransito;
				MOV.EsCertificacion = "S";
				MOV.IdUsuario = entrada.p_IdUsuario;
				grupo.transitosActualizar.add(MOV);
			}
		}
       
		return grupos;
	}
	
	private void configurarEntradaBase(ReversarFacturacionCustodia_Entrada entrada, Object destino) {
	    if (destino instanceof RetiroCustodia.RetiroCustodiaEntrada) {
	        RetiroCustodia.RetiroCustodiaEntrada d = (RetiroCustodia.RetiroCustodiaEntrada) destino;
	        d.p_IdEmpresa = entrada.p_IdEmpresa;
	        d.p_IdUsuario = entrada.p_IdUsuario;
	        d.p_IdModulo = entrada.p_IdModulo;
	        d.p_SimularOperacion = entrada.p_simularOperacion; 
	        d.p_TicketAtencion = 0L;
	        d.p_ValorizacionAutomatica = entrada.p_ValorizacionAutomatica;
	    } else if (destino instanceof IngresoCustodia.IngresoCustodiaEntrada) {
	        IngresoCustodia.IngresoCustodiaEntrada d = (IngresoCustodia.IngresoCustodiaEntrada) destino;
	        d.p_IdEmpresa = entrada.p_IdEmpresa;
	        d.p_IdUsuario = entrada.p_IdUsuario;
	        d.p_IdModulo = entrada.p_IdModulo;
	        d.p_SimularOperacion = entrada.p_simularOperacion;
	        d.p_TicketAtencion = 0L;
	        d.p_ValorizacionAutomatica = entrada.p_ValorizacionAutomatica;
	    }
	}

	private void mapearCamposComunes(GestionMovimientoCarteraEnTransito.ObjMovimientosEnTransito origen, Object d) {
	    
	    if (d instanceof IngresoCustodia.ObjDatosEntrada) {
	        IngresoCustodia.ObjDatosEntrada m = (IngresoCustodia.ObjDatosEntrada) d;
	        m.IdGenerarMovimientoCustodia = origen.IdMovimientoCarteraEnTransito;
	        m.IdCustodia                  = origen.IdCustodiaOrigen;
	        m.AccionOperacion			  = "NCE".equalsIgnoreCase(origen.IdTipoComprobante) ? "LIQUIDACION" : "ANULACION";
	        m.FolioCustodia               = origen.FolioCustodia;
	        m.FechaOperacion              = origen.FechaOperacion;
	        m.IdInstrumento               = origen.IdInstrumento;
	        m.Cantidad                    = origen.Cantidad;
	        m.ValorRescate                = origen.ValorRescate;
	        m.IdCliente                   = origen.IdCliente;
	        m.IdBoveda					  = origen.IdBoveda;
	        m.IdPortafolio                = origen.IdPortafolio;
	        m.IdEmisor                    = origen.IdEmisor;
	        m.EsFungible                  = origen.EsFungible;
	        m.IdTipoComprobante           = origen.IdTipoComprobante;
	        m.FolioComprobante            = origen.FolioComprobante;
	        m.IdConcepto                  = origen.IdConcepto;
	        m.IdTipoOperacion             = origen.IdTipoOperacion;
	        m.PrecioCompra                = origen.PrecioCompra;
	        m.TasaCompra				  = origen.TasaCompra;
	        m.MontoCompra                 = origen.MontoCompra;
	        m.FechaVencimientoInstrumento = origen.FechaVencimientoInstrumento;
	        m.FechaVencimientoOperacion   = origen.FechaVencimientoOperacion;
	        m.MontoCompraMonedaInstrumento = origen.MontoCompraMonedaInstrumento;
	        m.Glosa                       = origen.Glosa;
	        m.IdMonedaEmision             = origen.IdMonedaEmision;
	        m.IdTipoReajusteInstrumento   = origen.IdTipoReajuste;
	        m.CorteMinimo                 = origen.CorteMinimo;
	        m.NemoSVS                     = origen.NemoSVS;
	        m.Nemotecnico                 = origen.Nemotecnico;
	        m.IdTipoProducto              = origen.IdTipoProducto;
	        m.IdTipoCanalIngreso          = origen.IdTipoCanalIngreso;
	        m.IdUsuario				      = origen.IdUsuarioIngreso;
	        m.IdAgente					  = origen.IdAgente;
	        m.IdSucursal                  = origen.IdSucursal;
	        m.IdCondicionLiquidacion      = origen.IdCondicionLiquidacion;
	        m.FechaLiquidacion			  = origen.FechaLiquidacion;
			m.IdMonedaInstrumento		  = origen.IdMonedaInstrumento;
			m.FolioExternoReferencia      = origen.FolioExternoReferencia;
			m.EsRegistroNuevo			  = "N";
	    } 
	    else if (d instanceof ControlRetiroObjDatosEntrada) {
	    	ControlRetiroObjDatosEntrada m = (ControlRetiroObjDatosEntrada) d;
	        m.IdGenerarMovimientoCustodia = origen.IdMovimientoCarteraEnTransito;
	        m.IdCustodia				  = origen.IdCustodiaOrigen;
	        m.AccionOperacion			  = "NCE".equalsIgnoreCase(origen.IdTipoComprobante) ? "LIQUIDACION" : "ANULACION";
	        m.FolioCustodia               = origen.FolioCustodia;
	        m.FechaOperacion              = origen.FechaOperacion;
	        m.IdInstrumento               = origen.IdInstrumento;
	        m.Cantidad                    = origen.Cantidad;
	        m.ValorRescate                = origen.ValorRescate;
	        m.IdCliente                   = origen.IdCliente;
	        m.IdPortafolio                = origen.IdPortafolio;
	        m.IdEmisor                    = origen.IdEmisor;
	        m.EsFungible                  = origen.EsFungible;
	        m.IdTipoComprobante           = origen.IdTipoComprobante;
	        m.FolioComprobante            = origen.FolioComprobante;
	        m.IdConcepto                  = origen.IdConcepto;
	        m.IdTipoOperacion             = origen.IdTipoOperacion;
	        m.PrecioCompra                = origen.PrecioCompra;
	        m.TasaCompra				  = origen.TasaCompra;
	        m.MontoCompra                 = origen.MontoCompra;
	        m.MontoCompraMonedaInstrumento = origen.MontoCompraMonedaInstrumento;
	        m.FechaVencimientoInstrumento = origen.FechaVencimientoInstrumento;
	        m.Glosa                       = origen.Glosa;
	        m.IdBoveda                    = origen.IdBoveda;
	        m.IdMonedaEmision             = origen.IdMonedaEmision;
	        m.IdTipoReajusteInstrumento   = origen.IdTipoReajuste;
	        m.CorteMinimo                 = origen.CorteMinimo;
	        m.NemoSVS                     = origen.NemoSVS;
	        m.Nemotecnico                 = origen.Nemotecnico;
	        m.IdTipoProducto              = origen.IdTipoProducto;
	        m.IdTipoCanalIngreso          = origen.IdTipoCanalIngreso;
	        m.IdUsuario				      = origen.IdUsuarioIngreso;
	        m.IdAgente					  = origen.IdAgente;
	        m.IdSucursal                  = origen.IdSucursal;
	        m.IdCondicionLiquidacion      = origen.IdCondicionLiquidacion;
	        m.FechaLiquidacion			  = origen.FechaLiquidacion;
	        m.IdMonedaInstrumento		  = origen.IdMonedaInstrumento;
	        m.FolioExternoReferencia      = origen.FolioExternoReferencia;
	        m.IdClasificacionTipoInstrumento = origen.IdClasificacionTipoInstrumento;
	    } 
	    else if (d instanceof RetiroCustodia.ObjDatosEntrada) {
	    	RetiroCustodia.ObjDatosEntrada m = (RetiroCustodia.ObjDatosEntrada) d;
	        m.IdGenerarMovimientoCustodia = origen.IdMovimientoCarteraEnTransito;
	        m.IdCustodia                  = origen.IdCustodiaOrigen;
	        m.AccionOperacion			  = "NCE".equalsIgnoreCase(origen.IdTipoComprobante) ? "LIQUIDACION" : "ANULACION";
	        m.FolioCustodia               = origen.FolioCustodia;
	        m.FechaOperacion              = origen.FechaOperacion;
	        m.IdInstrumento               = origen.IdInstrumento;
	        m.Cantidad                    = origen.Cantidad;
	        m.ValorRescate                = origen.ValorRescate;
	        m.IdCliente                   = origen.IdCliente;
	        m.IdPortafolio                = origen.IdPortafolio;
	        m.IdEmisor                    = origen.IdEmisor;
	        m.EsFungible                  = origen.EsFungible;
	        m.IdTipoComprobante           = origen.IdTipoComprobante;
	        m.FechaVencimientoInstrumento = origen.FechaVencimientoInstrumento;
	        m.FolioComprobante            = origen.FolioComprobante;
	        m.IdConcepto                  = origen.IdConcepto;
	        m.IdTipoOperacion             = origen.IdTipoOperacion;
	        m.PrecioCompra                = origen.PrecioCompra;
	        m.TasaCompra				  = origen.TasaCompra;
	        m.MontoCompra                 = origen.MontoCompra;
	        m.MontoCompraMonedaInstrumento = origen.MontoCompraMonedaInstrumento;
	        m.Glosa                       = origen.Glosa;
	        m.IdBoveda                    = origen.IdBoveda;
	        m.IdMonedaEmision             = origen.IdMonedaEmision;
	        m.IdTipoReajusteInstrumento   = origen.IdTipoReajuste;
	        m.CorteMinimo                 = origen.CorteMinimo;
	        m.NemoSVS                     = origen.NemoSVS;
	        m.Nemotecnico                 = origen.Nemotecnico;
	        m.IdTipoProducto              = origen.IdTipoProducto;
	        m.IdTipoCanalIngreso          = origen.IdTipoCanalIngreso;
	        m.IdUsuario				      = origen.IdUsuarioIngreso;
	        m.IdAgente					  = origen.IdAgente;
	        m.IdSucursal                  = origen.IdSucursal;
	        m.IdCondicionLiquidacion      = origen.IdCondicionLiquidacion;
	        m.FechaLiquidacion			  = origen.FechaLiquidacion;
	        m.IdMonedaInstrumento		  = origen.IdMonedaInstrumento;
	        m.FolioExternoReferencia      = origen.FolioExternoReferencia;
	        m.FechaVencimientoInstrumento = origen.FechaVencimientoInstrumento;
	        m.IdClasificacionTipoInstrumento = origen.IdClasificacionTipoInstrumento;
	    }
	}

	@Trace
	public ReversarFacturacionCustodia_Salida crearMovimientosEnTransito(Connection conn, ReversarFacturacionCustodia_Entrada entrada) throws Exception {
		logger.info("Inicio ReversarFacturacionCustodia_Salida: " + entrada.toString());
		ReversarFacturacionCustodia_Salida salida = new ReversarFacturacionCustodia_Salida();

		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa,"PLATAFORMA", "PLA.FechaSistema");
		String pasaAGarantiaReporteSimultanea = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "CUSTODIA", "IngresaAGarantiaSimRepDeCompra");
		List<InformacionConfiguracionCustodia.ObjConfiguracionMovimientoCustodia> _configuracion = InformacionConfiguracionCustodia.informacionConfiguracion(conn, entrada.p_IdEmpresa);
		HashMap<String,Object> ConfigCUS = Utils.List2Hash(_configuracion, "IdTipoOperacion");
		Set<String> _clavesVistas = new HashSet<>();
		_configuracion = _configuracion.stream()
		    .filter(_conf -> {
		        return _conf.IdTipoOperacionRelacionada != null 
		            && !_conf.IdTipoOperacionRelacionada.isEmpty();
		    })
		    .filter(_conf -> {
		        return _clavesVistas.add(_conf.IdTipoOperacionRelacionada);
		    })
		    .collect(Collectors.toList());

		HashMap<String, Object> ConfigCUSRel = Utils.List2Hash(_configuracion, "IdTipoOperacionRelacionada");

	    
		// para buscar los movs asociados a los comprobantes
		SQL _sql = new SQL();

		List<Comprobante_Proceso> comprobantes = new ArrayList<>();
		HashMap<Long, Object> hashComprobantesProceso = new HashMap<>();
		List<Long> idsComprobante = entrada.p_Comprobantes.stream().collect(Collectors.toList());
		HashMap<Long, Object> hashComprobantes = new HashMap<>();
		{
			HashMap<String, Object> params = new HashMap<>();
			params.put("idsComprobante", idsComprobante);
			List<Comprobante> _comprobantes = _sql.query(conn, SQL_SELECT_COMPROBANTE, params, Comprobante.class);
			hashComprobantes = Utils.List2Hash(_comprobantes, "IdComprobante");
		}

		for (Long idComprobante : entrada.p_Comprobantes) {
			Comprobante_Proceso c = new Comprobante_Proceso();
			Comprobante cmbte = (Comprobante) hashComprobantes.get(idComprobante);
			Utils.copyPojo(cmbte, c);
			comprobantes.add(c);
			hashComprobantesProceso.put(c.IdComprobante, c);
		}
		
		
		
		HashMap<Long, IndicadoresPortafolio> indicadoresPortafolio = new HashMap<>();
		{
			HashMap<String,Object> paramsInIndicadorPortafolio = new HashMap<>();
			ArrayList<Long> idsPortafolio = new ArrayList<>();
			
			for (Object obj : hashComprobantes.values()) {
				Comprobante cmbte = (Comprobante) obj;
				idsPortafolio.add((cmbte.IdPortafolio));
			}
			
			paramsInIndicadorPortafolio.put("idsPortafolio", idsPortafolio);
			List<IndicadoresPortafolio> _indicadoresPortafolio = _sql.query(conn, SQL_SELECT_INDICADORES_PORTAFOLIO, paramsInIndicadorPortafolio, IndicadoresPortafolio.class);
			HashMap<Object, Object>__indicadoresPortafolio = Utils.List2Hash(_indicadoresPortafolio, "IdPortafolio");
			for(Object key: __indicadoresPortafolio.keySet()) {
				indicadoresPortafolio.put((Long)key,(IndicadoresPortafolio) __indicadoresPortafolio.get(key));
			}
		}
		
		// Primero anulamos los transitos asociados al comprobante
		{
			HashMap<String, Object> params = new HashMap<>();
			params.put("idsComprobante", idsComprobante);
			List<MovimientoCarteraEnTransito_Proceso> transitos = _sql.query(conn, SQL_SELECT_CUSTODIA_TRANSITOS,
					params, MovimientoCarteraEnTransito_Proceso.class);

			List<ObjMovimientosAActualizar> movimientosCarteraEnTransito = new ArrayList<>();
			for (MovimientoCarteraEnTransito_Proceso transito : transitos) {

				Comprobante_Proceso cmbte = (Comprobante_Proceso) hashComprobantesProceso.get(transito.FolioComprobante);
				if (cmbte != null) {
					cmbte.movsCarteraTransito.add(transito);
				}

				ObjMovimientosAActualizar arg0 = new ObjMovimientosAActualizar();
				arg0.EsAnulacion = "S";
				arg0.EsCertificacion = "N";
				arg0.IdMovimientoCarteraEnTransito = transito.IdMovimientoCarteraEnTransito;
				arg0.IdUsuario = entrada.p_IdUsuario;
				movimientosCarteraEnTransito.add(arg0);
			}
			if (movimientosCarteraEnTransito.size() > 0 && entrada.p_simularOperacion.equalsIgnoreCase("N")) {
				boolean flag = GestionMovimientoCarteraEnTransito.ActualizarMovimientoEnTransito(conn, movimientosCarteraEnTransito);
				if(flag) {
					for(ObjMovimientosAActualizar objList:movimientosCarteraEnTransito ) {
						salida.P_OUT_MENSAJES.add(new MensajeSalida(objList.IdMovimientoCarteraEnTransito,MensajeSalida.MENSAJE_OK,"COMPROBANTE_ANULADO","El movimiento en transito "+objList.IdMovimientoCarteraEnTransito+" fue anulado con exito"));
					}
					
				}
			}
		}
		
		// Generacion de transitos para anular
		HashMap<String, Object> params = new HashMap<>();
		params.put("idsComprobante", idsComprobante);
		List<MovimientosParaTransito> carterasCompra = _sql.query(conn, SQL_SELECT_CUSTODIA_CARTERAS_COMPRAS, params,MovimientosParaTransito.class);
		List<MovimientosParaTransito> carterasVenta = _sql.query(conn, SQL_SELECT_CUSTODIA_CARTERAS_VENTAS, params,MovimientosParaTransito.class);
		List<MovimientosParaTransito> carterasNegativas = _sql.query(conn, SQL_SELECT_CUSTODIA_CARTERAS_NEGATIVAS, params, MovimientosParaTransito.class);
		List<MovimientosParaTransito> subyacentes = _sql.query(conn, SQL_SELECT_CUSTODIA_SUBYACENTES, params,MovimientosParaTransito.class);
		List<MovimientosParaTransito> garantiasCompra = _sql.query(conn, SQL_SELECT_CUSTODIA_GARANTIAS_COMPRAS, params, MovimientosParaTransito.class);

		HashMap<Long, List<TransitoAnulacionCompraNormalCustodia>> hashComprobantesAnulacionCompras = new HashMap<>();
		HashMap<Long, List<TransitoAnulacionVentaNormalCustodia>> hashComprobantesAnulacionVentas = new HashMap<>();
		HashMap<Long, List<TransitoAnulacionVentaSimultanea>> hashComprobantesAnulacionVentaSimultanea = new HashMap<>();
		
		
		if(pasaAGarantiaReporteSimultanea.equalsIgnoreCase("S")) {
			
			HashMap<Long, Object> hashComprobanteReferencia = Utils.List2Hash(entrada.p_NotasCreditos, "IdComprobanteReferencia");
			
			for (MovimientosParaTransito cartera : carterasCompra) {
				Comprobante_Proceso info = (Comprobante_Proceso) hashComprobantesProceso.get(cartera.FolioComprobante);
				if (info != null) {
					info.carteras.add(cartera);
				}
				if (cartera.FolioComprobante.equals(info.IdComprobante)) {
					IngresoCustodia.ObjDatosEntrada anulacionCartera = new IngresoCustodia.ObjDatosEntrada();
					anulacionCartera.FolioCustodia = cartera.FolioCartera;
					List<TransitoAnulacionCompraNormalCustodia> lista = hashComprobantesAnulacionCompras.get(info.IdComprobante);
					if (lista == null) {
						lista = new ArrayList<>();
						hashComprobantesAnulacionCompras.put(info.IdComprobante, lista);
					}
					TransitoAnulacionCompraNormalCustodia anulacion = new TransitoAnulacionCompraNormalCustodia(anulacionCartera);
					lista.add(anulacion);
				}
			}
			
			Function<MovimientosParaTransito, IngresoCustodia.ObjDatosEntrada> mapeadorGarantias = (gar) -> {
				
				CrearComprobante.Comprobante_Proceso comprobanteReferencia = (CrearComprobante.Comprobante_Proceso) hashComprobanteReferencia
						.get(gar.FolioComprobante);
				
			    IngresoCustodia.ObjDatosEntrada mov = new IngresoCustodia.ObjDatosEntrada();
			    
			    mov.IdGenerarMovimientoCustodia = gar.IdGarantia;
			    mov.EsRegistroNuevo = "S";
			    mov.TablaAfectada = "TRANSITO";
			    mov.IdCustodia = gar.IdGarantia;
			    mov.FolioCustodia = 0L;
			    mov.IdTipoOperacion = comprobanteReferencia.IdTipoOperacion;
			    mov.AccionOperacion = "INGRESO"; 
			    mov.FechaOperacion = Fecha.toDate(fechaSistema);
			    mov.IdInstrumento = gar.IdInstrumento;
			    mov.Cantidad = gar.INGCantidad;
			    mov.ValorRescate = gar.ValorRescate;
			    mov.IdCliente = gar.IdCliente;
			    mov.IdPortafolio = gar.IdPortafolio;
			    mov.Glosa = "ANULACIÓN GARANTIA SIMULTANEA " + gar.Nemotecnico;
			    mov.FolioCarteraGarantizada = gar.IdGarantia;
			    mov.MontoCompra = gar.MontoCompra;
			    mov.Nemotecnico = gar.Nemotecnico;
			    mov.NemoSVS = gar.NemoSVS;
			    mov.IdEmisor = gar.IdEmisor;
			    mov.IdMonedaInstrumento = gar.IdMonedaInstrumento;
			    mov.IdMonedaEmision = gar.IdMonedaEmision;
			    mov.MontoCompraMonedaInstrumento = gar.MontoCompraMonedaInstrumento;
			    mov.FechaLiquidacion = gar.INGFechaLiquidacion;
				mov.IdCondicionLiquidacion = gar.IdCondicionLiquidacion;
			    mov.IdBoveda = gar.IdBoveda;

			    return mov;
			};
			
			List<MovimientosParaTransito> garantiasATransitar = new ArrayList<>();
			for (MovimientosParaTransito gar : garantiasCompra) {
			    if(gar.IdTipoOperacion.equalsIgnoreCase("COMSIM")) {
			        Comprobante_Proceso info = (Comprobante_Proceso) hashComprobantesProceso.get(gar.FolioComprobante);
			        
			        if (info != null) {
			            info.garantias.add(gar);			  
			            List<TransitoAnulacionCompraNormalCustodia> lista = hashComprobantesAnulacionCompras.get(info.IdComprobante);
			            if (lista != null) {
			                for (TransitoAnulacionCompraNormalCustodia obj : lista) {
			                    if (obj.FolioCartera.compareTo(gar.FolioCartera) == 0) {
			                        garantiasATransitar.add(gar);
			                        break;
			                    }
			                }
			            }
			        }
			    }
			}

			if (!garantiasATransitar.isEmpty()) {
			    IngresoCustodia.IngresoCustodiaSalida salidaTransitoGar = procesarMovimientoTransito(
			        conn, 
			        entrada, 
			        garantiasATransitar, 
			        fechaSistema, 
			        mapeadorGarantias
			    );
			    
			    if(salidaTransitoGar.P_OUT_ERRORESCONTADOR > 0) {
			        salida.P_OUT_MENSAJES.addAll(salidaTransitoGar.P_OUT_MENSAJES);
			        salida.P_OUT_MENSAJES = salida.P_OUT_MENSAJES.stream()
			                .filter(t -> t.tipo.equalsIgnoreCase(MensajeSalida.MENSAJE_ERROR))
			                .collect(Collectors.toList());
			        return salida;
			    }
			    
			    
			    IngresoCustodia.IngresoCustodiaSalida salidaCertificarGar = certificarMovimientoTransito(
			        conn,
			        entrada,
			        salidaTransitoGar.P_OUT_DATOSSALIDA,
			        fechaSistema
			    );
			    
			    salida.P_OUT_MENSAJES.addAll(salidaCertificarGar.P_OUT_MENSAJES);
			    
			    if(salidaCertificarGar.P_OUT_ERRORESCONTADOR > 0) {
			        salida.P_OUT_MENSAJES = salidaCertificarGar.P_OUT_MENSAJES.stream()
			                .filter(t -> t.tipo.equalsIgnoreCase(MensajeSalida.MENSAJE_ERROR))
			                .collect(Collectors.toList());
			        return salida;
			    } else {
					carterasCompra = _sql.query(conn, SQL_SELECT_CUSTODIA_CARTERAS_COMPRAS, params,MovimientosParaTransito.class);
				}
			}
			hashComprobantesAnulacionCompras.clear();
			
		}
		
		IngresoCustodia.IngresoCustodiaEntrada entradaIngresoCustodia = new IngresoCustodia.IngresoCustodiaEntrada();
		
		// VALIDAMOS LAS CARTERAS ANTES DE ANULAR
		for (Comprobante_Proceso cmbte : comprobantes) {
			for (MovimientosParaTransito cartera : carterasCompra) {
				
				IndicadoresPortafolio ind = indicadoresPortafolio.getOrDefault(cmbte.IdPortafolio, new IndicadoresPortafolio());

				if (!cartera.IdEstado.equalsIgnoreCase("VIG") && portafolioPermiteSobregiro(cartera.IdClasificacionTipoInstrumento, ind)) {
					continue;
				}
				
				ProcesaRequisito(conn, entrada.p_IdEmpresa,
						!cartera.IdEstado.equalsIgnoreCase("VIG")
								&& !(cartera.IdTipoOperacion.equals("COMSIM") && cartera.IdEstado.equals("LIQ")),
						"CUSTODIA_VENDIDA_RETIRADA", MensajeSalida.MENSAJE_ERROR, cmbte, salida.P_OUT_MENSAJES);
			}
		}

		if (MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES) > 0) {

			salida.P_OUT_MENSAJES = salida.P_OUT_MENSAJES.stream()
					.filter(t -> t.tipo.equalsIgnoreCase(MensajeSalida.MENSAJE_ERROR)).collect(Collectors.toList());

			logger.info("Fin");
			return salida;
		}

		// ANULACION DE CUSTODIA
		HashMap<Long, CrearComprobante.Comprobante_Proceso> hashComprobanteReferencia = new HashMap<>();
		entrada.p_NotasCreditos.stream().forEach(a -> {
			hashComprobanteReferencia.put(a.IdComprobanteReferencia, a);
		});

		for (MovimientosParaTransito cartera : carterasCompra) {

			Comprobante_Proceso info = (Comprobante_Proceso) hashComprobantesProceso.get(cartera.FolioComprobante);
			CrearComprobante.Comprobante_Proceso comprobanteReferencia = (CrearComprobante.Comprobante_Proceso) hashComprobanteReferencia
					.get(cartera.FolioComprobante);
			if (info != null) {
				info.carteras.add(cartera);
			}

			if (cartera.FolioComprobante.equals(info.IdComprobante)) {
				IngresoCustodia.ObjDatosEntrada transitoAnulacionCartera = new IngresoCustodia.ObjDatosEntrada();

				transitoAnulacionCartera.IdGenerarMovimientoCustodia = cartera.IdCartera;
				transitoAnulacionCartera.TablaAfectada = "TRANSITO";
				transitoAnulacionCartera.IdCustodia = cartera.IdCartera;
				transitoAnulacionCartera.FolioCustodia = cartera.FolioCartera;
				transitoAnulacionCartera.AccionOperacion = "INGRESO";
				transitoAnulacionCartera.FechaOperacion = Fecha.toDate(fechaSistema);
				transitoAnulacionCartera.IdInstrumento = cartera.IdInstrumento;
				transitoAnulacionCartera.Cantidad = cartera.Cantidad;
				transitoAnulacionCartera.ValorRescate = cartera.ValorRescate;
				transitoAnulacionCartera.IdCliente = cartera.IdCliente;
				transitoAnulacionCartera.IdPortafolio = cartera.IdPortafolio;
				transitoAnulacionCartera.IdEmisor = cartera.IdEmisor;
				transitoAnulacionCartera.IdBoveda = cartera.IdBoveda;
				transitoAnulacionCartera.NemoSVS = cartera.NemoSVS;
				transitoAnulacionCartera.Nemotecnico = cartera.Nemotecnico;
				transitoAnulacionCartera.FechaVencimientoInstrumento = cartera.FechaVencimientoInstrumento;
				transitoAnulacionCartera.FechaVencimientoOperacion = cartera.FechaVencimientoOperacion;
				transitoAnulacionCartera.EsFungible = cartera.EsFungible;
				transitoAnulacionCartera.IdTipoReajusteInstrumento = cartera.IdTipoReajuste;
				transitoAnulacionCartera.IdTipoComprobante = comprobanteReferencia != null ? comprobanteReferencia.IdTipoComprobante: "CUS";
				transitoAnulacionCartera.FolioComprobante = comprobanteReferencia != null ? comprobanteReferencia.IdComprobante: 0L;
				transitoAnulacionCartera.IdConcepto = comprobanteReferencia != null ? comprobanteReferencia.IdConcepto : "";
				transitoAnulacionCartera.IdTipoOperacion = comprobanteReferencia != null? comprobanteReferencia.IdTipoOperacion: "";
				transitoAnulacionCartera.IdEstado = cartera.IdEstado;
				transitoAnulacionCartera.EsVentaCorta = cartera.EsVentaCorta;
				transitoAnulacionCartera.FechaLiquidacion = cartera.INGFechaLiquidacion;
				transitoAnulacionCartera.IdCondicionLiquidacion = cartera.IdCondicionLiquidacion;

				transitoAnulacionCartera.PrecioCompra = cartera.INGPrecioCompra;
				transitoAnulacionCartera.MontoCompra = cartera.INGMontoATasaCompraActual;
				transitoAnulacionCartera.EsRegistroNuevo = "N";
				transitoAnulacionCartera.Glosa = "ANULACION " + cartera.Nemotecnico;


				if (cartera.IdTipoOperacion.equalsIgnoreCase("VENSIM")) {
					List<TransitoAnulacionVentaSimultanea> lista = hashComprobantesAnulacionVentaSimultanea.get(info.IdComprobante);
					if (lista == null) {
						lista = new ArrayList<>();
						hashComprobantesAnulacionVentaSimultanea.put(info.IdComprobante, lista);
					}

					TransitoAnulacionVentaSimultanea anulacion = new TransitoAnulacionVentaSimultanea(cartera.FolioCartera,transitoAnulacionCartera);
					lista.add(anulacion);
				} else {
					List<TransitoAnulacionCompraNormalCustodia> lista = hashComprobantesAnulacionCompras.get(info.IdComprobante);
					if (lista == null) {
						lista = new ArrayList<>();
						hashComprobantesAnulacionCompras.put(info.IdComprobante, lista);
					}

					TransitoAnulacionCompraNormalCustodia anulacion = new TransitoAnulacionCompraNormalCustodia(transitoAnulacionCartera);
					lista.add(anulacion);
				}
			}
		}

		HashMap<Long, IngresoCustodia.ObjDatosEntrada> carterasAnular = new HashMap<Long, IngresoCustodia.ObjDatosEntrada>();
		HashMap<Long, IngresoCustodia.ObjDatosEntrada> carterasIngresos = new HashMap<Long, IngresoCustodia.ObjDatosEntrada>();
		HashMap<Long, Long> folioCarteraComprobante = new HashMap<Long, Long>();
		for (MovimientosParaTransito cartera : carterasVenta) {

			CrearComprobante.Comprobante_Proceso comprobanteReferencia = (CrearComprobante.Comprobante_Proceso) hashComprobanteReferencia
					.get(cartera.EGRFolioComprobante);
			// SOLO PROCESAMOS DIRECTAMENTE LAS RPA Y LIQ, LAS VIG SON INTERNAS
			if (cartera.IdEstado.equalsIgnoreCase("RPA") || cartera.IdEstado.equalsIgnoreCase("LIQ")) {

				Comprobante_Proceso info = (Comprobante_Proceso) hashComprobantesProceso
						.get(cartera.EGRFolioComprobante);
				if (info != null) {
					info.carteras.add(cartera);
				}

				IngresoCustodia.ObjDatosEntrada transitoAnulacionCartera = null;

				if ("RPA".equalsIgnoreCase(cartera.IdEstado)) {
					// BUSCAMOS LA CARTERA VIG (VIGENTE) PARA ANULARLA
					MovimientosParaTransito carteraVigente = null;
					for (MovimientosParaTransito carteraVig : carterasVenta) {
						if (carteraVig.IdEstado.equalsIgnoreCase("VIG")
								&& carteraVig.FolioCartera.compareTo(cartera.FolioCartera) == 0) {
							
							carteraVigente = carteraVig;
							break;
						}
					}

					if (carteraVigente != null && carterasAnular.get(carteraVigente.FolioCartera) == null) {
					
						InformacionConfiguracionCustodia.ObjConfiguracionMovimientoCustodia conf = (InformacionConfiguracionCustodia.ObjConfiguracionMovimientoCustodia) ConfigCUS.get(carteraVigente.IdTipoOperacion);	
						InformacionConfiguracionCustodia.ObjConfiguracionMovimientoCustodia confRel = (InformacionConfiguracionCustodia.ObjConfiguracionMovimientoCustodia) ConfigCUSRel.get(conf.IdTipoOperacion);	

						transitoAnulacionCartera = new IngresoCustodia.ObjDatosEntrada();
						transitoAnulacionCartera.IdGenerarMovimientoCustodia = cartera.IdCartera * -1;
						transitoAnulacionCartera.EsRegistroNuevo = "S";
						transitoAnulacionCartera.TablaAfectada = "TRANSITO";
						transitoAnulacionCartera.AccionOperacion = "INGRESO";
						transitoAnulacionCartera.IdCliente = carteraVigente.IdCliente;
						transitoAnulacionCartera.IdPortafolio = carteraVigente.IdPortafolio;
						transitoAnulacionCartera.FolioCustodia = carteraVigente.FolioCartera;
						transitoAnulacionCartera.FechaLiquidacion = carteraVigente.INGFechaLiquidacion;
						transitoAnulacionCartera.IdCustodia = carteraVigente.IdCartera;
						transitoAnulacionCartera.NemoSVS = cartera.NemoSVS;
						transitoAnulacionCartera.Nemotecnico = cartera.Nemotecnico;
						transitoAnulacionCartera.Cantidad = carteraVigente.INGCantidad;
						transitoAnulacionCartera.IdInstrumento = carteraVigente.IdInstrumento;
						transitoAnulacionCartera.ValorRescate = carteraVigente.INGValorRescate;								
						transitoAnulacionCartera.FechaOperacion = Fecha.toDate(fechaSistema);
						transitoAnulacionCartera.Glosa = "ANULACION " + cartera.Nemotecnico;
						transitoAnulacionCartera.IdBoveda = cartera.IdBoveda;

						transitoAnulacionCartera.PrecioCompra = carteraVigente.INGPrecioCompra;
						transitoAnulacionCartera.MontoCompra = carteraVigente.INGMontoATasaCompraActual;
						transitoAnulacionCartera.FechaVencimientoInstrumento = carteraVigente.FechaVencimientoInstrumento;
						transitoAnulacionCartera.FechaVencimientoOperacion = carteraVigente.FechaVencimientoOperacion;
						transitoAnulacionCartera.IdEmisor = carteraVigente.IdEmisor;
						transitoAnulacionCartera.IdCondicionLiquidacion = cartera.IdCondicionLiquidacion;
						
						transitoAnulacionCartera.IdTipoComprobante = carteraVigente.IdTipoComprobante;
						transitoAnulacionCartera.IdConcepto = carteraVigente.IdConcepto;
						transitoAnulacionCartera.IdTipoOperacion = confRel != null? confRel.IdTipoOperacion: comprobanteReferencia.IdTipoOperacion;

						carterasAnular.put(transitoAnulacionCartera.FolioCustodia, transitoAnulacionCartera);
					}

				}
				
				IngresoCustodia.ObjDatosEntrada transitoReingresoCartera = carterasIngresos.get(cartera.FolioCartera);
				if (transitoReingresoCartera == null) {
					
					String IdConceptoEncontrado = null;
					Long cantidad = entrada.p_NotasCreditos.stream().filter(nc -> nc.IdComprobanteReferencia.equals(cartera.EGRFolioComprobante)).count();
					if(cantidad.compareTo(0L) > 0) {
						IdConceptoEncontrado = entrada.p_NotasCreditos.stream().filter(nc -> nc.IdComprobanteReferencia.equals(cartera.EGRFolioComprobante)).findFirst().get().IdConcepto;
					}
					transitoReingresoCartera = new IngresoCustodia.ObjDatosEntrada();
					transitoReingresoCartera.IdGenerarMovimientoCustodia = cartera.IdCartera;
					transitoReingresoCartera.TablaAfectada = "TRANSITO";
					transitoReingresoCartera.AccionOperacion = "INGRESO";
					transitoReingresoCartera.IdCliente = cartera.IdCliente;
					transitoReingresoCartera.IdPortafolio = cartera.IdPortafolio;
					transitoReingresoCartera.FolioCustodia = cartera.FolioCartera;
					transitoReingresoCartera.IdCustodia = cartera.IdCartera;
					transitoReingresoCartera.Cantidad = cartera.EGRCantidad;
					transitoReingresoCartera.IdInstrumento = cartera.IdInstrumento;
					transitoReingresoCartera.ValorRescate = cartera.EGRValorRescate;
					transitoReingresoCartera.FechaOperacion = Fecha.toDate(fechaSistema);
					transitoReingresoCartera.EsRegistroNuevo = "N";
					transitoReingresoCartera.GenerarNuevoFolioCustodia = "N";
					transitoReingresoCartera.PlazoEnDiasOperacion = cartera.PlazoEnDiasOperacion;
					transitoReingresoCartera.IdEmisor = cartera.IdEmisor;
					transitoReingresoCartera.NemoSVS = cartera.NemoSVS;
					transitoReingresoCartera.Nemotecnico = cartera.Nemotecnico;
					transitoReingresoCartera.IdTipoReajusteInstrumento = cartera.IdTipoReajuste;
					transitoReingresoCartera.IdMonedaEmision = cartera.IdMonedaEmision;
					transitoReingresoCartera.PrecioCompra = cartera.INGPrecioCompra;
					transitoReingresoCartera.FechaLiquidacion = cartera.INGFechaLiquidacion;
					transitoReingresoCartera.IdCondicionLiquidacion = cartera.IdCondicionLiquidacion;
					transitoReingresoCartera.IdConcepto = IdConceptoEncontrado;
					transitoReingresoCartera.IdBoveda = cartera.IdBoveda;
					transitoReingresoCartera.EsRegistroNuevo = "N";
					transitoReingresoCartera.FechaVencimientoInstrumento = cartera.FechaVencimientoInstrumento;
					transitoReingresoCartera.FechaVencimientoOperacion = cartera.FechaVencimientoOperacion;
					transitoReingresoCartera.IdTipoComprobante = comprobanteReferencia != null ? comprobanteReferencia.IdTipoComprobante: "CUS";
					transitoReingresoCartera.IdConcepto = comprobanteReferencia != null ? comprobanteReferencia.IdConcepto : "";
					transitoReingresoCartera.FolioComprobante = cartera != null ? cartera.FolioComprobante : 0L;
					transitoReingresoCartera.IdTipoOperacion = comprobanteReferencia != null? comprobanteReferencia.IdTipoOperacion: "";
					transitoReingresoCartera.Glosa = "ANULACION " + cartera.Nemotecnico;
					
					if (transitoAnulacionCartera != null) {
						if (cartera.EsFungible.equalsIgnoreCase("S")) {
							transitoReingresoCartera.Cantidad = BigDecimal.ONE;
							transitoReingresoCartera.ValorRescate = transitoReingresoCartera.ValorRescate
									.add(transitoAnulacionCartera.ValorRescate);
						} else {
							transitoReingresoCartera.Cantidad = transitoReingresoCartera.Cantidad.add(transitoAnulacionCartera.Cantidad);
							transitoReingresoCartera.ValorRescate = transitoReingresoCartera.ValorRescate
									.add(transitoAnulacionCartera.ValorRescate);
						}

					}
					folioCarteraComprobante.put(transitoReingresoCartera.FolioCustodia, info.IdComprobante);
				} else {
					transitoReingresoCartera.Cantidad = transitoReingresoCartera.Cantidad.add(cartera.EGRCantidad,
							MathContext.DECIMAL128);
					transitoReingresoCartera.ValorRescate = transitoReingresoCartera.ValorRescate.add(cartera.EGRValorRescate,
							MathContext.DECIMAL128);
				}
				carterasIngresos.put(transitoReingresoCartera.FolioCustodia, transitoReingresoCartera);	 
			}
		}
		
		
		// NEGATIVAS
		for (MovimientosParaTransito cartera : carterasNegativas) {
			Comprobante_Proceso info = (Comprobante_Proceso) hashComprobantesProceso.get(cartera.FolioComprobante);
			CrearComprobante.Comprobante_Proceso comprobanteReferencia = (CrearComprobante.Comprobante_Proceso) hashComprobanteReferencia
					.get(cartera.FolioComprobante);
			if (info != null) {
				info.carteras.add(cartera);
			}
			
			IngresoCustodia.ObjDatosEntrada transitoReingresoCartera = carterasIngresos.get(cartera.FolioCartera);

			transitoReingresoCartera = new IngresoCustodia.ObjDatosEntrada();
			transitoReingresoCartera.IdGenerarMovimientoCustodia = cartera.IdCartera;
			transitoReingresoCartera.TablaAfectada = "TRANSITO";
			transitoReingresoCartera.AccionOperacion = "INGRESO";
			transitoReingresoCartera.IdCliente = cartera.IdCliente;
			transitoReingresoCartera.IdPortafolio = cartera.IdPortafolio;
			transitoReingresoCartera.FolioCustodia = cartera.FolioCartera;
			transitoReingresoCartera.IdCustodia = cartera.IdCartera;
			transitoReingresoCartera.Cantidad = cartera.Cantidad;
			transitoReingresoCartera.IdInstrumento = cartera.IdInstrumento;
			transitoReingresoCartera.ValorRescate = cartera.ValorRescate;
			transitoReingresoCartera.FechaOperacion = Fecha.toDate(fechaSistema);
			transitoReingresoCartera.EsRegistroNuevo = "N";
			transitoReingresoCartera.GenerarNuevoFolioCustodia = "N";
			transitoReingresoCartera.PlazoEnDiasOperacion = cartera.PlazoEnDiasOperacion;
			transitoReingresoCartera.IdCondicionLiquidacion = cartera.IdCondicionLiquidacion;
			transitoReingresoCartera.IdEmisor = cartera.IdEmisor;
			transitoReingresoCartera.IdTipoReajusteInstrumento = cartera.IdTipoReajuste;
			transitoReingresoCartera.IdMonedaEmision = cartera.IdMonedaEmision;
			transitoReingresoCartera.PrecioCompra = cartera.PrecioCompra;
			transitoReingresoCartera.FechaLiquidacion = cartera.FechaLiquidacion;
			transitoReingresoCartera.IdTipoComprobante = comprobanteReferencia != null ? comprobanteReferencia.IdTipoComprobante: "CUS";
			transitoReingresoCartera.FolioComprobante = comprobanteReferencia != null ? comprobanteReferencia.IdComprobante: 0L;
			transitoReingresoCartera.IdConcepto = comprobanteReferencia != null ? comprobanteReferencia.IdConcepto : "";
			transitoReingresoCartera.IdTipoOperacion = comprobanteReferencia != null? comprobanteReferencia.IdTipoOperacion: "";
			transitoReingresoCartera.EsRegistroNuevo = "N";
			transitoReingresoCartera.Glosa = "ANULACION CUSTODIA NEGATIVA " + cartera.Nemotecnico;
			transitoReingresoCartera.IdMovimientoCarteraEnTransitoRelacionado = cartera.IdCartera;
			entradaIngresoCustodia.p_DatosEntrada.add(transitoReingresoCartera);
		}
		
		for (IngresoCustodia.ObjDatosEntrada reingresoCartera : carterasIngresos.values()) {
			Long idComprobante = folioCarteraComprobante.get(reingresoCartera.FolioCustodia);
			List<TransitoAnulacionVentaNormalCustodia> lista = hashComprobantesAnulacionVentas.get(idComprobante);
			if (lista == null) {
				lista = new ArrayList<>();
			}
			
			if(pasaAGarantiaReporteSimultanea.equalsIgnoreCase("S") && "ANCSIM".equalsIgnoreCase(reingresoCartera.IdConcepto)) {
				continue;
			}
			
			IngresoCustodia.ObjDatosEntrada transitoAnulacionCartera = carterasAnular.get(reingresoCartera.FolioCustodia);
			TransitoAnulacionVentaNormalCustodia transitoAnulacion = new TransitoAnulacionVentaNormalCustodia(transitoAnulacionCartera,
					reingresoCartera);
			lista.add(transitoAnulacion);
			hashComprobantesAnulacionVentas.put(idComprobante, lista);
		}
		

		// TRANSITO SUBYACENTES
		for (MovimientosParaTransito suby : subyacentes) {
			Comprobante_Proceso info = (Comprobante_Proceso) hashComprobantesProceso.get(suby.FolioComprobante);
			if (info != null) {
				info.subyacentes.add(suby);
			}

			IngresoCustodia.ObjDatosEntrada transitoAnulacionCartera = new IngresoCustodia.ObjDatosEntrada();

			transitoAnulacionCartera.IdGenerarMovimientoCustodia = suby.IdSubyacente;
			transitoAnulacionCartera.TablaAfectada = "TRANSITO";
			transitoAnulacionCartera.IdCustodia = suby.IdSubyacente;
			transitoAnulacionCartera.FolioCustodia = suby.FolioSubyacente;
			transitoAnulacionCartera.IdTipoOperacion = suby.IdTipoOperacionAnulacion;
			transitoAnulacionCartera.AccionOperacion = "INGRESO";
			transitoAnulacionCartera.FechaOperacion = Fecha.toDate(fechaSistema);
			transitoAnulacionCartera.IdInstrumento = suby.IdInstrumento;
			transitoAnulacionCartera.Cantidad = suby.INGCantidad;
			transitoAnulacionCartera.ValorRescate = suby.INGValorRescate;
			transitoAnulacionCartera.IdCliente = suby.IdCliente;
			transitoAnulacionCartera.IdPortafolio = suby.IdPortafolio;
			transitoAnulacionCartera.IdBoveda = suby.IdBoveda;
			transitoAnulacionCartera.Glosa = "ANULACION SUBYACENTE " + suby.Nemotecnico; 
			transitoAnulacionCartera.EsRegistroNuevo = "N";
			transitoAnulacionCartera.IdMovimientoCarteraEnTransitoRelacionado = suby.FolioCarteraRelacionada;

			List<TransitoAnulacionVentaSimultanea> lista = hashComprobantesAnulacionVentaSimultanea
					.get(info.IdComprobante) == null ? new ArrayList<>()
							: hashComprobantesAnulacionVentaSimultanea.get(info.IdComprobante);

			for (TransitoAnulacionVentaSimultanea obj : lista) {
				if (obj.FolioCartera.compareTo(suby.FolioCarteraRelacionada) == 0) {
					obj.pasoTransitoRetiroSubyacente.add(transitoAnulacionCartera);
				}
			}

		}

		// ANULAR COMPRAS Y VENTAS PARCIALES
		for (TransitoAnulacionVentaSimultanea retiro : hashComprobantesAnulacionVentaSimultanea.values().stream().flatMap(List::stream).collect(Collectors.toList())) {
		    if (retiro.pasoTransitoAnulacionCabecera != null) {
		        entradaIngresoCustodia.p_DatosEntrada.add(retiro.pasoTransitoAnulacionCabecera);
		    }
		}
		for (TransitoAnulacionVentaSimultanea retiro : hashComprobantesAnulacionVentaSimultanea.values().stream().flatMap(List::stream).collect(Collectors.toList())) {
		    if (retiro.pasoTransitoRetiroSubyacente != null) {
		        entradaIngresoCustodia.p_DatosEntrada.addAll(retiro.pasoTransitoRetiroSubyacente);
		    }
		}
		for (TransitoAnulacionCompraNormalCustodia retiro : hashComprobantesAnulacionCompras.values().stream().flatMap(List::stream).collect(Collectors.toList())) {
		    if (retiro.pasoTransitoAnulacionCartera != null) {
		        if((pasaAGarantiaReporteSimultanea.equalsIgnoreCase("S") && retiro.pasoTransitoAnulacionCartera.IdTipoOperacion.equalsIgnoreCase("ANCSIM")))
		        {
		            if(retiro.pasoTransitoAnulacionCartera.IdEstado.equalsIgnoreCase("VIG")) {
		                entradaIngresoCustodia.p_DatosEntrada.add(retiro.pasoTransitoAnulacionCartera);
		            }

		        }else {
		            entradaIngresoCustodia.p_DatosEntrada.add(retiro.pasoTransitoAnulacionCartera);
		        }

		    }
		}
		for (TransitoAnulacionVentaNormalCustodia retiro : hashComprobantesAnulacionVentas.values().stream().flatMap(List::stream).collect(Collectors.toList())) {
		    if (retiro.pasoTransitoAnulacionCartera != null) {
		        entradaIngresoCustodia.p_DatosEntrada.add(retiro.pasoTransitoAnulacionCartera);
		    }
		}

		for (TransitoAnulacionVentaNormalCustodia retiro : hashComprobantesAnulacionVentas.values().stream().flatMap(List::stream).collect(Collectors.toList())) {
		    if (retiro.pasoTransitoIngresoCartera != null) {
		        entradaIngresoCustodia.p_DatosEntrada.add(retiro.pasoTransitoIngresoCartera);
		    }
		}
		
		if (entradaIngresoCustodia.p_DatosEntrada.size() > 0) {
			List<Long> idsFolioPrestamo = new ArrayList<>();
			HashMap<Long, IngresoCustodia.ObjDatosEntrada> hashObjDatosEntrada = new HashMap<>();
			for (IngresoCustodia.ObjDatosEntrada reg : entradaIngresoCustodia.p_DatosEntrada) {
				idsFolioPrestamo.add(reg.FolioCustodia);
				hashObjDatosEntrada.put(reg.FolioCustodia, reg);
			}

			HashMap<String, Object> paramsFolioPrestamo = new HashMap<>();
			paramsFolioPrestamo.put("FolioPrestamo", idsFolioPrestamo);
			List<FoliosPres> foliosPrestamos = _sql.query(conn, SQL_SELECT_PRESTAMOS_VIGENTES, paramsFolioPrestamo,
					FoliosPres.class);

			for (FoliosPres folio : foliosPrestamos) {
				if (folio.CantidadAsignada.compareTo(BigDecimal.ZERO) > 0) {
					entradaIngresoCustodia.p_DatosEntrada.remove(hashObjDatosEntrada.get(folio.FolioPrestamo));

					String codigo = "NOTA_DE_CREDITO_VC_PRESTAMISTAS";
					String descripcion = Diccionario.getMensaje(conn, entrada.p_IdEmpresa, codigo)
							.add("FolioPrestamo", folio.FolioPrestamo).msg();
					salida.P_OUT_MENSAJES.add(new MensajeSalida(folio.FolioPrestamo, MensajeSalida.MENSAJE_ERROR,
							codigo, descripcion));
				}
			}

			entradaIngresoCustodia.p_IdEmpresa = entrada.p_IdEmpresa;		
		    entradaIngresoCustodia.p_IdModulo = entrada.p_IdModulo;
		    entradaIngresoCustodia.p_IdUsuario = entrada.p_IdUsuario;
		    entradaIngresoCustodia.p_SimularOperacion = "N";
		    entradaIngresoCustodia.p_TicketAtencion = 0L;
		    entradaIngresoCustodia.p_ValorizacionAutomatica = "N";


			if (entrada.p_NotasCreditos.get(0).FechaLiquidacion != null) {
				entradaIngresoCustodia.p_DatosEntrada.get(0).FechaLiquidacion = entrada.p_NotasCreditos
						.get(0).FechaLiquidacion;
			} else {
				String sDate1 = "0001-01-01";
				Date date = Date.valueOf(sDate1);
				entradaIngresoCustodia.p_DatosEntrada.get(0).FechaLiquidacion = date;
			}
			if (entradaIngresoCustodia.p_DatosEntrada.size() == 0) {
				return salida;
			}
			
			IngresoCustodia ingresoCustodia = new IngresoCustodia();
		    IngresoCustodia.IngresoCustodiaSalida salidaIngresoCustodia = ingresoCustodia.logica(conn, entradaIngresoCustodia);			
		    logger.debug("Salida Ingreso Custodia:" + salidaIngresoCustodia);

			salida.P_OUT_MENSAJES.addAll(salidaIngresoCustodia.P_OUT_MENSAJES);

			if (MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES) > 0) {
				salida.P_OUT_MENSAJES = salida.P_OUT_MENSAJES.stream()
						.filter(t -> t.tipo.equalsIgnoreCase(MensajeSalida.MENSAJE_ERROR))
						.collect(Collectors.toList());
				logger.debug("Fin");
				logger.debug(salida);
				return salida;
			}
			
			IngresoCustodia.IngresoCustodiaSalida salidaCertificarGar = certificarMovimientoTransito(
		        conn,
		        entrada,
		        salidaIngresoCustodia.P_OUT_DATOSSALIDA,
		        fechaSistema
		    );
			    
			if (MensajeSalida.getNumeroErrores(salidaCertificarGar.P_OUT_MENSAJES) > 0) {
				salida.P_OUT_MENSAJES = salidaCertificarGar.P_OUT_MENSAJES.stream()
						.filter(t -> t.tipo.equalsIgnoreCase(MensajeSalida.MENSAJE_ERROR))
						.collect(Collectors.toList());
				return salida;
			}
			
			salida.P_OUT_MENSAJES.addAll(salidaCertificarGar.P_OUT_MENSAJES);
		}
		return salida;
	}
	
	
	private void ProcesaRequisito(Connection conn, Long idEmpresa, boolean esError, String codMsg, String codRequisito,
			Comprobante_Proceso cmbte, List<MensajeSalida> P_OUT_MENSAJES) throws Exception {
		if (esError) {
			String msg = Diccionario.getMensaje(conn, idEmpresa, codMsg).add("IdComprobante", cmbte.IdComprobante)
					.msg();

			if (codRequisito.equals(MensajeSalida.MENSAJE_ERROR)) {
				P_OUT_MENSAJES.add(new MensajeSalida(cmbte.IdComprobante, MensajeSalida.MENSAJE_ERROR, codMsg, msg));
				cmbte.errores++;
			} else if (codRequisito.equals(MensajeSalida.MENSAJE_AUTORIZACION)) {
				P_OUT_MENSAJES.add(new MensajeSalida(cmbte.IdComprobante, MensajeSalida.MENSAJE_ERROR, codMsg, msg));
				cmbte.errores++;
			} else if (codRequisito.equals(MensajeSalida.MENSAJE_AUTORIZACION)) {
				P_OUT_MENSAJES
						.add(new MensajeSalida(cmbte.IdComprobante, MensajeSalida.MENSAJE_AUTORIZACION, codMsg, msg));
				cmbte.autorizaciones++;
			} else if (codRequisito.equals(MensajeSalida.MENSAJE_ADVERTENCIA)) {
				P_OUT_MENSAJES
						.add(new MensajeSalida(cmbte.IdComprobante, MensajeSalida.MENSAJE_ADVERTENCIA, codMsg, msg));
				cmbte.advertencias++;
			} else {
				cmbte.ok++;
			}
		} else {
			cmbte.ok++;
		}
	}
	
	private boolean portafolioPermiteSobregiro(String clasificacion, IndicadoresPortafolio indicador) {

		if ("RV".equalsIgnoreCase(clasificacion) && "S".equalsIgnoreCase(indicador.SobregiroRV))  {
			return true;
		} else if ("RF".equalsIgnoreCase(clasificacion) && "S".equalsIgnoreCase(indicador.SobregiroRF)) {
			return true;
		} else if ("IF".equalsIgnoreCase(clasificacion) && "S".equalsIgnoreCase(indicador.SobregiroIF)) {
			return true;
		}
		
		return false;
	}
	
	private <T> IngresoCustodia.IngresoCustodiaSalida procesarMovimientoTransito(
	        Connection con, 
	        ReversarFacturacionCustodia_Entrada entrada, 
	        List<MovimientosParaTransito> carterasCompra, 
	        String fechaSistema,
	        Function<MovimientosParaTransito, cl.bcs.custodia.utils.IngresoCustodia.ObjDatosEntrada> mapeadorCarterasCompra) throws Exception {
	    
	    logger.trace("INICIO::ReversarFacturacionCustodia.procesarMovimientoTransito");
	            
	    IngresoCustodia.IngresoCustodiaEntrada entradaIngresoCustodia = new IngresoCustodia.IngresoCustodiaEntrada();
	    entradaIngresoCustodia.p_IdEmpresa = entrada.p_IdEmpresa;		
	    entradaIngresoCustodia.p_IdModulo = entrada.p_IdModulo;
	    entradaIngresoCustodia.p_IdUsuario = entrada.p_IdUsuario;
	    entradaIngresoCustodia.p_SimularOperacion = "N";
	    entradaIngresoCustodia.p_TicketAtencion = 0L;
	    entradaIngresoCustodia.p_ValorizacionAutomatica = "N";
	    
	    entradaIngresoCustodia.p_DatosEntrada = carterasCompra.stream()
	            .map(mapeadorCarterasCompra)
	            .collect(Collectors.toList());
	    
	    IngresoCustodia ingresoCustodia = new IngresoCustodia();
	    IngresoCustodia.IngresoCustodiaSalida salidaIngresoCustodia = ingresoCustodia.logica(con, entradaIngresoCustodia);
	    
	    logger.trace("FIN::ReversarFacturacionCustodia.procesarMovimientoTransito");
	    return salidaIngresoCustodia;
	}
	
	private IngresoCustodia.IngresoCustodiaSalida certificarMovimientoTransito(
			Connection con,
			ReversarFacturacionCustodia_Entrada entrada,
			List<IngresoCustodiaSalida.ObjDatosSalida> carterasTransito,
			String fechaSistema) throws Exception {
		
		logger.trace("INICIO::ReversarFacturacionCustodia.certificarMovimientoTransito");
		
		List<Long> idMovimientosTransito = carterasTransito
				.stream()
				.map(carteraTransito->carteraTransito.IdCustodia)
				.collect(Collectors.toList());
		
		List<GestionMovimientoCarteraEnTransito.ObjMovimientosEnTransito> movimientosEnTransito = GestionMovimientoCarteraEnTransito.ObtenerMovimientoEnTransito(con, idMovimientosTransito);
		
		CertificarMovimientosEnTransito.CertificarMovimientosEnTransitoEntrada entradaCertificarMovimientosEnTransito = new CertificarMovimientosEnTransito.CertificarMovimientosEnTransitoEntrada();
		entradaCertificarMovimientosEnTransito.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCertificarMovimientosEnTransito.p_IdUsuario = entrada.p_IdUsuario;
		entradaCertificarMovimientosEnTransito.p_IdModulo = entrada.p_IdModulo;
		entradaCertificarMovimientosEnTransito.p_FechaOperacion = fechaSistema;
		entradaCertificarMovimientosEnTransito.P_MOVIMIENTOSCERTIFICAR = movimientosEnTransito
				.stream()
				.map(movimientoEnTrasito->{
					CertificarMovimientosEnTransito.ObjMovimientos movimientoCertificar = new CertificarMovimientosEnTransito.ObjMovimientos();
					movimientoCertificar.IdMovimientoCarteraEnTransito = movimientoEnTrasito.IdMovimientoCarteraEnTransito;
					movimientoCertificar.IdTipoOperacion = movimientoEnTrasito.IdTipoOperacion;
					return movimientoCertificar;
				})
				.collect(Collectors.toList());
		
		CertificarMovimientosEnTransito certificarMovimientosEnTransito = new CertificarMovimientosEnTransito();
		
		CertificarMovimientosEnTransito.CertificarMovimientosEnTransitoSalida salidaCertificarMovimientosEnTrasito = certificarMovimientosEnTransito.logica(con, entradaCertificarMovimientosEnTransito);
		
		IngresoCustodia.IngresoCustodiaSalida out = new IngresoCustodia.IngresoCustodiaSalida();
		out.P_OUT_ERRORESCONTADOR = salidaCertificarMovimientosEnTrasito.P_OUT_ERRORESCONTADOR;
		out.P_OUT_MENSAJES.addAll(salidaCertificarMovimientosEnTrasito.P_OUT_MENSAJES);
		
		logger.trace("FIN::ReversarFacturacionCustodia.certificarMovimientoTransito");
		return out;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

};