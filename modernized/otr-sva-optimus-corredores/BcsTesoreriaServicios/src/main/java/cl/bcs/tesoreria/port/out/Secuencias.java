package cl.bcs.tesoreria.port.out;

/** INTENDED API: legacy {@code Utils.getSecuencia(con, name)} / HANA {@code "seq".NEXTVAL}: next value of the named database sequence (e.g. "BSCL.Tesoreria.BD.Secuencias::IdMandatoPAC"). */
public interface Secuencias {
    long siguiente(String nombre);
}
