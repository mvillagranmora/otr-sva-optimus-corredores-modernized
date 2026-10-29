package cl.bcs.apv.application.recaudacion;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.bcs.apv.api.casouso.CargarArchivoBanco;
import cl.bcs.apv.api.casouso.CrearRecaudacionDeposito;
import cl.bcs.apv.api.dto.cargararchivobanco.ArchivoBanco;
import cl.bcs.apv.api.dto.cargararchivobanco.CargaArchivo;
import cl.bcs.apv.api.dto.cargararchivobanco.CargarArchivoBancoEntrada;
import cl.bcs.apv.api.dto.cargararchivobanco.CargarArchivoBancoResultado;
import cl.bcs.apv.api.dto.crearrecaudaciondeposito.CrearRecaudacionDepositoEntrada;
import cl.bcs.apv.api.dto.crearrecaudaciondeposito.CrearRecaudacionDepositoResultado;
import cl.bcs.apv.api.dto.crearrecaudaciondeposito.Recaudacion;
import cl.bcs.apv.domain.port.out.AlmacenArchivos;
import cl.bcs.apv.domain.port.out.Colaboradores;
import cl.bcs.apv.domain.port.out.ConfiguracionSistema;
import cl.bcs.apv.domain.port.out.DiccionarioMensajes;
import cl.bcs.apv.domain.port.out.Reloj;
import cl.bcs.apv.domain.port.out.Secuencias;

