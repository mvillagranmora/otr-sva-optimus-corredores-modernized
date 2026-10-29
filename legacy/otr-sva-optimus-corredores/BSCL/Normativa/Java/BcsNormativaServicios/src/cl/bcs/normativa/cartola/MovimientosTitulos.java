package cl.bcs.normativa.cartola;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.custodia.ConsultarCustodiaMovimientos;
import cl.bcs.custodia.ConsultarCustodiaMovimientos.SalidaMovimiento;
import cl.bcs.custodia.ConsultarCustodiaMovimientos.EntradaConsultarCustodiaMovimientos;
import cl.bcs.custodia.ConsultarCustodiaMovimientos.SalidaConsultarCustodiaMovimientos;
import cl.bcs.normativa.cartola.pojo.TipoOperacion;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class MovimientosTitulos implements BcsCartola{
	
	final static Logger logger = LogManager.getLogger(MovimientosTitulos.class);

	private final String SQL_SELECT_TIPO_OPERACION = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_SelectTipoOperacion.sql");
	private final String SQL_SELECT_MOVIMIENTOS_LEGACY = loadResourceToString("cl/bcs/normativa/cartola/sql/MovimientosTitulos_SelectMovimientosLegacy.sql");
	private final String SQL_SELECT_MOVIMIENTOS_NOMINALES = loadResourceToString("cl/bcs/normativa/cartola/sql/MovimientosTitulos_SelectMovimientosNominales.sql");
	private final String SQL_INSERT_PORTAFOLIOS_LEGACY = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_InsertPortafoliosLegacy.sql");
	private final String SQL_DELETE_PORTAFOLIOS_LEGACY = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_DeletePortafoliosLegacy.sql");
	private final String SQL_SELECT_MOVIMIENTOS_ADL = loadResourceToString("cl/bcs/normativa/cartola/sql/MovimientosTitulos_SelectMovimientosADL.sql");
	
	public static class SalidaMovimientosTitulos{
		public Long IdFolioCartera;
		public Long IdCliente;
		public Long IdPortafolio;
		public String IdTipoComprobante;
		public Long FolioComprobante;
		public String Accion;
		public String IdTipoOperacion;
		public String FechaOperacion;
		public String IdClasificacionTipoInstrumento;
		public String NombreTipoOperacion;
		public String Nemotecnico;
		public String Emisor;
		public String IdTipoReajuste;
		public String IdMonedaInstrumento;
		public String IdConcepto;
		public String NombreConcepto;
		public BigDecimal Cantidad;
		public String CantidadString;
		public BigDecimal PrecioMercado;
		public BigDecimal TasaMercado;
		public BigDecimal PrecioCompra;
		public BigDecimal TasaCompra;
		public BigDecimal MontoCompraMonedaInstrumento;
		public BigDecimal MontoMercadoMonedaLocal;
		public BigDecimal MontoMercadoMonedaInstrumento;
		public BigDecimal MontoMercadoMonedaAdicional;
		public String IdEstado;
		public String TipoConcepto; // nuevo
		public String IdTipoInstrumento;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Portafolio {
		public Long IdPortafolio;
		public Long IdTrabajo;
	}
	
	public static class MovimientoADL{
		public Long FolioComprobante;
		public String Overnight;
		public String AdministracionLiquidez;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public SalidaCartola procesar(Connection con,EntradaCartola entrada) throws Exception{
		logger.info("INICIO::MovimientosTitulos");
		SalidaCartola out = new SalidaCartola();
		SQL sql = new SQL();
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		
		ConsultarCustodiaMovimientos consultarCustodiaMovimientos = new ConsultarCustodiaMovimientos();
		
		
		EntradaConsultarCustodiaMovimientos entradaConsultarCustodiaMovimientos = new EntradaConsultarCustodiaMovimientos();
		entradaConsultarCustodiaMovimientos.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaConsultarCustodiaMovimientos.p_IdUsuario = entrada.p_IdUsuario;
		entradaConsultarCustodiaMovimientos.p_IdModulo = entrada.p_IdModulo;
		entradaConsultarCustodiaMovimientos.p_FechaInicio = Fecha.stringToSqlDate(entrada.p_FechaDesde);
		entradaConsultarCustodiaMovimientos.p_FechaTermino = Fecha.stringToSqlDate(entrada.p_FechaHasta);
		
		List<Portafolio> portObjs = new ArrayList<>();
		Long idTrabajo = new Double(Math.random()*10000000000L).longValue();

		for(Dato dato : entrada.p_Datos){
			ConsultarCustodiaMovimientos.VariableProceso variableProceso = new ConsultarCustodiaMovimientos.VariableProceso();
			variableProceso.IdCliente = dato.IdCliente;
			variableProceso.IdPortafolio = dato.IdPortafolio;
			variableProceso.FolioCartera = 0L;
			variableProceso.IdClasificacionTipoInstrumento = "";
			variableProceso.IdTipoInstrumento = "";
			variableProceso.IdInstrumento = 0L;
			entradaConsultarCustodiaMovimientos.p_VariablesProceso.add(variableProceso);
			Portafolio p = new Portafolio();
			p.IdPortafolio = dato.IdPortafolio;
			p.IdTrabajo = idTrabajo;
			portObjs.add(p);
		}
		
		SalidaConsultarCustodiaMovimientos salidaConsultarCustodiaMovimientos = consultarCustodiaMovimientos.logica(con, entradaConsultarCustodiaMovimientos);

		List<SalidaMovimiento> movimientosAnulados = new ArrayList<SalidaMovimiento>();
		
		if ( salidaConsultarCustodiaMovimientos.P_OUT_TABLA.size() > 0) {

			for(SalidaMovimiento movimiento : salidaConsultarCustodiaMovimientos.P_OUT_TABLA) {
				if(("ANURES").equalsIgnoreCase(movimiento.INGIdTipoOperacion) || ("ANUAPO").equalsIgnoreCase(movimiento.INGIdTipoOperacion)) {
					movimientosAnulados.add(movimiento);
				}
			}

            for(SalidaMovimiento movimientoEliminado: movimientosAnulados) {
                salidaConsultarCustodiaMovimientos.P_OUT_TABLA.remove(movimientoEliminado);
            }
        }

		HashMap<String,SalidaMovimiento> hashMovimientos = new HashMap<String,SalidaMovimiento>();
		
		if ( salidaConsultarCustodiaMovimientos.P_OUT_TABLA.size() > 0) {
		
			//Obtiene Fecha GoLive
			String fechaGoLive = "0001-01-01";
	
			try {
				fechaGoLive = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaGoLive");
				Fecha.toDate(fechaGoLive);
			}
			catch(Exception ex) {
				fechaGoLive = "0001-01-01";
			}
	
			
			if (fechaGoLive != "0001-01-01") {
				for(int i=0; i < salidaConsultarCustodiaMovimientos.P_OUT_TABLA.size(); i++) {
					if (Fecha.toDate(fechaGoLive).after(salidaConsultarCustodiaMovimientos.P_OUT_TABLA.get(i).FechaOperacion) && salidaConsultarCustodiaMovimientos.P_OUT_TABLA.get(i).IdCarga > 0) {
						salidaConsultarCustodiaMovimientos.P_OUT_TABLA.remove(i);
						i = i-1;
					}else {
						if("S".equalsIgnoreCase(entrada.p_AgrupaMovimientos)) {

							String idHash = salidaConsultarCustodiaMovimientos.P_OUT_TABLA.get(i).FechaOperacion.toString() + '-' + salidaConsultarCustodiaMovimientos.P_OUT_TABLA.get(i).FolioComprobante + '-' + salidaConsultarCustodiaMovimientos.P_OUT_TABLA.get(i).Nemotecnico + '-' +salidaConsultarCustodiaMovimientos.P_OUT_TABLA.get(i).PrecioTir + '-' +salidaConsultarCustodiaMovimientos.P_OUT_TABLA.get(i).TipoOperacion;

							if(hashMovimientos.containsKey(idHash)){
								hashMovimientos.get(idHash).Cantidad = hashMovimientos.get(idHash).Cantidad.add(salidaConsultarCustodiaMovimientos.P_OUT_TABLA.get(i).Cantidad, MathContext.DECIMAL128);
								hashMovimientos.get(idHash).Monto = hashMovimientos.get(idHash).Monto.add(salidaConsultarCustodiaMovimientos.P_OUT_TABLA.get(i).Monto, MathContext.DECIMAL128);
							}else {

								hashMovimientos.put(idHash, salidaConsultarCustodiaMovimientos.P_OUT_TABLA.get(i));
							}
						}
					}
					
				}			
			}
		}

		if("S".equalsIgnoreCase(entrada.p_AgrupaMovimientos)) {
			salidaConsultarCustodiaMovimientos.P_OUT_TABLA = new ArrayList<SalidaMovimiento>(hashMovimientos.values());	
		}
		
		List<String> idTiposOperaciones = new ArrayList<String>();
		
		for(ConsultarCustodiaMovimientos.SalidaMovimiento mov : salidaConsultarCustodiaMovimientos.P_OUT_TABLA){
			if(!idTiposOperaciones.contains(mov.IdTipoOperacion)) idTiposOperaciones.add(mov.IdTipoOperacion);
		}
		
		params.put("IdTipoOperacion", idTiposOperaciones);
		List<TipoOperacion> tipoOperaciones = sql.query(con, SQL_SELECT_TIPO_OPERACION, params, TipoOperacion.class);
		HashMap<String,Object> hTiposOperacion = Utils.List2Hash(tipoOperaciones, "IdTipoOperacion");
		
		
		salidaConsultarCustodiaMovimientos.P_OUT_TABLA.sort(new Comparator<ConsultarCustodiaMovimientos.SalidaMovimiento>() {
		    @Override
		    public int compare(ConsultarCustodiaMovimientos.SalidaMovimiento mov1, ConsultarCustodiaMovimientos.SalidaMovimiento mov2) {
		    	
		        if(mov1.FechaRegistro.compareTo(mov2.FechaRegistro) != 0){
		        	return mov1.FechaRegistro.compareTo(mov2.FechaRegistro);
		        }
		        if (mov1.FolioComprobante.compareTo(mov2.FolioComprobante) != 0) {
		        	return mov1.FolioComprobante.compareTo(mov2.FolioComprobante);
		        }
		        else {
		        	return mov1.Nemotecnico.compareTo(mov2.Nemotecnico);
		        }
		     }
		});
		
		List<SalidaMovimientosTitulos> salidas = new ArrayList<SalidaMovimientosTitulos>();
		
		List<Long> portafolios = new ArrayList<>();
		for (Dato dato: entrada.p_Datos) {
			if (!portafolios.contains(dato.IdPortafolio))
				portafolios.add(dato.IdPortafolio);
		}
		params.clear();
		params.put("FechaDesde", entrada.p_FechaDesde);
		params.put("FechaHasta", entrada.p_FechaHasta);
		sql.batch(con, SQL_INSERT_PORTAFOLIOS_LEGACY, portObjs, Portafolio.class);
		List<SalidaMovimientosTitulos> movimientosLegacy = sql.query(con, SQL_SELECT_MOVIMIENTOS_LEGACY, params, SalidaMovimientosTitulos.class);
		
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		List<SalidaMovimientosTitulos> movimientosNominales = sql.query(con, SQL_SELECT_MOVIMIENTOS_NOMINALES, params, SalidaMovimientosTitulos.class);
		
		
		salidas.addAll(movimientosLegacy);		
		salidas.addAll(movimientosNominales);
		
		SimpleDateFormat formatoStringDate = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat formatoDateString = new SimpleDateFormat("dd-MM-yyyy");
		
		Collections.sort(salidaConsultarCustodiaMovimientos.P_OUT_TABLA, new Comparator<SalidaMovimiento>() {
			  public int compare(SalidaMovimiento o1, SalidaMovimiento o2) {
				  if (o1.FechaOperacion.compareTo(o2.FechaOperacion) != 0)
					  return o1.FechaOperacion.compareTo(o2.FechaOperacion);
				  return o1.FolioComprobante.compareTo(o2.FolioComprobante);
			  }
			});
		
		for(ConsultarCustodiaMovimientos.SalidaMovimiento mov : salidaConsultarCustodiaMovimientos.P_OUT_TABLA){
			if(!"DV".equals(mov.IdClasificacionTipoInstrumento)) {
				TipoOperacion tipoOperacion = (TipoOperacion) hTiposOperacion.get(mov.IdTipoOperacion);
				SalidaMovimientosTitulos salidaMovimientosTitulos = new SalidaMovimientosTitulos();
				salidaMovimientosTitulos.IdCliente = mov.IdCliente;
				salidaMovimientosTitulos.IdFolioCartera = mov.FolioCartera;
				salidaMovimientosTitulos.IdPortafolio = mov.IdPortafolio;
				salidaMovimientosTitulos.IdTipoComprobante = mov.IdTipoComprobante;
				salidaMovimientosTitulos.FolioComprobante = mov.FolioComprobante;
				salidaMovimientosTitulos.Accion = mov.Accion;
				salidaMovimientosTitulos.NombreTipoOperacion = tipoOperacion != null ? tipoOperacion.Nombre : mov.TipoOperacion;
				salidaMovimientosTitulos.IdTipoOperacion = mov.IdTipoOperacion;
				salidaMovimientosTitulos.FechaOperacion = formatoDateString.format(formatoStringDate.parse(Fecha.stringToSqlDate(mov.FechaOperacion)));
				salidaMovimientosTitulos.IdClasificacionTipoInstrumento = mov.IdClasificacionTipoInstrumento;
				salidaMovimientosTitulos.Nemotecnico = mov.Nemotecnico;
				salidaMovimientosTitulos.Emisor = mov.IdEmisor;
				salidaMovimientosTitulos.IdTipoReajuste = mov.IdTipoReajuste;
				salidaMovimientosTitulos.IdMonedaInstrumento = mov.IdMonedaInstr;
				salidaMovimientosTitulos.IdConcepto = mov.IdConcepto;
				salidaMovimientosTitulos.Cantidad = mov.Cantidad;
				salidaMovimientosTitulos.CantidadString = "RETIRO".equals(mov.Accion) ? "-" + mov.Cantidad.toPlainString() : mov.Cantidad.toPlainString();
				salidaMovimientosTitulos.PrecioMercado = mov.PrecioMercado;
				salidaMovimientosTitulos.TasaMercado = mov.TasaMercado;
				salidaMovimientosTitulos.PrecioCompra = mov.Precio;
				salidaMovimientosTitulos.TasaCompra = mov.Tasa;
				salidaMovimientosTitulos.MontoCompraMonedaInstrumento = mov.Monto;
				salidaMovimientosTitulos.MontoMercadoMonedaLocal = mov.MontoMercadoMonedaLocal;
				salidaMovimientosTitulos.MontoMercadoMonedaInstrumento = mov.MontoMercadoMonedaInstrumento;
				salidaMovimientosTitulos.MontoMercadoMonedaAdicional = mov.MontoMercadoMonedaAdicional;
				salidaMovimientosTitulos.IdEstado = mov.IdEstado;
				salidaMovimientosTitulos.TipoConcepto =mov.TipoConcepto;
				salidaMovimientosTitulos.IdTipoInstrumento = mov.IdTipoInstrumento;
				salidaMovimientosTitulos.NombreConcepto = mov.NombreConcepto != null ? mov.NombreConcepto : "";
				
				if (salidaMovimientosTitulos.IdTipoOperacion.equals("INGTIT")) {
				    salidaMovimientosTitulos.NombreTipoOperacion="I.T";
				} else if (salidaMovimientosTitulos.IdTipoOperacion.equals("RETTIT")) {
				    salidaMovimientosTitulos.NombreTipoOperacion="R.T";
				}
				if (salidaMovimientosTitulos.IdTipoOperacion.equals("APORFM") && salidaMovimientosTitulos.IdConcepto.equals("INTIFM")) {
				    salidaMovimientosTitulos.IdTipoOperacion="INTIFM";
				    salidaMovimientosTitulos.NombreTipoOperacion = "INGRESO TITULO";
				} else if (salidaMovimientosTitulos.IdTipoOperacion.equals("RESCFM")&& salidaMovimientosTitulos.IdConcepto.equals("RETIFM")) {
				    salidaMovimientosTitulos.IdTipoOperacion="RETIFM";
				    salidaMovimientosTitulos.NombreTipoOperacion = "RETIRO TITULO";
				}
				//Quita el id concepto incluidos en los nombres de conceptos
				if (salidaMovimientosTitulos.NombreConcepto.contains(salidaMovimientosTitulos.IdConcepto)) {
					salidaMovimientosTitulos.NombreConcepto = salidaMovimientosTitulos.NombreConcepto.replace(salidaMovimientosTitulos.IdConcepto, "").trim();
				}
							
				salidas.add(salidaMovimientosTitulos);
			}
		}
		
		List<HashMap<String,Object>> resultadoMovimientosTitulos = Utils.List2ListHashGenericObject(salidas);
		
		String parametroADL = "N";
		
		try {
			params.clear();
			params.put("IdEmpresa", entrada.p_IdEmpresa);
			parametroADL = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "NORMATIVA", "AgruparCartolaMovimientosADL");
		} catch (Exception e) {
		    parametroADL = "N";
		}

		if("S".equals(parametroADL)) {
			params.clear();
			params.put("FechaDesde", entrada.p_FechaDesde);
			params.put("FechaHasta", entrada.p_FechaHasta);
			List<MovimientoADL>  MovimientosADL = sql.query(con, SQL_SELECT_MOVIMIENTOS_ADL, params, MovimientoADL.class);
			
			Set<Long> idsADL = MovimientosADL.stream().map(m -> m.FolioComprobante).filter(Objects::nonNull).collect(Collectors.toSet());
			
			List<SalidaMovimientosTitulos> movimientosNormales = new ArrayList<>();
			List<SalidaMovimientosTitulos> movimientosParaAgrupar = new ArrayList<>();
			
			for (SalidaMovimientosTitulos m : salidas) {
			    if (m.FolioComprobante != null &&
			        idsADL.contains(m.FolioComprobante)) {
			    	movimientosParaAgrupar.add(m);
			    } else {
			    	movimientosNormales.add(m);
			    }
			}
			
			Map<String, List<SalidaMovimientosTitulos>> agrupados = movimientosParaAgrupar.stream().collect(Collectors.groupingBy(m -> m.Accion + "_" + m.IdMonedaInstrumento + m.IdCliente + m.IdPortafolio));
			
			
			List<SalidaMovimientosTitulos> agrupadosFinal = new ArrayList<>();
			String RescatesAgrupados = Diccionario.getMensaje(con, entrada.p_IdEmpresa, "RESCATES_ADL_AGRUPADOS", new HashMap<String, String>(0));
			String AportesAgrupados = Diccionario.getMensaje(con, entrada.p_IdEmpresa, "APORTES_ADL_AGRUPADOS", new HashMap<String, String>(0));

			for (Map.Entry<String, List<SalidaMovimientosTitulos>> entry : agrupados.entrySet()) {

			    List<SalidaMovimientosTitulos> grupo = entry.getValue();
			    SalidaMovimientosTitulos base = grupo.get(0);
			    SalidaMovimientosTitulos nuevo = new SalidaMovimientosTitulos();

			    nuevo.IdCliente = base.IdCliente;
			    nuevo.IdPortafolio = base.IdPortafolio;
			    nuevo.TipoConcepto = base.TipoConcepto;
			    nuevo.Accion = base.Accion;
			    nuevo.IdMonedaInstrumento = base.IdMonedaInstrumento;
			    nuevo.IdClasificacionTipoInstrumento = base.IdClasificacionTipoInstrumento;
			    nuevo.IdTipoInstrumento = base.IdTipoInstrumento;
			    nuevo.Emisor = base.Emisor;
			    nuevo.Nemotecnico = base.Nemotecnico;
			    nuevo.IdTipoOperacion = base.IdTipoOperacion;
			    nuevo.TasaMercado = base.TasaMercado;
			    nuevo.IdConcepto = base.IdConcepto;
			    nuevo.IdEstado = base.IdEstado;
			    if ("RESCATE FONDO MUTUO".equalsIgnoreCase(base.NombreTipoOperacion)) {
			        nuevo.NombreTipoOperacion = RescatesAgrupados;
			    }
			    else if ("APORTE FONDO MUTUO".equalsIgnoreCase(base.NombreTipoOperacion)) {
			        nuevo.NombreTipoOperacion = AportesAgrupados;
			    }
			    else {
			        nuevo.NombreTipoOperacion = base.NombreTipoOperacion;
			    }
			    nuevo.IdTipoComprobante = base.IdTipoComprobante;
			    nuevo.FolioComprobante = null;
			    nuevo.IdFolioCartera = null;
			    nuevo.FechaOperacion = "";
			    if ("RESCATE FONDO MUTUO".equalsIgnoreCase(base.NombreConcepto)) {
			        nuevo.NombreConcepto = RescatesAgrupados;
			    }
			    else if ("APORTE OVERNIGHT".equalsIgnoreCase(base.NombreConcepto)) {
			        nuevo.NombreConcepto = AportesAgrupados;
			    }
			    else {
			        nuevo.NombreConcepto = base.NombreConcepto;
			    }

			    BigDecimal totalCantidad = BigDecimal.ZERO;
			    BigDecimal totalMontoCompra = BigDecimal.ZERO;
			    BigDecimal totalMontoMercadoLocal = BigDecimal.ZERO;
			    BigDecimal totalMontoMercadoInstrumento = BigDecimal.ZERO;
			    BigDecimal totalMontoMercadoAdicional = BigDecimal.ZERO;

			    for (SalidaMovimientosTitulos m : grupo) {

			        BigDecimal cantidad = m.Cantidad == null ? BigDecimal.ZERO : m.Cantidad;
			        BigDecimal montoCompra = m.MontoCompraMonedaInstrumento == null ? BigDecimal.ZERO : m.MontoCompraMonedaInstrumento;
			        BigDecimal montoMercadoLocal = m.MontoMercadoMonedaLocal == null ? BigDecimal.ZERO : m.MontoMercadoMonedaLocal;
			        BigDecimal montoMercadoInstrumento = m.MontoMercadoMonedaInstrumento == null ? BigDecimal.ZERO : m.MontoMercadoMonedaInstrumento;
			        BigDecimal montoMercadoAdicional = m.MontoMercadoMonedaAdicional == null ? BigDecimal.ZERO : m.MontoMercadoMonedaAdicional;

			        totalCantidad = totalCantidad.add(cantidad);
			        totalMontoCompra = totalMontoCompra.add(montoCompra);
			        totalMontoMercadoLocal = totalMontoMercadoLocal.add(montoMercadoLocal);
			        totalMontoMercadoInstrumento = totalMontoMercadoInstrumento.add(montoMercadoInstrumento);
			        totalMontoMercadoAdicional = totalMontoMercadoAdicional.add(montoMercadoAdicional);
			    }

			    nuevo.Cantidad = totalCantidad;
			    nuevo.CantidadString = totalCantidad.toPlainString();
			    nuevo.MontoCompraMonedaInstrumento = totalMontoCompra;
			    nuevo.MontoMercadoMonedaLocal = totalMontoMercadoLocal;
			    nuevo.MontoMercadoMonedaInstrumento = totalMontoMercadoInstrumento;
			    nuevo.MontoMercadoMonedaAdicional = totalMontoMercadoAdicional;

			    if (totalCantidad.compareTo(BigDecimal.ZERO) != 0) {
			        nuevo.PrecioCompra = totalMontoCompra.divide(totalCantidad, 8, RoundingMode.HALF_UP);
			        nuevo.PrecioMercado = totalMontoMercadoInstrumento.divide(totalCantidad, 8, RoundingMode.HALF_UP);
			    }

			    agrupadosFinal.add(nuevo);
			}
			List<SalidaMovimientosTitulos> salidasFinal = new ArrayList<>();

			salidasFinal.addAll(movimientosNormales);
			salidasFinal.addAll(agrupadosFinal);


			List<HashMap<String,Object>> resultadoFinal = Utils.List2ListHashGenericObject(salidasFinal);
			out.P_OUT_SALIDA.put("movimientosCaja", resultadoFinal);
		}else {
			out.P_OUT_SALIDA.put("movimientosCaja", resultadoMovimientosTitulos);
		}
		
		SQL.exec(con, SQL_DELETE_PORTAFOLIOS_LEGACY.replace(":IdTrabajo", idTrabajo+""));
		logger.info("FIN::MovimientosTitulos");
		return out;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("master02","connections.properties")) {
		EntradaCartola Objin = new EntradaCartola();
		Objin.p_IdEmpresa = 1L;
		Objin.p_IdModulo = "NOR";
		Objin.p_IdUsuario = 97L;
		Objin.p_FechaDesde = Fecha.toDate("2026-03-01");
		Objin.p_FechaHasta = Fecha.toDate("2026-03-31");
		Dato dato = new Dato();
		
		dato.IdCliente = 2536L;
		dato.IdPortafolio = 7001L; 
		
		Objin.p_Datos.add(dato);
		
		MovimientosTitulos  demo= new MovimientosTitulos();				 
		try {
			SalidaCartola ObjOut=demo.procesar(con, Objin);
			con.commit();
			System.out.println(ObjOut.toString());
		}
		catch(Exception er) {
			er.printStackTrace();	
			con.rollback();
		}
			con.rollback(); 			
    	}
		System.out.println("== END ==");
	}
	
}
