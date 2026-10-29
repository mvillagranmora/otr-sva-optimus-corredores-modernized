package cl.bcs.custodia.flujo;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.LongStream;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.flujo.FlujoCustodiaModelo.AgrupadoPor;
import cl.bcs.custodia.flujo.FlujoCustodiaModelo.ConfiguracionUso;
import cl.bcs.custodia.flujo.FlujoCustodiaModelo.DatosFlujo;
import cl.bcs.custodia.flujo.FlujoCustodiaModelo.Entrada;
import cl.bcs.custodia.flujo.FlujoCustodiaModelo.FiltroBusqueda;
import cl.bcs.custodia.flujo.FlujoCustodiaModelo.MatrizUso;
import cl.bcs.custodia.flujo.FlujoCustodiaModelo.PortafolioCuentaPropia;
import cl.bcs.custodia.flujo.FlujoCustodiaModelo.Salida;
import cl.bcs.custodia.flujo.FlujoCustodiaModelo.TipoInstrumentoParaGarantia;
import cl.bcs.custodia.flujo.jdbc.SqlNombrado;
import cl.bcs.custodia.flujo.jdbc.SqlRecursos;
import cl.bcs.custodia.flujo.kit.DiaSiguiente;
import cl.bcs.custodia.flujo.kit.FechaLegacy;
import cl.bcs.custodia.flujo.kit.InformacionEmpresa;
import cl.bcs.custodia.flujo.kit.JsonFlujo;
import cl.bcs.custodia.flujo.kit.MensajeSalida;
import cl.bcs.custodia.flujo.kit.PlataformaLecturas;

/**
 * ConsultarFlujoCustodia (CUS-FLU-01..36): the custody flow of a company on a date and its projection over the following days, totalised by the
 * requested grouping and optionally valued. Reads the usage configuration and the sources of custody (SELECT only), keeps the winners of the usage
 * matrix in {@code CUS.FlujoCustodia_MatrizUso} under a random work id while it works (the only write; removed in {@code finally}, also after an error:
 * CUS-FLU-14, D-CUS-09) and never commits.
 *
 * <p>The two collaborators outside the slice come in through the constructor: ValorizarInstrumento (request JSON in, answer JSON out) and the
 * sequence service ({@code Utils.getSecuencia}: name and quantity in, that many values out). The connection belongs to the caller.
 *
 * <p>Defects of the legacy are kept bug for bug (D-CUS-01..10); the grouping is sequential (D-CUS-06). Where the legacy dereferenced a null, so does
 * this class: a NullPointerException is the answer to such a request.
 */
public class ConsultarFlujoCustodiaFlujo {

    private static final String SEC_FLUJO = "BSCL.Custodia.BD.Secuencias::FlujoCustodia";
    private static final String SEC_DATOS = "BSCL.Custodia.BD.Secuencias::DatosFlujoCustodia";

    private static final String SQL_PORTAFOLIOS_CUENTA_PROPIA = SqlRecursos.modulo("FlujoCustodia_SelectPortafoliosCuentaPropia.sql");
    private static final String SQL_TIPO_INSTRUMENTO_PARA_GARANTIA = SqlRecursos.modulo("FlujoCustodia_SelectTipoInstrumentoParaGarantia.sql");
    private static final String SQL_USO_FLUJO = SqlRecursos.modulo("FlujoCustodia_SelectUsoFlujoCustodia.sql");
    private static final String SQL_ITEMS_FLUJO = SqlRecursos.modulo("FlujoCustodia_SelectItemsFlujoCustodia.sql");
    private static final String SQL_INSTRUMENTOS_FLUJO = SqlRecursos.modulo("FlujoCustodia_SelectInstrumentosFlujo.sql");
    private static final String SQL_INSERT_MATRIZ_USO = SqlRecursos.modulo("FlujoCustodia_InsertFlujoCustodia_MatrizUso.sql");
    private static final String SQL_DELETE_MATRIZ_USO = SqlRecursos.modulo("FlujoCustodia_DeleteFlujoCustodia_MatrizUso.sql");

    private final Function<JsonObject, JsonObject> valorizador;
    private final BiFunction<String, Long, List<Long>> secuencias;

