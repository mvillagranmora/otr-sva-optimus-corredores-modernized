package cl.bcs.apv.application.traspasos;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import cl.bcs.apv.api.casouso.CrearTraspasoTesoreria;
import cl.bcs.apv.api.comun.Mensaje;
import cl.bcs.apv.api.dto.creartraspasotesoreria.CrearTraspasoTesoreriaEntrada;
import cl.bcs.apv.api.dto.creartraspasotesoreria.CrearTraspasoTesoreriaResultado;
import cl.bcs.apv.api.dto.creartraspasotesoreria.TraspasoTesoreria;
import cl.bcs.apv.application.comun.Json;
import cl.bcs.apv.application.comun.Mensajes;
import cl.bcs.apv.application.comun.Sneaky;
import cl.bcs.apv.application.traspasos.colab.CuentaInversionB;
import cl.bcs.apv.application.traspasos.colab.CuentaInversionB.CrearCargoAbonoEntrada;
import cl.bcs.apv.application.traspasos.colab.CuentaInversionB.CrearCargoAbonoSalida;
import cl.bcs.apv.application.traspasos.colab.CuentaInversionB.ObjCargoAbonoAGenerar;
import cl.bcs.apv.application.traspasos.colab.TesoreriaComprobanteB;
import cl.bcs.apv.application.traspasos.colab.TesoreriaIngresoEgresoB;
import cl.bcs.apv.domain.port.out.Colaboradores;
import cl.bcs.apv.domain.port.out.traspasos.CrearTraspasoTesoreriaRepositorio;
import cl.bcs.apv.domain.port.out.traspasos.CrearTraspasoTesoreriaRepositorio.ContratoSAAPV;
import cl.bcs.apv.domain.port.out.traspasos.CrearTraspasoTesoreriaRepositorio.DatosCuentaInversion;
import cl.bcs.apv.domain.port.out.traspasos.CrearTraspasoTesoreriaRepositorio.DatosEmpresa;
import cl.bcs.apv.domain.port.out.traspasos.CrearTraspasoTesoreriaRepositorio.DestinoCtaBco;
import cl.bcs.apv.domain.port.out.traspasos.CrearTraspasoTesoreriaRepositorio.OrdenTraspaso;
import cl.bcs.apv.domain.port.out.traspasos.CrearTraspasoTesoreriaRepositorio.Portafolio;
import cl.bcs.apv.domain.port.out.traspasos.CrearTraspasoTesoreriaRepositorio.Retencion;

/**
 * Use case {@code CrearTraspasoTesoreria}: legacy {@code cl.bcs.APV.CrearTraspasoTesoreria}. For every traspaso it reads the balance of the client's
 * investment account (stored procedure), and for every order (RV or FFMM) with enough balance it builds the cargo/abono, the Tesoreria egress and the
 * payment forms; then it asks Cuenta Inversion for the cargo/abono, Tesoreria for the egress movements and the comprobante. The legacy never fills
 * its output lists {@code p_IngresoEgreso}/{@code p_FormaDePago}.
 */
@Transactional(rollbackFor = Exception.class)
public class CrearTraspasoTesoreriaServicio implements CrearTraspasoTesoreria {

    private final Colaboradores colaboradores;
    private final CrearTraspasoTesoreriaRepositorio repositorio;

    public CrearTraspasoTesoreriaServicio(Colaboradores colaboradores, CrearTraspasoTesoreriaRepositorio repositorio) {
        this.colaboradores = colaboradores;
        this.repositorio = repositorio;
    }

    /** Output row of the balance procedure (legacy {@code CuentaInversion}). */
    public static class CuentaInversion {
        public String CodigoCuentaInversion;
        public Long IdCliente;
        public String NombreCliente;
        public String DocumentoIdentidad;
        public Long IdPortafolio;
        public String NombrePortafolio;
        public String NombrePlan;
        public Long IdPlanTipoInversion;
        public Long IdProductoAgrupador;
        public String Producto;
        public Long IdRegimenTributario;
        public String Regimen;
        public String NombreAgrupador;
        public String FechaSaldo;
        public String IdMoneda;
        public String CodigoMoneda;
        public String NombreMoneda;
        public BigDecimal SaldoDisponible;
        public BigDecimal SaldoEnGarantia;
        public BigDecimal SaldoAPlazo;
        public BigDecimal SaldoRetiro;
        public String IdEstado;
    }

