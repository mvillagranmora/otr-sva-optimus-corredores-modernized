package cl.bcs.apv.application.traspasos;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonObject;

import cl.bcs.apv.api.casouso.TraspasarEntreCuentasBancarias;
import cl.bcs.apv.api.comun.Mensaje;
import cl.bcs.apv.api.dto.traspasarentrecuentasbancarias.OrigenRecaudacion;
import cl.bcs.apv.api.dto.traspasarentrecuentasbancarias.TraspasarEntreCuentasBancariasEntrada;
import cl.bcs.apv.api.dto.traspasarentrecuentasbancarias.TraspasarEntreCuentasBancariasResultado;
import cl.bcs.apv.application.comun.Json;
import cl.bcs.apv.application.comun.Mensajes;
import cl.bcs.apv.application.comun.Parametros;
import cl.bcs.apv.domain.port.out.Colaboradores;
import cl.bcs.apv.domain.port.out.ConfiguracionSistema;
import cl.bcs.apv.domain.port.out.traspasos.TraspasoCuentasBancariasRepositorio;
import cl.bcs.apv.domain.port.out.traspasos.TraspasoCuentasBancariasRepositorio.OrigenDestinoTransicion;
import cl.bcs.apv.domain.port.out.traspasos.TraspasoCuentasBancariasRepositorio.RecaudacionPersona;

/**
 * Use case {@code TraspasarEntreCuentasBancarias}: legacy {@code TraspasoCuentasBancarias} (moves money between the corredora's bank accounts through the
 * Tesoreria services CrearIngresoEgreso, CrearComprobante and LiquidarComprobante). Called in process by family C (distribution, rezagos, bonification
 * refunds). Row objects are mutable on purpose: the legacy logic overwrites amounts and copies fields between rows.
 */
@Transactional(rollbackFor = Exception.class)
public class TraspasarEntreCuentasBancariasServicio implements TraspasarEntreCuentasBancarias {

    private final ConfiguracionSistema configuracion;
    private final TraspasoCuentasBancariasRepositorio repositorio;
    private final Colaboradores colaboradores;

    public TraspasarEntreCuentasBancariasServicio(ConfiguracionSistema configuracion, TraspasoCuentasBancariasRepositorio repositorio,
            Colaboradores colaboradores) {
        this.configuracion = configuracion;
        this.repositorio = repositorio;
        this.colaboradores = colaboradores;
    }

