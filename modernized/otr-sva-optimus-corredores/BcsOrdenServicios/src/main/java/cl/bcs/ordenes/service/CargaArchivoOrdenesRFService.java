package cl.bcs.ordenes.service;

import cl.bcs.ordenes.util.ConfiguracionSistemaUtil;
import cl.bcs.ordenes.util.ObtenerDiaSiguienteUtil;
import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Migrated from legacy cl.bcs.ordenes.CargaArchivoOrdenesRF (JavaCall).
 * Parses base64-encoded JSON file of RF orders, validates and creates orders
 * via CrearOrdenesService. Uses ObtenerDiaSiguienteUtil for vigencia computation.
 */
@Service
public class CargaArchivoOrdenesRFService {

    private static final Logger log = LoggerFactory.getLogger(CargaArchivoOrdenesRFService.class);
    private static final Gson GSON = new GsonBuilder()
        .setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();

    private final NamedParameterJdbcTemplate jdbc;
    private final ObtenerDiaSiguienteUtil diaSiguienteUtil;
    private final ConfiguracionSistemaUtil configUtil;
    private final CrearOrdenesService crearOrdenesService;

    private static final String SQL_SELECT_ID_PAIS =
        "SELECT \"IdPais\" FROM \"BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa\" WHERE \"IdEmpresa\" = :IdEmpresa";

    private final String SQL_INSERT_CARGA_ORDEN_RF;
    private final String SQL_SELECT_INSTRUMENTOS;
    private final String SQL_SELECT_PORTAFOLIOS;
    private final String SQL_SELECT_LIBRO_ORDENES;
    private final String SQL_SELECT_PARAMETRO_VIGENCIA;
    private final String SQL_SELECT_AUTORIZADOR;
    private final String SQL_SELECT_BANCO_LIQUIDADOR;
    private final String SQL_SELECT_PREFERENCIA_PAGO;
    private final String SQL_SELECT_CUENTA_CUSTODIO;

