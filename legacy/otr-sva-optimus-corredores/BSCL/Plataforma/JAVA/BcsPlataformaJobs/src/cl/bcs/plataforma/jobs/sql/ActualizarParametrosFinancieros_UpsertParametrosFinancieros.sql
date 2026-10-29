UPSERT "BSCL.Plataforma.BD::MER.ParametroFinanciero"(
    "IdParametroFinanciero",
    "Fecha",
    "AgnoMes",
    "Agno",
    "Valor",
    "FechaActualizacion"
) VALUES(
    :IdParametroFinanciero,
    :Fecha,
    :AgnoMes,
    :Agno,
    :Valor,
    :FechaActualizacion
    CURRENT_UTCTIMESTAMP
) 
WHERE "IdParametroFinanciero"= :IdParametroFinanciero  AND "Fecha"=:Fecha AND "AgnoMes"=:AgnoMes AND "Agno"= :Agno;
