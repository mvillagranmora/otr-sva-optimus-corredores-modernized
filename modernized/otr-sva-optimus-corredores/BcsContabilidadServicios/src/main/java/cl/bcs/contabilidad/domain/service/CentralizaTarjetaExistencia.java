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
 * Centralization type CentralizaTarjetaExistencia (legacy CentralizaTarjetaExistencia.java). Characterization: CONT-F-167..CONT-F-176.
 *
 * <p>Runs the type's own legacy SQL (legacy lines :66, :73, :79) and turns each data row into the
 * TabDatosMovimiento entries of legacy lines 83-105.
 */
@Service("centralizaTarjetaExistencia")
public class CentralizaTarjetaExistencia extends CentralizadorSql {

    private static final String SELECT_MOVIMIENTOS = "cl/bcs/contabilidad/sql/CentralizaTarjetaExistencia_SelectMovimientos.sql";
    private static final String SELECT_EVENTOS = "cl/bcs/contabilidad/sql/CentralizaTarjetaExistencia_SelectEventos.sql";
    private static final String SELECT_DATOS_MOVIMIENTO = "cl/bcs/contabilidad/sql/CentralizaTarjetaExistencia_SelectDatosMovimiento.sql";

    /** Legacy CentralizaTarjetaExistencia.java:83-105, one entry per legacy add(...), in order. */
    private static final List<Campo> CAMPOS = List.of(
            valorDe("IdMovimiento"),
            texto("DocumentoIdentidad"),
            texto("NombreCliente"),
            texto("PortafolioCliente"),
            texto("Moneda"),
            texto("Relacion"),
            texto("ClaseInstrumento"),
            texto("TipoInstrumento"),
            texto("Nemo"),
            vacio("IdCartera"),
            texto("IdEmisor"),
            texto("TipoCartera"),
            texto("ClaseEmisor"),
            texto("Agente"),
            texto("NombreAgente"),
            texto("SucursalAgente"),
            texto("NombreSucursalAgente"),
            texto("CentroCostoAgente"));

    public CentralizaTarjetaExistencia(PlataformaPort plataforma, ContabilidadRepository repository) {
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
                        "IdTipoCentralizacion", ctx.idTipo())))
                .datos(DatosMovimiento.de(ctx.idTipo(), filas(SELECT_DATOS_MOVIMIENTO, parametros(
                        "IdEmpresa", ctx.idEmpresa(), "FechaContable", ctx.fechaContable())), CAMPOS));
    }
}