    public CargaArchivoOrdenesRFService(NamedParameterJdbcTemplate jdbc,
                                         ObtenerDiaSiguienteUtil diaSiguienteUtil,
                                         ConfiguracionSistemaUtil configUtil,
                                         CrearOrdenesService crearOrdenesService) {
        this.jdbc = jdbc;
        this.diaSiguienteUtil = diaSiguienteUtil;
        this.configUtil = configUtil;
        this.crearOrdenesService = crearOrdenesService;
        this.SQL_INSERT_CARGA_ORDEN_RF   = loadSql("cl/bcs/ordenes/sql/CargaArchivoOrdenesRF_InsertCargaOrdenRF.sql");
        this.SQL_SELECT_INSTRUMENTOS     = loadSql("cl/bcs/ordenes/sql/CargaArchivoOrdenesRF_SelectInstrumentos.sql");
        this.SQL_SELECT_PORTAFOLIOS      = loadSql("cl/bcs/ordenes/sql/CargaArchivoOrdenesRF_SelectPortafolios.sql");
        this.SQL_SELECT_LIBRO_ORDENES    = loadSql("cl/bcs/ordenes/sql/CargaArchivoOrdenesRF_SelectLibroOrdenes.sql");
        this.SQL_SELECT_PARAMETRO_VIGENCIA = loadSql("cl/bcs/ordenes/sql/CargaArchivoOrdenesRF_SelectParametroVigencia.sql");
        this.SQL_SELECT_AUTORIZADOR      = loadSql("cl/bcs/ordenes/sql/CargaArchivoOrdenesRF_SelectAutorizador.sql");
        this.SQL_SELECT_BANCO_LIQUIDADOR = loadSql("cl/bcs/ordenes/sql/CargaArchivoOrdenesRF_SelectBancoLiquidador.sql");
        this.SQL_SELECT_PREFERENCIA_PAGO = loadSql("cl/bcs/ordenes/sql/CargaArchivoOrdenesRF_SelectPreferenciaPago.sql");
        this.SQL_SELECT_CUENTA_CUSTODIO  = loadSql("cl/bcs/ordenes/sql/GloblalesCargaArchivo_SelectCuentaCustodio.sql");
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("CargaArchivoOrdenesRFService.execute()");
        try {
            Long idEmpresa   = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : null;
            Long idUsuario   = params.has("p_IdUsuario") ? params.get("p_IdUsuario").getAsLong() : null;
            String idModulo  = params.has("p_IdModulo") ? params.get("p_IdModulo").getAsString() : "ORD";
            String archivoB64 = params.has("p_Archivo") ? params.get("p_Archivo").getAsString() : "";

            if (archivoB64.isBlank()) {
                JsonObject out = new JsonObject();
                out.addProperty("p_out_ErroresContador", 1L);
                out.addProperty("p_out_NombreArchivoLog", "");
                out.addProperty("p_out_RutaArchivoLog", "");
                return out;
            }

            // Decode base64 → JSON
            String archivoDecoded = new String(Base64.getDecoder().decode(archivoB64), StandardCharsets.UTF_8);
            JsonObject archivoJson = JsonParser.parseString(archivoDecoded).getAsJsonObject();
            JsonArray registros = archivoJson.has("p_contenido") ? archivoJson.get("p_contenido").getAsJsonArray() : new JsonArray();

            // Get IdPais and system date
            Long idPais = 1L;
            try {
                List<Map<String, Object>> empresa = jdbc.queryForList(SQL_SELECT_ID_PAIS,
                    new MapSqlParameterSource("IdEmpresa", idEmpresa));
                if (!empresa.isEmpty()) idPais = toLong(empresa.get(0).get("IdPais"));
            } catch (Exception e) {
                log.warn("Could not get IdPais: {}", e.getMessage());
            }
            String fechaSistema = configUtil.getParametroString(idEmpresa, "PLA.FechaSistema");

            // Get default vigencia
            long vigenciaDefault = 30L;
            try {
                List<Map<String, Object>> vig = jdbc.queryForList(SQL_SELECT_PARAMETRO_VIGENCIA,
                    new MapSqlParameterSource("IdEmpresa", idEmpresa));
                if (!vig.isEmpty()) vigenciaDefault = toLong(vig.get(0).get("Valor"));
            } catch (Exception e) {
                log.warn("Could not get vigencia: {}", e.getMessage());
            }

            // Parse valid records and collect lookup keys
            List<Map<String, Object>> ordenes = new ArrayList<>();
            List<String> nemotecnicos = new ArrayList<>();
            List<String> documentosIdentidad = new ArrayList<>();
            List<String> documentosRepresentante = new ArrayList<>();
            List<String> codigosPortafolio = new ArrayList<>();
            List<String> bancosCustodio = new ArrayList<>();
            List<String> bancosLiquida = new ArrayList<>();
            List<String> mediosPago = new ArrayList<>();
            List<String> codigosLibro = new ArrayList<>();
            List<String> cuentasCodigo = new ArrayList<>();
            List<String> bovedas = new ArrayList<>();
            int totalRegistros = 0;
            int errores = 0;

            for (var el : registros) {
                totalRegistros++;
                JsonObject elem = el.getAsJsonObject();
                String validacion = elem.has("MENSAJEVALIDACION") ? elem.get("MENSAJEVALIDACION").getAsString() : "";
                if (!"OK".equalsIgnoreCase(validacion)) {
                    errores++;
                    log.warn("Registro con error: {} - {}", validacion, elem);
                    continue;
                }
                try {
                    Map<String, Object> orden = new HashMap<>();
                    orden.put("IdInterno", (long) ordenes.size());
                    orden.put("CodigoLibro", gs(elem, "CORF_CODIGOLIBRO"));
                    orden.put("RutCavali", gs(elem, "CORF_RUTCAVALI"));
                    String cpStr = gs(elem, "CORF_CODIGOPORTAFOLIO");
                    orden.put("CodigoPortafolio", cpStr.isBlank() ? "0" : cpStr);
                    String vigStr = gs(elem, "CORF_VIGENCIA");
                    orden.put("Vigencia", vigStr.isBlank() ? 0L : Long.parseLong(vigStr));
                    orden.put("CondicionLiquidacion", gs(elem, "CORF_CONDICIONLIQUIDACION"));
                    orden.put("MonedaLiquidacion", gs(elem, "CORF_MONEDALIQUIDACION"));
                    orden.put("TipoInstrumento", gs(elem, "CORF_TIPOINSTRUMENTO"));
                    orden.put("Bolsa", gs(elem, "CORF_BOLSA"));
                    orden.put("FormaNegociacion", gs(elem, "CORF_FORMANEGOCIACION"));
                    orden.put("CodigoProducto", gs(elem, "CORF_CODIGOPRODUCTO"));
                    orden.put("TipoOperacion", gs(elem, "CORF_TIPOOPERACION"));
                    orden.put("Instrumento", gs(elem, "CORF_INSTRUMENTO"));
                    orden.put("Cantidad", toBD(elem, "CORF_CANTIDAD"));
                    orden.put("Nominal", toBD(elem, "CORF_NOMINAL"));
                    orden.put("TipoPrecio", gs(elem, "CORF_TIPOPRECIO"));
                    orden.put("Precio", toBD(elem, "CORF_PRECIO"));
                    orden.put("Comision", toBD(elem, "CORF_COMISION"));
                    orden.put("FormaPago", gs(elem, "CORF_FORMAPAGO"));
                    orden.put("TipoCustodia", gs(elem, "CORF_TIPOCUSTODIA"));
                    orden.put("BancoCustodio", gs(elem, "CORF_BANCOCUSTODIO"));
                    orden.put("BancoLiquida", gs(elem, "CORF_BANCOLIQUIDA"));
                    orden.put("MedioCaptacion", gs(elem, "CORF_MEDIOCAPTACION"));
                    String rep = gs(elem, "CORF_REPRESENTANTE");
                    orden.put("Representante", "0".equals(rep) ? "" : rep);
                    orden.put("Boveda", gs(elem, "CORF_BOVEDA"));
                    orden.put("Cuenta", gs(elem, "CORF_CUENTA"));
                    orden.put("IdEstado", "PEN");
                    orden.put("Resultado", "");
                    ordenes.add(orden);

                    String instr = (String) orden.get("Instrumento");
                    String rut = (String) orden.get("RutCavali");
                    String cp = (String) orden.get("CodigoPortafolio");
                    String bc = (String) orden.get("BancoCustodio");
                    String bl = (String) orden.get("BancoLiquida");
                    String fp = (String) orden.get("FormaPago");
                    String cl2 = (String) orden.get("CodigoLibro");
                    String cv = (String) orden.get("Cuenta");
                    String bv = (String) orden.get("Boveda");
                    String repr = (String) orden.get("Representante");
                    if (!nemotecnicos.contains(instr)) nemotecnicos.add(instr);
                    if (!documentosIdentidad.contains(rut)) documentosIdentidad.add(rut);
                    if (!codigosPortafolio.contains(cp)) codigosPortafolio.add(cp);
                    if (!bc.isBlank() && !bancosCustodio.contains(bc)) bancosCustodio.add(bc);
                    if (!bl.isBlank() && !bancosLiquida.contains(bl)) bancosLiquida.add(bl);
                    if (!fp.isBlank() && !mediosPago.contains(fp)) mediosPago.add(fp);
                    if (!cl2.isBlank() && !codigosLibro.contains(cl2)) codigosLibro.add(cl2);
                    if (!cv.isBlank() && !cuentasCodigo.contains(cv)) cuentasCodigo.add(cv);
                    if (!bv.isBlank() && !bovedas.contains(bv)) bovedas.add(bv);
                    if (!repr.isBlank() && !documentosRepresentante.contains(repr)) documentosRepresentante.add(repr);
                } catch (Exception e) {
                    errores++;
                    log.warn("Error parsing registro: {}", e.getMessage());
                }
            }

            if (ordenes.isEmpty()) {
                JsonObject out = new JsonObject();
                out.addProperty("p_out_ErroresContador", (long) errores);
                out.addProperty("p_out_NombreArchivoLog", "");
                out.addProperty("p_out_RutaArchivoLog", "");
                return out;
            }

            // Bulk lookups
            List<Map<String, Object>> instrumentos = jdbc.queryForList(SQL_SELECT_INSTRUMENTOS,
                new MapSqlParameterSource("Nemotecnicos", nemotecnicos));
            Map<String, Map<String, Object>> hashInstr = new HashMap<>();
            for (Map<String, Object> i : instrumentos) hashInstr.put(str(i, "Nemotecnico"), i);

            List<Map<String, Object>> portafolios = jdbc.queryForList(SQL_SELECT_PORTAFOLIOS,
                new MapSqlParameterSource("DocumentoIdentidad", documentosIdentidad)
                    .addValue("CodigoPortafolios", codigosPortafolio));
            // hash by "rut|codigoPortafolio"
            Map<String, Long> hashPort = new HashMap<>();
            Map<Long, Long> hashPortCliente = new HashMap<>();
            List<Long> idsPortafolio = new ArrayList<>();
            for (Map<String, Object> p : portafolios) {
                Long idPort = toLong(p.get("IdPortafolio"));
                String key = str(p, "DocumentoIdentidad") + "|" + str(p, "CodigoPortafolio");
                hashPort.put(key, idPort);
                hashPortCliente.put(idPort, toLong(p.get("IdCliente")));
                if (!idsPortafolio.contains(idPort)) idsPortafolio.add(idPort);
            }

            // Libros, autorizadores, cuentas custodio, preferencia pago
            Map<String, Long> hashLibro = new HashMap<>();
            Map<String, String> hashLibroTipo = new HashMap<>();
            Map<String, Long> hashRepresentante = new HashMap<>();
            Map<String, Long> hashCuentaCustodio = new HashMap<>();
            Map<String, Long> hashPreferencia = new HashMap<>();

            if (!idsPortafolio.isEmpty()) {
                List<String> codigosLibroActual = codigosLibro.isEmpty() ? List.of("__NONE__") : codigosLibro;
                List<Map<String, Object>> libros = jdbc.queryForList(SQL_SELECT_LIBRO_ORDENES,
                    new MapSqlParameterSource("NombreLibroOrdenes", codigosLibroActual)
                        .addValue("IdPortafolio", idsPortafolio));
                for (Map<String, Object> l : libros) {
                    String k = str(l, "NombreLibroOrdenes") + "|" + toLong(l.get("IdPortafolio"));
                    hashLibro.put(k, toLong(l.get("IdLibroOrdenes")));
                    hashLibroTipo.put(k, str(l, "IdTipoLibroOrden"));
                }

                List<String> docsRepr = documentosRepresentante.isEmpty() ? List.of("__NONE__") : documentosRepresentante;
                List<Map<String, Object>> representantes = jdbc.queryForList(SQL_SELECT_AUTORIZADOR,
                    new MapSqlParameterSource("DocumentoIdentidad", docsRepr)
                        .addValue("IdPortafolio", idsPortafolio));
                for (Map<String, Object> r : representantes) {
                    String k = str(r, "DocumentoIdentidad") + "|" + toLong(r.get("IdPortafolio"));
                    hashRepresentante.put(k, toLong(r.get("IdPersona")));
                }

                List<String> bcs2 = bancosCustodio.isEmpty() ? List.of("__NONE__") : bancosCustodio;
                List<String> bls2 = bancosLiquida.isEmpty() ? List.of("__NONE__") : bancosLiquida;
                List<Map<String, Object>> cuentasBanco = jdbc.queryForList(SQL_SELECT_BANCO_LIQUIDADOR,
                    new MapSqlParameterSource("BancoCustodio", bcs2)
                        .addValue("BancoLiquida", bls2)
                        .addValue("IdPortafolio", idsPortafolio));
                for (Map<String, Object> c : cuentasBanco) {
                    String k = str(c, "CodigoBancoCustodio") + "|" + str(c, "CodigoBancoLiquidador") + "|" + toLong(c.get("IdPortafolio"));
                    hashCuentaCustodio.put(k, toLong(c.get("IdCuentaCustodioPortafolio")));
                }

                List<String> mps = mediosPago.isEmpty() ? List.of("__NONE__") : mediosPago;
                List<Map<String, Object>> preferencias = jdbc.queryForList(SQL_SELECT_PREFERENCIA_PAGO,
                    new MapSqlParameterSource("IdMedioDePago", mps)
                        .addValue("IdPortafolio", idsPortafolio));
                for (Map<String, Object> p : preferencias) {
                    String k = str(p, "IdMedioDePago") + "|" + str(p, "IdTipoOperacion") + "|" + toLong(p.get("IdPortafolio"));
                    hashPreferencia.put(k, toLong(p.get("IdPreferenciaPagoPortafolio")));
                }

                List<String> bvs = bovedas.isEmpty() ? List.of("__NONE__") : bovedas;
                List<String> cvs = cuentasCodigo.isEmpty() ? List.of("__NONE__") : cuentasCodigo;
                List<Map<String, Object>> cuentasCustodioP = jdbc.queryForList(SQL_SELECT_CUENTA_CUSTODIO,
                    new MapSqlParameterSource("IdBoveda", bvs)
                        .addValue("Codigo", cvs)
                        .addValue("IdPortafolio", idsPortafolio));
                for (Map<String, Object> c : cuentasCustodioP) {
                    String k = str(c, "Codigo") + "|" + str(c, "IdBoveda") + "|" + toLong(c.get("IdPortafolio"));
                    hashCuentaCustodio.put(k, toLong(c.get("IdCuentaCustodioPortafolio")));
                }
            }

            // Map each order to CrearOrdenes params
            JsonArray ordenesEntrada = new JsonArray();
            List<Map<String, Object>> resultados = new ArrayList<>();

            for (Map<String, Object> ord : ordenes) {
                Long interno = toLong(ord.get("IdInterno"));
                try {
                    String rutCavali = str(ord, "RutCavali");
                    String codigoPortafolio = str(ord, "CodigoPortafolio");
                    Long idPortafolio = hashPort.get(rutCavali + "|" + codigoPortafolio);
                    if (idPortafolio == null)
                        throw new Exception("Portafolio no existe para cliente RUT Cavali: " + rutCavali + ", CodigoPortafolio: " + codigoPortafolio);

                    // Vigencia
                    long vigOrden = toLong(ord.get("Vigencia"));
                    long diasVig = vigOrden == 0 ? vigenciaDefault : vigOrden;
                    String fechaVigencia = fechaSistema;
                    try {
                        ObtenerDiaSiguienteUtil.FechaIn fi = new ObtenerDiaSiguienteUtil.FechaIn();
                        fi.fechaInicial = fechaSistema;
                        fi.numeroDias = diasVig;
                        fi.pais = idPais;
                        fi.IdInterno = interno;
                        ObtenerDiaSiguienteUtil.Salida sal = diaSiguienteUtil.calcular(idPais, "S", "N", List.of(fi));
                        if (!sal.p_out_fechas.isEmpty()) fechaVigencia = sal.p_out_fechas.get(0).fechaCalculada;
                    } catch (Exception e) {
                        log.warn("ObtenerDiaSiguiente error for orden {}: {}", interno, e.getMessage());
                    }

                    // Instrument lookup
                    String nemotecnico = str(ord, "Instrumento");
                    Map<String, Object> instr = hashInstr.get(nemotecnico);
                    if (instr == null) throw new Exception("Instrumento: " + nemotecnico + ", no existe.");
                    String idClasif = str(instr, "IdClasificacionTipoInstrumento");
                    if (!"RF".equalsIgnoreCase(idClasif)) throw new Exception("Instrumento: " + nemotecnico + ", no es RF.");
                    Long idInstrumento = toLong(instr.get("IdInstrumento"));
                    String idMonedaInstr = str(instr, "IdMoneda");

                    String condLiq = str(ord, "CondicionLiquidacion");
                    String monedaLiq = str(ord, "MonedaLiquidacion");
                    if (monedaLiq.isBlank()) monedaLiq = idMonedaInstr;
                    String tipoOp = "C".equalsIgnoreCase(str(ord, "TipoOperacion")) ? "COMRF" : "VENRF";
                    String bolsa = str(ord, "Bolsa").isBlank() ? "BVL" : str(ord, "Bolsa");
                    String tipoPrecio = str(ord, "TipoPrecio");
                    BigDecimal precio = (BigDecimal) ord.get("Precio");
                    if ("L".equalsIgnoreCase(tipoPrecio) && precio.compareTo(BigDecimal.ZERO) == 0)
                        throw new Exception("Precio es 0 para operacion Precio Limite, cliente: " + rutCavali);

                    // Libro
                    Long idLibroOrdenes = 0L;
                    String idTipoLibro = "NORMAL";
                    String codigoLibro = str(ord, "CodigoLibro");
                    if (!codigoLibro.isBlank()) {
                        String lk = codigoLibro + "|" + idPortafolio;
                        idLibroOrdenes = hashLibro.getOrDefault(lk, 0L);
                        idTipoLibro = hashLibroTipo.getOrDefault(lk, "NORMAL");
                    }

                    // Cuenta custodio from boveda/cuenta match
                    String cuenta = str(ord, "Cuenta");
                    String boveda = str(ord, "Boveda");
                    Long idCuentaCustodio = hashCuentaCustodio.get(cuenta + "|" + boveda + "|" + idPortafolio);

                    // Override with banco if specified
                    String bancoCustodio = str(ord, "BancoCustodio");
                    String bancoLiquida = str(ord, "BancoLiquida");
                    if (!bancoCustodio.isBlank() || !bancoLiquida.isBlank()) {
                        Long bcKey = hashCuentaCustodio.get(bancoCustodio + "|" + bancoLiquida + "|" + idPortafolio);
                        if (bcKey != null) idCuentaCustodio = bcKey;
                    }
                    if (idCuentaCustodio == null)
                        throw new Exception("Cuenta custodio no encontrada para cliente: " + rutCavali);

                    // Pago cobro cuenta inversion
                    String formaPago = str(ord, "FormaPago");
                    String pagoCobro = "S";
                    Long idPreferencia = null;
                    if (!formaPago.isBlank()) {
                        pagoCobro = "N";
                        idPreferencia = hashPreferencia.get(formaPago + "|" + tipoOp + "|" + idPortafolio);
                        if (idPreferencia == null)
                            throw new Exception("Preferencia pago: " + formaPago + " no existe para: " + rutCavali);
                    }

                    // Representante
                    Long idPersonaAutorizado = null;
                    String representante = str(ord, "Representante");
                    if (!representante.isBlank()) {
                        idPersonaAutorizado = hashRepresentante.get(representante + "|" + idPortafolio);
                        if (idPersonaAutorizado == null)
                            throw new Exception("Representante: " + representante + " no existe para: " + rutCavali);
                    }

                    // Build order entry JSON
                    JsonObject o = new JsonObject();
                    o.addProperty("IdInterno", interno);
                    o.addProperty("IdPortafolio", idPortafolio);
                    o.addProperty("IdLibroOrdenes", idLibroOrdenes);
                    o.addProperty("IdTipoLibroOrden", idTipoLibro);
                    o.addProperty("IdTipoVigencia", "FF");
                    o.addProperty("FechaVigencia", fechaVigencia);
                    o.addProperty("FechaEmision", fechaSistema);
                    o.addProperty("FechaRecepcion", fechaSistema);
                    o.addProperty("FechaOperacion", fechaSistema);
                    if (!condLiq.isBlank()) o.addProperty("IdCondicionDeLiquidacion", condLiq);
                    o.addProperty("IdMonedaLiquidacionOrden", monedaLiq);
                    o.addProperty("IdBolsa", bolsa);
                    o.addProperty("IdSistemaBursatil", str(ord, "FormaNegociacion"));
                    o.addProperty("IdTipoOperacion", tipoOp);
                    o.addProperty("IdInstrumento", idInstrumento);
                    o.addProperty("Cantidad", (BigDecimal) ord.get("Cantidad"));
                    o.addProperty("MontoNeto", BigDecimal.ZERO);
                    o.addProperty("ValorRescate", 0);
                    o.addProperty("IdTipoPrecio", tipoPrecio);
                    o.addProperty("PrecioLimite", precio);
                    o.addProperty("IdFormaCustodia", "C".equalsIgnoreCase(str(ord, "TipoCustodia")) ? "CUS" : "NOM");
                    o.addProperty("IdCuentaCustodioPortafolio", idCuentaCustodio);
                    o.addProperty("IdMecanismoCaptacion", str(ord, "MedioCaptacion"));
                    o.addProperty("PagoCobroCuentaInversion", pagoCobro);
                    if (idPreferencia != null) o.addProperty("IdPreferenciaPagoPortafolio", idPreferencia);
                    if (idPersonaAutorizado != null) o.addProperty("IdPersonaAutorizadoDarOrdenes", idPersonaAutorizado);
                    o.addProperty("IdBoveda", boveda);
                    o.addProperty("PorcentajeAllIn", (BigDecimal) ord.get("Comision"));
                    o.addProperty("IdTipoCanalIngreso", "AGE");
                    o.addProperty("EspecificaBolsa", "S");
                    o.addProperty("IdTipoCanalDistribucion", "MAN");
                    o.addProperty("IdPorcentajeVisibilidad", 1L);
                    o.addProperty("IdInstrumentoSubyacente", 0L);
                    o.addProperty("BaseTasa", 0L);
                    o.addProperty("TipoInteres", "");
                    o.addProperty("IdMonedaInstrumento", idMonedaInstr);
                    o.addProperty("EsFacturaInmediata", "N");
                    o.addProperty("EsVentaCorta", "N");
                    o.addProperty("EsOD", "N");
                    o.addProperty("EsFungible", "N");
                    o.addProperty("TasaReporte", 0);
                    o.addProperty("TasaSimultanea", 0);
                    o.addProperty("TasaRFIF", 0);
                    o.addProperty("PlazoIF", 0L);
                    o.addProperty("PlazoSimultanea", 0L);
                    o.addProperty("PlazoVentaCorta", 0L);
                    o.addProperty("TasaVentaCorta", 0);
                    o.addProperty("TasaPromedioReporte", 0);
                    o.addProperty("TasaReportanteReferencia", 0);
                    o.addProperty("PlazoReporte", 0L);
                    o.addProperty("IdCondicionDeLiquidacionPlazo", "TP");
                    o.addProperty("PagoCobroCuentaInversion", pagoCobro);
                    o.addProperty("IdPreferenciaPagoPortafolio", 0L);
                    o.addProperty("TipoInteres", "C");
                    ordenesEntrada.add(o);
                    ord.put("IdEstado", "OK");
                    ord.put("Resultado", "OK");
                } catch (Exception e) {
                    errores++;
                    ord.put("IdEstado", "ERR");
                    ord.put("Resultado", e.getMessage());
                    log.warn("Orden {} error: {}", interno, e.getMessage());
                }
            }

            // Call CrearOrdenesService
            if (ordenesEntrada.size() > 0) {
                try {
                    JsonObject crearParams = new JsonObject();
                    crearParams.addProperty("p_IdEmpresa", idEmpresa);
                    crearParams.addProperty("p_IdUsuario", idUsuario);
                    crearParams.addProperty("p_IdModulo", idModulo);
                    crearParams.addProperty("p_simularOperacion", "N");
                    crearParams.addProperty("p_UsaReglasNegocio", "S");
                    crearParams.add("p_Ordenes", ordenesEntrada);
                    crearParams.add("p_Garantias", new JsonArray());
                    crearParams.add("p_CobrosModificados", new JsonArray());
                    JsonObject crearResult = crearOrdenesService.execute(crearParams);

                    if (crearResult.has("P_OUT_RESULTADO")) {
                        for (var r : crearResult.get("P_OUT_RESULTADO").getAsJsonArray()) {
                            Long idInterno = r.getAsJsonObject().has("IdInterno") ? r.getAsJsonObject().get("IdInterno").getAsLong() : null;
                            if (idInterno != null) {
                                for (Map<String, Object> ord : ordenes) {
                                    if (idInterno.equals(toLong(ord.get("IdInterno")))) {
                                        ord.put("IdEstado", "OK");
                                        ord.put("Resultado", "OK");
                                    }
                                }
                            }
                        }
                    }
                    if (crearResult.has("P_OUT_MENSAJES")) {
                        for (var m : crearResult.get("P_OUT_MENSAJES").getAsJsonArray()) {
                            JsonObject msg = m.getAsJsonObject();
                            if ("ERROR".equalsIgnoreCase(str(msg, "tipo"))) {
                                Long idInterno = msg.has("id") ? msg.get("id").getAsLong() : null;
                                if (idInterno != null) {
                                    for (Map<String, Object> ord : ordenes) {
                                        if (idInterno.equals(toLong(ord.get("IdInterno"))) && "PEN".equalsIgnoreCase(str(ord, "IdEstado"))) {
                                            ord.put("IdEstado", "ERR");
                                            ord.put("Resultado", str(msg, "descripcion"));
                                            errores++;
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("Error calling CrearOrdenesService: {}", e.getMessage());
                    for (Map<String, Object> ord : ordenes) {
                        if ("PEN".equalsIgnoreCase(str(ord, "IdEstado"))) {
                            ord.put("IdEstado", "ERR");
                            ord.put("Resultado", "Error servicio Ordenes");
                            errores++;
                        }
                    }
                }
            }

            // Mark remaining PEN as ERR and insert log records
            for (Map<String, Object> ord : ordenes) {
                if ("PEN".equalsIgnoreCase(str(ord, "IdEstado"))) {
                    ord.put("IdEstado", "ERR");
                    errores++;
                }
                try {
                    jdbc.update(SQL_INSERT_CARGA_ORDEN_RF,
                        new MapSqlParameterSource()
                            .addValue("CodigoLibro", ord.get("CodigoLibro"))
                            .addValue("RutCavali", ord.get("RutCavali"))
                            .addValue("CodigoPortafolio", ord.get("CodigoPortafolio"))
                            .addValue("Vigencia", ord.get("Vigencia"))
                            .addValue("CondicionLiquidacion", ord.get("CondicionLiquidacion"))
                            .addValue("MonedaLiquidacion", ord.get("MonedaLiquidacion"))
                            .addValue("TipoInstrumento", ord.get("TipoInstrumento"))
                            .addValue("Bolsa", ord.get("Bolsa"))
                            .addValue("FormaNegociacion", ord.get("FormaNegociacion"))
                            .addValue("CodigoProducto", ord.get("CodigoProducto"))
                            .addValue("TipoOperacion", ord.get("TipoOperacion"))
                            .addValue("Instrumento", ord.get("Instrumento"))
                            .addValue("Cantidad", ord.get("Cantidad"))
                            .addValue("Nominal", ord.get("Nominal"))
                            .addValue("TipoPrecio", ord.get("TipoPrecio"))
                            .addValue("Precio", ord.get("Precio"))
                            .addValue("Comision", ord.get("Comision"))
                            .addValue("FormaPago", ord.get("FormaPago"))
                            .addValue("TipoCustodia", ord.get("TipoCustodia"))
                            .addValue("BancoCustodio", ord.get("BancoCustodio"))
                            .addValue("BancoLiquida", ord.get("BancoLiquida"))
                            .addValue("MedioCaptacion", ord.get("MedioCaptacion"))
                            .addValue("Representante", ord.get("Representante"))
                            .addValue("Boveda", ord.get("Boveda"))
                            .addValue("Cuenta", ord.get("Cuenta"))
                            .addValue("IdEstado", ord.get("IdEstado"))
                            .addValue("Resultado", ord.get("Resultado")));
                } catch (Exception e) {
                    log.warn("Error inserting carga orden RF log: {}", e.getMessage());
                }
            }

            JsonObject out = new JsonObject();
            out.addProperty("p_out_ErroresContador", (long) errores);
            out.addProperty("p_out_NombreArchivoLog", "");
            out.addProperty("p_out_RutaArchivoLog", "");
            return out;
        } catch (Exception e) {
            log.error("CargaArchivoOrdenesRFService error: {}", e.getMessage(), e);
            JsonObject err = new JsonObject();
            err.addProperty("p_out_ErroresContador", 1L);
            err.addProperty("error", e.getMessage());
            return err;
        }
    }

    private static String gs(JsonObject o, String key) {
        return o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsString() : "";
    }

    private static BigDecimal toBD(JsonObject o, String key) {
        if (!o.has(key) || o.get(key).isJsonNull()) return BigDecimal.ZERO;
        String v = o.get(key).getAsString();
        if (v.isBlank()) return BigDecimal.ZERO;
        try { return new BigDecimal(v); } catch (Exception e) { return BigDecimal.ZERO; }
    }

    private static String str(Map<String, Object> m, String key) {
        Object v = m == null ? null : m.get(key);
        return v != null ? v.toString() : "";
    }

    private static String str(JsonObject o, String key) {
        return o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsString() : "";
    }

    private static Long toLong(Object v) {
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).longValue();
        try { return Long.parseLong(v.toString()); } catch (Exception e) { return null; }
    }

    private static String loadSql(String path) {
        try (var is = CargaArchivoOrdenesRFService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
