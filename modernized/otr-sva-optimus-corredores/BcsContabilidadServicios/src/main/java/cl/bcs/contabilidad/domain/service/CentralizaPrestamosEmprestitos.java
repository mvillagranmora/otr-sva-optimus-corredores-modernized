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
 * Centralization type CentralizaPrestamosEmprestitos (legacy CentralizaPrestamosEmprestitos.java). Characterization: CONT-F-079..CONT-F-089.
 *
 * <p>Runs the type's own legacy SQL (legacy lines :99, :108, :116) and turns each data row into the
 * TabDatosMovimiento entries of legacy lines 121-149.
 */
@Service("centralizaPrestamosEmprestitos")
public class CentralizaPrestamosEmprestitos extends CentralizadorSql {

    private static final String SELECT_MOVIMIENTOS = "cl/bcs/contabilidad/sql/CentralizaPrestamosEmprestitos_SelectMovimientos.sql";
    private static final String SELECT_EVENTOS = "cl/bcs/contabilidad/sql/CentralizaPrestamosEmprestitos_SelectEventos.sql";
    private static final String SELECT_DATOS_MOVIMIENTO = "cl/bcs/contabilidad/sql/CentralizaPrestamosEmprestitos_SelectDatosMovimiento.sql";

    /** Legacy CentralizaPrestamosEmprestitos.java:121-149, one entry per legacy add(...), in order. */
    private static final List<Campo> CAMPOS = List.of(
            texto("DocumentoIdentidad"),
            texto("NombreCliente"),
            texto("Portafolio"),
            texto("Moneda"),
            texto("EsCuentaPropia"),
            texto("EsVentaCorta"),
            texto("Producto"),
            texto("ClaseInstrumento"),
            texto("TipoInstrumento"),
            texto("Nemo"),
            texto("Boveda"),
            texto("IdCartera"),
            texto("Emisor"),
            texto("TipoReajuste"),
            texto("CodigoIFRS"),
            texto("TipoCartera"),
            texto("ClaseEmisor"),
            texto("Agente"),
            texto("NombreAgente"),
            texto("SucursalAgente"),
            texto("NombreSucursalAgente"),
            texto("CentroCostoAgente"),
            texto("Relacion"),
            texto("Bolsa"),
            fecha("FechaLiquidacion"),
            fecha("FechaOperacion"),
            texto("IdConcepto"),
            texto("ClienteBancario"));

    public CentralizaPrestamosEmprestitos(PlataformaPort plataforma, ContabilidadRepository repository) {
        super(plataforma, repository);
    }

    @Override
    protected Tablas calcular(Contexto ctx) {
        return new Tablas()
                .movimientos(movimientos(SELECT_MOVIMIENTOS, parametros(
                        "IdEmpresa", ctx.idEmpresa(), "FechaContable", ctx.fechaContable(),
                        "IdTipoCentralizacion", ctx.idTipo())))
                .eventos(eventos(SELECT_EVENTOS, parametros(
                        "IdEmpresa", ctx.idEmpresa(), "FechaContable", ctx.fechaContable(),
                        "FechaAnterior", ctx.diaAnterior(), "IdTipoCentralizacion", ctx.idTipo())))
                .datos(DatosMovimiento.de(ctx.idTipo(), filas(SELECT_DATOS_MOVIMIENTO, parametros(
                        "IdEmpresa", ctx.idEmpresa(), "FechaContable", ctx.fechaContable(),
                        "LocSistema", ctx.localizacion())), CAMPOS));
    }
}
