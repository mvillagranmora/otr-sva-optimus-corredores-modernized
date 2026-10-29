package cl.bcs.apv.application.recaudacion;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonObject;

import cl.bcs.apv.api.casouso.CrearRecaudacionDeposito;
import cl.bcs.apv.api.dto.crearrecaudaciondeposito.CrearRecaudacionDepositoEntrada;
import cl.bcs.apv.api.dto.crearrecaudaciondeposito.CrearRecaudacionDepositoResultado;
import cl.bcs.apv.api.dto.crearrecaudaciondeposito.Resultado;
import cl.bcs.apv.application.recaudacion.DepositoColab.ComprobanteLiquidar_Entrada;
import cl.bcs.apv.application.recaudacion.DepositoColab.CrearIngresoEgreso_Entrada;
import cl.bcs.apv.application.recaudacion.DepositoColab.CrearIngresoEgreso_Salida;
import cl.bcs.apv.application.recaudacion.DepositoColab.FormaDePago_Entrada;
import cl.bcs.apv.application.recaudacion.DepositoColab.IngresoEgreso_Entrada;
import cl.bcs.apv.application.recaudacion.DepositoColab.LiquidarComprobante_Entrada;
import cl.bcs.apv.application.recaudacion.DepositoColab.LiquidarComprobante_Salida;
import cl.bcs.apv.application.recaudacion.DepositoFilas.CajaTesoreria;
import cl.bcs.apv.application.recaudacion.DepositoFilas.CuentaBanco;
import cl.bcs.apv.application.recaudacion.DepositoFilas.DatosBanco;
import cl.bcs.apv.application.recaudacion.DepositoFilas.DatosEmpresa;
import cl.bcs.apv.application.recaudacion.DepositoFilas.DatosEntidad;
import cl.bcs.apv.application.recaudacion.DepositoFilas.Recaudacion;
import cl.bcs.apv.domain.port.out.AlmacenArchivos;
import cl.bcs.apv.domain.port.out.Colaboradores;
import cl.bcs.apv.domain.port.out.ConfiguracionSistema;
import cl.bcs.apv.domain.port.out.DiccionarioMensajes;
import cl.bcs.apv.domain.port.out.Reloj;
import cl.bcs.apv.domain.port.out.Secuencias;

