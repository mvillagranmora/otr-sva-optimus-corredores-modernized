package cl.bcs.contabilidad.infrastructure.interfaz;

import cl.bcs.contabilidad.domain.port.out.InterfazSapSource;
import cl.bcs.contabilidad.domain.port.out.InterfazSapSource.MovimientoBpc;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.List;
import java.util.Objects;

import static cl.bcs.contabilidad.infrastructure.interfaz.ArchivoPlano.ISO_8859_1;
import static cl.bcs.contabilidad.infrastructure.interfaz.ArchivoPlano.col;

/**
 * B1_BROAD_BPC.TXT and B1_CTRL_BROAD_BPC.TXT in the BPC directory (CONT-A-22, CONT-A-23, CONT-A-29, CONT-A-36).
 *
 * <p>Legacy: {@code CreaMovimientoBPC.logica()} (BcsContabilidadServicios). The header lists 15 labels that do not
 * match the 14 detail columns (kept). The control sums debe and haber from {@code new BigDecimal(0)}; its timestamp
 * is the data file's write time. The file names are a parameter ({@link ArchivosInterfaz}); CreaInterfazDemanda used
 * {@code *_BPC_DM.TXT}. Run-log lines: CreaMovimientoBPC.java:98, :156, :163, :177, :247, :251.
 */
public final class MovimientoBpcGenerator implements InterfazGenerator {

    public static final String ARCHIVO = "B1_BROAD_BPC.TXT";
    public static final String ARCHIVO_CONTROL = "B1_CTRL_BROAD_BPC.TXT";
    /** The names CreaInterfazDiaria used. */
    public static final ArchivosInterfaz ARCHIVOS = new ArchivosInterfaz(ARCHIVO, ARCHIVO_CONTROL);

    static final String CABECERA = "SOCIEDAD CUENTA-DE-MAYOR VARIANTE-DE-EJERCICIO EJERCICIO-PERIODO "
            + "NRO-DOCUMENTO-CONTABLE NRO-APUNTE-CONTABLE SEGMENTO LEDGER SOC-GL-ASOCIADA TOTAL-CONTAB-DEBE "
            + "TOTAL-CONTB-HABER CUENTA-OPERATIVA CLASE-DE-REGISTRO TIPO-CUENTA INDICADOR-ANULACION \n";

    static final String CABECERA_CONTROL = "TIP-RUTINA DES-RUTINA DES-SUBRUTINA DES-ESTRUCUTRA "
            + "DES-CAMPOVALIDAR-1 POS-INICIAL-1 POS-FINAL-1 MTO-SUMCAMPO-1 DES-CAMPOVALIDAR-2 POS-INICIAL-2 "
            + "POS-FINAL-2 MTO-SUMCAMPO-2 MTO-SUMCTAREG FEC-PROCESO HRA-PROCESO \n";

    private final InterfazSapSource sap;
    private final Clock clock;
    private final ArchivosInterfaz archivos;

    public MovimientoBpcGenerator(InterfazSapSource sap, Clock clock) {
        this(sap, clock, ARCHIVOS);
    }

    public MovimientoBpcGenerator(InterfazSapSource sap, Clock clock, ArchivosInterfaz archivos) {
        this.sap = Objects.requireNonNull(sap, "sap");
        this.clock = Objects.requireNonNull(clock, "clock");
        this.archivos = Objects.requireNonNull(archivos, "archivos");
    }

    @Override
    public void generar(InterfazContext contexto) {
        List<MovimientoBpc> filas = sap.movimientosBpc(contexto.fechaEjecucion());
        InterfazBitacora bitacora = contexto.bitacora();
        String archivo = archivos.datos();

        bitacora.info(ArchivoPlano.creando(archivo));
        StringBuilder datos = new StringBuilder(CABECERA);
        BigDecimal debe = new BigDecimal(0);
        BigDecimal haber = new BigDecimal(0);
        for (MovimientoBpc m : ArchivoPlano.invertido(filas)) {
            datos.append(col(m.codigoSociedad(), 4))
                    .append(col(m.cuentaMayor(), 10))
                    .append(col(m.varianteDeEjercicio(), 2))
                    .append(col(m.ejercicioPeriodo(), 7))
                    .append(col(m.nroDocumentoContable(), 10))
                    .append(col(m.nroApunteContable(), 3))
                    .append(col(m.segmento(), 2))
                    .append(col(m.ledger(), 2))
                    .append(col(m.socGlAsociada(), 4))
                    .append(col(m.moneda(), 5))
                    .append(col(m.totalContabDebe(), 20))
                    .append(col(m.totalContaHaber(), 20))
                    .append(col(m.claseDeRegistro(), 1))
                    .append(col(m.indicadorAnulacion(), 1))
                    .append('\n');
            debe = ArchivoPlano.sumar(debe, m.totalContabDebe(), archivo, "TotalContabDebe");
            haber = ArchivoPlano.sumar(haber, m.totalContaHaber(), archivo, "TotalContaHaber");
        }
        ArchivoPlano.escribir(contexto.directorioBpc().resolve(archivo), datos.toString(), ISO_8859_1,
                bitacora, ArchivoPlano.finDatos(archivo));

        bitacora.info(ArchivoPlano.creando(archivos.control()));
        MarcaProceso marca = MarcaProceso.de(clock.instant(), clock.getZone());
        String control = CABECERA_CONTROL
                + col("C", 1)
                + col("SCRIPT", 30)
                + col("", 30)
                + col(archivo, 30)
                + col("TOTAL-CONTAB-DEBE", 20)
                + col("", 3)
                + col("", 3)
                + col(debe.toString(), 20)
                + col("TOTAL-CONTAB-HABER", 20)
                + col("", 3)
                + col("", 3)
                + col(haber.toString(), 20)
                + col(String.valueOf(filas.size()), 15)
                + marca.fecProceso()
                + marca.hraProceso();
        // CreaMovimientoBPC.java:247 is the only control "Fin" line with ": "
        ArchivoPlano.escribir(contexto.directorioBpc().resolve(archivos.control()), control, ISO_8859_1,
                bitacora, ArchivoPlano.finDatos(archivos.control()));
    }
}
