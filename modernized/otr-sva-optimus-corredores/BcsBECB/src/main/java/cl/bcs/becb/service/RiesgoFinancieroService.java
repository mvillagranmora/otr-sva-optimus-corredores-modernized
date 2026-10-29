package cl.bcs.becb.service;

import cl.bcs.becb.domain.DataRiesgoFinanciero;
import cl.bcs.becb.domain.DataRiesgoFinancieroAgrupado;
import cl.bcs.becb.domain.SalidaGenerarInforme;
import cl.bcs.becb.domain.TipoInforme;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class RiesgoFinancieroService {

    // B-BECB-01: tabla CUS when same date, HIS otherwise
    public static String determinarTabla(LocalDate fechaSistema, LocalDate fechaInforme) {
        return fechaSistema.equals(fechaInforme) ? "CUS" : "HIS";
    }

    // B-BECB-02: factory lookup by report code (case-insensitive)
    public static TipoInforme resolveCodigoInforme(String codigo) {
        if (codigo == null || codigo.isEmpty()) return null;
        return switch (codigo.toUpperCase()) {
            case "RIFI" -> TipoInforme.RIFI;
            case "DIFI" -> TipoInforme.DIFI;
            case "ALL"  -> TipoInforme.ALL;
            default     -> null;
        };
    }

    // B-BECB-03: row grouping for RIFI and ALL sheets
    // First group always has volatilidad=ZERO and var=ZERO (legacy quirk: seed is always zero-initialized)
    public static List<DataRiesgoFinancieroAgrupado> agruparFilasRifi(List<DataRiesgoFinanciero> filas) {
        if (filas.isEmpty()) return List.of();

        List<DataRiesgoFinancieroAgrupado> resultado = new ArrayList<>();
        DataRiesgoFinancieroAgrupado seed = new DataRiesgoFinancieroAgrupado();
        seed.instrumentoEmisor = filas.get(0).instrumentoEmisor();
        seed.rango             = filas.get(0).rango();
        seed.volatilidad       = BigDecimal.ZERO;
        seed.duracion          = filas.get(0).duracion();
        seed.valorPresente     = BigDecimal.ZERO;
        seed.var               = BigDecimal.ZERO;

        for (DataRiesgoFinanciero fila : filas) {
            if (fila.rango().compareTo(seed.rango) == 0
                    && fila.instrumentoEmisor().equalsIgnoreCase(seed.instrumentoEmisor)) {
                if (seed.duracion.compareTo(fila.duracion()) < 0) seed.duracion = fila.duracion();
                seed.valorPresente = seed.valorPresente.add(fila.valorPresente());
            } else {
                resultado.add(seed);
                seed = new DataRiesgoFinancieroAgrupado();
                seed.instrumentoEmisor = fila.instrumentoEmisor();
                seed.rango             = fila.rango();
                seed.volatilidad       = fila.volatilidad();
                seed.duracion          = fila.duracion();
                seed.valorPresente     = fila.valorPresente();
                seed.var               = fila.var();
            }
        }
        resultado.add(seed);
        return resultado;
    }

    // B-BECB-04: empty list → no report generated
    public static boolean tieneData(List<?> filas) {
        return !filas.isEmpty();
    }

    // B-BECB-05: file naming
    public static String generarNombreArchivo(String codigoInforme, String tabla, String fechaConTimestamp) {
        return "Informe_" + codigoInforme + "_" + tabla + "_" + fechaConTimestamp + ".xls";
    }

    public static String generarNombreArchivoLog(String codigoInforme, String tabla, String fechaConTimestamp) {
        return "Informe_" + codigoInforme + "_" + tabla + "_" + fechaConTimestamp + ".log";
    }

    // B-BECB-06: error output structure
    public static SalidaGenerarInforme salidaError(String comentario) {
        SalidaGenerarInforme out = new SalidaGenerarInforme();
        out.erroresContador = 1L;
        out.comentario = comentario;
        return out;
    }
}
