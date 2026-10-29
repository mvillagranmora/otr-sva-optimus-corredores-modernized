package cl.bcs.apv.application.contratos;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cl.bcs.apv.api.casouso.ConsultarCustodiaPortafolio;
import cl.bcs.apv.api.comun.Mensaje;
import cl.bcs.apv.api.dto.consultarcustodiaportafolio.ConsultarCustodiaPortafolioEntrada;
import cl.bcs.apv.api.dto.consultarcustodiaportafolio.ConsultarCustodiaPortafolioResultado;
import cl.bcs.apv.api.dto.consultarcustodiaportafolio.Custodia;
import cl.bcs.apv.application.comun.Json;
import cl.bcs.apv.application.comun.Mensajes;
import cl.bcs.apv.application.comun.Parametros;
import cl.bcs.apv.domain.port.out.Colaboradores;
import cl.bcs.apv.domain.port.out.ConfiguracionSistema;
import cl.bcs.apv.domain.port.out.DiccionarioMensajes;
import cl.bcs.apv.domain.port.out.contratos.CustodiaPortafolioRepositorio;
import cl.bcs.apv.domain.port.out.contratos.CustodiaPortafolioRepositorio.DetalleOrigenTraspasoFila;
import cl.bcs.apv.domain.port.out.contratos.CustodiaPortafolioRepositorio.InformacionPortafolioFila;
import cl.bcs.apv.domain.port.out.contratos.CustodiaPortafolioRepositorio.RegimenFila;

/**
 * Use case {@code ConsultarCustodiaPortafolio} (legacy {@code cl.bcs.APV.ConsultarCustodiaPortafolioAPV}); read only. The three services of other
 * modules it called in process (custody, fund custody, account balance) go through {@link Colaboradores} with the legacy JSON contract.
 */
public class ConsultarCustodiaPortafolioServicio implements ConsultarCustodiaPortafolio {

    private final CustodiaPortafolioRepositorio repositorio;
    private final ConfiguracionSistema configuracion;
    private final DiccionarioMensajes diccionario;
    private final Colaboradores colaboradores;

    public ConsultarCustodiaPortafolioServicio(CustodiaPortafolioRepositorio repositorio, ConfiguracionSistema configuracion,
            DiccionarioMensajes diccionario, Colaboradores colaboradores) {
        this.repositorio = repositorio;
        this.configuracion = configuracion;
        this.diccionario = diccionario;
        this.colaboradores = colaboradores;
    }

    // ---------------------------------------------------------------- legacy POJOs of the collaborators (only the members this service reads), decoded with Gson

