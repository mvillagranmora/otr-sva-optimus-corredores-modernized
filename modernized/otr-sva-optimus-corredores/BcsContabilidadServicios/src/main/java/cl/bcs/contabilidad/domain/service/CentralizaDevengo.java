package cl.bcs.contabilidad.domain.service;

import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabEventoRef;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabMovimientoRef;
import cl.bcs.contabilidad.domain.port.out.ContabilidadRepository;
import cl.bcs.contabilidad.domain.port.out.PlataformaPort;
import cl.bcs.contabilidad.domain.service.soporte.Campo;
import cl.bcs.contabilidad.domain.service.soporte.Contexto;
import cl.bcs.contabilidad.domain.service.soporte.DatosMovimiento;
import cl.bcs.contabilidad.domain.service.soporte.Fila;
import cl.bcs.contabilidad.domain.service.soporte.Tablas;

import java.util.List;

import static cl.bcs.contabilidad.domain.service.soporte.Campo.*;

/**
 * Daily accrual of lent / borrowed securities: legacy CentralizaDevengoEmprestito.java and CentralizaDevengoPrestamo.java,
 * which differ only in their SQL files, movement-type codes and instrument types (CONT-E-217..CONT-E-242).
 *
 * <p>Unlike the other types, the movements are built here, not read: the type's IdTipoMovimiento (SelectMovimientoTipo-
 * CentralizacionTipo) chooses the RV or RF variant; the open operations of the day (or the historic query when
 * FechaContable is not PLA.FechaSistema) are read, and every third-party operation (EsCuentaPropia = "NO") yields one
 * movement keyed by FolioOperacion, 16 data entries and 7 amount events in the system currency (legacy :97-190).
 * Own-account operations are skipped. Legacy also read PLATAFORMA / CUS.UsaFechaLiquidacionEnVencimiento but never used
 * it; it is not read here.
 */
public abstract class CentralizaDevengo extends CentralizadorSql {

    /** Legacy :161-176. */
    private static final List<Campo> CAMPOS = List.of(
            texto("DocumentoIdentidad"),
            texto("Portafolio", "CodigoPortafolio"),
            valorDe("EsCuentaPropia"),
            valorDe("EsRelacionado"),
            obligatorio("FechaOperacion"),
            obligatorio("FechaLiquidacion"),
            obligatorio("FolioOperacion"),
            texto("ClaseInstrumento", "IdClasificacionTipoInstrumento"),
            texto("TipoInstrumento", "IdTipoInstrumento"),
            texto("Nemotecnico"),
            texto("Boveda", "IdBoveda"),
            texto("Emisor", "IdEmisor"),
            texto("TipoReajuste", "IdTipoReajuste"),
            texto("CodigoIFRS"),
            texto("Moneda", "IdMonedaInstrumento"),
            decimal("Premio", "TasaCompra"));

    /** Legacy :180-186: event id, name and amount column. */
    private static final List<String[]> EVENTOS = List.of(
            new String[] {"MONTO_MERCADO_ANTERIOR", "Monto a Tasa Mercado Anterior", "MontoMercadoAnterior"},
            new String[] {"MONTO_MERCADO_ACTUAL", "Monto a Tasa Mercado Actual", "MontoMercadoActual"},
            new String[] {"MONTO_COMPRA_ANTERIOR", "Monto a Tasa Compra Anterior", "MontoCompraAnterior"},
            new String[] {"MONTO_COMPRA_ACTUAL", "Monto a Tasa Compra Actual", "MontoCompraActual"},
            new String[] {"MONTO_COMPRA_PROXIMO", "Monto a Tasa Compra Proximo", "MontoCompraProximo"},
            new String[] {"INTERES_COMPRA_ANTERIOR", "Interes Compra Anterior", "InteresMontoCompraAnterior"},
            new String[] {"INTERES_COMPRA_PROXIMO", "Interes Compra Proximo", "InteresMontoCompraProximo"});

    /** What distinguishes the two legacy classes. */
    protected record Variante(String sqlTipoMovimiento, String sqlOperaciones, String sqlOperacionesHistorico,
                              String movimientoRv, String movimientoRf, String instrumentoRv, String instrumentoRf) {}

    private final Variante variante;

    protected CentralizaDevengo(PlataformaPort plataforma, ContabilidadRepository repository, Variante variante) {
        super(plataforma, repository);
        this.variante = variante;
    }

    @Override
    protected Tablas calcular(Contexto ctx) {
        String tipoMovimiento = filas(variante.sqlTipoMovimiento(), parametros("IdTipoCentralizacion", ctx.idTipo()))
                .stream().findFirst().map(f -> f.texto("IdTipoMovimiento"))
                .orElseThrow(() -> new IllegalStateException("Tipo de centralizacion " + ctx.idTipo() + " sin IdTipoMovimiento"));
        boolean rv = variante.movimientoRv().equals(tipoMovimiento);

        List<Fila> operaciones = filas(ctx.esFechaSistema() ? variante.sqlOperaciones() : variante.sqlOperacionesHistorico(),
                parametros("FechaContable", ctx.fechaContable(),
                        "IdTipoInstrumento", rv ? variante.instrumentoRv() : variante.instrumentoRf(),
                        "IdEmpresa", ctx.idEmpresa()));

        Tablas t = new Tablas();
        for (Fila op : operaciones) {
            if (!"NO".equals(op.texto("EsCuentaPropia"))) continue;
            TabMovimientoRef mov = movimiento(ctx, op, rv ? variante.movimientoRv() : variante.movimientoRf());
            t.movimiento(mov);
            t.datos(DatosMovimiento.de(ctx.idTipo(), mov.idMovimiento(), op, CAMPOS));
            for (String[] e : EVENTOS) {
                t.evento(new TabEventoRef(ctx.idTipo(), mov.idMovimiento(), e[0], e[1], op.decimal(e[2]), op.texto("IdMonedaSistema")));
            }
        }
        return t;
    }

    /** Legacy :142-156. */
    private static TabMovimientoRef movimiento(Contexto ctx, Fila op, String tipoMovimiento) {
        Long folio = op.entero("FolioOperacion");
        return new TabMovimientoRef(ctx.idTipo(), folio, tipoMovimiento, op.entero("IdEmpresa"), folio, folio,
                ctx.fechaContable(), op.texto("DocumentoIdentidad"), op.entero("IdPortafolio"), ctx.entrada().ejercicio(),
                ctx.fechaContable().substring(0, 7), op.decimal("MontoCompraActual"), op.texto("IdMonedaSistema"),
                op.texto("IdEstado"));
    }
}
