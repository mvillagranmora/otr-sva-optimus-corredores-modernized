package cl.bcs.apv.application.recaudacion;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.bcs.apv.api.casouso.CrearRecaudacionCliente;
import cl.bcs.apv.api.casouso.CrearRecaudacionDeposito;
import cl.bcs.apv.api.casouso.CrearRecaudacionEmpleador;
import cl.bcs.apv.api.casouso.ProcesarCargaMasivaRecaudacion;
import cl.bcs.apv.api.comun.Mensaje;
import cl.bcs.apv.api.dto.crearrecaudacioncliente.CrearRecaudacionClienteEntrada;
import cl.bcs.apv.api.dto.crearrecaudacioncliente.CrearRecaudacionClienteResultado;
import cl.bcs.apv.api.dto.crearrecaudaciondeposito.CrearRecaudacionDepositoEntrada;
import cl.bcs.apv.api.dto.crearrecaudaciondeposito.CrearRecaudacionDepositoResultado;
import cl.bcs.apv.api.dto.crearrecaudacionempleador.CrearRecaudacionEmpleadorEntrada;
import cl.bcs.apv.api.dto.crearrecaudacionempleador.CrearRecaudacionEmpleadorResultado;
import cl.bcs.apv.api.dto.procesarcargamasivarecaudacion.ProcesarCargaMasivaRecaudacionEntrada;
import cl.bcs.apv.api.dto.procesarcargamasivarecaudacion.ProcesarCargaMasivaRecaudacionResultado;
import cl.bcs.apv.domain.port.out.AlmacenArchivos;
import cl.bcs.apv.domain.port.out.Colaboradores;
import cl.bcs.apv.domain.port.out.ConfiguracionSistema;
import cl.bcs.apv.domain.port.out.DiccionarioMensajes;
import cl.bcs.apv.domain.port.out.Reloj;
import cl.bcs.apv.domain.port.out.Secuencias;

