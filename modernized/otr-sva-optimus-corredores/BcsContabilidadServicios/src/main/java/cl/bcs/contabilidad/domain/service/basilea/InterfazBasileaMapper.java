package cl.bcs.contabilidad.domain.service.basilea;

import cl.bcs.contabilidad.domain.model.basilea.CuentaBasilea;
import cl.bcs.contabilidad.domain.model.basilea.DatoBusquedaBasilea;
import cl.bcs.contabilidad.domain.model.basilea.RegistroInterfazBasilea;

import java.util.List;
import java.util.Locale;
import java.util.function.Function;

/**
 * Pure rules of legacy {@code ProcesarInterfazBasilea}: CONT-D-60..70.
 * <ul>
 *   <li>{@link #buscarCuenta}: legacy {@code buscarCuenta} (ProcesarInterfazBasilea.java:347-368). For IdBolsa,
 *       EsVentaCorta, IdClasificacionTipoInstrumento, IdTipoInstrumento, IdEmisor and Nemotecnico, an empty position
 *       value requires an empty account value, and a non-empty one must be contained (case-sensitive) in the account's
 *       value, which may be a comma-separated list. Grupo, Producto, RelacionadoCorredora and IdTipoOperacion compare
 *       with equalsIgnoreCase.</li>
 *   <li>{@link #crearRegistro}: legacy {@code crearRegistroBasilea} (:370-447) plus the assignments of :329-333.</li>
 * </ul>
 * Where the legacy dereferenced a null value (NullPointerException), this class raises {@link InterfazBasileaException}
 * at the same point of the evaluation.
 */
public final class InterfazBasileaMapper {

    private InterfazBasileaMapper() {}

    // ------------------------------------------------------------------ account lookup

    public static ResolucionCuentaBasilea buscarCuenta(DatoBusquedaBasilea dato, List<CuentaBasilea> cuentas) {
        List<CuentaBasilea> coinciden = cuentas.stream().filter(c -> coincide(dato, c)).toList();
        if (coinciden.size() > 1) return ResolucionCuentaBasilea.MULTIPLE;
        if (coinciden.isEmpty() || coinciden.getFirst().cuentaContable() == null) {
            // :363-364 no match; :316 a single match with a null account is treated as "not found"
            return ResolucionCuentaBasilea.SIN_CUENTA;
        }
        return ResolucionCuentaBasilea.unica(coinciden.getFirst().cuentaContable());
    }

    /** :349-358, in the legacy filter order. */
    private static boolean coincide(DatoBusquedaBasilea d, CuentaBasilea c) {
        return contiene("IdBolsa", d.idBolsa(), c.idBolsa())
                && contiene("EsVentaCorta", d.esVentaCorta(), c.esVentaCorta())
                && igual("Grupo", d.grupo(), c.grupo())
                && igual("Producto", d.producto(), c.producto())
                && contiene("IdClasificacionTipoInstrumento", d.idClasificacionTipoInstrumento(),
                        c.idClasificacionTipoInstrumento())
                && contiene("IdTipoInstrumento", d.idTipoInstrumento(), c.idTipoInstrumento())
                && contiene("IdEmisor", d.idEmisor(), c.idEmisor())
                && contiene("Nemotecnico", d.nemotecnico(), c.nemotecnico())
                && igual("RelacionadoCorredora", d.relacionadoCorredora(), c.relacionadoCorredora())
                && igual("IdTipoOperacion", d.idTipoOperacion(), c.idTipoOperacion());
    }

    /** {@code dato.isEmpty() ? cuenta.equalsIgnoreCase(dato) : cuenta.contains(dato)}. */
    private static boolean contiene(String campo, String dato, String cuenta) {
        if (dato == null) throw nulo(campo, "la posición");
        if (cuenta == null) throw nulo(campo, "la cuenta Basilea");
        return dato.isEmpty() ? cuenta.isEmpty() : cuenta.contains(dato);
    }

    /** {@code cuenta.equalsIgnoreCase(dato)}: a null position value never matches. */
    private static boolean igual(String campo, String dato, String cuenta) {
        if (cuenta == null) throw nulo(campo, "la cuenta Basilea");
        return cuenta.equalsIgnoreCase(dato);
    }

    private static InterfazBasileaException nulo(String campo, String origen) {
        return new InterfazBasileaException("Valor nulo en " + campo + " de " + origen
                + " al buscar la cuenta Basilea");
    }

    // ------------------------------------------------------------------ record

