package cl.bcs.apv.application.recaudacion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.bcs.apv.api.casouso.CargarMasivaRecaudacion;
import cl.bcs.apv.api.dto.cargarmasivarecaudacion.ArchivoRecaudacion;
import cl.bcs.apv.api.dto.cargarmasivarecaudacion.CargaArchivo;
import cl.bcs.apv.api.dto.cargarmasivarecaudacion.CargarMasivaRecaudacionEntrada;
import cl.bcs.apv.api.dto.cargarmasivarecaudacion.CargarMasivaRecaudacionResultado;
import cl.bcs.apv.domain.port.out.AlmacenArchivos;
import cl.bcs.apv.domain.port.out.Colaboradores;
import cl.bcs.apv.domain.port.out.ConfiguracionSistema;
import cl.bcs.apv.domain.port.out.DiccionarioMensajes;
import cl.bcs.apv.domain.port.out.Reloj;
import cl.bcs.apv.domain.port.out.Secuencias;

/**
 * Use case {@code CargarMasivaRecaudacion} (family C): stages the folio pairs of a collection file, rejects a file whose folios were already loaded and
 * otherwise stores the load header and its detail rows. Legacy {@code cl.bcs.APV.CargaMasivaRecaudacion}.
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class CargarMasivaRecaudacionServicio implements CargarMasivaRecaudacion {

    private static final String SQL_INSERT_CARGA_ARCHIVO = SqlLegacy.recurso("CargaMasivaRecaudacion_CargaMasivaArchivo_Insert");
    private static final String SQL_INSERT_CARGA_DETALLE = SqlLegacy.recurso("CargaMasivaRecaudacion_CargaMasivaArchivoDetalle_Insert");
    private static final String SQL_SELECT_CARGA_DETALLE = SqlLegacy.recurso("CargaMasivaRecaudacion_CargaMasivaArchivoDetalle_Select");
    private static final String SQL_CREATE_TABLA_TEMPORAL = SqlLegacy.recurso("CargaMasivaRecaudacion_CargaMasivaRecaudacion_CargaArchivo_CreateTable");
    private static final String SQL_INSERT_TABLA_TEMPORAL = SqlLegacy.recurso("CargaMasivaRecaudacion_CargaMasivaRecaudacion_CargaArchivo_Insert");
    private static final String SQL_DROP_TABLA_TEMPORAL = SqlLegacy.recurso("CargaMasivaRecaudacion_CargaMasivaRecaudacion_CargaArchivo_Drop");

    private final SqlLegacy sql;
    private final Secuencias secuencias;

    public CargarMasivaRecaudacionServicio(NamedParameterJdbcTemplate jdbc, ConfiguracionSistema configuracion, DiccionarioMensajes diccionario,
            Secuencias secuencias, Reloj reloj, AlmacenArchivos archivos, Colaboradores colaboradores) {
        this.sql = new SqlLegacy(jdbc);
        this.secuencias = secuencias;
    }

    /** Folio pair row (legacy {@code DetalleRecaudacion}). */
    public static class FilaDetalle {
        public Long IdCargaMasiva;
        public Long FolioCartola;
        public Long FolioLineaCartola;
    }

    /** Header row (legacy {@code CargaArchivo}). */
    public static class FilaCargaArchivo {
        public Long IdEmpresa;
        public Long IdCargaMasiva;
        public String NombreCargaMasiva;
        public Long CantidadRegistros;
        public Long IdUsuario;
        public int IdTipoCarga;
        public String Descripcion;
        public String FechaCarga;
        public String IdEstado;
    }

    /** Detail row of the file (legacy {@code ArchivoRecaudacion}). */
    public static class FilaArchivo {
        public Long IdCargaMasiva;
        public String Fecha;
        public String TipoDeposito;
        public Long FolioCartola;
        public Long FolioLineaCartola;
        public String Banco;
        public String Cuenta;
        public Long Monto;
        public String PeriodoPago;
        public String TipoPago;
        public String RUTEmpleador;
        public String RUTCliente;
        public String TipodeAfiliado;
        public String Regimen;
        public String EstadoRendicion;
        public String EstadoProceso;
    }

    /** Legacy output object: the counter starts at 0 and is NOT recomputed by the legacy {@code call}. */
    private static final class Salida {
        final List<MensajeLegacy> mensajes = new ArrayList<>();
        Long contador = 0L;
    }

    @Override
    public CargarMasivaRecaudacionResultado ejecutar(CargarMasivaRecaudacionEntrada in) {
        try {
            Salida salida = logica(in);
            return new CargarMasivaRecaudacionResultado(MensajeLegacy.aMensajes(salida.mensajes), salida.contador);
        } catch (Exception e) {
            throw Sneaky.lanzar(e);
        }
    }

    private Salida logica(CargarMasivaRecaudacionEntrada in) throws Exception {
        Salida salida = new Salida();
        // CARGA ARCHIVO TABLAS TEMPORALES
        boolean tablaLimpia = false;
        try {
            insertTablaTemporal(in);
            // VALIDA QUE NO HAYA CARGA IGUAL FOLIO - FOLIO LINEA
            if (validaCarga(salida)) {
                guardarCargaMasiva(in);
            }
            // BORRA TABLA TEMPORAL
            sql.ejecutar(SQL_DROP_TABLA_TEMPORAL);
            tablaLimpia = true;
        } finally {
            // the session-scoped temporary table must not outlive a failed block (the pooled connection is reused)
            if (!tablaLimpia) {
                try {
                    sql.ejecutar(SQL_DROP_TABLA_TEMPORAL);
                } catch (Exception ignorada) {
                    // best effort, the original failure is the one that matters
                }
            }
        }
        if (salida.contador.compareTo(0L) == 0) {
            salida.mensajes.add(new MensajeLegacy(1L, MensajeLegacy.OK, "CARGA_RECAUDACION_EXITOSA", "Carga Completada Exitosamente"));
        }
        return salida;
    }

    private void insertTablaTemporal(CargarMasivaRecaudacionEntrada in) throws Exception {
        List<FilaDetalle> listaArchivo = new ArrayList<>();
        for (ArchivoRecaudacion det : in.parametros()) {
            FilaDetalle d = new FilaDetalle();
            d.FolioCartola = det.folioCartola();
            d.FolioLineaCartola = det.folioLineaCartola();
            listaArchivo.add(d);
        }
        sql.ejecutar(SQL_CREATE_TABLA_TEMPORAL);
        sql.lote(SQL_INSERT_TABLA_TEMPORAL, listaArchivo);
    }

    private boolean validaCarga(Salida out) throws Exception {
        List<FilaDetalle> listaDetalles = sql.consultar(SQL_SELECT_CARGA_DETALLE, new HashMap<String, Object>(), FilaDetalle.class);
        if (!listaDetalles.isEmpty()) {
            listaDetalles.forEach(detalle -> out.mensajes.add(new MensajeLegacy(1L, MensajeLegacy.ERROR, "CARGA_RECAUDACION_EXITE_CARGA",
                    "Folio Cartola " + detalle.FolioCartola + " y Folio Linea Cartola existen " + detalle.FolioLineaCartola)));
        }
        if (!out.mensajes.isEmpty()) {
            out.contador = 1L;
        }
        return out.mensajes.isEmpty();
    }

    private void guardarCargaMasiva(CargarMasivaRecaudacionEntrada in) throws Exception {
        List<Long> idsCargaAchivo = Colab.secuencia(secuencias, "BSCL.APV.BD.Secuencia::APV.IdCargaMasiva", (long) in.archivo().size());
        Long idCargaMasiva = idsCargaAchivo.get(0);

        List<FilaCargaArchivo> archivos = new ArrayList<>();
        for (CargaArchivo a : in.archivo()) {
            FilaCargaArchivo f = new FilaCargaArchivo();
            f.IdEmpresa = a.idEmpresa();
            f.IdCargaMasiva = idCargaMasiva;
            f.NombreCargaMasiva = a.nombreCargaMasiva();
            f.CantidadRegistros = a.cantidadRegistros();
            f.IdUsuario = a.idUsuario();
            f.IdTipoCarga = a.idTipoCarga();
            f.Descripcion = a.descripcion();
            f.FechaCarga = a.fechaCarga().substring(0, 10);
            f.IdEstado = a.idEstado();
            archivos.add(f);
        }
        List<FilaArchivo> detalles = new ArrayList<>();
        for (ArchivoRecaudacion d : in.parametros()) {
            FilaArchivo f = new FilaArchivo();
            f.IdCargaMasiva = idCargaMasiva;
            f.Fecha = d.fecha();
            f.TipoDeposito = d.tipoDeposito();
            f.FolioCartola = d.folioCartola();
            f.FolioLineaCartola = d.folioLineaCartola();
            f.Banco = d.banco();
            f.Cuenta = d.cuenta();
            f.Monto = d.monto();
            f.PeriodoPago = d.periodoPago();
            f.TipoPago = d.tipoPago();
            f.RUTEmpleador = d.rutEmpleador();
            f.RUTCliente = d.rutCliente();
            f.TipodeAfiliado = d.tipodeAfiliado();
            f.Regimen = d.regimen();
            f.EstadoRendicion = d.estadoRendicion();
            f.EstadoProceso = d.estadoProceso();
            detalles.add(f);
        }
        sql.lote(SQL_INSERT_CARGA_ARCHIVO, archivos);
        sql.lote(SQL_INSERT_CARGA_DETALLE, detalles);
    }
}
