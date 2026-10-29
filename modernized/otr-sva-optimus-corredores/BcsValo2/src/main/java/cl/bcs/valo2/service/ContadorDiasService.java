package cl.bcs.valo2.service;

import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Set;

@Service
public class ContadorDiasService {

    // Alias sets pinned from legacy ContadorDias.mapCalculadoras
    public static final Set<String> ALIAS_30_360 = Set.of(
            "30/360", "30U/360", "Bond basis", "30/360 US", "US MUNI: 30/360",
            "MUNI30/360", "ISDA30/360", "DC30_360");
    public static final Set<String> ALIAS_30E_360 = Set.of(
            "30E/360", "30/360 ICMA", "30S/360", "Eurobond basis",
            "Eurobond basis (ISDA 2006)", "Special German", "ISMA 30/360", "30E/360 ISDA",
            "Eurobond basis (ISDA 2000)", "German", "German:30/360", "Ger:30/360",
            "ISDA SWAPS:30/360", "ISDA 30E/360", "DC30E_360");
    public static final Set<String> ALIAS_30_365    = Set.of("30/365", "30/365 German", "DC30_365");
    public static final Set<String> ALIAS_ACT_360   = Set.of("Actual/360", "Act/360", "DCAct_360");
    public static final Set<String> ALIAS_ACT_365   = Set.of("Actual/365 Fixed", "Act/365 Fixed", "DCAct_365");
    public static final Set<String> ALIAS_ACT_ACT_ISDA = Set.of(
            "Actual/Actual ISDA", "Act/Act ISDA", "US:WIT Act/Act", "DCAct_Act_ISDA");
    public static final Set<String> ALIAS_NL_365    = Set.of(
            "Actual/365 JGB", "Actual/365 JGB (NL)", "NL/365", "DCNL_365");
    public static final Set<String> ALIAS_BUS252    = Set.of("BUS252", "BUS DAYS252", "BUS/252");

    // --- 30/360 raw formula (no EOM adjustments) ---
    public static int dias30_360(LocalDate d1, LocalDate d2) {
        return 360 * (d2.getYear() - d1.getYear())
                + 30 * (d2.getMonthValue() - d1.getMonthValue())
                + (d2.getDayOfMonth() - d1.getDayOfMonth());
    }

    public static double fraccion30_360(LocalDate d1, LocalDate d2) {
        return dias30_360(d1, d2) / 360.0;
    }

    // --- 30E/360 (ISDA 2006 §4.16(g) — ambos días clampeados a min(day,30)) ---
    public static int dias30E_360(LocalDate d1, LocalDate d2) {
        int dd1 = Math.min(d1.getDayOfMonth(), 30);
        int dd2 = Math.min(d2.getDayOfMonth(), 30);
        return 360 * (d2.getYear() - d1.getYear())
                + 30 * (d2.getMonthValue() - d1.getMonthValue())
                + (dd2 - dd1);
    }

    // --- 30/365 (same raw numerator, different denominator for fraction) ---
    public static int dias30_365(LocalDate d1, LocalDate d2) {
        return 360 * (d2.getYear() - d1.getYear())
                + 30 * (d2.getMonthValue() - d1.getMonthValue())
                + (d2.getDayOfMonth() - d1.getDayOfMonth());
    }

    public static double fraccion30_365(LocalDate d1, LocalDate d2) {
        return dias30_365(d1, d2) / 365.0;
    }

    // --- Actual day count ---
    public static int diasAct(LocalDate d1, LocalDate d2) {
        return (int) ChronoUnit.DAYS.between(d1, d2);
    }

    public static double fraccionAct_360(LocalDate d1, LocalDate d2) {
        return ChronoUnit.DAYS.between(d1, d2) / 360.0;
    }

    public static double fraccionAct_365(LocalDate d1, LocalDate d2) {
        return ChronoUnit.DAYS.between(d1, d2) / 365.0;
    }