    @Override
    public TraspasarEntreCuentasBancariasResultado ejecutar(TraspasarEntreCuentasBancariasEntrada in) {
        List<Mensaje> mensajes = new ArrayList<>();
        String fechaSistema = Parametros.texto(configuracion, in.idEmpresa(), Parametros.PLATAFORMA, "PLA.FechaSistema");
        String abreRegistrosTesoreria = Parametros.texto(configuracion, in.idEmpresa(), Parametros.PLATAFORMA, "APV.AbreRegistrosTesoreria");

        String conceptoIngreso;
        String conceptoEgreso;
        if (in.opcion().equals("REZ")) {
            conceptoIngreso = "INGREZ";
        } else if (in.opcion().equals("DEV")) {
            conceptoIngreso = "INGDEV";
        } else if (in.opcion().equals("DBF")) {
            conceptoIngreso = "INGBON";
        } else if (in.opcion().equals("DIS") && in.esTraspaso()) {
            conceptoIngreso = "ITRAPV";
        } else {
            conceptoIngreso = "INGAPV";
        }
        if (in.opcion().equals("REZ")) {
            conceptoEgreso = "EGRREZ";
        } else if (in.opcion().equals("DEV") && "APV".equalsIgnoreCase(in.idModulo())) {
            conceptoEgreso = "DEVRZG";
        } else if (in.opcion().equals("DEV")) {
            conceptoEgreso = "EGRDEV";
        } else if (in.opcion().equals("DBF")) {
            conceptoEgreso = "EGRBON";
        } else if (in.opcion().equals("DIS") && in.esTraspaso()) {
            conceptoEgreso = "ETRAPV";
        } else {
            conceptoEgreso = "EGRAPV";
        }

        String origenOperacion = "";
        Long idPersonaCliente = 0L;
        Long idCuentaBancoTraspaso = 0L;

        List<OrigenDestinoTransicion> transicion = repositorio.transicion(in.idEmpresa(), in.opcion());

        if (in.opcion().equals("REZ")) {
            if ("N".equalsIgnoreCase(abreRegistrosTesoreria)) {
                for (OrigenDestinoTransicion fila : transicion) {
                    fila.Monto = in.monto();
                }
            }
        } else {
            for (OrigenDestinoTransicion fila : transicion) {
                fila.Monto = in.monto();
            }
        }
        List<JsonObject> ingresosEgresos = new ArrayList<>();
        List<JsonObject> formasDePago = new ArrayList<>();
        List<JsonObject> comprobanteFormasDePago = new ArrayList<>();

        if (in.origenRecaudacion().size() > 0) {
            List<Long> idsRecaudacion = new ArrayList<>();
            for (OrigenRecaudacion o : in.origenRecaudacion()) {
                idsRecaudacion.add(o.idRecaudacionCliente());
            }
            List<RecaudacionPersona> personaRecaudacion = repositorio.personaClienteRecaudacion(idsRecaudacion);
            // LEGACY DEFECT kept (see README APV-B-159): no row for the recaudacion ids is an IndexOutOfBoundsException
            idPersonaCliente = personaRecaudacion.get(0).IdPersonaCliente;

            List<OrigenDestinoTransicion> transicionRec = repositorio.origenRecaudacion(in.idEmpresa(), in.opcion(), idsRecaudacion);

            if (in.opcion().equals("REZ") && "S".equalsIgnoreCase(abreRegistrosTesoreria)) {
                List<OrigenDestinoTransicion> transicionAux = new ArrayList<>();
                for (OrigenDestinoTransicion recas : transicionRec) {
                    OrigenDestinoTransicion od = new OrigenDestinoTransicion();
                    od.IdPersonaCliente = recas.IdPersonaCliente;
                    od.Monto = recas.Monto;
                    transicionAux.add(od);
                }
                llenadoTransicionArmada(transicion, transicionAux);
                transicion = transicionAux.isEmpty() ? transicion : transicionAux;
            }

            for (OrigenDestinoTransicion filaTrans : transicion) {
                if (filaTrans.OrigenDestino.equals("DESTINO")) {
                    for (OrigenDestinoTransicion filaRec : transicionRec) {
                        origenOperacion = filaRec.Origen;
                        idPersonaCliente = filaRec.IdPersonaCliente;
                        idCuentaBancoTraspaso = filaRec.IdCuentaBanco;
                    }
                    transicionRec.add(filaTrans);
                }
            }
            transicion = transicionRec;
        }

        long contador = 1L;
        for (OrigenDestinoTransicion fila : transicion) {
            boolean origen = fila.OrigenDestino.equals("ORIGEN");
            String idTipoOperacion = origen ? "EGRTES" : "INGTES";
            Long idPersona;
            if ("INGTES".equalsIgnoreCase(idTipoOperacion)) {
                idPersona = (in.opcion().equals("REZ") && ("S".equalsIgnoreCase(abreRegistrosTesoreria))) ? fila.IdPersonaCliente : idPersonaCliente;
            } else {
                idPersona = fila.IdPersona;
            }
            ingresosEgresos.add(ingresoEgreso(contador, origen ? "MOVEGR" : "MOVING", idPersona, fila, origen ? conceptoEgreso : conceptoIngreso,
                    idTipoOperacion));

            Long cuentaCorredora;
            if ("TRASPA".equalsIgnoreCase(origenOperacion) && "INGTES".equalsIgnoreCase(idTipoOperacion)) {
                cuentaCorredora = idCuentaBancoTraspaso;
            } else {
                cuentaCorredora = fila.IdCuentaBanco;
            }
            formasDePago.add(formaDePagoTesoreria(contador, fila, cuentaCorredora));
            comprobanteFormasDePago.add(formaDePagoComprobante(contador, fila, in.fecha().toString()));
            contador = contador + 1;
        }

        if (transicion.isEmpty()) {
            // the legacy returns the Salida as built by its constructor: the two comprobante lists stay null (omitted by the legacy Gson)
            return new TraspasarEntreCuentasBancariasResultado(mensajes, 0L, new ArrayList<>(), new ArrayList<>(), null, null);
        }

        // CREACION INGRESO EGRESO TESORERIA
        JsonObject entradaIngresoEgreso = Json.objeto().poner("p_IdEmpresa", in.idEmpresa()).poner("p_IdUsuario", in.idUsuario())
                .poner("p_IdModulo", in.idModulo()).poner("p_SimularOperacion", "N").poner("p_UsaReglasNegocio", "S").poner("p_IdOrigenProceso", null)
                .poner("p_EsIngresoAutomatico", "N").poner("p_EsIngresoUI", "N").poner("p_FechaMovimiento", "0001-01-01")
                .poner("p_IngresoEgresoGenerar", ingresosEgresos).poner("p_FormaDePago", formasDePago).construir();
        JsonObject outTesoreria = colaboradores.invocar("CrearIngresoEgreso", entradaIngresoEgreso);
        mensajes.addAll(Json.mensajes(outTesoreria, "P_OUT_MENSAJES"));
        List<JsonObject> comprobanteIngresoEgreso = new ArrayList<>();
        for (JsonObject generado : Json.lista(outTesoreria, "P_OUT_INGRESOEGRESOGENERADO")) {
            comprobanteIngresoEgreso.add(Json.objeto().poner("NumeroReferencia", Json.largo(generado, "NumeroReferencia"))
                    .poner("IdIngresoEgreso", Json.largo(generado, "IdIngresoEgreso")).poner("TipoComprobante", "").construir());
        }

        // COMPROBANTE
        JsonObject entradaComprobante = Json.objeto().poner("p_IdEmpresa", in.idEmpresa()).poner("p_IdUsuario", in.idUsuario())
                .poner("p_IdModulo", in.idModulo()).poner("p_SimularOperacion", "N").poner("p_UsaReglasNegocio", "S")
                .poner("p_FechaMovimiento", in.fecha().toString()).poner("p_IdCaja", 1L).poner("p_GenerarComprobanteDesdeMovimientos", "S")
                .poner("p_GenerarComprobanteDesdeComprobantes", "N").poner("p_EsIngresoAutomatico", "N")
                .poner("p_IngresoEgresoGenerar", comprobanteIngresoEgreso).poner("p_ComprobanteGenerar", new ArrayList<JsonObject>())
                .poner("p_FormaDePago", comprobanteFormasDePago).construir();
        JsonObject outComprobante = colaboradores.invocar("CrearComprobante", entradaComprobante);
        List<Mensaje> mensajesComprobante = Json.mensajes(outComprobante, "P_OUT_MENSAJES");
        mensajes.addAll(mensajesComprobante);
        List<JsonObject> comprobantesALiquidar = new ArrayList<>();

        if (Mensajes.numeroErrores(mensajesComprobante) > 0) {
            // nothing is liquidated when the comprobante failed
        } else {
            List<JsonObject> resultado = Json.lista(outComprobante, "P_OUT_RESULTADOPROCESO");
            for (int i = 0; i < resultado.size(); i++) {
                if (Json.texto(resultado.get(i), "Campo").equals("IdComprobante")) {
                    // LEGACY DEFECT kept (see README APV-B-155): the index of the comprobante result is used on the movement list (IndexOutOfBounds when there are more results)
                    String tipoMovimiento = Json.texto(ingresosEgresos.get(i), "IdTipoMovimiento");
                    comprobantesALiquidar.add(Json.objeto().poner("IdComprobante", Json.largo(resultado.get(i), "Id"))
                            .poner("IdTipoMovimiento", tipoMovimiento).poner("AfectaCuentaInversion", "CLI").construir());
                }
            }
        }

        String valorParametroLiquidacion = Parametros.texto(configuracion, in.idEmpresa(), Parametros.APV, "REC.LiquidacionAutomatica");
        if (valorParametroLiquidacion.equals("S") && !("DEVRZG".equals(conceptoEgreso))) {
            JsonObject entradaLiquidar = Json.objeto().poner("p_IdEmpresa", in.idEmpresa()).poner("p_IdUsuario", in.idUsuario())
                    .poner("p_IdModulo", in.idModulo()).poner("p_SimularOperacion", "N").poner("p_UsaReglasNegocio", "S")
                    .poner("p_FechaMovimiento", fechaSistema).poner("FolioCSC", "").poner("p_ComprobanteLiquidar", comprobantesALiquidar)
                    .poner("p_FormaDePagoLiquidar", new ArrayList<JsonObject>()).construir();
            JsonObject outLiquidar = colaboradores.invocar("LiquidarComprobante", entradaLiquidar);
            mensajes.addAll(Json.mensajes(outLiquidar, "P_OUT_MENSAJES"));
        }

        return new TraspasarEntreCuentasBancariasResultado(mensajes, Mensajes.numeroErrores(mensajes), sinNulos(ingresosEgresos), sinNulos(formasDePago),
                sinNulos(comprobanteIngresoEgreso), sinNulos(comprobanteFormasDePago));
    }

