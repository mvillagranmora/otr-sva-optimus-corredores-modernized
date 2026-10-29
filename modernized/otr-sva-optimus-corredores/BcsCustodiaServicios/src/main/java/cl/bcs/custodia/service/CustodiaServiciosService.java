package cl.bcs.custodia.service;

import cl.bcs.custodia.domain.PosicionCustodia;
import cl.bcs.custodia.port.in.CustodiaServiciosUseCase;
import cl.bcs.custodia.port.out.CustodiaServiciosDataPort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Pure-logic service for Custodia Servicios.
 * All business-logic methods are public static for testability without Spring context.
 *
 * Legacy sources:
 *   - LiquidarCustodiaEmprestito.java  (cuentaDias, calculoInteres)
 *   - ControlRetiroCustodia.java        (getHashBusqueda)
 *   - GuardarPatrimonioRetroactivo.java (obtenerRangoDeFechas)
 *   - AnticiparPrestamo.java            (TIPO_PRESTAMO_VENTA_CORTA)
 *   - ConsultarCustodiaPortafolio.java  (TIPO_BUSQUEDAS)
 */
@Service
public class CustodiaServiciosService implements CustodiaServiciosUseCase {

    private final CustodiaServiciosDataPort dataPort;

    public CustodiaServiciosService(CustodiaServiciosDataPort dataPort) {
        this.dataPort = dataPort;
    }

    // -----------------------------------------------------------------------
    // Inbound port implementation (H-3: driving-side contract)
    // -----------------------------------------------------------------------

    @Override
    public String obtenerFechaSistema(Long idEmpresa) {
        return dataPort.obtenerFechaSistema(idEmpresa);
    }

    @Override
    public List<PosicionCustodia> consultarPosicion(Long idPortafolio) {
        return dataPort.consultarPosicion(idPortafolio);
    }

    // -----------------------------------------------------------------------
    // Constants
    // -----------------------------------------------------------------------

    /**
     * Tipo préstamo para venta corta.
     * Legacy: AnticiparPrestamo.java
     */
    public static final String TIPO_PRESTAMO_VENTA_CORTA = "PRTVTC";

    /**
     * Map of blockchain error message strings to BC_NNN error codes.
     * Legacy: ConstantesCustodia.java (getCodigoErroresBlockChain).
     *
     * NOTE: The legacy code puts 41 entries but the key "Missing field: Arbitrator1"
     * appears twice (mapped to both BC_518 and BC_529). HashMap retains the last
     * written value (BC_529), so the runtime size is 40, not 41.
     * This map is intentionally unmodifiable to make the constant safe as a shared static.
     */
    public static final Map<String, String> BLOCKCHAIN_ERROR_CODES;
    static {
        Map<String, String> m = new HashMap<>();
        m.put("Add Lending Client not on Schedule",                                                  "BC_501");
        m.put("Create Lending Agreement not on Schedule",                                            "BC_502");
        m.put("Trying to access an Lending Agreement that doesnt exist yet",                         "BC_503");
        m.put("timeout expired while executing transaction",                                         "BC_504");
        m.put("Proposed data is invalid Symbol",                                                     "BC_505");
        m.put("Proposed data is invalid BorrowingBrokerageHouseID",                                  "BC_506");
        m.put("Proposed data is invalid LoanType",                                                   "BC_507");
        m.put("Lending Client already exists in Storage",                                            "BC_508");
        m.put("New clients Lending Quantity surpasses set total quantity for Lending Agreement",      "BC_509");
        m.put("Requesting Brokerage House ID does not match Borrower",                               "BC_510");
        m.put("Matching Route not found",                                                            "BC_511");
        m.put("Lending Qty surpasses Loans Qty To Be Assigned",                                      "BC_512");
        m.put("MSA not found or not Approved for the Borrowing Client",                              "BC_513");
        m.put("Requesting Brokerage House ID does not match Lender",                                 "BC_514");
        m.put("Lending Agreement not on the allowed date to modify.",                                "BC_515");
        m.put("Collateral is Mandatory for non Related Third clients.",                              "BC_516");
        m.put("No matching Short Sale found for data",                                               "BC_517");
        m.put("Missing field: Arbitrator1",                                                          "BC_518");  // overwritten below by BC_529 (duplicate key in legacy)
        m.put("Lending Agreement not on Schedule for Termination",                                   "BC_519");
        m.put("Successfully sent invoke transaction to the orderer.",                                "BC_520");
        m.put("Invalid Symbol for this type of Lending Agreement",                                   "BC_521");
        m.put("is NOT multiple of LAN minimum cut",                                                  "BC_522");
        m.put("No matching Loan found for data",                                                     "BC_523");
        m.put("Proposed data is invalid ClientRUT, ",                                                "BC_524");
        m.put("Missing field: ClientAddress",                                                        "BC_525");
        m.put("Missing field: ClientPhone",                                                          "BC_526");
        m.put("Proposed data is invalid Email, ",                                                    "BC_527");
        m.put("Missing field: ClientName",                                                           "BC_528");
        m.put("Missing field: Arbitrator1",                                                          "BC_529");  // duplicate key — overwrites BC_518; oracle: legacy HashMap behavior
        m.put("Missing field: Arbitrator2",                                                          "BC_530");
        m.put("Successfully Added Lending Client",                                                   "BC_531");
        m.put("Lending Agreement not on the allowed date to modify",                                 "BC_532");
        m.put("New client's Lending Quantity surpasses set total quantity for Lending Agreement",     "BC_533");
        m.put("Lending Clients not on Schedule for modification",                                    "BC_534");
        m.put("Collateral is Mandatory for this LendingAgreementType",                               "BC_535");
        m.put("Lending Agreement not on Schedule for modification",                                  "BC_536");
        m.put("Yield is not valid, value must be between 0 and 100",                                "BC_537");
        m.put("CREATED",                                                                             "BC_538");
        m.put("ACTIVE",                                                                              "BC_539");
        m.put("TERMINATED",                                                                          "BC_540");
        m.put("MATURED",                                                                             "BC_541");
        BLOCKCHAIN_ERROR_CODES = Collections.unmodifiableMap(m);
    }

