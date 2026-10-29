SELECT CAST(BINS."Fecha" AS NVARCHAR(10)) AS "Valor1",INS."Nemotecnico" AS "Valor2"
FROM "BSCL.Plataforma.BD::INS.BolsaInstrumentoPrecio" BINS
INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS 
ON BINS."IdInstrumento" = INS."IdInstrumento"
WHERE BINS."Fecha" = :p_FechaSistema 
AND BINS."PrecioCierre" = 0;