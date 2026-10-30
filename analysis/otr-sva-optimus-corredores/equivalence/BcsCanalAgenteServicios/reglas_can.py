"""BcsCanalAgenteServicios: catalog of the rules (B-CA-nn) and defects (D-CAN-nn) that the cases and the JUnit tests cite. Single source: check_canal.py verifies that every id a case cites is here and that
every id here is cited by at least one case, except the ones in SIN_CASO (documentation or text pins without a case). Legacy line numbers are those of the frozen legacy/ sources.

B-CA-01..05 are the ids of the previous skeleton (TRANSFORMATION_NOTES of 2026-10-05) CORRECTED against the legacy (the skeleton assumed things the legacy does not do: see TRANSFORMATION_NOTES.md).
"""

H = "ConsultarHistorialMovimientosCuentaInversion.java"
SP_CI = "BD/Servicios/ConsultarHistorialMovimientosCuentaInversion.sql"
SP_CU = "BD/Servicios/ConsultarHistorialMovimientosCustodia.sql"
SP_CL = "BD/Servicios/ConsultaAvanzadaClientes.sql"

REGLAS = {
    "B-CA-01": ("Busca el IdCliente del portafolio con SELECT TOP 1 \"IdCliente\" FROM POR.Portafolio WHERE \"IdPortafolio\" = :IdPortafolio y usa el primero", H + ":157-162, :178"),
    "B-CA-02": ("Delega a ConsultarCartolaDeMovimiento con p_UsaReglasNegocio \"S\", p_SaldoPorFechaEfectiva \"N\", p_IdCuentaInversion y p_IdEstado nulos, p_IdCliente el encontrado y el resto copiado de la entrada", H + ":172-191"),
    "B-CA-03": ("Enriquece con FolioComprobante, CodigoPortafolio, IdRespuestaDTE, RespuestaPdfDTE y DocumentoIdentidad: INSERT por lote de (IdCargoAbonoMovimiento, IdPortafolio, IdComprobante) en la tabla de trabajo, SELECT con los LEFT JOIN y DELETE sin WHERE, en ese orden", H + ":195-242"),
    "B-CA-04": ("Un movimiento sin comprobante (IdComprobante nulo o sin fila en FAC.Comprobante) sale con FolioComprobante, IdRespuestaDTE y RespuestaPdfDTE nulos; CodigoPortafolio y DocumentoIdentidad salen igual, porque vienen del portafolio de la fila (nulos solo si el portafolio, el cliente o su documento no existen)", H + ":205-237, SelectCartolaDeMovimientoComprobante.sql"),
    "B-CA-05": ("p_IdMatrizAgrupador y p_EsADC son opcionales (nulos por omision), viajan a la cartola y cada movimiento devuelve el IdMatrizAgrupador y el EsADC de SU fila de cartola", H + ":44-49,185-186,238-239"),
    "B-CA-06": ("Mapeo de campos del movimiento: FechaMovimiento = FechaSistema, FechaLiquidacion = FechaLiquidacionOperacion, el resto 1:1 con la cartola (Cargo, Abono, SaldoSimulado sin calculo)", H + ":214-239"),
    "B-CA-07": ("El orden del resultado es el de la cartola (no el del SELECT de comprobantes)", H + ":210-240"),
    "B-CA-08": ("Al terminar bien P_OUT_ERRORESCONTADOR es 0 y las listas de errores y advertencias van vacias", H + ":243"),
    "B-CA-09": ("La tabla de trabajo queda vacia al terminar (DELETE sin WHERE: borra tambien filas ajenas de la sesion)", H + ":242"),
    "B-CA-10": ("FolioComprobante sale como texto (TO_VARCHAR del folio numerico) y el FolioComprobante que trae la cartola se ignora", H + ":225, SelectCartolaDeMovimientoComprobante.sql"),
    "B-CA-11": ("JavaCall.call: el JSON de entrada se lee con Gson de la plataforma (fechas, numeros) y la salida se escribe con nulos explicitos y fechas \"yyyy-MM-dd HH:mm:ss.SSS\"", H + ":255-262"),
    "B-CA-12": ("El comprobante se une al movimiento por IdCargoAbonoMovimiento (mapa hash), no por posicion", H + ":205-211"),
    "B-CA-13": ("El modulo no hace commit ni rollback: usa la conexion recibida y deja el estado en la sesion (el llamador decide)", H + ":150-252"),
}