    /** Legacy {@code llenadoTransicionArmada}: every created row ends with the fields of the LAST transition row. */
    private static void llenadoTransicionArmada(List<OrigenDestinoTransicion> transicion, List<OrigenDestinoTransicion> transicionAux) {
        for (OrigenDestinoTransicion creados : transicionAux) {
            for (OrigenDestinoTransicion filaTrans : transicion) {
                creados.IdBanco = filaTrans.IdBanco;
                creados.IdBancoSocio = filaTrans.IdBancoSocio;
                creados.IdCuentaBanco = filaTrans.IdCuentaBanco;
                creados.IdPersona = filaTrans.IdPersona;
                creados.IdPortafolioDefecto = filaTrans.IdPortafolioDefecto;
                creados.IdSocioDeNegocio = filaTrans.IdSocioDeNegocio;
                creados.IdTipoCuentaBancaria = filaTrans.IdTipoCuentaBancaria;
                // LEGACY DEFECT kept (see README APV-B-160): the socio account type is filled from the corredora account type
                creados.IdTipoCuentaBancariaSocio = filaTrans.IdTipoCuentaBancaria;
                creados.MueveCuentaCorriente = filaTrans.MueveCuentaCorriente;
                creados.MueveCuentaInversion = filaTrans.MueveCuentaInversion;
                creados.NombreTipoCuentaBancaria = filaTrans.NombreTipoCuentaBancaria;
                creados.NumeroCuentaBancariaSocio = filaTrans.NumeroCuentaBancariaSocio;
                creados.Origen = filaTrans.Origen;
                creados.OrigenDestino = filaTrans.OrigenDestino;
                creados.SocioNegocioCtaBco = filaTrans.SocioNegocioCtaBco;
                creados.SocioNegocioNumCta = filaTrans.SocioNegocioNumCta;
            }
        }
    }