    /** Legacy {@code ConsultarCustodiaPortafolio.Tabla_Salida}: the amounts default to zero as in its constructor. */
    static class Tabla {
        public String Producto;
        public String CodigoRegimenTipoAhorro;
        public String IdRegimenTributario;
        public String IdClasificacionTipoInstrumento;
        public String IdTipoInstrumento;
        public String Nemotecnico;
        public BigDecimal CantidadCustodia = BigDecimal.ZERO;
        public BigDecimal PPPCustodia = BigDecimal.ZERO;
        public BigDecimal ValorPPPCustodia = BigDecimal.ZERO;
        public BigDecimal MontoPPPCustodia = BigDecimal.ZERO;
        public Long IdPortafolio;
        public String IdEmisor;
        public Long IdInstrumento;
        public String IdMonedaInstrReajuste;
        public String DocumentoIdentidad;
        public String DocumentoIdentidadRutCavali;
        public String NombreCliente;
        public String Relacion;
        public String ISIN;
        public String ClasificacionRiesgoInstrumento;
        public String EsFungible;
        public String EsPagaderoEn;
        public String IdBoveda;
        public String NumeroCuentaBoveda;
        public BigDecimal TotalPosicion = BigDecimal.ZERO;
        public BigDecimal TotalPosicionPresunta = BigDecimal.ZERO;
        public BigDecimal Rentabilidad = BigDecimal.ZERO;
        public BigDecimal PrecioMercadoCustodia = BigDecimal.ZERO;
        public BigDecimal ValorMercadoCustodia = BigDecimal.ZERO;
        public BigDecimal MontoMercadoCustodia = BigDecimal.ZERO;
        public BigDecimal DiferenciaMercadoCustodia = BigDecimal.ZERO;
        public BigDecimal CantidadGarantia = BigDecimal.ZERO;
        public BigDecimal PrecioMercadoGarantia = BigDecimal.ZERO;
        public BigDecimal ValorMercadoGarantia = BigDecimal.ZERO;
        public BigDecimal DiferenciaMercadoGarantia = BigDecimal.ZERO;
        public BigDecimal CantidadSimulCom = BigDecimal.ZERO;
        public BigDecimal PrecioMercadoSimulCom = BigDecimal.ZERO;
        public BigDecimal ValorMercadoSimulCom = BigDecimal.ZERO;
        public BigDecimal DiferenciaMercadoSimulCom = BigDecimal.ZERO;
        public BigDecimal CantidadSimulVen = BigDecimal.ZERO;
        public BigDecimal PrecioMercadoSimulVen = BigDecimal.ZERO;
        public BigDecimal ValorMercadoSimulVen = BigDecimal.ZERO;
        public BigDecimal DiferenciaMercadoSimulVen = BigDecimal.ZERO;
        public BigDecimal CantidadPrestamo = BigDecimal.ZERO;
        public BigDecimal PrecioMercadoPrestamo = BigDecimal.ZERO;
        public BigDecimal ValorMercadoPrestamo = BigDecimal.ZERO;
        public BigDecimal DiferenciaMercadoPrestamo = BigDecimal.ZERO;
        public BigDecimal CantidadEmpresti = BigDecimal.ZERO;
        public BigDecimal PrecioMercadoEmpresti = BigDecimal.ZERO;
        public BigDecimal ValorMercadoEmpresti = BigDecimal.ZERO;
        public BigDecimal DiferenciaMercadoEmpresti = BigDecimal.ZERO;
        public BigDecimal CantidadPactosV = BigDecimal.ZERO;
        public BigDecimal PrecioMercadoPactosV = BigDecimal.ZERO;
        public BigDecimal ValorMercadoPactosV = BigDecimal.ZERO;
        public BigDecimal DiferenciaMercadoPactosV = BigDecimal.ZERO;
        public BigDecimal CantidadPactosC = BigDecimal.ZERO;
        public BigDecimal PrecioMercadoPactosC = BigDecimal.ZERO;
        public BigDecimal ValorMercadoPactosC = BigDecimal.ZERO;
        public BigDecimal DiferenciaMercadoPactosC = BigDecimal.ZERO;
        public BigDecimal CantidadReporteCompra = BigDecimal.ZERO;
        public BigDecimal PrecioMercadoReporteCompra = BigDecimal.ZERO;
        public BigDecimal ValorMercadoReporteCompra = BigDecimal.ZERO;
        public BigDecimal DiferenciaMercadoReporteCompra = BigDecimal.ZERO;
        public BigDecimal CantidadReporteVenta = BigDecimal.ZERO;
        public BigDecimal PrecioMercadoReporteVenta = BigDecimal.ZERO;
        public BigDecimal ValorMercadoReporteVenta = BigDecimal.ZERO;
        public BigDecimal DiferenciaMercadoReporteVenta = BigDecimal.ZERO;
        public BigDecimal CantidadTransIng = BigDecimal.ZERO;
        public BigDecimal CantidadTransEgr = BigDecimal.ZERO;
        public String CodigoPortafolio;
    }

    static class SalidaCustodiaPortafolio {
        public List<Tabla> P_OUT_TBL = new ArrayList<>();
    }

    /** Legacy {@code ConsultarCustodiaPortafolioFondoMutuo.SalidaCustodia}. */
    static class CustodiaFondo {
        public Long IdInstrumento;
        public Long IdPortafolio;
        public String Nemotecnico;
        public Long IdEmpresa;
        public String IdRegimenFondoMutuo;
        public BigDecimal CantidadCustodia = BigDecimal.ZERO;
        public BigDecimal CantidadTransSolicitudesRescate = BigDecimal.ZERO;
    }

    static class SalidaCustodiaFondo {
        public List<CustodiaFondo> P_OUT_TBL = new ArrayList<>();
    }

    /** Legacy {@code SaldoCuentaInversion.SaldoPorFechaEfectiva}. */
    static class SaldoPorFechaEfectiva {
        public Long IdPortafolio;
        public String IdMoneda;
        public BigDecimal Saldo;
    }

