package cl.bcs.apv.application.recaudacion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.bcs.apv.api.casouso.CambiarEstadoRecaudacion;
import cl.bcs.apv.api.casouso.CrearRecaudacionEmpleador;
import cl.bcs.apv.api.dto.cambiarestadorecaudacion.CambiarEstadoRecaudacionEntrada;
import cl.bcs.apv.api.dto.cambiarestadorecaudacion.ParametrosRecaudacionEstado;
import cl.bcs.apv.api.dto.crearrecaudacionempleador.CrearRecaudacionEmpleadorEntrada;
import cl.bcs.apv.api.dto.crearrecaudacionempleador.CrearRecaudacionEmpleadorResultado;
import cl.bcs.apv.api.dto.crearrecaudacionempleador.Empleador;
import cl.bcs.apv.api.dto.crearrecaudacionempleador.Resultado;
import cl.bcs.apv.domain.port.out.AlmacenArchivos;
import cl.bcs.apv.domain.port.out.Colaboradores;
import cl.bcs.apv.domain.port.out.ConfiguracionSistema;
import cl.bcs.apv.domain.port.out.DiccionarioMensajes;
import cl.bcs.apv.domain.port.out.Reloj;
import cl.bcs.apv.domain.port.out.Secuencias;

/**
 * Use case {@code CrearRecaudacionEmpleador} (family C): creates or modifies the employer detail rows of a deposit and moves the state of the employer
 * and of its client details to ING. Legacy {@code cl.bcs.APV.CrearRecaudacionEmpleador} (call and logica).
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class CrearRecaudacionEmpleadorServicio implements CrearRecaudacionEmpleador {

    private static final String SQL_CREAR = SqlLegacy.recurso("DetalleEmpleador_Insert");
    private static final String SQL_MODIFICAR = SqlLegacy.recurso("DetalleEmpleador_Update");
    private static final String SQL_DETALLE_CLIENTES_POR_DETALLE_EMPLEADOR = SqlLegacy.recurso("ConsultarDetalleClientesPorDetalleEmpleador");

    private final NamedParameterJdbcTemplate jdbc;
    private final Secuencias secuencias;
    private final SqlLegacy sql;
    private final CambiarEstadoRecaudacion cambiarEstadoRecaudacion;

    public CrearRecaudacionEmpleadorServicio(NamedParameterJdbcTemplate jdbc, ConfiguracionSistema configuracion, DiccionarioMensajes diccionario,
            Secuencias secuencias, Reloj reloj, AlmacenArchivos archivos, Colaboradores colaboradores, CambiarEstadoRecaudacion cambiarEstadoRecaudacion) {
        this.jdbc = jdbc;
        this.secuencias = secuencias;
        this.sql = new SqlLegacy(jdbc);
        this.cambiarEstadoRecaudacion = cambiarEstadoRecaudacion;
    }

    /** Row of the insert / update statements (legacy {@code CrearRecaudacionEmpleador.Empleador}). */
    public static class EmpleadorFila {
        public Long IdInterno = 0L;
        public Long IdRecaudacionEmpleador;
        public Long IdRecaudacion;
        public String CodigoArchivo;
        public Long IdEmpresa;
        public Long IdPersona;
        public String FechaIngreso;
        public String PeriodoPago;
        public Long NumeroHojas;
        public String TipoAfiliado;
        public String TipoPago;
        public String Insistencias;
        public Long MontoDepositoConvenido;
        public Long MontoDepositoAPV;
        public Long Reajustes;
        public Long Intereses;
        public String Origen;
        public String Observaciones;
        public String IdEstado;
    }

    /** Row of the client details query. */
    public static class DetalleCliente {
        public Long IdRecaudacionCliente;
    }

    @Override
    public CrearRecaudacionEmpleadorResultado ejecutar(CrearRecaudacionEmpleadorEntrada in) {
        try {
            List<MensajeLegacy> mensajes = new ArrayList<>();
            List<Resultado> resultados = new ArrayList<>();
            Long[] idRecEmp = new Long[1];
            logica(in, mensajes, resultados, idRecEmp);
            return new CrearRecaudacionEmpleadorResultado(MensajeLegacy.aMensajes(mensajes), MensajeLegacy.contarErrores(mensajes), idRecEmp[0], resultados);
        } catch (Exception e) {
            throw Sneaky.lanzar(e);
        }
    }

    private void logica(CrearRecaudacionEmpleadorEntrada in, List<MensajeLegacy> salidaMensajes, List<Resultado> salidaResultado, Long[] salidaIdRecEmp)
            throws Exception {
        // LEGACY DEFECT kept (see README APV-C-230..236): one MensajeSalida instance (type "APV") is shared by every entry, so the batch shows the LAST row's result
        MensajeLegacy msg = new MensajeLegacy();
        List<Long> idRecaudacionEmpleador = Colab.secuencia(secuencias, "BSCL.APV.BD.Secuencia::REC.IdRecaudacionEmpleador", (long) in.parametros().size());
        int contadorIds = 0;

        for (Empleador fila : in.parametros()) {
            Long idRecaudacionEmpleadorFila = fila.idRecaudacionEmpleador();
            if (in.tipo().compareTo("M") == 0) {
                // sin cambios
            } else {
                idRecaudacionEmpleadorFila = idRecaudacionEmpleador.get(contadorIds);
            }

            EmpleadorFila emp = new EmpleadorFila();
            emp.IdRecaudacionEmpleador = idRecaudacionEmpleadorFila;
            emp.IdRecaudacion = fila.idRecaudacion();
            emp.CodigoArchivo = fila.codigoArchivo();
            emp.IdEmpresa = in.idEmpresa();
            emp.IdPersona = fila.idPersona();
            emp.FechaIngreso = fila.fechaIngreso();
            emp.PeriodoPago = fila.periodoPago();
            emp.NumeroHojas = fila.numeroHojas();
            emp.TipoAfiliado = fila.tipoAfiliado();
            emp.TipoPago = fila.tipoPago();
            emp.Insistencias = fila.insistencias();
            emp.MontoDepositoConvenido = fila.montoDepositoConvenido();
            emp.MontoDepositoAPV = fila.montoDepositoAPV();
            emp.Reajustes = fila.reajustes();
            emp.Intereses = fila.intereses();
            emp.Origen = fila.origen();
            emp.Observaciones = fila.observaciones();
            emp.IdEstado = fila.idEstado();

            List<EmpleadorFila> paramsInsertEmpleador = new ArrayList<>();
            paramsInsertEmpleador.add(emp);
            int[] res = LoteConteo.ejecutar(jdbc, in.tipo().compareTo("M") == 0 ? SQL_MODIFICAR : SQL_CREAR, paramsInsertEmpleador);

            if (res != null && res.length > 0 && res[0] == 1) {
                msg.id = 1L;
                msg.tipo = "APV";
                msg.codigo = in.tipo().compareTo("M") == 0 ? "MODIFICACION_EMPLEADOR_OK" : "CREACION_EMPLEADOR_OK";
                msg.descripcion = in.tipo().compareTo("M") == 0 ? "Modificación de Detalle Empleador exitosa" : "Creacion de Detalle Empleador exitosa";

                salidaResultado.add(new Resultado(fila.idInterno(), fila.idRecaudacion(), idRecaudacionEmpleadorFila));
                salidaMensajes.add(msg);
                salidaIdRecEmp[0] = idRecaudacionEmpleadorFila;

                List<ParametrosRecaudacionEstado> paramsCambioEstado = new ArrayList<>();
                HashMap<String, Object> param = new HashMap<>();
                param.put("IdRecaudacionEmpleador", idRecaudacionEmpleadorFila);
                List<DetalleCliente> detalleClientes = sql.consultar(SQL_DETALLE_CLIENTES_POR_DETALLE_EMPLEADOR, param, DetalleCliente.class);

                paramsCambioEstado.add(new ParametrosRecaudacionEstado(fila.idRecaudacion(), idRecaudacionEmpleadorFila, 0L, "ING"));
                for (DetalleCliente detCli : detalleClientes) {
                    paramsCambioEstado.add(new ParametrosRecaudacionEstado(0L, 0L, detCli.IdRecaudacionCliente, "ING"));
                }
                // the legacy ignores the answer of the state change
                cambiarEstadoRecaudacion.ejecutar(new CambiarEstadoRecaudacionEntrada(in.idEmpresa(), in.idUsuario(), in.idModulo(), paramsCambioEstado));
            } else {
                msg.id = 1L;
                msg.tipo = "APV";
                msg.codigo = "ERROR";
                msg.descripcion = in.tipo().compareTo("M") == 0 ? "Problemas al modificar Detalle Empleador" : "Problemas al crear Detalle Empleador";
                salidaMensajes.add(msg);
            }
            contadorIds++;
        }
    }
}
