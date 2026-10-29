package cl.bcs.valo2.entidades;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

import cl.bcs.valo2.fechas.Fecha;

public class Indicador implements Serializable {

    public static class Clave implements Serializable {
        private static final long serialVersionUID = 1L;

        private final Fecha fecha;
        private final String nombre;

        public Clave(
                final String nombre,
                final Fecha fecha) {
            this.nombre = nombre;
            this.fecha = fecha;
        }

        @Override
        public boolean equals(
                final Object o) {
            if (this == o) {
                return true;
            }

            if (o == null) {
                return false;
            }

            if (getClass() != o.getClass()) {
                return false;
            }

            final Clave clave = (Clave) o;
            return fecha.compareTo(clave.fecha) == 0 && nombre.compareTo(clave.nombre) == 0;
        }

        public Fecha getFecha() {
            return fecha;
        }

        public String getNombre() {
            return nombre;
        }

        @Override
        public int hashCode() {
            return Objects.hash(nombre, fecha);
        }

        @Override
        public String toString() {
            return "Clave [fecha=" + fecha + ", nombre=" + nombre + "]";
        }
    }

    private static final long serialVersionUID = 1L;

    private Clave clave;
    private BigDecimal valor;

    public Clave getClave() {
        return clave;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setClave(
            final Clave clave) {
        this.clave = clave;
    }

    public void setValor(
            final BigDecimal valor) {
        this.valor = valor;
    }

    @Override
    public String toString() {
        return "Indicador [clave=" + clave + ", valor=" + valor + "]";
    }

}
