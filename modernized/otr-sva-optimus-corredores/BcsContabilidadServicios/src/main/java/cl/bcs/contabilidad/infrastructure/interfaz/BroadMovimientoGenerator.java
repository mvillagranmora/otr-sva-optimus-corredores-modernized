package cl.bcs.contabilidad.infrastructure.interfaz;

import cl.bcs.contabilidad.domain.port.out.InterfazSapSource;
import cl.bcs.contabilidad.domain.port.out.InterfazSapSource.BroadMovimiento;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.List;
import java.util.Objects;

import static cl.bcs.contabilidad.infrastructure.interfaz.ArchivoPlano.ISO_8859_1;
import static cl.bcs.contabilidad.infrastructure.interfaz.ArchivoPlano.col;
import static cl.bcs.contabilidad.infrastructure.interfaz.ArchivoPlano.fecha;

/**
 * B1_BROAD_MOV_CONT.TXT and B1_CTRL_BROAD_MOV_CONT.TXT (CONT-A-04, CONT-A-12, CONT-A-13, CONT-A-29, CONT-A-35).
 *
 * <p>Legacy: {@code CreaBroadMovimiento.logica()} (BcsContabilidadServicios). NUM-REGISTRO carries the contador,
 * never the row's value. The control sums MTO-TRANSACCION-CONTABLE starting from {@code new BigDecimal(0)} and,
 * like CTAS, always prints the epoch timestamp (it stats a path that does not exist). The file names are a
 * parameter ({@link ArchivosInterfaz}). Run-log lines: CreaBroadMovimiento.java:102, :179, :185, :201, :259, :262.
 */
public final class BroadMovimientoGenerator implements InterfazGenerator {

    public static final String ARCHIVO = "B1_BROAD_MOV_CONT.TXT";
    public static final String ARCHIVO_CONTROL = "B1_CTRL_BROAD_MOV_CONT.TXT";
    /** The names CreaInterfazDiaria and CreaInterfazDemanda used. */
    public static final ArchivosInterfaz ARCHIVOS = new ArchivosInterfaz(ARCHIVO, ARCHIVO_CONTROL);

    static final String CABECERA = "COD-INSTITUCION COD-APLICATIVO COD-ENTIDAD FEC-DIA HRA-TRASACCION "
            + "NUM-OPERACION NUM-POS_OPERACION NUM-REGISTRO COD-CTA-CONTABLE COD-TIPO-CUENTA "
            + "COD-PRODUCTO-CONTABLE COD-CENTRO-COSTO FEC-DOCUMENTO NRO-SERIE-DOC NRO-DOC COD-AFILIADA "
            + "TIP-CARGO-ABONO COD-MONEDA-CONTABLE MTO-TRANSACCION-CONTABLE IND-ANULADO IND-OPE-AFILIADA "
            + "FILLER-01 FILLER-02\n";

    static final String CABECERA_CONTROL = "TIP-RUTINA DES-RUTINA DES-SUBRUTINA DES-ESTRUCTURA "
            + "DES-CAMPOVALIDAR POS-INICIAL POS-FINAL MTO-SUMCAMPO MTO-SUMCTAREG FEC-PROCESO HRA-PROCESO \n";

    private final InterfazSapSource sap;
    private final Clock clock;
    private final ArchivosInterfaz archivos;

    public BroadMovimientoGenerator(InterfazSapSource sap, Clock clock) {
        this(sap, clock, ARCHIVOS);
    }

    public BroadMovimientoGenerator(InterfazSapSource sap, Clock clock, ArchivosInterfaz archivos) {
        this.sap = Objects.requireNonNull(sap, "sap");
        this.clock = Objects.requireNonNull(clock, "clock");
        this.archivos = Objects.requireNonNull(archivos, "archivos");
    }

    @Override
    public void generar(InterfazContext contexto) {
        List<BroadMovimiento> filas = sap.movimientosBroad(contexto.fechaEjecucion());
        InterfazBitacora bitacora = contexto.bitacora();
        String archivo = archivos.datos();

        bitacora.info(ArchivoPlano.creando(archivo));
        StringBuilder datos = new StringBuilder(CABECERA);
        BigDecimal total = new BigDecimal(0);
        for (BroadMovimiento m : ArchivoPlano.invertido(filas)) {
            datos.append(col(m.codInstitucion(), 1))
                    .append(col(m.codAplicativo(), 2))
                    .append(col(m.codEntidad(), 7))
                    .append(col(fecha(m.fecDia(), archivo, "FecDia"), 8))
                    .append(col(m.hraTransaccion(), 8))
                    .append(col(m.numOperacion(), 10))
                    .append(col(m.numPosOperacion(), 10))
                    .append(col(contexto.contador(), 10))
                    .append(col(m.codCtaContable(), 20))
                    .append(col(m.codTipoCuenta(), 20))
                    .append(col(m.codProductoContable(), 8))
                    .append(col(m.codCentroCosto(), 8))
                    .append(col(fecha(m.fecDocumento(), archivo, "FecDocumento"), 8))
                    .append(col(m.nroSerieDoc(), 10))
                    .append(col(m.nroDoc(), 11))
                    .append(col(m.codAfiliada(), 4))
                    .append(col(m.tipCargoAbono(), 1))
                    .append(col(m.codMonedaContable(), 4))
                    .append(col(m.montoTransaccionContable(), 20))
                    .append(col(m.indAnulado(), 1))
                    .append(col(m.indOpeAfiliada(), 1))
                    .append(col(m.filler01(), 4))
                    .append(col(m.filler02(), 4))
                    .append('\n');
            total = ArchivoPlano.sumar(total, m.montoTransaccionContable(), archivo, "MontoTransaccionContable");
        }
        ArchivoPlano.escribir(contexto.directorioFi().resolve(archivo), datos.toString(), ISO_8859_1,
                bitacora, ArchivoPlano.finDatos(archivo));

        bitacora.info(ArchivoPlano.creando(archivos.control()));
        MarcaProceso marca = MarcaProceso.epoca(clock.getZone());
        String control = CABECERA_CONTROL
                + col("C", 1)
                + col("SCRIPT", 30)
                + col("", 30)
                + col(archivo, 30)
                + "MTO-TRANSACCION-CONTABLE"
                + "151"
                + "170"
                + total
                + col(String.valueOf(filas.size()), 15)
                + marca.fecProceso()
                + marca.hraProceso();
        ArchivoPlano.escribir(contexto.directorioFi().resolve(archivos.control()), control, ISO_8859_1,
                bitacora, ArchivoPlano.finControlSinSeparador(archivos.control()));
    }
}
