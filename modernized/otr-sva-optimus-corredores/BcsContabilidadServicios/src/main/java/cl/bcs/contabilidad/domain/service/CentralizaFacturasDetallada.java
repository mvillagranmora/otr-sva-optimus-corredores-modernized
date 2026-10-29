package cl.bcs.contabilidad.domain.service;

import cl.bcs.contabilidad.domain.port.out.ContabilidadRepository;
import cl.bcs.contabilidad.domain.port.out.PlataformaPort;
import cl.bcs.contabilidad.domain.service.soporte.Campo;
import cl.bcs.contabilidad.domain.service.soporte.Contexto;
import cl.bcs.contabilidad.domain.service.soporte.DatosMovimiento;
import cl.bcs.contabilidad.domain.service.soporte.Tablas;
import org.springframework.stereotype.Service;

import java.util.List;

import static cl.bcs.contabilidad.domain.service.soporte.Campo.*;

/**
 * Centralization type CentralizaFacturasDetallada (legacy CentralizaFacturasDetallada.java). Characterization: CONT-E-290..CONT-E-300.
 *
 * <p>Runs the type's own legacy SQL. Movements by the broker RUT (:105-111); events and data rows exist only for
 * localization CL (:113-124, :204-206; the Peru branch is commented out in legacy).
 */
@Service("centralizaFacturasDetallada")
public class CentralizaFacturasDetallada extends CentralizadorSql {

    private static final String SELECT_MOVIMIENTOS = "cl/bcs/contabilidad/sql/CentralizaFacturasDetallada_SelectMovimientos.sql";
    private static final String SELECT_EVENTOS_CHILE = "cl/bcs/contabilidad/sql/CentralizaFacturasDetallada_SelectEventosChile.sql";
    private static final String SELECT_DATOS_MOVIMIENTO_CHILE = "cl/bcs/contabilidad/sql/CentralizaFacturasDetallada_SelectDatosMovimientoChile.sql";

    /** Legacy CentralizaFacturasDetallada.java:140-205, one entry per legacy add(...), in order. */
    private static final List<Campo> CAMPOS = List.of(
            texto("TipoDocumento"),
            texto("FechaDocumento"),
            texto("CompraVenta"),
            texto("Operacion"),
            texto("Producto"),
            texto("ClaseInstrumento"),
            texto("TipoInstrumento"),
            texto("CondicionLiquidacion"),
            texto("MonedaComprobante"),
            texto("Bolsa"),
            texto("DocumentoIdentidad"),
            texto("NombreCliente"),
            texto("Portafolio"),
            texto("Concepto"),
            texto("NombreConcepto"),
            texto("Agente"),
            texto("NombreAgente"),
            texto("SucursalAgente"),
            texto("NombreSucursalAgente"),
            texto("CentroCostoAgente"),
            valorDe("FolioPoliza"),
            valorDe("Folio"),
            texto("EsExento"),
            texto("Moneda"),
            texto("MonedaLiquidacion"),
            texto("EsContraparte"),
            texto("Relacion"),
            texto("Emisor"),
            texto("Nemotecnico"),
            texto("TipoPersona"),
            texto("CodigoTipoComprobante"),
            fecha("FechaLiquidacion"),
            texto("TipoComprobante"),
            fechaHora("FechaComprobante"),
            texto("EstadoComprobante"),
            texto("EsOtrosConceptos"),
            texto("EsAPlazo"),
            texto("CostingCode", "CentroCostoAgente"),
            texto("CostingCode2", "Producto"),
            texto("CostingCode3", "Relacion"),
            texto("CostingCode4", "Bolsa"),
            texto("EsVentaCorta"),
            texto("EsArbitraje"),
            texto("CorredoraContraparte"),
            texto("NombreCorredoraContraparte"),
            texto("EstadoSII"));

    public CentralizaFacturasDetallada(PlataformaPort plataforma, ContabilidadRepository repository) {
        super(plataforma, repository);
    }

    @Override
    protected Tablas calcular(Contexto ctx) {
        Tablas t = new Tablas()
                .movimientos(movimientos(SELECT_MOVIMIENTOS, parametros(
                        "FechaContable", ctx.fechaContable(), "IdTipoCentralizacion", ctx.idTipo(),
                        "DocumentoIdentidad", ctx.documentoCorredora())));
        // events and data rows exist only for Chile (:113-124); the Peru branch is commented out in legacy
        if (!"CL".equals(ctx.localizacion())) {
            return t;
        }
        return t.eventos(eventos(SELECT_EVENTOS_CHILE, parametros(
                "FechaContable", ctx.fechaContable(), "IdTipoCentralizacion", ctx.idTipo(),
                "DocumentoIdentidad", ctx.documentoCorredora())))
                .datos(DatosMovimiento.de(ctx.idTipo(), filas(SELECT_DATOS_MOVIMIENTO_CHILE, parametros(
                        "FechaContable", ctx.fechaContable(), "IdEmpresa", ctx.idEmpresa(),
                        "IdTipoCentralizacion", ctx.idTipo(), "DocumentoIdentidad", ctx.documentoCorredora())), CAMPOS));
    }
}
