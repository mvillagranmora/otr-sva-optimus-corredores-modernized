UPSERT "BSCL.Plataforma.BD::MER.ParametroFinanciero"
("IdParametroFinanciero","Fecha","AgnoMes","Agno","Valor","FechaActualizacion")
VALUES(?,?,?,?,?,CURRENT_UTCTIMESTAMP)
WHERE
    "IdParametroFinanciero" = ?
    AND "Fecha" = ?
    AND "AgnoMes" = ?
    AND "Agno" = ?;