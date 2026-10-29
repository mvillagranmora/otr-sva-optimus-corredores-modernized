package cl.bcs.contabilidad.infrastructure.interfaz;

import cl.bcs.contabilidad.domain.port.out.InterfazSapSource;
import cl.bcs.contabilidad.domain.port.out.InterfazSapSource.CuentaBpc;

import java.time.Clock;
import java.util.List;
import java.util.Objects;

import static cl.bcs.contabilidad.infrastructure.interfaz.ArchivoPlano.ISO_8859_1;
import static cl.bcs.contabilidad.infrastructure.interfaz.ArchivoPlano.col;

/**
 * B1_CUENTAS_BPC.TXT and B1_CTRL_CUENTAS_BPC.TXT in the BPC directory (CONT-A-20, CONT-A-21, CONT-A-36).
 *
 * <p>Legacy: {@code CreaCuentasBPC.logica()} (BcsContabilidadServicios). The query takes no fecha; the control
 * timestamp is the data file's write time. The file names are a parameter ({@link ArchivosInterfaz});
 * CreaInterfazDemanda used {@code *_BPC_DM.TXT}. Run-log lines: CreaCuentasBPC.java:86, :132, :138, :154, :199, :202.
 */
public final class CuentasBpcGenerator implements InterfazGenerator {

    public static final String ARCHIVO = "B1_CUENTAS_BPC.TXT";
    public static final String ARCHIVO_CONTROL = "B1_CTRL_CUENTAS_BPC.TXT";
    /** The names CreaInterfazDiaria used. */
    public static final ArchivosInterfaz ARCHIVOS = new ArchivosInterfaz(ARCHIVO, ARCHIVO_CONTROL);

    static final String CABECERA = "CODIGO-SOCIEDAD CUENTA-OPERATIVA DESCRIPCION TIPO-CUENTA CUENTA-GRUPO \n";

    static final String CABECERA_CONTROL =
            "TIP-RUTINA DES-RUTINA DES-SUBRUTINA DES-ESTRUCUTRA MTO-SUMCTAREG FEC-PROCESO HRA-PROCESO \n";

    private final InterfazSapSource sap;
    private final Clock clock;
    private final ArchivosInterfaz archivos;

    public CuentasBpcGenerator(InterfazSapSource sap, Clock clock) {
        this(sap, clock, ARCHIVOS);
    }

    public CuentasBpcGenerator(InterfazSapSource sap, Clock clock, ArchivosInterfaz archivos) {
        this.sap = Objects.requireNonNull(sap, "sap");
        this.clock = Objects.requireNonNull(clock, "clock");
        this.archivos = Objects.requireNonNull(archivos, "archivos");
    }

    @Override
    public void generar(InterfazContext contexto) {
        List<CuentaBpc> filas = sap.cuentasBpc();
        InterfazBitacora bitacora = contexto.bitacora();
        String archivo = archivos.datos();

        bitacora.info(ArchivoPlano.creando(archivo));
        StringBuilder datos = new StringBuilder(CABECERA);
        for (CuentaBpc c : ArchivoPlano.invertido(filas)) {
            datos.append(col(c.codigoSociedad(), 4))
                    .append(col(c.cuentaOperativa(), 10))
                    .append(col(c.descripcion(), 60))
                    .append(col(c.tipoCuenta(), 3))
                    .append(col(c.cuentaGrupo(), 10))
                    .append('\n');
        }
        ArchivoPlano.escribir(contexto.directorioBpc().resolve(archivo), datos.toString(), ISO_8859_1,
                bitacora, ArchivoPlano.finDatos(archivo));

        bitacora.info(ArchivoPlano.creando(archivos.control()));
        MarcaProceso marca = MarcaProceso.de(clock.instant(), clock.getZone());
        String control = CABECERA_CONTROL
                + col("C", 1)
                + col("SCRIPT", 30)
                + col("CONSULTACUENTABPC", 30)
                + col(archivo, 30)
                + col(String.valueOf(filas.size()), 15)
                + marca.fecProceso()
                + marca.hraProceso();
        ArchivoPlano.escribir(contexto.directorioBpc().resolve(archivos.control()), control, ISO_8859_1,
                bitacora, ArchivoPlano.finControlSinSeparador(archivos.control()));
    }
}
