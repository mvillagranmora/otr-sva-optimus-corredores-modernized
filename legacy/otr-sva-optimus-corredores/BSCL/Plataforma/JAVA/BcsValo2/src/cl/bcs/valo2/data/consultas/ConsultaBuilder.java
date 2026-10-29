package cl.bcs.valo2.data.consultas;

import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.sql.DataSource;

import cl.bcs.sql.metadata.SqlMetadata;
import cl.bcs.valo2.data.consultas.saphana.ConsultaBonosSH;
import cl.bcs.valo2.data.consultas.saphana.ConsultaConfigSH;
import cl.bcs.valo2.data.consultas.saphana.ConsultaCuponesSH;
import cl.bcs.valo2.data.consultas.saphana.ConsultaFeriadosSH;
import cl.bcs.valo2.data.consultas.saphana.ConsultaIndicadoresSH;
import cl.bcs.valo2.data.consultas.saphana.ConsultaParidadesSH;
import cl.bcs.valo2.data.consultas.sqlserver.ConsultaBonosSS;
import cl.bcs.valo2.data.consultas.sqlserver.ConsultaConfigSS;
import cl.bcs.valo2.data.consultas.sqlserver.ConsultaCuponesSS;
import cl.bcs.valo2.data.consultas.sqlserver.ConsultaFeriadosSS;
import cl.bcs.valo2.data.consultas.sqlserver.ConsultaIndicadoresSS;
import cl.bcs.valo2.data.consultas.sqlserver.ConsultaParidadesSS;
import cl.bcs.valo2.entidades.Bono;
import cl.bcs.valo2.entidades.Cupon;
import cl.bcs.valo2.entidades.Indicador;
import cl.bcs.valo2.entidades.Indicador.Clave;
import cl.bcs.valo2.fechas.Fecha;

public final class ConsultaBuilder {

    public static Consulta<String, Bono> newConsultaBonos(
            final DataSource dataSource)
                    throws SQLException {
        if (SqlMetadata.isSqlServer(dataSource)) {
            return new ConsultaBonosSS(dataSource);
        }

        return new ConsultaBonosSH(dataSource);
    }

    public static Consulta<String, Properties> newConsultaConfig(
            final DataSource dataSource)
                    throws SQLException {
        if (SqlMetadata.isSqlServer(dataSource)) {
            return new ConsultaConfigSS(dataSource);
        }

        return new ConsultaConfigSH(dataSource);
    }

    public static Consulta<String, List<Cupon>> newConsultaCupones(
            final DataSource dataSource)
                    throws SQLException {
        if (SqlMetadata.isSqlServer(dataSource)) {
            return new ConsultaCuponesSS(dataSource);
        }

        return new ConsultaCuponesSH(dataSource);
    }

    public static Consulta<String, Set<Fecha>> newConsultaFeriados(
            final DataSource dataSource)
                    throws SQLException {
        if (SqlMetadata.isSqlServer(dataSource)) {
            return new ConsultaFeriadosSS(dataSource);
        }

        return new ConsultaFeriadosSH(dataSource);
    }

    public static Consulta<Clave, Indicador> newConsultaIndicadores(
            final DataSource dataSource)
                    throws SQLException {
        if (SqlMetadata.isSqlServer(dataSource)) {
            return new ConsultaIndicadoresSS(dataSource);
        }

        return new ConsultaIndicadoresSH(dataSource);
    }

    public static Consulta<Clave, Indicador> newConsultaParidades(
            final DataSource dataSource)
                    throws SQLException {
        if (SqlMetadata.isSqlServer(dataSource)) {
            return new ConsultaParidadesSS(dataSource);
        }

        return new ConsultaParidadesSH(dataSource);
    }

    private ConsultaBuilder() {
    }
}
