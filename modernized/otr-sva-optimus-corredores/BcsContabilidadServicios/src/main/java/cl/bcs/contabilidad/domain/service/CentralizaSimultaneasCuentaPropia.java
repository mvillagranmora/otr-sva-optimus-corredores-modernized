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
 * Centralization type CentralizaSimultaneasCuentaPropia (legacy CentralizaSimultaneasCuentaPropia.java). Characterization: CONT-F-134..CONT-F-144.
 *
 * <p>Runs the type's own legacy SQL (legacy lines :74, :81, :88) and turns each data row into the
 * TabDatosMovimiento entries of legacy lines 92-122.
 */
@Service("centralizaSimultaneasCuentaPropia")
public class CentralizaSimultaneasCuentaPropia extends CentralizadorSql {

    private static final String SELECT_MOVIMIENTOS = "cl/bcs/contabilidad/sql/CentralizaSimultaneasCuentaPropia_SelectMovimientos.sql";
    private static final String SELECT_EVENTOS = "cl/bcs/contabilidad/sql/CentralizaSimultaneasCuentaPropia_SelectEventos.sql";
    private static final String SELECT_DATOS_MOVIMIENTO = "cl/bcs/contabilidad/sql/CentralizaSimultaneasCuentaPropia_SelectDatosMovimiento.sql";

    /** Legacy CentralizaSimultaneasCuentaPropia.java:92-122, one entry per legacy add(...), in order. */
    private static final List<Campo> CAMPOS = List.of(
            valorDe("IdMovimiento"),
            texto("DocumentoIdentidad"),
            texto("NombreCliente"),
            texto("PortafolioCliente", "Portafolio"),
            texto("Moneda"),
            texto("Producto"),
            texto("ClaseInstrumento"),
            texto("TipoInstrumento"),
            texto("Nemo"),
            texto("Boveda"),
            texto("IdCartera"),
            texto("IdEmisor", "Emisor"),
            texto("CodigoIFRS"),
            texto("TipoCartera"),
            texto("Agente"),
            texto("NombreAgente"),
            texto("SucursalAgente"),
            texto("NombreSucursalAgente"),
            texto("CentroCostoAgente"),
            texto("Relacion"),
            texto("Bolsa"),
            fecha("FechaLiquidacion"),
            texto("TipoPersona"),
            fecha("FechaOperacion"),
            texto("Concepto", "IdConcepto"),
            texto("CostingCode", "CentroCostoAgente"),
            texto("CostingCode2", "Producto"),
            texto("CostingCode3", "Relacion"),
            texto("CostingCode4", "Bolsa"),
            vacio("CostingCode5"),
            texto("ClienteBancario"));

    public CentralizaSimultaneasCuentaPropia(PlataformaPort plataforma, ContabilidadRepository repository) {
        super(plataforma, repository);
    }

    @Override
    protected Tablas calcular(Contexto ctx) {
        return new Tablas()
                .movimientos(movimientos(SELECT_MOVIMIENTOS, parametros(
                        "p_IdEmpresa", ctx.idEmpresa(), "p_FechaContable", ctx.fechaContable(),
                        "p_IdTipoCentralizacion", ctx.idTipo(), "p_IdInforme", ctx.entrada().idInforme())))
                .eventos(eventos(SELECT_EVENTOS, parametros(
                        "p_IdEmpresa", ctx.idEmpresa(), "p_IdTipoCentralizacion", ctx.idTipo(),
                        "p_IdInforme", ctx.entrada().idInforme())))
                .datos(DatosMovimiento.de(ctx.idTipo(), filas(SELECT_DATOS_MOVIMIENTO, parametros(
                        "p_IdEmpresa", ctx.idEmpresa(), "p_DocumentoIdentidad", ctx.documentoCorredora(),
                        "p_IdInforme", ctx.entrada().idInforme())), CAMPOS));
    }
}
