package cl.bcs.fondomutuo.consultas.detalle;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import cl.bcs.fondomutuo.cierre.FormatoMoneda;
import cl.bcs.fondomutuo.cuotas.CalcularMontoNumeroCuota;
import cl.bcs.fondomutuo.kit.MensajeSalida;
import cl.bcs.fondomutuo.port.out.kit.ConfiguracionSistemaPort;
import cl.bcs.fondomutuo.port.out.kit.DiccionarioMensajesPort;

/**
 * Legacy {@code ConsultarDetalleSolicitud.logica} (FM-DET-01..04, read only) INCLUDING its delegate {@code ConsultarSolicitudAporteRescateUI.logica} (D-F3A-24): the
 * delegate SELECT for the company and the id, the valuation of the rows in state ING / PEN with {@code CalcularMontoNumeroCuota} (the only collaborators outside
 * the module, {@code CalcularParidad} and {@code EntregarPreciosTasas}, are its ports), the APV retention, and the copy of the 110 fields. Bug for bug: a
 * missing id is the message {@code NO_SE_INGRESARON_SOLICITUDES_APORTE_RESCATE} before anything is read; the valuation ALWAYS runs (even with no series); a row repeated by a
 * JOIN is an IllegalStateException of the {@code toMap}; a failed valuation leaves a null {@code Monto} and the retention raises NullPointerException (not caught);
 * the order of the SQL result is preserved. No state; the unit of work is the ambient read-only transaction.
 */
public class ConsultarDetalleService implements ConsultarDetallePort {

    private static final String ESTADO_INGRESADO = "ING";
    private static final String ESTADO_PENDIENTE = "PEN";
    private static final String TIPO_SOLICITUD_APORTE = "APO";
    private static final String TIPO_SOLICITUD_RESCATE = "RES";
    private static final String RESCATE_FONDO_MUTUO = "RESNORM";
    private static final String APV_B = "APV-B";
    private static final String APV_AN = "APV-AN";
    private static final String CUENTA_INVERSION = "CIN";
    private static final String UF = "UF";

    private final DiccionarioMensajesPort diccionario;
    private final ConsultarDetalleDatos datos;
    private final CalcularMontoNumeroCuota cuotas;
    private final ConfiguracionSistemaPort configuracion;
    private final FormatoMoneda formato;

    public ConsultarDetalleService(DiccionarioMensajesPort diccionario, ConsultarDetalleDatos datos, CalcularMontoNumeroCuota cuotas,
            ConfiguracionSistemaPort configuracion, FormatoMoneda formato) {
        this.diccionario = diccionario;
        this.datos = datos;
        this.cuotas = cuotas;
        this.configuracion = configuracion;
        this.formato = formato;
    }

    @Override
    public ConsultarDetalleContrato.Salida logica(ConsultarDetalleContrato.Entrada entrada) throws Exception {
        ConsultarDetalleContrato.Salida out = new ConsultarDetalleContrato.Salida();
        if (entrada.p_SolicitudesAporteRescate == null || entrada.p_SolicitudesAporteRescate == 0) {
            out.P_OUT_ERRORESCONTADOR = 1L;
            out.P_OUT_MENSAJES.add(MensajeSalida.conTexto(0L, MensajeSalida.MENSAJE_ERROR, "NO_SE_INGRESARON_SOLICITUDES_APORTE_RESCATE",
                    new HashMap<String, String>(), diccionario, entrada.p_IdEmpresa));
            return out;
        }
        List<ConsultarDetalleContrato.SolicitudUi> solicitudes = datos.solicitudes(entrada.p_IdEmpresa, entrada.p_SolicitudesAporteRescate);
        valorizar(entrada.p_IdEmpresa, solicitudes);
        BigDecimal porcentajeRetencion = porcentajeRetencion(entrada.p_IdEmpresa);
        calcularRetencion(porcentajeRetencion, solicitudes);
        out.P_OUT_RESULTADO = resultado(solicitudes);
        return out;
    }

    private static boolean pendienteCertificacion(String estadoSolicitud) {
        return ESTADO_INGRESADO.equals(estadoSolicitud) || ESTADO_PENDIENTE.equals(estadoSolicitud);
    }

    /** Legacy {@code Fecha.stringToSqlDate(java.util.Date)}: {@code yyyy-MM-dd}; a null date is a NullPointerException. */
    private static String texto(java.util.Date fecha) {
        return new SimpleDateFormat("yyyy-MM-dd").format(fecha);
    }

