package cl.bcs.apv.infrastructure.persistence.traspasos;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import cl.bcs.apv.domain.port.out.traspasos.CrearTraspasoTesoreriaRepositorio;

/** JDBC adapter of {@link CrearTraspasoTesoreriaRepositorio}; the SQL files are the legacy ones byte for byte, the inline statements are the legacy strings. */
@Repository
public class CrearTraspasoTesoreriaJdbc implements CrearTraspasoTesoreriaRepositorio {

    private static final String SQL_CtaBco = "SELECT \"DestinoCtaBco\" FROM \"BSCL.APV.BD::APV.OrigenDestinoTransicion\" WHERE \"Operacion\" = 'DEV' AND \"IdEmpresa\" = :IdEmpresa";
    private static final String SQL_CONTRATOSAAPV = "SELECT \"IdContratoSAAPV\" FROM \"BSCL.APV.BD::TRA.Traspaso\" WHERE \"IdTraspaso\" = :IdTraspaso";
    private static final String SQL_SELECT_CONFIGURACION_RETENCION = SqlTraspasos.recurso("ConsultarRetencion");
    private static final String SQL_PORTAFOLIO_PERSONA = "SELECT POR.\"IdPortafolio\", CLI.\"IdCliente\""
            + " FROM \"BSCL.Portafolio.BD::POR.Portafolio\" AS POR"
            + " INNER JOIN \"BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente\" AS CLI"
            + "		ON CLI.\"IdCliente\" = POR.\"IdCliente\""
            + " INNER JOIN \"BSCL.SocioNegocio.BD::SOC.SocioDeNegocio\" AS SOC"
            + "		ON SOC.\"IdCliente\" = CLI.\"IdCliente\""
            + "	   AND SOC.\"IdPersona\" = CLI.\"IdPersona\""
            + " WHERE SOC.\"IdPersona\" = :IdPersona AND \"IdMatrizAgrupadorAPV\" > 0 AND POR.\"IdEstado\" <> 'INA' ";
    private static final String SQL_CONSULTARPORTAFOLIOEMPRESA = "SELECT Empresa.\"IdPersona\",\"IdPortafolioDefecto\" FROM \"BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa\" Empresa"
            + " INNER JOIN \"BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente\" Cliente"
            + " ON Cliente.\"IdSocioDeNegocio\" = Empresa.\"IdSocioDeNegocio\""
            + " WHERE Empresa.\"IdEmpresa\" = :IdEmpresa";
    private static final String SQL_CONSULTA_ORDENES_RV_FFMM = SqlTraspasos.recurso("ConsultaOrdenesTraspaso");
    private static final String SQL_CREAR_TABLA = "CREATE LOCAL TEMPORARY TABLE #p_CuentaInversionAConsultar"
            + "(\"IdEjecutivo\"	 BIGINT"
            + ",\"IdCliente\" BIGINT"
            + ",\"IdMoneda\" NVARCHAR(255)"
            + ",\"IdPortafolio\" BIGINT"
            + ")";
    private static final String SQL_INSERTAR = "INSERT INTO #p_CuentaInversionAConsultar"
            + "(\"IdEjecutivo\""
            + ",\"IdCliente\""
            + ",\"IdMoneda\""
            + ",\"IdPortafolio\") "
            + "VALUES(:IdEjecutivo"
            + ", :IdCliente"
            + ", :IdMoneda"
            + ", :IdPortafolio)";
    private static final String SQL_ELIMINAR = "DROP TABLE #p_CuentaInversionAConsultar";

    private final SqlTraspasos sql;

    public CrearTraspasoTesoreriaJdbc(NamedParameterJdbcTemplate jdbc) {
        this.sql = new SqlTraspasos(jdbc);
    }

    @Override
    public List<DestinoCtaBco> destinoCuentaBanco(Long idEmpresa) throws SQLException {
        Map<String, Object> p = new HashMap<>();
        p.put("IdEmpresa", idEmpresa);
        return sql.consultar(SQL_CtaBco, p, DestinoCtaBco.class);
    }

    @Override
    public List<DatosEmpresa> portafolioEmpresa(Long idEmpresa) throws SQLException {
        Map<String, Object> p = new HashMap<>();
        p.put("IdEmpresa", idEmpresa);
        return sql.consultar(SQL_CONSULTARPORTAFOLIOEMPRESA, p, DatosEmpresa.class);
    }

    @Override
    public List<Retencion> configuracionRetencion() throws SQLException {
        return sql.consultar(SQL_SELECT_CONFIGURACION_RETENCION, Map.of(), Retencion.class);
    }

    @Override
    public List<Portafolio> portafoliosPersona(Long idPersona) throws SQLException {
        Map<String, Object> p = new HashMap<>();
        p.put("IdPersona", idPersona);
        return sql.consultar(SQL_PORTAFOLIO_PERSONA, p, Portafolio.class);
    }

    @Override
    public void crearTablaCuentaInversionAConsultar() throws SQLException {
        sql.ejecutar(SQL_CREAR_TABLA);
    }

    @Override
    public void insertarCuentaInversionAConsultar(List<DatosCuentaInversion> filas) throws SQLException {
        sql.lote(SQL_INSERTAR, filas);
    }

    @Override
    public void eliminarTablaCuentaInversionAConsultar() throws SQLException {
        sql.ejecutar(SQL_ELIMINAR);
    }

    @Override
    public List<OrdenTraspaso> ordenesDelTraspaso(Long idTraspaso) throws SQLException {
        Map<String, Object> p = new HashMap<>();
        p.put("IdTraspaso", idTraspaso);
        return sql.consultar(SQL_CONSULTA_ORDENES_RV_FFMM, p, OrdenTraspaso.class);
    }

    @Override
    public List<ContratoSAAPV> contratoDelTraspaso(Long idTraspaso) throws SQLException {
        Map<String, Object> p = new HashMap<>();
        p.put("IdTraspaso", idTraspaso);
        return sql.consultar(SQL_CONTRATOSAAPV, p, ContratoSAAPV.class);
    }
}
