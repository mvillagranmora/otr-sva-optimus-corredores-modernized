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
 * Centralization type CentralizaFacturasCtaProp (legacy CentralizaFacturasCtaProp.java). Characterization: CONT-E-280..CONT-E-289.
 *
 * <p>Runs the type's own legacy SQL. The configured counterparty RUT and name (PLATAFORMA /
 * CON.DocumentoIdentidadContraparte, CON.NombreContraparte, default "", :85-95) are rendered into every data row
 * (:157-158).
 */
@Service("centralizaFacturasCtaProp")
public class CentralizaFacturasCtaProp extends CentralizadorSql {

    private static final String SELECT_MOVIMIENTOS_CTA_PROP = "cl/bcs/contabilidad/sql/CentralizaFacturas_SelectMovimientosCtaProp.sql";
    private static final String SELECT_EVENTOS_CTA_PROP = "cl/bcs/contabilidad/sql/CentralizaFacturas_SelectEventosCtaProp.sql";
    private static final String SELECT_DATOS_MOVIMIENTO_CTA_PROP = "cl/bcs/contabilidad/sql/CentralizaFacturas_SelectDatosMovimientoCtaProp.sql";

    /** Legacy CentralizaFacturasCtaProp.java:121-162, one entry per legacy add(...), in order. */
    private static List<Campo> campos(String documentoContraparte, String nombreContraparte) {
        return List.of(
                texto("TipoDocumento"),
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
                valorDe("Folio"),
                valorDe("FolioDTE"),
                texto("NombreAgente"),
                texto("SucursalAgente"),
                texto("NombreSucursalAgente"),
                texto("CentroCostoAgente"),
                texto("EstadoSII"),
                texto("EsExento"),
                texto("Moneda"),
                texto("MonedaLiquidacion"),
                texto("ClaseEmisor"),
                texto("Emisor"),
                texto("TipoEmisor"),
                texto("Nemotecnico"),
                texto("Relacion"),
                texto("TipoPersona"),
                texto("CodigoIFRS"),
                texto("CodigoTipoComprobante"),
                fecha("FechaLiquidacion"),
                texto("TipoComprobante"),
                fechaHora("FechaComprobante"),
                literal("DocumentoIdentidadContraparte", documentoContraparte),
                literal("NombreContraparte", nombreContraparte),
                texto("IndicadorOrdenDirecta"),
                texto("TipoReajuste"),
                texto("AreaCliente"),
                valorDe("FolioDTE"));
    }

    public CentralizaFacturasCtaProp(PlataformaPort plataforma, ContabilidadRepository repository) {
        super(plataforma, repository);
    }

    @Override
    protected Tablas calcular(Contexto ctx) {
        // PLATAFORMA / CON.DocumentoIdentidadContraparte and CON.NombreContraparte, "" when not configured
        String documentoContraparte = ctx.parametro("PLATAFORMA", "CON.DocumentoIdentidadContraparte", "");
        String nombreContraparte = ctx.parametro("PLATAFORMA", "CON.NombreContraparte", "");
        return new Tablas()
                .movimientos(movimientos(SELECT_MOVIMIENTOS_CTA_PROP, parametros(
                        "FechaContable", ctx.fechaContable(), "IdTipoCentralizacion", ctx.idTipo(),
                        "DocumentoIdentidad", ctx.documentoCorredora())))
                .eventos(eventos(SELECT_EVENTOS_CTA_PROP, parametros(
                        "FechaContable", ctx.fechaContable(), "IdTipoCentralizacion", ctx.idTipo(),
                        "DocumentoIdentidad", ctx.documentoCorredora())))
                .datos(DatosMovimiento.de(ctx.idTipo(), filas(SELECT_DATOS_MOVIMIENTO_CTA_PROP, parametros(
                        "FechaContable", ctx.fechaContable(), "IdEmpresa", ctx.idEmpresa(),
                        "DocumentoIdentidad", ctx.documentoCorredora())), campos(documentoContraparte, nombreContraparte)));
    }
}