    private void valorizar(Long idEmpresa, List<ConsultarDetalleContrato.SolicitudUi> solicitudes) throws Exception {
        CalcularMontoNumeroCuota.EntradaCalcularMontoNumeroCuota entradaCalculo = new CalcularMontoNumeroCuota.EntradaCalcularMontoNumeroCuota();
        entradaCalculo.p_IdEmpresa = idEmpresa;
        entradaCalculo.p_Series = solicitudes.stream().filter(s -> pendienteCertificacion(s.EstadoSolicitud)).map(s -> {
            CalcularMontoNumeroCuota.Serie serie = new CalcularMontoNumeroCuota.Serie();
            serie.IdInterno = s.IdSolicitudAporteRescate;
            serie.Fecha = texto(s.FechaValorCuota);
            serie.IdSerieFondo = s.IdSerieFondo;
            serie.CalculoPorMontoCuota = s.CalculoPorMontoCuota;
            serie.Monto = s.Monto;
            serie.NumeroCuotas = s.NumeroCuotas;
            serie.IdMonedaFondo = s.IdMonedaFondo;
            serie.IdMonedaInversion = s.IdMoneda;
            serie.TipoSolicitud = s.TipoSolicitud;
            serie.TipoFactorAjuste = UF;
            serie.IdEmpresa = idEmpresa;
            serie.IdTipoMovimientoFondoMutuo = s.IdTipoMovimientoFondoMutuo == null ? "" : s.IdTipoMovimientoFondoMutuo;
            return serie;
        }).collect(Collectors.toList());

        CalcularMontoNumeroCuota.SalidaCalcularMontoNumeroCuota salida = cuotas.logica(entradaCalculo);
        Map<Long, CalcularMontoNumeroCuota.Resultado> montosValorizados = salida.P_OUT_RESULTADO.stream()
                .collect(Collectors.toMap(r -> r.IdInterno, r -> r));

        for (ConsultarDetalleContrato.SolicitudUi s : solicitudes) {
            if (!pendienteCertificacion(s.EstadoSolicitud)) {
                continue;
            }
            CalcularMontoNumeroCuota.Resultado r = montosValorizados.get(s.IdSolicitudAporteRescate);
            if (r != null) {
                s.CostoMonedaLocal = r.MontoDestino;
                s.Monto = r.Monto;
                s.ValorCuota = r.ValorCuota;
                s.NumeroCuotas = r.NumeroCuotas;
                s.ParidadFechaCertificacion = r.ValorParidad;
                s.CostoUF = r.CostoEnFactorAjuste;
                s.ValorUFFechaInversion = r.ValorFactorAjuste;
                s.MontoMonedaSistema = cambiaMontoMonedaSistema(s) ? r.MontoMonedaSistema : s.MontoMonedaSistema;
            }
        }
    }

    private static boolean cambiaMontoMonedaSistema(ConsultarDetalleContrato.SolicitudUi s) {
        return TIPO_SOLICITUD_RESCATE.equals(s.TipoSolicitud)
                || (TIPO_SOLICITUD_APORTE.equals(s.TipoSolicitud) && (s.IdOperacionSpot == null || s.IdOperacionSpot.equals(0L)));
    }

    /** Legacy: {@code APV.PorcentajeRetencion} as a long; any failure (missing, not a long) is 0; then divided by 100. */
    private BigDecimal porcentajeRetencion(Long idEmpresa) {
        BigDecimal porcentaje = BigDecimal.ZERO;
        try {
            porcentaje = new BigDecimal(configuracion.entero(idEmpresa, "PLATAFORMA", "APV.PorcentajeRetencion"));
        } catch (Exception e) {
            // the legacy swallows it: the retention is 0
        }
        return porcentaje.divide(new BigDecimal("100"), MathContext.DECIMAL128);
    }

    private void calcularRetencion(BigDecimal porcentajeRetencion, List<ConsultarDetalleContrato.SolicitudUi> solicitudes) {
        for (ConsultarDetalleContrato.SolicitudUi s : solicitudes) {
            boolean conRetencion = RESCATE_FONDO_MUTUO.equals(s.IdTipoMovimientoFondoMutuo)
                    && (APV_AN.equals(s.IdRegimenTributarioAPV) || APV_B.equals(s.IdRegimenTributarioAPV))
                    && s.IdTraspaso == null
                    && !CUENTA_INVERSION.equals(s.IdMedioDePago);
            if (conRetencion) {
                BigDecimal montoRetencion = s.Monto.multiply(porcentajeRetencion, MathContext.DECIMAL128);   // a null Monto is a NullPointerException, not caught
                try {
                    montoRetencion = formato.formatear(montoRetencion, FormatoMoneda.MONTO, s.IdMoneda);
                } catch (Exception e) {
                    // the legacy swallows it: the value stays unformatted
                }
                s.MontoRetencion = montoRetencion;
            }
        }
    }

