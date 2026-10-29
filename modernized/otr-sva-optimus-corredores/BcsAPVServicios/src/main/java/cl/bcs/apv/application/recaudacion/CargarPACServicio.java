package cl.bcs.apv.application.recaudacion;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.bcs.apv.api.casouso.CargarPAC;
import cl.bcs.apv.api.casouso.ConciliarDeposito;
import cl.bcs.apv.api.casouso.CrearRecaudacionCliente;
import cl.bcs.apv.api.casouso.CrearRecaudacionDeposito;
import cl.bcs.apv.api.casouso.ProcesarDistribucion;
import cl.bcs.apv.api.comun.Mensaje;
import cl.bcs.apv.api.dto.cargarpac.ArchivoPAC;
import cl.bcs.apv.api.dto.cargarpac.CargaArchivo;
import cl.bcs.apv.api.dto.cargarpac.CargarPACEntrada;
import cl.bcs.apv.api.dto.cargarpac.CargarPACResultado;
import cl.bcs.apv.api.dto.cargarpac.TotalPAC;
import cl.bcs.apv.api.dto.conciliardeposito.ConciliarDepositoEntrada;
import cl.bcs.apv.api.dto.conciliardeposito.ConciliarDepositoResultado;
import cl.bcs.apv.api.dto.crearrecaudacioncliente.Cliente;
import cl.bcs.apv.api.dto.crearrecaudacioncliente.CrearRecaudacionClienteEntrada;
import cl.bcs.apv.api.dto.crearrecaudacioncliente.CrearRecaudacionClienteResultado;
import cl.bcs.apv.api.dto.crearrecaudaciondeposito.CrearRecaudacionDepositoEntrada;
import cl.bcs.apv.api.dto.crearrecaudaciondeposito.CrearRecaudacionDepositoResultado;
import cl.bcs.apv.api.dto.crearrecaudaciondeposito.Recaudacion;
import cl.bcs.apv.api.dto.procesardistribucion.ParametrosRecaudacion;
import cl.bcs.apv.api.dto.procesardistribucion.ProcesarDistribucionEntrada;
import cl.bcs.apv.api.dto.procesardistribucion.ProcesarDistribucionResultado;
import cl.bcs.apv.domain.port.out.AlmacenArchivos;
import cl.bcs.apv.domain.port.out.Colaboradores;
import cl.bcs.apv.domain.port.out.ConfiguracionSistema;
import cl.bcs.apv.domain.port.out.DiccionarioMensajes;
import cl.bcs.apv.domain.port.out.Reloj;
import cl.bcs.apv.domain.port.out.Secuencias;

