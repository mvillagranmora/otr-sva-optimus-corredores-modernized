package cl.bcs.contabilidad.infrastructure.interfaz;

import cl.bcs.contabilidad.domain.port.out.InterfazSapSource;
import cl.bcs.contabilidad.domain.port.out.InterfazSapSource.CentroCosto;
import cl.bcs.contabilidad.domain.port.out.InterfazSapSource.Entidad;
import cl.bcs.contabilidad.domain.port.out.InterfazSapSource.TipoCuenta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.time.Clock;
import java.util.Objects;
import java.util.function.Supplier;

import static cl.bcs.contabilidad.infrastructure.interfaz.ArchivoPlano.ISO_8859_1;
import static cl.bcs.contabilidad.infrastructure.interfaz.ArchivoPlano.col;

/**
 * B1_DES_ENTIDAD.TXT, B1_DES_TIPO_CTA.TXT and B1_DES_CC_PROD.TXT, without control files (CONT-A-16..19).
 *
 * <p>Legacy: {@code CreaDescriptivasBroad.logicaEntidad/logicaTipoCuenta/logicaCentroCosto}
 * (BcsContabilidadServicios). Each file is isolated: any failure, including its query, is logged and the next file
 * is still produced, so this generator never throws. B1_DES_CC_PROD.TXT is written with the platform default
 * charset, as the legacy {@code FileUtils.writeStringToFile(file, text, true)} did (kept, flagged for review).
 * Each file reports "Creando Archivo" / "Fin de creacion de Archivo: " or "ERR" to the run log
 * ({@link InterfazContext#bitacora()}), as CreaDescriptivasBroad.java:139-173, :193-224 and :243-276 did.
 */
public final class DescriptivasBroadGenerator implements InterfazGenerator {

    public static final String ARCHIVO_ENTIDAD = "B1_DES_ENTIDAD.TXT";
    public static final String ARCHIVO_TIPO_CUENTA = "B1_DES_TIPO_CTA.TXT";
    public static final String ARCHIVO_CENTRO_COSTO = "B1_DES_CC_PROD.TXT";

    private static final Logger log = LoggerFactory.getLogger(DescriptivasBroadGenerator.class);

    private final InterfazSapSource sap;

    /** The clock keeps the generator signature uniform; these files carry no timestamp. */
    public DescriptivasBroadGenerator(InterfazSapSource sap, Clock clock) {
        this.sap = Objects.requireNonNull(sap, "sap");
        Objects.requireNonNull(clock, "clock");
    }

    @Override
    public void generar(InterfazContext contexto) {
        Path fi = contexto.directorioFi();
        InterfazBitacora bitacora = contexto.bitacora();
        archivo(fi.resolve(ARCHIVO_ENTIDAD), ISO_8859_1, this::entidad, bitacora);
        archivo(fi.resolve(ARCHIVO_TIPO_CUENTA), ISO_8859_1, this::tipoCuenta, bitacora);
        archivo(fi.resolve(ARCHIVO_CENTRO_COSTO), Charset.defaultCharset(), this::centroCosto, bitacora);
    }

    private String entidad() {
        StringBuilder sb = new StringBuilder("COD-ENTIDAD DESCRIPCION \n");
        for (Entidad e : ArchivoPlano.invertido(sap.entidades())) {
            sb.append(col(e.codEntidad(), 20)).append(col(e.descripcion(), 100)).append('\n');
        }
        return sb.toString();
    }

    private String tipoCuenta() {
        StringBuilder sb = new StringBuilder("TIPO-CUENTA DESCRIPCION \n");
        for (TipoCuenta t : ArchivoPlano.invertido(sap.tiposCuenta())) {
            sb.append(col(t.tipoCuenta(), 20)).append(col(t.descripcion(), 100)).append('\n');
        }
        return sb.toString();
    }

    private String centroCosto() {
        StringBuilder sb = new StringBuilder("COD-CC-PRD DESCRIPCION TIPO \n");
        for (CentroCosto c : ArchivoPlano.invertido(sap.centrosCosto())) {
            sb.append(col(c.codccPro(), 8)).append(col(c.descripcion(), 100)).append(col(c.tipo(), 3)).append('\n');
        }
        return sb.toString();
    }

    /**
     * One isolated file. Legacy order (CreaDescriptivasBroad.java:137-173, and the same for the other two):
     * "Creando Archivo" BEFORE the query, then query, write, "Fin de creacion de Archivo: "; any exception is logged
     * as {@code "ERR - " + e.getMessage()} and swallowed.
     */
    private static void archivo(Path destino, Charset charset, Supplier<String> contenido, InterfazBitacora bitacora) {
        String nombre = destino.getFileName().toString();
        try {
            bitacora.info(ArchivoPlano.creando(nombre));
            ArchivoPlano.anexar(destino, contenido.get(), charset);
            log.info("Interfaz file written: {}", nombre);
            bitacora.info(ArchivoPlano.finDatos(nombre));
        } catch (Exception e) {
            log.error("Cannot generate interfaz file {}: {}", nombre, e.toString());
            bitacora.error(e.getMessage());
        }
    }
}
