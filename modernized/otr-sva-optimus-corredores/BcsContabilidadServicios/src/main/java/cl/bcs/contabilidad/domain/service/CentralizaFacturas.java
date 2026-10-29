package cl.bcs.contabilidad.domain.service;

import cl.bcs.contabilidad.domain.port.out.ContabilidadRepository;
import cl.bcs.contabilidad.domain.port.out.PlataformaPort;
import cl.bcs.contabilidad.domain.service.soporte.Campo;
import cl.bcs.contabilidad.domain.service.soporte.Contexto;
import cl.bcs.contabilidad.domain.service.soporte.DatosMovimiento;
import cl.bcs.contabilidad.domain.service.soporte.Tablas;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static cl.bcs.contabilidad.domain.service.soporte.Campo.*;

/**
 * Centralization type CentralizaFacturas (legacy CentralizaFacturas.java). Characterization: CONT-E-263..CONT-E-279.
 *
 * <p>Runs the type's own legacy SQL. Reads the counterparty RUT and name (PLATAFORMA /
 * CON.DocumentoIdentidadContraparte, CON.NombreContraparte, default "", :107-115). Only localizations CL and PE
 * query anything (:119-165; Peru passes an empty DocumentoIdentidad); data rows are the common entries (:167-212)
 * plus the country's own (:215-235).
 */
@Service("centralizaFacturas")
public class CentralizaFacturas extends CentralizadorSql {

    private static final String SELECT_MOVIMIENTOS = "cl/bcs/contabilidad/sql/CentralizaFacturas_SelectMovimientos.sql";
    private static final String SELECT_EVENTOS_CHILE = "cl/bcs/contabilidad/sql/CentralizaFacturas_SelectEventosChile.sql";
    private static final String SELECT_DATOS_MOVIMIENTO_CHILE = "cl/bcs/contabilidad/sql/CentralizaFacturas_SelectDatosMovimientoChile.sql";
    private static final String SELECT_EVENTOS_PERU = "cl/bcs/contabilidad/sql/CentralizaFacturas_SelectEventosPeru.sql";
    private static final String SELECT_DATOS_MOVIMIENTO_PERU = "cl/bcs/contabilidad/sql/CentralizaFacturas_SelectDatosMovimientoPeru.sql";

    /** Peru-only entries, legacy CentralizaFacturas.java:215-230. */
    private static final List<Campo> PERU = List.of(
            texto("EstadoSUNAT"),
            texto("TipoBonoIRF"),
            texto("CodigoIFRS"),
            texto("OrigenInstrumento"),
            texto("IndicadorRelacionadoGrupo"),
            texto("Plaza"),
            texto("PactoAndino"),
            texto("FormadorMercado"),
            texto("PresenciaBursatil"),
            texto("TipoInstrumentoVenta"),
            texto("TipoInstrumentoCompra"),
            texto("DocumentoIdentidadCAVALI"),
            texto("OpCentros"),
            texto("CostingCode5", "OpCentros"));

    /** Chile-only entries, legacy CentralizaFacturas.java:231-235. */
    private static final List<Campo> CHILE = List.of(
            texto("EstadoSII"),
            texto("ClienteBancario"),
            texto("AreaCliente"));

    /** Legacy CentralizaFacturas.java:167-212: entries common to both localizations, then the localization's own. */
    private static List<Campo> campos(String documentoContraparte, String nombreContraparte, boolean peru) {
        List<Campo> campos = new ArrayList<>(List.of(
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
                valorDe("FolioDTE"),
                valorDe("Folio"),
                texto("EsExento"),
                texto("Moneda"),
                texto("MonedaLiquidacion"),
                texto("MonedaNegociacion"),
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
                texto("CostingCode", "CentroCostoAgente"),
                texto("CostingCode2", "Producto"),
                texto("CostingCode3", "Relacion"),
                texto("CostingCode4", "Bolsa"),
                texto("EsVentaCorta"),
                texto("EsArbitraje"),
                literal("DocumentoIdentidadContraparte", documentoContraparte),
                literal("NombreContraparte", nombreContraparte)));
        campos.addAll(peru ? PERU : CHILE);
        return campos;
    }

    public CentralizaFacturas(PlataformaPort plataforma, ContabilidadRepository repository) {
        super(plataforma, repository);
    }

    @Override
    protected Tablas calcular(Contexto ctx) {
        // PLATAFORMA / CON.DocumentoIdentidadContraparte and CON.NombreContraparte, "" when not configured
        String documentoContraparte = ctx.parametro("PLATAFORMA", "CON.DocumentoIdentidadContraparte", "");
        String nombreContraparte = ctx.parametro("PLATAFORMA", "CON.NombreContraparte", "");
        boolean peru;
        switch (String.valueOf(ctx.localizacion())) {
            case "CL" -> peru = false;
            case "PE" -> peru = true;
            default -> {
                return new Tablas(); // legacy queries nothing for any other localization (:119,143)
            }
        }
        // Chile filters by the broker's RUT; Peru passes an empty DocumentoIdentidad (:125,149)
        String documento = peru ? "" : ctx.documentoCorredora();
        return new Tablas()
                .movimientos(movimientos(SELECT_MOVIMIENTOS, parametros(
                        "FechaContable", ctx.fechaContable(), "IdTipoCentralizacion", ctx.idTipo(),
                        "DocumentoIdentidad", documento)))
                .eventos(eventos(peru ? SELECT_EVENTOS_PERU : SELECT_EVENTOS_CHILE, parametros(
                        "FechaContable", ctx.fechaContable(), "IdTipoCentralizacion", ctx.idTipo(),
                        "DocumentoIdentidad", documento)))
                .datos(DatosMovimiento.de(ctx.idTipo(), filas(peru ? SELECT_DATOS_MOVIMIENTO_PERU : SELECT_DATOS_MOVIMIENTO_CHILE, parametros(
                        "FechaContable", ctx.fechaContable(), "IdEmpresa", ctx.idEmpresa(),
                        "IdTipoCentralizacion", ctx.idTipo(), "DocumentoIdentidad", documento)),
                        campos(documentoContraparte, nombreContraparte, peru)));
    }
}
