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
 * Centralization type CentralizaOrdenDirecta (legacy CentralizaOrdenDirecta.java). Characterization: CONT-F-055..CONT-F-065.
 *
 * <p>Runs the type's own legacy SQL (legacy lines :69, :77, :83) and turns each data row into the
 * TabDatosMovimiento entries of legacy lines 88-99.
 */
@Service("centralizaOrdenDirecta")
public class CentralizaOrdenDirecta extends CentralizadorSql {

    private static final String SELECT_MOVIMIENTOS = "cl/bcs/contabilidad/sql/CentralizaOrdenDirecta_SelectMovimientos.sql";
    private static final String SELECT_EVENTOS = "cl/bcs/contabilidad/sql/CentralizaOrdenDirecta_SelectEventos.sql";
    private static final String SELECT_DATOS_MOVIMIENTO = "cl/bcs/contabilidad/sql/CentralizaOrdenDirecta_SelectDatosMovimiento.sql";

    /** Legacy CentralizaOrdenDirecta.java:88-99, one entry per legacy add(...), in order. */
    private static final List<Campo> CAMPOS = List.of(
            texto("TipoDocumento"),
            texto("FolioTransaccion"),
            texto("CompraVenta"),
            texto("CodigoBolsa"),
            texto("TipoInstrumento"),
            texto("EsCuentaPropia"),
            texto("FamiliaInstrumento"),
            fecha("FechaLiquidacion"),
            texto("Nemo"),
            texto("Moneda"),
            texto("Producto"),
            texto("Estado"));

    public CentralizaOrdenDirecta(PlataformaPort plataforma, ContabilidadRepository repository) {
        super(plataforma, repository);
    }

    @Override
    protected Tablas calcular(Contexto ctx) {
        return new Tablas()
                .movimientos(movimientos(SELECT_MOVIMIENTOS, parametros(
                        "FechaContable", ctx.fechaContable(), "IdEmpresa", ctx.idEmpresa(),
                        "IdTipoCentralizacion", ctx.idTipo())))
                .eventos(eventos(SELECT_EVENTOS, parametros(
                        "FechaContable", ctx.fechaContable(), "IdEmpresa", ctx.idEmpresa(),
                        "IdTipoCentralizacion", ctx.idTipo(), "MonedaSistema", ctx.monedaSistema())))
                .datos(DatosMovimiento.de(ctx.idTipo(), filas(SELECT_DATOS_MOVIMIENTO, parametros(
                        "FechaContable", ctx.fechaContable(), "IdEmpresa", ctx.idEmpresa())), CAMPOS));
    }
}
