package cl.bcs.cuentainversion.reportes;

import cl.bcs.cuentainversion.port.out.ColaboradoresReportes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Client side of the {@code CalcularParidad} collaborator (currency conversion) as the reporting services use it: the request is the
 * JSON form of the legacy {@code CalcularParidadEntrada} (company, work id 0 and one {@link CalculoEntrada} per amount), the answer is
 * read as {@link CalculoSalida} rows correlated by {@code IdInterno}.
 */
public final class ParidadReportes {

    private ParidadReportes() {
    }

    public static class CalculoEntrada {
        public Long IdTrabajo;
        public Long IdInterno;
        public String Fecha;
        public String IdMonedaOrigen;
        public String IdMonedaDestino;
        public BigDecimal ValorCambioLocalModificado = BigDecimal.ZERO;
        public BigDecimal MontoOrigen;
        public String IdUso = "";

        public static CalculoEntrada de(Long idInterno, String fecha, String origen, String destino, BigDecimal monto) {
            CalculoEntrada c = new CalculoEntrada();
            c.IdTrabajo = 0L;
            c.IdInterno = idInterno;
            c.Fecha = fecha;
            c.IdMonedaOrigen = origen;
            c.IdMonedaDestino = destino;
            c.MontoOrigen = monto;
            return c;
        }
    }

    public static class CalculoSalida {
        public Long IdTrabajo;
        public Long IdInterno;
        public String IdMonedaOrigen;
        public String IdMonedaDestino;
        public BigDecimal MontoOrigen;
        public BigDecimal MontoDestino;
        public BigDecimal MontoLocal;
    }

    private static class Entrada {
        Long p_IdEmpresa;
        Long p_IdTrabajo = 0L;
        List<CalculoEntrada> p_CalculosEntrada = new ArrayList<>();
    }

    private static class Salida {
        List<CalculoSalida> p_out_CalculosSalida = new ArrayList<>();
        Long P_OUT_ERRORESCONTADOR = 0L;
    }

    /** One call; the rows are returned only when the service reported no errors, otherwise the list is empty (legacy behaviour). */
    public static List<CalculoSalida> calcular(ColaboradoresReportes colaboradores, Long idEmpresa, List<CalculoEntrada> calculos) {
        Entrada e = new Entrada();
        e.p_IdEmpresa = idEmpresa;
        e.p_CalculosEntrada = calculos;
        Salida s = JsonReportes.leer(colaboradores.calcularParidad(JsonReportes.escribir(e)), Salida.class);
        return s.P_OUT_ERRORESCONTADOR.equals(0L) && !s.p_out_CalculosSalida.isEmpty() ? s.p_out_CalculosSalida : new ArrayList<>();
    }
}
