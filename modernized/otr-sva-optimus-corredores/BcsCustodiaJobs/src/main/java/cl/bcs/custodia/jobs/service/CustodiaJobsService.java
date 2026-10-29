package cl.bcs.custodia.jobs.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

/**
 * Pure-logic service for Custodia Jobs.
 * All business-logic methods are public static for testability without Spring context.
 *
 * Legacy sources:
 *   - ValidarHorarioEjercicioBloqueoOpciones.java (lines 98-103)
 *   - GenerarArchivoInformeMayorMenorValor.java   (lines 43-52, 125-133)
 */
@Service
public class CustodiaJobsService {

    // -----------------------------------------------------------------------
    // Constants  (GenerarArchivoInformeMayorMenorValor.java lines 43-51)
    // -----------------------------------------------------------------------

    public static final int DECIMALES_TASAS              = 6;
    public static final int DECIMALES_PRECIOS            = 2;
    public static final int DECIMALES_CANTIDAD_UF        = 4;
    public static final int DECIMALES_CANTIDAD_USD_EUR   = 2;
    public static final int DECIMALES_CANTIDAD_CLP       = 0;
    public static final int DECIMALES_PORCENTAJES        = 2;

    /**
     * Diferenciadores de pactos.
     * Legacy: GenerarArchivoInformeMayorMenorValor.java line 52
     *   private static final List<String> DIFERENCIADORES_PACTOS = Arrays.asList("PP", "PC", "PD");
     */
    public static final List<String> DIFERENCIADORES_PACTOS = List.of("PP", "PC", "PD");

    // -----------------------------------------------------------------------
    // Pure-logic methods
    // -----------------------------------------------------------------------

    /**
     * Returns true if fechaLocal is strictly AFTER horaIni AND strictly BEFORE horaTer.
     *
     * Legacy: ValidarHorarioEjercicioBloqueoOpciones.java lines 98-102
     *   Timestamp fechaLocalIni = Fecha.toDateTime(fechaLocal.substring(0, 10)+" "+horaIniStr);
     *   Timestamp fechaLocalTer = Fecha.toDateTime(fechaLocal.substring(0, 10)+" "+horaTerStr);
     *   if (fechaLocalTS.after(fechaLocalIni) && fechaLocalTS.before(fechaLocalTer)) { ...
     *
     * @param fechaLocal  current datetime (local timezone)
     * @param horaIni     start-of-window time
     * @param horaTer     end-of-window time
     * @return true when fechaLocal is strictly inside [horaIni, horaTer)
     */
    public static boolean esEnHorario(LocalDateTime fechaLocal, LocalTime horaIni, LocalTime horaTer) {
        LocalTime hora = fechaLocal.toLocalTime();
        return hora.isAfter(horaIni) && hora.isBefore(horaTer);
    }

    /**
     * Returns the number of decimal places to use for a given currency.
     *
     * Legacy: GenerarArchivoInformeMayorMenorValor.java lines 125-133
     *   private Integer obtenerDecimalesCantidad(String IdMoneda) {
     *     if (IdMoneda.equals("UF"))                            return DECIMALES_CANTIDAD_UF;      // 4
     *     else if (IdMoneda.equals("USD") || IdMoneda.equals("EUR")) return DECIMALES_CANTIDAD_USD_EUR; // 2
     *     else                                                  return DECIMALES_CANTIDAD_CLP;     // 0
     *   }
     *
     * @param idMoneda currency code (e.g. "UF", "USD", "EUR", "CLP")
     * @return decimal places: UF=4, USD/EUR=2, else=0
     */
    public static int obtenerDecimalesCantidad(String idMoneda) {
        if ("UF".equals(idMoneda)) {
            return DECIMALES_CANTIDAD_UF;
        } else if ("USD".equals(idMoneda) || "EUR".equals(idMoneda)) {
            return DECIMALES_CANTIDAD_USD_EUR;
        } else {
            return DECIMALES_CANTIDAD_CLP;
        }
    }

    /**
     * Returns true if the given tipo is a known "diferenciador de pacto".
     *
     * Legacy: GenerarArchivoInformeMayorMenorValor.java line 52
     *   private static final List<String> DIFERENCIADORES_PACTOS = Arrays.asList("PP", "PC", "PD");
     *   (used implicitly throughout the report generation logic)
     *
     * @param tipoPacto type code (e.g. "PP", "PC", "PD", "SF")
     * @return true only for "PP", "PC", "PD"
     */
    public static boolean esDiferenciadorPacto(String tipoPacto) {
        if (tipoPacto == null || tipoPacto.isEmpty()) {
            return false;
        }
        return DIFERENCIADORES_PACTOS.contains(tipoPacto);
    }

    /**
     * Determines the scheduling event based on current horario status and whether
     * a previous notification was recorded.
     *
     * Legacy: ValidarHorarioEjercicioBloqueoOpciones.java lines 138-149
     *   if (resConsulta.size() < 1 && horario.Estado.equalsIgnoreCase("SI"))  -> INICIO
     *   if (res.Evento == INICIO && horario.Estado == "NO" && size <= 1)       -> TERMINO
     *
     * @param estadoHorario          "SI" if the schedule window is active, "NO" otherwise
     * @param tieneConsultaAnterior  true if a prior INICIO notification was recorded
     * @return "INICIO", "TERMINO", or null (no action needed)
     */
    public static String determinarEventoHorario(String estadoHorario, boolean tieneConsultaAnterior) {
        if ("SI".equalsIgnoreCase(estadoHorario) && !tieneConsultaAnterior) {
            return "INICIO";
        }
        if ("NO".equalsIgnoreCase(estadoHorario) && tieneConsultaAnterior) {
            return "TERMINO";
        }
        return null;
    }
}
