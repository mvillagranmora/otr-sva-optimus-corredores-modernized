package cl.bcs.cuentainversion.domain;

import java.time.LocalDate;
import java.util.List;

public record CuadraturaCierreDiarioEntrada(
        Long idUsuario,
        Long idEmpresa,
        String idModulo,
        LocalDate fechaInicio,
        List<String> idsProceso,
        String tipoLlamado
) {}
