package cl.bcs.tesoreria.javacall.distribucion;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import cl.bcs.tesoreria.javacall.distribucion.Filas.ContratoSuscripcionCuotaDetalle;
import cl.bcs.tesoreria.javacall.distribucion.Filas.DistribucionInversion;
import cl.bcs.tesoreria.javacall.distribucion.Filas.PortafolioCliente;
import cl.bcs.tesoreria.javacall.kit.MensajeSalida;
import cl.bcs.tesoreria.javacall.kit.Plataforma;
import cl.bcs.tesoreria.javacall.kit.SqlNombrado;
import cl.bcs.tesoreria.javacall.kit.colaborador.CalcularMontoNumeroCuota;
import cl.bcs.tesoreria.javacall.kit.colaborador.CalcularMontoYCobrosOrden;
import cl.bcs.tesoreria.javacall.kit.colaborador.ConsultarInstrumentoPrecioBolsa;
import cl.bcs.tesoreria.javacall.kit.colaborador.CrearCargoAbono;
import cl.bcs.tesoreria.javacall.kit.colaborador.CrearIngresoEgreso;
import cl.bcs.tesoreria.javacall.kit.colaborador.CrearOrdenes;
import cl.bcs.tesoreria.javacall.kit.colaborador.CrearSolicitudAporteRescateNormal;
import cl.bcs.tesoreria.javacall.kit.colaborador.LiquidarComprobante;
import cl.bcs.tesoreria.javacall.kit.colaborador.ResultadoSolicitud;
import cl.bcs.tesoreria.port.out.Colaboradores;
import cl.bcs.tesoreria.port.out.DiccionarioMensajes;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.Utils;

/**
 * The four movement steps of ProcesarDistribucion.logica: 1 Tesoreria (income/expense + liquidation), 2 Cuenta de Inversion (charge/credit),
 * 3 Fondos Mutuos (contribution requests) and 4 Ordenes (stock orders), each one through its collaborator service. Legacy
 * {@code getSalidaTesoreria}, {@code getSalidaCuentaInversion}, {@code getSalidaFFMM}, {@code getOrdenes} (TES-B-277..308, 319..321).
 * Every step reports into the {@link Corrida} it receives and marks the requests whose collaborator answered an error.
 */
@Component
public class MovimientosDistribucion {

    private static final Logger log = LoggerFactory.getLogger(MovimientosDistribucion.class);

    private static final String SQL_PORTAFOLIO_CLIENTE = SqlNombrado.recurso("ProcesarDistribucion_PortafolioCliente");
    private static final String SQL_COMPROBANTE = SqlNombrado.recurso("ProcesarDistribucion_SelectComprobante");

    private final SqlNombrado sql;
    private final DiccionarioMensajes diccionario;
    private final Colaboradores colaboradores;

    public MovimientosDistribucion(NamedParameterJdbcTemplate jdbc, DiccionarioMensajes diccionario, Colaboradores colaboradores) {
        this.sql = new SqlNombrado(jdbc);
        this.diccionario = diccionario;
        this.colaboradores = colaboradores;
    }

    /** Data of the broker the Tesoreria step needs (own portfolio, digitiser, distribution bank account, PAC cash box). */
    public record DatosTesoreria(String monedaSistema, Long idPortafolioCorredor, Long idPersonaCorredor, Long idEmpleadoDigitador, Long idCtaBancoCorredor,
            Long idCajaTesoreriaPac, Long idPersonaBanco) {
    }

    // ------------------------------------------------------------------------------------------------ 1 TESORERIA

