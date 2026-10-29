INSERT INTO "BSCL.Normativa.BD::LogEjecucionInterfaz"
("IdLogEjecucionInterfaz", "IdInterfaz", "FechaEjecucion", "NumeroRegistros",
"NumeroRegistrosCorrectos", "NumeroRegistrosIncorrectos")
VALUES(
"BSCL.Contabilidad.BD.Secuencias::IdLogEjecucionInterfaz".NEXTVAL
,:IdInterfaz
,CURRENT_UTCTIMESTAMP
,:NumeroRegistros
,:NumeroRegistrosCorrectos
,:NumeroRegistrosIncorrectos
)