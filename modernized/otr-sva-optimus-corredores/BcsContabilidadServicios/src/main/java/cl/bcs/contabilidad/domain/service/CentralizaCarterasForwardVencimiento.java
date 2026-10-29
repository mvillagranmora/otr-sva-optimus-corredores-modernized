package cl.bcs.contabilidad.domain.service;

import cl.bcs.contabilidad.domain.port.out.ContabilidadRepository;
import cl.bcs.contabilidad.domain.port.out.PlataformaPort;
import cl.bcs.contabilidad.domain.service.soporte.Campo;
import cl.bcs.contabilidad.domain.service.soporte.Contexto;
import cl.bcs.contabilidad.domain.service.soporte.DatosMovimiento;
import cl.bcs.contabilidad.domain.service.soporte.Fila;
import cl.bcs.contabilidad.domain.service.soporte.Tablas;
import cl.bcs.contabilidad.domain.service.soporte.ValorRazonable;
import org.springframework.stereotype.Service;

import java.util.List;

import static cl.bcs.contabilidad.domain.service.soporte.Campo.*;
import static cl.bcs.contabilidad.domain.service.soporte.ValorRazonable.Columnas.ACTUALES;

/**
 * Centralization type CentralizaCarterasForwardVencimiento (legacy CentralizaCarterasForwardVencimiento.java:79-188). Characterization: CONT-E-098..CONT-E-110.
 *
 * <p>Legacy flow: same flow as CentralizaCarterasForward: own movements/events, fair-value events of today's and the previous
 * business day's amount rows (lines 131-151), data rows (lines 153-184).
 */
@Service("centralizaCarterasForwardVencimiento")
public class CentralizaCarterasForwardVencimiento extends CentralizadorSql {

    private static final String SELECT_MOVIMIENTOS = "cl/bcs/contabilidad/sql/CentralizaCarterasForwardVencimiento_SelectMovimientos.sql";
    private static final String SELECT_EVENTOS = "cl/bcs/contabilidad/sql/CentralizaCarterasForwardVencimiento_SelectEventos.sql";
    private static final String SELECT_MONTO_EVENTOS = "cl/bcs/contabilidad/sql/CentralizaCarterasForwardVencimiento_SelectMontoEventos.sql";
    private static final String SELECT_MONTO_EVENTOS_HIS = "cl/bcs/contabilidad/sql/CentralizaCarterasForwardVencimiento_SelectMontoEventosHis.sql";
    private static final String SELECT_DATOS_MOVIMIENTO = "cl/bcs/contabilidad/sql/CentralizaCarterasForwardVencimiento_SelectDatosMovimiento.sql";

    /** Legacy CentralizaCarterasForwardVencimiento.java:154-184, one entry per legacy add(...), in order. */
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

    public CentralizaCarterasForwardVencimiento(PlataformaPort plataforma, ContabilidadRepository repository) {
        super(plataforma, repository);
    }

    @Override
    protected Tablas calcular(Contexto ctx) {
        Tablas t = new Tablas()
                .movimientos(movimientos(SELECT_MOVIMIENTOS, parametros(
                        "IdEmpresa", ctx.idEmpresa(), "IdTipoCentralizacion", ctx.idTipo(),
                        "FechaContable", ctx.fechaContable())))
                .eventos(eventos(SELECT_EVENTOS, parametros(
                        "IdEmpresa", ctx.idEmpresa(), "IdTipoCentralizacion", ctx.idTipo(),
                        "FechaContable", ctx.fechaContable())));
        List<Fila> montos = filas(SELECT_MONTO_EVENTOS, parametros(
                "IdEmpresa", ctx.idEmpresa(), "FechaContable", ctx.fechaContable()));
        List<Fila> montosAnteriores = filas(SELECT_MONTO_EVENTOS_HIS, parametros(
                "IdEmpresa", ctx.idEmpresa(), "FechaContable", ctx.fechaContable(),
                "FechaAnteriorSistema", ctx.diaAnterior()));
        List<Fila> datos = filas(SELECT_DATOS_MOVIMIENTO, parametros(
                "IdEmpresa", ctx.idEmpresa(), "FechaContable", ctx.fechaContable()));
        String moneda = ctx.monedaSistema();
        // the previous-day rows carry the same Razonable* columns as today's
        montos.forEach(f -> t.eventos(ValorRazonable.eventos(ctx.idTipo(), f, moneda, false, ACTUALES)));
        montosAnteriores.forEach(f -> t.eventos(ValorRazonable.eventos(ctx.idTipo(), f, moneda, true, ACTUALES)));
        return t.datos(DatosMovimiento.de(ctx.idTipo(), datos, CAMPOS));
    }
}
