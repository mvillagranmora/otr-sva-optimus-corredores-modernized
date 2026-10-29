package cl.bcs.fondomutuo.requisitos;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;

import cl.bcs.fondomutuo.kit.JsonLegacy;
import cl.bcs.fondomutuo.kit.MensajeSalida;
import cl.bcs.fondomutuo.port.out.http.ProductosPortafolioPort;
import cl.bcs.fondomutuo.port.out.kit.ConfiguracionSistemaPort;
import cl.bcs.fondomutuo.port.out.kit.DiccionarioMensajesPort;
import cl.bcs.fondomutuo.requisitos.RequisitosDatos.ClientePortafolioSolicitudes;
import cl.bcs.fondomutuo.requisitos.RequisitosDatos.ObjValidaProducto;

/**
 * Legacy {@code cl.bcs.fondomutuo.ServicioCumpleRequisitosCrearSolicitudAporteRescate} (529 LOC), rules FM-REQ-01..21 and the defects D-REQ-1..4.
 * Plain Java, no state of its own; the collaborators are ports: data reads ({@link RequisitosDatos}), the dictionary, the configuration and the
 * platform class {@code ConsultarProductosPortafolio} (H port, JSON of the legacy class).
 *
 * <p>Kept bug-for-bug (do not "fix" without the domain expert): the first-phase messages are collapsed BY DESCRIPTION keeping the first one and come
 * out in {@link HashMap} order (D-REQ-1: a repeated text of a later request is lost, so a request that fails can pass); the message of
 * {@code EsFechaValidaRescate} is built with the wrong parameter map (D-REQ-2); the unknown-user error looks the dictionary up with the USER id as
 * company and {@code p_IdEmpleado} is never read (D-REQ-3); {@code CLIENTE_NO_EXISTE}/{@code PORTAFOLIO_NO_EXISTE} are unreachable because the
 * portfolio is dereferenced before the checks (D-REQ-4); a request whose client row is missing raises {@link NullPointerException}; the two shared
 * parameter maps keep the legacy put/clear sequence.
 */
