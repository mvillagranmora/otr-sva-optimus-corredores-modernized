package cl.bcs.contabilidad.domain.service;

import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabDatoMovimientoRef;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabEventoRef;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabMovimientoRef;
import cl.bcs.contabilidad.domain.port.out.ContabilidadRepository;
import cl.bcs.contabilidad.domain.port.out.PlataformaPort;
import cl.bcs.contabilidad.domain.port.out.PlataformaPort.ClearingFinancieroDetalle;
import cl.bcs.contabilidad.domain.service.soporte.DatoRequeridoException;
import cl.bcs.contabilidad.domain.service.soporte.DatosMovimiento;
import cl.bcs.contabilidad.domain.service.soporte.Contexto;
import cl.bcs.contabilidad.domain.service.soporte.Tablas;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

/**
 * Financial clearing (legacy CentralizaClearingFinanciero.java:53-198). Characterization: CONT-E-141..CONT-E-151.
 *
 * <p>No SQL of its own: the clearing detail comes from Tesoreria's ConsultarClearingFinanciero for the accounting date
 * (legacy also passes UsaReglasNegocio = "S" and cameras PM, RV, SM, PH, HS, which the platform port applies). Every
 * detail with IdComprobanteTesoreria &gt; 0 yields one CLEARING movement, the TOTAL_VENTA / TOTAL_COMPRA events and
 * ten data entries.
 */
@Service("centralizaClearingFinanciero")
public class CentralizaClearingFinanciero extends CentralizadorSql {

    private static final DateTimeFormatter PERIODO = DateTimeFormatter.ofPattern("MM-yy");

    public CentralizaClearingFinanciero(PlataformaPort plataforma, ContabilidadRepository repository) {
        super(plataforma, repository);
    }

    @Override
    protected Tablas calcular(Contexto ctx) {
        List<ClearingFinancieroDetalle> detalles = plataforma.consultarClearingFinanciero(
                ctx.idEmpresa(), ctx.entrada().idUsuario(), ctx.entrada().idModulo(), ctx.fechaContable());
        List<ClearingFinancieroDetalle> conComprobante = detalles == null ? List.of() : detalles.stream()
                .filter(Objects::nonNull)
                .filter(d -> d.idComprobanteTesoreria() != null && d.idComprobanteTesoreria() > 0)
                .toList();

        Tablas t = new Tablas();
        conComprobante.forEach(d -> t.movimiento(movimiento(ctx, d)));
        conComprobante.forEach(d -> {
            // legacy :92-110: a null total is reported as zero
            t.evento(new TabEventoRef(ctx.idTipo(), d.idTransaccion(), "TOTAL_VENTA", "Total Venta",
                    Objects.requireNonNullElse(d.totalVenta(), BigDecimal.ZERO), d.idMonedaTransaccion()));
            t.evento(new TabEventoRef(ctx.idTipo(), d.idTransaccion(), "TOTAL_COMPRA", "Total Compra",
                    Objects.requireNonNullElse(d.totalCompra(), BigDecimal.ZERO), d.idMonedaTransaccion()));
        });
        conComprobante.forEach(d -> datos(ctx, d, t));
        return t;
    }

    /** Legacy :71-88. */
    private static TabMovimientoRef movimiento(Contexto ctx, ClearingFinancieroDetalle d) {
        LocalDate fecha = LocalDate.parse(ctx.fechaContable());
        if (d.totalVenta() == null) {
            // legacy TotalVenta.compareTo(ZERO) -> NullPointerException
            throw new DatoRequeridoException("Monto", "TotalVenta");
        }
        BigDecimal monto = d.totalVenta().compareTo(BigDecimal.ZERO) > 0 ? d.totalVenta() : d.totalCompra();
        return new TabMovimientoRef(ctx.idTipo(), d.idTransaccion(), "CLEARING", ctx.idEmpresa(), numero(d),
                0L, ctx.fechaContable(), d.rutContraparte(), 0L, (long) fecha.getYear(), fecha.format(PERIODO), monto,
                d.idMonedaTransaccion(), "");
    }

    /** Legacy Long.parseLong(FolioTransaccion, "0" when null); a non-numeric folio aborts the type. */
    private static Long numero(ClearingFinancieroDetalle d) {
        String folio = d.folioTransaccion() == null ? "0" : d.folioTransaccion();
        try {
            return Long.parseLong(folio);
        } catch (NumberFormatException e) {
            throw new DatoRequeridoException("FolioTransaccion no numerico en la transaccion " + d.idTransaccion() + ": " + folio);
        }
    }

    /**
     * Legacy :113-194. The entries are built field by field (no-arg TabDatosMovimiento constructor), so Folio keeps a
     * null FolioTransaccion as null; every other text value defaults to "".
     */
    private static void datos(Contexto ctx, ClearingFinancieroDetalle d, Tablas t) {
        Long idTipo = ctx.idTipo();
        Long id = d.idTransaccion();
        t.dato(new TabDatoMovimientoRef(
                idTipo, id, "Folio", d.folioTransaccion(), BigDecimal.ZERO));
        t.dato(DatosMovimiento.dato(idTipo, id, "FechaDocumento", ctx.fechaContable(), BigDecimal.ZERO));
        t.dato(DatosMovimiento.dato(idTipo, id, "CompraVenta", d.compraVenta(), BigDecimal.ZERO));
        t.dato(DatosMovimiento.dato(idTipo, id, "ClaseInstrumento", d.idClasificacionTipoInstrumento(), BigDecimal.ZERO));
        t.dato(DatosMovimiento.dato(idTipo, id, "Camara", d.nombreGrupoCamara(), BigDecimal.ZERO));
        t.dato(DatosMovimiento.dato(idTipo, id, "CondicionLiquidacion", d.idCondicionDeLiquidacion(), BigDecimal.ZERO));
        t.dato(DatosMovimiento.dato(idTipo, id, "Bolsa", d.idBolsa(), BigDecimal.ZERO));
        t.dato(DatosMovimiento.dato(idTipo, id, "DocumentoIdentidad", d.rutContraparte(), BigDecimal.ZERO));
        t.dato(DatosMovimiento.dato(idTipo, id, "NombreCliente", d.razonSocialContraparte(), BigDecimal.ZERO));
        t.dato(DatosMovimiento.dato(idTipo, id, "EsContraparte", "SI", BigDecimal.ZERO));
    }
}
