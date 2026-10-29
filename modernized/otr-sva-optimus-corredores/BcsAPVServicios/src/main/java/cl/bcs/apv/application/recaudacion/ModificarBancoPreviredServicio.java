package cl.bcs.apv.application.recaudacion;

import java.util.List;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.bcs.apv.api.casouso.ModificarBancoPrevired;
import cl.bcs.apv.api.dto.modificarbancoprevired.ModificarBancoPreviredEntrada;
import cl.bcs.apv.api.dto.modificarbancoprevired.ModificarBancoPreviredResultado;
import cl.bcs.apv.application.recaudacion.BancoPreviredValidador.BancoPreviredFila;
import cl.bcs.apv.application.recaudacion.BancoPreviredValidador.ResultadoValidacion;
import cl.bcs.apv.domain.port.out.AlmacenArchivos;
import cl.bcs.apv.domain.port.out.Colaboradores;
import cl.bcs.apv.domain.port.out.ConfiguracionSistema;
import cl.bcs.apv.domain.port.out.DiccionarioMensajes;
import cl.bcs.apv.domain.port.out.Reloj;
import cl.bcs.apv.domain.port.out.Secuencias;

/** Use case {@code ModificarBancoPrevired} (family C): validates the Previred bank codes and applies the valid ones. Legacy {@code cl.bcs.APV.ModificarBancoPrevired}. */
@Service
@Transactional(rollbackFor = Exception.class)
public class ModificarBancoPreviredServicio implements ModificarBancoPrevired {

    private static final String SQL_UPDATE = SqlLegacy.recurso("ModificarBancoPrevired_UpdateBancosPrevired");

    private final SqlLegacy sql;
    private final BancoPreviredValidador validador;

    public ModificarBancoPreviredServicio(NamedParameterJdbcTemplate jdbc, ConfiguracionSistema configuracion, DiccionarioMensajes diccionario, Secuencias secuencias,
            Reloj reloj, AlmacenArchivos archivos, Colaboradores colaboradores) {
        this.sql = new SqlLegacy(jdbc);
        this.validador = new BancoPreviredValidador(sql, diccionario);
    }

    @Override
    public ModificarBancoPreviredResultado ejecutar(ModificarBancoPreviredEntrada entrada) {
        try {
            List<BancoPreviredFila> filas = entrada.bancosPrevired().stream()
                    .map(b -> new BancoPreviredFila(b.idInterno(), b.idCuentaBanco(), b.codigoBancoPrevired())).toList();
            ResultadoValidacion r = validador.validarModificacion(filas, entrada.idEmpresa());
            sql.lote(SQL_UPDATE, r.bancosPreviredCorrectos);
            return new ModificarBancoPreviredResultado(MensajeLegacy.contarErrores(r.mensajesErrores), MensajeLegacy.aMensajes(r.mensajesErrores));
        } catch (Exception e) {
            throw Sneaky.lanzar(e);
        }
    }
}