    private static final Type LISTA_CUENTA_INVERSION = new TypeToken<List<CuentaInversion>>() { }.getType();

    @Override
    public CrearTraspasoTesoreriaResultado ejecutar(CrearTraspasoTesoreriaEntrada in) {
        try {
            List<Mensaje> mensajes = logica(in);
            // the legacy call() sets the counter AFTER logica (also on the early returns); an exception never gets here
            return new CrearTraspasoTesoreriaResultado(mensajes, Mensajes.numeroErrores(mensajes), new ArrayList<>(), new ArrayList<>());
        } catch (Exception e) {
            throw Sneaky.lanzar(e);
        }
    }

    private List<Mensaje> logica(CrearTraspasoTesoreriaEntrada in) throws Exception {
        List<Mensaje> salida = new ArrayList<>();
        List<TesoreriaIngresoEgresoB.IngresoEgreso_Entrada> paramsIngresoEgreso = new ArrayList<>();
        List<TesoreriaIngresoEgresoB.FormaDePago_Entrada> paramsFormaDePago = new ArrayList<>();
        List<TesoreriaComprobanteB.FormaDePago_Entrada> paramsFormaPagoComprobante = new ArrayList<>();
        List<ObjCargoAbonoAGenerar> paramsCargoAbono = new ArrayList<>();
        Long contadorIds = 1L;
        Long contadorCARABO = 1L;

        List<DestinoCtaBco> ctaBco = repositorio.destinoCuentaBanco(in.idEmpresa());
        Long idCtaBco = ctaBco.get(0).DestinoCtaBco;
        List<DatosEmpresa> datosEmpresa = repositorio.portafolioEmpresa(in.idEmpresa());
        List<Retencion> configuracionRetencion = repositorio.configuracionRetencion();

        String fechaIngreso = "";
        Long idPortafolioCorredora = datosEmpresa.get(0).IdPortafolioDefecto;

        for (TraspasoTesoreria fila : in.parametros()) {
            fechaIngreso = fila.fecha();
            List<Portafolio> portafolios = repositorio.portafoliosPersona(fila.idPersonaCliente());
            if (portafolios.size() <= 0) {
                salida.add(new Mensaje(1L, "Warning", "CLIENTE_SIN_PORTAFOLIO", "Cliente no cuenta con Portafolio APV."));
                return salida;
            }

            List<DatosCuentaInversion> lstCtaInversion = new ArrayList<>();
            DatosCuentaInversion datosCuentaInversion = new DatosCuentaInversion();
            datosCuentaInversion.IdEjecutivo = null;
            datosCuentaInversion.IdCliente = portafolios.get(0).IdCliente;
            datosCuentaInversion.IdPortafolio = null;
            datosCuentaInversion.IdMoneda = "CLP";
            lstCtaInversion.add(datosCuentaInversion);

            List<CuentaInversion> cuentaInversion;
            repositorio.crearTablaCuentaInversionAConsultar();
            boolean tablaLimpia = false;
            try {
                if (lstCtaInversion.size() > 0) {
                    repositorio.insertarCuentaInversionAConsultar(lstCtaInversion);
                }

                // balance of the investment account: stored procedure ConsultarSaldoCuentaInversionCliente (the temporary table above is its input)
                JsonObject entradaSaldo = Json.objeto()
                        .poner("p_IdEmpresa", in.idEmpresa())
                        .poner("p_IdUsuario", in.idUsuario())
                        .poner("p_IdModulo", in.idModulo())
                        .poner("p_UsaReglasNegocio", "N")
                        .poner("p_IncluyeOtrosSaldos", "N")
                        .poner("p_TipoAgrupador", "PO")
                        .poner("p_CuentaInversionAConsultar", lstCtaInversion)
                        .poner("p_FechaDesde", fechaIngreso)
                        .poner("p_FechaHasta", fechaIngreso)
                        .construir();
                JsonObject resultSP = colaboradores.invocar("StoredProcedure", Json.objeto()
                        .poner("procedimiento", "BSCL.CuentaInversion.BD.Servicios::ConsultarSaldoCuentaInversionCliente")
                        .poner("entrada", entradaSaldo).construir());
                JsonElement saldos = resultSP == null ? null : resultSP.get("p_out_SaldoPorFechaMovimiento");
                cuentaInversion = saldos == null || saldos.isJsonNull() ? null : Json.GSON.fromJson(saldos, LISTA_CUENTA_INVERSION);

                repositorio.eliminarTablaCuentaInversionAConsultar();
                tablaLimpia = true;
            } finally {
                // the session-scoped temporary table must not outlive a failed block (the pooled connection is reused)
                if (!tablaLimpia) {
                    try {
                        repositorio.eliminarTablaCuentaInversionAConsultar();
                    } catch (Exception ignorada) {
                        // best effort, the original failure is the one that matters
                    }
                }
            }

            List<OrdenTraspaso> consultaOrdenTraspaso = repositorio.ordenesDelTraspaso(fila.idTraspaso());

            for (CuentaInversion ciRow : cuentaInversion) {
                for (OrdenTraspaso otRow : consultaOrdenTraspaso) {

                    if (otRow.RazonRetiro.equals("RES") && otRow.Regimen.contains("DC")) {
                        continue;
                    }

                    if (otRow.MontoOrden == null && otRow.MontoOrdenFFMM == null) {
                        continue;
                    }
                    if (fila.idTraspaso().compareTo(otRow.IdTraspaso) != 0) {
                        continue;
                    }

                    BigDecimal montoOrden = otRow.MontoOrden == null || otRow.MontoOrden.compareTo(BigDecimal.ZERO) == 0 ? otRow.MontoOrdenFFMM : otRow.MontoOrden;
                    BigDecimal montoRetencion = BigDecimal.ZERO;

                    if (otRow.IdPortafolio.equals(ciRow.IdPortafolio) && ciRow.SaldoDisponible.compareTo(montoOrden) >= 0) {
                        ObjCargoAbonoAGenerar ctaInvAbono = new ObjCargoAbonoAGenerar();
                        ObjCargoAbonoAGenerar ctaInvCargo = new ObjCargoAbonoAGenerar();

                        for (Retencion retRow : configuracionRetencion) {
                            if (otRow.RazonRetiro.equals(retRow.CodigoRetiro) && retRow.PorcentajeRetencion.compareTo(BigDecimal.ZERO) != 0) {
                                // LEGACY DEFECT kept (see README APV-B-164, APV-B-165): the regimen ids are compared with == (Long references, equal only inside the Long cache)
                                if (retRow.IdRegimenTributario == null || retRow.IdRegimenTributario == otRow.IdRegimenTributarioMatriz) {
                                    montoRetencion = montoOrden.multiply(retRow.PorcentajeRetencion);
                                    montoOrden = montoOrden.subtract(montoRetencion);
                                    // LEGACY DEFECT kept (see README APV-B-362): every matching retention is applied in turn to the same order and only the LAST amount is reported
                                }
                            }
                        }

                        // cargo to the client
                        ctaInvCargo.NumeroCargoAbono = contadorCARABO;
                        ctaInvCargo.IdPortafolio = otRow.IdPortafolio;
                        ctaInvCargo.IdMoneda = "CLP";
                        ctaInvCargo.IdTipoMovimiento = "MOVCAR";
                        ctaInvCargo.IdConcepto = "CARAPV";
                        ctaInvCargo.Descripcion = "APV";
                        ctaInvCargo.IdCondicionDeLiquidacion = "T0";
                        ctaInvCargo.FechaCondicionDeLiquidacion = fechaIngreso;
                        ctaInvCargo.Monto = montoOrden;
                        ctaInvCargo.IdEjecutivo = 0L;
                        ctaInvCargo.IdSucursal = 0L;
                        ctaInvCargo.IdTipoProducto = "";
                        ctaInvCargo.IdTipoOperacion = "";
                        ctaInvCargo.IdOperacion = fila.idTraspaso();
                        ctaInvCargo.FechaOperacion = "0001-01-01";
                        ctaInvCargo.FechaLiquidacionOperacion = "0001-01-01";
                        ctaInvCargo.FechaVencimientoOperacion = null;
                        ctaInvCargo.IdMonedaOperacion = "";
                        ctaInvCargo.MontoOperacion = null;
                        ctaInvCargo.TipoCambioOperacion = BigDecimal.valueOf(0L);
                        ctaInvCargo.EsIndicadorBilateral = "N";
                        ctaInvCargo.EsCargoAbonoPorTraspaso = "N";
                        ctaInvCargo.IdPortafolioTraspaso = null;
                        ctaInvCargo.GeneroContraparteEnTesoreria = "N";
                        ctaInvCargo.CertificadoAutomatico = "S";
                        paramsCargoAbono.add(ctaInvCargo);

                        contadorCARABO = contadorCARABO + 1;

                        // abono to the corredora
                        ctaInvAbono.NumeroCargoAbono = contadorCARABO;
                        ctaInvAbono.IdPortafolio = idPortafolioCorredora;
                        ctaInvAbono.IdMoneda = "CLP";
                        ctaInvAbono.IdTipoMovimiento = "MOVABN";
                        ctaInvAbono.IdConcepto = "ABOAPV";
                        ctaInvAbono.Descripcion = "APV";
                        ctaInvAbono.IdCondicionDeLiquidacion = "T0";
                        ctaInvAbono.FechaCondicionDeLiquidacion = fechaIngreso;
                        ctaInvAbono.Monto = montoOrden;
                        ctaInvAbono.IdEjecutivo = 0L;
                        ctaInvAbono.IdSucursal = 0L;
                        ctaInvAbono.IdTipoProducto = "";
                        ctaInvAbono.IdTipoOperacion = "";
                        ctaInvAbono.IdOperacion = fila.idTraspaso();
                        ctaInvAbono.FechaOperacion = "0001-01-01";
                        ctaInvAbono.FechaLiquidacionOperacion = "0001-01-01";
                        ctaInvAbono.FechaVencimientoOperacion = null;
                        ctaInvAbono.IdMonedaOperacion = "";
                        ctaInvAbono.MontoOperacion = null;
                        ctaInvAbono.TipoCambioOperacion = BigDecimal.valueOf(0L);
                        ctaInvAbono.EsIndicadorBilateral = "N";
                        ctaInvAbono.EsCargoAbonoPorTraspaso = "N";
                        ctaInvAbono.IdPortafolioTraspaso = null;
                        ctaInvAbono.GeneroContraparteEnTesoreria = "N";
                        ctaInvAbono.CertificadoAutomatico = "S";
                        paramsCargoAbono.add(ctaInvAbono);

                        contadorCARABO = contadorCARABO + 1;

                        // Tesoreria egress movement
                        TesoreriaIngresoEgresoB.IngresoEgreso_Entrada ingresoEgreso = new TesoreriaIngresoEgresoB.IngresoEgreso_Entrada();
                        ingresoEgreso.NumeroReferencia = contadorIds;
                        ingresoEgreso.AgruparEnComprobante = "N";
                        ingresoEgreso.Bilateral = "N";
                        ingresoEgreso.FechaComprobante = "0001-01-01";
                        ingresoEgreso.FechaLiquidacion = fechaIngreso;
                        ingresoEgreso.FechaLiquidacionComprobante = "0001-01-01";
                        ingresoEgreso.FechaOperacion = fila.fecha() + " 00:00:00";
                        ingresoEgreso.FechaParidad = "0001-01-01";
                        ingresoEgreso.FolioCartera = 0L;
                        ingresoEgreso.GeneraComprobante = "N";
                        ingresoEgreso.Glosa = "";
                        ingresoEgreso.IdBolsa = "";
                        ingresoEgreso.IdCaja = 0L;
                        ingresoEgreso.IdClasificacionTipoInstrumento = "";
                        ingresoEgreso.IdComprobante = 0L;
                        ingresoEgreso.IdConcepto = "TREAPV";
                        ingresoEgreso.IdCondicionLiquidacion = "";
                        ingresoEgreso.IdEmpleadoDigitador = 0L;
                        ingresoEgreso.IdInstrumento = 0L;
                        ingresoEgreso.IdMoneda = "CLP";
                        ingresoEgreso.IdMonedaOperacion = "";
                        ingresoEgreso.IdOperacion = fila.idTraspaso();
                        ingresoEgreso.IdPersona = fila.idPersona();
                        ingresoEgreso.IdPersonaTitularPago = fila.idPersona();
                        ingresoEgreso.IdPortafolio = 0L;
                        ingresoEgreso.IdPortafolioTitularPago = 0L;
                        ingresoEgreso.IdSucursalEmpleadoDigitador = 0L;
                        ingresoEgreso.IdTipoComprobante = "APV";
                        ingresoEgreso.IdTipoInstrumento = "";
                        ingresoEgreso.IdTipoMovimiento = "MOVEGR";
                        ingresoEgreso.IdTipoOperacion = "EGRTES";
                        ingresoEgreso.IdTipoPersona = "PER";
                        ingresoEgreso.IdTipoPersonaTitularPago = "PER";
                        ingresoEgreso.IdTipoProducto = "";
                        ingresoEgreso.IdTipoReajuste = "";
                        ingresoEgreso.Monto = montoOrden;
                        ingresoEgreso.MontoOperacion = montoOrden;
                        ingresoEgreso.MueveCuentaInversion = "S";
                        ingresoEgreso.SecuenciaAgrupacion = "";
                        ingresoEgreso.SecuenciaRelacionada = null;
                        ingresoEgreso.TitularPago = "S";
                        ingresoEgreso.ValorParidad = BigDecimal.valueOf(0);
                        ingresoEgreso.EsTraspaso = "S";
                        paramsIngresoEgreso.add(ingresoEgreso);

                        TesoreriaIngresoEgresoB.FormaDePago_Entrada formaPagoEgreso = new TesoreriaIngresoEgresoB.FormaDePago_Entrada();
                        formaPagoEgreso.NumeroReferencia = contadorIds;
                        formaPagoEgreso.NumeroReferenciaFormaDePago = contadorIds;
                        formaPagoEgreso.FechaDocumentoPagoCorredora = "0001-01-01";
                        formaPagoEgreso.FechaDocumentoPagoSocio = fechaIngreso + " 00:00:00";
                        formaPagoEgreso.IdBancoSocio = fila.idBanco();
                        formaPagoEgreso.IdChequera = 0L;
                        formaPagoEgreso.IdCuentaBancariaCorredora = idCtaBco;
                        formaPagoEgreso.IdCuentaBancariaSocio = 0L;
                        formaPagoEgreso.IdMedioDePago = fila.idMedioPago();
                        formaPagoEgreso.IdModalidadDePago = "MODDEP";
                        formaPagoEgreso.IdMoneda = "CLP";
                        formaPagoEgreso.IdTarjetaBancariaCorredora = 0L;
                        formaPagoEgreso.IdTarjetaBancariaSocio = 0L;
                        formaPagoEgreso.IdTipoCuentaBancariaSocio = "";
                        formaPagoEgreso.IdTipoTarjetaBancariaSocio = "";
                        formaPagoEgreso.Monto = montoOrden;
                        formaPagoEgreso.NombreCuentaBancariaSocio = "";
                        formaPagoEgreso.NombreTipoCuentaBancariaSocio = "";
                        formaPagoEgreso.NombreTipoTarjetaBancariaSocio = "";
                        formaPagoEgreso.NumeroCuentaBancariaSocio = "";
                        formaPagoEgreso.NumeroDocumentoCorredora = 0L;
                        formaPagoEgreso.NumeroDocumentoSocio = 0L;
                        formaPagoEgreso.NumeroSerieSocio = "";
                        formaPagoEgreso.NumeroTarjetaBancariaSocio = "";
                        paramsFormaDePago.add(formaPagoEgreso);

                        TesoreriaComprobanteB.FormaDePago_Entrada formaPagoComprobante = new TesoreriaComprobanteB.FormaDePago_Entrada();
                        formaPagoComprobante.NumeroReferencia = 1L;
                        formaPagoComprobante.IdMedioDePago = fila.idMedioPago();
                        formaPagoComprobante.IdModalidadDePago = "MODDEP";
                        formaPagoComprobante.IdMoneda = "CLP";
                        formaPagoComprobante.Monto = montoOrden;
                        formaPagoComprobante.IdCuentaBancariaCorredora = idCtaBco;
                        formaPagoComprobante.IdTarjetaBancariaCorredora = 0L;
                        formaPagoComprobante.IdChequera = 0L;
                        formaPagoComprobante.NumeroDocumentoCorredora = 0L;
                        formaPagoComprobante.FechaDocumentoPagoCorredora = "0001-01-01";
                        formaPagoComprobante.IdCuentaBancariaSocio = 0L;
                        formaPagoComprobante.IdBancoSocio = 0L;
                        formaPagoComprobante.NombreCuentaBancariaSocio = "";
                        formaPagoComprobante.NumeroCuentaBancariaSocio = "";
                        formaPagoComprobante.IdTipoCuentaBancariaSocio = "";
                        formaPagoComprobante.NombreTipoCuentaBancariaSocio = "";
                        formaPagoComprobante.IdTarjetaBancariaSocio = 0L;
                        formaPagoComprobante.IdTipoTarjetaBancariaSocio = "";
                        formaPagoComprobante.NombreTipoTarjetaBancariaSocio = "";
                        formaPagoComprobante.NumeroTarjetaBancariaSocio = "";
                        formaPagoComprobante.NumeroSerieSocio = "";
                        formaPagoComprobante.NumeroDocumentoSocio = 0L;
                        formaPagoComprobante.FechaDocumentoPagoSocio = fechaIngreso;
                        paramsFormaPagoComprobante.add(formaPagoComprobante);

                        contadorIds = contadorIds + 1;

                        // retention movements (cargo/abono only)
                        if (montoRetencion.compareTo(BigDecimal.ZERO) > 0) {
                            ObjCargoAbonoAGenerar ctaInvAbonoRetencion = clonar(ctaInvAbono);
                            ObjCargoAbonoAGenerar ctaInvCargoRetencion = clonar(ctaInvCargo);

                            ctaInvCargoRetencion.NumeroCargoAbono = contadorCARABO;
                            ctaInvCargoRetencion.IdConcepto = "EGRRET";
                            ctaInvCargoRetencion.Monto = montoRetencion;
                            ctaInvCargoRetencion.IdOperacion = otRow.IdOrdenTraspaso;

                            contadorCARABO = contadorCARABO + 1;

                            ctaInvAbonoRetencion.NumeroCargoAbono = contadorCARABO;
                            ctaInvAbonoRetencion.Monto = montoRetencion;
                            ctaInvAbonoRetencion.IdConcepto = "EGRRET";
                            ctaInvAbonoRetencion.IdOperacion = otRow.IdOrdenTraspaso;

                            contadorCARABO = contadorCARABO + 1;

                            paramsCargoAbono.add(ctaInvAbonoRetencion);
                            paramsCargoAbono.add(ctaInvCargoRetencion);
                        }
                    }
                }
            }

            List<ContratoSAAPV> idContratoSAAPV = repositorio.contratoDelTraspaso(fila.idTraspaso());
            // the legacy takes the first row (an empty list is the IndexOutOfBounds) and builds a CambiarEstadoTraspaso list that it never uses
            idContratoSAAPV.get(0);
        }

        // cargo/abono in Cuenta Inversion
        CrearCargoAbonoEntrada paramsCargoAbonoEntrada = new CrearCargoAbonoEntrada();
        paramsCargoAbonoEntrada.p_IdEmpresa = in.idEmpresa();
        paramsCargoAbonoEntrada.p_IdUsuario = in.idUsuario();
        paramsCargoAbonoEntrada.p_IdModulo = in.idModulo();
        paramsCargoAbonoEntrada.p_simularOperacion = "N";
        paramsCargoAbonoEntrada.p_UsaReglasNegocio = "S";
        paramsCargoAbonoEntrada.p_FechaSistema = fechaIngreso;
        paramsCargoAbonoEntrada.p_CargoAbonoAGenerar = paramsCargoAbono;
        paramsCargoAbonoEntrada.p_EsIngresoManual = "N";

        CrearCargoAbonoSalida outCuentaInversion = Json.GSON.fromJson(
                colaboradores.invocar("CrearCargoAbono", Json.aElemento(paramsCargoAbonoEntrada).getAsJsonObject()), CrearCargoAbonoSalida.class);

        if (outCuentaInversion.P_OUT_ERRORESCONTADOR == 0) {
            for (CuentaInversionB.Msg mensaje : outCuentaInversion.P_OUT_MENSAJES) {
                salida.add(new Mensaje(mensaje.id, mensaje.tipo, mensaje.codigo, mensaje.descripcion));
            }
        }

        // movements in Tesoreria
        TesoreriaIngresoEgresoB.CrearIngresoEgreso_Entrada paramsIngresoEgresoEntrada = new TesoreriaIngresoEgresoB.CrearIngresoEgreso_Entrada();
        paramsIngresoEgresoEntrada.p_IdEmpresa = in.idEmpresa();
        paramsIngresoEgresoEntrada.p_IdUsuario = in.idUsuario();
        paramsIngresoEgresoEntrada.p_IdModulo = in.idModulo();
        paramsIngresoEgresoEntrada.p_SimularOperacion = "N";
        paramsIngresoEgresoEntrada.p_UsaReglasNegocio = "S";
        paramsIngresoEgresoEntrada.p_EsIngresoAutomatico = "N";
        paramsIngresoEgresoEntrada.p_FormaDePago = paramsFormaDePago;
        paramsIngresoEgresoEntrada.p_IngresoEgresoGenerar = paramsIngresoEgreso;

        List<TesoreriaComprobanteB.IngresoEgresoGenerar_Entrada> paramsComprobanteIngresoEgreso = new ArrayList<>();
        TesoreriaIngresoEgresoB.CrearIngresoEgreso_Salida outTesoreria = Json.GSON.fromJson(
                colaboradores.invocar("CrearIngresoEgreso", Json.aElemento(paramsIngresoEgresoEntrada).getAsJsonObject()),
                TesoreriaIngresoEgresoB.CrearIngresoEgreso_Salida.class);

        if (contarErrores(outTesoreria.P_OUT_MENSAJES, m -> m.tipo) > 0) {
            for (TesoreriaIngresoEgresoB.Msg m : outTesoreria.P_OUT_MENSAJES) {
                salida.add(new Mensaje(m.id, m.tipo, m.codigo, m.descripcion));
            }
            return salida;
        } else {
            contadorIds = 1L;
            for (TesoreriaIngresoEgresoB.Msg m : outTesoreria.P_OUT_MENSAJES) {
                salida.add(new Mensaje(m.id, m.tipo, m.codigo, m.descripcion));
            }

            for (TesoreriaIngresoEgresoB.IngresoEgresoGenerado_Salida outIngresoEgreso : outTesoreria.P_OUT_INGRESOEGRESOGENERADO) {
                TesoreriaComprobanteB.IngresoEgresoGenerar_Entrada comprobanteIngresoEgreso = new TesoreriaComprobanteB.IngresoEgresoGenerar_Entrada();
                comprobanteIngresoEgreso.NumeroReferencia = 1L;
                comprobanteIngresoEgreso.IdIngresoEgreso = outIngresoEgreso.IdIngresoEgreso;
                comprobanteIngresoEgreso.TipoComprobante = "PAREGR";

                for (TesoreriaIngresoEgresoB.IngresoEgreso_Entrada filaIngEgr : paramsIngresoEgreso) {
                    if (filaIngEgr.NumeroReferencia.toString().equals(outIngresoEgreso.NumeroReferencia.toString())
                            && filaIngEgr.IdConcepto.equals("EGRRET")) {
                        comprobanteIngresoEgreso.NumeroReferencia = 2L;
                    }
                }
                paramsComprobanteIngresoEgreso.add(comprobanteIngresoEgreso);
                contadorIds = contadorIds + 1;
            }
        }

        // comprobante
        TesoreriaComprobanteB.CrearComprobante_Entrada paramsComprobante = new TesoreriaComprobanteB.CrearComprobante_Entrada();
        List<TesoreriaComprobanteB.ComprobanteGenerar_Entrada> paramsComprobanteGenerar = new ArrayList<>();
        paramsComprobante.p_IdEmpresa = in.idEmpresa();
        paramsComprobante.p_IdUsuario = in.idUsuario();
        paramsComprobante.p_IdModulo = in.idModulo();
        paramsComprobante.p_UsaReglasNegocio = "S";
        paramsComprobante.p_FechaMovimiento = fechaIngreso;
        paramsComprobante.p_IdCaja = 1L;
        paramsComprobante.p_GenerarComprobanteDesdeMovimientos = "S";
        paramsComprobante.p_ComprobanteGenerar = paramsComprobanteGenerar;
        paramsComprobante.p_IngresoEgresoGenerar = paramsComprobanteIngresoEgreso;
        paramsComprobante.p_FormaDePago = paramsFormaPagoComprobante;

        TesoreriaComprobanteB.CrearComprobante_Salida outComprobante = Json.GSON.fromJson(
                colaboradores.invocar("CrearComprobante", Json.aElemento(paramsComprobante).getAsJsonObject()),
                TesoreriaComprobanteB.CrearComprobante_Salida.class);

        boolean hayErrores = contarErrores(outComprobante.P_OUT_MENSAJES, m -> m.tipo) > 0;
        for (TesoreriaComprobanteB.Msg filaMsg : outComprobante.P_OUT_MENSAJES) {
            salida.add(new Mensaje(contadorIds, filaMsg.tipo, filaMsg.codigo, filaMsg.descripcion));
        }
        if (!hayErrores) {
            // the legacy collects the comprobantes to liquidate (LiquidarComprobante list) but never calls the service, and the state change is commented out
            for (int i = 0; i < outComprobante.P_OUT_RESULTADOPROCESO.size(); i++) {
                outComprobante.P_OUT_RESULTADOPROCESO.get(i).Campo.equals("IdComprobante");
            }
        }
        return salida;
    }

    /** Legacy {@code MensajeSalida.getNumeroErrores} over the messages a collaborator answered. */
    private static <T> long contarErrores(List<T> mensajes, java.util.function.Function<T, String> tipo) {
        long c = 0;
        for (T m : mensajes) {
            if ("ERROR".equals(tipo.apply(m))) {
                c++;
            }
        }
        return c;
    }

    /** Legacy {@code ObjCargoAbonoAGenerar.clone()}: shallow copy of every field. */
    private static ObjCargoAbonoAGenerar clonar(ObjCargoAbonoAGenerar origen) {
        ObjCargoAbonoAGenerar copia = new ObjCargoAbonoAGenerar();
        try {
            for (Field f : ObjCargoAbonoAGenerar.class.getFields()) {
                if (!Modifier.isStatic(f.getModifiers())) {
                    f.set(copia, f.get(origen));
                }
            }
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
        return copia;
    }
}