/**
 * Use case {@code ProcesarCargaMasivaRecaudacion} (family C): pre-validates (PRE) or processes (PRO) the detail rows of a collection load, creating the
 * deposits, employer and client collection records. Legacy {@code cl.bcs.APV.ProcesarCargaMasivaRecaudacion}.
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ProcesarCargaMasivaRecaudacionServicio implements ProcesarCargaMasivaRecaudacion {

    private static final String SQL_SELECT_CARGA_DETALLE = SqlLegacy.recurso("ProcesarCargaMasivaRecaudacion_CargaMasivaDetalle_Select");
    private static final String SQL_SELECT_EMPLEADOR = SqlLegacy.recurso("ProcesarCargaMasivaRecaudacion_ConsultarEmpleador_Select");
    private static final String SQL_SELECT_CLIENTE = SqlLegacy.recurso("ProcesarCargaMasivaRecaudacion_ConsultarIdCliente_Select");
    private static final String SQL_SELECT_CONTRATOS = SqlLegacy.recurso("ProcesarCargaMasivaRecaudacion_ConsultarContratos_Select");
    private static final String SQL_SELECT_BANCOS = SqlLegacy.recurso("ProcesarCargaMasivaRecaudacion_ConsultarBancos_Select");
    private static final String SQL_SELECT_DEPOSITO = SqlLegacy.recurso("ProcesarCargaMasivaRecaudacion_ConsultarDepositos_Select");
    private static final String SQL_SELECT_RECAUDACION = SqlLegacy.recurso("ProcesarCargaMasivaRecaudacion_ConsultarRecaudacion_Select");
    private static final String SQL_UPDATE_ESTADO_RENDICION = SqlLegacy.recurso("ProcesarCargaMasivaRecaudacion_DetalleRecaudacion_Update");
    private static final String SQL_UPDATE_CARGA_MASIVA = SqlLegacy.recurso("ProcesarCargaMasivaRecaudacion_CargaMasiva_Update");

    private final SqlLegacy sql;
    private final CrearRecaudacionCliente crearRecaudacionCliente;
    private final CrearRecaudacionDeposito crearRecaudacionDeposito;
    private final CrearRecaudacionEmpleador crearRecaudacionEmpleador;

    public ProcesarCargaMasivaRecaudacionServicio(NamedParameterJdbcTemplate jdbc, ConfiguracionSistema configuracion, DiccionarioMensajes diccionario,
            Secuencias secuencias, Reloj reloj, AlmacenArchivos archivos, Colaboradores colaboradores, CrearRecaudacionCliente crearRecaudacionCliente,
            CrearRecaudacionDeposito crearRecaudacionDeposito, CrearRecaudacionEmpleador crearRecaudacionEmpleador) {
        this.sql = new SqlLegacy(jdbc);
        this.crearRecaudacionCliente = crearRecaudacionCliente;
        this.crearRecaudacionDeposito = crearRecaudacionDeposito;
        this.crearRecaudacionEmpleador = crearRecaudacionEmpleador;
    }

    // ---------------------------------------------------------------------------------------------- legacy row / working classes

    /** Detail row of the load (legacy {@code DetalleRecaudacion}); also the parameter row of the two UPDATE statements. */
    public static class DetalleRecaudacion {
        public Long IdCargaMasiva = 0L;
        public String Fecha;
        public String TipoDeposito;
        public Long FolioCartola;
        public Long FolioLineaCartola;
        public String Banco;
        public String Cuenta;
        public BigDecimal Monto;
        public String PeriodoPago;
        public String TipoPago;
        public String RUTEmpleador;
        public String RUTCliente;
        public String TipoDeAfiliado;
        public String Regimen;
        public String MensajeValidacion = "";
        public String EstadoRendicion = "";
        public String EstadoProceso = "";
        public Long IdDeposito = 0L;
    }

    public static class Empleador {
        public String DocumentoIdentidad;
        public Long IdPersona;
        public String PersonaBusqueda;
    }

    public static class Cliente {
        public String DocumentoIdentidad;
        public Long IdCliente;
    }

    public static class Contrato {
        public String RutCliente;
        public String RegimenTributario;
        public Long IdContratoSAAPV;
        public String FolioContratoSAAPV;
    }

    public static class Banco {
        public Long IdBanco;
        public String NombreBanco;
        public String NumeroCuentaBancaria;
    }

    public static class Deposito {
        public Long IdRecaudacion;
        public Long FolioCartola;
        public Long LineaCartola;
    }

    public static class EmpleadorRecaudacion {
        public Long IdEmpleador;
        public Long IdRecaudacion;
        public String PeriodoPago;
    }

    /** Mutable employer line under construction (legacy {@code CrearRecaudacionEmpleador.Empleador}). */
    private static final class EmpleadorFila {
        Long IdRecaudacionEmpleador;
        Long IdRecaudacion;
        Long IdEmpresa;
        Long IdPersona;
        String FechaIngreso;
        String PeriodoPago;
        Long NumeroHojas;
        String TipoAfiliado;
        String TipoPago;
        String Insistencias;
        Long MontoDepositoConvenido;
        Long MontoDepositoAPV;
        Long Reajustes;
        Long Intereses;
        String Origen;
        String Observaciones;
        String IdEstado;
    }

    /** Legacy output object: the counter starts at 0 and is NOT recomputed by the legacy {@code call}. */
    private static final class Salida {
        final List<MensajeLegacy> mensajes = new ArrayList<>();
        Long contador = 0L;
    }

    @Override
    public ProcesarCargaMasivaRecaudacionResultado ejecutar(ProcesarCargaMasivaRecaudacionEntrada in) {
        try {
            Salida salida = logica(in);
            return new ProcesarCargaMasivaRecaudacionResultado(MensajeLegacy.aMensajes(salida.mensajes), salida.contador);
        } catch (Exception e) {
            throw Sneaky.lanzar(e);
        }
    }

    // ---------------------------------------------------------------------------------------------- logica

    private Salida logica(ProcesarCargaMasivaRecaudacionEntrada in) throws Exception {
        Salida salida = new Salida();

        List<DetalleRecaudacion> listaDetalleRecaudacion = obtenerDetalleRecaudacion(in);

        List<String> documentosIdentidad = listaDetalleRecaudacion.stream().map(docI -> docI.RUTCliente).collect(Collectors.toList());

        if ("PRE".equals(in.accion())) {
            Map<String, Empleador> mapEmpleadores = obtenerEmpleadores(listaDetalleRecaudacion);
            Map<String, Cliente> mapCliente = obtenerClientes(documentosIdentidad);
            Map<String, Contrato> mapContrato = obtenerContratos(documentosIdentidad);

            List<DetalleRecaudacion> listaDetalleActualizada = listaDetalleRecaudacion.stream().map(detalle -> {
                Empleador empleador = mapEmpleadores.get(detalle.RUTEmpleador);
                Cliente cliente = mapCliente.get(detalle.RUTCliente);
                Contrato contrato = mapContrato.get(detalle.RUTCliente + detalle.Regimen);
                return preProceso(salida, empleador, cliente, contrato, detalle);
            }).collect(Collectors.toList());

            sql.lote(SQL_UPDATE_ESTADO_RENDICION, listaDetalleActualizada);

            if (salida.mensajes.isEmpty()) {
                salida.mensajes.add(new MensajeLegacy(1L, MensajeLegacy.OK, "PROCESA_CARGA_RECAUDACION_OK", "Preproceso de carga exitoso"));
            }
            // LEGACY DEFECT kept (see README APV-C-050..069): the PRE counter stays 0 even when error messages were added
            return salida;
        } else {
            // accion PRO
            List<DetalleRecaudacion> listaIngreso = listaDetalleRecaudacion.stream().filter(detalle -> detalle.EstadoProceso.equals("CRT"))
                    .collect(Collectors.toList());
            Map<String, DetalleRecaudacion> mapDetalleDeposito = new HashMap<String, DetalleRecaudacion>();
            Map<String, Banco> mapBancos = obtenerBancos(listaIngreso);
            Map<String, Cliente> mapCliente = obtenerClientes(documentosIdentidad);

            for (DetalleRecaudacion detalle : listaIngreso) {
                // LEGACY DEFECT kept (see README APV-C-065): the key is FolioCartola + FolioLineaCartola as text (1+23 collides with 12+3)
                String key = detalle.FolioCartola.toString() + detalle.FolioLineaCartola.toString();
                DetalleRecaudacion detMapDeposito = new DetalleRecaudacion();
                detMapDeposito.IdCargaMasiva = detalle.IdCargaMasiva;
                detMapDeposito.Fecha = detalle.Fecha;
                detMapDeposito.TipoDeposito = detalle.TipoDeposito;
                detMapDeposito.FolioCartola = detalle.FolioCartola;
                detMapDeposito.FolioLineaCartola = detalle.FolioLineaCartola;
                detMapDeposito.Banco = detalle.Banco;
                detMapDeposito.Cuenta = detalle.Cuenta;
                detMapDeposito.Monto = detalle.Monto;
                detMapDeposito.PeriodoPago = detalle.PeriodoPago;
                detMapDeposito.TipoPago = detalle.TipoPago;
                detMapDeposito.RUTEmpleador = detalle.RUTEmpleador;
                detMapDeposito.RUTCliente = detalle.RUTCliente;
                detMapDeposito.TipoDeAfiliado = detalle.TipoDeAfiliado;
                detMapDeposito.Regimen = detalle.Regimen;
                detMapDeposito.MensajeValidacion = detalle.MensajeValidacion;
                detMapDeposito.EstadoRendicion = detalle.EstadoRendicion;
                detMapDeposito.EstadoProceso = detalle.EstadoProceso;
                detMapDeposito.IdDeposito = detalle.IdDeposito;

                if (mapDetalleDeposito.get(key) == null) {
                    mapDetalleDeposito.put(key, detMapDeposito);
                } else {
                    BigDecimal montoSumado = mapDetalleDeposito.get(key).Monto.add(detMapDeposito.Monto);
                    mapDetalleDeposito.get(key).Monto = montoSumado;
                }
            }

            List<Long> listaIdDepositos = ingresarDeposito(in, salida, mapDetalleDeposito, mapBancos);

            Map<String, Empleador> mapEmpleadores = obtenerEmpleadores(listaIngreso);
            Map<String, Deposito> mapRecaudacion = obtenerDepositos(listaIdDepositos);

            List<Long> listaIdRecaudacionEmpleador = new ArrayList<Long>();
            if (salida.contador.compareTo(0L) == 0 && mapEmpleadores.size() > 0) {
                listaIdRecaudacionEmpleador = ingresarEmpleador(in, salida, mapRecaudacion, listaDetalleRecaudacion, mapEmpleadores);
            }

            if (salida.contador.compareTo(0L) == 0) {
                Map<String, EmpleadorRecaudacion> mapEmpleadorRecaudacion = obtenerEmpleadorRecaudacion(listaIdRecaudacionEmpleador);
                ingresarClientes(in, salida, listaIngreso, mapCliente, mapRecaudacion, mapEmpleadorRecaudacion);
            }

            if (salida.contador.compareTo(0L) == 0) {
                actualizaCargaMasiva(in.idCargaMasiva());
            }
        }
        return salida;
    }

    private List<Long> ingresarEmpleador(ProcesarCargaMasivaRecaudacionEntrada in, Salida out, Map<String, Deposito> mapRecaudacion,
            List<DetalleRecaudacion> listaDetalleRecaudacion, Map<String, Empleador> mapEmpleadores) throws Exception {
        List<Long> listaIdRecaudacionEmpleador = new ArrayList<>();
        Map<String, EmpleadorFila> hashEmpleador = new HashMap<String, EmpleadorFila>();

        for (DetalleRecaudacion obj : listaDetalleRecaudacion) {
            // LEGACY DEFECT kept (see README APV-C-061): walks ALL rows (not only CRT): a DEP row with another key throws NullPointerException
            if ("DEP".equals(obj.TipoDeAfiliado)) {
                String key = obj.FolioCartola.toString() + obj.FolioLineaCartola.toString();
                EmpleadorFila empleador = new EmpleadorFila();

                empleador.IdRecaudacionEmpleador = 0L;
                empleador.IdRecaudacion = mapRecaudacion.get(key).IdRecaudacion;
                empleador.IdEmpresa = in.idEmpresa();
                empleador.IdPersona = mapEmpleadores.get(obj.RUTEmpleador).IdPersona;
                empleador.FechaIngreso = obj.Fecha;
                empleador.PeriodoPago = obj.PeriodoPago;
                empleador.NumeroHojas = 0L;
                empleador.TipoAfiliado = obj.TipoDeAfiliado;
                empleador.TipoPago = obj.TipoPago;
                empleador.Insistencias = "";
                empleador.MontoDepositoConvenido = obj.Regimen.contains("APV") ? 0L : obj.Monto.longValue();
                empleador.MontoDepositoAPV = obj.Regimen.contains("APV") ? obj.Monto.longValue() : 0L;
                empleador.Reajustes = 0L;
                empleador.Intereses = 0L;
                empleador.Origen = "CARGA";
                empleador.Observaciones = "";
                empleador.IdEstado = "ING";
                if (hashEmpleador.get(key) == null) {
                    hashEmpleador.put(key, empleador);
                } else {
                    // LEGACY DEFECT kept (see README APV-C-065): Long compared by reference (same object here, so always true)
                    if (hashEmpleador.get(key).IdRecaudacion == mapRecaudacion.get(key).IdRecaudacion) {
                        if (obj.PeriodoPago.equals(hashEmpleador.get(key).PeriodoPago)) {
                            if (obj.Regimen.contains("APV")) {
                                hashEmpleador.get(key).MontoDepositoAPV = hashEmpleador.get(key).MontoDepositoAPV + obj.Monto.longValue();
                            } else {
                                hashEmpleador.get(key).MontoDepositoConvenido = hashEmpleador.get(key).MontoDepositoConvenido + obj.Monto.longValue();
                            }
                        } else {
                            hashEmpleador.put(key + obj.PeriodoPago, empleador);
                        }
                    }
                }
            }
        }

        List<cl.bcs.apv.api.dto.crearrecaudacionempleador.Empleador> parametros = new ArrayList<>();
        // LEGACY DEFECT kept (see README APV-C-064): HashMap iteration order decides the order of the employer lines
        for (Map.Entry<String, EmpleadorFila> obj : hashEmpleador.entrySet()) {
            EmpleadorFila e = obj.getValue();
            parametros.add(new cl.bcs.apv.api.dto.crearrecaudacionempleador.Empleador(0L, e.IdRecaudacionEmpleador, e.IdRecaudacion, null, e.IdEmpresa,
                    e.IdPersona, e.FechaIngreso, e.PeriodoPago, e.NumeroHojas, e.TipoAfiliado, e.TipoPago, e.Insistencias, e.MontoDepositoConvenido,
                    e.MontoDepositoAPV, e.Reajustes, e.Intereses, e.Origen, e.Observaciones, e.IdEstado));
        }

        CrearRecaudacionEmpleadorResultado salidaEmpleador = crearRecaudacionEmpleador
                .ejecutar(new CrearRecaudacionEmpleadorEntrada(in.idEmpresa(), in.idUsuario(), in.idModulo(), "I", "CARGA", parametros));
        agregar(out, salidaEmpleador.mensajes());
        out.contador = salidaEmpleador.contadorErrores() != null ? salidaEmpleador.contadorErrores() : 0L;

        salidaEmpleador.resultado().forEach(reca -> listaIdRecaudacionEmpleador.add(reca.idRecaudacionEmpleador()));
        return listaIdRecaudacionEmpleador;
    }

    private void ingresarClientes(ProcesarCargaMasivaRecaudacionEntrada in, Salida out, List<DetalleRecaudacion> listaDetalles, Map<String, Cliente> mapCliente,
            Map<String, Deposito> mapRecaudacion, Map<String, EmpleadorRecaudacion> mapEmpleadorRecaudacion) throws Exception {
        List<cl.bcs.apv.api.dto.crearrecaudacioncliente.Cliente> parametros = new ArrayList<>();

        listaDetalles.stream().forEach(detalle -> {
            String key = detalle.FolioCartola.toString() + detalle.FolioLineaCartola.toString();
            String periodo = detalle.PeriodoPago;

            Long idRecaudacionEmpleador = 0L;
            Long idRecaudacion = mapRecaudacion.get(key).IdRecaudacion;
            if (!mapEmpleadorRecaudacion.isEmpty() && "DEP".equals(detalle.TipoDeAfiliado)) {
                idRecaudacionEmpleador = mapEmpleadorRecaudacion.get(idRecaudacion.toString() + periodo).IdEmpleador;
            }
            Long idCliente = mapCliente.get(detalle.RUTCliente).IdCliente;
            parametros.add(new cl.bcs.apv.api.dto.crearrecaudacioncliente.Cliente(0L, 0L, idRecaudacion, idRecaudacionEmpleador, in.idEmpresa(), idCliente,
                    detalle.Fecha, detalle.TipoDeAfiliado, detalle.Regimen, detalle.Regimen.contains("APV") ? BigDecimal.ZERO : detalle.Monto,
                    detalle.Regimen.contains("APV") ? detalle.Monto : BigDecimal.ZERO, "CARGA", "ING"));
        });

        CrearRecaudacionClienteResultado salidaCliente = crearRecaudacionCliente
                .ejecutar(new CrearRecaudacionClienteEntrada(in.idEmpresa(), in.idUsuario(), in.idModulo(), "I", "CARGA", parametros));
        agregar(out, salidaCliente.mensajes());
        out.contador = salidaCliente.contadorErrores() != null ? salidaCliente.contadorErrores() : 0L;
    }

    private List<Long> ingresarDeposito(ProcesarCargaMasivaRecaudacionEntrada in, Salida out, Map<String, DetalleRecaudacion> mapIngreso,
            Map<String, Banco> mapBancos) throws Exception {
        List<Long> listaIdDepositos = new ArrayList<>();
        List<cl.bcs.apv.api.dto.crearrecaudaciondeposito.Recaudacion> parametros = new ArrayList<>();

        // LEGACY DEFECT kept (see README APV-C-064): HashMap iteration order decides the order of the deposits (and of the ids they receive)
        for (Map.Entry<String, DetalleRecaudacion> obj : mapIngreso.entrySet()) {
            Banco banco = mapBancos.get(obj.getValue().Banco + obj.getValue().Cuenta);
            parametros.add(new cl.bcs.apv.api.dto.crearrecaudaciondeposito.Recaudacion(0L, 0L, in.idEmpresa(), null, obj.getValue().FolioCartola,
                    obj.getValue().FolioLineaCartola, "", obj.getValue().Fecha, 0L, banco != null ? banco.IdBanco : 0L, obj.getValue().Cuenta,
                    obj.getValue().Monto, "", "", "", 0L, obj.getValue().TipoDeposito, 0L, 0L, "ING"));
        }

        CrearRecaudacionDepositoResultado salidaDepositos = crearRecaudacionDeposito
                .ejecutar(new CrearRecaudacionDepositoEntrada(in.idEmpresa(), in.idUsuario(), in.idModulo(), "I", "", parametros));
        agregar(out, salidaDepositos.mensajes());
        out.contador = salidaDepositos.contadorErrores();

        if (out.contador.compareTo(0L) == 0) {
            out.mensajes.forEach(mensaje -> {
                if ("UI_CREACION_DEPOSITO_OK".equals(mensaje.codigo)) {
                    listaIdDepositos.add(mensaje.id);
                }
            });
        }
        return listaIdDepositos;
    }

    private static void agregar(Salida out, List<Mensaje> mensajes) {
        for (Mensaje m : mensajes) {
            out.mensajes.add(MensajeLegacy.de(m));
        }
    }

    private boolean validaEmpleador(String empleadorArchivo, Empleador empleadorValidado, String clienteArchivo, String tipoAfiliado) {
        boolean validado = false;
        if ("IND".equals(tipoAfiliado)) {
            if (empleadorArchivo.equals(clienteArchivo)) {
                validado = true;
            }
        } else {
            if (empleadorValidado != null && empleadorArchivo.equals(empleadorValidado.DocumentoIdentidad)) {
                validado = true;
            }
        }
        return validado;
    }

    private boolean validaCliente(String clienteArchivo, Cliente clienteValidado) {
        boolean validado = false;
        if (clienteValidado != null && clienteArchivo.equals(clienteValidado.DocumentoIdentidad)) {
            validado = true;
        }
        return validado;
    }

    private DetalleRecaudacion preProceso(Salida salida, Empleador empleador, Cliente cliente, Contrato contrato, DetalleRecaudacion detalle) {
        boolean certificado = true;

        if (!validaCliente(detalle.RUTCliente, cliente)) {
            salida.mensajes.add(new MensajeLegacy(1L, MensajeLegacy.ERROR, "PROCESA_CARGA_RECAUDACION_CLIENTE_NO_VALIDO",
                    "Cliente rut " + detalle.RUTCliente + " no valido "));
            detalle.EstadoRendicion = "NO OK";
            detalle.MensajeValidacion += " Cliente Informado no existe, ";
            certificado = false;
            detalle.EstadoProceso = "ADV";
        }

        if (!validaEmpleador(detalle.RUTEmpleador, empleador, detalle.RUTCliente, detalle.TipoDeAfiliado)) {
            salida.mensajes.add(new MensajeLegacy(1L, MensajeLegacy.ERROR, "PROCESA_CARGA_RECAUDACION_EMPLEADOR_NO_VALIDO",
                    "Empleador rut " + detalle.RUTEmpleador + " no valido "));
            detalle.EstadoRendicion = "NO OK";
            detalle.MensajeValidacion += " Empleador Informado no existe ";
            certificado = false;
            detalle.EstadoProceso = "NOK";
        }

        if (!("NORMAL".equalsIgnoreCase(detalle.TipoPago) || "ATRASADO".equalsIgnoreCase(detalle.TipoPago))) {
            salida.mensajes.add(new MensajeLegacy(1L, MensajeLegacy.ERROR, "PROCESA_CARGA_RECAUDACION_TIPO_PAGO_NO_VALIDO",
                    "Cliente rut " + detalle.TipoPago + " no valido "));
            certificado = false;
        }

        detalle.EstadoRendicion = certificado == true ? "OK" : detalle.EstadoProceso;
        detalle.EstadoProceso = certificado == true ? "CRT" : detalle.EstadoProceso;
        return detalle;
    }

    private void actualizaCargaMasiva(Long idCargaMasiva) throws Exception {
        List<DetalleRecaudacion> listaIdCarga = new ArrayList<DetalleRecaudacion>();
        DetalleRecaudacion carga = new DetalleRecaudacion();
        carga.IdCargaMasiva = idCargaMasiva;
        listaIdCarga.add(carga);
        sql.lote(SQL_UPDATE_CARGA_MASIVA, listaIdCarga);
    }

    private Map<String, Banco> obtenerBancos(List<DetalleRecaudacion> listaIngreso) throws Exception {
        Map<String, Banco> mapBanco = new HashMap<String, Banco>();
        List<String> nombresBanco = new ArrayList<String>();
        List<String> cuentaBanco = new ArrayList<String>();
        listaIngreso.forEach(detalle -> {
            nombresBanco.add(detalle.Banco);
            cuentaBanco.add(detalle.Cuenta);
        });

        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("NombreBanco", nombresBanco);
        params.put("NumeroCuenta", cuentaBanco);

        List<Banco> listaBancos = sql.consultar(SQL_SELECT_BANCOS, params, Banco.class);
        listaBancos.forEach(banco -> {
            String key = banco.NombreBanco + banco.NumeroCuentaBancaria;
            mapBanco.put(key, banco);
        });
        return mapBanco;
    }

    private List<DetalleRecaudacion> obtenerDetalleRecaudacion(ProcesarCargaMasivaRecaudacionEntrada in) throws Exception {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("idCargaMasiva", in.idCargaMasiva());
        return sql.consultar(SQL_SELECT_CARGA_DETALLE, params, DetalleRecaudacion.class);
    }

    private Map<String, Empleador> obtenerEmpleadores(List<DetalleRecaudacion> archivoRecaudaciones) throws Exception {
        List<String> documentosIdentidad = archivoRecaudaciones.stream().map(docI -> docI.RUTEmpleador).collect(Collectors.toList());
        HashMap<String, Object> parametros = new HashMap<String, Object>();
        parametros.put("DocumentoIdentidad", documentosIdentidad);
        return sql.consultar(SQL_SELECT_EMPLEADOR, parametros, Empleador.class).stream()
                .collect(Collectors.toMap(emple -> emple.DocumentoIdentidad, emple -> emple));
    }

    private Map<String, Cliente> obtenerClientes(List<String> documentosIdentidad) throws Exception {
        HashMap<String, Object> parametros = new HashMap<String, Object>();
        parametros.put("DocumentoIdentidad", documentosIdentidad);
        return sql.consultar(SQL_SELECT_CLIENTE, parametros, Cliente.class).stream()
                .collect(Collectors.toMap(cliente -> cliente.DocumentoIdentidad, cliente -> cliente));
    }

    private Map<String, Deposito> obtenerDepositos(List<Long> listaIdDepositos) throws Exception {
        Map<String, Deposito> mapDepositos = new HashMap<String, Deposito>();
        HashMap<String, Object> parametros = new HashMap<String, Object>();
        parametros.put("IdRecaudacion", listaIdDepositos);
        List<Deposito> listaDepositosIngresados = sql.consultar(SQL_SELECT_DEPOSITO, parametros, Deposito.class);
        listaDepositosIngresados.forEach(deposito -> {
            String key = deposito.FolioCartola.toString() + deposito.LineaCartola.toString();
            mapDepositos.put(key, deposito);
        });
        return mapDepositos;
    }

    private Map<String, EmpleadorRecaudacion> obtenerEmpleadorRecaudacion(List<Long> listaIdRecaudacionEmpleador) throws Exception {
        Map<String, EmpleadorRecaudacion> mapEmpleadorRecaudacion = new HashMap<String, EmpleadorRecaudacion>();
        HashMap<String, Object> parametros = new HashMap<String, Object>();
        parametros.put("IdRecaudacionEmpleador", listaIdRecaudacionEmpleador);
        List<EmpleadorRecaudacion> lista = sql.consultar(SQL_SELECT_RECAUDACION, parametros, EmpleadorRecaudacion.class);
        lista.forEach(empReca -> {
            String key = empReca.IdRecaudacion.toString() + empReca.PeriodoPago;
            mapEmpleadorRecaudacion.put(key, empReca);
        });
        return mapEmpleadorRecaudacion;
    }

    private Map<String, Contrato> obtenerContratos(List<String> documentosIdentidad) throws Exception {
        HashMap<String, Object> parametros = new HashMap<String, Object>();
        Map<String, Contrato> contratos = new HashMap<String, Contrato>();
        parametros.put("RutCliente", documentosIdentidad);
        List<Contrato> listaContratos = sql.consultar(SQL_SELECT_CONTRATOS, parametros, Contrato.class);
        listaContratos.forEach(contrato -> {
            String key = contrato.RutCliente.toString() + contrato.RegimenTributario;
            contratos.put(key, contrato);
        });
        return contratos;
    }
}
