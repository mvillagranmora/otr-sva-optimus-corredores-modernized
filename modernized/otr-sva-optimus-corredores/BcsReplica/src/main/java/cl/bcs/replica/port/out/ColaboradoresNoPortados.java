package cl.bcs.replica.port.out;

import java.sql.Connection;
import java.util.List;

import com.google.gson.JsonObject;

import cl.bcs.replica.kit.ServicioNoServidoException;

public class ColaboradoresNoPortados implements ConsultarEmpresaPort, ObtenerDiaSiguientePort, FeriadosPort, ConfiguracionSistemaPort, FechaSistemaPort, SecuenciaPort {
    private static ServicioNoServidoException cerrado(String colaborador) {
        return new ServicioNoServidoException("colaborador no portado: " + colaborador);
    }

    @Override
    public Long idPais(Connection conn, Long idEmpresa) {
        throw cerrado("ConsultarEmpresa");
    }

    @Override
    public JsonObject logica(Connection conn, JsonObject entrada) {
        throw cerrado("ObtenerDiaSiguiente");
    }

    @Override
    public boolean esDiaHabil(Connection conn, String fecha, Long idPais) {
        throw cerrado("Feriados");
    }

    @Override
    public long parametroLong(Connection conn, Long idEmpresa, String modulo, String parametro) {
        throw cerrado("ConfiguracionSistema");
    }

    @Override
    public String parametroString(Connection conn, Long idEmpresa, String modulo, String parametro) {
        throw cerrado("ConfiguracionSistema");
    }

    @Override
    public String fechaSistema(Connection conn, String esquema, long idEmpresa) {
        throw cerrado("Utils.getFechaSistema");
    }

    @Override
    public List<Long> siguientes(Connection conn, String secuencia, Long cantidad) {
        throw cerrado("Utils.getSecuencia");
    }
}
