/**
 * COMMON KIT of the three use case families (published by implementer A; keep the signatures stable).
 *
 * <h2>How a use case is built (the same rules for contratos, traspasos and recaudacion)</h2>
 * <ul>
 *   <li><b>Service</b>: {@code cl.bcs.apv.application.<familia>.<Caso>Servicio implements cl.bcs.apv.api.casouso.<Caso>}, ONE public constructor that
 *       declares by TYPE what it needs: the ports of {@code cl.bcs.apv.domain.port.out} ({@code ConfiguracionSistema, DiccionarioMensajes, Secuencias,
 *       Reloj, AlmacenArchivos, Colaboradores}), repository ports of {@code cl.bcs.apv.domain.port.out.<familia>}, and OTHER APV use cases as their
 *       INTERFACE (a peer is injected typed, never reached through {@code Colaboradores}). No {@code new} of services or helpers inside a service. There is no
 *       wiring code to edit: {@code ApvCasosDeUso} (tests, in process) and {@code CasosDeUsoConfig} (Spring) register every class of
 *       {@code cl.bcs.apv.application} that implements a use case interface and build it by constructor injection.
 *       Every write entry point (the class) is {@code @Transactional(rollbackFor = Exception.class)}.</li>
 *   <li><b>Persistence</b>: a repository PORT per cohesive group of SQL in {@code cl.bcs.apv.domain.port.out.<familia>}, implemented by a
 *       {@code @Repository} class in {@code cl.bcs.apv.infrastructure.persistence.<familia>} with ONE constructor taking the
 *       {@code NamedParameterJdbcTemplate}, which uses {@link cl.bcs.apv.infrastructure.jdbc.SqlNombrado} (named parameters, empty collection = one NULL item,
 *       {@code SQLException} surfaced like the legacy, {@code setScale(16, HALF_DOWN)} batches, {@code confirmar()/revertir()} for the legacy mid-flow
 *       commit/rollback on the same connection). The SQL text lives in {@code src/main/resources/sql/<name>.sql}, BYTE FOR BYTE the legacy
 *       {@code cl/bcs/APV/sql} file (the H2 dialect bridge of the tests keys its overrides on that text).</li>
 *   <li><b>Checked exceptions</b>: the typed contract {@code R ejecutar(E)} declares none, and the goldens pin the SIMPLE CLASS NAME of what the legacy threw
 *       ({@code SQLException}, {@code Exception}, {@code NullPointerException}...). Re-throw the SAME object with {@link cl.bcs.apv.application.comun.Sneaky}
 *       ({@code throw Sneaky.lanzar(e)}, {@code Sneaky.sinChequear(() -> ...)}); never wrap. A repository port may declare {@code throws SQLException}
 *       when the service must catch it.</li>
 *   <li><b>Messages and counters</b>: {@link cl.bcs.apv.application.comun.Mensajes} ({@code Mensajes.de(...)}, {@code Mensajes.conDiccionario(...)},
 *       {@code Mensajes.numeroErrores(...)}). The legacy {@code call()} sets {@code P_OUT_ERRORESCONTADOR = getNumeroErrores(...)} AFTER {@code logica}: that
 *       belongs to {@code ejecutar}; a peer that the legacy called through {@code logica} does not set it.</li>
 *   <li><b>Configuration</b>: {@link cl.bcs.apv.application.comun.Parametros} (a missing parameter is {@code java.lang.Exception}
 *       "Parametro X en modulo Y no encontrado", exactly the legacy). The clock comes from the {@code Reloj} port, sequences from {@code Secuencias}.</li>
 *   <li><b>Collaborators outside APV</b>: the {@code Colaboradores} port, called with the simple name written as a string LITERAL (a test scans them all) and a payload that is the LEGACY input object built with
 *       {@link cl.bcs.apv.application.comun.Json#objeto()} (legacy field names; nulls written as JSON null) and the answer is read with
 *       {@code Json.lista/mensajes/texto/largo}. The simple name MUST be in {@code ColaboradoresJavaCall} (explicit map; an unknown name is an
 *       {@code IllegalArgumentException}); stored procedures are {@code "StoredProcedure"} with {@code {procedimiento, entrada}} and their call text goes in
 *       {@code src/main/resources/sql/Procedimiento_<name with every non alphanumeric character replaced by _>.sql}.</li>
 *   <li><b>Legacy defects</b> are reproduced bug for bug and marked with a one-line comment {@code LEGACY DEFECT kept (see README APV-A-NN)}.</li>
 * </ul>
 */
package cl.bcs.apv.application.comun;
