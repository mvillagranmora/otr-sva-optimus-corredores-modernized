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
 * Centralization type CentralizaCorteCuponRF (legacy CentralizaCorteCuponRF.java). Characterization: CONT-E-164..CONT-E-172.
 *
 * <p>Runs the type's own legacy SQL (legacy lines :89, :96, :104) and turns each data row into the
 * TabDatosMovimiento entries of legacy lines 108-147.
 */
@Service("centralizaCorteCuponRF")
public class CentralizaCorteCuponRF extends CentralizadorSql {

    private static final String SELECT_MOVIMIENTOS = "cl/bcs/contabilidad/sql/CentralizaCorteCuponRF_SelectMovimientos.sql";
    private static final String SELECT_EVENTOS = "cl/bcs/contabilidad/sql/CentralizaCorteCuponRF_SelectEventos.sql";
    private static final String SELECT_DATOS_MOVIMIENTO = "cl/bcs/contabilidad/sql/CentralizaCorteCuponRF_SelectDatosMovimiento.sql";

    /** Legacy CentralizaCorteCuponRF.java:108-147, one entry per legacy add(...), in order. */
    private static final List<Campo> CAMPOS = List.of(
            valorDe("IdMovimiento"),
            texto("NombreCuentaCorriente"),
            texto("TipoDocumento"),
            valorDe("Folio"),
            texto("FechaDocumento"),
            texto("NumeroCuentaCorriente"),
            texto("MedioDePago"),
            texto("NumeroDocumentoPago"),
            fecha("FechaLiquidacion"),
            texto("SerieDocumento"),
            texto("Concepto"),
            texto("DocumentoIdentidad"),
            texto("NombreCliente"),
            texto("PortafolioCliente", "Portafolio"),
            texto("Moneda"),
            valorDe("CodigoCaja"),
            texto("EmitidoA"),
            texto("EsBilateral"),
            texto("TipoPersona"),
            texto("Producto"),
            texto("Relacion"),
            texto("Bolsa"),
            texto("CentroCostoAgente"),
            texto("ComprobanteTipo"),
            texto("Nemotecnico"),
            texto("CostingCode", "CentroCostoAgente"),
            texto("CostingCode2", "Producto"),
            texto("CostingCode3", "Relacion"),
            texto("CostingCode4", "Bolsa"),
            texto("CostingCode5", "OpCentros"),
            texto("TipoEmisor"),
            texto("Mesa"),
            texto("TipoCartera"),
            texto("Emisor"),
            texto("EsCuentaPropia"),
            texto("ClienteBancario"),
            texto("IdTipoReajuste"),
            texto("ClaseEmisor"),
            texto("EsCuentaTercero"));

    public CentralizaCorteCuponRF(PlataformaPort plataforma, ContabilidadRepository repository) {
        super(plataforma, repository);
    }

    @Override
    protected Tablas calcular(Contexto ctx) {
        return new Tablas()
                .movimientos(movimientos(SELECT_MOVIMIENTOS, parametros(
                        "p_IdEmpresa", ctx.idEmpresa(), "p_FechaContable", ctx.fechaContable(),
                        "p_IdTipoCentralizacion", ctx.idTipo())))
                .eventos(eventos(SELECT_EVENTOS, parametros(
                        "p_IdEmpresa", ctx.idEmpresa(), "p_FechaContable", ctx.fechaContable(),
                        "p_IdTipoCentralizacion", ctx.idTipo())))
                .datos(DatosMovimiento.de(ctx.idTipo(), filas(SELECT_DATOS_MOVIMIENTO, parametros(
                        "p_IdEmpresa", ctx.idEmpresa(), "p_FechaContable", ctx.fechaContable(),
                        "LocSistema", ctx.localizacion())), CAMPOS));
    }
}