    static class SalidaSaldo {
        public List<SaldoPorFechaEfectiva> P_OUT_SALDOPORFECHAEFECTIVA = new ArrayList<>();
    }

    // ---------------------------------------------------------------- use case

    @Override
    public ConsultarCustodiaPortafolioResultado ejecutar(ConsultarCustodiaPortafolioEntrada entrada) {
        List<Mensaje> mensajes = new ArrayList<>();
        long[] errores = new long[1];

        String fechaConsulta = obtenerFechaConsulta(entrada);
        List<Tabla> custodias = obtenerCustodia(entrada, fechaConsulta, mensajes, errores);
        List<SaldoPorFechaEfectiva> saldosCuentaInversion = obtenerSaldoFechaEfectiva(custodias, entrada, fechaConsulta);
        Map<Long, InformacionPortafolioFila> informacionPortafolios = obtenerInformacionPortafolio(custodias);

        RegimenFila regimen = repositorio.regimenApvA();

        List<Custodia> salida = obtenerCustodiaSalida(entrada, custodias, saldosCuentaInversion, informacionPortafolios, regimen);
        if (!entrada.idTraspaso().equals(0L)) {
            List<DetalleOrigenTraspasoFila> detalleTraspasos = repositorio.detalleOrigenTraspaso(
                    custodias.parallelStream().map(custodia -> custodia.IdInstrumento).distinct().collect(Collectors.toList()), entrada.idTraspaso());
            salida = obtenerCustodiaTraspasoSalida(detalleTraspasos, salida);
        }

        if (!("0").equals(entrada.tipoAhorro()) && entrada.idRegimen().compareTo(0L) == 0) {
            salida = salida.stream().filter(custodia -> entrada.tipoAhorro().equals(custodia.codigoRegimenTipoAhorro())
                    || (("APV").equals(entrada.tipoAhorro()) && ("S").equals(custodia.cargaExtraParaAPVA()))).collect(Collectors.toList());
        }

        BigDecimal totalCustodia = obtenerTotal(salida, "");
        BigDecimal totalRV = obtenerTotal(salida, "ACN");
        BigDecimal totalFFMM = obtenerTotal(salida, "FFM");
        // the legacy call() does not recount the errors: the counter is the one logica incremented
        return new ConsultarCustodiaPortafolioResultado(errores[0], mensajes, totalCustodia, totalRV, totalFFMM, salida);
    }

    private String obtenerFechaConsulta(ConsultarCustodiaPortafolioEntrada entrada) {
        String fechaSistema = Parametros.fechaSistema(configuracion, entrada.idEmpresa());
        return "0001-01-01".equals(entrada.fechaConsulta()) ? fechaSistema : entrada.fechaConsulta();
    }