    /**
     * Tipos de búsqueda de custodia por portafolio.
     * Legacy: ConsultarCustodiaPortafolio.java
     */
    public static final List<String> TIPO_BUSQUEDAS = List.of(
            "CUS_NEG", "CUS", "VENPAC", "COMPAC", "SIM", "PRT_VC_CON_CUS",
            "PRT_VC_SIN_CUS", "PRT_AD_FN", "EMP", "EMP_EXT", "GTA", "TRA",
            "TRANOM", "REP", "SUB");

    // -----------------------------------------------------------------------
    // Pure-logic methods
    // -----------------------------------------------------------------------

    /**
     * Cuenta días entre dos fechas en formato yyyyMMdd.
     *
     * Legacy: LiquidarCustodiaEmprestito.java
     *
     * NOTE: The inner loop contains a known legacy bug: {@code ld = ld.plusDays(asd)}
     * instead of {@code ld.plusDays(1)}. This causes non-linear date progression
     * and is preserved here for behavioral equivalence.
     *
     * @param d1   start date (yyyyMMdd)
     * @param d2   end date   (yyyyMMdd)
     * @param tipo "C" for calendar days, "L" for working days (BUGGY)
     * @return number of days according to tipo
     */
    public static Long cuentaDias(String d1, String d2, String tipo) {
        LocalDate ld = LocalDate.parse(d1, DateTimeFormatter.ofPattern("yyyyMMdd"));
        LocalDate ld2 = LocalDate.parse(d2, DateTimeFormatter.ofPattern("yyyyMMdd"));
        // H-2 fix: use ChronoUnit.DAYS.between() to correctly handle cross-year ranges.
        // getDayOfYear() subtraction silently returned 0 for any cross-year pair.
        long diasDiferenciaCompleto = ChronoUnit.DAYS.between(ld, ld2);
        int diasDiferenciaLaboral = 0;
        for (int asd = 0; asd < diasDiferenciaCompleto; asd++) {
            ld = ld.plusDays(asd);  // PRESERVE THIS BUG — legacy behavioral equivalence
            if (ld.getDayOfWeek() != DayOfWeek.SATURDAY && ld.getDayOfWeek() != DayOfWeek.SUNDAY) {
                diasDiferenciaLaboral++;
            }
        }
        if (tipo.equalsIgnoreCase("L")) return (long) diasDiferenciaLaboral;
        else return diasDiferenciaCompleto;
    }

    /**
     * Calcula interés simple sobre un monto.
     *
     * Legacy: LiquidarCustodiaEmprestito.java
     *
     * NOTE: Uses {@link BigDecimal#divide(BigDecimal)} without RoundingMode.
     * This may throw ArithmeticException on non-terminating decimals — preserved
     * as legacy behavior.
     *
     * @param monto  capital amount
     * @param plazo  term (days)
     * @param tasa   annual rate (e.g. 5 for 5%)
     * @param base   day-count base (e.g. 360)
     * @return interest amount = monto * (tasa/100) * (plazo/base)
     */
    public static BigDecimal calculoInteres(BigDecimal monto, BigDecimal plazo, BigDecimal tasa, BigDecimal base) {
        BigDecimal tasaDivCien = tasa.divide(new BigDecimal(100));
        BigDecimal plazoDivBase = plazo.divide(base);
        BigDecimal total = monto.multiply(BigDecimal.ONE.add(tasaDivCien.multiply(plazoDivBase)));
        return total.subtract(monto);
    }

