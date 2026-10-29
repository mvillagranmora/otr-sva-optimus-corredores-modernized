package cl.bcs.tesoreria.domain.model;

import java.util.List;

public record LiquidarComprobanteRequest(
        Long idEmpresa,
        Long idUsuario,
        String idModulo,
        String simulaOperacion,
        String usaReglasNegocio,
        String fechaMovimiento,
        List<ComprobanteRef> comprobantes,
        List<FormaDePagoRequest> formasDePago
) {}
