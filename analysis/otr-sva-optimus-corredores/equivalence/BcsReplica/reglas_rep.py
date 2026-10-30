"""BcsReplica: catalog of the rules (B-REP-nn) and defects (D-REP-nn) that the cases and the JUnit tests cite. Single source: check_replica.py verifies that every id a case cites is here and
that every id here is cited by at least one case (D-REP-22 is documented only: no case can run an endless loop). Legacy line numbers are those of the frozen legacy/ sources."""

E = "EnviarTablaReplica.java"
U = "ReplicaUtils.java"
I = "IniciarReplicaFull.java / IniciarReplicaDelta.java"

REGLAS = {
    "B-REP-01": ("IniciarReplicaFull/Delta: la fila de WorkStack lleva Parameters = \"BCS_query:<consulta>;Fecha:<fecha de sistema de la empresa>\" (la fecha viene de Utils.getFechaSistema)", I + ":51"),
    "B-REP-02": ("IniciarReplicaFull usa la consulta QueryReplica y IniciarReplicaDelta QueryReplicaDelta; ambas encolan IdStack (secuencia WorkStack, primer valor), Key \"-1\", Table INICIAR_REPLICA_PAUTA, Action \"F\", Status 0, Stamp = ahora", I + ":45-50"),
    "B-REP-03": ("Respaldo Sebra: solo el valor \"S\" (sin distinguir mayusculas) de PLA.RespaldoReplicaTablasSebra lo activa; cualquier otro valor no hace nada", U + ":70"),
    "B-REP-04": ("COD_PER = los 7 primeros caracteres de la fecha de sistema (aaaa-mm)", U + ":76"),
    "B-REP-05": ("EnviarTablaReplica compara el proceso sin distinguir mayusculas; un proceso desconocido solo se registra (no escribe nada)", E + ":100-377"),
    "B-REP-06": ("INICIO_PAUTA: apaga los triggers, encola \"CUENTA INVERSION DIA ACTUAL\", limpia (retencion de 10 dias y fecha actual) e inserta el historico de cuenta inversion y de custodia, y respalda los dias inhabiles de cuenta inversion", E + ":100-153"),
    "B-REP-07": ("VALORIZAR_FFMM: encola \"CUSTODIA ACTUAL\" y \"CARTERAFM\" (truncarDestino:true) y respalda los dias inhabiles de custodia", E + ":154-182"),
    "B-REP-08": ("PAUTA_PATRIMONIO: encola \"PATRIMONIO DIA ACTUAL\" con las 6 tablas de patrimonio", E + ":183-195"),
    "B-REP-09": ("COPIA_PATRIMONIO_PARA_INHABIL: por cada dia inhabil siguiente a la fecha encola \"PATRIMONIO DIA <fecha>\"; si el siguiente es habil no encola nada", E + ":196-257"),
    "B-REP-10": ("PAUTA_RESPALDAR_CUSTODIA: encola CARTERAFMHIS, CARTERARVHIS y GARANTIAHIS (\"ACTUAL\")", E + ":258-292"),
    "B-REP-11": ("CAMBIO_FECHA_SISTEMA: encola \"CUENTA INVERSION DIA SIGUIENTE\"", E + ":293-303"),
    "B-REP-12": ("VALORIZAR_CARTERA_RV_PRECIO_MERCADO: encola \"CARTERARV\" con truncarDestino:true", E + ":304-315"),
    "B-REP-13": ("FIN_PAUTA: encola \"CUSTODIA DIA SIGUIENTE\", lee PLA.SegundosZonaHoraria, reactiva los triggers y registra el resumen (PAUTA / FIN_PAUTA / OK), luego limpia e inserta el historico", E + ":316-374"),
    "B-REP-14": ("respaldarDataDiaInhabil: avanza dia a dia mientras el dia siguiente es inhabil; por cada uno encola un Workstack y copia los datos de la fecha origen a la fecha destino segun el tipo de tabla (CUSTODIA ACTUAL DIA INHABIL / CUENTA INVERSION DIA INHABIL)", E + ":447-530"),
    "B-REP-15": ("Los Workstack se insertan en un solo lote al final, solo si la lista no esta vacia: Key \"\", Action \"M\", Status 0, Stamp = CURRENT_TIMESTAMP de la base", E + ":379-384"),
    "B-REP-16": ("Parameters de un dia inhabil = prefijo + fecha destino + \";serialDelete:false;truncarDestino:false;serialLaunch:false\"", E + ":498"),
    "B-REP-17": ("Respaldo Sebra: 13 tablas TBC* en orden fijo, cada una DELETE del periodo y luego INSERT desde la vista Sebra del mismo periodo", U + ":74-131"),
    "B-REP-18": ("Respaldo diario de carteras Sebra: 4 tablas (TBCTCIFM, TBCTCIRV, TBCTCIIF, TBCTCIRF), COD_DIA = \"90\" + dd + \"-01\"; borra COD_DIA y copia el periodo mensual bajo COD_DIA", U + ":299-349"),
    "B-REP-19": ("Respaldo Optimus: borra el actual e inserta el historico (cuenta inversion efectiva y movimiento, custodia operacion y liquidacion), guarda 4 envios post pauta y repite la copia por cada dia inhabil siguiente", U + ":176-287"),
    "B-REP-20": ("guardarEnvioPostPauta hace UPSERT de (FechaSistema, Tabla, Fecha) en EnvioPostPauta", U + ":162-174"),
    "B-REP-21": ("Retencion: el borrado historico elimina las filas con fecha <= fecha de sistema - 10 dias (limite inclusivo), en las 4 tablas, y luego las de la fecha de sistema", E + ":126-140"),
    "B-REP-22": ("La copia de un dia inhabil recalcula Checksum: -IdCuentaInversion si SaldoDisponible = 0; si no la suma ponderada (NULL si falta alguna columna)", "EnviarTablaReplica_InsertCuentaInversionFecha*DiaInhabil.sql"),
    "B-REP-23": ("Respaldo diario: si falta el parametro PLA.RespaldoDiarioReplicaCarteraSebra se asume \"N\"", U + ":303-308"),
    "B-REP-24": ("IniciarReplicaFull/Delta.call: lee el JSON como EntradaIniciarReplica, ejecuta logica y devuelve el JSON de la salida con P_OUT_ERRORESCONTADOR recontado (mensajes de tipo ERROR)", I + ":29-36"),
}