DEFECTOS = {
    "D-CAN-01": ("Cliente no encontrado: se crea el error \"Cliente no encontrado.\" pero salida.P_OUT_ERRORESCONTADOR++ opera sobre un Long nulo y lanza NullPointerException; el mensaje nunca llega al llamador (el esqueleto previo daba por hecho una respuesta con el error)", H + ":163-170, :64"),
    "D-CAN-02": ("P_OUT_ADVERTENCIASCONTADOR no se asigna nunca: sale null siempre (la lista de advertencias va vacia)", H + ":61-70"),
    "D-CAN-03": ("P_SALDOEFECTIVOINICIAL y P_SALDOEFECTIVOFINAL no se asignan nunca: salen null siempre (el procedimiento de BD los pone en 0)", H + ":58-59, " + SP_CI),
    "D-CAN-04": ("Los errores, mensajes y contador de la cartola no se propagan: si la cartola devuelve errores (p. ej. fallan los requisitos basicos) el servicio responde como exito, con la lista que haya y contador 0", H + ":191-243"),
    "D-CAN-05": ("Un fallo a mitad (insercion, seleccion o borrado) relanza la excepcion sin limpiar: las filas ya insertadas quedan en la tabla de trabajo de la sesion (HANA las borra al cerrar la transaccion: la tabla es GLOBAL TEMPORARY ON COMMIT DELETE ROWS)", H + ":244-247"),
    "D-CAN-06": ("Si un IdCargoAbonoMovimiento aparece mas de una vez en la tabla de trabajo (repetido en la cartola o fila ajena de la sesion) el mapa se queda con la ULTIMA fila que devuelve el SELECT, que no tiene ORDER BY: el resultado depende del orden de HANA", H + ":205-208"),
    "D-CAN-07": ("El reemplazo por un MovimientoCuentaInversionComprobante vacio es codigo muerto: toda fila de la cartola se inserto y vuelve en el SELECT, asi que la busqueda en el mapa nunca falla", H + ":211-212"),
    "D-CAN-08": ("Salida de la cartola defectuosa: lista nula -> SQLException de SQL.batch (Null parameters), elemento nulo -> SQLException \"Map Object failed\", salida nula -> NullPointerException; todas antes de tocar la tabla de trabajo", H + ":191-196"),
    "D-CAN-09": ("Sin validacion de entrada: empresa, usuario, modulo, moneda y fechas (nulas, invertidas, de formato libre) pasan tal cual a la cartola; solo un portafolio nulo o inexistente falla (D-CAN-01)", H + ":150-186"),
    "D-CAN-10": ("Entrada JSON de call: Gson acepta numeros como texto y enteros con \".0\", rechaza lo no numerico con JsonSyntaxException, ignora campos desconocidos y distingue mayusculas en los nombres (un nombre en minusculas queda nulo)", H + ":255-258"),
    "D-CAN-11": ("p_IdEstado y p_IdCuentaInversion van fijos en null y p_Validaciones, p_IncluyeSaldo y p_IncluyeAnulados toman los valores por omision de la entrada de la cartola (\"S\", \"N\", \"S\"): el servicio no permite filtrar por estado ni excluir anulados", H + ":177,181, ConsultarCartolaDeMovimiento.java:36-62"),
    "D-CAN-12": ("La tabla de trabajo no se limpia antes de insertar: las filas previas de la sesion se mezclan en el SELECT y el DELETE final borra tambien las ajenas", H + ":196-242"),
    "D-CAN-13": ("Procedimiento de BD ConsultarHistorialMovimientosCuentaInversion: sin cliente hace RETURN sin error (el Java lanza NPE), devuelve los saldos en 0 (el Java null), nunca asigna p_out_ErroresContador, une con la VISTA ConsultarPortafolio y con el comprobante por fila de la cartola (una fila por movimiento aunque se repita el id: el Java no)", SP_CI),
    "D-CAN-14": ("Procedimiento de BD ConsultarHistorialMovimientosCustodia: la cabecera dice \"Cuenta Inversion\" (copiada), el parametro p_IdCliente de la consulta esta comentado con :p_IdEmpresa, no asigna contadores de errores ni advertencias", SP_CU),
    "D-CAN-15": ("Procedimiento de BD ConsultaAvanzadaClientes: el INNER JOIN con CLI.Cliente descarta las filas sin cliente y llama a la consulta del SocioNegocio con una variable de tabla que no declara", SP_CL),
    "D-CAN-16": ("Los scripts BD/Scripts/Prueba*.sql llaman procedimientos que no existen (\"BD.Servicio::\" sin s) o con parametros que no tienen (p_FlujoCustodiaMinimo, p_FlujoCajaMinimo): no se portan", "BD/Scripts/Prueba*.sql"),
    "D-CAN-17": ("HANA QA tiene un cuarto procedimiento, BSCL.CanalAgente.BD.Servicios::ConsultarFlujoInstrumento, sin fuente en el repositorio", "catalog/canal_procedimientos.csv"),
}

# ids documented or pinned by text without a case that executes them
SIN_CASO = {"D-CAN-14", "D-CAN-16", "D-CAN-17"}
