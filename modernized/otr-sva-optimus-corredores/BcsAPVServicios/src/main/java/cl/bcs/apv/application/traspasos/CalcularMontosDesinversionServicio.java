package cl.bcs.apv.application.traspasos;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonObject;

import cl.bcs.apv.api.casouso.CalcularMontosDesinversion;
import cl.bcs.apv.api.comun.Mensaje;
import cl.bcs.apv.api.dto.calcularmontosdesinversion.CalcularMontosDesinversionEntrada;
import cl.bcs.apv.api.dto.calcularmontosdesinversion.CalcularMontosDesinversionResultado;
import cl.bcs.apv.api.dto.calcularmontosdesinversion.Custodia;
import cl.bcs.apv.application.comun.Json;
import cl.bcs.apv.application.comun.Mensajes;
import cl.bcs.apv.application.comun.Parametros;
import cl.bcs.apv.application.comun.Sneaky;
import cl.bcs.apv.domain.port.out.Colaboradores;
import cl.bcs.apv.domain.port.out.ConfiguracionSistema;
import cl.bcs.apv.domain.port.out.traspasos.CalcularMontosDesinversionRepositorio;
import cl.bcs.apv.domain.port.out.traspasos.CalcularMontosDesinversionRepositorio.DetalleTraspasoDesinversion;
import cl.bcs.apv.domain.port.out.traspasos.CalcularMontosDesinversionRepositorio.ValorCuotaTipoParidad;

/**
 * Use case {@code CalcularMontosDesinversion}: legacy {@code CalculoMontosDesinversion} (amounts and quantities to divest from the custody of a client, by total,
 * percentage or traspaso detail). The Plataforma services ConsultarEmpresa, EntregarPreciosTasas and CalcularParidad go through the {@code Colaboradores} port.
 * The custody objects are mutated and returned, as the legacy did.
 */
@Transactional(rollbackFor = Exception.class)
public class CalcularMontosDesinversionServicio implements CalcularMontosDesinversion {

    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final ConfiguracionSistema configuracion;
    private final Colaboradores colaboradores;
    private final CalcularMontosDesinversionRepositorio repositorio;

    public CalcularMontosDesinversionServicio(ConfiguracionSistema configuracion, Colaboradores colaboradores, CalcularMontosDesinversionRepositorio repositorio) {
        this.configuracion = configuracion;
        this.colaboradores = colaboradores;
        this.repositorio = repositorio;
    }

    /** Legacy {@code CalculoSalida} of CalcularParidad (the fields this class reads). */
    private record ParidadSalida(Long idInterno, Long idMonedaOrigenLegacy, BigDecimal montoOrigen, String idMonedaDestino, BigDecimal montoDestino,
            BigDecimal montoLocal) {
    }