    public static RegistroInterfazBasilea crearRegistro(DatoBusquedaBasilea dato, String fechaEnvio,
                                                        String cuentaContable, String rutCorredora) {
        String[] rut = partesRut(rutCorredora);
        RegistroInterfazBasilea.Builder rb = RegistroInterfazBasilea.builder()
                .fechaEnvio(fechaEnvio)
                .cuentaContable(cuentaContable)
                .rutCliente(rut[0])
                .digitoVerificador(rut[1])
                // :372-380 common fields
                .indicadorTipoFlujo("NATU")
                .indicadorReversa("N")
                .indicadorTipoVencimiento(1L)
                .indicadorFlujo("FK")
                .tipoTasaInteres(1L)
                .flujoMonedaOrigen(dato.monto())
                .flujoValorContable(dato.monto())
                .flujoValorFinal(dato.monto())
                .plazoFlujo(es("ACN", dato.grupo()) ? "" : dato.plazo());

        if (es("ACN", dato.grupo()) && es("ACCIONES", dato.producto())) {
            // :382-389
            rb.tipoProducto("AC")
                    .relacionContable(es("S", dato.esVentaCorta()) ? "PAC" : "ACT")
                    .codigoMoneda(es("USD", dato.idMonedaInstrumento()) ? 2L : 0L)
                    .codigoProducto("AC")
                    .codigoSubproducto("AC")
                    .claseEmision(dato.nemoBasilea())
                    .monedaFlujo(dato.idMonedaInstrumento());
        } else if ((es("GARANTIZADA", dato.grupo()) ^ es("LIBRE", dato.grupo())) && es("RF-IF", dato.producto())) {
            // :390-403
            rb.numeroOperacion(dato.folio())
                    .tasaInteresOperacion(dato.tasa())
                    .tipoProducto("RF")
                    .relacionContable("ACT")
                    .codigoProducto("RF")
                    .codigoSubproducto("RF")
                    .claseEmision(dato.emisor())
                    .fechaVencimientoFinal(dato.fechaVencimiento())
                    .fechaInicioOperacion(dato.fechaOperacion())
                    .fechaVencimientoFlujo(dato.fechaVencimiento())
                    .monedaFlujo(monedaFlujoPorReajuste(dato.idTipoReajuste()));
        } else if (es("PACTEADA", dato.grupo()) && es("RF-IF", dato.producto())) {
            // :404-422
            rb.numeroOperacion(dato.folio())
                    .tasaInteresOperacion(dato.tasa())
                    .tipoProducto("PC")
                    .relacionContable("PAS")
                    .codigoProducto("PC")
                    .codigoSubproducto("PC")
                    .claseEmision(dato.emisor())
                    .fechaVencimientoFinal(dato.fechaVencimientoOperacion())
                    .fechaInicioOperacion(dato.fechaLiquidacion())
                    .fechaVencimientoFlujo(dato.fechaVencimiento())
                    .codigoMoneda(codigoMoneda(dato.idTipoReajuste()))
                    .monedaFlujo(monedaFlujoPorReajuste(dato.idTipoReajuste()));
        } else if (es("FORWARD", dato.producto())) {
            // :423-444
            boolean obligacion = es("OBLIG", dato.grupo());
            Function<DatoBusquedaBasilea, String> moneda = obligacion
                    ? DatoBusquedaBasilea::idMonedaSecundaria : DatoBusquedaBasilea::idMonedaPrincipal;
            rb.tipoProducto("FW")
                    .relacionContable(obligacion ? "PAS" : "ACT")
                    .codigoProducto("FW")
                    .codigoSubproducto("FW")
                    .fechaVencimientoFinal(dato.fechaVencimiento())
                    .fechaInicioOperacion(dato.fechaOperacion())
                    .fechaVencimientoFlujo(dato.fechaVencimiento())
                    .numeroOperacion(dato.folio())
                    .tasaInteresOperacion(dato.tasa())
                    .codigoMoneda(obligacion || es("DERECHO", dato.grupo()) ? codigoMoneda(moneda.apply(dato)) : 0L)
                    .monedaFlujo(moneda.apply(dato));
        }
        return rb.build();
    }

    /** CLP 0, UF 1, USD 2, EUR 3, anything else 0 (:415-419, 433-442). */
    private static long codigoMoneda(String moneda) {
        return switch (moneda == null ? "" : moneda.toUpperCase(Locale.ROOT)) {
            case "UF" -> 1L;
            case "USD" -> 2L;
            case "EUR" -> 3L;
            default -> 0L;
        };
    }

    /** {@code UF ^ NR ? "CLP" : IdTipoReajuste} (:401-403, 420-422). */
    private static String monedaFlujoPorReajuste(String reajuste) {
        return es("UF", reajuste) || es("NR", reajuste) ? "CLP" : reajuste;
    }

    private static boolean es(String literal, String valor) {
        return literal.equalsIgnoreCase(valor);
    }

    /**
     * :332-333 {@code String.format("%08d", Long.valueOf(rut.split("-")[0]))} and {@code rut.split("-")[1]}. A RUT that
     * the legacy could not split or parse is a controlled error instead of an exception escaping the service.
     */
    private static String[] partesRut(String rut) {
        String[] partes = rut == null ? new String[0] : rut.split("-");
        try {
            if (partes.length < 2) throw new NumberFormatException();
            return new String[]{String.format(Locale.ROOT, "%08d", Long.valueOf(partes[0])), partes[1]};
        } catch (NumberFormatException e) {
            throw new InterfazBasileaException("RUT de la cuenta propia con formato inválido: " + rut);
        }
    }
}
