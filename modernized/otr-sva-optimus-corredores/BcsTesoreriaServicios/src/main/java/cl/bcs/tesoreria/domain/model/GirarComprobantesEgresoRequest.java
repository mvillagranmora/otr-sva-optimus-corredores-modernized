package cl.bcs.tesoreria.domain.model;

import java.util.List;

public record GirarComprobantesEgresoRequest(
        Long idEmpresa,
        Long idUsuario,
        String idModulo,
        String simulaOperacion,
        String usaReglasNegocio,
        String fechaMovimiento,
        List<ComprobanteRef> comprobantesAGirar
) {}
