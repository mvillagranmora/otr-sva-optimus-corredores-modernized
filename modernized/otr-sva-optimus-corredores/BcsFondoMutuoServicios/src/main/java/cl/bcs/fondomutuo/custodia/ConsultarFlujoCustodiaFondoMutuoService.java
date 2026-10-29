package cl.bcs.fondomutuo.custodia;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;

import cl.bcs.fondomutuo.custodia.FlujoCustodiaFmDatos.RescateTotal;
import cl.bcs.fondomutuo.custodia.FlujoCustodiaFmDatos.SerieFondo;
import cl.bcs.fondomutuo.kit.Compuertas;
import cl.bcs.fondomutuo.kit.JsonLegacy;
import cl.bcs.fondomutuo.port.out.http.CustodiaPort;
import cl.bcs.fondomutuo.port.out.kit.CalendarioPort;
import cl.bcs.fondomutuo.requisitos.Constantes3B;
import cl.bcs.fondomutuo.requisitos.contrato.CuotaContrato;

/**
 * Legacy {@code cl.bcs.fondomutuo.ConsultarFlujoCustodiaFondoMutuo} (428 LOC), rules FM-REQ-24 / FM-REQ-25. Custody is NOT migrated: the flow of the
 * portfolios comes from {@code cl.bcs.custodia.ConsultarFlujoCustodia} over HTTP behind {@link CustodiaPort}, and the call is behind gate G-CUS
 * ({@link Compuertas#gCustodia()}, closed by default, evaluated right before the call so a closed gate has had no effect). The valuation is the
 * collaborator {@code CalcularMontoNumeroCuota} of step 3A behind {@link CuotaContrato.Puerto}.
 * Kept as legacy: a repeated key in the series read ({@code Collectors.toMap}) raises; the flow matches the FIRST total of the same client, portfolio and
 * instrument whose regime equals the requested one or when the requested regime is empty (D-REQ-9); only a TOTAL rescue in transit ({@code ING},
 * {@code T}) zeroes the available quantity (D-REQ-10); a null instrument raises {@link NullPointerException}.
 */
