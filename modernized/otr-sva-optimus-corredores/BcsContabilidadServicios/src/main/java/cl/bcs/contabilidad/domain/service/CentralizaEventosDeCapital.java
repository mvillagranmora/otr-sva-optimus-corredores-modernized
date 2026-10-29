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
 * Centralization type CentralizaEventosDeCapital (legacy CentralizaEventosDeCapital.java). Characterization: CONT-E-255..CONT-E-262.
 *
 * <p>Runs the type's own legacy SQL (legacy lines :76, :83, :91) and turns each data row into the
 * TabDatosMovimiento entries of legacy lines 95-124.
 */
@Service("centralizaEventosDeCapital")
public class CentralizaEventosDeCapital extends CentralizadorSql {

    private static final String SELECT_MOVIMIENTOS = "cl/bcs/contabilidad/sql/CentralizaEventosDeCapital_SelectMovimientos.sql";
    private static final String SELECT_EVENTOS = "cl/bcs/contabilidad/sql/CentralizaEventosDeCapital_SelectEventos.sql";
    private static final String SELECT_DATOS_MOVIMIENTO = "cl/bcs/contabilidad/sql/CentralizaEventosDeCapital_SelectDatosMovimiento.sql";

    /** Legacy CentralizaEventosDeCapital.java:95-124, one entry per legacy add(...), in order. */
    private static final List<Campo> CAMPOS = List.of(
            valorDe("IdMovimiento"),
            texto("TipoEvento"),
            texto("FechaLiquidacion"),
            texto("DocumentoIdentidad"),
            texto("NombreCliente"),
            texto("Portafolio"),
            texto("Moneda"),
            texto("EsCuentaMandante"),
            texto("EsCuentaDepositante"),
            texto("TipoPersona"),
            texto("Producto"),
            texto("ClaseInstrumento"),
            texto("TipoInstrumento"),
            texto("Nemo"),
            texto("TipoCartera"),
            texto("Boveda"),
            texto("Agente"),
            texto("NombreAgente"),
            texto("EsContraparte"),
            texto("Relacion"),
            texto("Bolsa"),
            texto("SucursalAgente"),
            texto("NombreSucursalAgente"),
            texto("CentroCostoAgente"),
            texto("Concepto"),
            texto("OpCentros"),
            texto("DocumentoIdentidadCAVALI"),
            texto("TipoEmisor"),
            texto("TipoDocumento"),
            texto("ClienteBancario"));

    public CentralizaEventosDeCapital(PlataformaPort plataforma, ContabilidadRepository repository) {
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
                        "p_DocumentoIdentidadCorredora", ctx.documentoCorredora(), "LocSistema", ctx.localizacion())), CAMPOS));
    }
}