    // --- Act/Act ISDA — multi-year: diasRestantes/diasAno1 + (Y2-Y1-1) + diasTranscurridos/diasAno2 ---
    public static double fraccionActAct_ISDA(LocalDate d1, LocalDate d2) {
        if (d1.getYear() == d2.getYear()) {
            // Mismo año: fracción simple sobre el año
            return (d2.getDayOfYear() - d1.getDayOfYear()) / (d1.isLeapYear() ? 366.0 : 365.0);
        }
        int diasRestantes     = d1.lengthOfYear() - d1.getDayOfYear();
        int diasTranscurridos = d2.getDayOfYear() - 1;
        double anoDias1 = d1.isLeapYear() ? 366.0 : 365.0;
        double anoDias2 = d2.isLeapYear() ? 366.0 : 365.0;
        return diasRestantes / anoDias1 + diasTranscurridos / anoDias2 + (d2.getYear() - d1.getYear() - 1);
    }

    // --- NL/365 — right-inclusive for leap day count: (d1, d2] ---
    public static int contarFeb29(LocalDate d1, LocalDate d2) {
        int count = 0;
        LocalDate cur = d1.plusDays(1);
        while (!cur.isAfter(d2)) {
            if (cur.getMonthValue() == 2 && cur.getDayOfMonth() == 29) count++;
            cur = cur.plusDays(1);
        }
        return count;
    }

    public static int diasNL_365(LocalDate d1, LocalDate d2) {
        return diasAct(d1, d2) - contarFeb29(d1, d2);
    }

    public static double fraccionNL_365(LocalDate d1, LocalDate d2) {
        return diasNL_365(d1, d2) / 365.0;
    }

    // --- BUS/252 — [d1, d2) left-inclusive, matching legacy while(left!=right){check left++} ---
    public static int diasHabiles(LocalDate d1, LocalDate d2, Set<LocalDate> feriados) {
        int count = 0;
        LocalDate cur = d1;
        while (cur.isBefore(d2)) {
            int dow = cur.getDayOfWeek().getValue();
            if (dow <= 5 && !feriados.contains(cur)) count++;
            cur = cur.plusDays(1);
        }
        return count;
    }

    public static double fraccionBUS252(LocalDate d1, LocalDate d2, Set<LocalDate> feriados) {
        return diasHabiles(d1, d2, feriados) / 252.0;
    }

    // --- 30/360 EOM — non-standard legacy: adjAnterior=-1 adds +1 to month term ---
    public static int dias30_360_EOM(LocalDate d1, LocalDate d2) {
        int y1 = d1.getYear(),  m1 = d1.getMonthValue(),  dd1 = d1.getDayOfMonth();
        int y2 = d2.getYear(),  m2 = d2.getMonthValue(),  dd2 = d2.getDayOfMonth();
        int adjAnterior = 0, adjPosterior = 0;
        if (dd2 + adjPosterior == 31 && (dd1 + adjAnterior == 30 || dd1 + adjAnterior == 31)) adjPosterior--;
        if (dd1 + adjAnterior == 31) adjAnterior--;
        return 360 * (y2 - y1) + 30 * (m2 - m1 + adjPosterior - adjAnterior) + (dd2 - dd1);
    }

    // --- Dispatcher ---
    public static int contarDias(LocalDate d1, LocalDate d2, String convencion, boolean ajusteEOM,
                                  Set<LocalDate> feriados) {
        if (ajusteEOM && (ALIAS_30_360.contains(convencion) || ALIAS_30E_360.contains(convencion)))
            return dias30_360_EOM(d1, d2);
        if (ALIAS_30_360.contains(convencion))     return dias30_360(d1, d2);
        if (ALIAS_30E_360.contains(convencion))    return dias30E_360(d1, d2);
        if (ALIAS_30_365.contains(convencion))     return dias30_365(d1, d2);
        if (ALIAS_ACT_360.contains(convencion))    return diasAct(d1, d2);
        if (ALIAS_ACT_365.contains(convencion))    return diasAct(d1, d2);
        if (ALIAS_ACT_ACT_ISDA.contains(convencion)) return (int) Math.round(fraccionActAct_ISDA(d1, d2) * 365);
        if (ALIAS_NL_365.contains(convencion))     return diasNL_365(d1, d2);
        if (ALIAS_BUS252.contains(convencion))     return diasHabiles(d1, d2, feriados);
        throw new IllegalArgumentException("Convencion de dias no soportada: " + convencion);
    }
}