    /** Legacy {@code CrearIngresoEgreso.IngresoEgreso_Entrada} with its constructor defaults. */
    private static JsonObject ingresoEgreso(long numeroReferencia, String idTipoMovimiento, Long idPersona, OrigenDestinoTransicion fila, String idConcepto,
            String idTipoOperacion) {
        return Json.objeto().poner("NumeroReferencia", numeroReferencia).poner("IdTipoMovimiento", idTipoMovimiento).poner("IdTipoPersona", "CLI")
                .poner("IdPersona", idPersona).poner("IdPortafolio", fila.IdPortafolioDefecto).poner("IdMoneda", "CLP").poner("Monto", fila.Monto)
                .poner("FechaLiquidacion", null).poner("IdConcepto", idConcepto).poner("IdTipoPersonaTitularPago", "CLI")
                .poner("IdPersonaTitularPago", fila.IdPersona).poner("IdPortafolioTitularPago", fila.IdPortafolioDefecto).poner("IdCaja", 0L)
                .poner("SecuenciaAgrupacion", "").poner("SecuenciaRelacionada", null).poner("Glosa", "").poner("RequiereAutorizacion", "N")
                .poner("IdEmpleadoDigitador", 0L).poner("IdSucursalEmpleadoDigitador", 0L).poner("IdTipoProducto", "")
                .poner("IdTipoOperacion", idTipoOperacion).poner("IdTipoComprobante", "").poner("IdComprobante", 0L)
                .poner("FechaComprobante", "0001-01-01").poner("FechaLiquidacionComprobante", "0001-01-01").poner("IdOperacion", 0L)
                .poner("FechaOperacion", null).poner("IdCondicionLiquidacion", "").poner("IdBolsa", "").poner("IdMonedaOperacion", "")
                .poner("MontoOperacion", fila.Monto).poner("IdTipoReajuste", "").poner("FechaParidad", "0001-01-01")
                .poner("ValorParidad", BigDecimal.valueOf(0)).poner("FolioCartera", 0L).poner("IdInstrumento", 0L).poner("IdTipoInstrumento", "")
                .poner("IdClasificacionTipoInstrumento", "").poner("Bilateral", "N").poner("GeneraComprobante", "N").poner("LiquidarComprobante", "N")
                .poner("AgruparEnComprobante", "N").poner("MueveCuentaInversion", fila.MueveCuentaInversion).poner("TitularPago", "S")
                .poner("EsTraspaso", "N").poner("IdTipoCanalIngreso", "OPT").poner("CodigoEjecutivoDigitador", "").poner("FolioCSC", "").construir();
    }

