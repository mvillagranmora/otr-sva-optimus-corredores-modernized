package cl.bcs.apv.application.recaudacion;

import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.bcs.apv.api.casouso.CargarRecaudacion;
import cl.bcs.apv.api.casouso.CrearRecaudacionDeposito;
import cl.bcs.apv.api.dto.cargarrecaudacion.CargaArchivo;
import cl.bcs.apv.api.dto.cargarrecaudacion.CargarRecaudacionEntrada;
import cl.bcs.apv.api.dto.cargarrecaudacion.CargarRecaudacionResultado;
import cl.bcs.apv.api.dto.cargarrecaudacion.Recaudacion;
import cl.bcs.apv.api.dto.crearrecaudaciondeposito.CrearRecaudacionDepositoEntrada;
import cl.bcs.apv.api.dto.crearrecaudaciondeposito.CrearRecaudacionDepositoResultado;
import cl.bcs.apv.domain.port.out.AlmacenArchivos;
import cl.bcs.apv.domain.port.out.Colaboradores;
import cl.bcs.apv.domain.port.out.ConfiguracionSistema;
import cl.bcs.apv.domain.port.out.DiccionarioMensajes;
import cl.bcs.apv.domain.port.out.Reloj;
import cl.bcs.apv.domain.port.out.Secuencias;

/** Use case {@code CargarRecaudacion} (family C): registers a deposit load file and creates its deposits. Legacy {@code cl.bcs.APV.CargaRecaudacion}. */
@Service
@Transactional(rollbackFor = Exception.class)
public class CargarRecaudacionServicio implements CargarRecaudacion {

    private static final String SQL_INSERT_CARGA_ARCHIVO = SqlLegacy.recurso("CargaMasivaArchivo_Insert");

    private final SqlLegacy sql;
    private final ConfiguracionSistema configuracion;
    private final Secuencias secuencias;
    private final CrearRecaudacionDeposito crearRecaudacionDeposito;

    public CargarRecaudacionServicio(NamedParameterJdbcTemplate jdbc, ConfiguracionSistema configuracion, DiccionarioMensajes diccionario,
            Secuencias secuencias, Reloj reloj, AlmacenArchivos archivos, Colaboradores colaboradores, CrearRecaudacionDeposito crearRecaudacionDeposito) {
        this.sql = new SqlLegacy(jdbc);
        this.configuracion = configuracion;
        this.secuencias = secuencias;
        this.crearRecaudacionDeposito = crearRecaudacionDeposito;
    }

    /** Row of the load header insert: public fields are the SQL parameter names (legacy {@code CargaRecaudacion.CargaArchivo}). */
    public static class FilaCargaArchivo {
        public Long IdEmpresa;
        public Long IdCargaMasiva;
        public String NombreCargaMasiva;
        public Long CantidadRegistros;
        public Long IdUsuario;
        public int IdTipoCarga;
        public String Descripcion;
        public String FechaCarga;
        public String IdEstado;
    }

    @Override
    public CargarRecaudacionResultado ejecutar(CargarRecaudacionEntrada in) {
        try {
            List<Long> ids = new ArrayList<>();
            List<MensajeLegacy> mensajes = new ArrayList<>();
            logica(in, ids, mensajes);
            return new CargarRecaudacionResultado(MensajeLegacy.aMensajes(mensajes), MensajeLegacy.contarErrores(mensajes), ids);
        } catch (Exception e) {
            throw Sneaky.lanzar(e);
        }
    }

