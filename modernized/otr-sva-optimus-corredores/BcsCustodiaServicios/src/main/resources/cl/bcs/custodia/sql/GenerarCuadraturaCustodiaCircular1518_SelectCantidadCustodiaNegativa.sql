SELECT 
CASE WHEN tpInst."IdClasificacionTipoInstrumento" = 'IF' THEN SUM(CN."ValorRescate")
else
SUM(CN."Cantidad") END  AS "Cantidad",
CN."NemoSVS" as "Nemotecnico"
FROM "BSCL.Custodia.BD::CUS.CarteraNegativa" CN
INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" ins ON ins."IdInstrumento" = CN."IdInstrumento" 
INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" tpInst ON tpInst."IdTipoInstrumento" = ins."IdTipoInstrumento" 
INNER JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" clInst ON clInst."IdClasificacionTipoInstrumento" = tpInst."IdClasificacionTipoInstrumento"
WHERE CN."IdInstrumento" = :IdInstrumento
AND CN."NemoSVS" = :NemoSVS
AND CN."IdEstado" = 'VIG'
GROUP BY tpInst."IdClasificacionTipoInstrumento",CN."NemoSVS";