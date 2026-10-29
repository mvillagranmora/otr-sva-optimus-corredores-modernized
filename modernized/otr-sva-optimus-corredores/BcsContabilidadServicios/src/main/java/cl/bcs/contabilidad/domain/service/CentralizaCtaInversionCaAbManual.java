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
 * Centralization type CentralizaCtaInversionCaAbManual (legacy CentralizaCtaInversionCaAbManual.java). Characterization: CONT-E-173..CONT-E-181.
 *
 * <p>Runs the type's own legacy SQL (legacy lines :72, :78, :84) and turns each data row into the
 * TabDatosMovimiento entries of legacy lines 88-116.
 */
@Service("centralizaCtaInversionCaAbManual")
public class CentralizaCtaInversionCaAbManual extends CentralizadorSql {

    private static final String SELECT_MOVIMIENTOS = "cl/bcs/contabilidad/sql/CentralizaCtaInversionCaAbManual_SelectMovimientos.sql";
    private static final String SELECT_EVENTOS = "cl/bcs/contabilidad/sql/CentralizaCtaInversionCaAbManual_SelectEventos.sql";
    private static final String SELECT_DATOS_MOVIMIENTO = "cl/bcs/contabilidad/sql/CentralizaCtaInversionCaAbManual_SelectDatosMovimiento.sql";

    /** Legacy CentralizaCtaInversionCaAbManual.java:88-116, one entry per legacy add(...), in order. */
    private static final List<Campo> CAMPOS = List.of(
            decimalDeEntero("IdEmpresa", "IdEmpresa"),
            texto("Fecha"),
            texto("DocumentoIdentidad"),
            texto("Portafolio"),
            texto("Concepto"),
            texto("NombreConcepto"),
            texto("DocumentoIdentidadCAVALI"),
            texto("NombreCliente"),
            texto("Moneda"),
            texto("Producto"),
            texto("Agente"),
            texto("NombreAgente"),
            texto("SucursalAgente"),
            texto("NombreSucursalAgente"),
            texto("CentroCostoAgente"),
            texto("EsContraparte"),
            texto("Relacion"),
            texto("Bolsa"),
            texto("TipoPersona"),
            texto("Nemo"),
            texto("TipoDocumento"),
            texto("TipoComprobante", "IdTipoComprobante"),
            texto("CostingCode", "CentroCostoAgente"),
            texto("CostingCode2", "Producto"),
            texto("CostingCode3", "Relacion"),
            texto("CostingCode4", "Bolsa"),
            texto("CostingCode5", "OpCentros"),
            texto("ClienteBancario"),
            texto("OperacionNoFacturable"));

    public CentralizaCtaInversionCaAbManual(PlataformaPort plataforma, ContabilidadRepository repository) {
        super(plataforma, repository);
    }

    @Override
    protected Tablas calcular(Contexto ctx) {
        return new Tablas()
                .movimientos(movimientos(SELECT_MOVIMIENTOS, parametros(
                        "FechaContable", ctx.fechaContable(), "IdTipoCentralizacion", ctx.idTipo())))
                .eventos(eventos(SELECT_EVENTOS, parametros(
                        "FechaContable", ctx.fechaContable(), "IdTipoCentralizacion", ctx.idTipo())))
                .datos(DatosMovimiento.de(ctx.idTipo(), filas(SELECT_DATOS_MOVIMIENTO, parametros(
                        "FechaContable", ctx.fechaContable(), "LocSistema", ctx.localizacion())), CAMPOS));
    }
}
