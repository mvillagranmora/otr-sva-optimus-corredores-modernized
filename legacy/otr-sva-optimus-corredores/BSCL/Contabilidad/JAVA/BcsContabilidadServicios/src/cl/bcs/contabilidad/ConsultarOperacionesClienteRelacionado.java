package cl.bcs.contabilidad;

import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.contabilidad.ConsultarCondicionesConceptosNormativos.ConfiguracionesConceptosNormativos_IN;
import cl.bcs.contabilidad.ConsultarCondicionesConceptosNormativos.ConfiguracionesConceptosNormativos_OUT;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;

public class ConsultarOperacionesClienteRelacionado implements JavaCall {
	private final String SQL_SELECT_COMPROBANTES = loadResourceToString("cl/bcs/contabilidad/sql/ConsultarOperacionesClienteRelacionado_SelectComprobantes.sql");
	private final String SQL_SELECT_SPOT = loadResourceToString("cl/bcs/contabilidad/sql/ConsultarOperacionesClienteRelacionado_SelectOperacionesSpot.sql");
	private final String SQL_SELECT_FORWARD = loadResourceToString("cl/bcs/contabilidad/sql/ConsultarOperacionesClienteRelacionado_SelectOperacionesForward.sql");
	private final String SQL_SELECT_PACTOS = loadResourceToString("cl/bcs/contabilidad/sql/ConsultarOperacionesClienteRelacionado_SelectOperacionesPacto.sql");
	private final String SQL_SELECT_TERMINO = loadResourceToString("cl/bcs/contabilidad/sql/ConsultarOperacionesClienteRelacionado_SelectOperacionesTermino.sql");
	private final String SQL_SELECT_TERMINO_CARTERA = loadResourceToString("cl/bcs/contabilidad/sql/ConsultarOperacionesClienteRelacionado_SelectOperacionesTerminoCartera.sql");
	private final String SQL_SELECT_CARTERAS_PRESTAMOS_EMPRESTITOS = loadResourceToString("cl/bcs/contabilidad/sql/ConsultarOperacionesClienteRelacionado_SelectPrestamos.sql");

	private static final Set<String> EXCLUIR_OPERACIONES = new HashSet<>(Arrays.asList("VENTER", "COMTER", "COMPAC", "VENPAC"));

	private static class ReglaConcepto {
		long id;
		Map<String, Map<String, String>> valoresPorDato = new HashMap<>();
		Set<String> grupos = new HashSet<>();
		ConsultarOperacionesClienteRelacionado.ConceptoNormativo acumulado;
		Set<Long> foliosProcesados = new HashSet<>();
	}

	public static abstract class OperacionesClienteRelacionadoBase {
		public Long FolioOperacion;
		public String FechaOperacion;
		public String Nombre;
		public String Portafolio;
		public String DocumentoIdentidad;
		public String IdTipoInstrumento;
		public String TipoDocumento;
		public String CompraVenta;
		public String IdTipoOperacion;
		public String IdClasificacionTipoInstrumento;
	}

	public static class TotalTransaccion {
		public BigDecimal Cantidad;
		public BigDecimal Monto;
		public BigDecimal Resultado;
	}

	public static class Saldo {
		public BigDecimal Activos;
		public BigDecimal Pasivos;
	}

	public static class ConceptoNormativo {
		public String Concepto;
		public TotalTransaccion totalTransaccion;
		public Saldo saldo;
	}

	public static class OperacionDolar extends OperacionesClienteRelacionadoBase {
		public BigDecimal MontoCompra;
		public BigDecimal MontoVenta;
	}

	public static class Comprobante extends OperacionesClienteRelacionadoBase {
		public String     IdMoneda;
		public BigDecimal MontoOperado;
		public BigDecimal MontoTotal;
		public BigDecimal MontoCobro;
		public BigDecimal TotalComision;
		public BigDecimal TotalGasto;
		public BigDecimal TotalImpto;
		public BigDecimal TotalRetencion;
		public BigDecimal TotalDerecho;
	}

	public static class PactoTermino extends OperacionesClienteRelacionadoBase {
		public String FechaInicio;
		public String FechaTermino;
		public BigDecimal MontoFinal;
		public BigDecimal MontoInicial;
		public BigDecimal Perdida;
		public BigDecimal Ganancia;
	}

	public static class ConsultaOperacionesClientesRelacionados_OUT {
		public String P_OUT_MENSAJE;
		public Long P_OUT_ERRORESCONTADOR;
		List<ConceptoNormativo> conceptos;
		ConceptoNormativo totalizado;
		List<OperacionesClienteRelacionadoBase> operaciones;
	}

