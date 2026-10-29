package cl.bcs.fondomutuo.fechas;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import cl.bcs.fondomutuo.cierre.FechaLegacy;
import cl.bcs.fondomutuo.fechas.ObtenerDiaAnterior.OutObtenerDiaAnterior.FechasOut;
import cl.bcs.fondomutuo.port.out.kit.CalendarioPort;

/**
 * Port of Plataforma {@code ObtenerDiaAnterior.logica}, bug for bug: subtracts days from a date skipping the non-working days of ONE country
 * ({@code idPais}) when {@code diaHabil="S"}; with {@code diaHabil="N"} and {@code soloCaeDiaHabil="S"} the result moves FORWARD to the next working day
 * (as the legacy). {@code excluirConsultarDiaFeriado} ignores the FERIADO rows (only the non-working weekdays count). Holidays are read through
 * {@link CalendarioPort}; the comparison is by ISO text as the legacy ({@code Date.toString}).
 */
public class ObtenerDiaAnterior {

    private final CalendarioPort calendario;

    public ObtenerDiaAnterior(CalendarioPort calendario) {
        this.calendario = calendario;
    }

    public static class InObtenerDiaAnterior {
        public Long idPais;
        public String diaHabil;
        public String soloCaeDiaHabil;
        public boolean excluirConsultarDiaFeriado = false;
        public List<FechasIn> fechas;

        public InObtenerDiaAnterior() {
            fechas = new ArrayList<FechasIn>();
            soloCaeDiaHabil = "N";
        }

        public static class FechasIn {
            public String fechaInicial;
            public Long numeroDias;

            public FechasIn() {
            }

            public FechasIn(String fechaInicial, Long numeroDias) {
                this.fechaInicial = fechaInicial;
                this.numeroDias = numeroDias;
            }
        }
    }

    public static class OutObtenerDiaAnterior {
        public Long erroresContador;
        public List<FechasOut> fechas;

        public OutObtenerDiaAnterior() {
            fechas = new ArrayList<FechasOut>();
        }

        public static class FechasOut {
            public String fechaInicial;
            public Long numeroDiasInicial;
            public Long numeroDias;
            public String fechaCalculada;
        }
    }

    public OutObtenerDiaAnterior logica(InObtenerDiaAnterior entrada) throws Exception {
        OutObtenerDiaAnterior salida = new OutObtenerDiaAnterior();
        salida.erroresContador = Long.valueOf(0);
        List<Long> pais = new ArrayList<>();
        pais.add(entrada.idPais);
        List<DiaFeriadoCalendario> listaDiaFeriado = DiaFeriadoCalendario.desde(calendario.feriados(pais));
        for (InObtenerDiaAnterior.FechasIn item : entrada.fechas) {
            OutObtenerDiaAnterior.FechasOut temp = new OutObtenerDiaAnterior.FechasOut();
            temp.fechaInicial = item.fechaInicial;
            temp.numeroDiasInicial = item.numeroDias;
            temp.numeroDias = item.numeroDias;
            temp.fechaCalculada = "0001-01-01";
            boolean esinhabil = true;
            boolean buscar = true;
            int diasSumados = 0;
            int diasSumadosReal = 0;
            if (item.numeroDias == null || item.numeroDias < 1)
                buscar = false;
            LocalDate fPivot = FechaLegacy.aFecha(temp.fechaInicial);
            while (buscar) {
                fPivot = fPivot.minusDays(1);
                diasSumados++;
                diasSumadosReal++;
                buscar = false;

                if (entrada.diaHabil.equals("S")) {
                    esinhabil = true;
                    while (esinhabil) {
                        esinhabil = false;
                        int day = FechaLegacy.diaOptimus(fPivot);
                        if (esDiaNoHabil(entrada, listaDiaFeriado, fPivot, day)) {
                            esinhabil = true;
                        }
                        if (esinhabil) {
                            fPivot = fPivot.minusDays(1);
                            diasSumadosReal++;
                        }
                    }
                }
                if (diasSumados == item.numeroDias)
                    break;
                else
                    buscar = true;
            }
            temp.fechaCalculada = FechaLegacy.aTexto(fPivot);
            temp.numeroDias = (long) diasSumadosReal;
            salida.fechas.add(temp);
        }

        if (entrada.diaHabil.equalsIgnoreCase("N") && entrada.soloCaeDiaHabil.equalsIgnoreCase("S")) {

            for (FechasOut out : salida.fechas) {

                boolean esinhabil = true;
                LocalDate fPivot = FechaLegacy.aFecha(out.fechaCalculada);

                while (esinhabil) {
                    esinhabil = false;

                    int day = FechaLegacy.diaOptimus(fPivot);
                    if (esDiaNoHabil(entrada, listaDiaFeriado, fPivot, day)) {
                        esinhabil = true;
                    }

                    if (esinhabil) {
                        fPivot = fPivot.plusDays(1);
                        out.numeroDias++;
                    }
                }

                out.fechaCalculada = FechaLegacy.aTexto(fPivot);
            }
        }
        return salida;
    }

    /** The legacy loop over the holiday rows: the first row that makes the day non-working wins; with the FERIADO rows excluded only INHABIL counts. */
    private static boolean esDiaNoHabil(InObtenerDiaAnterior entrada, List<DiaFeriadoCalendario> listaDiaFeriado, LocalDate fPivot, int day) {
        for (DiaFeriadoCalendario fer : listaDiaFeriado) {
            if (entrada.excluirConsultarDiaFeriado == false) {
                if ((fer.tipo.equalsIgnoreCase("FERIADO") && fPivot.toString().equalsIgnoreCase(fer.fecha.toString()))
                        || (fer.tipo.equalsIgnoreCase("INHABIL") && day == fer.dia)) {
                    return true;
                }
            } else {
                if (fer.tipo.equalsIgnoreCase("INHABIL") && day == fer.dia) {
                    return true;
                }
            }
        }
        return false;
    }
}