    @Override
    public CalcularMontosDesinversionResultado ejecutar(CalcularMontosDesinversionEntrada in) {
        List<Mensaje> mensajes = new ArrayList<>();
        // LEGACY DEFECT kept (see README APV-B-103): ONE MensajeSalida instance, every message type is CALCULO so the error counter is always 0
        MensajeLegacy msg = new MensajeLegacy();
        List<MensajeLegacy> salidaMensajes = new ArrayList<>();
        BigDecimal porcentajeSalida = null;
        BigDecimal montoTotalSalida = null;
        List<CustodiaDesinversion> custodiaSalida = null;

        BigDecimal cantidadTotal = new BigDecimal(0);
        BigDecimal montoTotalPatrimonio = new BigDecimal(0);
        BigDecimal montoTotal = new BigDecimal(0);
        List<CustodiaDesinversion> listCustodia = new ArrayList<>();
        List<String> listInstrumentos = new ArrayList<>();
        String idMonedaSistema = Parametros.texto(configuracion, in.idEmpresa(), Parametros.PLATAFORMA, "PLA.MonedaSistema");
        JsonObject empresa = colaboradores.invocar("ConsultarEmpresa", Json.objeto().poner("idEmpresa", in.idEmpresa()).construir());
        Long idPaisEmpresa = Json.largo(empresa, "IdPais");
        String fechaSistema = Parametros.fechaSistema(configuracion, in.idEmpresa());

        List<CustodiaDesinversion> custodia = new ArrayList<>();
        for (Custodia c : in.custodia()) {
            custodia.add(CustodiaDesinversion.de(c));
        }

        // PRECIO MERCADO
        {
            List<JsonObject> datosInstrumentos = new ArrayList<>();
            for (CustodiaDesinversion cust : custodia) {
                if (cust.IdClasificacionTipoInstrumento.equalsIgnoreCase("RV") && !cust.IdTipoInstrumento.equalsIgnoreCase("SIM")) {
                    datosInstrumentos.add(Json.objeto().poner("IdRegistro", cust.IdRegistro).poner("TipoCartera", "0").poner("IdUso", "CARTERA")
                            .poner("IdTipoCriterioTasa", "0").poner("IdClasificacionTipoInstrumento", "RV").poner("IdTipoInstrumento", "0")
                            .poner("IdInstrumento", cust.IdInstrumento).poner("Nemotecnico", cust.Nemotecnico).poner("IdMoneda", cust.IdMonedaInstrumento)
                            .poner("FechaConsulta", fechaLegacy(cust.FechaOperacion)).poner("PlazoResidual", 0L).poner("NemoSVS", null).construir());
                }
            }
            if (datosInstrumentos.size() > 0) {
                JsonObject entrada = Json.objeto().poner("p_IdEmpresa", in.idEmpresa()).poner("p_IdUsuario", in.idUsuario()).poner("p_IdModulo", in.idModulo())
                        .poner("p_IdPais", idPaisEmpresa).poner("p_DatosInstrumentos", datosInstrumentos).construir();
                JsonObject salidaPrecios = colaboradores.invocar("EntregarPreciosTasas", entrada);
                Map<Long, JsonObject> precios = listaAHash(Json.lista(salidaPrecios, "P_OUT_PRECIOSTASAS"), "IdRegistro");

                for (CustodiaDesinversion custodiaIn : custodia) {
                    if (!custodiaIn.IdClasificacionTipoInstrumento.equalsIgnoreCase("RE")) {
                        JsonObject registroPrecio = precios.get(custodiaIn.IdRegistro);
                        BigDecimal precio = registroPrecio == null ? null : Json.decimal(registroPrecio, "Precio");
                        if (registroPrecio != null && precio != null && precio.doubleValue() > 0) {
                            custodiaIn.PrecioMercadoCustodia = precio;
                        }
                    }
                }
            }
        }

        if (custodia.size() > 0) {
            try {
                String indicadorRedondeo = Parametros.texto(configuracion, in.idEmpresa(), Parametros.PLATAFORMA, "APV.IndicadorRedondeoDesinversion");

                for (CustodiaDesinversion custodiaIn : custodia) {
                    listInstrumentos.add(custodiaIn.Nemotecnico);
                    if (custodiaIn.MontoMercadoCustodia.compareTo(BigDecimal.ZERO) == 0) {
                        msg.id = 1L;
                        msg.tipo = "CALCULO";
                        msg.codigo = "ERROR";
                        msg.descripcion = "Custodia con Producto " + custodiaIn.NombreProductoAgrupador + " y Portafolio " + custodiaIn.Regimen
                                + " tiene el Monto Mercado Custodia en " + custodiaIn.MontoMercadoCustodia;
                        salidaMensajes.add(msg);
                        return resultado(salidaMensajes, null, null, porcentajeSalida);
                    }
                    // LEGACY DEFECT kept (see README APV-B-103): the sum is discarded (BigDecimal is immutable) but a null CantidadCustodia still throws NullPointerException
                    cantidadTotal.add(custodiaIn.CantidadCustodia);
                    montoTotalPatrimonio = montoTotalPatrimonio.add(custodiaIn.MontoMercadoCustodia);
                }
                BigDecimal monto = BigDecimal.ZERO;
                // LEGACY DEFECT kept (see README APV-B-121): a percentage below 1 has longValue() 0 and is treated as "amount mode"
                if (in.porcentajeADesinvertir().longValue() == 0) {
                    // LEGACY DEFECT kept (see README APV-B-124): the percentage comes from a double division (new BigDecimal(double): the exact binary expansion)
                    porcentajeSalida = new BigDecimal((double) in.totalADesinvertir() / montoTotalPatrimonio.longValue());
                    monto = new BigDecimal(in.totalADesinvertir()).divide(new BigDecimal(custodia.size()), MathContext.DECIMAL128);
                } else {
                    porcentajeSalida = in.porcentajeADesinvertir().divide(new BigDecimal(100));
                }

                if (in.idTraspaso() > 0) {
                    List<DetalleTraspasoDesinversion> detalleTraspaso = repositorio.detalleTraspaso(in.idTraspaso());
                    List<JsonObject> calculosEntrada = new ArrayList<>();
                    if (!detalleTraspaso.isEmpty()) {
                        for (DetalleTraspasoDesinversion detalle : detalleTraspaso) {
                            // LEGACY DEFECT kept (see README APV-B-129): the query does not select UFTraspaso, so this UF branch never runs
                            if (BigDecimal.ZERO.compareTo(detalle.UFTraspaso) == -1) {
                                calculosEntrada.add(Json.objeto().poner("IdTrabajo", 2001L).poner("IdInterno", 1L).poner("Fecha", fechaSistema)
                                        .poner("IdMonedaOrigen", "UF").poner("IdMonedaDestino", detalle.IdMoneda).poner("ValorCambioLocalModificado", 0L)
                                        .poner("MontoOrigen", detalle.UFTraspaso).poner("IdUso", "ORDENES").construir());
                            }
                        }
                        if (calculosEntrada.size() > 0) {
                            JsonObject salidaParidad = colaboradores.invocar("CalcularParidad", Json.objeto().poner("p_IdEmpresa", in.idEmpresa())
                                    .poner("p_IdTrabajo", 9999L).poner("p_CalculosEntrada", calculosEntrada).construir());
                            for (ParidadSalida regParidad : paridades(salidaParidad)) {
                                detalleTraspaso.forEach(traspaso -> {
                                    // LEGACY DEFECT kept (see README APV-B-129): reference comparisons (== on BigDecimal and String)
                                    if (traspaso.UFTraspaso == regParidad.montoOrigen() && traspaso.IdMoneda == regParidad.idMonedaDestino()) {
                                        traspaso.MontoCalculadoUF = regParidad.montoDestino();
                                        traspaso.MontoCalculadoCLP = regParidad.montoLocal();
                                    }
                                });
                            }
                        }
                    }
                    for (CustodiaDesinversion custodiaIn : custodia) {
                        for (DetalleTraspasoDesinversion detalle : detalleTraspaso) {
                            if (custodiaIn.IdInstrumento.equals(detalle.IdInstrumento) && custodiaIn.Regimen.equals(detalle.CodigoRegimenTributario)
                                    && custodiaIn.IdPortafolio.equals(detalle.IdPortafolio)) {
                                List<ValorCuotaTipoParidad> valorCuotaTipoParidad = repositorio.valorCuotaTipoCambio(in.idEmpresa(), custodiaIn.Nemotecnico);

                                if (BigDecimal.ZERO.compareTo(detalle.MontoTraspaso) == -1) {
                                    if (custodiaIn.IdClasificacionTipoInstrumento.equalsIgnoreCase("FM")) {
                                        custodiaIn.MontoADesinvertirCLP = detalle.MontoTraspaso;
                                        convertirMontos(custodiaIn, custodiaIn.MontoADesinvertirCLP, idMonedaSistema, in, fechaSistema, false);
                                        custodiaIn.CantidadADesinvertir = custodiaIn.MontoADesinvertir.divide(custodiaIn.PrecioMercadoCustodia, MathContext.DECIMAL128)
                                                .setScale(valorCuotaTipoParidad.get(0).DecimalesCuota.intValue(), RoundingMode.HALF_DOWN);
                                    } else {
                                        custodiaIn.MontoADesinvertirCLP = detalle.MontoTraspaso;
                                        convertirMontos(custodiaIn, custodiaIn.MontoADesinvertirCLP, idMonedaSistema, in, fechaSistema, false);
                                        custodiaIn.CantidadADesinvertir = custodiaIn.MontoADesinvertir.divide(custodiaIn.PrecioMercadoCustodia, 0, RoundingMode.HALF_UP);
                                    }
                                    break;
                                } else if (BigDecimal.ZERO.compareTo(detalle.UFTraspaso) == -1) {
                                    if (custodiaIn.IdClasificacionTipoInstrumento.equalsIgnoreCase("FM")) {
                                        custodiaIn.MontoADesinvertir = detalle.MontoCalculadoUF;
                                        custodiaIn.MontoADesinvertirCLP = detalle.MontoCalculadoCLP;
                                        custodiaIn.CantidadADesinvertir = custodiaIn.MontoADesinvertir.divide(custodiaIn.PrecioMercadoCustodia, MathContext.DECIMAL128)
                                                .setScale(valorCuotaTipoParidad.get(0).DecimalesCuota.intValue(), RoundingMode.HALF_DOWN);
                                    } else {
                                        custodiaIn.MontoADesinvertir = detalle.MontoCalculadoUF;
                                        custodiaIn.MontoADesinvertirCLP = detalle.MontoCalculadoCLP;
                                        custodiaIn.CantidadADesinvertir = custodiaIn.MontoADesinvertir.divide(custodiaIn.PrecioMercadoCustodia, 0, RoundingMode.HALF_UP);
                                    }
                                    break;
                                } else if (BigDecimal.ZERO.compareTo(detalle.PorcentajeTraspaso) == -1) {
                                    if (custodiaIn.IdClasificacionTipoInstrumento.equalsIgnoreCase("FM")) {
                                        custodiaIn.CantidadADesinvertir = (detalle.PorcentajeTraspaso.multiply(custodiaIn.CantidadCustodia)).movePointLeft(2)
                                                .setScale(valorCuotaTipoParidad.get(0).DecimalesCuota.intValue(), RoundingMode.HALF_DOWN);
                                        custodiaIn.MontoADesinvertir = custodiaIn.CantidadADesinvertir.multiply(custodiaIn.PrecioMercadoCustodia)
                                                .setScale(valorCuotaTipoParidad.get(0).DecimalesCuota.intValue(), RoundingMode.HALF_DOWN);
                                        convertirMontos(custodiaIn, custodiaIn.MontoADesinvertir, idMonedaSistema, in, fechaSistema, true);
                                        custodiaIn.MontoADesinvertirCLP.setScale(0, RoundingMode.HALF_DOWN);
                                    } else {
                                        custodiaIn.CantidadADesinvertir = (detalle.PorcentajeTraspaso.multiply(custodiaIn.CantidadCustodia)).divide(new BigDecimal(100), 0,
                                                RoundingMode.HALF_UP);
                                        custodiaIn.MontoADesinvertir = custodiaIn.CantidadADesinvertir.multiply(custodiaIn.PrecioMercadoCustodia).setScale(0,
                                                RoundingMode.HALF_UP);
                                        convertirMontos(custodiaIn, custodiaIn.MontoADesinvertir, idMonedaSistema, in, fechaSistema, true);
                                        custodiaIn.MontoADesinvertirCLP.setScale(0, RoundingMode.HALF_DOWN);
                                    }
                                    break;
                                }
                            }
                        }
                        listCustodia.add(custodiaIn);
                        montoTotal = montoTotal.add(custodiaIn.MontoADesinvertirCLP);
                    }
                } else {
                    // LEGACY DEFECT kept (see README APV-B-113): total >= patrimony reports P_OUT_MONTO_TOTAL 0 (the amounts are not accumulated)
                    if (in.totalADesinvertir() >= montoTotalPatrimonio.longValue()) {
                        for (CustodiaDesinversion custodiaIn : custodia) {
                            custodiaIn.CantidadADesinvertir = custodiaIn.CantidadCustodia;
                            custodiaIn.MontoADesinvertir = custodiaIn.MontoMercadoCustodia;
                            custodiaIn.MontoADesinvertirCLP = custodiaIn.MontoMercadoCustodia;
                            listCustodia.add(custodiaIn);
                        }
                    } else {
                        if (in.porcentajeADesinvertir().compareTo(BigDecimal.ZERO) == 0) {
                            convertirMontosLista(custodia, monto, idMonedaSistema, in, fechaSistema);
                        }
                        for (CustodiaDesinversion custodiaIn : custodia) {
                            switch (indicadorRedondeo) {
                                // LEGACY DEFECT kept (see README APV-B-114/115/116/126): ROUND_UP, ROUND_DOWN and ROUND_HALF_UP never set MontoADesinvertirCLP: the total then fails with NullPointerException
                                case "ROUND_UP":
                                    if (custodiaIn.IdClasificacionTipoInstrumento.equalsIgnoreCase("RV")) {
                                        custodiaIn.CantidadADesinvertir = porcentajeSalida.multiply(custodiaIn.CantidadCustodia).setScale(0, RoundingMode.UP);
                                        custodiaIn.MontoADesinvertir = custodiaIn.CantidadADesinvertir.multiply(custodiaIn.PrecioMercadoCustodia).setScale(0, RoundingMode.UP);
                                    } else {
                                        custodiaIn.CantidadADesinvertir = porcentajeSalida.multiply(custodiaIn.CantidadCustodia);
                                        custodiaIn.MontoADesinvertir = custodiaIn.CantidadADesinvertir.multiply(custodiaIn.PrecioMercadoCustodia);
                                    }
                                    break;
                                case "ROUND_DOWN":
                                    if (custodiaIn.IdClasificacionTipoInstrumento.equalsIgnoreCase("RV")) {
                                        custodiaIn.CantidadADesinvertir = porcentajeSalida.multiply(custodiaIn.CantidadCustodia).setScale(0, RoundingMode.DOWN);
                                        custodiaIn.MontoADesinvertir = custodiaIn.CantidadADesinvertir.multiply(custodiaIn.PrecioMercadoCustodia).setScale(0, RoundingMode.DOWN);
                                    } else {
                                        custodiaIn.CantidadADesinvertir = porcentajeSalida.multiply(custodiaIn.CantidadCustodia);
                                        custodiaIn.MontoADesinvertir = custodiaIn.CantidadADesinvertir.multiply(custodiaIn.PrecioMercadoCustodia);
                                    }
                                    break;
                                case "ROUND_HALF_UP":
                                    if (custodiaIn.IdClasificacionTipoInstrumento.equalsIgnoreCase("RV")) {
                                        custodiaIn.CantidadADesinvertir = porcentajeSalida.multiply(custodiaIn.CantidadCustodia).setScale(0, RoundingMode.HALF_UP);
                                        custodiaIn.MontoADesinvertir = custodiaIn.CantidadADesinvertir.multiply(custodiaIn.PrecioMercadoCustodia).setScale(0, RoundingMode.HALF_UP);
                                    } else {
                                        custodiaIn.CantidadADesinvertir = porcentajeSalida.multiply(custodiaIn.CantidadCustodia);
                                        custodiaIn.MontoADesinvertir = custodiaIn.CantidadADesinvertir.multiply(custodiaIn.PrecioMercadoCustodia);
                                    }
                                    break;
                                case "HALF_DOWN":
                                    List<ValorCuotaTipoParidad> valorCuotaTipoParidad = repositorio.valorCuotaTipoCambio(in.idEmpresa(), custodiaIn.Nemotecnico);
                                    if (custodiaIn.IdClasificacionTipoInstrumento.equalsIgnoreCase("FM")) {
                                        custodiaIn.CantidadADesinvertir = (porcentajeSalida.multiply(custodiaIn.CantidadCustodia))
                                                .setScale(valorCuotaTipoParidad.get(0).DecimalesCuota.intValue(), RoundingMode.HALF_DOWN);
                                        custodiaIn.MontoADesinvertir = custodiaIn.CantidadADesinvertir.multiply(custodiaIn.PrecioMercadoCustodia)
                                                .setScale(valorCuotaTipoParidad.get(0).DecimalesCuota.intValue(), RoundingMode.HALF_DOWN);
                                        convertirMontos(custodiaIn, custodiaIn.MontoADesinvertir, idMonedaSistema, in, fechaSistema, true);
                                        custodiaIn.MontoADesinvertirCLP = custodiaIn.MontoADesinvertirCLP.setScale(0, RoundingMode.HALF_DOWN);
                                    } else {
                                        custodiaIn.CantidadADesinvertir = porcentajeSalida.multiply(custodiaIn.CantidadCustodia).setScale(0, RoundingMode.HALF_DOWN);
                                        custodiaIn.MontoADesinvertir = custodiaIn.CantidadADesinvertir.multiply(custodiaIn.PrecioMercadoCustodia).setScale(0, RoundingMode.HALF_DOWN);
                                        convertirMontos(custodiaIn, custodiaIn.MontoADesinvertir, idMonedaSistema, in, fechaSistema, true);
                                        custodiaIn.MontoADesinvertirCLP = custodiaIn.MontoADesinvertirCLP.setScale(0, RoundingMode.HALF_DOWN);
                                    }
                                    break;
                                default:
                                    // LEGACY DEFECT kept (see README APV-B-107): an unknown indicator does nothing and the total then fails with NullPointerException
                                    break;
                            }

                            listCustodia.add(custodiaIn);
                            montoTotal = montoTotal.add(custodiaIn.MontoADesinvertirCLP);
                        }
                    }
                }

                montoTotalSalida = montoTotal;
                custodiaSalida = listCustodia;
                porcentajeSalida = porcentajeSalida.multiply(new BigDecimal(100));

                msg.id = 1L;
                msg.tipo = "CALCULO";
                msg.codigo = "OK";
                msg.descripcion = "Calculo Realizado";
                salidaMensajes.add(msg);
            } catch (Exception er) {
                msg.id = 1L;
                msg.tipo = "CALCULO";
                msg.codigo = "ERROR";
                msg.descripcion = er.toString();
                salidaMensajes.add(msg);
            }
        } else {
            msg.id = 1L;
            msg.tipo = "CALCULO";
            msg.codigo = "ERROR";
            msg.descripcion = "No hay Custodias para calcular";
            salidaMensajes.add(msg);
        }
        return resultado(salidaMensajes, montoTotalSalida, custodiaSalida, porcentajeSalida);
    }