    private List<Tabla> obtenerCustodia(ConsultarCustodiaPortafolioEntrada entrada, String fechaConsulta, List<Mensaje> mensajes, long[] errores) {
        JsonObject peticion = Json.objeto().poner("p_IdEmpresa", entrada.idEmpresa()).poner("p_IdUsuario", entrada.idUsuario())
                .poner("p_IdModulo", entrada.idModulo()).poner("p_IdCliente", entrada.idCliente()).poner("p_IdPortafolio", entrada.idPortafolio())
                .poner("p_IdClasificacionTipoInstrumento", entrada.idClasificacionTipoInstrumento()).poner("p_IdTipoInstrumento", entrada.idTipoInstrumento())
                .poner("p_IdInstrumento", entrada.idInstrumento()).poner("p_Nemotecnico", "").poner("p_IdBoveda", entrada.idBoveda())
                .poner("p_AgrupaPorBoveda", entrada.agrupaPorBoveda()).poner("p_AgrupaPorRelacion", entrada.agrupaPorRelacion())
                .poner("p_SaldosNegativos", "N").poner("p_SaldosNegativosPresuntos", "N").poner("p_FechaConsulta", fechaConsulta)
                .poner("p_TipoFechaConsulta", "LIQ").poner("p_IdProductoAgrupador", null).poner("p_LlamadaDesdeAnalitica", "N")
                .poner("p_LlamadaDesdePatrimonio", "N").poner("p_SolicitaDividendo", "N").construir();
        SalidaCustodiaPortafolio salidaCustodia = leer(colaboradores.invocar("ConsultarCustodiaPortafolio", peticion), SalidaCustodiaPortafolio.class);

        JsonObject peticionFondo = Json.objeto().poner("p_IdEmpresa", entrada.idEmpresa()).poner("p_IdUsuario", entrada.idUsuario())
                .poner("p_IdModulo", entrada.idModulo()).poner("p_IdCliente", entrada.idCliente()).poner("p_IdPortafolio", entrada.idPortafolio())
                .poner("p_IdInstrumento", entrada.idInstrumento()).poner("p_FechaConsulta", fechaConsulta).poner("p_IdProductoAgrupador", null)
                .poner("p_IdEmisor", null).poner("p_IdFamiliaFondo", "").poner("p_BuscaMLT", "N").construir();
        SalidaCustodiaFondo salidaFondo = leer(colaboradores.invocar("ConsultarCustodiaPortafolioFondoMutuo", peticionFondo), SalidaCustodiaFondo.class);

        // LEGACY DEFECT kept (see README APV-A, ConsultarCustodiaPortafolioAPV): a parallel stream adds the messages and counts the errors without
        // synchronisation (the cases keep at most one zero-custody row so that the result is deterministic)
        salidaFondo.P_OUT_TBL.parallelStream().forEach(validaCustodia -> {
            if (validaCustodia.CantidadCustodia.equals(BigDecimal.ZERO)) {
                Map<String, String> valores = new HashMap<>();
                valores.put("Nemotecnico", validaCustodia.Nemotecnico);
                Mensaje msg = Mensajes.conDiccionario(diccionario, validaCustodia.IdEmpresa, validaCustodia.IdInstrumento, Mensajes.MENSAJE_ERROR,
                        "UI_ERROR_CLIENTE_SIN_CUSTODIA", valores);
                mensajes.add(msg);
                errores[0]++;
                return;
            }
        });

        Map<Object, List<CustodiaFondo>> solicitudesAgrupadas = salidaFondo.P_OUT_TBL.stream()
                .filter(solicitud -> solicitud.CantidadTransSolicitudesRescate.compareTo(BigDecimal.ZERO) != 0)
                .collect(Collectors.groupingBy(solicitud -> solicitud.IdInstrumento + "_" + solicitud.IdPortafolio + "_" + solicitud.IdRegimenFondoMutuo));
        for (Tabla custodia : salidaCustodia.P_OUT_TBL) {
            List<CustodiaFondo> solicitudes = solicitudesAgrupadas.get(custodia.IdInstrumento + "_" + custodia.IdPortafolio + "_" + custodia.IdRegimenTributario);
            if (solicitudes != null) {
                for (CustodiaFondo solicitud : solicitudes) {
                    custodia.CantidadCustodia = custodia.CantidadCustodia.subtract(solicitud.CantidadTransSolicitudesRescate);
                }
            }
        }

        return salidaCustodia.P_OUT_TBL.parallelStream().filter(custodia -> !"Otros (No APV)".equals(custodia.Producto)
                && ("RV".equals(custodia.IdClasificacionTipoInstrumento) || "FM".equals(custodia.IdClasificacionTipoInstrumento))
                && custodia.CantidadCustodia.compareTo(BigDecimal.ZERO) == 1).collect(Collectors.toList());
    }

    private List<SaldoPorFechaEfectiva> obtenerSaldoFechaEfectiva(List<Tabla> custodias, ConsultarCustodiaPortafolioEntrada entrada, String fechaSistema) {
        List<JsonObject> cuentas = custodias.parallelStream().map(custodia -> custodia.IdPortafolio).distinct()
                .map(portafolio -> Json.objeto().poner("IdEjecutivo", null).poner("IdPortafolio", portafolio).poner("IdMoneda", null).construir())
                .collect(Collectors.toList());
        JsonObject peticion = Json.objeto().poner("p_IdEmpresa", entrada.idEmpresa()).poner("p_IdUsuario", entrada.idUsuario())
                .poner("p_IdModulo", entrada.idModulo()).poner("p_UsaReglasNegocio", "S").poner("p_CuentaInversionAConsultar", cuentas)
                .poner("p_FechaDesde", fechaSistema).poner("p_FechaHasta", fechaSistema).poner("p_SoloSaldoNegativo", "N").poner("p_SoloSaldoDisponible", "N")
                .poner("p_SoloSaldoDisponibleNegativo", "N").poner("p_IncluyeCargosPendientes", "N").poner("p_IncluyeAbonosPendientes", "N")
                .poner("p_IncluyeOtrosSaldos", "N").poner("p_IdPortafolio", null).poner("p_IncluyeProductoApv", "N").poner("p_IdEjecutivo", null)
                .poner("p_IdCliente", null).poner("p_IdProductoAgrupador", 9999L).poner("p_IdMoneda", null)
                .poner("p_TipoSaldoConsultar", List.of("TODO")).construir();
        return leer(colaboradores.invocar("ConsultarSaldoCuentaInversion", peticion), SalidaSaldo.class).P_OUT_SALDOPORFECHAEFECTIVA;
    }

