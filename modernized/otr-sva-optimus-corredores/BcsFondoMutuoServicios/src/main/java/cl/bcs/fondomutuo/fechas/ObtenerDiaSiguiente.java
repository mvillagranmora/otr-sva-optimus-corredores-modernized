package cl.bcs.fondomutuo.fechas;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cl.bcs.fondomutuo.cierre.FechaLegacy;
import cl.bcs.fondomutuo.fechas.ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn;
import cl.bcs.fondomutuo.fechas.ObtenerDiaSiguiente.OutObtenerDiaSiguiente.FechasOut;
import cl.bcs.fondomutuo.port.out.kit.CalendarioPort;

/**
 * Port of Plataforma {@code ObtenerDiaSiguiente.logica} (the date arithmetic that step 2 left out of the kit), bug for bug: adds days to a date, skipping
 * holidays/non-working weekdays of ONE country per date when {@code p_diaHabil="S"}, and with {@code p_diaHabil="N"} + {@code p_soloCaeDiaHabil="S"}
 * moves the result to the next (or, with {@code p_consideraHabilAnterior="S"}, previous) working day. The holiday table is read through
 * {@link CalendarioPort} (verbatim {@code ConsultarFeriados.sql}); dates are {@link LocalDate} (legacy: {@code java.sql.Date} + {@code Calendar}, same days
 * under UTC). The input is mutated exactly as the legacy did ({@code p_soloCaeDiaHabil}, {@code FechasIn.pais}).
 *
 * <p>Its day-skipping loop is DUPLICATED ON PURPOSE in {@link cl.bcs.fondomutuo.consultas.custodia.ConsultarCustodiaPortafolioDiaSiguiente} (D-F3A-29: CPF needs the
 * {@code java.sql.Date}/{@code Calendar} arithmetic and this class is PROVEN): a fix here must be considered there too. Architecture review F3 W1 (M2): the loops carry a
 * safety stop ({@link #MAX_DIAS_INHABILES_CONSECUTIVOS}) so a calendar with every weekday non-working fails instead of hanging.
 */
public class ObtenerDiaSiguiente {

    /** About 10 years of consecutive non-working days; the legacy loop never ended beyond it, here it throws {@link IllegalStateException}. */
    public static final int MAX_DIAS_INHABILES_CONSECUTIVOS = 3700;

    private final CalendarioPort calendario;

    public ObtenerDiaSiguiente(CalendarioPort calendario) {
        this.calendario = calendario;
    }

    public static class InObtenerDiaSiguiente {
        public Long p_idPais;
        public String p_diaHabil;
        public String p_soloCaeDiaHabil;
        public String p_consideraHabilAnterior;
        public List<FechasIn> p_fechas;

        public InObtenerDiaSiguiente() {
            p_fechas = new ArrayList<FechasIn>();
            p_consideraHabilAnterior = "N";
        }

        public static class FechasIn {
            public String fechaInicial;
            public Long numeroDias;
            public Long pais;
            public Long IdInterno = 0L;

            public FechasIn() {
            }

            public FechasIn(String fechaInicial, Long numeroDias, Long idPais) {
                this.fechaInicial = fechaInicial;
                this.numeroDias = numeroDias;
                this.pais = idPais;
            }
        }
    }

    public static class OutObtenerDiaSiguiente {
        public Long P_OUT_ERRORESCONTADOR;
        public List<FechasOut> p_out_fechas;

        public OutObtenerDiaSiguiente() {
            p_out_fechas = new ArrayList<FechasOut>();
        }

        public static class FechasOut {
            public String fechaInicial;
            public Long numeroDiasInicial;
            public Long numeroDias;
            public String fechaCalculada;
            public Long idPais;
            public Long IdInterno;
        }
    }

