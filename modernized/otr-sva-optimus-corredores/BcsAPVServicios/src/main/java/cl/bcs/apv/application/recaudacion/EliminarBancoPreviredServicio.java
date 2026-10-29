package cl.bcs.apv.application.recaudacion;

import java.util.List;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.bcs.apv.api.casouso.EliminarBancoPrevired;
import cl.bcs.apv.api.dto.eliminarbancoprevired.EliminarBancoPreviredEntrada;
import cl.bcs.apv.api.dto.eliminarbancoprevired.EliminarBancoPreviredResultado;
import cl.bcs.apv.application.recaudacion.BancoPreviredValidador.BancoPreviredFila;
import cl.bcs.apv.application.recaudacion.BancoPreviredValidador.ResultadoValidacion;
import cl.bcs.apv.domain.port.out.AlmacenArchivos;
import cl.bcs.apv.domain.port.out.Colaboradores;
import cl.bcs.apv.domain.port.out.ConfiguracionSistema;
import cl.bcs.apv.domain.port.out.DiccionarioMensajes;
import cl.bcs.apv.domain.port.out.Reloj;
import cl.bcs.apv.domain.port.out.Secuencias;

/** Use case {@code EliminarBancoPrevired} (family C): validates the Previred bank codes and applies the valid ones. Legacy {@code cl.bcs.APV.EliminarBancoPrevired}. */
@Service
@Transactional(rollbackFor = Exception.class)
public class EliminarBancoPreviredServicio implements EliminarBancoPrevired {

    private static final String SQL_DELETE = SqlLegacy.recurso("EliminarBancoPrevired_DeleteBancosPrevired");

    private final SqlLegacy sql;
    private final BancoPreviredValidador validador;

    public EliminarBancoPreviredServicio(NamedParameterJdbcTemplate jdbc, ConfiguracionSistema configuracion, DiccionarioMensajes diccionario, Secuencias secuencias,
            Reloj reloj, AlmacenArchivos archivos, Colaboradores colaboradores) {
        this.sql = new SqlLegacy(jdbc);
        this.validador = new BancoPreviredValidador(sql, diccionario);
    }

    @Override
    public EliminarBancoPreviredResultado ejecutar(EliminarBancoPreviredEntrada entrada) {
        try {
            List<BancoPreviredFila> filas = entrada.bancosPrevired().stream()
                    .map(b -> new BancoPreviredFila(b.idInterno(), b.idCuentaBanco(), b.codigoBancoPrevired())).toList();
            ResultadoValidacion r = validador.validarEliminacion(filas, entrada.idEmpresa());
            sql.lote(SQL_DELETE, r.bancosPreviredCorrectos);
            return new EliminarBancoPreviredResultado(MensajeLegacy.contarErrores(r.mensajesErrores), MensajeLegacy.aMensajes(r.mensajesErrores));
        } catch (Exception e) {
            throw Sneaky.lanzar(e);
        }
    }
}