    private static CalcularMontosDesinversionResultado resultado(List<MensajeLegacy> mensajes, BigDecimal montoTotal, List<CustodiaDesinversion> custodia,
            BigDecimal porcentaje) {
        List<Mensaje> salida = MensajeLegacy.aMensajes(mensajes);
        List<Custodia> custodiaSalida = null;
        if (custodia != null) {
            custodiaSalida = new ArrayList<>();
            for (CustodiaDesinversion c : custodia) {
                custodiaSalida.add(c.aRecord());
            }
        }
        // LEGACY DEFECT kept (see README APV-B-101): every message has type CALCULO, so P_OUT_ERRORESCONTADOR (counted after logica) is always 0
        return new CalcularMontosDesinversionResultado(salida, Mensajes.numeroErrores(salida), montoTotal, custodiaSalida, porcentaje);
    }

    /** Legacy {@code convertirMontos(List...)}: converts the amount from the system currency to the currency of each instrument. */
    private void convertirMontosLista(List<CustodiaDesinversion> custodia, BigDecimal monto, String idMonedaSistema, CalcularMontosDesinversionEntrada in, String fecha) {
        List<JsonObject> entrada = new ArrayList<>();
        for (CustodiaDesinversion detalle : custodia) {
            if (!idMonedaSistema.equalsIgnoreCase(detalle.IdMonedaInstrumento)) {
                entrada.add(calculoEntrada(fecha, detalle.IdRegistro, idMonedaSistema, detalle.IdMonedaInstrumento, monto));
            }
        }
        List<ParidadSalida> salida = obtenerCalculoParidad(in.idEmpresa(), entrada);
        Map<Long, ParidadSalida> hash = listaAHash(salida, ParidadSalida::idInterno);
        for (CustodiaDesinversion detalle : custodia) {
            ParidadSalida calculo = hash.get(detalle.IdRegistro);
            detalle.MontoADesinvertir = calculo != null ? calculo.montoDestino() : BigDecimal.ZERO;
        }
    }

