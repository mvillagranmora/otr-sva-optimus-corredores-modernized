package cl.bcs.apv.application.recaudacion;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.bcs.apv.api.casouso.CargarArchivoPrevired;
import cl.bcs.apv.api.casouso.CrearRecaudacionCliente;
import cl.bcs.apv.api.casouso.CrearRecaudacionEmpleador;
import cl.bcs.apv.api.comun.Mensaje;
import cl.bcs.apv.api.dto.cargararchivoprevired.ArchivoPrevired;
import cl.bcs.apv.api.dto.cargararchivoprevired.CargaArchivo;
import cl.bcs.apv.api.dto.cargararchivoprevired.CargarArchivoPreviredEntrada;
import cl.bcs.apv.api.dto.cargararchivoprevired.CargarArchivoPreviredResultado;
import cl.bcs.apv.api.dto.crearrecaudacioncliente.Cliente;
import cl.bcs.apv.api.dto.crearrecaudacioncliente.CrearRecaudacionClienteEntrada;
import cl.bcs.apv.api.dto.crearrecaudacioncliente.CrearRecaudacionClienteResultado;
import cl.bcs.apv.api.dto.crearrecaudacionempleador.CrearRecaudacionEmpleadorEntrada;
import cl.bcs.apv.api.dto.crearrecaudacionempleador.CrearRecaudacionEmpleadorResultado;
import cl.bcs.apv.api.dto.crearrecaudacionempleador.Empleador;
import cl.bcs.apv.domain.port.out.AlmacenArchivos;
import cl.bcs.apv.domain.port.out.Colaboradores;
import cl.bcs.apv.domain.port.out.ConfiguracionSistema;
import cl.bcs.apv.domain.port.out.DiccionarioMensajes;
import cl.bcs.apv.domain.port.out.Reloj;
import cl.bcs.apv.domain.port.out.Secuencias;