public class ConsultarFlujoCustodiaFondoMutuoService {

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public Long p_IdPais;
        public List<InInstrumento> p_Instrumentos = new ArrayList<>();
        public List<InCarteras> p_Carteras = new ArrayList<>();
        public String p_FechaSaldo;
        public Integer p_DiasProyeccion;
    }

    public static class InInstrumento {
        public Long IdInstrumento;
        public String IdRegimenFondoMutuo;
    }

    public static class InCarteras {
        public Long IdCliente;
        public Long IdPortafolio;
    }

    public static class Salida {
        public List<FlujoFondoMutuoTotalizado> P_OUT_FLUJOFONDOMUTUOTOTALIZADO = new ArrayList<>();
    }

    public static class FlujoFondoMutuoTotalizado {
        public Long IdInterno;
        public Long IdInstrumento;
        public Long IdCliente;
        public Long IdPortafolio;
        public BigDecimal CantidadDisponible = BigDecimal.ZERO;
        public BigDecimal MontoDisponible = BigDecimal.ZERO;
        public BigDecimal MontoDisponibleCustodia = BigDecimal.ZERO;
        public String RescateTotal;
        public String Nemotecnico = "";
        public String IdRegimenTributario;
        public String IdMoneda = "";
    }

    /** JSON contract of the collaborator {@code ConsultarFlujoCustodia} (legacy names). */
    public static class FlujoCustodiaEntrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_FechaSaldo;
        public Long p_IdPais;
        public Integer p_DiasProyeccion;
        public String p_DiaHabil = "S";
        public String p_ValorizarFlujo;
        public Object p_BarraProgreso;
        public List<FlujoUso> p_Uso = new ArrayList<>();
        public List<FlujoFiltrosBusqueda> p_FiltrosBusqueda = new ArrayList<>();
        public List<FlujoEsAgrupadoPor> p_EsAgrupadoPor = new ArrayList<>();
    }

    public static class FlujoUso {
        public String UsoTipoOperacion;
        public Long IdCliente;
        public Long IdPortafolio;
        public String BusquedaEspecifica;
        public String IdTipoInstrumentoEspecifico;
    }

    public static class FlujoFiltrosBusqueda {
        public String IdClasificacionTipoInstrumento;
        public String IdTipoInstrumento;
        public Long IdInstrumento;
        public String IdEmisor;
        public Date FechaVencimiento;
        public String IdTipoReajuste;
        public String EsFungible;
        public String IdBoveda;
        public String IdRegimenFondoMutuo;
    }

    public static class FlujoEsAgrupadoPor {
        public String ClasificacionTipoInstrumento;
        public String TipoInstrumento;
        public String Instrumento;
        public String Emisor;
        public String Nemotecnico;
        public String NemoSVS;
        public String FechaEmisionInstrumento;
        public String FechaVencimientoInstrumento;
        public String FechaLiquidacionInstrumento;
        public String TipoReajuste;
        public String EsFungible;
        public String CodigoIFRS;
        public String Boveda;
        public String Bolsa;
        public String TipoOperacion;
        public String Mesa;
        public String FolioRegistro;
        public String ItemOrigen;
        public String IdRegimenFondoMutuo;
        public String GrupoRegimen;
        public String MonedaInstrumento;
    }

    /** Only what the answer is read for. */
    public static class FlujoCustodiaSalida {
        public List<FlujoCustodia> p_out_FlujoTotalizado = new ArrayList<>();
    }

    public static class FlujoCustodia {
        public Long IdCliente;
        public Long IdPortafolio;
        public Long IdInstrumento;
        public String IdRegimenFondoMutuo;
        public BigDecimal Cantidad;
        public BigDecimal MontoAMercadoMonedaInstrumento;
    }

    private final FlujoCustodiaFmDatos datos;
    private final CustodiaPort custodia;
    private final CuotaContrato.Puerto valorizacion;
    private final CalendarioPort calendario;
    private final Compuertas compuertas;

    public ConsultarFlujoCustodiaFondoMutuoService(FlujoCustodiaFmDatos datos, CustodiaPort custodia, CuotaContrato.Puerto valorizacion,
            CalendarioPort calendario, Compuertas compuertas) {
        this.datos = datos;
        this.custodia = custodia;
        this.valorizacion = valorizacion;
        this.calendario = calendario;
        this.compuertas = compuertas;
    }

    public Salida logica(Entrada entrada) throws Exception {
        Salida out = new Salida();
        FlujoCustodiaSalida flujo = obtenerFlujoCustodia(entrada);
        out.P_OUT_FLUJOFONDOMUTUOTOTALIZADO = sinValorizar(entrada, flujo);
        out.P_OUT_FLUJOFONDOMUTUOTOTALIZADO = valorizada(entrada, out.P_OUT_FLUJOFONDOMUTUOTOTALIZADO);
        return out;
    }

    private FlujoCustodiaSalida obtenerFlujoCustodia(Entrada entrada) {
        FlujoCustodiaEntrada e = new FlujoCustodiaEntrada();
        e.p_IdEmpresa = entrada.p_IdEmpresa;
        e.p_IdUsuario = entrada.p_IdUsuario;
        e.p_IdModulo = entrada.p_IdModulo;
        e.p_FechaSaldo = entrada.p_FechaSaldo;
        e.p_IdPais = entrada.p_IdPais;
        e.p_DiasProyeccion = entrada.p_DiasProyeccion;
        e.p_DiaHabil = "S";
        e.p_ValorizarFlujo = "N";

        for (InCarteras cartera : entrada.p_Carteras) {
            FlujoUso uso = new FlujoUso();
            uso.UsoTipoOperacion = "RESCFM";
            uso.IdCliente = cartera.IdCliente;
            uso.IdPortafolio = cartera.IdPortafolio;
            uso.BusquedaEspecifica = "";
            uso.IdTipoInstrumentoEspecifico = "";
            e.p_Uso.add(uso);
        }
        HashMap<String, String> enviados = new HashMap<>();
        for (InInstrumento instrumento : entrada.p_Instrumentos) {
            String key = instrumento.IdInstrumento + "_" + instrumento.IdRegimenFondoMutuo;
            if (enviados.get(key) == null) {
                FlujoFiltrosBusqueda busqueda = new FlujoFiltrosBusqueda();
                busqueda.IdClasificacionTipoInstrumento = "FM";
                busqueda.IdInstrumento = instrumento.IdInstrumento.intValue() == 0 ? null : instrumento.IdInstrumento;
                busqueda.IdRegimenFondoMutuo = instrumento.IdRegimenFondoMutuo;
                e.p_FiltrosBusqueda.add(busqueda);
                enviados.put(key, key);
            }
        }
        FlujoEsAgrupadoPor agrupador = new FlujoEsAgrupadoPor();
        agrupador.ClasificacionTipoInstrumento = "S";
        agrupador.TipoInstrumento = "S";
        agrupador.Instrumento = "S";
        agrupador.Emisor = "N";
        agrupador.Nemotecnico = "N";
        agrupador.NemoSVS = "N";
        agrupador.FechaEmisionInstrumento = "N";
        agrupador.FechaVencimientoInstrumento = "N";
        agrupador.FechaLiquidacionInstrumento = "N";
        agrupador.TipoReajuste = "N";
        agrupador.EsFungible = "N";
        agrupador.CodigoIFRS = "N";
        agrupador.Boveda = "N";
        agrupador.Bolsa = "N";
        agrupador.TipoOperacion = "N";
        agrupador.Mesa = "N";
        agrupador.FolioRegistro = "N";
        agrupador.ItemOrigen = "N";
        agrupador.IdRegimenFondoMutuo = "S";
        e.p_EsAgrupadoPor.add(agrupador);

        compuertas.gCustodia();   // G-CUS: shut by default -> ServicioNoServidoException before any effect; open only with the in-process custody (same transaction)
        JsonObject respuesta = custodia.consultarFlujo(JsonLegacy.conNulos().toJsonTree(e).getAsJsonObject());
        return JsonLegacy.plano().fromJson(respuesta, FlujoCustodiaSalida.class);
    }

    private List<FlujoFondoMutuoTotalizado> sinValorizar(Entrada entrada, FlujoCustodiaSalida flujoCustodia) throws Exception {
        Long idInterno = 1L;
        Map<Long, SerieFondo> seriePorInstrumento = series(entrada.p_Instrumentos);
        List<RescateTotal> rescates = rescatesTotalesEnTransito(entrada.p_Instrumentos, entrada.p_Carteras);
        List<Map<String, Object>> formatos = null;
        List<FlujoFondoMutuoTotalizado> salida = new ArrayList<>();
        for (InCarteras cartera : entrada.p_Carteras) {
            for (InInstrumento instrumento : entrada.p_Instrumentos) {
                FlujoFondoMutuoTotalizado flujo = new FlujoFondoMutuoTotalizado();
                flujo.IdInterno = idInterno;
                flujo.IdCliente = cartera.IdCliente;
                flujo.IdPortafolio = cartera.IdPortafolio;
                flujo.IdInstrumento = instrumento.IdInstrumento;
                flujo.IdRegimenTributario = instrumento.IdRegimenFondoMutuo;
                SerieFondo serieFondo = seriePorInstrumento.get(flujo.IdInstrumento);
                if (serieFondo != null) {
                    flujo.Nemotecnico = serieFondo.IdSerieFondo;
                    flujo.IdMoneda = serieFondo.IdMoneda;
                }

                for (FlujoCustodia total : flujoCustodia.p_out_FlujoTotalizado) {
                    if (flujo.IdCliente.equals(total.IdCliente) && flujo.IdPortafolio.equals(total.IdPortafolio)
                            && flujo.IdInstrumento.equals(total.IdInstrumento)
                            && (regimenVacio(flujo.IdRegimenTributario) || flujo.IdRegimenTributario.equals(total.IdRegimenFondoMutuo))) {
                        flujo.CantidadDisponible = total.Cantidad;
                        flujo.MontoDisponibleCustodia = total.MontoAMercadoMonedaInstrumento;
                        break;
                    }
                }

                flujo.RescateTotal = Constantes3B.NO;
                for (RescateTotal r : rescates) {
                    if (flujo.IdCliente.equals(r.IdCliente) && flujo.IdPortafolio.equals(r.IdPortafolio) && flujo.IdInstrumento.equals(r.IdInstrumento)
                            && (regimenVacio(flujo.IdRegimenTributario) || flujo.IdRegimenTributario.equals(r.IdRegimenTributario))) {
                        flujo.RescateTotal = Constantes3B.SI;
                        flujo.CantidadDisponible = BigDecimal.ZERO;
                        flujo.MontoDisponible = BigDecimal.ZERO;
                        flujo.MontoDisponibleCustodia = BigDecimal.ZERO;
                        break;
                    }
                }

                if (formatos == null) {
                    formatos = calendario.monedaFormato();
                }
                flujo.MontoDisponibleCustodia = FormatoMonto.formatear(formatos, flujo.MontoDisponibleCustodia, flujo.IdMoneda);
                idInterno++;
                salida.add(flujo);
            }
        }
        return salida;
    }

    private static boolean regimenVacio(String idRegimenTributario) {
        return idRegimenTributario == null || idRegimenTributario.isEmpty();
    }

    private Map<Long, SerieFondo> series(List<InInstrumento> instrumentos) {
        List<Long> ids = instrumentos.stream().map(i -> i.IdInstrumento).distinct().collect(Collectors.toList());
        List<SerieFondo> series = datos.series(ids);
        return series.stream().collect(Collectors.toMap(s -> s.IdInstrumento, s -> s));
    }

    private List<RescateTotal> rescatesTotalesEnTransito(List<InInstrumento> instrumentos, List<InCarteras> carteras) {
        List<Long> idsInstrumentos = instrumentos.stream().map(i -> i.IdInstrumento).distinct().collect(Collectors.toList());
        List<Long> idsClientes = carteras.stream().map(c -> c.IdCliente).distinct().collect(Collectors.toList());
        List<Long> idsPortafolio = carteras.stream().map(c -> c.IdPortafolio).distinct().collect(Collectors.toList());
        return datos.rescatesTotalesEnTransito(idsInstrumentos, idsClientes, idsPortafolio);
    }

    private List<FlujoFondoMutuoTotalizado> valorizada(Entrada entrada, List<FlujoFondoMutuoTotalizado> salida) throws Exception {
        CuotaContrato.Entrada entradaCalculo = new CuotaContrato.Entrada();
        entradaCalculo.p_IdEmpresa = entrada.p_IdEmpresa;
        entradaCalculo.p_Series = salida.stream().filter(c -> cantidadValida(c.CantidadDisponible)).map(c -> {
            CuotaContrato.Serie s = new CuotaContrato.Serie();
            s.IdInterno = c.IdInterno;
            s.Fecha = entrada.p_FechaSaldo;
            s.IdSerieFondo = c.Nemotecnico;
            s.CalculoPorMontoCuota = Constantes3B.CALCULO_POR_CUOTA;
            s.Monto = BigDecimal.ZERO;
            s.NumeroCuotas = c.CantidadDisponible;
            s.IdMonedaFondo = c.IdMoneda;
            s.IdMonedaInversion = c.IdMoneda;
            s.TipoSolicitud = Constantes3B.TIPO_SOLICITUD_RESCATE;
            s.TipoFactorAjuste = Constantes3B.UF;
            s.IdEmpresa = entrada.p_IdEmpresa;
            return s;
        }).collect(Collectors.toList());

        CuotaContrato.Salida salidaCalculo = valorizacion.calcular(entradaCalculo);
        Map<Long, CuotaContrato.Resultado> montos = salidaCalculo.P_OUT_RESULTADO.stream().collect(Collectors.toMap(r -> r.IdInterno, r -> r));
        for (FlujoFondoMutuoTotalizado c : salida) {
            CuotaContrato.Resultado resultado = montos.get(c.IdInterno);
            if (resultado != null) {
                c.MontoDisponible = resultado.Monto;
            }
        }
        return salida;
    }

    private static boolean cantidadValida(BigDecimal cantidad) {
        return cantidad != null && cantidad.compareTo(BigDecimal.ZERO) == 1;
    }
}
