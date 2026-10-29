SELECT 
CAR."IdCartera" AS "IdCustodia", 
CAR."FolioCartera" AS "FolioCustodia",
INS."IdInstrumento" AS "IdInstrumentoINS",
CAR.* 
FROM "BSCL.Custodia.BD::CUS.Cartera" CAR
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" INS ON (INS."Nemotecnico" = CAR."Nemotecnico")
WHERE CAR."IdCartera" = :p_IdCartera;