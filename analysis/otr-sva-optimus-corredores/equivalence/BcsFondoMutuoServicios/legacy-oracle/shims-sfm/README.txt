F3 W1 front 3 (ConsultarSolicitudesFFMM / ConsultarSolicitudesFFMMWS): compile-only shims of record_sfm.sh.
None is needed: neither class imports a type outside the closure it compiles (no dead import of an unported class, unlike the FSA front). The directory exists
because record_sfm.sh scans it (find shims-sfm -name '*.java'); any *.java placed here is compiled with the oracle and NEVER executed unless a case reaches it.
