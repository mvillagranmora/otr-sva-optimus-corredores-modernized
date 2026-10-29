package cl.bcs.normativa.dal;

import java.sql.ResultSet;
import java.sql.SQLException;

import cl.bcs.normativa.esquema.Direccion;
import cl.bcs.normativa.esquema.Empresa;
import cl.bcs.utils.JdbcUtil;

/**
 * Implementa mapeo para producir {@link Empresa}.
 *
 * @author fpino@email.com
 *
 */
public final class EmpresaMapper {

    /**
     * Obtiene una nueva instancia de {@link Empresa} a partir de
     * {@link ResultSet}.
     *
     * @param rs
     *            El {@link ResultSet}.
     * @return Una nueva instancia de {@link Empresa} a partir de
     *         {@link ResultSet}.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public static Empresa map(final ResultSet rs) throws SQLException {
        final Empresa empresa = new Empresa();
        empresa.setId(JdbcUtil.getLong(rs, "IdEmpresa"));
        empresa.setCodigo(JdbcUtil.getString(rs, "Codigo"));
        empresa.setNombre(JdbcUtil.getString(rs, "Nombre"));
        empresa.setRut(JdbcUtil.getString(rs, "DocumentoIdentidad"));
        empresa.setRazonSocial(JdbcUtil.getString(rs, "RazonSocial"));
        empresa.setCorredora(JdbcUtil.getStringAsBoolean(rs, "EsCorredora"));
        empresa.setRutRepresentante(JdbcUtil.getString(rs, "DocumentoIdentidadRepresentante"));
        empresa.setEmailPrincipal(JdbcUtil.getString(rs, "DireccionElectronicaPrincipal"));
        empresa.setFonoPrincipal(JdbcUtil.getString(rs, "TelefonoPrincipal"));

        final Direccion direccion = new Direccion();
        direccion.setCalle(JdbcUtil.getString(rs, "DireccionFisicaPrincipalCalle"));
        direccion.setNumero(JdbcUtil.getString(rs, "DireccionFisicaPrincipalNumero"));
        direccion.setDepartamento(JdbcUtil.getString(rs, "DireccionFisicaPrincipalDepartamento"));
        direccion.setSector(JdbcUtil.getString(rs, "DireccionFisicaPrincipalSector"));
        direccion.setCiudad(JdbcUtil.getString(rs, "DireccionFisicaPrincipalNombreCiudad"));
        direccion.setComuna(JdbcUtil.getString(rs, "DireccionFisicaPrincipalNombreDivisionPais"));
        empresa.setDireccionPrincipal(direccion);

        return empresa;
    }

    private EmpresaMapper() {
    }

}
