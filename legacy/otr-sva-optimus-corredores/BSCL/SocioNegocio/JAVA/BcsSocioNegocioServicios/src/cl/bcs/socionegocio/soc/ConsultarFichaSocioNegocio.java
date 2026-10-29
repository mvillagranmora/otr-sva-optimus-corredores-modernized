package cl.bcs.socionegocio.soc;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.soc.pojo.Cliente;
import cl.bcs.socionegocio.soc.pojo.Indicador;
import cl.bcs.socionegocio.soc.pojo.Portafolio;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.StringUtils;
import cl.bcs.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConsultarFichaSocioNegocio implements JavaCall {
    static final Logger logger = LogManager.getLogger(ConsultarFichaSocioNegocio.class);

    private final String SQL_SELECT_TIPO_INDICADOR_PERSONA = loadResourceToString("cl/bcs/socionegocio/soc/sql/ConsultarFichaSocioNegocio_SelectTipoIndicadorPersona.sql");
    private final String SQL_SELECT_TIPO_INDICADOR_CLIENTE = loadResourceToString("cl/bcs/socionegocio/soc/sql/ConsultarFichaSocioNegocio_SelectTipoIndicadorCliente.sql");
    private final String SQL_SELECT_TIPO_INDICADOR_PORTAFOLIO = loadResourceToString("cl/bcs/socionegocio/soc/sql/ConsultarFichaSocioNegocio_SelectTipoIndicadorPortafolio.sql");
    private final String SQL_TEMPORAL_INSERT_TIPO_INDICADOR_ENTIDAD = loadResourceToString("cl/bcs/socionegocio/soc/sql/ConsultarFichaSocioNegocio_InsertTemporalIndicador.sql");
    private final String SQL_SELECT_INDICADOR_ENTIDAD = loadResourceToString("cl/bcs/socionegocio/soc/sql/ConsultarFichaSocioNegocio_SelectIndicadorEntidad.sql");
    private final String SQL_DELETE_TEMPORAL_INDICADOR_ENTIDAD = loadResourceToString("cl/bcs/socionegocio/soc/sql/ConsultarFichaSocioNegocio_DeleteTemporalIndicador.sql");

    private final String SQL_SELECT_DATOS_GENERALES_CLIENTE_PERSONA = loadResourceToString("cl/bcs/socionegocio/soc/sql/ConsultarFichaSocioNegocio_SelectDatosGeneralesClientePersona.sql");
    private final String SQL_SELECT_PORTAFOLIOS = loadResourceToString("cl/bcs/socionegocio/soc/sql/ConsultarFichaSocioNegocio_SelectPortafolios.sql");
    private final String SQL_SELECT_FATCACRS = loadResourceToString("cl/bcs/socionegocio/soc/sql/ConsultarFichaSocioNegocio_SelectFatcaCrs.sql");

    private final String SQL_COUNT_DOCUMENTOS_IDENTIDAD = loadResourceToString("cl/bcs/socionegocio/soc/sql/ConsultarFichaSocioNegocio_CountDocumentosIdentidad.sql");
    private final String SQL_COUNT_CUENTAS_BANCARIAS = loadResourceToString("cl/bcs/socionegocio/soc/sql/ConsultarFichaSocioNegocio_CountCuentasBancarias.sql");
    private final String SQL_COUNT_TARJETAS_BANCARIAS = loadResourceToString("cl/bcs/socionegocio/soc/sql/ConsultarFichaSocioNegocio_CountTarjetasBancarias.sql");
    private final String SQL_COUNT_TELEFONOS = loadResourceToString("cl/bcs/socionegocio/soc/sql/ConsultarFichaSocioNegocio_CountTelefonos.sql");
    private final String SQL_COUNT_DIRECCIONES_FISICAS = loadResourceToString("cl/bcs/socionegocio/soc/sql/ConsultarFichaSocioNegocio_CountDireccionesFisicas.sql");
    private final String SQL_COUNT_DIRECCIONES_ELECTRONICAS = loadResourceToString("cl/bcs/socionegocio/soc/sql/ConsultarFichaSocioNegocio_CountDireccionesElectronicas.sql");
    private final String SQL_COUNT_GRUPOS_DIRECCIONES_ELECTRONICAS = loadResourceToString("cl/bcs/socionegocio/soc/sql/ConsultarFichaSocioNegocio_CountGruposDireccionesElectronicas.sql");
    private final String SQL_COUNT_RELACIONES = loadResourceToString("cl/bcs/socionegocio/soc/sql/ConsultarFichaSocioNegocio_CountRelaciones.sql");
    private final String SQL_COUNT_NOMBRES_FANTASIA = loadResourceToString("cl/bcs/socionegocio/soc/sql/ConsultarFichaSocioNegocio_CountNombresFantasia.sql");
    private final String SQL_COUNT_PAISES_FATCA = loadResourceToString("cl/bcs/socionegocio/soc/sql/ConsultarFichaSocioNegocio_CountPaisesFatca.sql");
    private final String SQL_COUNT_CUENTAS_CUSTODIOS = loadResourceToString("cl/bcs/socionegocio/soc/sql/ConsultarFichaSocioNegocio_CountCuentasCustodios.sql");
    private final String SQL_COUNT_PREFERENCIAS_PAGO_PAGA = loadResourceToString("cl/bcs/socionegocio/soc/sql/ConsultarFichaSocioNegocio_CountPreferenciasPagoPaga.sql");
    private final String SQL_COUNT_PREFERENCIAS_PAGO_RECIBE = loadResourceToString("cl/bcs/socionegocio/soc/sql/ConsultarFichaSocioNegocio_CountPreferenciasPagoRecibe.sql");


    public static class EntradaConsultarFichaSocioNegocio extends BcsPojo {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public List<ClienteEntidad> entidades;

        public EntradaConsultarFichaSocioNegocio() {
            this.entidades = new ArrayList<>();
        }
    }

    public static class Entidad extends BcsPojo {
        public Long IdTrabajo;
        public Long IdEntidad;
        public String IdTipoEntidad;
    }

    public static class TipoIndicadorEntidad extends BcsPojo {
        public Long IdTrabajo;
        public Long IdEmpresa;
        public Long IdEntidad;
        public Long IdTipoIndicador;
        public String IdTipoEntidad;
        public String CodigoTipoIndicador;
    }

    public static class ClienteEntidad extends BcsPojo {
        public Long IdCliente;
    }

    public static class SalidaConsultarFichaSocioNegocio extends BcsPojo {

        public List<MensajeSalida> P_OUT_MENSAJES;
        public Long P_OUT_ERRORESCONTADOR;
        public List<Cliente> P_OUT_RESULTADOPROCESO;

        public SalidaConsultarFichaSocioNegocio() {
            this.P_OUT_ERRORESCONTADOR = 0L;
            this.P_OUT_MENSAJES = new ArrayList<>();
            this.P_OUT_RESULTADOPROCESO = new ArrayList<>();
        }
    }

    public static class CountResult extends BcsPojo {
        public Long IdEntidad;
        public Long Cantidad;
    }


    public SalidaConsultarFichaSocioNegocio logica(Connection conn, EntradaConsultarFichaSocioNegocio in) throws Exception {
        logger.info("INICIO::ConsultarFichaSocioNegocio");
        long inicio = System.currentTimeMillis();

        SalidaConsultarFichaSocioNegocio salida = new SalidaConsultarFichaSocioNegocio();
        final long idTrabajo = Double.valueOf(Math.random() * 10000000000L).longValue();

        try {
            List<Long> idsClientes = in.entidades.stream().map(clienteEntidad -> clienteEntidad.IdCliente).distinct().collect(Collectors.toList());
            List<Cliente.DatosGenerales> clientesDatosGenerales = obtenerClientesDatosGenerales(conn, idsClientes);
            List<Cliente.Fatca> clientesFatcaCrs = obtenerClientesFactaCrs(conn, idsClientes);
            List<Long> idsPersonas = clientesDatosGenerales.stream().map(clienteEntidad -> clienteEntidad.IdPersona).distinct().collect(Collectors.toList());

            Map<Long, Long> cantidadDocumentosIdentidadPorIdPersona = obtenerConteosPorIdEntidad(conn, idsPersonas, SQL_COUNT_DOCUMENTOS_IDENTIDAD);
            Map<Long, Long> cantidadCuentasBancariasPorIdPersona = obtenerConteosPorIdEntidad(conn, idsPersonas, SQL_COUNT_CUENTAS_BANCARIAS);
            Map<Long, Long> cantidadTarjetasBancariasPorIdPersona = obtenerConteosPorIdEntidad(conn, idsPersonas, SQL_COUNT_TARJETAS_BANCARIAS);
            Map<Long, Long> cantidadTelefonosPorIdPersona = obtenerConteosPorIdEntidad(conn, idsPersonas, SQL_COUNT_TELEFONOS);
            Map<Long, Long> cantidadDireccionesFisicasPorIdPersona = obtenerConteosPorIdEntidad(conn, idsPersonas, SQL_COUNT_DIRECCIONES_FISICAS);
            Map<Long, Long> cantidadDireccionesElectronicasPorIdPersona = obtenerConteosPorIdEntidad(conn, idsPersonas, SQL_COUNT_DIRECCIONES_ELECTRONICAS);
            Map<Long, Long> cantidadGruposDireccionesElectronicasPorIdPersona = obtenerConteosPorIdEntidad(conn, idsPersonas, SQL_COUNT_GRUPOS_DIRECCIONES_ELECTRONICAS);
            Map<Long, Long> cantidadRelacionesPorIdPersona = obtenerConteosPorIdEntidad(conn, idsPersonas, SQL_COUNT_RELACIONES);
            Map<Long, Long> cantidadNombresFantasiaPorIdPersona = obtenerConteosPorIdEntidad(conn, idsPersonas, SQL_COUNT_NOMBRES_FANTASIA);
            Map<Long, Long> cantidadFatcaPorIdPersona = obtenerConteosPorIdEntidad(conn, idsPersonas, SQL_COUNT_PAISES_FATCA);

            insertarIndicadoresTemporales(conn, idTrabajo, in.p_IdEmpresa, "PERSONA", idsPersonas);
            insertarIndicadoresTemporales(conn, idTrabajo, in.p_IdEmpresa, "CLIENTE", idsClientes);

            Map<Long, List<Indicador>> indicadoresPersonaPorIdPersona = obtenerIndicadoresPorIdEntidad(conn, idTrabajo, "PERSONA");
            Map<Long, List<Indicador>> indicadoresClientePorIdCliente = obtenerIndicadoresPorIdEntidad(conn, idTrabajo, "CLIENTE");

            Map<Long, List<Portafolio>> portafoliosPorIdCliente = obtenerPortafoliosPorIdCliente(conn, idTrabajo, in, idsClientes);

            Map<Long, Cliente.Fatca> fatcaCrsPorIdCliente = clientesFatcaCrs.stream().collect(Collectors.toMap(u -> u.IdCliente, u -> u));
            salida.P_OUT_RESULTADOPROCESO = clientesDatosGenerales
                    .stream()
                    .map(datosCliente -> {
                        Cliente cliente = new Cliente();
                        cliente.DatosGenerales = datosCliente;
                        cliente.DatosGenerales.EsBorrador = "N";
                        cliente.Fatca = fatcaCrsPorIdCliente.getOrDefault(cliente.DatosGenerales.IdCliente, new Cliente.Fatca());
                        cliente.IdPersona = datosCliente.IdPersona;
                        cliente.CantidadDocumentosIdentidad = cantidadDocumentosIdentidadPorIdPersona.getOrDefault(cliente.IdPersona, 0L);
                        cliente.CantidadCuentasBancarias = cantidadCuentasBancariasPorIdPersona.getOrDefault(cliente.IdPersona, 0L);
                        cliente.CantidadTarjetasBancarias = cantidadTarjetasBancariasPorIdPersona.getOrDefault(cliente.IdPersona, 0L);
                        cliente.CantidadTelefonos = cantidadTelefonosPorIdPersona.getOrDefault(cliente.IdPersona, 0L);
                        cliente.CantidadDireccionesFisicas = cantidadDireccionesFisicasPorIdPersona.getOrDefault(cliente.IdPersona, 0L);
                        cliente.CantidadDireccionesElectronicas = cantidadDireccionesElectronicasPorIdPersona.getOrDefault(cliente.IdPersona, 0L);
                        cliente.CantidadGruposDireccionesElectronicas = cantidadGruposDireccionesElectronicasPorIdPersona.getOrDefault(cliente.IdPersona, 0L);
                        cliente.CantidadRelaciones = cantidadRelacionesPorIdPersona.getOrDefault(cliente.IdPersona, 0L);
                        cliente.CantidadNombresFantasia = cantidadNombresFantasiaPorIdPersona.getOrDefault(cliente.IdPersona, 0L);
                        cliente.CantidadFatca = cantidadFatcaPorIdPersona.getOrDefault(cliente.IdPersona, 0L);
                        cliente.IndicadoresPersonaPorCodigoTipoIndicador = obtenerIndicadorPorCodigoTipoIndicador(indicadoresPersonaPorIdPersona.getOrDefault(datosCliente.IdPersona, new ArrayList<>()));
                        cliente.IndicadoresClientePorCodigoTipoIndicador = obtenerIndicadorPorCodigoTipoIndicador(indicadoresClientePorIdCliente.getOrDefault(datosCliente.IdCliente, new ArrayList<>()));
                        cliente.Portafolios = portafoliosPorIdCliente.getOrDefault(datosCliente.IdCliente, new ArrayList<>());

                        cliente.TieneResidenciaTributariaExtranjera = StringUtils.isNullOrBlank(cliente.DatosGenerales.ResidenciaTributariaExtranjera) ? "N" : cliente.DatosGenerales.ResidenciaTributariaExtranjera;

                        return cliente;
                    })
                    .collect(Collectors.toList());
        } finally {
            eliminarIndicadoresTemporales(conn, idTrabajo);
        }

        long fin = System.currentTimeMillis();
        logger.info("FIN::ConsultarFichaSocioNegocio {}", fin - inicio);

        return salida;
    }

    private void insertarIndicadoresTemporales(Connection conn, long idTrabajo, long idEmpresa, String idTipoEntidad, List<Long> idsEntidades) throws SQLException {
        logger.debug("INICIO::ConsultarFichaSocioNegocio.insertarIndicadoresTemporales");
        long inicio = System.currentTimeMillis();

        String selectString;
        switch (idTipoEntidad) {
            case "PERSONA": {
                selectString = SQL_SELECT_TIPO_INDICADOR_PERSONA;
                break;
            }
            case "CLIENTE": {
                selectString = SQL_SELECT_TIPO_INDICADOR_CLIENTE;
                break;
            }
            case "PORTAFOLIO": {
                selectString = SQL_SELECT_TIPO_INDICADOR_PORTAFOLIO;
                break;
            }
            default:
                throw new IllegalArgumentException("No se ha definido insercion de indicadores temporales para el tipo de entidad " + idTipoEntidad);
        }

        HashMap<String, Object> params = new HashMap<>(8);
        params.put("IdTrabajo", idTrabajo);
        params.put("IdEmpresa", idEmpresa);
        params.put("IdsEntidades", idsEntidades);

        SQL sql = new SQL();
        List<TipoIndicadorEntidad> tiposIndicadoresEntidades = sql.query(conn, selectString, params, TipoIndicadorEntidad.class);
        sql.batch(conn, SQL_TEMPORAL_INSERT_TIPO_INDICADOR_ENTIDAD, tiposIndicadoresEntidades, TipoIndicadorEntidad.class);

        long fin = System.currentTimeMillis();
        logger.debug("FIN::ConsultarFichaSocioNegocio.insertarIndicadoresTemporales {}", fin - inicio);
    }

    private void eliminarIndicadoresTemporales(Connection conn, long idTrabajo) throws SQLException {
        logger.debug("INICIO::ConsultarFichaSocioNegocio.eliminarIndicadoresTemporales");
        long inicio = System.currentTimeMillis();

        Entidad entidad = new Entidad();
        entidad.IdTrabajo = idTrabajo;

        List<Entidad> entidades = new ArrayList<>(Collections.singletonList(entidad));

        SQL sql = new SQL();
        sql.batch(conn, SQL_DELETE_TEMPORAL_INDICADOR_ENTIDAD, entidades, Entidad.class);

        long fin = System.currentTimeMillis();
        logger.debug("FIN::ConsultarFichaSocioNegocio.eliminarIndicadoresTemporales {}", fin - inicio);
    }

    private Map<Long, List<Portafolio>> obtenerPortafoliosPorIdCliente(Connection conn, long idTrabajo, EntradaConsultarFichaSocioNegocio in, List<Long> idsClientes) throws SQLException {
        logger.debug("INICIO::ConsultarFichaSocioNegocio.obtenerPortafoliosPorIdCliente");
        long inicio = System.currentTimeMillis();

        HashMap<String, Object> parametros = new HashMap<>(2);
        parametros.put("IdsClientes", idsClientes);

        SQL sql = new SQL();
        List<Portafolio.DatosGenerales> datosGeneralesPortafolios = sql.query(conn, SQL_SELECT_PORTAFOLIOS, parametros, Portafolio.DatosGenerales.class);
        List<Long> idsPortafolios = datosGeneralesPortafolios.stream().map(datosPortafolio -> datosPortafolio.IdPortafolio).collect(Collectors.toList());

        // Obtener indicadores
        insertarIndicadoresTemporales(conn, idTrabajo, in.p_IdEmpresa, "PORTAFOLIO", idsPortafolios);
        Map<Long, List<Indicador>> indicadoresPorIdPortafolio = obtenerIndicadoresPorIdEntidad(conn, idTrabajo, "PORTAFOLIO");

        // Obtener conteos
        Map<Long, Long> cantidadCuentaCustodioPorIdPortafolio = obtenerConteosPorIdEntidad(conn, idsPortafolios, SQL_COUNT_CUENTAS_CUSTODIOS);
        Map<Long, Long> cantidadPreferenciasPagoPagaPorIdPortafolio = obtenerConteosPorIdEntidad(conn, idsPortafolios, SQL_COUNT_PREFERENCIAS_PAGO_PAGA);
        Map<Long, Long> cantidadPreferenciasPagoRecibePorIdPortafolio = obtenerConteosPorIdEntidad(conn, idsPortafolios, SQL_COUNT_PREFERENCIAS_PAGO_RECIBE);

        // Armar objetos Portafolio
        Map<Long, List<Portafolio>> portafoliosPorIdCliente = datosGeneralesPortafolios
                .stream()
                .map(datosGeneralesPortafolio -> {
                    Portafolio portafolio = new Portafolio();
                    portafolio.IdPortafolio = datosGeneralesPortafolio.IdPortafolio;
                    portafolio.DatosGenerales = datosGeneralesPortafolio;
                    portafolio.IndicadoresPorCodigoTipoIndicador = obtenerIndicadorPorCodigoTipoIndicador(indicadoresPorIdPortafolio.getOrDefault(portafolio.IdPortafolio, new ArrayList<>()));
                    portafolio.CantidadCuentaCustodio = cantidadCuentaCustodioPorIdPortafolio.getOrDefault(datosGeneralesPortafolio.IdPortafolio, 0L);
                    portafolio.CantidadPreferenciasPagoPaga = cantidadPreferenciasPagoPagaPorIdPortafolio.getOrDefault(datosGeneralesPortafolio.IdPortafolio, 0L);
                    portafolio.CantidadPreferenciasPagoRecibe = cantidadPreferenciasPagoRecibePorIdPortafolio.getOrDefault(datosGeneralesPortafolio.IdPortafolio, 0L);

                    return portafolio;
                })
                .collect(Collectors.groupingBy(portafolio -> portafolio.DatosGenerales.IdCliente, Collectors.mapping(portafolio -> portafolio, Collectors.toList())));

        long fin = System.currentTimeMillis();
        logger.debug("FIN::ConsultarFichaSocioNegocio.obtenerPortafoliosPorIdCliente {}", fin - inicio);

        return portafoliosPorIdCliente;
    }

    private Map<Long, List<Indicador>> obtenerIndicadoresPorIdEntidad(Connection conn, Long idTrabajo, String tipoEntidad) throws SQLException {
        logger.debug("INICIO::ConsultarFichaSocioNegocio.obtenerIndicadoresPorIdEntidad");
        long inicio = System.currentTimeMillis();

        HashMap<String, Object> parametros = new HashMap<>(4);
        parametros.put("IdTipoEntidad", tipoEntidad);
        parametros.put("IdTrabajo", idTrabajo);

        SQL sql = new SQL();
        List<Indicador> indicadores = sql.query(conn, SQL_SELECT_INDICADOR_ENTIDAD, parametros, Indicador.class);
        Map<Long, List<Indicador>> indicadoresPorIdEntidad = new HashMap<>(indicadores.size());
        for (Indicador indicador : indicadores) {
            indicador.TieneValorAsignado = indicador.IdIndicador == null ? "N" : "S";
            indicadoresPorIdEntidad.computeIfAbsent(indicador.IdEntidad, id -> new ArrayList<>()).add(indicador);
        }

        long fin = System.currentTimeMillis();
        logger.debug("FIN::ConsultarFichaSocioNegocio.obtenerIndicadoresPorIdEntidad {}", fin - inicio);

        return indicadoresPorIdEntidad;
    }

    private Map<Long, Long> obtenerConteosPorIdEntidad(Connection conn, List<Long> idsEntidades, String query) throws SQLException {
        logger.debug("INICIO::ConsultarFichaSocioNegocio.obtenerConteosPorIdEntidad");
        long inicio = System.currentTimeMillis();

        SQL sql = new SQL();
        HashMap<String, Object> parametros = new HashMap<>(2);
        parametros.put("IdsEntidades", idsEntidades);

        List<CountResult> resultadosConteos = sql.query(conn, query, parametros, CountResult.class);
        Map<Long, Long> cantidadesPorIdEntidad = resultadosConteos.stream().collect(Collectors.toMap(resultado -> resultado.IdEntidad, resultado -> resultado.Cantidad));

        long fin = System.currentTimeMillis();
        logger.debug("FIN::ConsultarFichaSocioNegocio.obtenerConteosPorIdEntidad {}", fin - inicio);

        return cantidadesPorIdEntidad;
    }

    private List<Cliente.DatosGenerales> obtenerClientesDatosGenerales(Connection conn, List<Long> idsClientes) throws SQLException {
        logger.debug("INICIO::ConsultarFichaSocioNegocio.obtenerClientesDatosGenerales");
        long inicio = System.currentTimeMillis();

        SQL sql = new SQL();
        HashMap<String, Object> parametros = new HashMap<>(2);
        parametros.put("idsClientes", idsClientes);

        List<Cliente.DatosGenerales> datosGenerales = sql.query(conn, SQL_SELECT_DATOS_GENERALES_CLIENTE_PERSONA, parametros, Cliente.DatosGenerales.class);

        long fin = System.currentTimeMillis();
        logger.debug("FIN::ConsultarFichaSocioNegocio.obtenerClientesDatosGenerales {}", fin - inicio);
        return datosGenerales;
    }

    private List<Cliente.Fatca> obtenerClientesFactaCrs(Connection conn, List<Long> idsClientes) throws SQLException {
        logger.debug("INICIO::ConsultarFichaSocioNegocio.obtenerClientesFactaCrs");
        long inicio = System.currentTimeMillis();

        SQL sql = new SQL();
        HashMap<String, Object> parametros = new HashMap<>(2);
        parametros.put("idsClientes", idsClientes);

        List<Cliente.Fatca> fatcas = sql.query(conn, SQL_SELECT_FATCACRS, parametros, Cliente.Fatca.class);

        long fin = System.currentTimeMillis();
        logger.debug("FIN::ConsultarFichaSocioNegocio.obtenerClientesFactaCrs {}", fin - inicio);
        return fatcas;
    }

    private Map<String, Indicador> obtenerIndicadorPorCodigoTipoIndicador(List<Indicador> indicadores) {
        logger.debug("INICIO::ConsultarFichaSocioNegocio.obtenerIndicadorPorCodigoTipoIndicador");
        long inicio = System.currentTimeMillis();

        Map<String, Indicador> indicadorPorCodigoTipoIndicador = indicadores
                .stream()
                .collect(Collectors.toMap(indicador -> indicador.CodigoTipoIndicador, indicador -> indicador));

        long fin = System.currentTimeMillis();
        logger.debug("FIN::ConsultarFichaSocioNegocio.obtenerIndicadorPorCodigoTipoIndicador {}", fin - inicio);

        return indicadorPorCodigoTipoIndicador;
    }

    @Override
    public JsonObject call(Connection conn, JsonObject jin) throws Exception {
        Gson gson = Utils.getGsonBuilder();
        EntradaConsultarFichaSocioNegocio in = gson.fromJson(jin.toString(), EntradaConsultarFichaSocioNegocio.class);
        SalidaConsultarFichaSocioNegocio out = logica(conn, in);

        JsonParser parser = new JsonParser();
        return parser.parse(gson.toJson(out)).getAsJsonObject();
    }

    public static void main(String[] args) {
        System.out.println("== INICIO PRUEBA LOCAL ==");

        try (Connection con = PlataformaUtils.getDESAConnection()) {
            Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd").serializeNulls().create();

            String jsonRequest = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":\"21\",\"entidades\":[{\"IdInterno\":1,\"IdCliente\":524891}]}";

            EntradaConsultarFichaSocioNegocio entrada = gson.fromJson(jsonRequest, EntradaConsultarFichaSocioNegocio.class);

            ConsultarFichaSocioNegocio demo = new ConsultarFichaSocioNegocio();
            System.out.println("ENTRADA");
            System.out.println(entrada);

            try {
                SalidaConsultarFichaSocioNegocio salida = demo.logica(con, entrada);
                con.rollback();
                System.out.println("SALIDA");
                System.out.println(salida);
            } catch (Exception er) {
                er.printStackTrace();
                con.rollback();
            }
            con.rollback();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println("== FIN PRUEBA LOCAL ==");
    }

}