    /** Legacy {@code CrearIngresoEgreso.FormaDePago_Entrada}. */
    private static JsonObject formaDePagoTesoreria(long numeroReferencia, OrigenDestinoTransicion fila, Long cuentaCorredora) {
        return Json.objeto().poner("NumeroReferencia", numeroReferencia).poner("NumeroReferenciaFormaDePago", numeroReferencia).poner("IdMedioDePago", "TRA")
                .poner("IdModalidadDePago", "MODDEP").poner("IdMoneda", "CLP").poner("Monto", fila.Monto).poner("IdCuentaBancariaCorredora", cuentaCorredora)
                .poner("IdTarjetaBancariaCorredora", 0L).poner("IdChequera", 0L).poner("NumeroDocumentoCorredora", 0L)
                .poner("FechaDocumentoPagoCorredora", "0001-01-01").poner("IdCuentaBancariaSocio", fila.SocioNegocioCtaBco)
                .poner("IdBancoSocio", fila.IdBancoSocio).poner("NombreCuentaBancariaSocio", fila.NombreTipoCuentaBancaria)
                .poner("NumeroCuentaBancariaSocio", fila.NumeroCuentaBancariaSocio).poner("IdTipoCuentaBancariaSocio", fila.IdTipoCuentaBancariaSocio)
                .poner("NombreTipoCuentaBancariaSocio", "").poner("IdTarjetaBancariaSocio", 0L).poner("IdTipoTarjetaBancariaSocio", "")
                .poner("NombreTipoTarjetaBancariaSocio", "").poner("NumeroTarjetaBancariaSocio", "").poner("NumeroSerieSocio", "")
                .poner("NumeroDocumentoSocio", 0L).poner("FechaDocumentoPagoSocio", null).construir();
    }

    /** Legacy {@code CrearComprobante.FormaDePago_Entrada}. */
    private static JsonObject formaDePagoComprobante(long numeroReferencia, OrigenDestinoTransicion fila, String fecha) {
        return Json.objeto().poner("NumeroReferencia", numeroReferencia).poner("IdMedioDePago", "TRA").poner("IdModalidadDePago", "MODDEP")
                .poner("IdMoneda", "CLP").poner("Monto", fila.Monto).poner("IdCuentaBancariaCorredora", 0L).poner("IdTarjetaBancariaCorredora", 0L)
                .poner("IdChequera", 0L).poner("NumeroDocumentoCorredora", 0L).poner("FechaDocumentoPagoCorredora", "0001-01-01")
                .poner("IdCuentaBancariaSocio", 0L).poner("IdBancoSocio", 0L).poner("NombreCuentaBancariaSocio", "").poner("NumeroCuentaBancariaSocio", "")
                .poner("IdTipoCuentaBancariaSocio", fila.IdTipoCuentaBancariaSocio).poner("NombreTipoCuentaBancariaSocio", "")
                .poner("IdTarjetaBancariaSocio", 0L).poner("IdTipoTarjetaBancariaSocio", "").poner("NombreTipoTarjetaBancariaSocio", "")
                .poner("NumeroTarjetaBancariaSocio", "").poner("NumeroSerieSocio", "").poner("NumeroDocumentoSocio", 0L)
                .poner("FechaDocumentoPagoSocio", fecha).construir();
    }

    /** The legacy Gson of the Salida omits null members; the payloads handed to the collaborators keep them (serializeNulls). */
    private static List<JsonObject> sinNulos(List<JsonObject> filas) {
        List<JsonObject> salida = new ArrayList<>();
        for (JsonObject f : filas) {
            JsonObject copia = new JsonObject();
            f.entrySet().forEach(e -> {
                if (!e.getValue().isJsonNull()) {
                    copia.add(e.getKey(), e.getValue());
                }
            });
            salida.add(copia);
        }
        return salida;
    }
}