/**
 * Use case {@code CrearRecaudacionDeposito} (family C): creates or modifies deposits and, for new ones, the Tesoreria income movements and their
 * settlement. Legacy {@code cl.bcs.APV.CrearRecaudacionDeposito} (call and logica).
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class CrearRecaudacionDepositoServicio implements CrearRecaudacionDeposito {

    private static final String SQL_CREAR_RECAUDACION = SqlLegacy.recurso("Deposito_Insert");
    private static final String SQL_MODIFICAR_RECAUDACION = SqlLegacy.recurso("Deposito_Update");
    private static final String SQL_SELECT_ENTIDAD_RECAUDADORA = SqlLegacy.recurso("ConsultaEntidadRecaudadora");
    private static final String SQL_SELECT_PERSONA_BANCO = SqlLegacy.recurso("ConsultarPersonaBanco");
    private static final String SQL_SELECT_RECAUDACION = SqlLegacy.recurso("DepositoC_SelectRecaudacion");
    private static final String SQL_DATOS_EMPRESA = SqlLegacy.recurso("DepositoC_DatosEmpresa");
    private static final String SQL_SELECT_CUENTA_BANCO = SqlLegacy.recurso("DepositoC_SelectCuentaBanco");
    private static final String SQL_SELECT_CAJA_RECAUDACION_TESORERIA = SqlLegacy.recurso("DepositoC_SelectCajaRecaudacion");

    private final NamedParameterJdbcTemplate jdbc;
    private final ConfiguracionSistema configuracion;
    private final DiccionarioMensajes diccionario;
    private final Secuencias secuencias;
    private final Colaboradores colaboradores;
    private final SqlLegacy sql;

    public CrearRecaudacionDepositoServicio(NamedParameterJdbcTemplate jdbc, ConfiguracionSistema configuracion, DiccionarioMensajes diccionario,
            Secuencias secuencias, Reloj reloj, AlmacenArchivos archivos, Colaboradores colaboradores) {
        this.jdbc = jdbc;
        this.configuracion = configuracion;
        this.diccionario = diccionario;
        this.secuencias = secuencias;
        this.colaboradores = colaboradores;
        this.sql = new SqlLegacy(jdbc);
    }

    @Override
    public CrearRecaudacionDepositoResultado ejecutar(CrearRecaudacionDepositoEntrada in) {
        try {
            Salida out = logica(in);
            return new CrearRecaudacionDepositoResultado(MensajeLegacy.aMensajes(out.mensajes), MensajeLegacy.contarErrores(out.mensajes), out.idRec,
                    out.resultado);
        } catch (Exception e) {
            throw Sneaky.lanzar(e);
        }
    }

    /** Mutable outcome of the legacy logica (ParametrosSalidaServicio). */
    private static final class Salida {
        final List<MensajeLegacy> mensajes = new ArrayList<>();
        final List<Resultado> resultado = new ArrayList<>();
        Long idRec;
    }

    private Salida logica(CrearRecaudacionDepositoEntrada in) throws Exception {
        String fechaSistema = Plataforma.fechaSistema(configuracion, in.idEmpresa());
        List<ComprobanteLiquidar_Entrada> lstComprobanteLiquidar = new ArrayList<>();

        HashMap<String, String> valDic = new HashMap<>();
        valDic.put("a", "n");

        Salida salida = new Salida();
        List<IngresoEgreso_Entrada> paramsIngresoEgreso = new ArrayList<>();
        List<FormaDePago_Entrada> paramsFormaDePago = new ArrayList<>();
        List<CuentaBanco> lstCuentaBancoCorredora = new ArrayList<>();

        List<Long> idsRecaudacion = Colab.secuencia(secuencias, "BSCL.APV.BD.Secuencia::REC.IdRecaudacion", (long) in.parametros().size());

        // IdPersona y IdPortafolioDefecto de la corredora
        HashMap<String, Object> paramsDatosEmpresa = new HashMap<>();
        paramsDatosEmpresa.put("IdEmpresa", in.idEmpresa());
        List<DatosEmpresa> lstDatosEmpresa = sql.consultar(SQL_DATOS_EMPRESA, paramsDatosEmpresa, DatosEmpresa.class);

        if (lstDatosEmpresa.isEmpty()) {
            salida.mensajes.add(Plataforma.mensaje(diccionario, in.idEmpresa(), 0L, MensajeLegacy.ERROR, "NO_SE_PUEDE_OBTENER_CONNFIGURACION_EMPRESA", null));
            return salida;
        }

        Long idPortafolioCorredora = lstDatosEmpresa.get(0).IdPortafolioDefecto;
        Long idPersonaCorredora = lstDatosEmpresa.get(0).IdPersona;

        // Si es traspaso se busca informacion de la entidad recaudadora
        List<Long> idContratosSAAPV = new ArrayList<>();
        for (cl.bcs.apv.api.dto.crearrecaudaciondeposito.Recaudacion fila : in.parametros()) {
            idContratosSAAPV.add(fila.numeroDocumento());
        }
        HashMap<String, Object> paramsDatosEntidad = new HashMap<>();
        paramsDatosEntidad.put("IdContratoSAAPV", idContratosSAAPV);
        List<DatosEntidad> personaEntidad = sql.consultar(SQL_SELECT_ENTIDAD_RECAUDADORA, paramsDatosEntidad, DatosEntidad.class);

        // Caja tesoreria (PAC) destinada para recaudacion APV
        List<CajaTesoreria> cajaTesoreria = sql.consultar(SQL_SELECT_CAJA_RECAUDACION_TESORERIA, paramsDatosEmpresa, CajaTesoreria.class);
        if (cajaTesoreria.size() == 0) {
            salida.mensajes.add(Plataforma.mensaje(diccionario, in.idEmpresa(), 0L, MensajeLegacy.ERROR, "CAJA_RECAUDACION_CERRADA_O_INEXISTENTE", null));
            return salida;
        }
        Long idCajaRecaudacion = cajaTesoreria.get(0).IdCaja;

        int contadorIds = 0;
        List<Recaudacion> lstRecaudacion = new ArrayList<>();
        List<Long> lstIdRecaudacion = new ArrayList<>();
        HashMap<String, Object> params = new HashMap<>();
        List<String> lstNumeroCuentaCorredora = new ArrayList<>();
        List<Long> lstIdBanco = new ArrayList<>();

        for (cl.bcs.apv.api.dto.crearrecaudaciondeposito.Recaudacion fila : in.parametros()) {
            Long idRecaudacionFila = fila.idRecaudacion();
            if (!(in.tipo().compareTo("M") == 0)) {
                idRecaudacionFila = idsRecaudacion.get(contadorIds);
            }
            contadorIds = contadorIds + 1;

            Recaudacion objRecaudacion = new Recaudacion();
            objRecaudacion.IdRecaudacion = idRecaudacionFila;
            objRecaudacion.IdEmpresa = in.idEmpresa();
            objRecaudacion.CodigoArchivo = fila.codigoArchivo();
            objRecaudacion.FolioCartola = fila.folioCartola();
            objRecaudacion.LineaCartola = fila.lineaCartola();
            objRecaudacion.Sucursal = fila.sucursal();
            objRecaudacion.FechaDeposito = fila.fechaDeposito();
            objRecaudacion.CuentaCorriente = fila.cuentaCorriente();
            objRecaudacion.MontoDeposito = fila.montoDeposito();
            objRecaudacion.ConceptoRecaudacion = fila.conceptoRecaudacion();
            objRecaudacion.Origen = in.origen();
            objRecaudacion.Observacion = fila.observacion();
            objRecaudacion.NumeroDocumento = fila.numeroDocumento();
            objRecaudacion.IdTipoDeposito = fila.idTipoDeposito();
            objRecaudacion.IdBanco = fila.idBanco();
            objRecaudacion.IdRegimenTributario = 0L;
            objRecaudacion.IdCarga = fila.idCarga();
            objRecaudacion.IdEstado = fila.idEstado();
            objRecaudacion.IdInterno = fila.idInterno();
            objRecaudacion.IdCuentaBanco = fila.idCuentaBanco();
            lstIdBanco.add(fila.idBanco());
            lstRecaudacion.add(objRecaudacion);
            lstIdRecaudacion.add(idRecaudacionFila);

            // Para rescatar IdCuentaBanco de la corredora en caso de que no lo declaren
            if (in.tipo().compareTo("I") == 0) {
                if (fila.idCuentaBanco().equals(0L)) {
                    lstNumeroCuentaCorredora.add(fila.cuentaCorriente().trim().replaceFirst("^0*", ""));
                }
            }
        }

        HashMap<Long, Recaudacion> hashRecaudacionOK = new HashMap<>();

        if (!lstRecaudacion.isEmpty()) {
            int[] res = LoteConteo.ejecutar(jdbc, in.tipo().compareTo("M") == 0 ? SQL_MODIFICAR_RECAUDACION : SQL_CREAR_RECAUDACION, lstRecaudacion);

            if (res == null || res.length == 0) {
                salida.mensajes.add(Plataforma.mensaje(diccionario, in.idEmpresa(), (long) contadorIds, MensajeLegacy.ERROR,
                        in.tipo().compareTo("M") == 0 ? "UI_MODIFICACION_DEPOSITO_ERROR" : "UI_CREACION_DEPOSITO_ERROR", valDic));
                return salida;
            }

            // Datos de las recaudaciones correctamente insertadas
            params.clear();
            params.put("IdsRecaudacion", lstIdRecaudacion);
            List<Recaudacion> lstRecaudacionOK = sql.consultar(SQL_SELECT_RECAUDACION, params, Recaudacion.class);
            // legacy Utils.List2Hash: a repeated key throws
            for (Recaudacion r : lstRecaudacionOK) {
                if (hashRecaudacionOK.containsKey(r.IdRecaudacion)) {
                    throw new Exception("key field is not unique");
                }
                hashRecaudacionOK.put(r.IdRecaudacion, r);
            }
        }

        // IdCuentaBanco de la corredora en caso de que no lo declaren
        if (!lstNumeroCuentaCorredora.isEmpty()) {
            params.clear();
            params.put("IdsBanco", lstIdBanco);
            params.put("NumerosCuentaBancaria", lstNumeroCuentaCorredora);
            lstCuentaBancoCorredora = sql.consultar(SQL_SELECT_CUENTA_BANCO, params, CuentaBanco.class);
            if (lstCuentaBancoCorredora.size() == 0) {
                salida.mensajes.add(Plataforma.mensaje(diccionario, in.idEmpresa(), 0L, MensajeLegacy.ERROR, "UI_BANCO_CORREDORA_Y_CUENTA_NO_VALIDOS", valDic));
                return salida;
            }
        }
        // identificador de persona de la entidad bancaria que deposita
        HashMap<String, Object> paramsDatosBanco = new HashMap<>();
        paramsDatosBanco.put("IdsBanco", lstIdBanco);
        List<DatosBanco> personaBanco = sql.consultar(SQL_SELECT_PERSONA_BANCO, paramsDatosBanco, DatosBanco.class);

        // PREPARA PARAMETROS PARA ENVIO A TESORERIA
        long numeroReferencia = 0L;
        for (Recaudacion fila : lstRecaudacion) {
            numeroReferencia++;
            Recaudacion infoRecaudacion = hashRecaudacionOK.get(fila.IdRecaudacion);

            if (infoRecaudacion != null) {
                String codigoMensajeExito = in.tipo().compareTo("M") == 0 ? "UI_MODIFICACION_DEPOSITO_OK" : "UI_CREACION_DEPOSITO_OK";
                String mensajeExito = diccionario.mensaje(in.idEmpresa(), codigoMensajeExito, Plataforma.valores().con("IdRecaudacion", fila.IdRecaudacion));
                salida.mensajes.add(new MensajeLegacy(fila.IdRecaudacion, MensajeLegacy.OK, codigoMensajeExito, mensajeExito));

                salida.resultado.add(new Resultado(fila.IdInterno, fila.IdRecaudacion));
                salida.idRec = fila.IdRecaudacion;

                if (in.tipo().compareTo("I") == 0) {
                    IngresoEgreso_Entrada objIngresoEgreso = new IngresoEgreso_Entrada();
                    FormaDePago_Entrada objFormaDePago = new FormaDePago_Entrada();

                    objIngresoEgreso.NumeroReferencia = numeroReferencia;
                    objIngresoEgreso.AgruparEnComprobante = "N";
                    objIngresoEgreso.Bilateral = "N";
                    objIngresoEgreso.FechaComprobante = fechaSistema;
                    objIngresoEgreso.FechaLiquidacion = fechaSistema;
                    objIngresoEgreso.FechaLiquidacionComprobante = fechaSistema;
                    objIngresoEgreso.FechaOperacion = fechaSistema;
                    objIngresoEgreso.FechaParidad = "0001-01-01";
                    objIngresoEgreso.FolioCartera = 0L;
                    objIngresoEgreso.GeneraComprobante = "S";
                    objIngresoEgreso.Glosa = "";
                    objIngresoEgreso.IdBolsa = "";
                    objIngresoEgreso.IdCaja = idCajaRecaudacion;
                    objIngresoEgreso.IdClasificacionTipoInstrumento = "";

                    if (in.origen().equals("BONIF")) {
                        objIngresoEgreso.IdComprobante = 0L;
                        objIngresoEgreso.IdConcepto = "INGBON";
                    } else {
                        objIngresoEgreso.IdComprobante = fila.IdRecaudacion;
                        objIngresoEgreso.IdConcepto = "TRASPA".equals(in.origen()) ? "ATRAPV" : "ABNAPV";
                    }

                    objIngresoEgreso.IdCondicionLiquidacion = "";
                    objIngresoEgreso.IdEmpleadoDigitador = 0L;
                    objIngresoEgreso.IdInstrumento = 0L;
                    objIngresoEgreso.IdMoneda = "CLP";
                    objIngresoEgreso.IdMonedaOperacion = "";
                    objIngresoEgreso.IdOperacion = fila.IdRecaudacion;
                    objIngresoEgreso.IdPersona = idPersonaCorredora;

                    if ("TRASPA".equalsIgnoreCase(in.origen())) {
                        for (int j = 0; j < personaEntidad.size(); j++) {
                            if (personaEntidad.get(j).IdContratoSAAPV.compareTo(fila.NumeroDocumento) == 0) {
                                objIngresoEgreso.IdPersonaTitularPago = personaEntidad.get(j).IdPersonaEntidad;
                            }
                        }
                    } else {
                        for (int i = 0; i < personaBanco.size(); i++) {
                            if (personaBanco.get(i).IdBanco.compareTo(fila.IdBanco) == 0) {
                                objIngresoEgreso.IdPersonaTitularPago = personaBanco.get(i).IdPersonaBanco;
                            }
                        }
                    }

                    objIngresoEgreso.IdPortafolioTitularPago = idPortafolioCorredora;
                    objIngresoEgreso.IdPortafolio = idPortafolioCorredora;

                    objIngresoEgreso.IdSucursalEmpleadoDigitador = 0L;
                    objIngresoEgreso.IdTipoComprobante = "APV";
                    objIngresoEgreso.IdTipoInstrumento = "";
                    objIngresoEgreso.IdTipoMovimiento = "MOVING";
                    objIngresoEgreso.IdTipoOperacion = "INGTES";
                    objIngresoEgreso.IdTipoPersona = "CLI";
                    objIngresoEgreso.IdTipoPersonaTitularPago = "CLI";
                    objIngresoEgreso.IdTipoProducto = "";
                    objIngresoEgreso.IdTipoReajuste = "";
                    objIngresoEgreso.Monto = fila.MontoDeposito;
                    objIngresoEgreso.MontoOperacion = fila.MontoDeposito;
                    objIngresoEgreso.MueveCuentaInversion = "S";
                    objIngresoEgreso.SecuenciaAgrupacion = "";
                    objIngresoEgreso.SecuenciaRelacionada = null;
                    objIngresoEgreso.TitularPago = "S";
                    objIngresoEgreso.ValorParidad = BigDecimal.valueOf(0);
                    paramsIngresoEgreso.add(objIngresoEgreso);

                    // forma de pago correspondiente al movimiento
                    objFormaDePago.NumeroReferencia = numeroReferencia;
                    objFormaDePago.NumeroReferenciaFormaDePago = fila.IdRecaudacion;
                    objFormaDePago.FechaDocumentoPagoCorredora = "0001-01-01";
                    objFormaDePago.FechaDocumentoPagoSocio = fechaSistema;
                    objFormaDePago.IdBancoSocio = 0L;
                    objFormaDePago.IdChequera = 0L;

                    if (fila.IdCuentaBanco.compareTo(0L) == 0) {
                        for (CuentaBanco cta : lstCuentaBancoCorredora) {
                            if (fila.IdBanco.compareTo(cta.IdBanco) == 0
                                    && fila.CuentaCorriente.trim().replaceFirst("^0*", "").equals(cta.NumeroCuentaBancaria.trim().replaceFirst("^0*", ""))) {
                                fila.IdCuentaBanco = cta.IdCuentaBanco;
                            }
                        }
                    }

                    objFormaDePago.IdCuentaBancariaCorredora = fila.IdCuentaBanco;
                    objFormaDePago.IdCuentaBancariaSocio = 0L;
                    objFormaDePago.IdMedioDePago = fila.IdTipoDeposito;
                    objFormaDePago.IdModalidadDePago = "MODDEP";
                    objFormaDePago.IdMoneda = "CLP";
                    objFormaDePago.IdTarjetaBancariaCorredora = 0L;
                    objFormaDePago.IdTarjetaBancariaSocio = 0L;
                    objFormaDePago.IdTipoCuentaBancariaSocio = "";
                    objFormaDePago.IdTipoTarjetaBancariaSocio = "";
                    objFormaDePago.Monto = fila.MontoDeposito;
                    objFormaDePago.NombreCuentaBancariaSocio = "";
                    objFormaDePago.NombreTipoCuentaBancariaSocio = "";
                    objFormaDePago.NombreTipoTarjetaBancariaSocio = "";
                    objFormaDePago.NumeroCuentaBancariaSocio = "";
                    objFormaDePago.NumeroDocumentoCorredora = 0L;
                    objFormaDePago.NumeroDocumentoSocio = 0L;
                    objFormaDePago.NumeroSerieSocio = "";
                    objFormaDePago.NumeroTarjetaBancariaSocio = "";

                    paramsFormaDePago.add(objFormaDePago);
                }
            }
        }

        // ENVIO A TESORERIA (Movimiento y comprobante)
        if (!paramsIngresoEgreso.isEmpty()) {
            CrearIngresoEgreso_Entrada paramsIngresoEgresoEntrada = new CrearIngresoEgreso_Entrada();
            paramsIngresoEgresoEntrada.p_IdEmpresa = in.idEmpresa();
            paramsIngresoEgresoEntrada.p_IdUsuario = in.idUsuario();
            paramsIngresoEgresoEntrada.p_IdModulo = in.idModulo();
            paramsIngresoEgresoEntrada.p_SimularOperacion = "N";
            paramsIngresoEgresoEntrada.p_UsaReglasNegocio = "S";
            paramsIngresoEgresoEntrada.p_EsIngresoAutomatico = "N";
            paramsIngresoEgresoEntrada.p_FormaDePago = paramsFormaDePago;
            paramsIngresoEgresoEntrada.p_IngresoEgresoGenerar = paramsIngresoEgreso;

            JsonObject respuesta = colaboradores.invocar("CrearIngresoEgreso", Colab.aJson(paramsIngresoEgresoEntrada));
            CrearIngresoEgreso_Salida outTesoreria = Colab.de(respuesta, CrearIngresoEgreso_Salida.class);

            salida.mensajes.addAll(outTesoreria.P_OUT_MENSAJES);

            if (MensajeLegacy.contarErrores(outTesoreria.P_OUT_MENSAJES) > 0) {
                return salida;
            } else {
                for (DepositoColab.ResultadoProceso_Salida outResultado : outTesoreria.P_OUT_RESULTADOPROCESO) {
                    // Id de los comprobantes de tesoreria generados para liquidarlos
                    if (outResultado.Campo.equals("IdComprobante") && outResultado.Resultado.equals("ING")
                            && outResultado.Servicio.equals("BSCL.Tesoreria.BD.Servicios::CrearComprobante")) {
                        ComprobanteLiquidar_Entrada objComprobanteLiquidar = new ComprobanteLiquidar_Entrada();
                        objComprobanteLiquidar.IdComprobante = outResultado.Id;
                        objComprobanteLiquidar.IdTipoMovimiento = "MOVING";
                        objComprobanteLiquidar.AfectaCuentaInversion = "CLI";
                        lstComprobanteLiquidar.add(objComprobanteLiquidar);
                    }
                }
            }
        }

        // LIQUIDACION TESORERIA
        String valorParametroLiquidacion = Plataforma.parametro(configuracion, in.idEmpresa(), "APV", "REC.LiquidacionAutomatica");

        if (valorParametroLiquidacion.equals("S") && lstComprobanteLiquidar.size() > 0) {
            LiquidarComprobante_Entrada paramsLiquidarEntrada = new LiquidarComprobante_Entrada();
            paramsLiquidarEntrada.p_IdEmpresa = in.idEmpresa();
            paramsLiquidarEntrada.p_IdUsuario = in.idUsuario();
            paramsLiquidarEntrada.p_IdModulo = in.idModulo();
            paramsLiquidarEntrada.p_UsaReglasNegocio = "S";
            paramsLiquidarEntrada.p_FechaMovimiento = fechaSistema.toString();
            paramsLiquidarEntrada.p_ComprobanteLiquidar = lstComprobanteLiquidar;

            JsonObject respuesta = colaboradores.invocar("LiquidarComprobante", Colab.aJson(paramsLiquidarEntrada));
            LiquidarComprobante_Salida outLiquidar = Colab.de(respuesta, LiquidarComprobante_Salida.class);

            salida.mensajes.addAll(outLiquidar.P_OUT_MENSAJES);

            if (MensajeLegacy.contarErrores(outLiquidar.P_OUT_MENSAJES) > 0) {
                return salida;
            }
        }
        return salida;
    }
}
