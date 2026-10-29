F3 W1 front 9 (ConsultarCustodiaPortafolioFFMM): compile-only shims of record_ffmm.sh.
None is needed: ConsultarCustodiaPortafolioFFMM, ConstantesFondoMutuo and the generated doubles (ConsultarEmpresa, CalcularParidad, EntregarPreciosTasas) import nothing outside the
closure that record_ffmm.sh compiles. The directory exists because record_ffmm.sh scans it (find shims-ffmm -name '*.java').
