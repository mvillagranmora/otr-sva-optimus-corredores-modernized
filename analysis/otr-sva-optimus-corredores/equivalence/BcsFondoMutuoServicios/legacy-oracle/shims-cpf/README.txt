F3 W1 front 8 (ConsultarCustodiaPortafolioFondoMutuo): compile-only shims of record_cpf.sh.
None is needed so far: ConsultarCustodiaPortafolioFondoMutuo, CalcularMontoNumeroCuota and the generated doubles (ConsultarCustodiaPortafolioFFMM, CalcularParidad,
EntregarPreciosTasas, ConsultarEmpresa) import nothing outside the closure that record_cpf.sh compiles. The directory exists because record_cpf.sh scans it
(find shims-cpf -name '*.java').
