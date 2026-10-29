package cl.bcs.apv.domain.port.out;

/** INTENDED API: legacy {@code Utils.getSecuencia(con, name)} / HANA {@code "seq".NEXTVAL}: next value of the named database sequence (e.g. "BSCL.APV.BD.Secuencia::APV.IdCargaMasiva"). */
public interface Secuencias {
    long siguiente(String nombre);
}
