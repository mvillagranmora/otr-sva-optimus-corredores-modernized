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
 * Centralization type CentralizaCarterasForwardIngreso (legacy CentralizaCarterasForwardIngreso.java). Characterization: CONT-E-070..CONT-E-078.
 *
 * <p>Runs the type's own legacy SQL (legacy lines :70, :76, :81) and turns each data row into the
 * TabDatosMovimiento entries of legacy lines 84-114.
 */
@Service("centralizaCarterasForwardIngreso")
public class CentralizaCarterasForwardIngreso extends CentralizadorSql {

    private static final String SELECT_MOVIMIENTOS = "cl/bcs/contabilidad/sql/CentralizaCarterasForwardIngreso_SelectMovimientos.sql";
    private static final String SELECT_EVENTOS = "cl/bcs/contabilidad/sql/CentralizaCarterasForwardIngresos_SelectEventos.sql";
    private static final String SELECT_DATOS_MOVIMIENTO = "cl/bcs/contabilidad/sql/CentralizaCarterasForwardIngreso_SelectDatosMovimiento.sql";

    /** Legacy CentralizaCarterasForwardIngreso.java:84-114, one entry per legacy add(...), in order. */
    private static final List<Campo> CAMPOS = List.of(
            texto("DocumentoIdentidad"),
            texto("NombreCliente"),
            texto("PortafolioCliente", "Portafolio"),
            texto("MonedaPrincipal"),
            texto("MonedaSecundaria"),
            valorDe("Modalidad", "ModalidaPago"),
            texto("Producto"),
            texto("ClaseInstrumento"),
            texto("TipoInstrumento"),
            texto("TipoOperacion"),
            texto("Nemo"),
            texto("Agente"),
            texto("NombreAgente"),
            texto("SucursalAgente"),
            texto("NombreSucursalAgente"),
            texto("CentroCostoAgente"),
            texto("Relacion"),
            texto("Bolsa"),
            plano("TasaCambio"),
            texto("TipoPersona"),
            texto("Concepto"),
            fechaOVacio("FechaLiquidacion"),
            fechaOVacio("FechaOperacion"),
            texto("CompraVenta"),
            texto("CostingCode", "CentroCostoAgente"),
            texto("CostingCode2", "Producto"),
            texto("CostingCode3", "Relacion"),
            texto("CostingCode4", "Bolsa"),
            vacio("CostingCode5"));

    public CentralizaCarterasForwardIngreso(PlataformaPort plataforma, ContabilidadRepository repository) {
        super(plataforma, repository);
    }

    @Override
    protected Tablas calcular(Contexto ctx) {
        return new Tablas()
                .movimientos(movimientos(SELECT_MOVIMIENTOS, parametros(
                        "IdEmpresa", ctx.idEmpresa(), "IdTipoCentralizacion", ctx.idTipo(),
                        "FechaContable", ctx.fechaContable())))
                .eventos(eventos(SELECT_EVENTOS, parametros(
                        "IdEmpresa", ctx.idEmpresa(), "IdTipoCentralizacion", ctx.idTipo(),
                        "FechaContable", ctx.fechaContable())))
                .datos(DatosMovimiento.de(ctx.idTipo(), filas(SELECT_DATOS_MOVIMIENTO, parametros(
                        "IdEmpresa", ctx.idEmpresa(), "FechaContable", ctx.fechaContable())), CAMPOS));
    }
}
