package cl.bcs.cartolas.service;

import cl.bcs.cartolas.domain.Cartola;
import cl.bcs.cartolas.domain.Config;
import cl.bcs.cartolas.domain.Datos;
import cl.bcs.cartolas.port.in.CartolasUseCase;
import cl.bcs.cartolas.port.out.CartolaPort;
import cl.bcs.cartolas.port.out.ConfigPort;
import cl.bcs.cartolas.port.out.DatosPort;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Servicio principal del dominio de cartolas.
 * Orquesta consultas y resolución de nombres de archivo.
 */
@Service
public class CartolasService implements CartolasUseCase {

    private final CartolaPort cartolaPort;
    private final ConfigPort  configPort;
    private final DatosPort   datosPort;

    public CartolasService(final CartolaPort cartolaPort,
                           final ConfigPort configPort,
                           final DatosPort datosPort) {
        this.cartolaPort = Objects.requireNonNull(cartolaPort, "cartolaPort es requerido");
        this.configPort  = Objects.requireNonNull(configPort,  "configPort es requerido");
        this.datosPort   = Objects.requireNonNull(datosPort,   "datosPort es requerido");
    }

    @Override
    public Optional<Cartola> getCartola(final long idCartola) {
        return cartolaPort.findById(idCartola);
    }

    @Override
    public List<Cartola> getCartolasByNomina(final long idNomina, final boolean soloReproceso) {
        return cartolaPort.findByIdNomina(idNomina, soloReproceso);
    }

    @Override
    public List<Config> getConfig(final long idEmpresa) {
        return configPort.findByIdEmpresa(idEmpresa);
    }

    @Override
    public List<Datos> getDatos(final long idCartola) {
        return datosPort.findByIdCartola(idCartola);
    }

    // -------------------------------------------------------------------------
    // Resolución de nombres de archivo — pure-logic (public static)
    // Equivalente a ResolvedorNombre del legacy
    // -------------------------------------------------------------------------

    /**
     * Extrae las claves {@code {CLAVE}} de un patrón de nombre.
     * Pure-logic: sin efectos secundarios.
     */
    public static Set<String> extractKeys(final String patron) {
        Objects.requireNonNull(patron, "patron es requerido");
        final Set<String> keys = new HashSet<>();
        int x = 0;
        while (x < patron.length()) {
            final int open = patron.indexOf('{', x);
            if (open < 0) break;
            final int close = patron.indexOf('}', open);
            if (close > open) {
                keys.add(patron.substring(open + 1, close));
                x = close + 1;
            } else {
                x = open + 1;
            }
        }
        return keys;
    }

    /**
     * Resuelve un patrón de nombre de archivo sustituyendo claves por valores del contexto.
     * Pure-logic: sin efectos secundarios.
     *
     * <p>Claves disponibles:
     * <ul>
     *   <li>{@code YYYYMMDD}          — fecha actual</li>
     *   <li>{@code YYYYMMDDHHMMSS}    — fecha y hora actuales</li>
     *   <li>{@code NombreEmpresa}     — del contexto</li>
     *   <li>{@code DocumentoCliente}  — del contexto</li>
     *   <li>{@code CodigoPortafolio}  — del contexto</li>
     *   <li>{@code IdPortafolio}      — del contexto</li>
     *   <li>{@code IdCliente}         — del contexto</li>
     * </ul>
     * </p>
     */
    public static String resolverNombre(final String patron,
                                         final Map<String, String> contexto,
                                         final Date ahora) {
        Objects.requireNonNull(patron,   "patron es requerido");
        Objects.requireNonNull(contexto, "contexto es requerido");
        Objects.requireNonNull(ahora,    "ahora es requerido");

        String result = patron;
        final DateFormat dfDate     = new SimpleDateFormat("yyyyMMdd");
        final DateFormat dfDateHora = new SimpleDateFormat("yyyyMMddHHmmss");

        for (final String key : extractKeys(patron)) {
            final String valor;
            switch (key) {
                case "YYYYMMDD"       -> valor = dfDate.format(ahora);
                case "YYYYMMDDHHMMSS" -> valor = dfDateHora.format(ahora);
                default               -> valor = contexto.getOrDefault(key, "");
            }
            result = result.replace("{" + key + "}", valor);
        }
        return result;
    }

    @Override
    public String resolverNombreArchivoCartola(final String patron,
                                                final Map<String, String> contexto) {
        Objects.requireNonNull(patron,   "patron es requerido");
        Objects.requireNonNull(contexto, "contexto es requerido");
        return resolverNombre(patron, contexto, new Date()) + ".pdf";
    }

    @Override
    public String resolverNombreArchivoNomina(final String patron,
                                               final Map<String, String> contexto) {
        Objects.requireNonNull(patron,   "patron es requerido");
        Objects.requireNonNull(contexto, "contexto es requerido");
        return resolverNombre(patron, contexto, new Date()) + ".txt";
    }

    @Override
    public String resolverNombreArchivoZip(final String patron,
                                            final Map<String, String> contexto) {
        Objects.requireNonNull(patron,   "patron es requerido");
        Objects.requireNonNull(contexto, "contexto es requerido");
        return resolverNombre(patron, contexto, new Date()) + ".zip";
    }
}