    /**
     * Constructs a composite search key for custodian position retrieval.
     *
     * Legacy: ControlRetiroCustodia.java
     *
     * @param idInstrumento  instrument ID
     * @param idPortafolio   portfolio ID
     * @param idCliente      client ID
     * @param nemosvs        nemotécnico SVS
     * @return hash string in format "idInstrumento-idPortafolio-idCliente-nemosvs"
     */
    public static String getHashBusqueda(Long idInstrumento, Long idPortafolio, Long idCliente, String nemosvs) {
        return "" + idInstrumento + "-" + idPortafolio + "-" + idCliente + "-" + nemosvs;
    }

    /**
     * Returns an inclusive list of dates between fechaInicio and fechaTermino (yyyy-MM-dd).
     *
     * Legacy: GuardarPatrimonioRetroactivo.java
     *
     * @param fechaInicio  start date (yyyy-MM-dd), inclusive
     * @param fechaTermino end date   (yyyy-MM-dd), inclusive
     * @return ordered list of dates in yyyy-MM-dd format
     */
    public static List<String> obtenerRangoDeFechas(String fechaInicio, String fechaTermino) {
        List<String> listaFechas = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate inicio = LocalDate.parse(fechaInicio, formatter);
        LocalDate termino = LocalDate.parse(fechaTermino, formatter);
        while (!inicio.isAfter(termino)) {
            listaFechas.add(inicio.format(formatter));
            inicio = inicio.plusDays(1);
        }
        return listaFechas;
    }

    /**
     * Computes the new nemotécnico for coupon-payment symbol transformations.
     *
     * Logic migrated verbatim from legacy DeterminaNuevoNemo.NuevoNemo(),
     * which was itself migrated from COBOL LMRUCANE.SRC.
     *
     * Rules (in priority order — first match wins):
     *   "**" in nemo  → replace "**" with " *"   (e.g. ABNA12**02 → ABNA12 *02)
     *   " *" in nemo  → replace " *" with "01"   (e.g. ABNA12 *02 → ABNA120102)
     *   "&&" in nemo  → replace "&&" with " &"   (e.g. ABNA12&&13 → ABNA12 &13)
     *   " &" in nemo  → extract mesNemo from chars[8..9]; compare to mesProceso:
     *                     mesNemo < mesProceso  → use anoProceso from fechaProceso
     *                     otherwise             → anoProceso-1, or "99" if anoProceso is "00"/"01"
     *   no marker     → return nemoActual unchanged
     *
     * @param nemoActual   current nemotécnico string
     * @param fechaProceso process date in "yyyy-MM-dd" format (e.g. "2018-12-15")
     * @return transformed nemotécnico
     * @throws Exception preserved from legacy signature (no checked exception is thrown by this logic)
     */
    public static String determinarNuevoNemo(String nemoActual, String fechaProceso) throws Exception {
        // REEMPLAZA *
        if (nemoActual.indexOf("**") > -1) {
            return (nemoActual.replace("**", " *"));   // Ej: ABNA12**02  >>  ABNA12 *02
        } else if (nemoActual.indexOf(" *") > -1) {
            return (nemoActual.replace(" *", "01"));   // Ej: ABNA12 *02  >>  ABNA120102
        }

        // REEMPLAZA &
        else if (nemoActual.indexOf("&&") > -1) {
            return (nemoActual.replace("&&", " &"));  // Ej: ABNA12&&13  >>   ABNA12 &13
        } else if (nemoActual.indexOf(" &") > -1) {

            StringBuilder nemoNuevo = new StringBuilder(nemoActual);

            String mesNemo = nemoActual.substring(8, 10);  // ABNA12 &13  >>  13
            nemoNuevo.replace(6, 8, mesNemo);              // Ej: ABNA12 &13  >>   ABNA121313

            String anoProceso = fechaProceso.substring(2, 4);  // 2018-12-15  >>   18
            String mesProceso = fechaProceso.substring(5, 7);  // 2018-12-15  >>   12

            if (Integer.parseInt(mesNemo) < Integer.parseInt(mesProceso)) {
                nemoNuevo = nemoNuevo.replace(8, 10, anoProceso);    // Ej: ABNA121313  >>   ABNA121318
            } else {
                if (anoProceso.equalsIgnoreCase("00") || anoProceso.equalsIgnoreCase("01")) {
                    nemoNuevo.replace(8, 10, "99");   // Ej: ABNA121313  >>   ABNA121399
                } else {
                    Integer anoNemo = Integer.parseInt(anoProceso) - 1;
                    nemoNuevo.replace(8, 10, anoNemo.toString());  // Ej: ABNA121313  >>   ABNA121317
                }
            }

            return nemoNuevo.toString();
        }

        // Retorno el mismo nemo en caso de no haber cambios
        return nemoActual;
    }
}