/**
 * Use case {@code CargarPAC} (family C): loads a PAC (automatic payment) file: one deposit per bank total, one client detail per line, then reconciles
 * and distributes the deposit. Legacy {@code cl.bcs.APV.CargaMasivaPAC}.
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class CargarPACServicio implements CargarPAC {

    private static final String SQL_DEPOSITOS_POR_CODIGO_ARCHIVO = SqlLegacy.recurso("ConsultarCodigoArchivoRecaudacion");
    private static final String SQL_INSERT_CARGA_ARCHIVO = SqlLegacy.recurso("CargaMasivaArchivo_Insert");
    private static final String SQL_CUENTA_POR_CODIGOBANCO = SqlLegacy.recurso("ConsultaCuentaBancoPorCodigo");
    private static final String SQL_IDCLIENTE_POR_CUENTABANCARIA = SqlLegacy.recurso("ConsultarIdClientePorCuentaBancaria");
    private static final String SQL_SUCURSAL_POR_ID = SqlLegacy.recurso("SucursalPorId");

    private final SqlLegacy sql;
    private final ConfiguracionSistema configuracion;
    private final Secuencias secuencias;
    private final ConciliarDeposito conciliarDeposito;
    private final CrearRecaudacionCliente crearRecaudacionCliente;
    private final CrearRecaudacionDeposito crearRecaudacionDeposito;
    private final ProcesarDistribucion procesarDistribucion;

    public CargarPACServicio(NamedParameterJdbcTemplate jdbc, ConfiguracionSistema configuracion, DiccionarioMensajes diccionario, Secuencias secuencias,
            Reloj reloj, AlmacenArchivos archivos, Colaboradores colaboradores, ConciliarDeposito conciliarDeposito,
            CrearRecaudacionCliente crearRecaudacionCliente, CrearRecaudacionDeposito crearRecaudacionDeposito, ProcesarDistribucion procesarDistribucion) {
        this.sql = new SqlLegacy(jdbc);
        this.configuracion = configuracion;
        this.secuencias = secuencias;
        this.conciliarDeposito = conciliarDeposito;
        this.crearRecaudacionCliente = crearRecaudacionCliente;
        this.crearRecaudacionDeposito = crearRecaudacionDeposito;
        this.procesarDistribucion = procesarDistribucion;
    }

    /** Deposit already loaded for the file code (legacy {@code ConciliarDeposito.Recaudacion}, only to count rows). */
    public static class DepositoFila {
        public Long IdRecaudacion;
    }

    public static class Banco {
        public Long IdBanco;
        public String NumeroCuentaBancaria;
    }

    public static class Sucursal {
        public Long IdSucursal;
        public String NombreSucursal;
        public Long IdBanco;
        public String IdEstado;
    }

    public static class ClienteCuenta {
        public Long IdCliente;
    }

    /** Legacy output object: the counter is NOT set by the logic and NOT recomputed by the legacy {@code call} (null, omitted from the JSON). */
    private static final class Salida {
        final List<MensajeLegacy> mensajes = new ArrayList<>();
        Long contador;
    }

    @Override
    public CargarPACResultado ejecutar(CargarPACEntrada in) {
        try {
            Salida salida = logica(in);
            return new CargarPACResultado(MensajeLegacy.aMensajes(salida.mensajes), salida.contador);
        } catch (Exception e) {
            throw Sneaky.lanzar(e);
        }
    }

    @SuppressWarnings("StringEquality")
    private Salida logica(CargarPACEntrada in) throws Exception {
        Salida salida = new Salida();
        HashMap<String, Object> param;
        param = new HashMap<>();
        MensajeLegacy msg = new MensajeLegacy();

        String fechaSistema = Plataforma.parametro(configuracion, in.idEmpresa(), "PLATAFORMA", "PLA.FechaSistema");
        List<Long> idsCargaArchivo = Colab.secuencia(secuencias, "BSCL.APV.BD.Secuencia::APV.IdCargaMasiva", (long) in.archivo().size());

        String codigoArchivo = "";

        String[] codigoArchivoExtencion = in.nombreCarga().split("_");
        if (codigoArchivoExtencion.length > 5) {
            String[] codigoArchivoList = codigoArchivoExtencion[codigoArchivoExtencion.length - 1].split("\\.");
            if (codigoArchivoList.length > 0) {
                codigoArchivo = codigoArchivoList[0];
            } else {
                msg.id = 1L;
                msg.tipo = "ERROR";
                msg.codigo = "ERROR";
                msg.descripcion = "Formato Nombre de Archivo Incorrecto";
                salida.mensajes.add(msg);
                return salida;
            }
        } else {
            msg.id = 1L;
            msg.tipo = "ERROR";
            msg.codigo = "ERROR";
            msg.descripcion = "Formato Nombre de Archivo Incorrecto";
            salida.mensajes.add(msg);
            return salida;
        }

        param.put("CodigoArchivo", codigoArchivo);
        List<DepositoFila> depositosPorCodigo = sql.consultar(SQL_DEPOSITOS_POR_CODIGO_ARCHIVO, param, DepositoFila.class);

        if (depositosPorCodigo.size() > 0) {
            msg.id = 1L;
            msg.tipo = "ERROR";
            msg.codigo = "ERROR";
            msg.descripcion = "El archivo ya fue cargado previamente.";
            salida.mensajes.add(msg);
            return salida;
        }
        // DEPOSITO

        Long idRecaudacion = 0L;
        Long contadorMSG = 0L;

        for (TotalPAC fila : in.parametrosTotalArchivo()) {
            contadorMSG = contadorMSG + 1;
            CrearRecaudacionDepositoResultado depOut;

            // LEGACY DEFECT kept (see README APV-C-160..173): the IdRecaudacion sequence block is requested per iteration with the size of the whole list
            List<Long> idsRecaudacion = Colab.secuencia(secuencias, "BSCL.APV.BD.Secuencia::REC.IdRecaudacion", (long) in.parametrosTotalArchivo().size());

            param = new HashMap<>();
            param.put("p_CodigoBanco", fila.codigoBanco());

            List<Banco> banco = sql.consultar(SQL_CUENTA_POR_CODIGOBANCO, param, Banco.class);

            if (banco.size() == 0) {
                msg.id = contadorMSG;
                msg.tipo = "ERROR";
                msg.codigo = "CUENTA_BANCO_ERROR";
                msg.descripcion = "No se encontro una cuenta asociada al código de banco ingresado.";
                salida.mensajes.add(msg);
                return salida;
            }

            param = new HashMap<>();
            // IdSucursal 7 es el Id de la sucursal banca en linea.
            param.put("p_IdSucursal", 7);

            List<Sucursal> sucursal = sql.consultar(SQL_SUCURSAL_POR_ID, param, Sucursal.class);

            if (sucursal.size() == 0) {
                msg.id = contadorMSG;
                msg.tipo = "ERROR";
                msg.codigo = "SUCURSAL_ERROR";
                msg.descripcion = "No se encontro la Sucursal Banca en Línea.";
                salida.mensajes.add(msg);
                return salida;
            }

            Recaudacion dep = new Recaudacion(0L, idsRecaudacion.get(0), in.idEmpresa(), codigoArchivo, 0L, 0L, String.valueOf(sucursal.get(0).IdSucursal),
                    fechaSistema, 0L, banco.get(0).IdBanco, banco.get(0).NumeroCuentaBancaria,
                    BigDecimal.valueOf(Long.parseLong(fila.montoTotalCargo())), "", in.origen(), "", 0L, "TRA", 0L, idsCargaArchivo.get(0), "ING");
            List<Recaudacion> listaDepositos = new ArrayList<Recaudacion>();
            listaDepositos.add(dep);
            CrearRecaudacionDepositoEntrada entradaDeposito = new CrearRecaudacionDepositoEntrada(in.idEmpresa(), in.idUsuario(), "APV", "I", "CARGA",
                    listaDepositos);

            try {
                depOut = crearRecaudacionDeposito.ejecutar(entradaDeposito);
                // LEGACY DEFECT kept (see README APV-C-160..173): mid-flow commit of the deposit
                sql.commit();
            } catch (Exception er) {
                sql.rollback();
                msg = new MensajeLegacy();
                msg.id = contadorMSG;
                msg.tipo = "ERROR";
                msg.codigo = "ERROR";
                msg.descripcion = Sneaky.causa(er).getMessage();
                salida.mensajes.add(msg);
                return salida;
            }

            if (depOut.contadorErrores() == 0) {
                for (Mensaje msgDep : depOut.mensajes()) {
                    msg = new MensajeLegacy();
                    msg.id = contadorMSG;
                    msg.tipo = msgDep.tipo();
                    msg.codigo = msgDep.codigo();
                    msg.descripcion = msgDep.descripcion();
                    salida.mensajes.add(msg);
                }
                idRecaudacion = depOut.idRec();
            } else {
                for (Mensaje mensaje : depOut.mensajes()) {
                    msg = new MensajeLegacy();
                    msg.id = contadorMSG;
                    msg.tipo = mensaje.tipo();
                    msg.codigo = mensaje.codigo();
                    msg.descripcion = mensaje.descripcion();
                    salida.mensajes.add(msg);
                }
                return salida;
            }
        }

        // CLIENTE

        Long cliIngresados = 0L;
        Long cliRechazados = 0L;

        for (ArchivoPAC fila : in.parametrosArchivo()) {
            String cuentaCorriente = fila.idServicioEmpresa().substring(0, 14);
            String codigoBanco = fila.idServicioEmpresa().substring(14, 17);

            param = new HashMap<>();
            param.put("p_CodigoBanco", codigoBanco);
            param.put("p_NumeroCuentaBancaria", cuentaCorriente);

            List<ClienteCuenta> cliente = sql.consultar(SQL_IDCLIENTE_POR_CUENTABANCARIA, param, ClienteCuenta.class);

            if (cliente.size() > 0) {
                // PARAMETROS CLIENTE
                Cliente cli = new Cliente(0L, 0L, idRecaudacion, null, in.idEmpresa(), cliente.get(0).IdCliente, fechaSistema, "IND", "",
                        BigDecimal.valueOf(0L), BigDecimal.valueOf(fila.montoCargo()), "CARGAPAC", "ING");
                List<Cliente> paramsCliente = new ArrayList<Cliente>();
                paramsCliente.add(cli);
                CrearRecaudacionClienteEntrada entradaCli = new CrearRecaudacionClienteEntrada(in.idEmpresa(), in.idUsuario(), "APV", "I", "CARGA",
                        paramsCliente);
                try {
                    CrearRecaudacionClienteResultado outCli = crearRecaudacionCliente.ejecutar(entradaCli);

                    if (outCli.contadorErrores() == 0) {
                        msg = new MensajeLegacy();
                        msg.id = contadorMSG++;
                        msg.tipo = "OK";
                        msg.codigo = "CREACION_CLIENTE_OK";
                        msg.descripcion = "Detalle Cliente Ingresado";
                        salida.mensajes.add(msg);
                        cliIngresados++;
                    } else {
                        for (Mensaje mensaje : outCli.mensajes()) {
                            msg = new MensajeLegacy();
                            msg.id = contadorMSG++;
                            msg.tipo = mensaje.tipo();
                            msg.codigo = mensaje.codigo();
                            msg.descripcion = mensaje.descripcion();
                            salida.mensajes.add(msg);
                            cliRechazados++;
                        }
                        return salida;
                    }

                    sql.commit();
                } catch (Exception er) {
                    sql.rollback();
                    msg = new MensajeLegacy();
                    msg.id = contadorMSG++;
                    msg.tipo = "ERROR";
                    msg.codigo = "ERROR";
                    msg.descripcion = Sneaky.causa(er).getMessage();
                    salida.mensajes.add(msg);
                }
            } else {
                msg = new MensajeLegacy();
                msg.id = contadorMSG++;
                msg.tipo = "ERROR";
                msg.codigo = "ERROR";
                msg.descripcion = "No existe un Cliente con Codigo Banco " + codigoBanco + " y Numero de Cuenta " + cuentaCorriente;
                salida.mensajes.add(msg);
                cliRechazados++;
            }
        }

        List<FilaCargaMasiva> filas = new ArrayList<>();
        for (CargaArchivo a : in.archivo()) {
            filas.add(new FilaCargaMasiva(a.idEmpresa(), a.idCargaMasiva(), a.nombreCargaMasiva(), a.cantidadRegistros(), a.idUsuario(), a.idTipoCarga(),
                    a.descripcion(), a.fechaCarga(), a.idEstado()));
        }
        filas.get(0).IdCargaMasiva = idsCargaArchivo.get(0);
        filas.get(0).FechaCarga = fechaSistema;

        int[] res = CargasSql.lote(sql, SQL_INSERT_CARGA_ARCHIVO, filas);
        contadorMSG = contadorMSG + 1;
        if (res != null && res.length > 0 && res[0] == 1) {
            msg = new MensajeLegacy();
            msg.id = contadorMSG;
            msg.tipo = "INFORMACION";
            msg.codigo = "INFORMACION";
            msg.descripcion = "Total Detalle Clientes. Ingresados: " + cliIngresados + " Rechazados: " + cliRechazados + ".";
            salida.mensajes.add(msg);

            // CONCILIAR
            ConciliarDepositoEntrada d = ConciliarDepositoEntrada.porDefecto();
            List<cl.bcs.apv.api.dto.conciliardeposito.Recaudacion> paramsConci = new ArrayList<>();
            paramsConci.add(new cl.bcs.apv.api.dto.conciliardeposito.Recaudacion(idRecaudacion, null));
            ConciliarDepositoEntrada entradaCon = new ConciliarDepositoEntrada(in.idEmpresa(), in.idUsuario(), "APV", d.asignacionAutomaticaContrato(),
                    d.esProcesoMasivo(), paramsConci);

            ConciliarDepositoResultado conciliar = conciliarDeposito.ejecutar(entradaCon);

            for (Mensaje fila : conciliar.mensajes()) {
                msg = new MensajeLegacy();
                msg.id = contadorMSG;
                msg.tipo = fila.tipo();
                msg.codigo = fila.codigo();
                msg.descripcion = fila.descripcion();
                salida.mensajes.add(msg);

                // LEGACY DEFECT kept (see README APV-C-160..173): String compared by reference (literal of the reconciliation)
                if (fila.codigo() == "CONCILIADO_OK") {
                    // DISTRIBUIR
                    ProcesarDistribucionEntrada dd = ProcesarDistribucionEntrada.porDefecto();
                    List<ParametrosRecaudacion> listRec = new ArrayList<>();
                    listRec.add(new ParametrosRecaudacion(idRecaudacion, null, null));
                    ProcesarDistribucionEntrada paramDis = new ProcesarDistribucionEntrada(in.idEmpresa(), in.idUsuario(), in.idModulo(), "PAC", listRec,
                            dd.simula(), dd.canal(), dd.soloAporteCuentaInversion(), dd.soloAporteSAAPV(), dd.permiteOperarProspectos(),
                            dd.idsDetalleTraspasoReapertura(), dd.distribucionOrigen(), dd.origen());

                    ProcesarDistribucionResultado procesarDistibucion = procesarDistribucion.ejecutar(paramDis);

                    for (Mensaje row : procesarDistibucion.mensajes()) {
                        msg = new MensajeLegacy();
                        msg.id = contadorMSG;
                        msg.tipo = row.tipo();
                        msg.codigo = row.codigo();
                        msg.descripcion = row.descripcion();
                        salida.mensajes.add(msg);

                        if (row.codigo() == "OK") {
                            return salida;
                        }
                    }
                }
            }
        }
        sql.commit();
        return salida;
    }
}