DEFECTOS = {
    "D-REP-01": ("EnviarTablaReplica.call y ReplicaUtils.call son stubs (\"TODO Auto-generated method stub\"): devuelven null", E + ":86-90, " + U + ":351-355"),
    "D-REP-02": ("procesarRespaldoOptimus pasa el id de empresa como pais de cada fecha (FechasIn.pais = idEmpresa) y el id de pais como p_idPais", U + ":220"),
    "D-REP-03": ("COPIA_PATRIMONIO_PARA_INHABIL: si el dia siguiente da error devuelve sin insertar los Workstack ya acumulados", E + ":223-227"),
    "D-REP-04": ("respaldarDataDiaInhabil: si el dia siguiente da error corta el ciclo sin excepcion y los Workstack acumulados si se insertan (incoherente con D-REP-03)", E + ":478-482"),
    "D-REP-05": ("VALORIZAR_CARTERA_RV_PRECIO_MERCADO: el Parameters tiene un espacio sobrante antes de \";serialDelete\"", E + ":310"),
    "D-REP-06": ("INICIO_PAUTA apaga ambos triggers en un solo try: si falla el primero se traga el error y no se intenta el segundo", E + ":104-111"),
    "D-REP-07": ("FIN_PAUTA reactiva ambos triggers y registra el resumen en un solo try: un error se traga (la pauta termina bien) y se omite el resto del bloque, incluido el resumen", E + ":340-350"),
    "D-REP-08": ("Los INSERT historicos llaman a las vistas analiticas con p_idempresa => 1 y p_fechaconsulta => '0001-01-01' fijos en el SQL", "EnviarTablaReplica_Insert*Historica.sql"),
    "D-REP-09": ("ResumenReplica: UltimaActualizacion es la hora local del servidor mas PLA.SegundosZonaHoraria (depende del reloj) y la clave primaria la incluye, de modo que el UPSERT agrega una fila por ejecucion", E + ":326-334,418-445"),
    "D-REP-10": ("EnvioPostPauta tiene clave (FechaSistema, Tabla, Fecha): el UPSERT no sobrescribe una fecha de envio distinta, la agrega", "EnviarTablaReplica_InsertEnvioPostPauta.sql"),
    "D-REP-11": ("FechaSistemaEjecucion no se valida: nulo falla con NullPointerException (en INICIO_PAUTA despues de apagar los triggers), un no-texto con ClassCastException, un texto con hora o basura pasa al SQL", E + ":95"),
    "D-REP-12": ("procesarRespaldoSebra propaga la ausencia del parametro, procesarRespaldoDiarioCarterasSebra la traga y asume \"N\"", U + ":63-68,304-308"),
    "D-REP-13": ("Ningun respaldo es atomico: ante un error a mitad quedan en la sesion las tablas ya reemplazadas (el llamador decide commit o rollback)", U + ":74-130"),
    "D-REP-14": ("COD_DIA = \"90\" + dd + \"-01\" no incluye el mes: el respaldo diario de un dia pisa el del mismo dia del mes anterior", U + ":315"),
    "D-REP-15": ("WorkStack.Parameters es VARCHAR(500): un parametro largo falla recien en la insercion final, despues de los borrados", E + ":383"),
    "D-REP-16": ("Un proceso nulo falla con NullPointerException; uno desconocido, con espacios o truncado no hace nada y solo deja un log", E + ":100,375-377"),
    "D-REP-17": ("IniciarReplicaFull/Delta: Key \"-1\", Action \"F\", Stamp con la hora de la JVM (no de la base) y p_IdUsuario ignorado", I + ":45-50"),
    "D-REP-18": ("IniciarReplica sin validacion: p_IdEmpresa nulo falla con NullPointerException al leer la fecha de sistema (despues de pedir la secuencia); un JSON con empresa no numerica falla en Gson", I + ":29-31,45,51"),
    "D-REP-19": ("FIN_PAUTA convierte la fecha con Fecha.toDate: acepta aaaa-mm-dd o aaaa-mm-dd hh:mm:ss, cualquier otro formato (o nulo) falla con DateTimeParseException / NullPointerException despues de leer el desfase horario y antes de escribir o encolar nada", E + ":330"),
    "D-REP-20": ("respaldarDataDiaInhabil con un nombre de tabla distinto de los dos conocidos (comparacion exacta, con mayusculas) encola los Workstack pero no copia datos, sin avisar", E + ":511-525"),
    "D-REP-21": ("Los Workstack de los dias inhabiles se insertan despues de las copias: si falla la insercion final las copias ya hechas quedan", E + ":379-384"),
    "D-REP-22": ("Los ciclos de dias inhabiles no tienen tope: si el calendario solo entrega dias inhabiles no terminan (documentado, sin caso: el legacy no se puede ejecutar)", E + ":204,460"),
}
