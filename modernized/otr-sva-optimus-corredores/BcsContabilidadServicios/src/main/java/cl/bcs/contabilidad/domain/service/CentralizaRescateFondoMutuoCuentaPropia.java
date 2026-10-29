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
 * Centralization type CentralizaRescateFondoMutuoCuentaPropia (legacy CentralizaRescateFondoMutuoCuentaPropia.java). Characterization: CONT-F-090..CONT-F-100.
 *
 * <p>Runs the type's own legacy SQL (legacy lines :81, :90, :97) and turns each data row into the
 * TabDatosMovimiento entries of legacy lines 101-135.
 */
@Service("centralizaRescateFondoMutuoCuentaPropia")
public class CentralizaRescateFondoMutuoCuentaPropia extends CentralizadorSql {

    private static final String SELECT_MOVIMIENTOS = "cl/bcs/contabilidad/sql/CentralizaRescateFondoMutuoCuentaPropia_SelectMovimientos.sql";
    private static final String SELECT_EVENTOS = "cl/bcs/contabilidad/sql/CentralizaRescateFondoMutuoCuentaPropia_SelectEventos.sql";
    private static final String SELECT_DATOS_MOVIMIENTO = "cl/bcs/contabilidad/sql/CentralizaRescateFondoMutuoCuentaPropia_SelectDatosMovimiento.sql";

    /** Legacy CentralizaRescateFondoMutuoCuentaPropia.java:101-135, one entry per legacy add(...), in order. */
    private static final List<Campo> CAMPOS = List.of(
            texto("DocumentoIdentidad"),
            texto("NombreCliente"),
            texto("Portafolio"),
            texto("Moneda"),
            texto("Producto"),
            texto("ClaseInstrumento"),
            texto("TipoInstrumento"),
            texto("Nemo"),
            texto("Boveda"),
            valorDe("IdCartera"),
            texto("TipoReajuste"),
            texto("CodigoIFRS"),
            texto("Agente"),
            texto("NombreAgente"),
            texto("SucursalAgente"),
            texto("NombreSucursalAgente"),
            texto("Relacion"),
            texto("Bolsa"),
            fecha("FechaOperacion"),
            texto("CostingCode", "CentroCostoAgente"),
            texto("CostingCode2", "Producto"),
            texto("CostingCode3", "Relacion"),
            texto("CostingCode4", "Bolsa"),
            vacio("CostingCode5"),
            decimal("MontoMonedaSistema"),
            texto("TipoOperacionFFMM"),
            texto("IdFondo"),
            texto("Emisor"),
            texto("ClienteBancario"));

    public CentralizaRescateFondoMutuoCuentaPropia(PlataformaPort plataforma, ContabilidadRepository repository) {
        super(plataforma, repository);
    }

    @Override
    protected Tablas calcular(Contexto ctx) {
        return new Tablas()
                .movimientos(movimientos(SELECT_MOVIMIENTOS, parametros(
                        "FechaContable", ctx.fechaContable(), "IdTipoCentralizacion", ctx.idTipo(),
                        "IdEmpresa", ctx.idEmpresa(), "DocumentoIdentidadCorredora", ctx.documentoCorredora())))
                .eventos(eventos(SELECT_EVENTOS, parametros(
                        "FechaContable", ctx.fechaContable(), "IdTipoCentralizacion", ctx.idTipo(),
                        "IdMonedaSistema", ctx.monedaSistema(), "IdEmpresa", ctx.idEmpresa(),
                        "DocumentoIdentidadCorredora", ctx.documentoCorredora())))
                .datos(DatosMovimiento.de(ctx.idTipo(), filas(SELECT_DATOS_MOVIMIENTO, parametros(
                        "FechaContable", ctx.fechaContable(), "IdEmpresa", ctx.idEmpresa(),
                        "DocumentoIdentidadCorredora", ctx.documentoCorredora())), CAMPOS));
    }
}