    public ConsultarFlujoCustodiaFlujo(Function<JsonObject, JsonObject> valorizador, BiFunction<String, Long, List<Long>> secuencias) {
        this.valorizador = valorizador;
        this.secuencias = secuencias;
    }

    /** The JavaCall entry point: JSON in, JSON out; {@code P_OUT_ERRORESCONTADOR} is recomputed from the messages (CUS-FLU-01). */
    public JsonObject call(Connection con, JsonObject jin) throws Exception {
        Gson gson = JsonFlujo.legacy();
        Entrada in = gson.fromJson(jin.toString(), Entrada.class);
        Salida out = logica(con, in);
        out.P_OUT_ERRORESCONTADOR = numeroErrores(out.P_OUT_MENSAJES);
        return JsonParser.parseString(gson.toJson(out)).getAsJsonObject();
    }

    private static long numeroErrores(List<MensajeSalida> mensajes) {
        long errores = 0;
        for (MensajeSalida m : mensajes) {
            if (MensajeSalida.MENSAJE_ERROR.equals(m.tipo)) {
                errores++;
            }
        }
        return errores;
    }

    Salida logica(Connection con, Entrada in) throws Exception {
        Throwable fallo = null;
        Salida out = new Salida();
        long idTrabajo = (long) (Math.random() * 10000000000L);   // D-CUS-09: random, two concurrent calls can collide
        List<MatrizUso> matrizUso = new ArrayList<>();
        try {
            if (in.p_Uso.size() == 0) {
                out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "SIN_INFORMAR_USO_FLUJO_CUSTODIA",
                        PlataformaLecturas.mensaje(con, in.p_IdEmpresa, "SIN_INFORMAR_USO_FLUJO_CUSTODIA", null)));
                return out;
            }