    /**
     * Three movements per request (client income, broker expense, broker income from the distribution bank) through CrearIngresoEgreso and, unless it is
     * a simulation, the liquidation of the created vouchers through LiquidarComprobante.
     */
    public void tesoreria(Corrida c, List<RegistroRecaudacion> solicitudProcesar, DatosTesoreria d) throws Exception {
        SalidaRecaudacionTraspaso out = c.out;
        String simulacion = c.entrada.p_simular;
        String fechaSistema = c.fechaSistema;

        CrearIngresoEgreso.CrearIngresoEgreso_Entrada entradaIngresoEgreso = new CrearIngresoEgreso.CrearIngresoEgreso_Entrada();
        Map<Long, Long> mapIdSolicitud = new HashMap<>();

        List<Long> idsPortafolios = new ArrayList<>();
        for (RegistroRecaudacion solicitud : solicitudProcesar) {
            if (!idsPortafolios.contains(solicitud.IdPortafolio)) {
                idsPortafolios.add(solicitud.IdPortafolio);
            }
        }
        Map<Long, PortafolioCliente> portafolioCliente = new HashMap<>();
        if (!idsPortafolios.isEmpty()) {
            Map<String, Object> param = new HashMap<>();
            param.put("IdsPortafolio", idsPortafolios);
            for (PortafolioCliente fila : sql.consultar(SQL_PORTAFOLIO_CLIENTE, param, PortafolioCliente.class)) {
                // legacy Utils.List2Hash: a repeated key is an error
                if (portafolioCliente.containsKey(fila.IdPortafolio())) {
                    throw new Exception("key field is not unique");
                }
                portafolioCliente.put(fila.IdPortafolio(), fila);
            }
        }

        long i = 1L;
        long j = "S".equals(simulacion) ? 1 : -1;
        for (RegistroRecaudacion solicitud : solicitudProcesar) {
            PortafolioCliente infoPortafolioCliente = portafolioCliente.get(solicitud.IdPortafolio);

            CrearIngresoEgreso.IngresoEgreso_Entrada ie = new CrearIngresoEgreso.IngresoEgreso_Entrada();
            CrearIngresoEgreso.IngresoEgreso_Entrada ie2 = new CrearIngresoEgreso.IngresoEgreso_Entrada();
            CrearIngresoEgreso.IngresoEgreso_Entrada ie3 = new CrearIngresoEgreso.IngresoEgreso_Entrada();
            CrearIngresoEgreso.FormaDePago_Entrada fp = new CrearIngresoEgreso.FormaDePago_Entrada();
            CrearIngresoEgreso.FormaDePago_Entrada fp2 = new CrearIngresoEgreso.FormaDePago_Entrada();
            CrearIngresoEgreso.FormaDePago_Entrada fp3 = new CrearIngresoEgreso.FormaDePago_Entrada();

            boolean abonoApv = solicitud.IdConcepto.equals("ABNAPV");

            // 1) income of the client's collection
            ie.NumeroReferencia = i * j;
            ie.IdTipoMovimiento = Constantes.INGRESO_TESORERIA;
            ie.IdTipoPersona = Constantes.TIPO_PERSONA_CLIENTE;
            ie.IdPersona = infoPortafolioCliente != null ? infoPortafolioCliente.IdPersona() : 0L;
            ie.IdPortafolio = solicitud.IdPortafolio;
            ie.IdTipoPersonaTitularPago = Constantes.TIPO_PERSONA_CORREDOR;
            ie.IdPersonaTitularPago = d.idPersonaCorredor();
            ie.IdPortafolioTitularPago = d.idPortafolioCorredor();
            ie.IdMoneda = d.monedaSistema();
            ie.Monto = solicitud.MontoDeposito;
            ie.FechaLiquidacion = fechaSistema;
            ie.IdConcepto = abonoApv ? Constantes.AAPVIR : Constantes.ACSCIR;
            ie.IdCaja = d.idCajaTesoreriaPac();
            ie.SecuenciaAgrupacion = "";
            ie.SecuenciaRelacionada = 0L;
            ie.Glosa = "";
            ie.IdEmpleadoDigitador = d.idEmpleadoDigitador();
            ie.IdSucursalEmpleadoDigitador = 0L;
            ie.IdTipoProducto = Constantes.TIPO_PRODUCTO;
            ie.IdTipoOperacion = Constantes.TIPO_OPERACION_APORTE_FONDO_MUTUO;
            ie.IdTipoComprobante = Constantes.TIPO_COMPROBANTE_TESORERIA;
            ie.IdComprobante = solicitud.IdTraspaso;
            ie.FechaComprobante = Fecha.stringToSqlDate(solicitud.FechaAceptacion);
            ie.FechaLiquidacionComprobante = "0001-01-01";
            ie.IdOperacion = solicitud.IdTraspaso;
            ie.FechaOperacion = fechaSistema;
            ie.IdCondicionLiquidacion = Constantes.T0;
            ie.IdBolsa = Constantes.BCS;
            ie.IdMonedaOperacion = d.monedaSistema();
            ie.MontoOperacion = solicitud.MontoDeposito;
            ie.IdTipoReajuste = "";
            ie.FechaParidad = "0001-01-01";
            ie.ValorParidad = BigDecimal.valueOf(0);
            ie.FolioCartera = 0L;
            ie.IdInstrumento = 0L;
            ie.IdTipoInstrumento = "";
            ie.IdClasificacionTipoInstrumento = "";
            ie.Bilateral = Constantes.NO;
            ie.GeneraComprobante = Constantes.SI;
            ie.AgruparEnComprobante = Constantes.NO;
            ie.MueveCuentaInversion = Constantes.SI;
            ie.TitularPago = Constantes.NO;
            mapIdSolicitud.put(i * j, solicitud.IdMandatoPAC);

            fp.NumeroReferencia = i * j;
            fp.NumeroReferenciaFormaDePago = i * j;
            fp.IdMedioDePago = solicitud.IdMedioDePago;
            fp.IdModalidadDePago = Constantes.MODCAJ;
            fp.IdMoneda = d.monedaSistema();
            fp.Monto = solicitud.MontoDeposito;
            fp.IdCuentaBancariaCorredora = d.idCtaBancoCorredor();
            fp.IdTarjetaBancariaCorredora = 0L;
            fp.IdChequera = 0L;
            fp.NumeroDocumentoCorredora = 0L;
            fp.FechaDocumentoPagoCorredora = "0001-01-01";
            fp.IdCuentaBancariaSocio = 0L;
            fp.IdBancoSocio = 0L;
            fp.NombreCuentaBancariaSocio = "";
            fp.NumeroCuentaBancariaSocio = "";
            fp.IdTipoCuentaBancariaSocio = "";
            fp.NombreTipoCuentaBancariaSocio = "";
            fp.IdTarjetaBancariaSocio = 0L;
            fp.IdTipoTarjetaBancariaSocio = "";
            fp.NombreTipoTarjetaBancariaSocio = "";
            fp.NumeroTarjetaBancariaSocio = "";
            fp.NumeroSerieSocio = "";
            fp.NumeroDocumentoSocio = 0L;
            fp.FechaDocumentoPagoSocio = "0001-01-01";

            entradaIngresoEgreso.p_IngresoEgresoGenerar.add(ie);
            entradaIngresoEgreso.p_FormaDePago.add(fp);

            // 2) broker's expense (collection account)
            i++;
            Utils.copyPojo(ie, ie2);
            ie2.IdConcepto = abonoApv ? Constantes.AAPVER : Constantes.ACSCER;
            ie2.IdTipoMovimiento = Constantes.EGRESO_TESORERIA;
            ie2.NumeroReferencia = i * j;
            ie2.IdTipoPersona = Constantes.TIPO_PERSONA_CORREDOR;
            ie2.IdPersona = d.idPersonaCorredor();
            ie2.IdPortafolio = d.idPortafolioCorredor();
            ie2.IdTipoPersonaTitularPago = Constantes.TIPO_PERSONA_CORREDOR;
            ie2.IdPersonaTitularPago = d.idPersonaCorredor();
            ie2.IdPortafolioTitularPago = d.idPortafolioCorredor();
            mapIdSolicitud.put(i * j, solicitud.IdMandatoPAC);

            Utils.copyPojo(fp, fp2);
            fp2.NumeroReferencia = i * j;
            fp2.NumeroReferenciaFormaDePago = i * j;

            entradaIngresoEgreso.p_IngresoEgresoGenerar.add(ie2);
            entradaIngresoEgreso.p_FormaDePago.add(fp2);

            // 3) broker's income from the distribution account (the bank is the counterparty, without portfolio)
            i++;
            Utils.copyPojo(ie, ie3);
            ie3.IdConcepto = abonoApv ? Constantes.AAPVID : Constantes.ACSCID;
            ie3.IdTipoMovimiento = Constantes.INGRESO_TESORERIA;
            ie3.NumeroReferencia = i * j;
            ie3.IdTipoPersona = Constantes.TIPO_PERSONA_CORREDOR;
            ie3.IdPersona = d.idPersonaCorredor();
            ie3.IdPortafolio = d.idPortafolioCorredor();
            ie3.IdTipoPersonaTitularPago = Constantes.TIPO_PERSONA_CONTRAPARTE;
            ie3.IdPersonaTitularPago = d.idPersonaBanco();
            ie3.IdPortafolioTitularPago = 0L;
            mapIdSolicitud.put(i * j, solicitud.IdMandatoPAC);

            Utils.copyPojo(fp, fp3);
            fp3.NumeroReferencia = i * j;
            fp3.NumeroReferenciaFormaDePago = i * j;
            fp3.IdCuentaBancariaCorredora = d.idCtaBancoCorredor();
            entradaIngresoEgreso.p_IngresoEgresoGenerar.add(ie3);
            entradaIngresoEgreso.p_FormaDePago.add(fp3);
            i++;
        }

        entradaIngresoEgreso.p_IdEmpresa = c.entrada.p_IdEmpresa;
        entradaIngresoEgreso.p_IdUsuario = c.entrada.p_IdUsuario;
        entradaIngresoEgreso.p_IdModulo = c.entrada.p_IdModulo;
        entradaIngresoEgreso.p_IdOrigenProceso = "PAC";
        entradaIngresoEgreso.p_SimularOperacion = simulacion;
        entradaIngresoEgreso.p_UsaReglasNegocio = "S";
        entradaIngresoEgreso.p_EsIngresoAutomatico = "S";

        log.info("Llamada a CrearIngresoEgreso");
        CrearIngresoEgreso.CrearIngresoEgreso_Salida salida = CrearIngresoEgreso.invocar(colaboradores, entradaIngresoEgreso);
        log.info("Fin CrearIngresoEgreso");

        if (salida.P_OUT_ERRORESCONTADOR != null) {
            out.P_OUT_ERRORESCONTADOR += salida.P_OUT_ERRORESCONTADOR;
        }

        for (MensajeSalida mensaje : salida.P_OUT_MENSAJES) {
            if (MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo)) {
                Long id = mapIdSolicitud.get(mensaje.id);
                if (id != null) {
                    for (RegistroRecaudacion solicitud : solicitudProcesar) {
                        if (id.equals(solicitud.IdMandatoPAC)) {
                            rechazar(out, solicitud, mensaje.descripcion, mensaje.codigo);
                            solicitud.EstadoProceso = Constantes.ESTADO_RECHAZADO;
                            solicitud.Codigo = mensaje.codigo;
                            solicitud.DescripcionSolicitud = mensaje.descripcion;
                        }
                    }
                }
            }
        }

