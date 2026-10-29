package cl.bcs.contabilidad.infrastructure.interfaz;

import cl.bcs.contabilidad.domain.port.out.InterfazSapSource;
import cl.bcs.contabilidad.domain.port.out.InterfazSapSource.BroadSaldo;

import java.time.Clock;
import java.util.List;
import java.util.Objects;

import static cl.bcs.contabilidad.infrastructure.interfaz.ArchivoPlano.ISO_8859_1;
import static cl.bcs.contabilidad.infrastructure.interfaz.ArchivoPlano.col;
import static cl.bcs.contabilidad.infrastructure.interfaz.ArchivoPlano.fecha;

/**
 * B1_BROAD_SALD_CONT.TXT and B1_CTRL_BROAD_SALD_CONT.TXT (CONT-A-04, CONT-A-14, CONT-A-15, CONT-A-28).
 *
 * <p>Legacy: {@code CreaBroadSaldo.logica()} (BcsContabilidadServicios). NUM-REGISTRO carries the contador. The
 * control never carries a sum (legacy {@code setMtoSumcampo("")}) and its timestamp is the data file's write time.
 * The file names are a parameter ({@link ArchivosInterfaz}). Run-log lines: CreaBroadSaldo.java:97, :151, :157,
 * :172, :229, :232.
 */
public final class BroadSaldoGenerator implements InterfazGenerator {

    public static final String ARCHIVO = "B1_BROAD_SALD_CONT.TXT";
    public static final String ARCHIVO_CONTROL = "B1_CTRL_BROAD_SALD_CONT.TXT";
    /** The names CreaInterfazDiaria and CreaInterfazDemanda used. */
    public static final ArchivosInterfaz ARCHIVOS = new ArchivosInterfaz(ARCHIVO, ARCHIVO_CONTROL);

    static final String CABECERA = "COD-INSTITUCION COD-APLICATIVO COD-ENTIDAD FEC-DIA NUM-REGISTRO "
            + "COD-CTA-CONTABLE COD-TIPO-CUENTA COD-PRODUCTO-CONTABLE COD-CENTRO-COSTOS COD-MONEDA-CONTABLE "
            + "MTO-SALDO-CONTABLE FILLER-01FILLER-02\n";

    static final String CABECERA_CONTROL = "TIP-RUTINA DES-RUTINA DES-SUBRUTINA DES-ESTRUCUTRA "
            + "DES-CAMPOVALIDAR POS-INICIAL POS-FINAL MTO-SUMCAMPO MTO-SUMCTAREG FEC-PROCESO HRA-PROCESO \n";

    private final InterfazSapSource sap;
    private final Clock clock;
    private final ArchivosInterfaz archivos;

    public BroadSaldoGenerator(InterfazSapSource sap, Clock clock) {
        this(sap, clock, ARCHIVOS);
    }

    public BroadSaldoGenerator(InterfazSapSource sap, Clock clock, ArchivosInterfaz archivos) {
        this.sap = Objects.requireNonNull(sap, "sap");
        this.clock = Objects.requireNonNull(clock, "clock");
        this.archivos = Objects.requireNonNull(archivos, "archivos");
    }

    @Override
    public void generar(InterfazContext contexto) {
        List<BroadSaldo> filas = sap.saldosBroad(contexto.fechaEjecucion());
        InterfazBitacora bitacora = contexto.bitacora();
        String archivo = archivos.datos();

        bitacora.info(ArchivoPlano.creando(archivo));
        StringBuilder datos = new StringBuilder(CABECERA);
        for (BroadSaldo s : ArchivoPlano.invertido(filas)) {
            datos.append(col(s.codInstitucion(), 1))
                    .append(col(s.codAplicativo(), 2))
                    .append(col(s.codEntidad(), 7))
                    .append(col(fecha(s.fecDia(), archivo, "FecDia"), 8))
                    .append(col(contexto.contador(), 10))
                    .append(col(s.codCtaContable(), 20))
                    .append(col(s.codTipoCuenta(), 20))
                    .append(col(s.codProductoContable(), 8))
                    .append(col(s.codCentroCosto(), 8))
                    .append(col(s.codMonedaContable(), 4))
                    .append(col(s.montoSaldoContable(), 20))
                    .append(col(s.filler01(), 6))
                    .append(col(s.filler02(), 6))
                    .append('\n');
        }
        ArchivoPlano.escribir(contexto.directorioFi().resolve(archivo), datos.toString(), ISO_8859_1,
                bitacora, ArchivoPlano.finDatos(archivo));

        bitacora.info(ArchivoPlano.creando(archivos.control()));
        MarcaProceso marca = MarcaProceso.de(clock.instant(), clock.getZone());
        String control = CABECERA_CONTROL
                + col("C", 1)
                + col("SCRIPT", 30)
                + col("CONSULTABROADSALDO", 30)
                + col(archivo, 30)
                + "MTO-SALDO-CONTABLE"
                + "089"
                + "108"
                + col(String.valueOf(filas.size()), 15)
                + marca.fecProceso()
                + marca.hraProceso();
        ArchivoPlano.escribir(contexto.directorioFi().resolve(archivos.control()), control, ISO_8859_1,
                bitacora, ArchivoPlano.finControlSinSeparador(archivos.control()));
    }
}