    public OutObtenerDiaSiguiente logica(InObtenerDiaSiguiente entrada) throws Exception {
        OutObtenerDiaSiguiente salida = new OutObtenerDiaSiguiente();
        salida.P_OUT_ERRORESCONTADOR = Long.valueOf(0);

        entrada.p_soloCaeDiaHabil = entrada.p_diaHabil.equalsIgnoreCase("S") ? "S" : entrada.p_soloCaeDiaHabil == null ? "N" : entrada.p_soloCaeDiaHabil;

        List<Long> paises = new ArrayList<>();
        HashMap<Long, Object> hashFechas = new HashMap<Long, Object>();
        for (FechasIn f : entrada.p_fechas) {
            f.pais = obtenerPaisConsulta(entrada.p_idPais, f.pais);

            if (!hashFechas.containsKey(f.pais)) {
                paises.add(f.pais);
                hashFechas.put(f.pais, new Object());
            }
        }

        List<DiaFeriadoCalendario> listaDiaFeriado = DiaFeriadoCalendario.desde(calendario.feriados(paises));
        HashMap<String, DiaFeriadoCalendario> hashDiaFeriado = new HashMap<String, DiaFeriadoCalendario>();
        HashMap<String, DiaFeriadoCalendario> hashDiaInhabil = new HashMap<String, DiaFeriadoCalendario>();
        for (DiaFeriadoCalendario diaFeriado : listaDiaFeriado) {
            if ("FERIADO".equals(diaFeriado.tipo) && !hashDiaFeriado.containsKey(diaFeriado.tipo + "_" + diaFeriado.fecha)) {
                hashDiaFeriado.put(diaFeriado.tipo + "_" + diaFeriado.fecha + "_" + diaFeriado.idPais, diaFeriado);
            }
            if ("INHABIL".equals(diaFeriado.tipo) && !hashDiaInhabil.containsKey(diaFeriado.tipo + "_" + diaFeriado.dia)) {
                hashDiaInhabil.put(diaFeriado.tipo + "_" + diaFeriado.dia + "_" + diaFeriado.idPais, diaFeriado);
            }
        }

        for (InObtenerDiaSiguiente.FechasIn item : entrada.p_fechas) {
            Long idPais = obtenerPaisConsulta(entrada.p_idPais, item.pais);
            OutObtenerDiaSiguiente.FechasOut temp = new OutObtenerDiaSiguiente.FechasOut();
            temp.fechaInicial = item.fechaInicial;
            temp.numeroDiasInicial = item.numeroDias;
            temp.numeroDias = item.numeroDias;
            temp.fechaCalculada = item.fechaInicial;
            temp.IdInterno = item.IdInterno;

            boolean buscar = item.numeroDias == null || item.numeroDias < 1 ? false : true;
            int diasSumados = 0;
            int diasSumadosReal = 0;
            boolean esinhabil = true;
            LocalDate fPivot = FechaLegacy.aFecha(temp.fechaInicial);

            while (buscar) {
                fPivot = fPivot.plusDays(1);
                diasSumados++;
                diasSumadosReal++;

                if (entrada.p_diaHabil.equals("S")) {
                    esinhabil = true;
                    int consecutivos = 0;
                    while (esinhabil) {
                        esinhabil = false;
                        int day = FechaLegacy.diaOptimus(fPivot);
                        if (esInhabil(hashDiaFeriado, hashDiaInhabil, FechaLegacy.aTexto(fPivot), day, idPais)) {
                            topar(++consecutivos, idPais);
                            esinhabil = true;
                            fPivot = fPivot.plusDays(1);
                            diasSumadosReal++;
                        }
                    }
                }

                if (diasSumados == item.numeroDias)
                    break;
            }
            temp.fechaCalculada = FechaLegacy.aTexto(fPivot);
            temp.numeroDias = (long) diasSumadosReal;
            temp.idPais = idPais;
            salida.p_out_fechas.add(temp);
        }

        if (entrada.p_diaHabil.equalsIgnoreCase("N") && entrada.p_soloCaeDiaHabil.equalsIgnoreCase("S")) {
            for (FechasOut out : salida.p_out_fechas) {
                boolean esinhabil = true;
                int consecutivos = 0;
                LocalDate fPivot = FechaLegacy.aFecha(out.fechaCalculada);

                while (esinhabil) {
                    esinhabil = false;
                    int day = FechaLegacy.diaOptimus(fPivot);
                    if (esInhabil(hashDiaFeriado, hashDiaInhabil, FechaLegacy.aTexto(fPivot), day, out.idPais) && entrada.p_consideraHabilAnterior.equalsIgnoreCase("S")) {
                        topar(++consecutivos, out.idPais);
                        esinhabil = true;
                        fPivot = fPivot.plusDays(-1);
                        out.numeroDias--;
                    } else if (esInhabil(hashDiaFeriado, hashDiaInhabil, FechaLegacy.aTexto(fPivot), day, out.idPais)) {
                        topar(++consecutivos, out.idPais);
                        esinhabil = true;
                        fPivot = fPivot.plusDays(1);
                        out.numeroDias++;
                    }
                }

                out.fechaCalculada = FechaLegacy.aTexto(fPivot);
            }
        }
        return salida;
    }

    private static void topar(int consecutivos, Long idPais) {
        if (consecutivos > MAX_DIAS_INHABILES_CONSECUTIVOS) {
            throw new IllegalStateException("No working day found after " + MAX_DIAS_INHABILES_CONSECUTIVOS + " consecutive non-working days (country " + idPais + ")");
        }
    }

    private Long obtenerPaisConsulta(Long paisEntrada, Long paisFecha) {
        Long idPais = paisFecha == null && paisEntrada != null ? paisEntrada : paisFecha;
        return idPais;
    }

    private Boolean esInhabil(HashMap<String, DiaFeriadoCalendario> hashDiaFeriado, HashMap<String, DiaFeriadoCalendario> hashDiaInhabil, String fecha, int dia,
            Long idPais) {
        return hashDiaFeriado.get("FERIADO_" + fecha + "_" + idPais) != null || hashDiaInhabil.get("INHABIL_" + dia + "_" + idPais) != null;
    }
}