    private static BigDecimal obtenerSaldoMoneda(List<SaldoPorFechaEfectiva> saldos, String idMoneda, Long idPortafolio) {
        Optional<BigDecimal> saldo = saldos.parallelStream()
                .filter(s -> idMoneda.equals(s.IdMoneda) && idPortafolio.equals(s.IdPortafolio)).map(s -> s.Saldo).findFirst();
        return saldo.isPresent() ? saldo.get() : BigDecimal.ZERO;
    }

    private Map<Long, InformacionPortafolioFila> obtenerInformacionPortafolio(List<Tabla> custodias) {
        List<Long> portafolios = custodias.parallelStream().map(custodia -> custodia.IdPortafolio).distinct().collect(Collectors.toList());
        // LEGACY DEFECT kept (see README APV-A, ConsultarCustodiaPortafolioAPV): toMap throws IllegalStateException if the portfolio came twice
        return repositorio.informacionPortafolio(portafolios).parallelStream().collect(Collectors.toMap(p -> p.IdPortafolio(), p -> p));
    }

    private List<Custodia> obtenerCustodiaSalida(ConsultarCustodiaPortafolioEntrada entrada, List<Tabla> custodiasPortafolio,
            List<SaldoPorFechaEfectiva> saldos, Map<Long, InformacionPortafolioFila> informacionPortafolios, RegimenFila regimenAPV) {
        return custodiasPortafolio.parallelStream().map(t -> {
            BigDecimal saldoCLP = obtenerSaldoMoneda(saldos, "CLP", t.IdPortafolio);
            BigDecimal saldoUSD = obtenerSaldoMoneda(saldos, "USD", t.IdPortafolio);
            BigDecimal saldoEUR = obtenerSaldoMoneda(saldos, "EUR", t.IdPortafolio);
            InformacionPortafolioFila info = informacionPortafolios.get(t.IdPortafolio);
            // LEGACY DEFECT kept (see README APV-A, ConsultarCustodiaPortafolioAPV): NullPointerException when the portfolio is unknown (info.Regimen())
            String codigoRegimenTipoAhorro = ("BF").equals(info.Regimen()) ? "BF" : t.CodigoRegimenTipoAhorro;
            String cargaExtra = ("BF").equals(t.IdRegimenTributario)
                    && (entrada.idRegimen().equals(regimenAPV.IdRegimenTributario()) || entrada.idRegimen().equals(0L))
                    && ("BF").equals(codigoRegimenTipoAhorro) ? "S" : "N";
            return new Custodia(
                    info.NombreProductoAgrupador(),
                    info.IdProductoAgrupador(),
                    t.IdRegimenTributario,
                    t.IdClasificacionTipoInstrumento,
                    t.IdTipoInstrumento,
                    t.Nemotecnico,
                    t.CantidadCustodia,
                    t.PPPCustodia,
                    t.ValorPPPCustodia,
                    t.MontoPPPCustodia,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    t.IdPortafolio,
                    t.IdEmisor,
                    t.IdInstrumento,
                    t.IdMonedaInstrReajuste,
                    t.DocumentoIdentidad,
                    t.DocumentoIdentidadRutCavali,
                    t.NombreCliente,
                    t.Relacion,
                    t.ISIN,
                    t.ClasificacionRiesgoInstrumento,
                    t.EsFungible,
                    t.EsPagaderoEn,
                    t.IdBoveda,
                    t.NumeroCuentaBoveda,
                    t.TotalPosicion,
                    t.TotalPosicionPresunta,
                    t.Rentabilidad,
                    t.PrecioMercadoCustodia,
                    t.ValorMercadoCustodia,
                    t.MontoMercadoCustodia,
                    t.DiferenciaMercadoCustodia,
                    t.CantidadGarantia,
                    t.PrecioMercadoGarantia,
                    t.ValorMercadoGarantia,
                    t.DiferenciaMercadoGarantia,
                    t.CantidadSimulCom,
                    t.PrecioMercadoSimulCom,
                    t.ValorMercadoSimulCom,
                    t.DiferenciaMercadoSimulCom,
                    t.CantidadSimulVen,
                    t.PrecioMercadoSimulVen,
                    t.ValorMercadoSimulVen,
                    t.DiferenciaMercadoSimulVen,
                    t.CantidadPrestamo,
                    t.PrecioMercadoPrestamo,
                    t.ValorMercadoPrestamo,
                    t.DiferenciaMercadoPrestamo,
                    t.CantidadEmpresti,
                    t.PrecioMercadoEmpresti,
                    t.ValorMercadoEmpresti,
                    t.DiferenciaMercadoEmpresti,
                    t.CantidadPactosV,
                    t.PrecioMercadoPactosV,
                    t.ValorMercadoPactosV,
                    t.DiferenciaMercadoPactosV,
                    t.CantidadPactosC,
                    t.PrecioMercadoPactosC,
                    t.ValorMercadoPactosC,
                    t.DiferenciaMercadoPactosC,
                    t.CantidadReporteCompra,
                    t.PrecioMercadoReporteCompra,
                    t.ValorMercadoReporteCompra,
                    t.DiferenciaMercadoReporteCompra,
                    t.CantidadReporteVenta,
                    t.PrecioMercadoReporteVenta,
                    t.ValorMercadoReporteVenta,
                    t.DiferenciaMercadoReporteVenta,
                    t.CantidadTransIng,
                    t.CantidadTransEgr,
                    saldoCLP,
                    saldoUSD,
                    saldoEUR,
                    info.IdRegimenTributario(),
                    info.IdPlanTipoInversion(),
                    t.CodigoPortafolio,
                    codigoRegimenTipoAhorro,
                    cargaExtra);
        }).filter(custodia -> {
            return (entrada.idRegimen().equals(0L) || entrada.idRegimen().equals(custodia.idRegimenTributarioMatriz()) || ("S").equals(custodia.cargaExtraParaAPVA()))
                    && (entrada.idProducto().equals(0) || entrada.idProducto().equals(custodia.idProductoAgrupador()))
                    && (entrada.idPlan().equals(0) || entrada.idPlan().equals(custodia.idPlanTipoInversion()))
                    && (entrada.idPortafolio().equals(0L) || entrada.idPortafolio().equals(custodia.idPortafolio()))
                    && (!("").equals(custodia.codigoRegimenTipoAhorro()))
                    && ((("BF").equals(custodia.codigoRegimenTipoAhorro()) && ("BF").equals(custodia.regimen()))
                            || (!("BF").equals(custodia.codigoRegimenTipoAhorro()) && !("BF").equals(custodia.regimen())));
        }).collect(Collectors.toList());
    }