    /** Legacy {@code convertirMontos(Custodia...)}. */
    private void convertirMontos(CustodiaDesinversion custodia, BigDecimal monto, String idMonedaSistema, CalcularMontosDesinversionEntrada in, String fecha,
            boolean transformarCLP) {
        List<JsonObject> entrada = new ArrayList<>();
        entrada.add(calculoEntrada(fecha, custodia.IdRegistro, transformarCLP ? custodia.IdMonedaInstrumento : idMonedaSistema,
                transformarCLP ? idMonedaSistema : custodia.IdMonedaInstrumento, monto));
        List<ParidadSalida> salida = obtenerCalculoParidad(in.idEmpresa(), entrada);
        Map<Long, ParidadSalida> hash = listaAHash(salida, ParidadSalida::idInterno);

        ParidadSalida calculo = hash.get(custodia.IdRegistro);
        BigDecimal montoDestino = calculo != null ? (transformarCLP ? calculo.montoDestino().setScale(0, RoundingMode.HALF_DOWN) : calculo.montoDestino())
                : BigDecimal.ZERO;
        if (transformarCLP) {
            custodia.MontoADesinvertirCLP = montoDestino;
        } else {
            custodia.MontoADesinvertir = montoDestino;
        }
    }

    private static JsonObject calculoEntrada(String fecha, Long idInterno, String monedaOrigen, String monedaDestino, BigDecimal monto) {
        return Json.objeto().poner("IdTrabajo", 0L).poner("IdInterno", idInterno).poner("Fecha", fecha).poner("IdMonedaOrigen", monedaOrigen)
                .poner("IdMonedaDestino", monedaDestino).poner("ValorCambioLocalModificado", 0L).poner("MontoOrigen", monto).poner("IdUso", "").construir();
    }

