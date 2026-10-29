package cl.bcs.apv.domain.port.out.contratos;

import java.util.List;

/** Queries and session temporary table of the validation and assignment of contracts to late collections (legacy ValidarYAsignarContratosARecaudaciones SQL). */
public interface RecaudacionRezagoRepositorio {

    /** Legacy {@code pojo.DatosRecaudaciones}: mutable, legacy defaults, public fields = column labels and SQL parameter names. */
    class DatosRecaudaciones {
        public Long IdRecaudacionCliente = 0L;
        public String RutCliente = "";
        public Long MontoRecaudacion = 0L;
        public String TipoRegimen = "";
        public String Regimen = "";
        public String RutEmpleador = "";
        public Long IdContratoSAAPV = 0L;
        public Long MontoPesos = 0L;
        public Long FolioRecaudacion = 0L;
        public String FolioContratoSAAPV = "";
        public String CodigoMedioRecaudacion = "";
    }

    List<DatosRecaudaciones> recaudacionesEnRezago(String fechaDesde, String fechaHasta);

    void crearTablaTemporal();

    /** One DELETE per row (a batch, as the legacy). */
    void borrarTablaTemporal(List<DatosRecaudaciones> filas);

    void insertarATablaTemporal(List<DatosRecaudaciones> filas);

    /** One DROP per row (a batch, as the legacy: with two or more rows the second DROP fails). */
    void dropTablaTemporal(List<DatosRecaudaciones> filas);
}
