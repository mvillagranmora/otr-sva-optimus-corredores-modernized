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
import static cl.bcs.contabilidad.domain.service.soporte.ValorRazonable.Columnas.ANTERIORES;

/**
 * Centralization type CentralizaCarterasForwardValorizacion (legacy CentralizaCarterasForwardValorizacion.java:86-210). Characterization: CONT-E-079..CONT-E-097.
 *
 * <p>Legacy flow: when FechaContable is not PLA.FechaSistema every query switches to its *His variant (lines 118-152); each amount
 * row yields today's fair-value events and the previous day's from its *Anterior columns (lines 154-173).
 */
@Service("centralizaCarterasForwardValorizacion")
public class CentralizaCarterasForwardValorizacion extends CentralizadorSql {

    private static final String SELECT_MOVIMIENTOS = "cl/bcs/contabilidad/sql/CentralizaCarterasForwardValorizacion_SelectMovimientos.sql";
    private static final String SELECT_MOVIMIENTOS_HIS = "cl/bcs/contabilidad/sql/CentralizaCarterasForwardValorizacion_SelectMovimientosHis.sql";
    private static final String SELECT_EVENTOS = "cl/bcs/contabilidad/sql/CentralizaCarterasForwardValorizacion_SelectEventos.sql";
    private static final String SELECT_EVENTOS_HIS = "cl/bcs/contabilidad/sql/CentralizaCarterasForwardValorizacion_SelectEventosHis.sql";
    private static final String SELECT_MONTO_EVENTOS = "cl/bcs/contabilidad/sql/CentralizaCarterasForwardValorizacion_SelectMontoEventos.sql";
    private static final String SELECT_MONTO_EVENTOS_HIS = "cl/bcs/contabilidad/sql/CentralizaCarterasForwardValorizacion_SelectMontoEventosHis.sql";
    private static final String SELECT_DATOS_MOVIMIENTO = "cl/bcs/contabilidad/sql/CentralizaCarterasForwardValorizacion_SelectDatosMovimiento.sql";
    private static final String SELECT_DATOS_MOVIMIENTO_HIS = "cl/bcs/contabilidad/sql/CentralizaCarterasForwardValorizacion_SelectDatosMovimientoHis.sql";

    /** Legacy CentralizaCarterasForwardValorizacion.java:176-206, one entry per legacy add(...), in order. */
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
            texto("CompraVenta"),
            fechaOVacio("FechaLiquidacion"),
            fechaOVacio("FechaOperacion"),
            texto("CostingCode", "CentroCostoAgente"),
            texto("CostingCode2", "Producto"),
            texto("CostingCode3", "Relacion"),
            texto("CostingCode4", "Bolsa"),
            vacio("CostingCode5"));

    public CentralizaCarterasForwardValorizacion(PlataformaPort plataforma, ContabilidadRepository repository) {
        super(plataforma, repository);
    }

    @Override
    protected Tablas calcular(Contexto ctx) {
        boolean actual = ctx.esFechaSistema();
        Tablas t = new Tablas()
                .movimientos(movimientos(actual ? SELECT_MOVIMIENTOS : SELECT_MOVIMIENTOS_HIS, parametros(
                        "IdEmpresa", ctx.idEmpresa(), "IdTipoCentralizacion", ctx.idTipo(),
                        "FechaContable", ctx.fechaContable())))
                .eventos(eventos(actual ? SELECT_EVENTOS : SELECT_EVENTOS_HIS, parametros(
                        "IdEmpresa", ctx.idEmpresa(), "IdTipoCentralizacion", ctx.idTipo(),
                        "FechaContable", ctx.fechaContable(), "IdMonedaSistema", ctx.monedaSistema())));
        List<Fila> montos = filas(actual ? SELECT_MONTO_EVENTOS : SELECT_MONTO_EVENTOS_HIS, parametros(
                "IdEmpresa", ctx.idEmpresa(), "FechaContable", ctx.fechaContable(),
                "FechaAnteriorSistema", ctx.diaAnterior()));
        List<Fila> datos = filas(actual ? SELECT_DATOS_MOVIMIENTO : SELECT_DATOS_MOVIMIENTO_HIS, parametros(
                "IdEmpresa", ctx.idEmpresa(), "FechaContable", ctx.fechaContable()));
        String moneda = ctx.monedaSistema();
        // each amount row carries today's values and the previous day's (Razonable*Anterior)
        for (Fila f : montos) {
            t.eventos(ValorRazonable.eventos(ctx.idTipo(), f, moneda, false, ACTUALES));
            t.eventos(ValorRazonable.eventos(ctx.idTipo(), f, moneda, true, ANTERIORES));
        }
        return t.datos(DatosMovimiento.de(ctx.idTipo(), datos, CAMPOS));
    }
}