/**
 * Use case {@code CargarArchivoBanco} (family C): loads a bank (Previred "banco") file creating one deposit per row. Legacy
 * {@code cl.bcs.APV.CargaMasivaBanco}. (The legacy class never calls ValidarBancoPrevired.)
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class CargarArchivoBancoServicio implements CargarArchivoBanco {

    private static final String SQL_INSERT_CARGA_ARCHIVO = SqlLegacy.recurso("CargaMasivaArchivo_Insert");
    private static final String SQL_SUCURSAL_POR_ID = SqlLegacy.recurso("SucursalPorId");
    private static final String SQL_CODIGOBANCO_PREVIRED_POR_IDBANCO = SqlLegacy.recurso("CodigoBancoPreviredPorId");
    private static final String SQL_EXISTEN_EMPLEADORES = SqlLegacy.recurso("ConsultaExistenEmpleadores");
    private static final String SQL_DEPOSITOS_PREVIRED_POR_NOMBRE_COMPLETO_ARCHIVO = SqlLegacy.recurso("ConsultarRecaudacionPorNombreCompletoArchivo");

    private final SqlLegacy sql;
    private final ConfiguracionSistema configuracion;
    private final Secuencias secuencias;
    private final CrearRecaudacionDeposito crearRecaudacionDeposito;

    public CargarArchivoBancoServicio(NamedParameterJdbcTemplate jdbc, ConfiguracionSistema configuracion, DiccionarioMensajes diccionario,
            Secuencias secuencias, Reloj reloj, AlmacenArchivos archivos, Colaboradores colaboradores, CrearRecaudacionDeposito crearRecaudacionDeposito) {
        this.sql = new SqlLegacy(jdbc);
        this.configuracion = configuracion;
        this.secuencias = secuencias;
        this.crearRecaudacionDeposito = crearRecaudacionDeposito;
    }

    public static class ExisteEmpleador {
        public String DocumentoIdentidad;
        public Long IdPersona;
    }

    public static class Sucursal {
        public Long IdSucursal;
        public String NombreSucursal;
        public Long IdBanco;
        public String IdEstado;
    }

    public static class CodigoBanco {
        public Long IdBanco;
        public String CodigoBancoPrevired;
    }

    /** Deposit already loaded for the file (legacy {@code CrearRecaudacionDeposito.Recaudacion}, only to count rows). */
    public static class DepositoFila {
        public Long IdRecaudacion;
    }

    /** Legacy output object: the counter starts at 0 and is NOT recomputed by the legacy {@code call}. */
    private static final class Salida {
        final List<MensajeLegacy> mensajes = new ArrayList<>();
        Long contador = 0L;
    }

    @Override
    public CargarArchivoBancoResultado ejecutar(CargarArchivoBancoEntrada in) {
        try {
            Salida salida = logica(in);
            return new CargarArchivoBancoResultado(MensajeLegacy.aMensajes(salida.mensajes), salida.contador);
        } catch (Exception e) {
            throw Sneaky.lanzar(e);
        }
    }

    private Salida logica(CargarArchivoBancoEntrada in) throws Exception {
        String fechaSistema = Plataforma.parametro(configuracion, in.idEmpresa(), "PLATAFORMA", "PLA.FechaSistema");
        Salida salida = new Salida();
        HashMap<String, Object> param;
        MensajeLegacy msg = new MensajeLegacy();
        List<Long> idsCargaArchivo = Colab.secuencia(secuencias, "BSCL.APV.BD.Secuencia::APV.IdCargaMasiva", (long) in.archivo().size());

        if (!in.nombreCarga().matches("[a-zA-Z0-9]+-[a-zA-Z0-9]+_[a-zA-Z0-9]+_[a-zA-Z0-9]+.[a-zA-Z0-9]+")) {
            msg.id = 1L;
            msg.tipo = "ERROR";
            msg.codigo = "FORMATO_NOMBRE_INVALIDO";
            msg.descripcion = "Error nombre del archivo no es valido.";
            salida.mensajes.add(msg);
            // LEGACY DEFECT kept (see README APV-C-180..197): an invalid file name message does not increment the counter
            return salida;
        }

        param = new HashMap<>();
        param.put("NombreCargaArchivo", in.nombreCarga());
        List<DepositoFila> depositosPorNombreCargaArchivo = sql.consultar(SQL_DEPOSITOS_PREVIRED_POR_NOMBRE_COMPLETO_ARCHIVO, param, DepositoFila.class);

        if (depositosPorNombreCargaArchivo.size() == 0) {
            param = new HashMap<>();
            // IdSucursal 7 es el Id de la sucursal banca en linea.
            param.put("p_IdSucursal", 7);

            List<Sucursal> sucursal = sql.consultar(SQL_SUCURSAL_POR_ID, param, Sucursal.class);
            param.clear();
            if (sucursal.size() == 0) {
                msg.id = 1L;
                msg.tipo = "ERROR";
                msg.codigo = "SUCURSAL_ERROR";
                msg.descripcion = "No se encontro la Sucursal Banca en Linea.";
                salida.mensajes.add(msg);
                return salida;
            }
            List<Recaudacion> paramsRecaudacion = new ArrayList<Recaudacion>();

            Long numeroReferencial = 0L;

            HashMap<String, Object> params = new HashMap<String, Object>();
            List<String> rutsEmpleadores = new ArrayList<String>();
            List<ExisteEmpleador> empleadoresExistentes = new ArrayList<ExisteEmpleador>();
            for (ArchivoBanco fila : in.parametros()) {
                if ("OK".equalsIgnoreCase(fila.mensajeValidacion())) {
                    Long rutLongEmpleador = Long.parseLong(fila.rutPagador().substring(0, fila.rutPagador().length() - 1));
                    String rutEmpleador = rutLongEmpleador.toString() + "-" + fila.rutPagador().substring(fila.rutPagador().length() - 1, fila.rutPagador().length());
                    rutsEmpleadores.add(rutEmpleador);
                }
            }

            if (rutsEmpleadores.size() > 0) {
                params.put("p_DoctosIdentidad", rutsEmpleadores);
                empleadoresExistentes = sql.consultar(SQL_EXISTEN_EMPLEADORES, params, ExisteEmpleador.class);
                params.clear();
            }
            if (empleadoresExistentes.size() == 0) {
                MensajeLegacy msg1 = new MensajeLegacy((long) numeroReferencial, "ERROR", "ERROR", "Los Empleadores del archivo no existen en el sistema.");
                salida.mensajes.add(msg1);
                salida.contador++;
                return salida;
            } else {
                if (empleadoresExistentes.size() != rutsEmpleadores.size()) {
                    List<String> strRutsExts = new ArrayList<String>();
                    for (ExisteEmpleador emp : empleadoresExistentes) {
                        strRutsExts.add(emp.DocumentoIdentidad);
                    }
                    for (String rutEmpleador : rutsEmpleadores) {
                        if (!strRutsExts.contains(rutEmpleador)) {
                            numeroReferencial++;
                            MensajeLegacy msg1 = new MensajeLegacy((long) numeroReferencial, "ERROR", "ERROR",
                                    "El Empleador RUT : " + rutEmpleador + " no existe en el sistema.");
                            salida.mensajes.add(msg1);
                            salida.contador++;
                        }
                    }
                    if (salida.contador > 0) {
                        return salida;
                    }
                }
            }

            for (ArchivoBanco fila : in.parametros()) {
                numeroReferencial++;

                param = new HashMap<>();
                param.put("p_CodigoBanco", fila.idBanco().trim());

                List<CodigoBanco> codigoBanco = sql.consultar(SQL_CODIGOBANCO_PREVIRED_POR_IDBANCO, param, CodigoBanco.class);
                param.clear();
                if (codigoBanco.size() == 0) {
                    msg.id = 1L;
                    msg.tipo = "ERROR";
                    msg.codigo = "CODIGOBANCO_PREVIRED_ERROR";
                    msg.descripcion = "El Banco: " + fila.idBanco() + " no esta registrado en el sistema";
                    salida.mensajes.add(msg);
                    return salida;
                }

                // LEGACY DEFECT kept (see README APV-C-180..197): every row (even with MENSAJEVALIDACION != OK) becomes a deposit
                paramsRecaudacion.add(new Recaudacion(0L, 0L, in.idEmpresa(), in.nombreCarga().split("_")[2].split("\\.")[0], fila.folio(), 1L,
                        String.valueOf(sucursal.get(0).IdSucursal), fila.fechaAbono(), 0L, codigoBanco.get(0).IdBanco,
                        fila.cuentaCorrienteInstitucion().replaceFirst("^0+", "").replaceAll("\\s+$", ""),
                        BigDecimal.valueOf(Long.parseLong(fila.montoAbono())), "", in.origen(), "", 1L, "TRA", 0L, idsCargaArchivo.get(0), "ING"));
            }

            try {
                CrearRecaudacionDepositoResultado outDep = crearRecaudacionDeposito
                        .ejecutar(new CrearRecaudacionDepositoEntrada(in.idEmpresa(), in.idUsuario(), "APV", "I", in.origen(), paramsRecaudacion));
                for (cl.bcs.apv.api.comun.Mensaje m : outDep.mensajes()) {
                    salida.mensajes.add(MensajeLegacy.de(m));
                }
                salida.contador = outDep.contadorErrores();
                if (salida.contador > 0) {
                    return salida;
                }
            } catch (Exception er) {
                // LEGACY DEFECT kept (see README APV-C-180..197): the exception is swallowed after a rollback and the CargaMasiva row is still written
                sql.rollback();
            }

            List<FilaCargaMasiva> filas = new ArrayList<>();
            for (CargaArchivo a : in.archivo()) {
                filas.add(new FilaCargaMasiva(a.idEmpresa(), a.idCargaMasiva(), a.nombreCargaMasiva(), a.cantidadRegistros(), a.idUsuario(),
                        a.idTipoCarga(), a.descripcion(), a.fechaCarga(), a.idEstado()));
            }
            filas.get(0).IdCargaMasiva = idsCargaArchivo.get(0);
            filas.get(0).FechaCarga = fechaSistema;

            sql.lote(SQL_INSERT_CARGA_ARCHIVO, filas);
        } else {
            msg.id = 1L;
            msg.tipo = "ERROR";
            msg.codigo = "ERROR";
            msg.descripcion = "El archivo " + in.nombreCarga() + " ya fue cargado previamente.";
            salida.mensajes.add(msg);
        }
        return salida;
    }
}
