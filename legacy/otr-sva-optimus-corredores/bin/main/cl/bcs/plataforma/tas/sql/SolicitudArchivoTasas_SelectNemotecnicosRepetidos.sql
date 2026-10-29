SELECT "Nemotecnico" as "nemotecnico"
FROM "BSCL.Plataforma.BD.Vistas::TAS.ConsultarInstrumentoCustodiaParaTasas" INS
WHERE "NemotecnicoAux" = 'PAGARE R'
GROUP BY "Nemotecnico"
HAVING count(*) > 1;