        if (simulacion.equals("N")) {
            List<Long> idsComprobante = new ArrayList<>();
            for (CrearIngresoEgreso.ResultadoProceso_Salida resultado : salida.P_OUT_RESULTADOPROCESO) {
                if (resultado.Campo.equalsIgnoreCase("IdComprobante")) {
                    idsComprobante.add(resultado.Id);
                }
            }

            if (!idsComprobante.isEmpty()) {
                log.info("SE INVOCA SERVICIO LIQUIDAR COMPROBANTE (LiquidarComprobante.java)");
                LiquidarComprobante.LiquidarComprobante_Entrada liquidar = new LiquidarComprobante.LiquidarComprobante_Entrada();
                liquidar.p_IdEmpresa = c.entrada.p_IdEmpresa;
                liquidar.p_IdUsuario = c.entrada.p_IdUsuario;
                liquidar.p_IdModulo = "TES";
                liquidar.p_SimularOperacion = "N";
                liquidar.p_UsaReglasNegocio = "S";
                liquidar.p_FechaMovimiento = fechaSistema;

                Map<String, Object> parametros = new HashMap<>();
                parametros.put("idsComprobantes", idsComprobante);
                List<LiquidarComprobante.ComprobanteLiquidar_Entrada> comprobantes =
                        sql.consultar(SQL_COMPROBANTE, parametros, LiquidarComprobante.ComprobanteLiquidar_Entrada.class);
                liquidar.p_ComprobanteLiquidar = comprobantes;

                log.info("Llamada a LiquidarComprobanteTesoreria");
                LiquidarComprobante.LiquidarComprobante_Salida salidaLiquidar = LiquidarComprobante.invocar(colaboradores, liquidar);
                log.info("Fin LiquidarComprobanteTesoreria");

                String codigoMensajeExito = "";
                String mensajeExito = "";
                if (MensajeSalida.contarErrores(salidaLiquidar.P_OUT_MENSAJES) == 0) {
                    codigoMensajeExito = "UI_LIQUIDACION_EXITOSA";
                    mensajeExito = diccionario.mensaje(1L, codigoMensajeExito, Plataforma.valores().con("Liquidacion", comprobantes.size()));
                }
                for (LiquidarComprobante.ResultadoProceso_Salida resultado : salidaLiquidar.P_OUT_RESULTADOPROCESO) {
                    if (resultado.Resultado.equalsIgnoreCase("LIQ")) {
                        out.P_OUT_MENSAJES.add(new MensajeSalida(resultado.Id, MensajeSalida.MENSAJE_INFORMACION, codigoMensajeExito, mensajeExito));
                    }
                }
            }

            for (CrearIngresoEgreso.IngresoEgresoGenerado_Salida gen : salida.P_OUT_INGRESOEGRESOGENERADO) {
                Long id = mapIdSolicitud.get(gen.NumeroReferencia);
                for (RegistroRecaudacion solicitud : solicitudProcesar) {
                    if (solicitud.IdMandatoPAC.equals(id)) {
                        agregarMovimiento(solicitud, gen.IdIngresoEgreso, Constantes.MODULO_TESORERIA);

                        String codigoMensajeExito = "";
                        if (gen.IdTipoMovimiento.equalsIgnoreCase(Constantes.INGRESO_TESORERIA)) {
                            codigoMensajeExito = "UI_CREADO_MOVIMIENTO_INGRESO";
                        } else if (gen.IdTipoMovimiento.equalsIgnoreCase(Constantes.EGRESO_TESORERIA)) {
                            codigoMensajeExito = "UI_CREADO_MOVIMIENTO_EGRESO";
                        }
                        String mensajeExito = diccionario.mensaje(solicitud.IdEmpresa, codigoMensajeExito, Plataforma.valores().con("IdIngresoEgreso", gen.IdIngresoEgreso));
                        out.P_OUT_MENSAJES.add(new MensajeSalida(solicitud.IdMandatoPAC, MensajeSalida.MENSAJE_INFORMACION, codigoMensajeExito, mensajeExito));
                    }
                }
            }
        }
    }

    // ------------------------------------------------------------------------------------------------ 2 CUENTA DE INVERSION

    /** Charge to the broker, credit to the client and credit to the broker per request through CrearCargoAbono. */
    public void cuentaInversion(Corrida c, List<RegistroRecaudacion> solicitudProcesar, String monedaSistema, Long idEmpleadoDigitador, Long idPortafolioCorredor,
            BigDecimal montoTotalCorredora) throws Exception {
        SalidaRecaudacionTraspaso out = c.out;
        String fechaSistema = c.fechaSistema;
        Map<Long, Long> ids = new HashMap<>();
        // LEGACY DEFECT kept (see README TES-B-277): the counter is never incremented, so the broker counter-entry is added for EVERY request
        Long contador = 0L;
        CrearCargoAbono.CrearCargoAbonoEntrada entrada = new CrearCargoAbono.CrearCargoAbonoEntrada();

        Long i = 1L;
        for (RegistroRecaudacion solicitud : solicitudProcesar) {
            CrearCargoAbono.ObjCargoAbonoAGenerar obj = new CrearCargoAbono.ObjCargoAbonoAGenerar();
            CrearCargoAbono.ObjCargoAbonoAGenerar abonoCorredora = new CrearCargoAbono.ObjCargoAbonoAGenerar();
            CrearCargoAbono.ObjCargoAbonoAGenerar abonoCliente = new CrearCargoAbono.ObjCargoAbonoAGenerar();
            CrearCargoAbono.ObjCargoAbonoAGenerar cargoCorredora = new CrearCargoAbono.ObjCargoAbonoAGenerar();

            obj.NumeroCargoAbono = i;
            obj.IdPortafolio = idPortafolioCorredor;
            obj.IdMoneda = monedaSistema;
            obj.IdTipoMovimiento = Constantes.CARGO_CUENTA_INVERSION;
            obj.Monto = solicitud.MontoDeposito;
            obj.IdMonedaOperacion = monedaSistema;
            obj.MontoOperacion = solicitud.MontoDeposito;
            obj.IdConcepto = solicitud.IdConcepto;
            obj.Descripcion = solicitud.DescripccionConcepto;
            obj.IdCondicionDeLiquidacion = "";
            obj.FechaCondicionDeLiquidacion = fechaSistema;
            obj.IdEjecutivo = idEmpleadoDigitador;
            obj.IdSucursal = 0L;
            obj.IdTipoProducto = Constantes.TIPO_PRODUCTO;
            obj.IdTipoOperacion = Constantes.TIPO_OPERACION_APORTE_FONDO_MUTUO;
            obj.IdOperacion = solicitud.IdTraspaso;
            obj.FechaOperacion = fechaSistema;
            obj.FechaLiquidacionOperacion = fechaSistema;
            obj.TipoCambioOperacion = new BigDecimal(0);
            obj.IdTipoComprobante = Constantes.TIPO_COMPROBANTE_CUENTA_INVERSION;
            obj.IdComprobante = solicitud.IdTraspaso;
            obj.FolioComprobante = solicitud.IdTraspaso.toString();
            obj.IdComprobanteDetalle = solicitud.IdTraspaso;
            obj.IdTipoMovimientoAsociado = "";
            obj.IdMovimientoAsociado = 0L;
            obj.FechaComprobante = fechaSistema;
            obj.IdInstrumento = 0L;
            obj.IdTipoInstrumento = "";
            obj.Nemo = "";
            obj.EsCargoAbonoPorTraspaso = Constantes.NO;
            obj.GeneroContraparteEnTesoreria = Constantes.NO;
            obj.CertificadoAutomatico = Constantes.SI;
            ids.put(i, solicitud.IdMandatoPAC);
            i++;

            if (contador == 0) {
                Utils.copyPojo(obj, abonoCorredora);
                abonoCorredora.NumeroCargoAbono = i;
                abonoCorredora.IdTipoMovimiento = Constantes.ABONO_CUENTA_INVERSION;
                abonoCorredora.Monto = montoTotalCorredora;
                abonoCorredora.MontoOperacion = montoTotalCorredora;
                entrada.p_CargoAbonoAGenerar.add(abonoCorredora);
                ids.put(i, solicitud.IdTraspaso);
                i++;
            }

            Utils.copyPojo(obj, abonoCliente);
            abonoCliente.NumeroCargoAbono = i;
            abonoCliente.Monto = solicitud.MontoDeposito;
            abonoCliente.MontoOperacion = solicitud.MontoDeposito;
            abonoCliente.IdPortafolio = solicitud.IdPortafolio;
            abonoCliente.IdTipoMovimiento = Constantes.ABONO_CUENTA_INVERSION;
            entrada.p_CargoAbonoAGenerar.add(abonoCliente);
            ids.put(i, solicitud.IdMandatoPAC);
            i++;

            // the charge keeps the number of the template line (NumeroCargoAbono is not renumbered, as in the legacy)
            Utils.copyPojo(obj, cargoCorredora);
            cargoCorredora.IdTipoMovimiento = Constantes.CARGO_CUENTA_INVERSION;
            cargoCorredora.Monto = solicitud.MontoDeposito;
            cargoCorredora.MontoOperacion = solicitud.MontoDeposito;
            cargoCorredora.IdPortafolio = idPortafolioCorredor;
            entrada.p_CargoAbonoAGenerar.add(cargoCorredora);
            ids.put(i, solicitud.IdMandatoPAC);
            i++;
        }

        entrada.p_IdEmpresa = c.entrada.p_IdEmpresa;
        entrada.p_IdUsuario = c.entrada.p_IdUsuario;
        entrada.p_IdModulo = c.entrada.p_IdModulo;
        entrada.p_simularOperacion = c.entrada.p_simular;
        entrada.p_UsaReglasNegocio = "S";
        entrada.p_FechaSistema = fechaSistema;
        entrada.p_EsIngresoManual = "N";

        log.info("Llamada a CrearCargoAbono");
        CrearCargoAbono.CrearCargoAbonoSalida salida = CrearCargoAbono.invocar(colaboradores, entrada);
        log.info("Fin CrearCargoAbono");

        out.P_OUT_ERRORESCONTADOR += salida.P_OUT_ERRORESCONTADOR;

        for (MensajeSalida mje : salida.P_OUT_MENSAJES) {
            if (MensajeSalida.MENSAJE_ERROR.equals(mje.tipo)) {
                Long id = ids.get(mje.id);
                if (id != null) {
                    for (RegistroRecaudacion solicitud : solicitudProcesar) {
                        if (solicitud.IdMandatoPAC.equals(id)) {
                            rechazar(out, solicitud, mje.descripcion, mje.codigo);
                            solicitud.EstadoProceso = Constantes.ESTADO_RECHAZADO;
                            solicitud.Codigo = mje.codigo;
                            solicitud.DescripcionSolicitud = mje.descripcion;
                        }
                    }
                }
            }
        }

        for (CrearCargoAbono.ObjCargoAbonoGenerado gen : salida.P_OUT_CARGOABONOGENERADO) {
            Long id = ids.get(gen.NumeroCargoAbono);
            for (RegistroRecaudacion solicitud : solicitudProcesar) {
                // LEGACY DEFECT kept (see README TES-B-277): a register is compared with a Long, never equal: MovimientoGenerado is never filled here
                if (solicitud.equals(id)) {
                    agregarMovimiento(solicitud, gen.IdCargoAbonoMovimiento, Constantes.MODULO_CTA_INVERSION);
                }
            }
        }
    }

    // ------------------------------------------------------------------------------------------------ 3 FONDOS MUTUOS

    /** One contribution request per distribution line (CSC detail / SAAPV fund line) through CrearSolicitudAporteRescateNormal; USD funds are converted first. */
    public void fondosMutuos(Corrida c, List<RegistroRecaudacion> solicitudProcesar, List<ContratoSuscripcionCuotaDetalle> distribucionCSC,
            List<DistribucionInversion> distribucionSAPV, String monedaSistema) throws Exception {
        SalidaRecaudacionTraspaso out = c.out;
        long iterator = 1L;
        Map<Long, String> ids = new HashMap<>();
        CrearSolicitudAporteRescateNormal.EntradaCrearSolicitudAporteRescateNormal entrada = new CrearSolicitudAporteRescateNormal.EntradaCrearSolicitudAporteRescateNormal();

        log.info("INFO::ProcesarDistribucion.getSalidaFFMM - Recorre Solicitudes");
        for (RegistroRecaudacion solicitud : solicitudProcesar) {
            if (!(solicitud.FolioCSC.equals("0") || solicitud.FolioCSC.equals(""))) {
                for (ContratoSuscripcionCuotaDetalle cscd : distribucionCSC) {
                    if (cscd.IdFolioCSC.equalsIgnoreCase(solicitud.FolioCSC)) {
                        CrearSolicitudAporteRescateNormal.InSolicitudesAporteRescate s = solicitudBase(iterator, solicitud);
                        s.IdPortafolio = cscd.IdPortafolio;
                        s.IdSerieFondo = cscd.Serie;
                        s.Monto = cscd.Monto;
                        s.IdMoneda = cscd.IdMonedaInstrumento;
                        s.IdRegimenTributario = cscd.Regimen;
                        s.Canal = cscd.Canal;
                        s.TipoContratoOperacion = "CSC";
                        s.FolioContratoOperacion = cscd.IdFolioCSC;
                        ids.put(iterator, solicitud.FolioCSC);
                        entrada.p_SolicitudesAporteRescate.add(s);
                        iterator++;
                    }
                }
            } else {
                for (DistribucionInversion di : distribucionSAPV) {
                    if (di.FolioContratoSAAPV.equals(solicitud.FolioAPV.toString()) && di.IdTipoInstrumento.equals("FFM")) {
                        CrearSolicitudAporteRescateNormal.InSolicitudesAporteRescate s = solicitudBase(iterator, solicitud);
                        s.IdPortafolio = solicitud.IdPortafolio;
                        s.IdSerieFondo = di.Nemotecnico;
                        s.Monto = di.MontoPesos;
                        s.IdMoneda = di.IdMonedaInstrumento;
                        s.IdRegimenTributario = di.Codigo;
                        s.Canal = di.Canal;
                        s.TipoContratoOperacion = "SAAPV";
                        s.FolioContratoOperacion = di.FolioContratoSAAPV;
                        ids.put(iterator, solicitud.FolioAPV + "");
                        entrada.p_SolicitudesAporteRescate.add(s);
                        iterator++;
                    }
                }
            }
        }

        // funds in dollars: the amount and the number of units come from CalcularMontoNumeroCuota and two spot payment legs are added
        CalcularMontoNumeroCuota.EntradaCalcularMontoNumeroCuota entradaCalculo = new CalcularMontoNumeroCuota.EntradaCalcularMontoNumeroCuota();
        entradaCalculo.p_IdEmpresa = c.entrada.p_IdEmpresa;
        entradaCalculo.p_Series = entrada.p_SolicitudesAporteRescate.stream()
                .filter(solicitud -> Constantes.DOLAR.equals(solicitud.IdMoneda))
                .map(solicitud -> {
                    CalcularMontoNumeroCuota.Serie serie = new CalcularMontoNumeroCuota.Serie();
                    serie.IdInterno = solicitud.IdInterno;
                    serie.Fecha = c.fechaSistema;
                    serie.IdSerieFondo = solicitud.IdSerieFondo;
                    serie.CalculoPorMontoCuota = solicitud.CalculoPorMontoCuota;
                    serie.Monto = solicitud.Monto;
                    serie.NumeroCuotas = solicitud.NumeroCuotas;
                    serie.IdMonedaFondo = solicitud.IdMoneda;
                    serie.IdMonedaInversion = "CLP";
                    serie.TipoSolicitud = solicitud.TipoSolicitud;
                    serie.TipoFactorAjuste = Constantes.UF;
                    serie.IdEmpresa = c.entrada.p_IdEmpresa;
                    return serie;
                })
                .collect(Collectors.toList());

        CalcularMontoNumeroCuota.SalidaCalcularMontoNumeroCuota salidaCalculo = CalcularMontoNumeroCuota.invocar(colaboradores, entradaCalculo);
        Map<Long, CalcularMontoNumeroCuota.Resultado> resultados = salidaCalculo.P_OUT_RESULTADO.stream()
                .collect(Collectors.toMap(resultado -> resultado.IdInterno, resultado -> resultado));
        long indexSpot = 1L;
        for (CrearSolicitudAporteRescateNormal.InSolicitudesAporteRescate solicitud : entrada.p_SolicitudesAporteRescate) {
            if (!Constantes.DOLAR.equals(solicitud.IdMoneda)) {
                continue;
            }
            CalcularMontoNumeroCuota.Resultado resultado = resultados.get(solicitud.IdInterno);
            if (resultado != null) {
                CrearSolicitudAporteRescateNormal.InFormaPagoSpot pago = new CrearSolicitudAporteRescateNormal.InFormaPagoSpot();
                pago.IdInternoPagoOperacion = indexSpot++;
                pago.IdComprobantePagoOperacion = 0L;
                pago.IdTipoMovimiento = "MOVEGR";
                pago.IdFormaDePago = "VENSPT_CIN_USD_0";
                pago.IdOperacion = solicitud.IdInterno;
                pago.RecibePagaPortafolio = "P";
                pago.IdMoneda = solicitud.IdMoneda;
                pago.IdMonedaLiquidacion = solicitud.IdMoneda;
                pago.MontoFormaDePago = resultado.Monto;
                pago.AfectarTesoreria = "N";
                entrada.p_FormaPagoSpot.add(pago);

                CrearSolicitudAporteRescateNormal.InFormaPagoSpot pago2 = new CrearSolicitudAporteRescateNormal.InFormaPagoSpot();
                pago2.IdInternoPagoOperacion = indexSpot++;
                pago2.IdComprobantePagoOperacion = 0L;
                pago2.IdTipoMovimiento = "MOVING";
                pago2.IdFormaDePago = "COMSPT_CIN_CLP_0";
                pago2.IdOperacion = solicitud.IdInterno;
                pago2.RecibePagaPortafolio = "R";
                pago2.IdMoneda = "CLP";
                pago2.IdMonedaLiquidacion = "CLP";
                pago2.MontoFormaDePago = solicitud.Monto;
                pago2.AfectarTesoreria = "N";
                entrada.p_FormaPagoSpot.add(pago2);

                solicitud.Monto = resultado.Monto;
                solicitud.NumeroCuotas = resultado.NumeroCuotas;
                solicitud.IdMonedaPago = monedaSistema;
                solicitud.ForzarSpot = Constantes.SI;
            } else {
                solicitud.Monto = BigDecimal.ZERO;
            }
        }

        entrada.p_IdEmpresa = c.entrada.p_IdEmpresa;
        entrada.p_IdUsuario = c.entrada.p_IdUsuario;
        entrada.p_IdModulo = c.entrada.p_IdModulo;
        entrada.p_SimularOperacion = c.entrada.p_simular;
        entrada.p_UsaReglasNegocio = Constantes.SI;
        entrada.p_IdTipoCanalIngreso = Constantes.CANAL_AGENTE;
        entrada.p_NumeroDividendo = 0L;
        entrada.p_IdOrigenProceso = "PAC";

        log.info("ENTRADA::ProcesarDistribucion.getSalidaFFMM.crearSolicitudAporteRescateNormal - Llamada CrearSolicitudAporteRescateNormal");
        CrearSolicitudAporteRescateNormal.SalidaCrearSolicitudAporteRescateNormal salida = CrearSolicitudAporteRescateNormal.invocar(colaboradores, entrada);

        out.P_OUT_ERRORESCONTADOR += salida.P_OUT_ERRORESCONTADOR;
        out.P_OUT_MENSAJES.addAll(salida.P_OUT_MENSAJES.stream().filter(m -> !MensajeSalida.MENSAJE_ERROR.equals(m.tipo)).collect(Collectors.toList()));

        for (MensajeSalida mje : salida.P_OUT_MENSAJES) {
            String id = ids.get(mje.id);
            if (id != null) {
                for (RegistroRecaudacion solicitud : solicitudProcesar) {
                    try {
                        if (solicitud.FolioCSC.equals(id) || solicitud.FolioAPV.equalsIgnoreCase(id) || solicitud.FolioAPV.equals(id)) {
                            if (MensajeSalida.MENSAJE_ERROR.equals(mje.tipo)) {
                                String descripcion = mje.descripcion + ", Documento Identidad: " + solicitud.DocumentoIdentidad + ", IdMandatoPAC: " + solicitud.IdMandatoPAC;
                                solicitud.EstadoRendicion = descripcion;
                                solicitud.EstadoProcesoRendicion = mje.codigo;
                                out.P_OUT_ERRORESCONTADOR++;
                                out.P_OUT_MENSAJES.add(new MensajeSalida(solicitud.IdTraspaso, mje.tipo, mje.codigo, descripcion));
                                out.P_OUT_SOLICITUDES_NO_PROCESADAS.add(solicitud);
                            } else {
                                // LEGACY DEFECT kept (see README TES-B-277): the non-error messages are added here again after the bulk addAll above
                                out.P_OUT_MENSAJES.add(new MensajeSalida(solicitud.IdTraspaso, mje.tipo, mje.codigo, mje.descripcion));
                            }
                        }
                    } catch (Exception e) {
                        // LEGACY: any failure of the per-request reporting (e.g. a null folio) is swallowed
                        log.debug("ProcesarDistribucion.getSalidaFFMM: message not attributed to a request", e);
                    }
                }
            }
        }

        if (salida.P_OUT_ERRORESCONTADOR == 0) {
            for (ResultadoSolicitud res : salida.P_OUT_RESULTADO) {
                String codigoMensajeExito = "";
                if (res.IdTipoMovimientoFondoMutuo.equalsIgnoreCase(Constantes.APORTE_FONDO_MUTUO)) {
                    codigoMensajeExito = "UI_SE_CREO_EL_APORTE_FONDO_MUTUO_N";
                } else if (res.IdTipoMovimientoFondoMutuo.equalsIgnoreCase(Constantes.RESCATE_FONDO_MUTUO)) {
                    codigoMensajeExito = "UI_SE_CREO_EL_RESCATE_FONDO_MUTUO_N";
                }
                String mensajeExito = diccionario.mensaje(c.entrada.p_IdEmpresa, codigoMensajeExito, Plataforma.valores().con("IdSolicitudAporteRescate", res.IdSolicitudAporteRescate));
                out.P_OUT_MENSAJES.add(new MensajeSalida(225L, MensajeSalida.MENSAJE_INFORMACION, codigoMensajeExito, mensajeExito));
            }
            for (ResultadoSolicitud gen : salida.P_OUT_RESULTADO) {
                for (RegistroRecaudacion solicitud : solicitudProcesar) {
                    agregarMovimiento(solicitud, gen.IdSolicitudAporteRescate, Constantes.MODULO_CTA_INVERSION);
                }
            }
        }
    }

    private CrearSolicitudAporteRescateNormal.InSolicitudesAporteRescate solicitudBase(long iterator, RegistroRecaudacion solicitud) {
        CrearSolicitudAporteRescateNormal.InSolicitudesAporteRescate s = new CrearSolicitudAporteRescateNormal.InSolicitudesAporteRescate();
        s.IdInterno = iterator;
        s.IdCliente = solicitud.IdCliente;
        s.TipoSolicitud = Constantes.TIPO_SOLICITUD_APORTE;
        s.CalculoPorMontoCuota = Constantes.CALCULO_POR_MONTO;
        s.EsPorMontoLiquido = Constantes.NO;
        s.NumeroCuotas = BigDecimal.ZERO;
        s.IdMedioDePago = Constantes.CUENTA_INVERSION;
        s.IdPreferenciaPagoPortafolio = 0L;
        s.Visado = Constantes.NO;
        s.SolicitudBatch = Constantes.NO;
        s.OrigenOperacion = Constantes.ORIGEN_MANUAL;
        s.FechaOperacion = "0001-01-01";
        s.Glosa = solicitud.Observacion;
        s.IndicadorMLT = Constantes.NO;
        s.IdSerieFondoDestino = "";
        s.IdEmisorDestino = "";
        s.IdAutorizadoOrigen = 0L;
        s.Porcentaje = BigDecimal.ZERO;
        s.FolioContrato = 0L;
        s.IngresoAutomatico = Constantes.SI;
        return s;
    }

    // ------------------------------------------------------------------------------------------------ 4 ORDENES

    /** Stock purchase orders for the ACN lines of the SAAPV distribution: prices, quantities (CalcularMontoYCobrosOrden) and creation (CrearOrdenes). */
    public void ordenes(Corrida c, List<RegistroRecaudacion> solicitudProcesar, List<DistribucionInversion> distribucionSAPV) throws Exception {
        SalidaRecaudacionTraspaso out = c.out;
        long iterator = 1L;

        List<CrearOrdenes.ObjOrdenesEntrada> ordenes = new ArrayList<>();
        List<CalcularMontoYCobrosOrden.Orden_Entrada> calculoMontoOrden = new ArrayList<>();
        List<CalcularMontoYCobrosOrden.Orden_Entrada> calculos = new ArrayList<>();

        ConsultarInstrumentoPrecioBolsa.ConsultarInstrumentoPrecioBolsa_Salida salidaPrecioBolsa = precios(c, distribucionSAPV);

        Map<Long, ConsultarInstrumentoPrecioBolsa.DatosInstrumentosPrecioBolsa_Salida> instrumentos = new HashMap<>();
        for (ConsultarInstrumentoPrecioBolsa.DatosInstrumentosPrecioBolsa_Salida precio : salidaPrecioBolsa.P_OUT_INSTRUMENTO) {
            instrumentos.putIfAbsent(precio.IdInstrumento, precio);
        }

        if (salidaPrecioBolsa.P_OUT_ERRORESCONTADOR != null) {
            out.P_OUT_ERRORESCONTADOR += salidaPrecioBolsa.P_OUT_ERRORESCONTADOR;
        }

        if (salidaPrecioBolsa.P_OUT_ERRORESCONTADOR == 0 && !salidaPrecioBolsa.P_OUT_INSTRUMENTO.isEmpty()) {
            long y = 1L;
            for (DistribucionInversion di : distribucionSAPV) {
                ConsultarInstrumentoPrecioBolsa.DatosInstrumentosPrecioBolsa_Salida precio = instrumentos.get(di.IdInstrumento);
                if (precio != null) {
                    calculos.add(calculoOrden(di, c.fechaSistema, precio, y));
                    y++;
                }
            }
        }

        for (DistribucionInversion di : distribucionSAPV) {
            if (di.IdTipoInstrumento.equals("ACN")) {
                for (CalcularMontoYCobrosOrden.Orden_Entrada calculo : calculos) {
                    if (calculo.IdInstrumento.equals(di.IdInstrumento) && calculo.IdPortafolio.equals(di.IdPortafolio)) {
                        ConsultarInstrumentoPrecioBolsa.DatosInstrumentosPrecioBolsa_Salida precio = instrumentos.get(calculo.IdInstrumento);
                        if (precio != null) {
                            calculoMontoOrden.add(calculo);
                            ordenes.add(ordenDeCompra(iterator, di, calculo, precio));
                            iterator++;
                        }
                    }
                }
            }
        }

        if (!ordenes.isEmpty()) {
            CalcularMontoYCobrosOrden.CalcularMontoYCobrosOrden_Entrada entradaCalculo = new CalcularMontoYCobrosOrden.CalcularMontoYCobrosOrden_Entrada();
            entradaCalculo.p_IdEmpresa = c.entrada.p_IdEmpresa;
            entradaCalculo.p_IdUsuario = c.entrada.p_IdUsuario;
            entradaCalculo.p_IdModulo = c.entrada.p_IdModulo;
            entradaCalculo.p_Ordenes = calculoMontoOrden;
            CalcularMontoYCobrosOrden.CalcularMontoYCobrosOrden_Salida salidaCalculo = CalcularMontoYCobrosOrden.invocar(colaboradores, entradaCalculo);

            for (int i = salidaCalculo.p_out_Ordenes.size() - 1; i >= 0; i--) {
                for (int j = ordenes.size() - 1; j >= 0; j--) {
                    if (salidaCalculo.p_out_Ordenes.get(i).IdInterno.toString().equals(ordenes.get(j).IdInterno.toString())) {
                        ordenes.get(j).Cantidad = salidaCalculo.p_out_Ordenes.get(i).CantidadOperacion;
                    }
                }
            }

            CrearOrdenes.CrearOrdenesEntrada entradaOrdenes = new CrearOrdenes.CrearOrdenesEntrada();
            entradaOrdenes.p_IdEmpresa = c.entrada.p_IdEmpresa;
            entradaOrdenes.p_IdUsuario = c.entrada.p_IdUsuario;
            entradaOrdenes.p_IdModulo = c.entrada.p_IdModulo;
            entradaOrdenes.p_simularOperacion = c.entrada.p_simular;
            entradaOrdenes.p_UsaReglasNegocio = "S";
            entradaOrdenes.p_Ordenes = ordenes;

            log.info("Llamada a CrearOrdenes");
            CrearOrdenes.CrearOrdenesSalida salida = CrearOrdenes.invocar(colaboradores, entradaOrdenes);
            log.info("Fin CrearOrdenes");

            // LEGACY DEFECT kept (see README TES-B-284): every message of the order service is attributed to EVERY request of the batch (message x request)
            for (MensajeSalida mje : salida.P_OUT_MENSAJES) {
                for (RegistroRecaudacion solicitud : solicitudProcesar) {
                    if (MensajeSalida.MENSAJE_ERROR.equals(mje.tipo)) {
                        String descripcion = mje.descripcion + ", Documento Identidad: " + solicitud.DocumentoIdentidad + ", IdMandatoPAC: " + solicitud.IdMandatoPAC;
                        solicitud.EstadoRendicion = descripcion;
                        solicitud.EstadoProcesoRendicion = mje.codigo;
                        out.P_OUT_ERRORESCONTADOR++;
                        out.P_OUT_MENSAJES.add(new MensajeSalida(solicitud.IdTraspaso, mje.tipo, mje.codigo, descripcion));
                        out.P_OUT_SOLICITUDES_NO_PROCESADAS.add(solicitud);
                    } else {
                        out.P_OUT_MENSAJES.add(new MensajeSalida(solicitud.IdTraspaso, mje.tipo, mje.codigo, mje.descripcion));
                    }
                    if (MensajeSalida.MENSAJE_ERROR.equals(mje.tipo)) {
                        solicitud.EstadoProceso = Constantes.ESTADO_RECHAZADO;
                        solicitud.Codigo = mje.codigo;
                        solicitud.DescripcionSolicitud = mje.descripcion;
                    }
                }
            }

            for (CrearOrdenes.ObjRESULTADO gen : salida.P_OUT_RESULTADO) {
                for (RegistroRecaudacion solicitud : solicitudProcesar) {
                    agregarMovimiento(solicitud, gen.IdOrden, Constantes.MODULO_CTA_INVERSION);
                }
            }
        }
    }

    /** Legacy {@code getInstrumentos}: one price query per run for the ACN lines (every instrument carries IdInterno 1, as in the legacy). */
    private ConsultarInstrumentoPrecioBolsa.ConsultarInstrumentoPrecioBolsa_Salida precios(Corrida c, List<DistribucionInversion> distribucionSAPV) {
        ConsultarInstrumentoPrecioBolsa.ConsultarInstrumentoPrecioBolsa_Entrada entrada = new ConsultarInstrumentoPrecioBolsa.ConsultarInstrumentoPrecioBolsa_Entrada();
        entrada.p_IdEmpresa = c.entrada.p_IdEmpresa;
        entrada.p_IdUsuario = c.entrada.p_IdUsuario;
        long iteradorInstrumento = 1L;
        entrada.p_Instrumentos = distribucionSAPV.stream()
                .filter(di -> di.IdTipoInstrumento.equals("ACN"))
                .map(di -> {
                    ConsultarInstrumentoPrecioBolsa.Instrumento instrumento = new ConsultarInstrumentoPrecioBolsa.Instrumento();
                    instrumento.IdInterno = iteradorInstrumento;
                    instrumento.IdInstrumento = di.IdInstrumento;
                    instrumento.IdBolsa = "BCS";
                    instrumento.Fecha = c.fechaSistema;
                    instrumento.IdCondicionDeLiquidacion = "T2";
                    instrumento.IdEmpresa = c.entrada.p_IdEmpresa;
                    return instrumento;
                })
                .collect(Collectors.toList());
        return ConsultarInstrumentoPrecioBolsa.invocar(colaboradores, entrada);
    }

    private CalcularMontoYCobrosOrden.Orden_Entrada calculoOrden(DistribucionInversion di, String fechaSistema,
            ConsultarInstrumentoPrecioBolsa.DatosInstrumentosPrecioBolsa_Salida precio, long iterator) {
        CalcularMontoYCobrosOrden.Orden_Entrada o = new CalcularMontoYCobrosOrden.Orden_Entrada();
        o.PrecioOrden = precio.PrecioCierre;
        o.IdInterno = iterator;
        o.CantidadOrden = BigDecimal.ZERO;
        o.EsFungible = "N";
        o.IdBolsa = "BCS";
        o.IdCorredorCorresponsal = null;
        o.IdInstrumento = di.IdInstrumento;
        o.IdInstrumentoSubyacente = 0L;
        o.IdMonedaLiquidacionOrden = precio.IdMoneda;
        o.IdPortafolio = di.IdPortafolio;
        o.IdTipoProducto = "INAC";
        o.IdTipoOperacion = "VENACN";
        o.IdTipoCanalIngreso = "AGE";
        o.FechaOperacion = fechaSistema;
        o.MontoOrden = BigDecimal.ZERO;
        o.TasaSimultanea = BigDecimal.ZERO;
        o.TasaRFIF = BigDecimal.ZERO;
        o.TasaVentaCorta = BigDecimal.ZERO;
        o.TipoCambio = BigDecimal.ZERO;
        o.ValorRescateOrden = BigDecimal.ZERO;
        o.PlazoSimultanea = 0L;
        o.PlazoVentaCorta = 0L;
        o.PlazoIF = 0L;
        o.EsAPlazo = "N";
        o.MontoTotal = di.MontoPesos;
        o.EsPorMonto = "S";
        o.TasaReporte = BigDecimal.ZERO;
        o.TasaReportado = BigDecimal.ZERO;
        o.PlazoReporte = 0L;
        o.BaseTasa = 0L;
        return o;
    }

    private CrearOrdenes.ObjOrdenesEntrada ordenDeCompra(long iterator, DistribucionInversion di, CalcularMontoYCobrosOrden.Orden_Entrada calculo,
            ConsultarInstrumentoPrecioBolsa.DatosInstrumentosPrecioBolsa_Salida precio) {
        CrearOrdenes.ObjOrdenesEntrada orden = new CrearOrdenes.ObjOrdenesEntrada();
        orden.IdInterno = iterator;
        orden.FechaRecepcion = "0001-01-01 00:00:00";
        orden.FechaVigencia = "0001-01-01";
        orden.FechaEmision = "0001-01-01";
        orden.FechaOperacion = "0001-01-01";
        orden.IdCondicionDeLiquidacion = "T2";
        orden.IdTipoOperacion = "COMACN";
        orden.IdTipoVigencia = "OP";
        orden.Glosa = "";
        orden.MontoNeto = di.MontoPesos;
        orden.EsPorMonto = "S";
        orden.Cantidad = BigDecimal.ZERO;
        orden.ValorRescate = BigDecimal.ZERO;
        orden.IdBoveda = "DCV";
        orden.IdFormaCustodia = "CUS";
        orden.IdPorcentajeVisibilidad = 1L;
        orden.PrecioLimite = precio.PrecioCierre;
        orden.EsFacturaInmediata = "N";
        orden.IdTipoPrecio = "M";
        orden.IdPortafolio = calculo.IdPortafolio;
        orden.IdTipoCanalIngreso = "AGE";
        orden.IdTipoReajuste = "";
        orden.IdEmisor = calculo.IdEmisor;
        orden.IdBolsa = "BCS";
        orden.EspecificaBolsa = "S";
        orden.IdMonedaLiquidacionOrden = precio.IdMoneda;
        orden.TipoCambio = BigDecimal.ZERO;
        orden.IdInstrumento = calculo.IdInstrumento;
        orden.IdInstrumentoSubyacente = 0L;
        orden.IdCondicionDeLiquidacionPlazo = "CN";
        orden.TasaVentaCorta = BigDecimal.ZERO;
        orden.PlazoVentaCorta = 0L;
        orden.EsVentaCorta = "N";
        orden.TasaSimultanea = BigDecimal.ZERO;
        orden.PlazoSimultanea = 0L;
        orden.PlazoIF = 0L;
        orden.TasaRFIF = BigDecimal.ZERO;
        orden.EsOD = "N";
        orden.EsFungible = "N";
        orden.IdTipoLibroOrden = "NORMAL";
        orden.IdLibroOrdenes = null;
        orden.IdCuentaCustodioPortafolio = null;
        orden.IdCorredorCorresponsal = null;
        orden.IdPersonaAutorizadoDarOrdenes = 0L;
        orden.IdTipoCanalDistribucion = "MAN";
        orden.EsOrdenEspecial = "N";
        orden.PagoCobroCuentaInversion = "S";
        orden.IdPreferenciaPagoPortafolio = calculo.IdPortafolio;
        orden.BaseTasa = 0L;
        orden.TipoInteres = "";
        orden.IdMecanismoCaptacion = "OM";
        return orden;
    }

    // ------------------------------------------------------------------------------------------------ shared

    /** A request whose collaborator answered an error: it is rejected, counted, reported and listed as not processed. */
    private void rechazar(SalidaRecaudacionTraspaso out, RegistroRecaudacion solicitud, String descripcion, String codigo) {
        solicitud.EstadoRendicion = descripcion;
        solicitud.EstadoProcesoRendicion = codigo;
        out.P_OUT_ERRORESCONTADOR++;
        out.P_OUT_MENSAJES.add(new MensajeSalida(solicitud.IdMandatoPAC, MensajeSalida.MENSAJE_ERROR, solicitud.EstadoProcesoRendicion, solicitud.EstadoRendicion));
        out.P_OUT_SOLICITUDES_NO_PROCESADAS.add(solicitud);
    }

    private void agregarMovimiento(RegistroRecaudacion solicitud, Long idSecuenciaMovimiento, String modulo) {
        InMovimientoGenerado movimiento = new InMovimientoGenerado();
        movimiento.IdSecuenciaMovimiento = idSecuenciaMovimiento;
        movimiento.IdEmpresa = solicitud.IdEmpresa;
        movimiento.IdUsuario = solicitud.IdUsuario;
        movimiento.IdModulo = modulo;
        movimiento.FechaIngreso = solicitud.FechaIngreso;
        movimiento.IdSolicitud = solicitud.IdentServicio;
        movimiento.FolioAPV = solicitud.FolioAPV;
        movimiento.FolioCSC = solicitud.FolioCSC;
        solicitud.MovimientoGenerado.add(movimiento);
    }
}
