package cl.bcs.apv.infrastructure.persistence.traspasos;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import cl.bcs.apv.application.comun.Sneaky;
import cl.bcs.apv.domain.port.out.traspasos.CargarHistoricoTraspasosRepositorio;

/** JDBC adapter of {@link CargarHistoricoTraspasosRepositorio}; the SQL files are the legacy ones, byte for byte. */
@Repository
public class CargarHistoricoTraspasosJdbc implements CargarHistoricoTraspasosRepositorio {

    private static final String SQL_CABECERA = SqlTraspasos.recurso("CargaMasivaArchivo_Insert");
    private static final String SQL_XML = SqlTraspasos.recurso("CargaXMLRegistroHistorico_Insert");

    private final SqlTraspasos sql;

    public CargarHistoricoTraspasosJdbc(NamedParameterJdbcTemplate jdbc) {
        this.sql = new SqlTraspasos(jdbc);
    }

    @Override
    public int insertarCabeceraCarga(String idEmpresa, Long idCargaMasiva, String nombreCarga, Long cantidadRegistros, String idUsuario, Long idTipoCarga,
            String descripcion, String fechaCarga, String idEstado) {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("IdEmpresa", idEmpresa);
        p.put("IdCargaMasiva", idCargaMasiva);
        p.put("NombreCargaMasiva", nombreCarga);
        p.put("CantidadRegistros", cantidadRegistros);
        p.put("IdUsuario", idUsuario);
        p.put("IdTipoCarga", idTipoCarga);
        p.put("Descripcion", descripcion);
        p.put("FechaCarga", fechaCarga);
        p.put("IdEstado", idEstado);
        return Sneaky.sinChequear(() -> sql.actualizar(SQL_CABECERA, p));
    }

    @Override
    public void insertarRegistroXml(String idEmpresa, String idModulo, String idUsuario, String nombreCarga, String origen, String fechaCarga, String archivo) {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("p_IdEmpresa", idEmpresa);
        p.put("p_IdModulo", idModulo);
        p.put("p_IdUsuario", idUsuario);
        p.put("p_NombreCarga", nombreCarga);
        p.put("p_Origen", origen);
        p.put("p_FechaCarga", fechaCarga);
        p.put("p_Archivo", archivo);
        Sneaky.sinChequear(() -> sql.actualizar(SQL_XML, p));
    }

    @Override
    public void confirmar() {
        Sneaky.sinChequear(() -> sql.confirmar());
    }
}