            // CUS-FLU-05: system date and the switch of the underlying read
            String fechaSistema = PlataformaLecturas.parametro(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
            String agregarSimultaneas = PlataformaLecturas.parametro(con, in.p_IdEmpresa, "CUSTODIA", "CUS.AgregarSimultaneasEventoCapitalCli");
            java.sql.Date dateFechaSistema = FechaLegacy.aFechaSql(fechaSistema);
            java.sql.Timestamp fechaEjecucion = FechaLegacy.aTimestamp(in.p_FechaSaldo + " 00:00:00");

            // CUS-FLU-04: p_IdPais -1 is the country of the company
            InformacionEmpresa empresa = PlataformaLecturas.empresa(con, in.p_IdEmpresa);
            Long idPaisEmpresa = in.p_IdPais == -1 ? empresa.IdPais : in.p_IdPais;

            List<FiltroBusqueda> filtros = in.p_FiltrosBusqueda == null || in.p_FiltrosBusqueda.size() == 0 ? new ArrayList<>() : in.p_FiltrosBusqueda;
            List<AgrupadoPor> agrupaciones = in.p_EsAgrupadoPor == null || in.p_EsAgrupadoPor.size() == 0 ? new ArrayList<>() : in.p_EsAgrupadoPor;

            // CUS-FLU-36: the key of the flow
            out.p_out_KeyFlujoCustodia = secuencias.apply(SEC_FLUJO, 1L).get(0);

            // CUS-FLU-06: the projected dates
            DiaSiguiente.Entrada diaSiguiente = new DiaSiguiente.Entrada();
            diaSiguiente.p_idPais = idPaisEmpresa;
            diaSiguiente.p_diaHabil = in.p_DiaHabil;
            for (int i = 1; i <= in.p_DiasProyeccion; i++) {
                DiaSiguiente.FechaEntrada f = new DiaSiguiente.FechaEntrada();
                f.fechaInicial = in.p_FechaSaldo;
                f.numeroDias = Long.valueOf(i);
                diaSiguiente.p_fechas.add(f);
            }
            DiaSiguiente.Salida fechas = DiaSiguiente.logica(con, diaSiguiente);
            Map<String, DiaSiguiente.FechaSalida> fechasPorCalculo = new HashMap<>();
            for (DiaSiguiente.FechaSalida f : fechas.p_out_fechas) {
                if (fechasPorCalculo.containsKey(f.fechaCalculada)) {
                    throw new Exception("key field is not unique");
                }
                fechasPorCalculo.put(f.fechaCalculada, f);
            }
            Map<String, String> vistas = new HashMap<>();
            vistas.put(in.p_FechaSaldo, in.p_FechaSaldo);
            List<String> fechasCalculadas = new ArrayList<>();
            fechasCalculadas.add(in.p_FechaSaldo);
            for (DiaSiguiente.FechaSalida f : fechas.p_out_fechas) {
                if (!vistas.containsKey(f.fechaCalculada)) {
                    vistas.put(f.fechaCalculada, f.fechaCalculada);
                    fechasCalculadas.add(f.fechaCalculada);
                }
            }
            String fechaMinima = Collections.min(fechasCalculadas);
            String fechaMaxima = Collections.max(fechasCalculadas);
            java.sql.Date fechaMinimaDate = FechaLegacy.aFechaSql(fechaMinima);

            // configuration read by the flow
            Map<String, Object> params = new HashMap<>();
            params.put("IdEmpresa", empresa.IdEmpresa);
            Set<Long> portafoliosCuentaPropia = new HashSet<>();
            for (PortafolioCuentaPropia p : SqlNombrado.consultar(con, SQL_PORTAFOLIOS_CUENTA_PROPIA, params, PortafolioCuentaPropia.class)) {
                portafoliosCuentaPropia.add(p.IdPortafolio);
            }
            Map<String, TipoInstrumentoParaGarantia> tiposParaGarantia = new HashMap<>();
            for (TipoInstrumentoParaGarantia t : SqlNombrado.consultar(con, SQL_TIPO_INSTRUMENTO_PARA_GARANTIA, new HashMap<>(),
                    TipoInstrumentoParaGarantia.class)) {
                tiposParaGarantia.put(t.IdTipoInstrumentoAval + "#" + t.IdTipoInstrumentoGarantizado, t);
            }
            Map<String, Object> paramsUso = new HashMap<>();
            paramsUso.put("IdEmpresa", in.p_IdEmpresa);
            List<ConfiguracionUso> usosConfigurados = SqlNombrado.consultar(con, SQL_USO_FLUJO, paramsUso, ConfiguracionUso.class);
            List<ConfiguracionUso> itemsConfigurados = SqlNombrado.consultar(con, SQL_ITEMS_FLUJO, paramsUso, ConfiguracionUso.class);

            // CUS-FLU-08..13: the usage matrix and its base table
            MatrizDeUso matriz = MatrizDeUso.construir(in.p_Uso, filtros, usosConfigurados, itemsConfigurados, empresa.IdEmpresa, idTrabajo);
            matrizUso.addAll(matriz.ganadores);

            List<DatosFlujo> lista = new ArrayList<>();
            List<Long> idsMatriz = LongStream.rangeClosed(0, matriz.tablaBase.size()).boxed().collect(java.util.stream.Collectors.toList());
            java.util.Iterator<Long> idMatriz = idsMatriz.iterator();
            for (MatrizUso tb : matriz.tablaBase) {
                DatosFlujo dfc = filaDeMatriz(tb);
                dfc.IdDatosFlujoCustodia = idMatriz.next();
                dfc.KeyFlujoCustodia = out.p_out_KeyFlujoCustodia;
                dfc.FechaEjecucion = FechaLegacy.aTimestamp(in.p_FechaSaldo + " 00:00:00");
                dfc.TipoRegistro = "M";
                dfc.ValorizarFlujo = in.p_ValorizarFlujo;
                lista.add(dfc);
            }

            // CUS-FLU-14: the winners are written while the flow is built
            SqlNombrado.lote(con, SQL_INSERT_MATRIZ_USO, matrizUso, MatrizUso.class);

            // CUS-FLU-15..23: the sources
            java.sql.Date dateFechaMaxima = FechaLegacy.aFechaSql(fechaMaxima);
            String simultaneasEvc = matriz.clavesGanadores.stream().anyMatch(clave -> clave.contains("FLUJO_CLIENTES_DERECHO_CAPITAL")) ? "S" : "N";
            FuentesFlujo fuentes = new FuentesFlujo(con, new FuentesFlujo.Contexto(in.p_IdEmpresa, idTrabajo, dateFechaMaxima, fechaMinimaDate,
                    dateFechaSistema, in.p_FechaSaldo, agregarSimultaneas, simultaneasEvc, matrizUso, matriz.origenPorItem));
            for (String fuente : matriz.fuentes) {
                lista.addAll(fuentes.leer(fuente));
            }

            // CUS-FLU-24..26: process values, discards
            List<String> instrumentos = ProcesamientoFlujo.marcar(lista, new ProcesamientoFlujo.Parametros(fechaEjecucion, out.p_out_KeyFlujoCustodia,
                    fechaMinimaDate, in.p_ValorizarFlujo, portafoliosCuentaPropia, tiposParaGarantia, matriz.itemsPorUsoYCalculo));
            Map<String, Object> paramsInstrumentos = new HashMap<>();
            paramsInstrumentos.put("Instrumentos", instrumentos);
            List<DatosFlujo> subyacentes = SqlNombrado.consultar(con, SQL_INSTRUMENTOS_FLUJO, paramsInstrumentos, DatosFlujo.class);
            ProcesamientoFlujo.descartarNoSubyacentes(lista, subyacentes);

            // CUS-FLU-27/28: detail and projection
            List<Long> idsProyeccion = secuencias.apply(SEC_DATOS, Long.valueOf(lista.size() * fechasCalculadas.size()));
            lista.addAll(ProcesamientoFlujo.proyectar(lista, fechasCalculadas, idsProyeccion.iterator(), out.p_out_FlujoDetallado));

            // CUS-FLU-29..32: grouping and totals
            AgrupacionFlujo.Union union = AgrupacionFlujo.unir(matriz.tablaBase, lista, fechasCalculadas);
            out.p_out_FlujoTotalizado.addAll(AgrupacionFlujo.totalizar(union, agrupaciones, fechasPorCalculo));

            // CUS-FLU-33..35: valuation
            if (in.p_ValorizarFlujo.equals("S")) {
                ValorizacionFlujo.valorizar(valorizador, in, out, union.instrumentos(), matriz.usosConfigurados);
            }
        } catch (Exception | Error e) {
            fallo = e;
            throw e;
        } finally {
            limpiarMatrizUso(con, matrizUso, fallo);
        }
        return out;
    }

    /**
     * CUS-FLU-14: removes the winners of this call from the work table, also after an error. A failure of the cleanup never hides the real cause: it is
     * attached to it as suppressed. The caller owns the transaction and must roll back (this slice never commits).
     */
    private static void limpiarMatrizUso(Connection con, List<MatrizUso> matrizUso, Throwable fallo) throws Exception {
        try {
            SqlNombrado.lote(con, SQL_DELETE_MATRIZ_USO, matrizUso, MatrizUso.class);
        } catch (Exception d) {
            if (fallo == null) {
                throw d;
            }
            fallo.addSuppressed(d);
        }
    }

    /** A base row as a matrix row of the flow (type M): the dimensions of the winner and its item. */
    private static DatosFlujo filaDeMatriz(MatrizUso tb) {
        DatosFlujo dfc = new DatosFlujo();
        dfc.IdUsoFlujoCustodia = tb.IdUsoFlujoCustodia;
        dfc.UsoTipoOperacion = tb.UsoTipoOperacion;
        dfc.BusquedaEspecifica = tb.BusquedaEspecifica;
        dfc.ItemCalculo = tb.ItemCalculo;
        dfc.ItemOrigen = tb.ItemOrigen;
        dfc.FuncionOperador = tb.FuncionOperador;
        dfc.IdCliente = tb.IdCliente;
        dfc.IdPortafolio = tb.IdPortafolio;
        dfc.IdClasificacionTipoInstrumento = tb.IdClasificacionTipoInstrumento;
        dfc.IdTipoInstrumento = tb.IdTipoInstrumento;
        dfc.IdInstrumento = tb.IdInstrumento;
        dfc.IdEmisor = tb.IdEmisor;
        dfc.IdTipoReajuste = tb.IdTipoReajuste;
        dfc.EsFungible = tb.EsFungible;
        dfc.IdBoveda = tb.IdBoveda;
        dfc.IdTipoInstrumentoEspecifico = tb.IdTipoInstrumentoEspecifico;
        dfc.IdRegimenFondoMutuo = tb.IdRegimenFondoMutuo;
        return dfc;
    }
}