    /** Legacy {@code obtenerCalculoParidad}: the answer is ignored unless it has no errors and some rows. */
    private List<ParidadSalida> obtenerCalculoParidad(Long idEmpresa, List<JsonObject> paridades) {
        List<ParidadSalida> convertidas = new ArrayList<>();
        JsonObject entrada = Json.objeto().poner("p_IdEmpresa", idEmpresa).poner("p_IdTrabajo", 0L).poner("p_CalculosEntrada", paridades).construir();
        JsonObject salida = colaboradores.invocar("CalcularParidad", entrada);
        // LEGACY DEFECT kept (see README APV-B-125): an answer whose error counter is not 0 (or absent: NullPointerException) is ignored
        if (Json.largo(salida, "P_OUT_ERRORESCONTADOR").equals(0L) && !Json.lista(salida, "p_out_CalculosSalida").isEmpty()) {
            convertidas = paridades(salida);
        }
        return convertidas;
    }

    private static List<ParidadSalida> paridades(JsonObject salida) {
        List<ParidadSalida> lista = new ArrayList<>();
        for (JsonObject c : Json.lista(salida, "p_out_CalculosSalida")) {
            lista.add(new ParidadSalida(Json.largo(c, "IdInterno"), null, Json.decimal(c, "MontoOrigen"), Json.texto(c, "IdMonedaDestino"),
                    Json.decimal(c, "MontoDestino"), Json.decimal(c, "MontoLocal")));
        }
        return lista;
    }