    private static List<Custodia> obtenerCustodiaTraspasoSalida(List<DetalleOrigenTraspasoFila> detalleTraspasos, List<Custodia> custodias) {
        return custodias.parallelStream().filter(custodia -> {
            List<DetalleOrigenTraspasoFila> filtro = detalleTraspasos.parallelStream()
                    .filter(traspaso -> custodia.idInstrumento().equals(traspaso.IdInstrumento())
                            && (custodia.idPortafolio().equals(traspaso.IdPortafolio()) || traspaso.IdPortafolio().equals(0L))
                            && custodia.regimen().equalsIgnoreCase(traspaso.CodigoRegimenTributario()))
                    .collect(Collectors.toList());
            return filtro.size() > 0;
        }).collect(Collectors.toList());
    }

    private static BigDecimal obtenerTotal(List<Custodia> custodias, String idTipoInstrumento) {
        return custodias.parallelStream().filter(custodia -> idTipoInstrumento.isEmpty() || idTipoInstrumento.equals(custodia.idTipoInstrumento()))
                .map(custodia -> custodia.montoMercadoCustodia()).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** The legacy decoded the answer of the collaborator into its Salida class (Gson, absent members keep the constructor default). */
    private static <T> T leer(JsonObject respuesta, Class<T> tipo) {
        JsonElement e = respuesta;
        return Json.GSON.fromJson(e, tipo);
    }
}