/**
 * Use case {@code CargarArchivoPrevired} (family C): loads a Previred collection file creating employer and client detail rows over the deposits of the
 * bank file. Legacy {@code cl.bcs.APV.CargaMasivaPrevired}.
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class CargarArchivoPreviredServicio implements CargarArchivoPrevired {

    private static final String SQL_DEPOSITOS_POR_CODIGO_ARCHIVO = SqlLegacy.recurso("ConsultarCodigoArchivoRecaudacion");
    private static final String SQL_EXISTE_EMPLEADOR = SqlLegacy.recurso("ConsultaExisteEmpleador");
    private static final String SQL_EXISTE_EMPLEADOR_RECAUDACION = SqlLegacy.recurso("ConsultarExisteEmpleadorRecaudacion");
    private static final String SQL_EXISTE_CLIENTE = SqlLegacy.recurso("ConsultarExisteCliente");
    private static final String SQL_INSERT_CARGA_ARCHIVO = SqlLegacy.recurso("CargaMasivaArchivo_Insert");
    private static final String SQL_CONSULTAR_CARGA_ARCHIVO_POR_NOMBRE = SqlLegacy.recurso("ConsultarRecaudacionesEmpleadorPorNombreCompletoArchivo");

    private final SqlLegacy sql;
    private final ConfiguracionSistema configuracion;
    private final Secuencias secuencias;
    private final Colaboradores colaboradores;
    private final CrearRecaudacionCliente crearRecaudacionCliente;
    private final CrearRecaudacionEmpleador crearRecaudacionEmpleador;

    public CargarArchivoPreviredServicio(NamedParameterJdbcTemplate jdbc, ConfiguracionSistema configuracion, DiccionarioMensajes diccionario,
            Secuencias secuencias, Reloj reloj, AlmacenArchivos archivos, Colaboradores colaboradores, CrearRecaudacionCliente crearRecaudacionCliente,
            CrearRecaudacionEmpleador crearRecaudacionEmpleador) {
        this.sql = new SqlLegacy(jdbc);
        this.configuracion = configuracion;
        this.secuencias = secuencias;
        this.colaboradores = colaboradores;
        this.crearRecaudacionCliente = crearRecaudacionCliente;
        this.crearRecaudacionEmpleador = crearRecaudacionEmpleador;
    }

    // ---------------------------------------------------------------------------------------------- legacy row classes

    /** Deposit of the bank file (legacy {@code CrearRecaudacionDeposito.Recaudacion}, only the columns this class reads). */
    public static class DepositoFila {
        public Long IdRecaudacion;
        public Long FolioCartola;
    }

    public static class ExistePersona {
        public Long IdRecaudacionEmpleador;
        public String DocumentoIdentidad;
        public Long IdPersona;
    }

    public static class ExisteEmpleador {
        public Long IdPersona;
        public String PersonaBusqueda;
    }

    public static class ExisteCliente {
        public Long IdCliente;
    }

    public static class ConsultaCargaArchivoExiste {
        public Long IdCargaMasiva;
        public String NombreCargaArchivo;
        public Long CantidadRegistros;
        public Long IdUsuario;
        public Long IdTipoCarga;
        public String Descripcion;
        public String FechaCarga;
        public String IdEstado;
    }

    /** Legacy {@code AgregarLog.InAgregarLog} (contract of the collaborator AgregarLog), only what this class fills. */
    public static class InAgregarLog {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_UsaReglasNegocio;
        public String p_IdEstadoAutorizacion;
        public Long p_IdTrabajo = 0L;
        public List<Log> p_Log = new ArrayList<>();
        public List<Object> p_LogDetalle = new ArrayList<>();
    }

    public static class Log {
        public Long IdTrabajo = 0L;
        public Long IdCabeceraLog;
        public String IdTipoLog;
        public String IdModulo;
        public Long IdUsuario;
        public Long IdEmpresa;
        public String FechaSistemaLog;
        public Long IdUsuarioAtendido;
        public Long IdCliente = 0L;
        public String EstadoCliente = "";
        public String NombreCliente = "";
        public String IdTipoDocumentoIdentidad = "";
        public String IdDocumentoIdentidad = "";
        public Long IdPortafolio = 0L;
        public String EstadoPortafolio = "";
        public String IdTipoRegistro;
        public String IdRegistro = "";
        public String EstadoRegistro = "";
        public String IdTipoOperacion = "";
        public Long Folio = 0L;
        public BigDecimal Cantidad = BigDecimal.ZERO;
        public BigDecimal PrecioTasa = BigDecimal.ZERO;
        public BigDecimal Monto = BigDecimal.ZERO;
        public String IdMonedaOrigen = "";
        public String IdMonedaLiquidacion = "";
        public String TipoCambio = "";
        public String Nemo = "";
        public Long IdProceso = 0L;
        public String EstadoProceso = "";
        public String NombreProceso = "";
        public Long IdPersona = 0L;
    }

    /** Legacy output object: the counter starts at 0 and is NOT recomputed by the legacy {@code call}. */
    private static final class Salida {
        final List<MensajeLegacy> mensajes = new ArrayList<>();
        Long contador = 0L;
    }

    @Override
    public CargarArchivoPreviredResultado ejecutar(CargarArchivoPreviredEntrada in) {
        try {
            Salida salida = logica(in);
            return new CargarArchivoPreviredResultado(MensajeLegacy.aMensajes(salida.mensajes), salida.contador);
        } catch (Exception e) {
            throw Sneaky.lanzar(e);
        }
    }

    private static MensajeLegacy mensaje(Salida salida, String tipo, String codigo, String descripcion) {
        MensajeLegacy m = new MensajeLegacy();
        m.id = salida.mensajes.size() + 1L;
        m.tipo = tipo;
        m.codigo = codigo;
        m.descripcion = descripcion;
        return m;
    }

    private Salida logica(CargarArchivoPreviredEntrada in) throws Exception {
        Salida salida = new Salida();
        HashMap<String, Object> param;
        Long idRecaudacionEmpleador = 0L;
        param = new HashMap<>();

        String codigoArchivo = "";

        String[] codigoArchivoExtencion = in.nombreCarga().split("_");

        if (codigoArchivoExtencion.length > 2) {
            String[] codigoArchivoList = codigoArchivoExtencion[2].split("\\.");
            if (codigoArchivoList.length > 0) {
                codigoArchivo = codigoArchivoList[0];
            } else {
                MensajeLegacy msg = new MensajeLegacy(1L, "ERROR", "ERROR", "Formato Nombre de Archivo Incorrecto");
                salida.mensajes.add(msg);
                salida.contador++;
                return salida;
            }
        } else {
            MensajeLegacy msg = new MensajeLegacy(1L, "ERROR", "ERROR", "Formato Nombre de Archivo Incorrecto");
            salida.mensajes.add(msg);
            salida.contador++;
            return salida;
        }

        param.put("CodigoArchivo", codigoArchivo);
        List<DepositoFila> depositosPorCodigo = sql.consultar(SQL_DEPOSITOS_POR_CODIGO_ARCHIVO, param, DepositoFila.class);

        if (depositosPorCodigo.size() > 0) {
            param = new HashMap<>();
            param.put("NombreCargaArchivo", in.nombreCarga());
            List<ConsultaCargaArchivoExiste> depositosPorCodigoEmpleador = sql.consultar(SQL_CONSULTAR_CARGA_ARCHIVO_POR_NOMBRE, param,
                    ConsultaCargaArchivoExiste.class);

            if (depositosPorCodigoEmpleador.size() == 0) {
                String fechaSistema = Plataforma.parametro(configuracion, in.idEmpresa(), "PLATAFORMA", "PLA.FechaSistema");
                List<Long> idsCargaArchivo = Colab.secuencia(secuencias, "BSCL.APV.BD.Secuencia::APV.IdCargaMasiva", (long) in.archivo().size());

                String documentoIdentidadAnterior = "";
                Long idRecaudacionEmpleadorAnterior = 0L;
                Long idPersonaEmpleadorAnterior = 0L;
                Long folioRecaudacioAnterior = 0L;

                Long idPersona = 0L;
                Long idRecaudacionDeposito = 0L;

                List<Cliente> paramsCliente = new ArrayList<Cliente>();

                Long empIngresados = 0L;
                Long empRechazados = 0L;
                Long cliIngresados = 0L;
                Long cliRechazados = 0L;

                for (ArchivoPrevired fila : in.parametros()) {
                    // EMPLEADOR
                    Long rutLong = Long.parseLong(fila.rutEmpleador().substring(0, fila.rutEmpleador().length() - 1));
                    String rutPer = rutLong.toString() + "-" + fila.rutEmpleador().substring(fila.rutEmpleador().length() - 1, fila.rutEmpleador().length());

                    if (documentoIdentidadAnterior.equals(rutPer) == false) {
                        param = new HashMap<>();
                        param.put("p_DoctoIdentidad", rutPer);
                        List<ExisteEmpleador> existeEmpleador = sql.consultar(SQL_EXISTE_EMPLEADOR, param, ExisteEmpleador.class);

                        if (existeEmpleador.size() == 0) {
                            salida.mensajes.add(mensaje(salida, "ADVERTENCIA", "ADVERTENCIA", "El Empleador con Documento Identidad: " + rutPer
                                    + " no es un Empleador valido. Detalle Empleador y Detalle Cliente no ingresados."));
                            documentoIdentidadAnterior = rutPer;
                            idRecaudacionEmpleadorAnterior = 0L;
                            idPersonaEmpleadorAnterior = 0L;
                            empRechazados++;
                            cliRechazados++;
                            continue;
                        }
                        if (existeEmpleador.size() == 1) {
                            idPersona = existeEmpleador.get(0).IdPersona;
                        } else {
                            for (ExisteEmpleador emp : existeEmpleador) {
                                if (emp.PersonaBusqueda.equalsIgnoreCase(rutPer.trim() + " " + fila.razonSocialEmpleador().trim())) {
                                    idPersona = emp.IdPersona;
                                }
                            }
                        }

                        param = new HashMap<>();
                        param.put("p_IdEmpresa", in.idEmpresa());
                        param.put("p_DoctoIdentidad", rutPer);
                        List<ExistePersona> existeDetalleEmpleador = sql.consultar(SQL_EXISTE_EMPLEADOR_RECAUDACION, param, ExistePersona.class);

                        idRecaudacionDeposito = 0L;
                        idRecaudacionDeposito = obtenerIdRecaudacionDeposito(depositosPorCodigo, idRecaudacionDeposito, fila);

                        Empleador emp = new Empleador(0L, 0L, idRecaudacionDeposito, in.nombreCarga().split("_")[2].split("\\.")[0], in.idEmpresa(),
                                existeDetalleEmpleador.size() == 0 ? idPersona : existeDetalleEmpleador.get(0).IdPersona, fechaSistema,
                                fila.periodoPago().substring(0, 4) + "-" + fila.periodoPago().substring(4, 6), 1L, "DEP", "Normal", "",
                                fila.montoTotalDC(), fila.montoTotalAPV(), 0L, 0L, "CARGA", "", "ING");
                        CrearRecaudacionEmpleadorEntrada entradaEmp = new CrearRecaudacionEmpleadorEntrada(in.idEmpresa(), in.idUsuario(), "APV", "I",
                                "CARGA", new ArrayList<>(List.of(emp)));

                        try {
                            CrearRecaudacionEmpleadorResultado outEmp = crearRecaudacionEmpleador.ejecutar(entradaEmp);

                            for (Mensaje filaMsg : outEmp.mensajes()) {
                                salida.mensajes.add(mensaje(salida, "INFORMACION", "INFORMACION", filaMsg.descripcion()));
                            }

                            idRecaudacionEmpleador = outEmp.idRecEmp();
                            documentoIdentidadAnterior = rutPer;
                            idRecaudacionEmpleadorAnterior = outEmp.idRecEmp();
                            idPersonaEmpleadorAnterior = emp.idPersona();
                            folioRecaudacioAnterior = fila.folio();
                            empIngresados++;
                        } catch (Exception er) {
                            // LEGACY DEFECT kept (see README APV-C-110..131): any failure of the employer creation is swallowed as a message
                            salida.mensajes.add(mensaje(salida, "ERROR", "ERROR", "Error al crear empleador"));
                            salida.contador++;
                            documentoIdentidadAnterior = rutPer;
                            idRecaudacionEmpleadorAnterior = 0L;
                            idPersonaEmpleadorAnterior = 0L;
                        }
                    } else {
                        if (idRecaudacionEmpleadorAnterior == 0) {
                            salida.mensajes.add(mensaje(salida, "ADVERTENCIA", "ADVERTENCIA", "El Empleador con Documento Identidad: " + rutPer
                                    + " no es un Empleador valido. Detalle Empleador y Detalle Cliente no ingresados."));
                            cliRechazados++;
                            continue;
                        } else {
                            // Si el Folio Deposito coincide, usamos el IdRecaudacionEmpleador anterior sino creamos uno nuevo
                            if (folioRecaudacioAnterior.equals(fila.folio())) {
                                idRecaudacionEmpleador = idRecaudacionEmpleadorAnterior;
                            } else {
                                idRecaudacionDeposito = obtenerIdRecaudacionDeposito(depositosPorCodigo, idRecaudacionDeposito, fila);
                                Empleador emp = new Empleador(0L, 0L, idRecaudacionDeposito, in.nombreCarga().split("_")[2].split("\\.")[0], in.idEmpresa(),
                                        idPersonaEmpleadorAnterior, fechaSistema,
                                        fila.periodoPago().substring(0, 4) + "-" + fila.periodoPago().substring(4, 6), 1L, "DEP", "Normal", "",
                                        fila.montoTotalDC(), fila.montoTotalAPV(), 0L, 0L, "CARGA", "", "ING");
                                CrearRecaudacionEmpleadorEntrada entradaEmp = new CrearRecaudacionEmpleadorEntrada(in.idEmpresa(), in.idUsuario(), "APV",
                                        "I", "CARGA", new ArrayList<>(List.of(emp)));

                                try {
                                    CrearRecaudacionEmpleadorResultado outEmp = crearRecaudacionEmpleador.ejecutar(entradaEmp);

                                    for (Mensaje filaMsg : outEmp.mensajes()) {
                                        salida.mensajes.add(mensaje(salida, "INFORMACION", "INFORMACION", filaMsg.descripcion()));
                                    }

                                    idRecaudacionEmpleador = outEmp.idRecEmp();
                                    documentoIdentidadAnterior = rutPer;
                                    idRecaudacionEmpleadorAnterior = outEmp.idRecEmp();
                                    folioRecaudacioAnterior = fila.folio();
                                    idPersonaEmpleadorAnterior = emp.idPersona();
                                    empIngresados++;
                                } catch (Exception er) {
                                    salida.mensajes.add(mensaje(salida, "ERROR", "ERROR", "Error al crear empleador"));
                                    salida.contador++;
                                    documentoIdentidadAnterior = rutPer;
                                    idRecaudacionEmpleadorAnterior = 0L;
                                }
                            }
                        }
                    }

                    // CLIENTE

                    // Valida si existe Cliente.
                    idPersona = 0L;
                    Long rutCliLong = Long.parseLong(fila.rutTrabajador().substring(0, fila.rutTrabajador().length() - 1));
                    String ruttrab = rutCliLong + "-" + fila.rutTrabajador().substring(fila.rutTrabajador().length() - 1, fila.rutTrabajador().length());

                    param = new HashMap<>();
                    param.put("DocumentoIdentidad", ruttrab);

                    List<ExisteCliente> existeCliente = sql.consultar(SQL_EXISTE_CLIENTE, param, ExisteCliente.class);
                    param.clear();
                    if (existeCliente.size() > 0) {
                        // PARAMETROS CLIENTE
                        // LEGACY DEFECT kept (see README APV-C-110..131): BigDecimal compared with != (reference): works only because of the BigDecimal cache of valueOf(0)
                        if (BigDecimal.valueOf(fila.montoAPV()) != BigDecimal.valueOf(0) && BigDecimal.valueOf(fila.montoDC()) != BigDecimal.valueOf(0)) {
                            paramsCliente.add(new Cliente(0L, 0L, idRecaudacionDeposito, idRecaudacionEmpleador, in.idEmpresa(),
                                    existeCliente.get(0).IdCliente, fechaSistema, "DEP", "APV", BigDecimal.valueOf(0),
                                    BigDecimal.valueOf(fila.montoAPV()), "CARGA", "ING"));
                            paramsCliente.add(new Cliente(0L, 0L, idRecaudacionDeposito, idRecaudacionEmpleador, in.idEmpresa(),
                                    existeCliente.get(0).IdCliente, fechaSistema, "DEP", "DC", BigDecimal.valueOf(fila.montoDC()),
                                    BigDecimal.valueOf(0), "CARGA", "ING"));
                        } else {
                            String regimen;
                            if (BigDecimal.valueOf(fila.montoAPV()).compareTo(BigDecimal.valueOf(0)) > 0) {
                                regimen = "APV";
                            } else if (BigDecimal.valueOf(fila.montoDC()).compareTo(BigDecimal.valueOf(0)) > 0) {
                                regimen = "DC";
                            } else {
                                regimen = "";
                            }
                            paramsCliente.add(new Cliente(0L, 0L, idRecaudacionDeposito, idRecaudacionEmpleador, in.idEmpresa(),
                                    existeCliente.get(0).IdCliente, fechaSistema, "DEP", regimen, BigDecimal.valueOf(fila.montoDC()),
                                    BigDecimal.valueOf(fila.montoAPV()), "CARGA", "ING"));
                        }
                    } else {
                        salida.mensajes.add(mensaje(salida, "ERROR", "ERROR", "El Cliente con Documento Identidad: " + ruttrab + " no existe."));
                        cliRechazados++;
                    }
                }

                CrearRecaudacionClienteEntrada entradaCli = new CrearRecaudacionClienteEntrada(in.idEmpresa(), in.idUsuario(), "APV", "I", "CARGA",
                        paramsCliente);

                try {
                    CrearRecaudacionClienteResultado outCli = crearRecaudacionCliente.ejecutar(entradaCli);
                    for (Mensaje filaMsg : outCli.mensajes()) {
                        salida.mensajes.add(mensaje(salida, "INFORMACION", "INFORMACION", filaMsg.descripcion()));
                        if (("CREACION_CLIENTE_OK").equals(filaMsg.codigo())) {
                            cliIngresados++;
                        }
                    }
                } catch (Exception er) {
                    MensajeLegacy msg1 = mensaje(salida, "ERROR", "ERROR", "Error al crear cliente");
                    salida.contador++;
                    salida.mensajes.add(msg1);
                    cliRechazados++;
                }

                List<FilaCargaMasiva> filas = new ArrayList<>();
                for (CargaArchivo a : in.archivo()) {
                    filas.add(new FilaCargaMasiva(a.idEmpresa(), a.idCargaMasiva(), a.nombreCargaMasiva(), a.cantidadRegistros(), a.idUsuario(),
                            a.idTipoCarga(), a.descripcion(), a.fechaCarga(), a.idEstado()));
                }
                filas.get(0).IdCargaMasiva = idsCargaArchivo.get(0);
                filas.get(0).FechaCarga = fechaSistema;

                int[] res = CargasSql.lote(sql, SQL_INSERT_CARGA_ARCHIVO, filas);
                if (res != null && res.length > 0 && res[0] == 1) {
                    salida.mensajes.add(mensaje(salida, "INFORMACION", "INFORMACION",
                            "Total Detalle Empleadores. Ingresados: " + empIngresados + " Rechazados: " + empRechazados + "."));
                    salida.mensajes.add(mensaje(salida, "INFORMACION", "INFORMACION",
                            "Total Detalle Clientes. Ingresados: " + cliIngresados + " Rechazados: " + cliRechazados + "."));
                }
            } else {
                MensajeLegacy msg = mensaje(salida, "ERROR", "ERROR", "El archivo ya fue cargado previamente.");
                salida.contador++;
                salida.mensajes.add(msg);
            }
        } else {
            MensajeLegacy msg = mensaje(salida, "ERROR", "ERROR", "Antes de cargar el Detalle debe realizar la carga del archivo Banco.");
            salida.contador++;
            salida.mensajes.add(msg);
        }

        InAgregarLog entradaAgregarLog = new InAgregarLog();
        Log inLog = new Log();
        inLog.IdCabeceraLog = 0L;
        inLog.IdTipoLog = "INGRESO";
        inLog.IdModulo = in.idModulo();
        inLog.IdUsuario = in.idUsuario();
        inLog.IdEmpresa = in.idEmpresa();
        inLog.IdUsuarioAtendido = in.idUsuario();
        inLog.IdTipoRegistro = "PREVIRED";
        inLog.EstadoProceso = "VIG";
        inLog.NombreProceso = "CargaMasivaPrevired";
        entradaAgregarLog.p_Log.add(inLog);
        colaboradores.invocar("AgregarLog", Colab.aJson(entradaAgregarLog));
        return salida;
    }

    private Long obtenerIdRecaudacionDeposito(List<DepositoFila> depositosPorCodigo, Long idRecaudacionDeposito, ArchivoPrevired fila) {
        for (DepositoFila deposito : depositosPorCodigo) {
            if (deposito.FolioCartola.equals(fila.folio()) == true) {
                idRecaudacionDeposito = deposito.IdRecaudacion;
                break;
            }
        }
        return idRecaudacionDeposito;
    }
}