    private void logica(CargarRecaudacionEntrada in, List<Long> idsSalida, List<MensajeLegacy> salida) throws Exception {
        String fechaSistema = Plataforma.fechaSistema(configuracion, in.idEmpresa());
        MensajeLegacy msg = new MensajeLegacy();
        try {
            if (in.archivo().size() > 0) {
                List<Long> idsCargaArchivo = Colab.secuencia(secuencias, "BSCL.APV.BD.Secuencia::APV.IdCargaMasiva", (long) in.archivo().size());
                List<FilaCargaArchivo> filas = new ArrayList<>();
                for (CargaArchivo a : in.archivo()) {
                    FilaCargaArchivo f = new FilaCargaArchivo();
                    f.IdEmpresa = a.idEmpresa();
                    f.IdCargaMasiva = a.idCargaMasiva();
                    f.NombreCargaMasiva = a.nombreCargaMasiva();
                    f.CantidadRegistros = a.cantidadRegistros();
                    f.IdUsuario = a.idUsuario();
                    f.IdTipoCarga = a.idTipoCarga();
                    f.Descripcion = a.descripcion();
                    f.FechaCarga = a.fechaCarga();
                    f.IdEstado = a.idEstado();
                    filas.add(f);
                }
                filas.get(0).IdCargaMasiva = idsCargaArchivo.get(0);
                filas.get(0).FechaCarga = fechaSistema;

                int[] res = CargasSql.lote(sql, SQL_INSERT_CARGA_ARCHIVO, filas);
                if (res != null && res.length > 0 && res[0] == 1) {
                    msg.id = 1L;
                    msg.tipo = "APV";
                    msg.codigo = "CARGA_MASIVA_OK";
                    msg.descripcion = "Carga masiva de recaudacion exitosa.";
                    idsSalida.add(idsCargaArchivo.get(0));
                    salida.add(msg);

                    if (in.parametrosRecaudacion().size() > 0) {
                        List<cl.bcs.apv.api.dto.crearrecaudaciondeposito.Recaudacion> deps = new ArrayList<>();
                        for (Recaudacion p : in.parametrosRecaudacion()) {
                            deps.add(new cl.bcs.apv.api.dto.crearrecaudaciondeposito.Recaudacion(p.idInterno(), p.idRecaudacion(), p.idEmpresa(),
                                    p.codigoArchivo(), p.folioCartola(), p.lineaCartola(), p.sucursal(), p.fechaDeposito(), p.idCuentaBanco(),
                                    p.idBanco(), p.cuentaCorriente(), p.montoDeposito(), p.conceptoRecaudacion(), p.origen(), p.observacion(),
                                    p.numeroDocumento(), p.idTipoDeposito(), p.idRegimenTributario(), idsCargaArchivo.get(0), p.idEstado()));
                        }
                        CrearRecaudacionDepositoResultado paramsalida = crearRecaudacionDeposito
                                .ejecutar(new CrearRecaudacionDepositoEntrada(in.idEmpresa(), in.idUsuario(), in.idModulo(), "I", "CARGA MASIVA", deps));
                        // LEGACY DEFECT kept (see README APV-C-210..216): the counter test is never true, the deposit messages are never copied
                        if (paramsalida.contadorErrores() == null) {
                            for (cl.bcs.apv.api.comun.Mensaje mensaje : paramsalida.mensajes()) {
                                msg.id = mensaje.id();
                                msg.tipo = mensaje.tipo();
                                msg.codigo = mensaje.codigo();
                                msg.descripcion = mensaje.descripcion();
                                salida.add(msg);
                            }
                        }
                    } else {
                        msg.id = 3L;
                        msg.tipo = "APV";
                        msg.codigo = "CARGA_DEPOSITO_ERROR";
                        msg.descripcion = "La tabla de deposito viene vacia.";
                        salida.add(msg);
                    }
                } else {
                    msg.id = 4L;
                    msg.tipo = "APV";
                    msg.codigo = "CARGA_MASIVA_ERROR";
                    msg.descripcion = "Carga masiva de recaudacion con problemas de ingreso.";
                    salida.add(msg);
                }
            }
        } catch (Exception error) {
            // LEGACY DEFECT kept (see README APV-C-210..216): swallowed exceptions become message id 5 and share the one message instance
            msg.id = 5L;
            msg.tipo = "APV";
            msg.codigo = "CARGA_ERROR_JSON";
            msg.descripcion = Sneaky.causa(error).getMessage();
            salida.add(msg);
        }
    }
}
