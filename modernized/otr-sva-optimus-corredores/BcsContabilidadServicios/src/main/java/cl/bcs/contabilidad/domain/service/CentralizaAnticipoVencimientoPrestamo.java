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
 * Centralization type CentralizaAnticipoVencimientoPrestamo (legacy CentralizaAnticipoVencimientoPrestamo.java). Characterization: CONT-E-020..CONT-E-029.
 *
 * <p>Runs the type's own legacy SQL (legacy lines :67, :76, :84, :92) and turns each data row into the
 * TabDatosMovimiento entries of legacy lines 97-119.
 */
@Service("centralizaAnticipoVencimientoPrestamo")
public class CentralizaAnticipoVencimientoPrestamo extends CentralizadorSql {

    private static final String SELECT_CLASIFICACION_TIPO_INSTRUMENTO = "cl/bcs/contabilidad/sql/CentralizaAnticipoVencimientoPrestamo_SelectClasificacionTipoInstrumento.sql";
    private static final String SELECT_MOVIMIENTOS = "cl/bcs/contabilidad/sql/CentralizaAnticipoVencimientoPrestamo_SelectMovimientos.sql";
    private static final String SELECT_EVENTOS = "cl/bcs/contabilidad/sql/CentralizaAnticipoVencimientoPrestamo_SelectEventos.sql";
    private static final String SELECT_DATOS_MOVIMIENTO = "cl/bcs/contabilidad/sql/CentralizaAnticipoVencimientoPrestamo_SelectDatosMovimiento.sql";

    /** Legacy CentralizaAnticipoVencimientoPrestamo.java:97-119, one entry per legacy add(...), in order. */
    private static final List<Campo> CAMPOS = List.of(
            texto("DocumentoIdentidad"),
            texto("NombreCliente"),
            texto("Portafolio"),
            texto("Moneda"),
            texto("EsRelacionado"),
            texto("EsCuentaPropia"),
            texto("Producto"),
            texto("Nemo"),
            texto("Boveda"),
            texto("IdCartera"),
            texto("Emisor"),
            texto("TipoReajuste"),
            texto("CodigoIFRS"),
            texto("Agente"),
            texto("NombreAgente"),
            texto("SucursalAgente"),
            texto("NombreSucursalAgente"),
            texto("CentroCostoAgente"),
            texto("TipoPrestamo"),
            texto("TipoPrestatario"),
            texto("CondicionLiquidacion"),
            obligatorio("FechaLiquidacion"),
            obligatorio("FechaOperacion"));

    public CentralizaAnticipoVencimientoPrestamo(PlataformaPort plataforma, ContabilidadRepository repository) {
        super(plataforma, repository);
    }

    @Override
    protected Tablas calcular(Contexto ctx) {
        String clasificacion = clasificacionTipoInstrumento(SELECT_CLASIFICACION_TIPO_INSTRUMENTO, ctx);
        return new Tablas()
                .movimientos(movimientos(SELECT_MOVIMIENTOS, parametros(
                        "IdEmpresa", ctx.idEmpresa(), "FechaContable", ctx.fechaContable(),
                        "IdTipoCentralizacion", ctx.idTipo(), "ClasificacionTipoInstrumento", clasificacion)))
                .eventos(eventos(SELECT_EVENTOS, parametros(
                        "IdEmpresa", ctx.idEmpresa(), "FechaContable", ctx.fechaContable(),
                        "IdTipoCentralizacion", ctx.idTipo(), "ClasificacionTipoInstrumento", clasificacion)))
                .datos(DatosMovimiento.de(ctx.idTipo(), filas(SELECT_DATOS_MOVIMIENTO, parametros(
                        "IdEmpresa", ctx.idEmpresa(), "FechaContable", ctx.fechaContable(),
                        "LocSistema", ctx.localizacion(), "ClasificacionTipoInstrumento", clasificacion)), CAMPOS));
    }
}
