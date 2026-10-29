package cl.bcs.apv.infrastructure.persistence.traspasos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import cl.bcs.apv.application.comun.Sneaky;
import cl.bcs.apv.domain.port.out.traspasos.GrabarContratosTraspasoRepositorio;

/** JDBC adapter of {@link GrabarContratosTraspasoRepositorio}; the SQL files are the legacy ones, byte for byte, under {@code sql/tra}. */
@Repository
public class GrabarContratosTraspasoJdbc implements GrabarContratosTraspasoRepositorio {

    private final SqlTraspasos sql;

    public GrabarContratosTraspasoJdbc(NamedParameterJdbcTemplate jdbc) {
        this.sql = new SqlTraspasos(jdbc);
    }

    /** Row of the address query and parameter class of the copy statement (legacy {@code ConsultaDireccion}). */
    public static class ConsultaDireccion {
        public Long IdDireccionFisica;
    }

    /** Row of the relation query (legacy {@code ConsultaRelacionPersona}). */
    public static class ConsultaRelacionPersona {
        public Long IdRelacionPersona;
    }

    /** Parameter class of the position update (legacy {@code CambiarCargoPersona}). */
    public static class CambiarCargoPersona {
        public Long IdPersona;
        public Long IdTipoCargoPersona;
    }

    @Override
    public List<Long> direccionesFisicas(Long idPersona) {
        Map<String, Object> p = new HashMap<>();
        p.put("IdPersona", idPersona);
        List<ConsultaDireccion> filas = Sneaky.sinChequear(() -> sql.consultar(SqlTraspasos.recurso("ConsultarDireccionFisica"), p, ConsultaDireccion.class));
        List<Long> ids = new ArrayList<>();
        for (ConsultaDireccion f : filas) {
            ids.add(f.IdDireccionFisica);
        }
        return ids;
    }

    @Override
    public void copiarDireccionesConPropositoApv(List<Long> idsDireccionFisica) {
        List<ConsultaDireccion> lote = new ArrayList<>();
        for (Long id : idsDireccionFisica) {
            ConsultaDireccion c = new ConsultaDireccion();
            c.IdDireccionFisica = id;
            lote.add(c);
        }
        Sneaky.sinChequear(() -> sql.lote(SqlTraspasos.recurso("CreaDireccionPropositoAPV"), lote));
    }

    @Override
    public int[] cambiarCargoPersona(Long idPersona, Long idTipoCargoPersona) {
        CambiarCargoPersona c = new CambiarCargoPersona();
        c.IdPersona = idPersona;
        c.IdTipoCargoPersona = idTipoCargoPersona;
        return Sneaky.sinChequear(() -> sql.loteConConteos(SqlTraspasos.recurso("CrearPersonaActividaCargo_Update"), List.of(c)));
    }

    @Override
    public List<TipoIndicador> tiposIndicador(String idTipoEntidad, String grupoIndicador) {
        Map<String, Object> p = new HashMap<>();
        p.put("IdTipoEntidad", idTipoEntidad);
        p.put("GrupoIndicador", grupoIndicador);
        return Sneaky.sinChequear(() -> sql.consultar(SqlTraspasos.recurso("ConsultaTipoIndicadorPersona"), p, TipoIndicador.class));
    }

    @Override
    public void crearTablaRegistroAtributo() {
        Sneaky.sinChequear(() -> sql.ejecutar(SqlTraspasos.recurso("GrabarContratoSAAPV_CrearRegistroAtributo")));
    }

    @Override
    public void insertarRegistroAtributo(List<IndicadorEncargadoRRHH> indicadores) {
        Sneaky.sinChequear(() -> sql.lote(SqlTraspasos.recurso("GrabarContratoSAAPV_InsertRegistroAtributo"), indicadores));
    }

    @Override
    public void eliminarTablaRegistroAtributo() {
        Sneaky.sinChequear(() -> sql.ejecutar(SqlTraspasos.recurso("GrabarContratoSAAPV_DropRegistroAtributo")));
    }

    @Override
    public List<Long> relacionesPersona(Long idPersona, Long idEmpleador) {
        Map<String, Object> p = new HashMap<>();
        p.put("IdPersona", idPersona);
        p.put("IdEmpleador", idEmpleador);
        List<ConsultaRelacionPersona> filas = Sneaky
                .sinChequear(() -> sql.consultar(SqlTraspasos.recurso("ConsultarRelacionPersona"), p, ConsultaRelacionPersona.class));
        List<Long> ids = new ArrayList<>();
        for (ConsultaRelacionPersona f : filas) {
            ids.add(f.IdRelacionPersona);
        }
        return ids;
    }

    @Override
    public int[] insertarContrato(ContratoSAAPV contrato) {
        return Sneaky.sinChequear(() -> sql.loteConConteos(SqlTraspasos.recurso("ContratoSAAPV_Insert"), List.of(contrato)));
    }

    @Override
    public int[] insertarTraspaso(Traspaso traspaso) {
        return Sneaky.sinChequear(() -> sql.loteConConteos(SqlTraspasos.recurso("Traspaso_Insert"), List.of(traspaso)));
    }

    @Override
    public int[] insertarDetalleOrigenTraspaso(List<DetalleOrigenTraspaso> detalles) {
        return Sneaky.sinChequear(() -> sql.loteConConteos(SqlTraspasos.recurso("DetalleOrigenTraspaso_Insert"), detalles));
    }

    @Override
    public int[] insertarDistribucionInversion(List<DistribucionInversion> distribucion) {
        return Sneaky.sinChequear(() -> sql.loteConConteos(SqlTraspasos.recurso("AgregarDistribucionInversion"), distribucion));
    }
}
