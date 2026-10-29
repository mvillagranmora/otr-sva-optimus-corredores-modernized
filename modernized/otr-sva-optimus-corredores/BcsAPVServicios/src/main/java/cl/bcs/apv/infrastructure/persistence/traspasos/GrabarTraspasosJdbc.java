package cl.bcs.apv.infrastructure.persistence.traspasos;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import cl.bcs.apv.domain.port.out.traspasos.GrabarTraspasosRepositorio;

/** JDBC adapter of {@link GrabarTraspasosRepositorio}; the SQL files are the legacy ones byte for byte. */
@Repository
public class GrabarTraspasosJdbc implements GrabarTraspasosRepositorio {

    private static final String SQL_CREAR_DETALLETRASPASO = SqlTraspasos.recurso("DetalleTraspaso_Insert");
    private static final String SQL_MODIFICA_TRASPASO = SqlTraspasos.recurso("Traspaso_Update");
    private static final String SQL_CREAR_PORTAFOLIOS_CONTRATOS_SAAPV = SqlTraspasos.recurso("Crear_PortafolioContratoSAAPV");
    private static final String SQL_INHABILITAR_ESTADO_PORTAFOLIO = SqlTraspasos.recurso("InhabilitarPortafolioTraspaso");
    private static final String SQL_SELECT_ESTADO_CLIENTE = SqlTraspasos.recurso("GrabarTraspasos_SelectEstadoCliente");
    private static final String SQL_VISTA_PORTAFOLIOS_CLIENTE = "SELECT \"IdPortafolio\", \"CodigoPortafolio\", \"Proposito\", \"IdCliente\", \"IdEstado\" FROM BSCL.\"BSCL.Portafolio.BD.Vistas::ConsultarPortafolio\"  WHERE \"IdCliente\" = :IdCliente AND \"Proposito\" LIKE '%PROPOSITOAPV%';";

    private final SqlTraspasos sql;

    public GrabarTraspasosJdbc(NamedParameterJdbcTemplate jdbc) {
        this.sql = new SqlTraspasos(jdbc);
    }

    /** Batch parameters of the portafolio state update (legacy {@code EstadoPortafolio}). */
    public static class EstadoPortafolio {
        public Long IdPortafolio;
        public String IdEstado;
        public String IdEstadoAutorizacion;
    }

    @Override
    public List<EstadoCliente> estadoClientes(List<Long> idsCliente) throws SQLException {
        Map<String, Object> p = new HashMap<>();
        p.put("idCliente", idsCliente);
        return sql.consultar(SQL_SELECT_ESTADO_CLIENTE, p, EstadoCliente.class);
    }

    @Override
    public List<VistaInfoPort> portafoliosDelCliente(Long idCliente) throws SQLException {
        Map<String, Object> p = new HashMap<>();
        p.put("IdCliente", idCliente);
        return sql.consultar(SQL_VISTA_PORTAFOLIOS_CLIENTE, p, VistaInfoPort.class);
    }

    @Override
    public boolean modificarEstadoPortafolio(Long idPortafolio, String estado) throws SQLException {
        List<EstadoPortafolio> lista = new ArrayList<>();
        EstadoPortafolio e = new EstadoPortafolio();
        e.IdPortafolio = idPortafolio;
        e.IdEstado = estado.compareTo("ACT") == 0 ? estado : "INA";
        e.IdEstadoAutorizacion = "AUT";
        lista.add(e);
        int[] modCon = sql.loteConConteos(SQL_INHABILITAR_ESTADO_PORTAFOLIO, lista);
        return modCon.length > 0;
    }

    @Override
    public int[] insertarDetallesTraspaso(List<DetalleTraspaso> detalles) throws SQLException {
        return sql.loteConConteos(SQL_CREAR_DETALLETRASPASO, detalles);
    }

    @Override
    public int[] insertarPortafoliosContrato(List<PortafolioContratoSAAPV> filas) throws SQLException {
        return sql.loteConConteos(SQL_CREAR_PORTAFOLIOS_CONTRATOS_SAAPV, filas);
    }

    @Override
    public int[] modificarEstadoTraspaso(List<TraspasoModificar> filas) throws SQLException {
        return sql.loteConConteos(SQL_MODIFICA_TRASPASO, filas);
    }
}
