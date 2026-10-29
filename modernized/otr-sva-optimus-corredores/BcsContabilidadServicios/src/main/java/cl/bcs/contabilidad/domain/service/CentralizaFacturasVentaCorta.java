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
 * Centralization type CentralizaFacturasVentaCorta (legacy CentralizaFacturasVentaCorta.java). Characterization: CONT-E-301..CONT-E-311.
 *
 * <p>Runs the type's own legacy SQL. Counterparty configuration as CentralizaFacturasCtaProp (:114-122), the type's
 * instrument classification from SelectMercadoCentralizacion (:127-129), then movements and events with the same
 * parameters and the data rows with IdEmpresa instead of IdTipoCentralizacion (:131-140).
 */
@Service("centralizaFacturasVentaCorta")
public class CentralizaFacturasVentaCorta extends CentralizadorSql {

    private static final String SELECT_MERCADO_CENTRALIZACION = "cl/bcs/contabilidad/sql/CentralizaFacturaVentaCorta_SelectMercadoCentralizacion.sql";
    private static final String SELECT_MOVIMIENTOS = "cl/bcs/contabilidad/sql/CentralizaFacturaVentaCorta_SelectMovimientos.sql";
    private static final String SELECT_EVENTOS = "cl/bcs/contabilidad/sql/CentralizaFacturaVentaCorta_SelectEventos.sql";
    private static final String SELECT_MOVIMIENTOS_DATOS = "cl/bcs/contabilidad/sql/CentralizaFacturaVentaCorta_SelectMovimientosDatos.sql";

    /** Legacy CentralizaFacturasVentaCorta.java:143-182, one entry per legacy add(...), in order. */
    private static List<Campo> campos(String documentoContraparte, String nombreContraparte) {
        return List.of(
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
                fechaHora("FechaComprobante"),
                texto("EstadoComprobante"),
                texto("EsOtrosConceptos"),
                texto("EsAPlazo"),
                texto("EsVentaCorta"),
                texto("EsArbitraje"),
                literal("DocumentoIdentidadContraparte", documentoContraparte),
                literal("NombreContraparte", nombreContraparte),
                texto("EstadoSII"));
    }

    public CentralizaFacturasVentaCorta(PlataformaPort plataforma, ContabilidadRepository repository) {
        super(plataforma, repository);
    }

    @Override
    protected Tablas calcular(Contexto ctx) {
        // PLATAFORMA / CON.DocumentoIdentidadContraparte and CON.NombreContraparte, "" when not configured
        String documentoContraparte = ctx.parametro("PLATAFORMA", "CON.DocumentoIdentidadContraparte", "");
        String nombreContraparte = ctx.parametro("PLATAFORMA", "CON.NombreContraparte", "");
        String clasificacion = clasificacionTipoInstrumento(SELECT_MERCADO_CENTRALIZACION, ctx);
        return new Tablas()
                .movimientos(movimientos(SELECT_MOVIMIENTOS, parametros(
                        "FechaContable", ctx.fechaContable(), "IdTipoCentralizacion", ctx.idTipo(),
                        "DocumentoIdentidad", ctx.documentoCorredora(), "IdClasificacionTipoInstrumento", clasificacion)))
                .eventos(eventos(SELECT_EVENTOS, parametros(
                        "FechaContable", ctx.fechaContable(), "IdTipoCentralizacion", ctx.idTipo(),
                        "DocumentoIdentidad", ctx.documentoCorredora(), "IdClasificacionTipoInstrumento", clasificacion)))
                .datos(DatosMovimiento.de(ctx.idTipo(), filas(SELECT_MOVIMIENTOS_DATOS, parametros(
                        "FechaContable", ctx.fechaContable(), "DocumentoIdentidad", ctx.documentoCorredora(),
                        "IdClasificacionTipoInstrumento", clasificacion, "IdEmpresa", ctx.empresa().idEmpresa())), campos(documentoContraparte, nombreContraparte)));
    }
}
