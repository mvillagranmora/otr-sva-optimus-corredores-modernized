package cl.bcs.normativa.cartolas.entidades;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import cl.bcs.normativa.cartolas.dal.CartolaService;
import cl.bcs.normativa.cartolas.procesos.ContextoCartolas;
import cl.bcs.normativa.dal.RecordNotFoundException;

public class Cartola implements Serializable {

    public static final class Builder {

        public static Cartola newInstance(
                final long idNomina,
                final long idCliente,
                final long idPortafolio) {
            final Cartola cartola = new Cartola();
            cartola.setIdNomina(idNomina);
            cartola.setIdCliente(idCliente);
            cartola.setIdPortafolio(idPortafolio);
            cartola.setNotificar(Opcion.NO);
            cartola.setReprocesar(Opcion.NO);
            return cartola;
        }

        public static Cartola newInstance(
                final ResultSet rs)
                        throws SQLException {
            final Cartola cartola = new Cartola();
            cartola.setId(rs.getLong("IdCartola"));
            cartola.setIdNomina(rs.getLong("IdNomina"));
            cartola.setIdCliente(rs.getLong("IdCliente"));
            cartola.setIdPortafolio(rs.getLong("IdPortafolio"));
            cartola.setReprocesar(rs.getString("Reprocesar"));
            cartola.setNotificar(rs.getString("Notificar"));
            cartola.setEstadoProceso(rs.getString("EstadoProceso"));
            cartola.setMensajeProceso(rs.getString("MensajeProceso"));
            return cartola;
        }

        private Builder() {
        }

    }

    private static final long serialVersionUID = 1L;

    public static Cartola buscarCartolaPorIdPortafolio(
            List<Cartola> cartolas,
            final Long idPortafolio) {
        for (final Cartola cartola : cartolas) {
            if (cartola.getIdPortafolio() == idPortafolio) {
                return cartola;
            }
        }

        throw new RecordNotFoundException(
                "Cartola no encontrada para el portafolio: " + idPortafolio);
    }

    public static Cartola newInstance(
            final long idNomina,
            final long idCliente,
            final long idPortafolio) {
        return Builder.newInstance(idNomina, idCliente, idPortafolio);
    }

    public static Cartola newInstance(
            final Nomina nomina,
            final long idCliente,
            final long idPortafolio) {
        return newInstance(nomina.getId(), idCliente, idPortafolio);
    }

    public static Cartola newInstance(
            final ResultSet rs)
                    throws SQLException {
        return Builder.newInstance(rs);
    }

    private String estadoProceso;
    private long id;
    private long idCliente;
    private long idNomina;
    private long idPortafolio;
    private String mensajeProceso;
    private String notificar;
    private String reprocesar;

    public void abrir(
            final ContextoCartolas contexto) {
        if (EstadoProceso.EN_PROCESO.equals(estadoProceso)) {
            throw new NominaException("La cartola esta en proceso: id=" + this.getId());
        }

        try {
            this.setEstadoProceso(EstadoProceso.EN_PROCESO);
            CartolaService.save(contexto, this);
        } catch (SQLException ex) {
            throw new NominaException("Error al abrir cartola: id=" + this.getId(), ex);
        }
    }

    public void cerrar(
            final ContextoCartolas contexto) {
        cerrar(contexto, null);
    }

    public void cerrar(
            final ContextoCartolas contexto,
            final Throwable t) {
        try {
            String msg = null;
            String estado = EstadoProceso.TERMINADO;
            if (t != null) {
                estado = EstadoProceso.ERROR;
                if (t.getCause() != null) {
                    msg = t.getCause().getMessage();
                } else {
                    msg = t.getMessage();
                }
            }

            this.setEstadoProceso(estado);
            this.setMensajeProceso(msg);
            this.setReprocesar(Opcion.NO);
            CartolaService.save(contexto, this);
        } catch (SQLException ex) {
            throw new NominaException("Error al cerrar cartola: id=" + this.getId(), ex);
        }
    }

    public String getEstadoProceso() {
        return estadoProceso;
    }

    public long getId() {
        return id;
    }

    public long getIdCliente() {
        return idCliente;
    }

    public long getIdNomina() {
        return idNomina;
    }

    public long getIdPortafolio() {
        return idPortafolio;
    }

    public String getMensajeProceso() {
        return mensajeProceso;
    }

    public String getNotificar() {
        return notificar;
    }

    public String getReprocesar() {
        return reprocesar;
    }

    public void setEstadoProceso(
            final String estadoProceso) {
        this.estadoProceso = estadoProceso;
    }

    public void setId(
            final long id) {
        this.id = id;
    }

    public void setIdCliente(
            final long idCliente) {
        this.idCliente = idCliente;
    }

    public void setIdNomina(
            final long idNomina) {
        this.idNomina = idNomina;
    }

    public void setIdPortafolio(
            final long idPortafolio) {
        this.idPortafolio = idPortafolio;
    }

    public void setMensajeProceso(
            final String mensajeProceso) {
        this.mensajeProceso = mensajeProceso;
    }

    public void setNotificar(
            final String notificar) {
        this.notificar = notificar;
    }

    public void setReprocesar(
            final String reprocesar) {
        this.reprocesar = reprocesar;
    }

}