public class ServicioCumpleRequisitosService {

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public Long p_IdEmpleado;
        public List<InSolicitudes> p_SolicitudesAporteRescate = new ArrayList<>();
        public List<InPortafolio> p_Portafolios = new ArrayList<>();
        public String p_PermiteOperarProspectos = "N";
    }

    public static class InSolicitudes {
        public Long IdInterno;
        public Long IdCliente;
        public Long IdPortafolio;
        public String IdSerieFondo;
        public String IdTipoMovimientoFondoMutuo;
        public String IdEstadoSerie;
        public BigDecimal Monto;
        public BigDecimal NumeroCuotas;
        public String EstadoFondo;
        public String EstadoBloqueo;
        public String EsFondoValido;
        public String EsFechaValidaAporte;
        public String EsFechaValidaRescate;
        public String EsDiferido;
        public String EsMontoMaximo;
        public String EsMontoMinimo;
        public BigDecimal ValorCuota;
        public String Overnight;
        public String EstadoLiquidez;
        public String EsMoneyMarket;
        public String EsFamiliaEquivalente;
        public String EsPortafolioRegimenValido;
        public String EsRegimenValido;
        public String PermiteAporte;
        public String PermiteTraspaso = "S";
        public String PermiteRescate = "S";
        public String RestringeAporte = "N";
        public String RestringeRescate = "N";
        public String PermiteRescateMandato;
        public String PermiteCambioGrupo;
        public String EsRegimenDestinoValido;
        public String PermiteCambioFondo = "S";
        public String EsPortafolioValido;
        public String EsCuentaInversionValido;
        public String IdTipoOperacion;
        public String IdTipoProducto;
        public String Glosa = "";
        public Long FolioCartera = 0L;
        public String ProcedenciaCanal = "";
    }

    public static class InPortafolio {
        public Long IdInterno;
        public Long IdPortafolio;
        public Long IdPortafolioReal;
        public String IdEstadoPortafolio;
        public Long IdCliente;
        public Long IdClienteReal;
        public String IdEstadoCliente;
        public Long IdClienteDestino;
        public Long IdClienteRealDestino;
        public Long IdPortafolioDestino;
        public Long IdPortafolioRealDestino;
        public String IdEstadoClienteDestino;
        public String IdEstadoPortafolioDestino;
        public String DocumentoIdentidad;
        public Long IdEmpleadoPortafolio;
    }

    public static class Salida {
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
    }

    /** JSON contract of the collaborator {@code ConsultarProductosPortafolio} (legacy names). */
    public static class ConsultarProductosPortafolioEntrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public List<PortafolioEntrada> p_portafolios = new ArrayList<>();
    }

    public static class PortafolioEntrada {
        public Long IdPortafolio;
    }

    public static class ConsultarProductosPortafolioSalida {
        public List<DetallePerfilProducto> p_out_PortafolioProductos = new ArrayList<>();
    }

    public static class DetallePerfilProducto {
        public Long IdPortafolio;
        public String IdEstadoPerfilProducto;
        public String IdTipoProducto;
        public String IdTipoOperacion;
    }

    private final RequisitosDatos datos;
    private final DiccionarioMensajesPort diccionario;
    private final ConfiguracionSistemaPort configuracion;
    private final ProductosPortafolioPort productos;

    public ServicioCumpleRequisitosService(RequisitosDatos datos, DiccionarioMensajesPort diccionario, ConfiguracionSistemaPort configuracion,
            ProductosPortafolioPort productos) {
        this.datos = datos;
        this.diccionario = diccionario;
        this.configuracion = configuracion;
        this.productos = productos;
    }

    private MensajeSalida msg(Long id, String tipo, String codigo, HashMap<String, String> valores, Long idEmpresa) {
        return MensajeSalida.conTexto(id, tipo, codigo, valores, diccionario, idEmpresa);
    }

    public Salida logica(Entrada entrada) throws Exception {
        Salida out = new Salida();
        String reinversionDistintasFamilias;
        try {
            reinversionDistintasFamilias = configuracion.texto(entrada.p_IdEmpresa, "FONDOMUTUO", "VALIDAR_REINVERSION_DISTINTAS_FAMILIAS");
        } catch (Exception e) {
            reinversionDistintasFamilias = "S";
        }

        HashMap<Long, InPortafolio> portafolios = Hashes.aHash(entrada.p_Portafolios, p -> p.IdInterno);
        HashMap<String, String> params = new HashMap<>();
        HashMap<String, String> params2 = new HashMap<>();

        List<String> listaTipoProducto = new ArrayList<>();
        List<String> listaTipoOperacion = new ArrayList<>();
        ConsultarProductosPortafolioEntrada entradaProductos = new ConsultarProductosPortafolioEntrada();

        List<Long> idsPortafolios = new ArrayList<>();
        for (InSolicitudes solicitud : entrada.p_SolicitudesAporteRescate) {
            InPortafolio portafolio = portafolios.get(solicitud.IdInterno);
            if (!idsPortafolios.contains(portafolio.IdPortafolio)) {
                idsPortafolios.add(portafolio.IdPortafolio);
            }
        }

        List<ClientePortafolioSolicitudes> clientesSolicitudes = datos.clientesPortafolio(idsPortafolios);
        HashMap<Long, ClientePortafolioSolicitudes> hclientes = Hashes.aHash(clientesSolicitudes, c -> c.IdPortafolio);

        for (InSolicitudes solicitud : entrada.p_SolicitudesAporteRescate) {
            validarSolicitud(entrada, out, solicitud, portafolios.get(solicitud.IdInterno), hclientes.get(solicitud.IdPortafolio), params, params2,
                    reinversionDistintasFamilias, listaTipoProducto, listaTipoOperacion, entradaProductos);
        }
        // En caso de descripcion duplicada, mantiene el primera descripcion (legacy: toMap -> HashMap order)
        out.P_OUT_MENSAJES = out.P_OUT_MENSAJES.stream().collect(Collectors.toMap(m -> m.descripcion, m -> m, (m1, m2) -> m1)).values().stream()
                .collect(Collectors.toCollection(ArrayList::new));

        entradaProductos.p_IdEmpresa = entrada.p_IdEmpresa;
        entradaProductos.p_IdModulo = "FMM";
        entradaProductos.p_IdUsuario = entrada.p_IdUsuario;

        JsonObject respuesta = productos.consultar(JsonLegacy.conNulos().toJsonTree(entradaProductos).getAsJsonObject());
        ConsultarProductosPortafolioSalida salidaProductos = JsonLegacy.plano().fromJson(respuesta, ConsultarProductosPortafolioSalida.class);
        boolean validaProducto;
        for (InSolicitudes solicitud : entrada.p_SolicitudesAporteRescate) {
            validaProducto = false;
            for (DetallePerfilProducto detalle : salidaProductos.p_out_PortafolioProductos) {
                if (detalle.IdPortafolio.equals(solicitud.IdPortafolio) && detalle.IdTipoProducto.equalsIgnoreCase(solicitud.IdTipoProducto)
                        && (detalle.IdTipoOperacion.equalsIgnoreCase("0") || detalle.IdTipoOperacion.equalsIgnoreCase(solicitud.IdTipoOperacion))
                        && (detalle.IdEstadoPerfilProducto.equalsIgnoreCase("ACT"))) {
                    validaProducto = true;
                }
            }
            if (!validaProducto) {
                out.P_OUT_MENSAJES.add(msg(solicitud.IdInterno, MensajeSalida.MENSAJE_ERROR, "PORTAFOLIO_NO_OPERA_PRODUCTO_SELECCIONADO", params,
                        entrada.p_IdEmpresa));
            }
        }

        List<ObjValidaProducto> resEmpleadoUsuario = datos.empleadoUsuario(entrada.p_IdUsuario);
        for (ObjValidaProducto res : resEmpleadoUsuario) {
            if (res.ValidaProducto.equals("N")) {
                for (InSolicitudes solicitud : entrada.p_SolicitudesAporteRescate) {
                    // D-REQ-3: the dictionary is read with the USER id in the place of the company
                    out.P_OUT_MENSAJES.add(msg(solicitud.IdInterno, MensajeSalida.MENSAJE_ERROR, "ERROR_AL_ENCONTRAR_ID_USUARIO", params,
                            entrada.p_IdUsuario));
                }
            }
        }
        return out;
    }

    private void validarSolicitud(Entrada entrada, Salida out, InSolicitudes solicitud, InPortafolio portafolio, ClientePortafolioSolicitudes cliente,
            HashMap<String, String> params, HashMap<String, String> params2, String reinversionDistintasFamilias, List<String> listaTipoProducto,
            List<String> listaTipoOperacion, ConsultarProductosPortafolioEntrada entradaProductos) {
        Long emp = entrada.p_IdEmpresa;
        String E = MensajeSalida.MENSAJE_ERROR;
        if (!listaTipoProducto.contains(solicitud.IdTipoProducto)) listaTipoProducto.add(solicitud.IdTipoProducto);
        if (!listaTipoOperacion.contains(solicitud.IdTipoOperacion)) listaTipoOperacion.add(solicitud.IdTipoOperacion);
        PortafolioEntrada port = new PortafolioEntrada();
        port.IdPortafolio = portafolio.IdPortafolio;
        entradaProductos.p_portafolios.add(port);

        // Valida Cliente
        if (portafolio == null || portafolio.IdClienteReal == null) {
            out.P_OUT_MENSAJES.add(msg(solicitud.IdInterno, E, "CLIENTE_NO_EXISTE", params, emp));
        }
        // Valida Cliente Habilitado
        if (portafolio != null && !Constantes3B.ESTADO_ACTIVO.equalsIgnoreCase(portafolio.IdEstadoCliente)
                && !((Constantes3B.SI).equals(entrada.p_PermiteOperarProspectos) && Constantes3B.SI.equals(cliente.EsClienteNuevoProspecto))) {
            params.put("DocumentoIdentidad", cliente.DocumentoIdentidad);
            out.P_OUT_MENSAJES.add(msg(solicitud.IdInterno, E, "CLIENTE_BLOQUEADO", params, emp));
            params.clear();
        }
        // Valida Portafolio Existe
        if (portafolio == null || portafolio.IdPortafolioReal == null) {
            out.P_OUT_MENSAJES.add(msg(solicitud.IdInterno, E, "PORTAFOLIO_NO_EXISTE", params, emp));
        }
        // Valida Portafolio habilitado
        if (portafolio != null && !Constantes3B.ESTADO_ACTIVO.equalsIgnoreCase(portafolio.IdEstadoPortafolio)) {
            out.P_OUT_MENSAJES.add(msg(solicitud.IdInterno, E, "PORTAFOLIO_NO_HABILITADO", params, emp));
        }
        if (portafolio != null && (portafolio.IdClienteRealDestino == null || portafolio.IdClienteRealDestino.intValue() != 0)) {
            if (portafolio.IdClienteRealDestino == null) {
                out.P_OUT_MENSAJES.add(msg(solicitud.IdInterno, E, "CLIENTE_DESTINO_NO_EXISTE", params, emp));
            }
            if (!Constantes3B.ESTADO_ACTIVO.equalsIgnoreCase(portafolio.IdEstadoClienteDestino)) {
                out.P_OUT_MENSAJES.add(msg(solicitud.IdInterno, E, "CLIENTE_DESTINO_BLOQUEADO", params, emp));
            }
            if (portafolio.IdClienteRealDestino != null && portafolio.IdClienteReal != null
                    && portafolio.IdClienteRealDestino.intValue() == portafolio.IdClienteReal.intValue()) {
                out.P_OUT_MENSAJES.add(msg(solicitud.IdInterno, E, "CLIENTE_DESTINO_NO_VALIDO", params, emp));
            }
            if (portafolio.IdPortafolioRealDestino == null || portafolio.IdPortafolioRealDestino.intValue() != 0) {
                if (portafolio.IdPortafolioRealDestino == null) {
                    out.P_OUT_MENSAJES.add(msg(solicitud.IdInterno, E, "PORTAFOLIO_DESTINO_NO_EXISTE", params, emp));
                }
                if (!Constantes3B.ESTADO_ACTIVO.equalsIgnoreCase(portafolio.IdEstadoPortafolio)) {
                    out.P_OUT_MENSAJES.add(msg(solicitud.IdInterno, E, "PORTAFOLIO_DESTINO_NO_HABILITADO", params, emp));
                }
                if (portafolio.IdPortafolioRealDestino != null && portafolio.IdPortafolioReal != null
                        && portafolio.IdPortafolioRealDestino.intValue() == portafolio.IdPortafolioReal.intValue()) {
                    out.P_OUT_MENSAJES.add(msg(solicitud.IdInterno, E, "PORTAFOLIO_DESTINO_NO_VALIDO", params, emp));
                }
            }
        }
        validarSerieYMontos(entrada, out, solicitud, cliente, params, params2, reinversionDistintasFamilias);
    }

    private void validarSerieYMontos(Entrada entrada, Salida out, InSolicitudes solicitud, ClientePortafolioSolicitudes cliente,
            HashMap<String, String> params, HashMap<String, String> params2, String reinversionDistintasFamilias) {
        Long emp = entrada.p_IdEmpresa;
        String E = MensajeSalida.MENSAJE_ERROR;
        String A = MensajeSalida.MENSAJE_ADVERTENCIA;
        if ("".equalsIgnoreCase(solicitud.IdSerieFondo)) {
            params.put("Nemotecnico", solicitud.IdSerieFondo);
            out.P_OUT_MENSAJES.add(msg(solicitud.IdInterno, E, "INSTRUMENTO_NO_EXISTE", params, emp));
            params.clear();
        }
        if (!Constantes3B.ESTADO_ACTIVO.equalsIgnoreCase(solicitud.IdEstadoSerie)) {
            params.put("Nemotecnico", solicitud.IdSerieFondo);
            params.put("DocumentoIdentidad", cliente.DocumentoIdentidad);
            out.P_OUT_MENSAJES.add(msg(solicitud.IdInterno, E, "INSTRUMENTO_NO_VIGENTE_RUT_CLIENTE", params, emp));
            params.clear();
        }
        if ("".equalsIgnoreCase(solicitud.IdTipoMovimientoFondoMutuo)) {
            params2.put("IdInstrumento", solicitud.IdSerieFondo);
            params2.put("DocumentoIdentidad", cliente.DocumentoIdentidad);
            out.P_OUT_MENSAJES.add(msg(solicitud.IdInterno, E, "TIPO_OPERACION_NO_VALIDA_REQ_APO_RES", params2, emp));
            params2.clear();
        }
        if (!Constantes3B.ABIERTO.equalsIgnoreCase(solicitud.EstadoFondo)) {
            params2.put("IdInstrumento", solicitud.IdSerieFondo);
            params2.put("DocumentoIdentidad", cliente.DocumentoIdentidad);
            out.P_OUT_MENSAJES.add(msg(solicitud.IdInterno, A, "FONDO_CERRADO_REQ_APO_RES", params2, emp));
            params2.clear();
        }
        if (!Constantes3B.NO.equalsIgnoreCase(solicitud.EstadoBloqueo)) {
            params2.put("IdInstrumento", solicitud.IdSerieFondo);
            params2.put("DocumentoIdentidad", cliente.DocumentoIdentidad);
            out.P_OUT_MENSAJES.add(msg(solicitud.IdInterno, E, "FONDO_BLOQUEADO_REQ_APO_RES", params2, emp));
            params2.clear();
        }
        if (!Constantes3B.SI.equalsIgnoreCase(solicitud.EsFondoValido)) {
            params2.put("IdInstrumento", solicitud.IdSerieFondo);
            params2.put("DocumentoIdentidad", cliente.DocumentoIdentidad);
            out.P_OUT_MENSAJES.add(msg(solicitud.IdInterno, E, "FONDO_INVALIDO_REQ_APO_RES", params2, emp));
            params2.clear();
        }
        if (!Constantes3B.NO.equalsIgnoreCase(solicitud.RestringeAporte)
                && Constantes3B.TIPO_OPERACION_APORTE_FONDO_MUTUO.equalsIgnoreCase(solicitud.IdTipoOperacion)) {
            params2.put("IdInstrumento", solicitud.IdSerieFondo);
            params2.put("DocumentoIdentidad", cliente.DocumentoIdentidad);
            out.P_OUT_MENSAJES.add(msg(solicitud.IdInterno, E, "FONDO_SERIE_RESTRINGIDO_APORTE_REQ_APO_RES", params2, emp));
            params2.clear();
        }
        if (!Constantes3B.NO.equalsIgnoreCase(solicitud.RestringeRescate)
                && Constantes3B.TIPO_OPERACION_RESCATE_FONDO_MUTUO.equalsIgnoreCase(solicitud.IdTipoOperacion)) {
            params2.put("IdInstrumento", solicitud.IdSerieFondo);
            params2.put("DocumentoIdentidad", cliente.DocumentoIdentidad);
            out.P_OUT_MENSAJES.add(msg(solicitud.IdInterno, E, "FONDO_SERIE_RESTRINGIDO_RESCATE_REQ_APO_RES", params2, emp));
            params2.clear();
        }
        if (!Constantes3B.SI.equalsIgnoreCase(solicitud.EsFechaValidaAporte)) {
            params2.put("DocumentoIdentidad", cliente.DocumentoIdentidad);
            out.P_OUT_MENSAJES.add(msg(solicitud.IdInterno, E, "FECHA_INVALIDA_APORTE_REQ_APO_RES", params2, emp));
            params2.clear();
        }
        if (!Constantes3B.SI.equalsIgnoreCase(solicitud.EsFechaValidaRescate)) {
            params2.put("DocumentoIdentidad", cliente.DocumentoIdentidad);
            out.P_OUT_MENSAJES.add(msg(solicitud.IdInterno, E, "FECHA_INVALIDA_RESCATE_REQ_APO_RES", params, emp));   // D-REQ-2: params, not params2
            params2.clear();
        }
        validarDemas(entrada, out, solicitud, cliente, params, params2, reinversionDistintasFamilias);
    }

    private void validarDemas(Entrada entrada, Salida out, InSolicitudes solicitud, ClientePortafolioSolicitudes cliente, HashMap<String, String> params,
            HashMap<String, String> params2, String reinversionDistintasFamilias) {
        Long emp = entrada.p_IdEmpresa;
        String E = MensajeSalida.MENSAJE_ERROR;
        String A = MensajeSalida.MENSAJE_ADVERTENCIA;
        if (!Constantes3B.SI.equalsIgnoreCase(solicitud.EsDiferido) && (!Constantes3B.RESCATE_TRASPASO.equalsIgnoreCase(solicitud.IdTipoMovimientoFondoMutuo)
                && !"DESINVERSION APV".equalsIgnoreCase(solicitud.Glosa))) {
            params2.put("DocumentoIdentidad", cliente.DocumentoIdentidad);
            out.P_OUT_MENSAJES.add(msg(solicitud.IdInterno, E, "FONDO_NO_DIFERIDO_REQ_APO_RES", params2, emp));
            params2.clear();
        }
        if (!Constantes3B.SI.equalsIgnoreCase(solicitud.PermiteCambioFondo) && (Constantes3B.REINVERSION.equalsIgnoreCase(solicitud.IdTipoMovimientoFondoMutuo)
                || Constantes3B.APORTE_REINVERSION.equalsIgnoreCase(solicitud.IdTipoMovimientoFondoMutuo))) {
            params2.put("DocumentoIdentidad", cliente.DocumentoIdentidad);
            params2.put("IdInstrumento", solicitud.IdSerieFondo);
            out.P_OUT_MENSAJES.add(msg(solicitud.IdInterno, E, "FONDO_NO_PERMITE_CAMBIO_FONDO", params2, emp));
            params2.clear();
        }
        if (!Constantes3B.SI.equalsIgnoreCase(solicitud.EsMontoMaximo)) {
            params2.put("IdInstrumento", solicitud.IdSerieFondo);
            params2.put("CodigoPortafolio", cliente.CodigoPortafolio);
            params2.put("DocumentoIdentidad", cliente.DocumentoIdentidad);
            out.P_OUT_MENSAJES.add(msg(solicitud.IdInterno, E, "MONTO_MAXIMO_REQ_APO_RES", params2, emp));
            params2.clear();
        }
        if (!Constantes3B.ORIGEN_PAC.equalsIgnoreCase(solicitud.ProcedenciaCanal)) {
            if (!Constantes3B.SI.equalsIgnoreCase(solicitud.EsMontoMinimo)) {
                params2.put("IdInstrumento", solicitud.IdSerieFondo);
                params2.put("CodigoPortafolio", cliente.CodigoPortafolio);
                params2.put("DocumentoIdentidad", cliente.DocumentoIdentidad);
                out.P_OUT_MENSAJES.add(msg(solicitud.IdInterno, E, "MONTO_MINIMO", params2, emp));
                params2.clear();
            }
        }
        // Cliente bloqueado por cumplimiento para ingreso de titulos
        if (Constantes3B.INGRESO_TITULO.equalsIgnoreCase(solicitud.IdTipoMovimientoFondoMutuo) && cliente.BloqueadoPorCumplimiento.equalsIgnoreCase("S")) {
            params2.clear();
            params2.put("DocumentoIdentidad", cliente.DocumentoIdentidad);
            params2.put("NombreCliente", cliente.NombreCompleto);
            out.P_OUT_MENSAJES.add(msg(solicitud.IdInterno, E, "CLIENTE_BLOQUEADO_POR_CUMPLIMIENTO", params2, emp));
            params2.clear();
        }
        if (BigDecimal.ZERO.compareTo(solicitud.ValorCuota) == 1 || BigDecimal.ZERO.compareTo(solicitud.ValorCuota) == 0) {
            out.P_OUT_MENSAJES.add(msg(solicitud.IdInterno, E, "VALOR_CUOTA_INCORRECTO", params, emp));
        }
        if (Constantes3B.SI.equalsIgnoreCase(solicitud.Overnight) && !Constantes3B.ABIERTO.equalsIgnoreCase(solicitud.EstadoLiquidez)) {
            out.P_OUT_MENSAJES.add(msg(solicitud.IdInterno, A, "FECHA_LIQUIDEZ_CERRADO", params, emp));
        }
        if (!Constantes3B.SI.equalsIgnoreCase(solicitud.EsMoneyMarket)) {
            out.P_OUT_MENSAJES.add(msg(solicitud.IdInterno, E, "NO_MONEY_MARKET", params, emp));
        }
        if (BigDecimal.ZERO.compareTo(solicitud.Monto) == 1 || BigDecimal.ZERO.compareTo(solicitud.Monto) == 0) {
            out.P_OUT_MENSAJES.add(msg(solicitud.IdInterno, E, "MONTO_VALIDO", params, emp));
        }
        if (BigDecimal.ZERO.compareTo(solicitud.NumeroCuotas) == 1 || BigDecimal.ZERO.compareTo(solicitud.NumeroCuotas) == 0) {
            params2.put("IdInstrumento", solicitud.IdSerieFondo);
            params2.put("DocumentoIdentidad", cliente.DocumentoIdentidad);
            params2.put("NombreCliente", cliente.NombreCompleto);
            out.P_OUT_MENSAJES.add(msg(solicitud.IdInterno, E, "NUMERO_CUOTA_VALIDO_REQ_APO_RES", params2, emp));
            params2.clear();
        }
        if (Constantes3B.SI.equals(reinversionDistintasFamilias)) {
            if (Constantes3B.REINVERSION.equalsIgnoreCase(solicitud.IdTipoMovimientoFondoMutuo)
                    && !Constantes3B.SI.equalsIgnoreCase(solicitud.EsFamiliaEquivalente)) {
                out.P_OUT_MENSAJES.add(msg(solicitud.IdInterno, E, "NO_ES_FAMILIA_EQUIVALENTE", params, emp));
            }
        }
        if (!Constantes3B.SI.equalsIgnoreCase(solicitud.EsPortafolioRegimenValido)) {
            out.P_OUT_MENSAJES.add(msg(solicitud.IdInterno, E, "PORTAFOLIO_REGIMEN_INVALIDO", params, emp));
        }
        if (!Constantes3B.SI.equalsIgnoreCase(solicitud.EsRegimenValido)) {
            params2.put("IdInstrumento", solicitud.IdSerieFondo);
            params2.put("DocumentoIdentidad", cliente.DocumentoIdentidad);
            out.P_OUT_MENSAJES.add(msg(solicitud.IdInterno, E, "REGIMEN_NO_EXISTE_REQ_APO_RES", params2, emp));
            params2.clear();
        }
        if (!Constantes3B.SI.equalsIgnoreCase(solicitud.PermiteRescateMandato)) {
            out.P_OUT_MENSAJES.add(msg(solicitud.IdInterno, E, "RESCATE_MANDATO_NO_PERMITIDO", params, emp));
        }
        if (!Constantes3B.SI.equalsIgnoreCase(solicitud.PermiteCambioGrupo)) {
            out.P_OUT_MENSAJES.add(msg(solicitud.IdInterno, E, "CAMBIO_GRUPO_REGIMEN_NO_PERMITIDO", params, emp));
        }
        if (!Constantes3B.SI.equalsIgnoreCase(solicitud.EsPortafolioValido)) {
            out.P_OUT_MENSAJES.add(msg(solicitud.IdInterno, E, "PORTAFOLIO_DESTINO_NO_COINCIDE_PROPOSITO", params, emp));
        }
        if (!Constantes3B.SI.equalsIgnoreCase(solicitud.EsCuentaInversionValido)) {
            out.P_OUT_MENSAJES.add(msg(solicitud.IdInterno, E, "PORTAFOLIO_NO_OPERA_CUENTA_INVERSION_FFMM", params, emp));
        }
        if (!Constantes3B.SI.equals(solicitud.PermiteAporte)) {
            out.P_OUT_MENSAJES.add(msg(solicitud.IdInterno, E, "REGIMEN_NO_PERMITE_APORTE", params, emp));
        }
        if (!Constantes3B.SI.equals(solicitud.PermiteTraspaso)) {
            out.P_OUT_MENSAJES.add(msg(solicitud.IdInterno, E, "REGIMEN_NO_PERMITE_TRASPASO", params, emp));
        }
        if (!Constantes3B.SI.equals(solicitud.PermiteRescate)) {
            out.P_OUT_MENSAJES.add(msg(solicitud.IdInterno, E, "REGIMEN_NO_PERMITE_RESCATE", params, emp));
        }
        if (!Constantes3B.SI.equals(solicitud.EsRegimenDestinoValido)) {
            out.P_OUT_MENSAJES.add(msg(solicitud.IdInterno, E, "COMBINACION_REGIMEN_REINVERSION_NO_CONFIGURADO", params, emp));
        }
    }
}
