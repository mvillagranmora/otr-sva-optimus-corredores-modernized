package cl.bcs.apv.application.recaudacion;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.bcs.apv.api.casouso.CambiarEstadoRecaudacion;
import cl.bcs.apv.api.casouso.CrearRecaudacionCliente;
import cl.bcs.apv.api.dto.cambiarestadorecaudacion.CambiarEstadoRecaudacionEntrada;
import cl.bcs.apv.api.dto.cambiarestadorecaudacion.ParametrosRecaudacionEstado;
import cl.bcs.apv.api.dto.crearrecaudacioncliente.Cliente;
import cl.bcs.apv.api.dto.crearrecaudacioncliente.CrearRecaudacionClienteEntrada;
import cl.bcs.apv.api.dto.crearrecaudacioncliente.CrearRecaudacionClienteResultado;
import cl.bcs.apv.api.dto.crearrecaudacioncliente.Resultado;
import cl.bcs.apv.domain.port.out.AlmacenArchivos;
import cl.bcs.apv.domain.port.out.Colaboradores;
import cl.bcs.apv.domain.port.out.ConfiguracionSistema;
import cl.bcs.apv.domain.port.out.DiccionarioMensajes;
import cl.bcs.apv.domain.port.out.Reloj;
import cl.bcs.apv.domain.port.out.Secuencias;

/**
 * Use case {@code CrearRecaudacionCliente} (family C): creates or modifies the client detail rows of a deposit and moves their state to ING.
 * Legacy {@code cl.bcs.APV.CrearRecaudacionCliente} (call and logica).
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class CrearRecaudacionClienteServicio implements CrearRecaudacionCliente {

    private static final String SQL_CREAR = SqlLegacy.recurso("DetalleCliente_Insert");
    private static final String SQL_MODIFICAR = SqlLegacy.recurso("DetalleCliente_Update");

    private final NamedParameterJdbcTemplate jdbc;
    private final Secuencias secuencias;
    private final CambiarEstadoRecaudacion cambiarEstadoRecaudacion;

    public CrearRecaudacionClienteServicio(NamedParameterJdbcTemplate jdbc, ConfiguracionSistema configuracion, DiccionarioMensajes diccionario,
            Secuencias secuencias, Reloj reloj, AlmacenArchivos archivos, Colaboradores colaboradores, CambiarEstadoRecaudacion cambiarEstadoRecaudacion) {
        this.jdbc = jdbc;
        this.secuencias = secuencias;
        this.cambiarEstadoRecaudacion = cambiarEstadoRecaudacion;
    }

    /** Row of the insert / update statements (legacy {@code CrearRecaudacionCliente.Cliente}). */
    public static class ClienteFila {
        public Long IdInterno = 0L;
        public Long IdRecaudacionCliente;
        public Long IdRecaudacion;
        public Long IdRecaudacionEmpleador;
        public Long IdEmpresa;
        public Long IdCliente;
        public String FechaIngreso;
        public String TipoAfiliado;
        public String IdRegimenTriburario;
        public BigDecimal MontoDepositoConvenido;
        public BigDecimal MontoDepositoAPV;
        public String Origen;
        public String IdEstado;
    }

    @Override
    public CrearRecaudacionClienteResultado ejecutar(CrearRecaudacionClienteEntrada in) {
        try {
            List<MensajeLegacy> mensajes = new ArrayList<>();
            List<Resultado> resultados = new ArrayList<>();
            logica(in, mensajes, resultados);
            return new CrearRecaudacionClienteResultado(MensajeLegacy.aMensajes(mensajes), MensajeLegacy.contarErrores(mensajes), resultados);
        } catch (Exception e) {
            throw Sneaky.lanzar(e);
        }
    }

    private void logica(CrearRecaudacionClienteEntrada in, List<MensajeLegacy> salidaMensajes, List<Resultado> salidaResultado) throws Exception {
        // LEGACY DEFECT kept (see README APV-C-220..228): one MensajeSalida instance (type "APV") is shared by every entry, so the batch shows the LAST row's result
        MensajeLegacy msg = new MensajeLegacy();
        List<Long> idRecaudacionCliente = Colab.secuencia(secuencias, "BSCL.APV.BD.Secuencia::REC.IdRecaudacionCliente", (long) in.parametros().size());
        int contadorIds = 0;

        for (Cliente fila : in.parametros()) {
            Long idRecaudacionClienteFila = fila.idRecaudacionCliente();
            if (in.tipo().compareTo("M") == 0) {
                // sin cambios
            } else {
                idRecaudacionClienteFila = idRecaudacionCliente.get(contadorIds);
            }

            ClienteFila cli = new ClienteFila();
            cli.IdRecaudacionCliente = idRecaudacionClienteFila;
            cli.IdRecaudacion = fila.idRecaudacion();
            cli.IdRecaudacionEmpleador = fila.idRecaudacionEmpleador();
            cli.IdEmpresa = in.idEmpresa();
            cli.IdCliente = fila.idCliente();
            cli.FechaIngreso = fila.fechaIngreso();
            cli.TipoAfiliado = fila.tipoAfiliado();
            cli.IdRegimenTriburario = fila.idRegimenTriburario();
            cli.MontoDepositoConvenido = fila.montoDepositoConvenido();
            cli.MontoDepositoAPV = fila.montoDepositoAPV();
            cli.Origen = fila.origen();
            cli.IdEstado = fila.idEstado();

            List<ClienteFila> paramsInsertCliente = new ArrayList<>();
            paramsInsertCliente.add(cli);
            int[] res = LoteConteo.ejecutar(jdbc, in.tipo().compareTo("M") == 0 ? SQL_MODIFICAR : SQL_CREAR, paramsInsertCliente);

            if (res != null && res.length > 0 && res[0] == 1) {
                msg.id = 1L;
                msg.tipo = "APV";
                msg.codigo = in.tipo().compareTo("M") == 0 ? "MODIFICACION_CLIENTE_OK" : "CREACION_CLIENTE_OK";
                msg.descripcion = in.tipo().compareTo("M") == 0 ? "Modificación de Detalle Cliente exitosa" : "Creacion de Detalle Cliente exitosa";
                salidaMensajes.add(msg);

                salidaResultado.add(new Resultado(fila.idInterno(), idRecaudacionClienteFila));

                Long idRecaudacionEmpleador = fila.idRecaudacionEmpleador();
                if (idRecaudacionEmpleador == null) {
                    idRecaudacionEmpleador = 0L;
                }
                List<ParametrosRecaudacionEstado> paramsCambioEstado = new ArrayList<>();
                paramsCambioEstado.add(new ParametrosRecaudacionEstado(fila.idRecaudacion(), idRecaudacionEmpleador, idRecaudacionClienteFila, "ING"));
                // the legacy ignores the answer of the state change
                cambiarEstadoRecaudacion.ejecutar(new CambiarEstadoRecaudacionEntrada(in.idEmpresa(), in.idUsuario(), in.idModulo(), paramsCambioEstado));
            } else {
                msg.id = 1L;
                msg.tipo = "APV";
                msg.codigo = "ERROR";
                msg.descripcion = in.tipo().compareTo("M") == 0 ? "Problemas al Modificar Detalle Cliente" : "Problemas al Crear Detalle Cliente";
                salidaMensajes.add(msg);
            }
            contadorIds++;
        }
    }
}
