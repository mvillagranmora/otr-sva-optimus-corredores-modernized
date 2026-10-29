package cl.bcs.fondomutuo.fechas;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import cl.bcs.fondomutuo.cierre.FechaLegacy;
import cl.bcs.fondomutuo.fechas.ObtenerDiaHabilMultiplesPaises.SalidaObtenerDiaHabilMultiplesPaises.FechasOut;
import cl.bcs.fondomutuo.port.out.kit.CalendarioPort;

/**
 * Port of Plataforma {@code ObtenerDiaHabilMultiplesPaises.logica}, bug for bug: like {@code ObtenerDiaSiguiente} but a day is non-working when it is a
 * holiday / non-working weekday in ANY of the countries listed for each date. The country list of the query is the distinct union of the lists of all
 * the dates. With {@code numeroDias} null or &lt;= 0 the date is returned unchanged (even on a holiday) unless {@code p_diaHabil="N"} and
 * {@code p_soloCaeDiaHabil="S"} (which only moves forward, there is no "previous" variant here).
 */
public class ObtenerDiaHabilMultiplesPaises {

    private final CalendarioPort calendario;

    public ObtenerDiaHabilMultiplesPaises(CalendarioPort calendario) {
        this.calendario = calendario;
    }

    public static class EntradaObtenerDiaHabilMultiplesPaises {
        public String p_diaHabil;
        public String p_soloCaeDiaHabil;
        public List<FechasIn> p_fechas;

        public EntradaObtenerDiaHabilMultiplesPaises() {
            p_fechas = new ArrayList<FechasIn>();
        }

        public static class FechasIn {
            public Long idInterno;
            public String fechaInicial;
            public Long numeroDias;
            public List<Long> paises;

            public FechasIn() {
                paises = new ArrayList<Long>();
            }

            public FechasIn(Long idInterno, String fechaInicial, Long numeroDias, List<Long> idPaises) {
                this.idInterno = idInterno;
                this.fechaInicial = fechaInicial;
                this.numeroDias = numeroDias;
                this.paises = idPaises;
            }
        }
    }

    public static class SalidaObtenerDiaHabilMultiplesPaises {
        public Long P_OUT_ERRORESCONTADOR;
        public List<FechasOut> p_out_fechas;

        public SalidaObtenerDiaHabilMultiplesPaises() {
            p_out_fechas = new ArrayList<FechasOut>();
        }

        public static class FechasOut {
            public Long idInterno;
            public String fechaInicial;
            public Long numeroDiasInicial;
            public Long numeroDias;
            public String fechaCalculada;
            public List<Long> idPaises;
        }
    }

    public SalidaObtenerDiaHabilMultiplesPaises logica(EntradaObtenerDiaHabilMultiplesPaises entrada) throws Exception {
        SalidaObtenerDiaHabilMultiplesPaises salida = new SalidaObtenerDiaHabilMultiplesPaises();
        salida.P_OUT_ERRORESCONTADOR = 0L;

        entrada.p_soloCaeDiaHabil = entrada.p_diaHabil.equalsIgnoreCase("S") ? "S" : entrada.p_soloCaeDiaHabil == null ? "N" : entrada.p_soloCaeDiaHabil;

        List<Long> paises = new ArrayList<>();
        for (EntradaObtenerDiaHabilMultiplesPaises.FechasIn f : entrada.p_fechas) {
            for (Long pais : f.paises) {
                paises.add(pais);
            }
        }
        paises = paises.stream().distinct().collect(Collectors.toList());
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

        for (EntradaObtenerDiaHabilMultiplesPaises.FechasIn item : entrada.p_fechas) {
            SalidaObtenerDiaHabilMultiplesPaises.FechasOut temp = new SalidaObtenerDiaHabilMultiplesPaises.FechasOut();
            temp.fechaInicial = item.fechaInicial;
            temp.numeroDiasInicial = item.numeroDias;
            temp.numeroDias = item.numeroDias;
            temp.fechaCalculada = item.fechaInicial;

            boolean buscar = item.numeroDias == null || item.numeroDias <= 0 ? false : true;
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
                    while (esinhabil) {
                        esinhabil = false;
                        int day = FechaLegacy.diaOptimus(fPivot);
                        if (esInhabil(hashDiaFeriado, hashDiaInhabil, FechaLegacy.aTexto(fPivot), day, item.paises)) {
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
            temp.idPaises = item.paises;
            temp.idInterno = item.idInterno;
            salida.p_out_fechas.add(temp);
        }

        if (entrada.p_diaHabil.equalsIgnoreCase("N") && entrada.p_soloCaeDiaHabil.equalsIgnoreCase("S")) {
            for (FechasOut out : salida.p_out_fechas) {
                boolean esinhabil = true;
                LocalDate fPivot = FechaLegacy.aFecha(out.fechaCalculada);

                while (esinhabil) {
                    esinhabil = false;
                    int day = FechaLegacy.diaOptimus(fPivot);
                    if (esInhabil(hashDiaFeriado, hashDiaInhabil, out.fechaCalculada, day, out.idPaises)) {
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

    private Boolean esInhabil(HashMap<String, DiaFeriadoCalendario> hashDiaFeriado, HashMap<String, DiaFeriadoCalendario> hashDiaInhabil, String fecha, int dia,
            List<Long> idPaises) {
        Boolean esInhabil = false;
        for (Long idPais : idPaises) {
            esInhabil = hashDiaFeriado.get("FERIADO_" + fecha + "_" + idPais) != null || hashDiaInhabil.get("INHABIL_" + dia + "_" + idPais) != null;
            if (esInhabil) {
                return esInhabil;
            }
        }
        return esInhabil;
    }
}