	public static class ConsultaOperacionesClientesRelacionados_IN {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_FechaInicio;
		public String p_FechaFin;
		public String p_IdModulo;
	}

	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();
		ConsultaOperacionesClientesRelacionados_IN in = gson.fromJson(jin.toString(), ConsultaOperacionesClientesRelacionados_IN.class);
		ConsultaOperacionesClientesRelacionados_OUT out = logica(con, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public ConsultaOperacionesClientesRelacionados_OUT logica(Connection conn, ConsultaOperacionesClientesRelacionados_IN in) {
	    ConsultaOperacionesClientesRelacionados_OUT out = new ConsultaOperacionesClientesRelacionados_OUT();
	    out.P_OUT_ERRORESCONTADOR = 0L;
	    out.conceptos = new ArrayList<>();
	    out.operaciones = new ArrayList<>();
	    SQL sql = new SQL();
	    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	    try {
			String MonedaSistema =  ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");

	        LocalDate fechaInicio = LocalDate.parse(in.p_FechaInicio, formatter);
	        LocalDate fechaFin = LocalDate.parse(in.p_FechaFin, formatter);
	        Period periodo = Period.between(fechaInicio, fechaFin);
	        int totalMeses = periodo.getYears() * 12 + periodo.getMonths();

	        if (totalMeses > 3 || (totalMeses == 3 && fechaFin.getDayOfMonth() > fechaInicio.getDayOfMonth())) {
	            out.P_OUT_ERRORESCONTADOR = 1L;
	            out.P_OUT_MENSAJE = "FECHA_FUERA_RANGO";
	            return out;
	        }
	        
		    HashMap<String, Object> params = new HashMap<>(2);
		    params.put("FechaInicio", in.p_FechaInicio);
		    params.put("FechaFin", in.p_FechaFin);
	        ConfiguracionesConceptosNormativos_IN condicionesIn = new ConfiguracionesConceptosNormativos_IN();
	        ConsultarCondicionesConceptosNormativos consultaCondicionesConceptos = new ConsultarCondicionesConceptosNormativos();
	        ConfiguracionesConceptosNormativos_OUT conceptosCondicionesOut = consultaCondicionesConceptos.logica(conn, condicionesIn);
	        if (conceptosCondicionesOut.P_OUT_ERRORESCONTADOR > 0L) {
	            out.P_OUT_ERRORESCONTADOR = conceptosCondicionesOut.P_OUT_ERRORESCONTADOR;
	            out.P_OUT_MENSAJE = conceptosCondicionesOut.P_OUT_MENSAJE;
	            return out;
	        }
	        
	        fechaInicio = LocalDate.parse(in.p_FechaInicio, formatter);
	        fechaFin = LocalDate.parse(in.p_FechaFin, formatter);

	        if (fechaInicio.isAfter(fechaFin)) {
	            out.P_OUT_ERRORESCONTADOR = 1L;
	            out.P_OUT_MENSAJE = "La fecha de inicio no puede ser posterior a la fecha de fin.";
	            return out;
	        }


	        List<ConsultarCondicionesConceptosNormativos.ConceptoNormativo> conceptosNormativos = conceptosCondicionesOut.conceptosNormativos;

	        List<Comprobante> comprobantes = sql.query(conn, SQL_SELECT_COMPROBANTES, params, Comprobante.class);
	        List<Comprobante> prestamosEmprestitos = sql.query(conn, SQL_SELECT_CARTERAS_PRESTAMOS_EMPRESTITOS, params, Comprobante.class);
	        List<PactoTermino> pactos = sql.query(conn, SQL_SELECT_PACTOS, params, PactoTermino.class);
	        List<PactoTermino> terminos = sql.query(conn, SQL_SELECT_TERMINO, params, PactoTermino.class);
	        List<OperacionDolar> spots = sql.query(conn, SQL_SELECT_SPOT, params, OperacionDolar.class);
	        List<PactoTermino> terminosCartera = sql.query(conn, SQL_SELECT_TERMINO_CARTERA, params, PactoTermino.class);
	        List<OperacionDolar> forwards = sql.query(conn, SQL_SELECT_FORWARD, params, OperacionDolar.class);

	        List<OperacionesClienteRelacionadoBase> operaciones = new ArrayList<>();
	        Arrays.asList(comprobantes, prestamosEmprestitos, pactos, spots, forwards, terminosCartera).forEach(operaciones::addAll);

	        Map<Long, ConsultarOperacionesClienteRelacionado.ConceptoNormativo> agrupados = new HashMap<>();
	        List<ReglaConcepto> reglas = new ArrayList<>();

	        for (ConsultarCondicionesConceptosNormativos.ConceptoNormativo conceptoNorm : conceptosNormativos) {
	            ConsultarOperacionesClienteRelacionado.ConceptoNormativo acumulado = new ConsultarOperacionesClienteRelacionado.ConceptoNormativo();
	            acumulado.Concepto = conceptoNorm.ConceptoNormativo;
	            acumulado.totalTransaccion = new ConsultarOperacionesClienteRelacionado.TotalTransaccion();
	            acumulado.totalTransaccion.Cantidad = BigDecimal.ZERO;
	            acumulado.totalTransaccion.Monto = BigDecimal.ZERO;
	            acumulado.totalTransaccion.Resultado = BigDecimal.ZERO;
	            acumulado.saldo = new ConsultarOperacionesClienteRelacionado.Saldo();
	            acumulado.saldo.Activos = BigDecimal.ZERO;
	            acumulado.saldo.Pasivos = BigDecimal.ZERO;

	            if (conceptoNorm.condiciones == null || conceptoNorm.condiciones.isEmpty()) {
	                agrupados.put(conceptoNorm.IdConceptoNormativo, acumulado);
	                continue;
	            }

	            ReglaConcepto rc = new ReglaConcepto();
	            rc.id = conceptoNorm.IdConceptoNormativo;
	            rc.acumulado = acumulado;

	            for (ConsultarCondicionesConceptosNormativos.CondicionesConceptoNormativo grupo : conceptoNorm.condiciones) {
	                rc.grupos.add(grupo.IdTipoDatoEvento);
	                Map<String, String> mapa = rc.valoresPorDato.get(grupo.IdTipoDatoEvento);
	                if (mapa == null) {
	                    mapa = new HashMap<>();
	                    rc.valoresPorDato.put(grupo.IdTipoDatoEvento, mapa);
	                }
	                for (ConsultarCondicionesConceptosNormativos.Valores v : grupo.valores) {
	                    mapa.put(v.ValorVarchar, v.OperadorFuncion);
	                }
	            }
	            reglas.add(rc);
	        }

	        for (ReglaConcepto rc : reglas) {
	            for (OperacionesClienteRelacionadoBase op : operaciones) {
	                if (op.FolioOperacion != null && rc.foliosProcesados.contains(op.FolioOperacion)) continue;

	                boolean cumpleTodos = true;
	                boolean restar = false;

	                for (String grupo : rc.grupos) {
	                    if ("TIPODATO".equalsIgnoreCase(grupo)) continue;

	                    if ("TIPODOCUMENTO".equalsIgnoreCase(grupo) && EXCLUIR_OPERACIONES.contains(op.IdTipoOperacion)) continue;

	                    String valorOperacion = null;
	                    if ("TIPOINSTRUMENTO".equalsIgnoreCase(grupo)) valorOperacion = op.IdTipoInstrumento;
	                    else if ("TIPODOCUMENTO".equalsIgnoreCase(grupo)) valorOperacion = op.TipoDocumento;
	                    else if ("COMPRAVENTA".equalsIgnoreCase(grupo)) valorOperacion = op.CompraVenta;

	                    Map<String, String> mapa = rc.valoresPorDato.get(grupo);
	                    boolean cumpleGrupo = false;

	                    if (valorOperacion != null && mapa != null) {
	                        if (mapa.containsKey(valorOperacion)) {
	                            cumpleGrupo = true;
	                            String opFun = mapa.get(valorOperacion);
	                            if ("-".equalsIgnoreCase(opFun)) restar = true;
	                        }
	                    }

	                    if (!cumpleGrupo) {
	                        cumpleTodos = false;
	                        break;
	                    }
	                }

	                if (cumpleTodos) {
	                    BigDecimal monto = BigDecimal.ZERO;

	                    if (op instanceof Comprobante) {
	                    	List<String> tipoMontos = new ArrayList<>(rc.valoresPorDato.getOrDefault("TipoDato", Collections.emptyMap()).keySet());
	                    	monto = obtenerMontoDesdeComprobante((Comprobante) op, tipoMontos, MonedaSistema, ((Comprobante)op).IdMoneda);
	                    } else if (op instanceof ConsultarOperacionesClienteRelacionado.PactoTermino) {
	                        BigDecimal m = ((ConsultarOperacionesClienteRelacionado.PactoTermino) op).MontoInicial;
	                        monto = m == null ? BigDecimal.ZERO : m;
	                    } else if (op instanceof OperacionDolar) {
	                        OperacionDolar d = (OperacionDolar) op;
	                        BigDecimal mc = d.MontoCompra == null ? BigDecimal.ZERO : d.MontoCompra;
	                        BigDecimal mv = d.MontoVenta == null ? BigDecimal.ZERO : d.MontoVenta;
	                        Set<String> tiposOperaciones = new HashSet<>(Arrays.asList("ANVSPT", "ANCSPT"));
	                        if (tiposOperaciones.contains(op.IdTipoOperacion)) {
	                        	mc = tiposOperaciones.contains(op.IdTipoOperacion) && "C".equalsIgnoreCase(op.CompraVenta) ? d.MontoCompra : BigDecimal.ZERO;
	                        	mv = tiposOperaciones.contains(op.IdTipoOperacion) && "V".equalsIgnoreCase(op.CompraVenta) ? d.MontoVenta : BigDecimal.ZERO;
	                        }

	                        monto = "C".equalsIgnoreCase(op.CompraVenta) ? mc : mv;
	                    }

	                    if (restar) monto = monto.negate();

	                    rc.acumulado.totalTransaccion.Cantidad =
	                        rc.acumulado.totalTransaccion.Cantidad.add(BigDecimal.ONE);
	                    rc.acumulado.totalTransaccion.Monto =
	                        rc.acumulado.totalTransaccion.Monto.add(monto);

	                    if (op.FolioOperacion != null) rc.foliosProcesados.add(op.FolioOperacion);
	                }
	            }
	            agrupados.put(rc.id, rc.acumulado);
	        }
	        
	        out.totalizado = new ConceptoNormativo();
	        out.totalizado.Concepto = "Total";
	        out.totalizado.totalTransaccion = new ConsultarOperacionesClienteRelacionado.TotalTransaccion();
	        out.totalizado.totalTransaccion.Cantidad = BigDecimal.ZERO;
	        out.totalizado.totalTransaccion.Monto = BigDecimal.ZERO;
	        out.totalizado.totalTransaccion.Resultado = BigDecimal.ZERO;
	        out.totalizado.saldo = new ConsultarOperacionesClienteRelacionado.Saldo();
	        out.totalizado.saldo.Activos = BigDecimal.ZERO;
	        out.totalizado.saldo.Pasivos = BigDecimal.ZERO;

	        for (ConsultarOperacionesClienteRelacionado.ConceptoNormativo c : agrupados.values()) {
	            if (c.totalTransaccion != null) {
	                if (c.totalTransaccion.Cantidad != null) {
	                	out.totalizado.totalTransaccion.Cantidad = out.totalizado.totalTransaccion.Cantidad.add(c.totalTransaccion.Cantidad);
	                }
	                if (c.totalTransaccion.Monto != null) {
	                	out.totalizado.totalTransaccion.Monto = out.totalizado.totalTransaccion.Monto.add(c.totalTransaccion.Monto);
	                }
	                if (c.totalTransaccion.Resultado != null) {
	                	out.totalizado.totalTransaccion.Resultado = out.totalizado.totalTransaccion.Resultado.add(c.totalTransaccion.Resultado);
	                }
	            }
	            if (c.saldo != null) {
	                if (c.saldo.Activos != null) {
	                	out.totalizado.saldo.Activos = out.totalizado.saldo.Activos.add(c.saldo.Activos);
	                }
	                if (c.saldo.Pasivos != null) {
	                	out.totalizado.saldo.Pasivos = out.totalizado.saldo.Pasivos.add(c.saldo.Pasivos);
	                }
	            }
	        }	        
	        
	        out.conceptos = new ArrayList<>(agrupados.values());
	        
	        Set<String> documentosNegativos = new HashSet<>(Arrays.asList("NCE", "NCR", "NDR", "NDE", "NEE", "NEO"));
	        
	        Arrays.asList(comprobantes, spots, forwards, pactos, terminos).forEach(out.operaciones::addAll);
	        for (OperacionesClienteRelacionadoBase op : out.operaciones) {
	            if (op.TipoDocumento != null && documentosNegativos.contains(op.TipoDocumento)) {
	                if (op instanceof Comprobante) {
	                    Comprobante c = (Comprobante) op;

	                    c.MontoOperado = negate(c.MontoOperado);
	                    c.MontoTotal = negate(c.MontoTotal);
	                    c.MontoCobro = negate(c.MontoCobro);
	                    c.TotalComision = negate(c.TotalComision);
	                    c.TotalGasto = negate(c.TotalGasto);
	                    c.TotalImpto = negate(c.TotalImpto);
	                    c.TotalRetencion = negate(c.TotalRetencion);
	                    c.TotalDerecho = negate(c.TotalDerecho);


	                } else if (op instanceof OperacionDolar) {
	                    OperacionDolar d = (OperacionDolar) op;
	                    d.MontoCompra = negate(d.MontoCompra);
	                    d.MontoVenta = negate(d.MontoVenta);
	                }
	            }
	        }   
	    } catch (Exception ex) {
	        out.P_OUT_ERRORESCONTADOR = out.P_OUT_ERRORESCONTADOR + 1;
	        out.P_OUT_MENSAJE = ex.getMessage();
	    }
	    return out;
	}
	
	private BigDecimal negate(BigDecimal val) {
	    return val == null ? null : val.negate();
	}

	private BigDecimal obtenerMontoDesdeComprobante(Comprobante c, List<String> tipoMontos, String monedaSistema, String monedaComprobante) {
	    BigDecimal total = BigDecimal.ZERO;
	    boolean mismaMoneda = monedaSistema != null && monedaSistema.equalsIgnoreCase(monedaComprobante);

	    for (String tipo : tipoMontos) {
	        if (tipo == null) continue;
	        switch (tipo.toUpperCase()) {
	            case "MONTO_COBRO":
	                total = total.add(mismaMoneda && c.MontoCobro != null ? c.MontoCobro : BigDecimal.ZERO);
	                break;
	            case "MONTO_COMISION":
	                total = total.add(mismaMoneda && c.TotalComision != null ? c.TotalComision : BigDecimal.ZERO);
	                break;
	            case "MONTO_DERECHO":
	                total = total.add(mismaMoneda && c.TotalDerecho != null ? c.TotalDerecho : BigDecimal.ZERO);
	                break;
	            case "MONTO_GASTOS":
	                total = total.add(mismaMoneda && c.TotalGasto != null ? c.TotalGasto : BigDecimal.ZERO);
	                break;
	            case "MONTO_IVA":
	                total = total.add(mismaMoneda && c.TotalImpto != null ? c.TotalImpto : BigDecimal.ZERO);
	                break;
	            case "MONTO_OPERADO":
	                total = total.add(mismaMoneda && c.MontoOperado != null ? c.MontoOperado : BigDecimal.ZERO);
	                break;
	            case "MONTO_TOTAL":
	                total = total.add(mismaMoneda && c.MontoTotal != null ? c.MontoTotal : BigDecimal.ZERO);
	                break;
	            case "VALOR_COBRO_MX":
	                total = total.add(!mismaMoneda && c.MontoCobro != null ? c.MontoCobro : BigDecimal.ZERO);
	                break;
	            case "MONTO_COMISION_MX":
	                total = total.add(!mismaMoneda && c.TotalComision != null ? c.TotalComision : BigDecimal.ZERO);
	                break;
	            case "VALOR_DERECHO_MX":
	                total = total.add(!mismaMoneda && c.TotalDerecho != null ? c.TotalDerecho : BigDecimal.ZERO);
	                break;
	            case "VALOR_GASTOS_MX":
	                total = total.add(!mismaMoneda && c.TotalGasto != null ? c.TotalGasto : BigDecimal.ZERO);
	                break;
	            case "VALOR_IVA_MX":
	                total = total.add(!mismaMoneda && c.TotalImpto != null ? c.TotalImpto : BigDecimal.ZERO);
	                break;
	            case "VALOR_OPERADO_MX":
	                total = total.add(!mismaMoneda && c.MontoOperado != null ? c.MontoOperado : BigDecimal.ZERO);
	                break;
	            case "VALOR_TOTAL_MX":
	                total = total.add(!mismaMoneda && c.MontoTotal != null ? c.MontoTotal : BigDecimal.ZERO);
	                break;
	        }
	    }

	    if (total.compareTo(BigDecimal.ZERO) == 0) {
	        return c.MontoOperado == null ? BigDecimal.ZERO : c.MontoOperado;
	    }
	    return total;
	}


	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa2", "C:\\Users\\lportales_ext\\OPTIMUS\\workspace\\optimus-corredores-dev-workspace\\OptimusDevJava\\application.properties")) {
			ConsultaOperacionesClientesRelacionados_IN entrada = new ConsultaOperacionesClientesRelacionados_IN();
			entrada.p_IdEmpresa = 1L;
			entrada.p_IdUsuario = 21L;
			entrada.p_FechaInicio = "2025-01-17";
			entrada.p_FechaFin = "2025-07-17";
			entrada.p_IdModulo = "CON";
			JsonParser parser = new JsonParser();
			JsonObject dataJsonIn = parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
			try {
				ConsultarOperacionesClienteRelacionado demo = new ConsultarOperacionesClienteRelacionado();
				System.out.println(demo.call(con, dataJsonIn).toString());
				con.commit();
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
		}
	}
}