    /** Legacy {@code Utils.List2Hash} over the answer of a collaborator. */
    private static Map<Long, JsonObject> listaAHash(List<JsonObject> lista, String campoClave) {
        return listaAHash(lista, f -> Json.largo(f, campoClave));
    }

    /** Legacy {@code Utils.List2Hash}: a null list and a repeated key are plain {@code Exception}s. */
    private static <T> Map<Long, T> listaAHash(List<T> lista, java.util.function.Function<T, Long> clave) {
        Map<Long, T> hash = new HashMap<>();
        if (lista == null) {
            throw Sneaky.lanzar(new Exception("List must be not null"));
        }
        for (T obj : lista) {
            Long kf = clave.apply(obj);
            if (hash.containsKey(kf)) {
                throw Sneaky.lanzar(new Exception("key field is not unique"));
            }
            hash.put(kf, obj);
        }
        return hash;
    }

    /** Legacy {@code Fecha.toDate} (19 characters: date and time, else date) written as the legacy Gson wrote a date: {@code yyyy-MM-dd HH:mm:ss.SSS}. */
    private static String fechaLegacy(String fecha) {
        LocalDate d = fecha.length() == 19 ? LocalDate.parse(fecha, FORMATO_FECHA_HORA) : LocalDate.parse(fecha, FORMATO_FECHA);
        return FORMATO_FECHA.format(d) + " 00:00:00.000";
    }
}