    private static List<ConsultarDetalleContrato.OutSolicitudesAporteRescate> resultado(List<ConsultarDetalleContrato.SolicitudUi> solicitudes) {
        List<ConsultarDetalleContrato.OutSolicitudesAporteRescate> resultados = new ArrayList<>();
        for (ConsultarDetalleContrato.SolicitudUi solicitud : solicitudes) {
            ConsultarDetalleContrato.OutSolicitudesAporteRescate resultado = new ConsultarDetalleContrato.OutSolicitudesAporteRescate();
            resultado.IdSolicitudAporteRescate = solicitud.IdSolicitudAporteRescate;
            resultado.IdCliente = solicitud.IdCliente;
            resultado.IdPortafolio = solicitud.IdPortafolio;
            resultado.FechaEmision = solicitud.FechaEmision;
            resultado.FechaIngresoRegistro = solicitud.FechaIngresoRegistro;
            resultado.IdTipoMovimientoFondoMutuo = solicitud.IdTipoMovimientoFondoMutuo;
            resultado.TipoSolicitud = solicitud.TipoSolicitud;
            resultado.DescripcionTipoMovimiento = solicitud.DescripcionTipoMovimiento;
            resultado.CalculoMontoCuotaUI = solicitud.CalculoMontoCuotaUI;
            resultado.EstadoSolicitud = solicitud.EstadoSolicitud;
            resultado.NombreEstado = solicitud.NombreEstado;
            resultado.NombreFondo = solicitud.NombreFondo;
            resultado.NombreFondoReal = solicitud.NombreFondoReal;
            resultado.IdSerieFondo = solicitud.IdSerieFondo;
            resultado.NombreSerieFondo = solicitud.NombreSerieFondo;
            resultado.CalculoPorMontoCuotaUI = solicitud.CalculoPorMontoCuotaUI;
            resultado.NumeroCuotas = solicitud.NumeroCuotas;
            resultado.ValorCuota = solicitud.ValorCuota;
            resultado.Monto = solicitud.Monto;
            resultado.MontoRetencion = solicitud.MontoRetencion;
            resultado.DocumentoIdentidad = solicitud.DocumentoIdentidad;
            resultado.NombreCompleto = solicitud.NombreCompleto;
            resultado.Nombres = solicitud.Nombres;
            resultado.ApellidoPaterno = solicitud.ApellidoPaterno;
            resultado.ApellidoMaterno = solicitud.ApellidoMaterno;
            resultado.IdInstrumento = solicitud.IdInstrumento;
            resultado.IdRegimenTributario = solicitud.IdRegimenTributario;
            resultado.DescripcionPortafolio = solicitud.DescripcionPortafolio;
            resultado.FechaCertificacion = solicitud.FechaCertificacion;
            resultado.FechaLiquidacion = solicitud.FechaLiquidacion;
            resultado.FechaAceptacion = solicitud.FechaAceptacion;
            resultado.FechaAnulacion = solicitud.FechaAnulacion;
            resultado.FechaAceptacionRegistro = solicitud.FechaAceptacionRegistro;
            resultado.FechaCertificacionRegistro = solicitud.FechaCertificacionRegistro;
            resultado.FechaLiquidacionRegistro = solicitud.FechaLiquidacionRegistro;
            resultado.FechaAnulacionRegistro = solicitud.FechaAnulacionRegistro;
            resultado.NombreEmpleado = solicitud.NombreEmpleado;
            resultado.IdEmisor = solicitud.IdEmisor;
            resultado.IdEmpresa = solicitud.IdEmpresa;
            resultado.IdMoneda = solicitud.IdMoneda;
            resultado.TextoPreferenciaPago = solicitud.TextoPreferenciaPago;
            resultado.Glosa = solicitud.Glosa;
            resultado.DecimalesValorCuota = solicitud.DecimalesValorCuota;
            resultado.DecimalesCuota = solicitud.DecimalesCuota;
            resultado.EsOvernightUI = solicitud.EsOvernightUI;
            resultado.IdSolicitudAporteRescateRelacionado = solicitud.IdSolicitudAporteRescateRelacionado;
            resultado.DescripcionPortafolioDestino = solicitud.DescripcionPortafolioDestino;
            resultado.IdPortafolioDestino = solicitud.IdPortafolioDestino;
            resultado.IdClienteDestino = solicitud.IdClienteDestino;
            resultado.DocumentoIdentidadDestino = solicitud.DocumentoIdentidadDestino;
            resultado.NombreCompletoDestino = solicitud.NombreCompletoDestino;
            resultado.EsIndicadorMLTUI = solicitud.EsIndicadorMLTUI;
            resultado.MontoComision = solicitud.MontoComision;
            resultado.IdTipoMovimientoFondoMutuoRelacionado = solicitud.IdTipoMovimientoFondoMutuoRelacionado;
            resultado.DescripcionTipoMovimientoRelacionado = solicitud.DescripcionTipoMovimientoRelacionado;
            resultado.IdMonedaFondo = solicitud.IdMonedaFondo;
            resultado.EsDiferido = solicitud.EsDiferido;
            resultado.EsDiferidoUI = solicitud.EsDiferidoUI;
            resultado.CostoMonedaLocal = solicitud.CostoMonedaLocal;
            resultado.ParidadFechaInversion = solicitud.ParidadFechaInversion;
            resultado.MontoInversion = solicitud.MontoInversion;
            resultado.NumeroCuotasInversion = solicitud.NumeroCuotasInversion;
            resultado.ValorCuotaInversion = solicitud.ValorCuotaInversion;
            resultado.CostoMonedaLocalInversion = solicitud.CostoMonedaLocalInversion;
            resultado.ParidadFechaCertificacion = solicitud.ParidadFechaCertificacion;
            resultado.CalculoPorMontoCuota = solicitud.CalculoPorMontoCuota;
            resultado.IdSerieFondoDestino = solicitud.IdSerieFondoDestino;
            resultado.IdEmisorDestino = solicitud.IdEmisorDestino;
            resultado.SolicitudBatch = solicitud.SolicitudBatch;
            resultado.OrigenOperacion = solicitud.OrigenOperacion;
            resultado.ComisionInformada = solicitud.ComisionInformada;
            resultado.EstadoLiquidacion = solicitud.EstadoLiquidacion;
            resultado.NombreEstadoLiquidacion = solicitud.NombreEstadoLiquidacion;
            resultado.FolioCartaLiquidacion = solicitud.FolioCartaLiquidacion;
            resultado.CostoUF = solicitud.CostoUF;
            resultado.ValorUFFechaInversion = solicitud.ValorUFFechaInversion;
            resultado.Spread = solicitud.Spread;
            resultado.FechaOriginalInversion = solicitud.FechaOriginalInversion;
            resultado.MontoMovimientoAsociado = solicitud.MontoMovimientoAsociado;
            resultado.NumeroCuotasMovimientoAsociado = solicitud.NumeroCuotasMovimientoAsociado;
            resultado.IndicadorMLT = solicitud.IndicadorMLT;
            resultado.IdEmisorOrigen = solicitud.IdEmisorOrigen;
            resultado.CodigoPortafolio = solicitud.CodigoPortafolio;
            resultado.CodigoPortafolioDestino = solicitud.CodigoPortafolioDestino;
            resultado.CodigoSerie = solicitud.CodigoSerie;
            resultado.IdFondoRelacionado = solicitud.IdFondoRelacionado;
            resultado.CodigoSerieRelacionado = solicitud.CodigoSerieRelacionado;
            resultado.DescripcionRegimenTributario = solicitud.DescripcionRegimenTributario;
            resultado.IdFondo = solicitud.IdFondo;
            resultado.DescripcionTipoSolicitud = solicitud.DescripcionTipoSolicitud;
            resultado.MontoMonedaSistema = solicitud.MontoMonedaSistema;
            resultado.IdDividendo = solicitud.IdDividendo;
            resultado.Switch = solicitud.Switch;
            resultado.SwitchUI = solicitud.SwitchUI;
            resultado.IdTipoCanalIngreso = solicitud.IdTipoCanalIngreso;
            resultado.FolioContrato = solicitud.FolioContrato;
            resultado.Producto = solicitud.Producto;
            resultado.IdProductoAgrupador = solicitud.IdProductoAgrupador;
            resultado.CodigoUsuario = solicitud.CodigoUsuario;
            resultado.IdIngresoEgreso = solicitud.IdIngresoEgreso;
            resultado.NombreEstadoTesoreria = solicitud.NombreEstadoTesoreria;
            resultado.FechaLiquidacionTesoreria = solicitud.FechaLiquidacionTesoreria;
            resultado.IdOperacionSpot = solicitud.IdOperacionSpot;
            resultado.NombreEstadoSpot = solicitud.NombreEstadoSpot;
            resultado.NombreEstadoConfirmacionSpot = solicitud.NombreEstadoConfirmacionSpot;
            resultado.FechaLiquidacionSpot = solicitud.FechaLiquidacionSpot;
            resultado.IdTipoOperacionSpot = solicitud.IdTipoOperacionSpot;
            resultado.NombreTipoOperacionSpot = solicitud.NombreTipoOperacionSpot;
            resultado.IdOmnibus = solicitud.IdOmnibus;
            resultado.IdAgrupador = solicitud.IdAgrupador;
            resultados.add(resultado);
        }
        return resultados;
    }
}
