INSERT INTO "BSCL.Plataforma.BD::MER.IndiceFinanciero"
(
 "IdIndiceFinanciero",
 "Fecha",
 "AgnoMes",
 "Agno",
 "Valor",
 "FechaActualizacion"
 )
VALUES
(
 :IdIndiceFinanciero,
 :Fecha,
 :AgnoMes,
 :Agno,
 :Valor,
 CURRENT_TIMESTAMP
)