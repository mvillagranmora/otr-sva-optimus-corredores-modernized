package cl.bcs.apv.application.recaudacion;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.bcs.apv.api.casouso.CargarBonificacionTGR;
import cl.bcs.apv.api.dto.cargarbonificaciontgr.ArchivoBonificacionTGR;
import cl.bcs.apv.api.dto.cargarbonificaciontgr.CargaArchivo;
import cl.bcs.apv.api.dto.cargarbonificaciontgr.CargarBonificacionTGREntrada;
import cl.bcs.apv.api.dto.cargarbonificaciontgr.CargarBonificacionTGRResultado;
import cl.bcs.apv.domain.port.out.AlmacenArchivos;
import cl.bcs.apv.domain.port.out.Colaboradores;
import cl.bcs.apv.domain.port.out.ConfiguracionSistema;
import cl.bcs.apv.domain.port.out.DiccionarioMensajes;
import cl.bcs.apv.domain.port.out.Reloj;
import cl.bcs.apv.domain.port.out.Secuencias;

/**
 * Use case {@code CargarBonificacionTGR} (family C): loads the TGR bonus file in batches of 1000 rows, rejecting clients that do not exist, were already
 * loaded for the year pair, and informing the missing sworn declarations. Legacy {@code cl.bcs.APV.CargaBonificacionTGR}.
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class CargarBonificacionTGRServicio implements CargarBonificacionTGR {

    private static final String SQL_CREAR_BONIFICACION_TGR = SqlLegacy.recurso("CrearBonificacion");
    private static final String SQL_BONIFICACION_POR_CODIGO_ARCHIVO = SqlLegacy.recurso("ConsultaCodigoArchivoBonificacion");
    private static final String SQL_EXISTE_CLIENTE = SqlLegacy.recurso("ConsultarExisteCliente");
    private static final String SQL_EXISTE_CLIENTE_BONIFICACION = SqlLegacy.recurso("ConsultarExisteClienteBonificacion");
    private static final String SQL_INSERT_CARGA_ARCHIVO = SqlLegacy.recurso("CargaMasivaArchivo_Insert");
    private static final String SQL_CONSULTA_DJ_ANO_Y_CLIENTE = SqlLegacy.recurso("ConsultarDJPorAnoYCliente");

    private final SqlLegacy sql;
    private final ConfiguracionSistema configuracion;
    private final Secuencias secuencias;

    public CargarBonificacionTGRServicio(NamedParameterJdbcTemplate jdbc, ConfiguracionSistema configuracion, DiccionarioMensajes diccionario,
            Secuencias secuencias, Reloj reloj, AlmacenArchivos archivos, Colaboradores colaboradores) {
        this.sql = new SqlLegacy(jdbc);
        this.configuracion = configuracion;
        this.secuencias = secuencias;
    }

    // ---------------------------------------------------------------------------------------------- legacy row classes

    /** Insert row of the bonus (legacy {@code Bonificacion}): public fields are the SQL parameter names. */
    public static class Bonificacion {
        public Long IdEmpresa;
        public Long IdBonificacion;
        public Long IdEntidadRecaudadora;
        public Date FechaGeneracion;
        public String FechaIngreso;
        public String TipoRegistro;
        public Long AnoAhorro;
        public Long AnoProceso;
        public Long FolioEgreso;
        public BigDecimal MontoPagadoNeto;
        public BigDecimal MontoSaldoNeto;
        public BigDecimal MontoPagadoReajustado;
        public Date FechaPago;
        public Long IdCliente;
        public BigDecimal MontoBonificacionAfiliadoNeto;
        public BigDecimal PA1;
        public BigDecimal PA2;
        public BigDecimal PA3;
        public BigDecimal MontoAfiliadoNeto;
        public BigDecimal MontoAfiliadoSaldoNeto;
        public BigDecimal MontoReajustadoPagadoAfiliado;
        public String IdEstado;
        public String CodigoArchivo;
        public String DocumentoIdentidad;
    }

    public static class BonificacionCantidad {
        public Long IdBonificacion;
    }

    public static class ExisteCliente {
        public Long IdCliente;
        public String DocumentoIdentidad;
    }

    public static class DJ {
        public Long ID;
        public Long IdCliente;
        public String DocumentoIdentidad;
    }

    /** Legacy {@code ParametrosSalidaProcesarLote}. */
    private static final class SalidaLote {
        Long Count;
        Long BonificacionesIngresadas;
        Long BonificacionesRechazadas;
    }

    /** Legacy output object: the counter is NEVER set (null, omitted from the JSON). */
    private static final class Salida {
        final List<MensajeLegacy> mensajes = new ArrayList<>();
    }

    @Override
    public CargarBonificacionTGRResultado ejecutar(CargarBonificacionTGREntrada in) {
        try {
            Salida salida = logica(in);
            return new CargarBonificacionTGRResultado(MensajeLegacy.aMensajes(salida.mensajes), null);
        } catch (Exception e) {
            throw Sneaky.lanzar(e);
        }
    }

    private Salida logica(CargarBonificacionTGREntrada in) throws Exception {
        Salida salida = new Salida();
        SalidaLote salidaLote = new SalidaLote();
        HashMap<String, Object> param;
        MensajeLegacy msg = new MensajeLegacy();
        param = new HashMap<>();
        Long count = 0L;
        Long bonificacionesIngresadas = 0L;
        Long bonificacionesRechazadas = 0L;
        param.put("CodigoArchivo", in.nombreCarga());
        List<BonificacionCantidad> bonificacion = sql.consultar(SQL_BONIFICACION_POR_CODIGO_ARCHIVO, param, BonificacionCantidad.class);

        if (bonificacion.size() == 0) {
            int batchSize = 1000;
            int totalRegistros = in.parametros().size();
            List<Long> idsCargaArchivo = Colab.secuencia(secuencias, "BSCL.APV.BD.Secuencia::APV.IdCargaMasiva", (long) in.archivo().size());
            String fechaSistema = Plataforma.parametro(configuracion, in.idEmpresa(), "PLATAFORMA", "PLA.FechaSistema");

            for (int i = 0; i < totalRegistros; i += batchSize) {
                int fin = Math.min(i + batchSize, totalRegistros);
                List<ArchivoBonificacionTGR> lote = in.parametros().subList(i, fin);
                salidaLote = procesarLoteBonificaciones(lote, salida, in, count, bonificacionesIngresadas, bonificacionesRechazadas);
                count = salidaLote.Count;
                bonificacionesIngresadas = salidaLote.BonificacionesIngresadas;
                bonificacionesRechazadas = salidaLote.BonificacionesRechazadas;
            }

            List<FilaCargaMasiva> filas = new ArrayList<>();
            for (CargaArchivo a : in.archivo()) {
                filas.add(new FilaCargaMasiva(a.idEmpresa(), a.idCargaMasiva(), a.nombreCargaMasiva(), a.cantidadRegistros(), a.idUsuario(),
                        a.idTipoCarga(), a.descripcion(), a.fechaCarga(), a.idEstado()));
            }
            filas.get(0).IdCargaMasiva = idsCargaArchivo.get(0);
            filas.get(0).FechaCarga = fechaSistema;

            int[] res = CargasSql.lote(sql, SQL_INSERT_CARGA_ARCHIVO, filas);
            if (res != null && res.length > 0 && res[0] == 1) {
                msg = new MensajeLegacy();
                msg.id = count + 1L;
                msg.tipo = "INFORMACION";
                msg.codigo = "INFORMACION";
                msg.descripcion = "Total Bonificaciones. Ingresadas: " + bonificacionesIngresadas + " Rechazadas: " + bonificacionesRechazadas + ".";
                salida.mensajes.add(msg);
            }
        } else {
            msg.id = count + 1L;
            msg.tipo = "ERROR";
            msg.codigo = "ERROR";
            msg.descripcion = "El archivo ya fue cargado previamente.";
            salida.mensajes.add(msg);
        }

        // LEGACY DEFECT kept (see README APV-C-270..287): the commit runs at the end (rows were already batched)
        sql.commit();
        return salida;
    }

    @SuppressWarnings("StringEquality")
    private SalidaLote procesarLoteBonificaciones(List<ArchivoBonificacionTGR> lote, Salida salida, CargarBonificacionTGREntrada in, Long count,
            Long bonificacionesIngresadas, Long bonificacionesRechazadas) throws Exception {
        HashMap<String, Object> param;
        List<String> clientes = new ArrayList<>();
        List<Long> idClientes = new ArrayList<>();
        List<Long> clientesDJ = new ArrayList<>();
        List<ExisteCliente> idClientesDJ = new ArrayList<>();
        SalidaLote salidaLote = new SalidaLote();
        List<Bonificacion> paramsInsertBonificacion = new ArrayList<Bonificacion>();
        String fechaSistema = Plataforma.parametro(configuracion, in.idEmpresa(), "PLATAFORMA", "PLA.FechaSistema");
        MensajeLegacy msg;

        for (ArchivoBonificacionTGR fila : lote) {
            Bonificacion registroBonificacion = new Bonificacion();

            SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
            // LEGACY DEFECT kept (see README APV-C-287): the date parser is lenient (15-03-2025 becomes year 0014)
            Date fechaGeneracion = format.parse(fila.fechaDeGeneracion());
            Date fechaPago = format.parse(fila.fechaDePago());

            String documentoIdentidadCliente = fila.rutAfiliado() + '-' + fila.dvAfiliado();

            param = new HashMap<>();

            clientes.add(documentoIdentidadCliente);

            registroBonificacion.IdEmpresa = in.idEmpresa();
            registroBonificacion.IdEntidadRecaudadora = 1L;
            registroBonificacion.FechaGeneracion = fechaGeneracion;
            registroBonificacion.FechaIngreso = fechaSistema;
            // LEGACY DEFECT kept (see README APV-C-271): String compared by reference, so the record type is ALWAYS ACTUAL
            registroBonificacion.TipoRegistro = fila.tipoDeRegistro() == "3" ? "ANTERIOR" : "ACTUAL";
            registroBonificacion.AnoAhorro = Long.parseLong(fila.anoAhorro());
            registroBonificacion.AnoProceso = Long.parseLong(fila.anoProceso());
            registroBonificacion.FolioEgreso = Long.parseLong(fila.folioEgreso());
            registroBonificacion.MontoPagadoNeto = new BigDecimal(fila.montoPagadoNeto());
            registroBonificacion.MontoSaldoNeto = new BigDecimal(fila.montoSaldoNeto());
            registroBonificacion.MontoPagadoReajustado = new BigDecimal(fila.montoPagadoReajustado());
            registroBonificacion.FechaPago = fechaPago;
            registroBonificacion.DocumentoIdentidad = documentoIdentidadCliente;
            registroBonificacion.MontoBonificacionAfiliadoNeto = new BigDecimal(fila.montoBonificacionAfiliadoNeto());
            registroBonificacion.PA1 = new BigDecimal(fila.pa1().substring(0, 3) + '.' + fila.pa1().substring(3, 5));
            registroBonificacion.PA2 = new BigDecimal(fila.pa2().substring(0, 3) + '.' + fila.pa2().substring(3, 5));
            registroBonificacion.PA3 = new BigDecimal(fila.pa3().substring(0, 3) + '.' + fila.pa3().substring(3, 5));
            registroBonificacion.MontoAfiliadoNeto = new BigDecimal(fila.montoAfiliadoNeto());
            registroBonificacion.MontoAfiliadoSaldoNeto = new BigDecimal(fila.montoAfiliadoSaldoNeto());
            registroBonificacion.MontoReajustadoPagadoAfiliado = new BigDecimal(fila.montoReajustadoPagadoAfiliado());
            registroBonificacion.IdEstado = "ING";
            registroBonificacion.CodigoArchivo = in.nombreCarga();

            paramsInsertBonificacion.add(registroBonificacion);
        }

        param = new HashMap<>();

        param.put("DocumentoIdentidad", clientes);
        List<ExisteCliente> existeCliente = sql.consultar(SQL_EXISTE_CLIENTE, param, ExisteCliente.class);

        Set<String> clientesExistentes = new HashSet<>();
        for (ExisteCliente cliente : existeCliente) {
            clientesExistentes.add(String.valueOf(cliente.DocumentoIdentidad).toLowerCase());
        }

        for (String cli : clientes) {
            if (!clientesExistentes.contains(cli.toLowerCase())) {
                count++;
                msg = new MensajeLegacy(count, "ERROR", "ERROR", "No existe el Cliente con RUT: " + cli + ".");
                salida.mensajes.add(msg);

                msg = new MensajeLegacy(count, "ERROR", "ERROR", "Bonificacion no ingresada.");
                salida.mensajes.add(msg);

                bonificacionesRechazadas++;
            }
        }

        for (ExisteCliente cli : existeCliente) {
            idClientes.add(cli.IdCliente);
        }

        Long anoAhorro = 0L;
        Long anoProceso = 0L;
        for (Bonificacion fila : paramsInsertBonificacion) {
            // LEGACY DEFECT kept (see README APV-C-285): the year pair of the FIRST row of each batch is used for the whole batch
            anoAhorro = fila.AnoAhorro;
            anoProceso = fila.AnoProceso;
            break;
        }

        param = new HashMap<>();

        param.put("IdCliente", idClientes);
        param.put("AnoAhorro", anoAhorro);
        param.put("AnoProceso", anoProceso);
        List<ExisteCliente> existeClienteBonificacion = sql.consultar(SQL_EXISTE_CLIENTE_BONIFICACION, param, ExisteCliente.class);

        Set<Long> idsConBonificacion = new HashSet<>();
        for (ExisteCliente cli : existeClienteBonificacion) {
            idsConBonificacion.add(cli.IdCliente);
        }

        for (ExisteCliente cliente : existeCliente) {
            if (idsConBonificacion.contains(cliente.IdCliente)) {
                count++;
                msg = new MensajeLegacy(count, "ERROR", "ERROR", "Cliente con RUT: " + cliente.DocumentoIdentidad + " ya fue ingresado en el ano ahorro: "
                        + anoAhorro + " y en el ano proceso: " + anoProceso + ".");
                salida.mensajes.add(msg);

                msg = new MensajeLegacy(count, "ERROR", "ERROR", "Bonificacion no ingresada.");
                salida.mensajes.add(msg);

                bonificacionesRechazadas++;
            } else {
                ExisteCliente cli = new ExisteCliente();
                cli.IdCliente = cliente.IdCliente;
                cli.DocumentoIdentidad = cliente.DocumentoIdentidad;
                idClientesDJ.add(cli);
            }
        }

        param = new HashMap<>();

        for (ExisteCliente cli : idClientesDJ) {
            clientesDJ.add(cli.IdCliente);
        }

        param.put("IdCliente", clientesDJ);
        param.put("AnoAhorro", anoAhorro);
        List<DJ> djs = sql.consultar(SQL_CONSULTA_DJ_ANO_Y_CLIENTE, param, DJ.class);

        Set<Long> idsConDJ = new HashSet<>();
        for (DJ dj : djs) {
            idsConDJ.add(dj.IdCliente);
        }

        for (ExisteCliente clienteDJ : idClientesDJ) {
            if (!idsConDJ.contains(clienteDJ.IdCliente)) {
                count++;
                msg = new MensajeLegacy(count, "INFORMACION", "INFORMACION",
                        "No se encontro una Declaracion Jurada para el Cliente: " + clienteDJ.DocumentoIdentidad + " en este ano: " + anoAhorro);
                salida.mensajes.add(msg);
            }
        }

        Map<String, Long> mapClientes = new HashMap<>();
        for (ExisteCliente cliente : existeCliente) {
            mapClientes.put(String.valueOf(cliente.DocumentoIdentidad), cliente.IdCliente);
        }

        for (Bonificacion fila : paramsInsertBonificacion) {
            Long idCliente = mapClientes.get(fila.DocumentoIdentidad);
            if (idCliente != null) {
                fila.IdCliente = idCliente;
            }
        }

        Set<Long> idsClientesValidos = new HashSet<>();
        for (ExisteCliente dj : idClientesDJ) {
            idsClientesValidos.add(dj.IdCliente);
        }

        Iterator<Bonificacion> it = paramsInsertBonificacion.iterator();
        while (it.hasNext()) {
            Bonificacion paramInsert = it.next();
            if (paramInsert.IdCliente == null || !idsClientesValidos.contains(paramInsert.IdCliente)) {
                it.remove();
            }
        }

        int[] resp = CargasSql.lote(sql, SQL_CREAR_BONIFICACION_TGR, paramsInsertBonificacion);
        if (resp != null && resp.length > 0) {
            for (int respuesta : resp) {
                if (respuesta == 1) {
                    count++;
                    msg = new MensajeLegacy(count, "INFORMACION", "INFORMACION", "Bonificacion Ingresada.");
                    salida.mensajes.add(msg);

                    bonificacionesIngresadas++;
                }
            }
        }

        salidaLote.Count = count;
        salidaLote.BonificacionesIngresadas = bonificacionesIngresadas;
        salidaLote.BonificacionesRechazadas = bonificacionesRechazadas;
        return salidaLote;
    }
}
