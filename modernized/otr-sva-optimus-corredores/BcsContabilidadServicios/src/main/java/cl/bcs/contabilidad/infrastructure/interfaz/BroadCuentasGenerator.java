package cl.bcs.contabilidad.infrastructure.interfaz;

import cl.bcs.contabilidad.domain.port.out.InterfazSapSource;
import cl.bcs.contabilidad.domain.port.out.InterfazSapSource.BroadCuenta;

import java.time.Clock;
import java.util.List;
import java.util.Objects;

import static cl.bcs.contabilidad.infrastructure.interfaz.ArchivoPlano.ISO_8859_1;
import static cl.bcs.contabilidad.infrastructure.interfaz.ArchivoPlano.col;

/**
 * B1_BROAD_CTAS.TXT and B1_CTRL_BROAD_CTAS.TXT (CONT-A-10, CONT-A-11, CONT-A-24..28, CONT-A-36).
 *
 * <p>Legacy: {@code CreaBroadCuentas.logica()} (BcsContabilidadServicios). The query takes no fecha. The control
 * timestamp is always the epoch because the legacy stats {@code <dir><ctrl name><data name>}, which never exists.
 * The file names are a parameter ({@link ArchivosInterfaz}); CreaInterfazDemanda used lower-case {@code .txt}.
 * Run-log lines: CreaBroadCuentas.java:83, :130, :138, :153, :201, :204.
 */
public final class BroadCuentasGenerator implements InterfazGenerator {

    public static final String ARCHIVO = "B1_BROAD_CTAS.TXT";
    public static final String ARCHIVO_CONTROL = "B1_CTRL_BROAD_CTAS.TXT";
    /** The names CreaInterfazDiaria used. */
    public static final ArchivosInterfaz ARCHIVOS = new ArchivosInterfaz(ARCHIVO, ARCHIVO_CONTROL);

    static final String CABECERA = "COD-ENTIDAD COD-CTA-CONTABLE DESCRIPCION TIPO-CUENTA CUENTA-GRUPO "
            + "COD-CUENTA-CONTABLE IND_CTA_CONFIDENCIAL IND_CTA-ASOCIADA IND-CTA-MONETARIA "
            + "IND_RELEVANTE_FLUJO-CAJA IND-CTA-AFILIADA\n";

    static final String CABECERA_CONTROL =
            "TIP-RUTINA DES-RUTINA DES-SUBRUTINA DES-ESTRUCUTRA MTO-SUMCTAREG FEC-PROCESO HRA-PROCESO \n";

    private final InterfazSapSource sap;
    private final Clock clock;
    private final ArchivosInterfaz archivos;

    public BroadCuentasGenerator(InterfazSapSource sap, Clock clock) {
        this(sap, clock, ARCHIVOS);
    }

    public BroadCuentasGenerator(InterfazSapSource sap, Clock clock, ArchivosInterfaz archivos) {
        this.sap = Objects.requireNonNull(sap, "sap");
        this.clock = Objects.requireNonNull(clock, "clock");
        this.archivos = Objects.requireNonNull(archivos, "archivos");
    }

    @Override
    public void generar(InterfazContext contexto) {
        List<BroadCuenta> filas = sap.cuentasBroad();
        InterfazBitacora bitacora = contexto.bitacora();

        bitacora.info(ArchivoPlano.creando(archivos.datos()));
        StringBuilder datos = new StringBuilder(CABECERA);
        for (BroadCuenta c : ArchivoPlano.invertido(filas)) {
            datos.append(col(c.codEntidad(), 7))
                    .append(col(c.codCtaContable(), 15))
                    .append(col(c.descripcion(), 100))
                    .append(col(c.tipoCuenta(), 20))
                    .append(col(c.cuentaGrupo(), 10))
                    .append(col(c.codMonedaContable(), 4))
                    .append(col(c.indCtaConfidencial(), 1))
                    .append(col(c.indCtaAsociada(), 1))
                    .append(col(c.indCtaMonetaria(), 1))
                    .append(col(c.indRelevanteFlujoCaja(), 1))
                    .append(col(c.indCtaAfiliada(), 1))
                    .append('\n');
        }
        ArchivoPlano.escribir(contexto.directorioFi().resolve(archivos.datos()), datos.toString(), ISO_8859_1,
                bitacora, ArchivoPlano.finDatos(archivos.datos()));

        bitacora.info(ArchivoPlano.creando(archivos.control()));
        MarcaProceso marca = MarcaProceso.epoca(clock.getZone());
        String control = CABECERA_CONTROL
                + col("C", 1)
                + col("SCRIPT", 30)
                + col("", 30)
                + col(archivos.datos(), 30)
                + col(String.valueOf(filas.size()), 15)
                + marca.fecProceso()
                + marca.hraProceso();
        ArchivoPlano.escribir(contexto.directorioFi().resolve(archivos.control()), control, ISO_8859_1,
                bitacora, ArchivoPlano.finControlSinSeparador(archivos.control()));
    }
}
