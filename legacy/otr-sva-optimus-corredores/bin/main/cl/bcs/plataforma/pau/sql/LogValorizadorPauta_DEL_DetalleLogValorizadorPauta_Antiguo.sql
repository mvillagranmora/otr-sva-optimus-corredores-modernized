DELETE FROM "BSCL.Plataforma.BD::PLA.DetalleLogValorizadorPauta"
WHERE "IdLogValorizadorPauta" IN (
	SELECT LVP."IdLogValorizadorPauta"
	FROM "BSCL.Plataforma.BD::PLA.LogValorizadorPauta" LVP
	WHERE LVP."FechaInicio" < TO_DATE(:FechaSistema, 'YYYY-MM-DD')
	  AND LVP."FechaTermino" IS NOT NULL
	  AND LVP."RutaLog" IS NOT NULL
	  AND LENGTH(TRIM(LVP."RutaLog")) > 0
	  AND LVP."Proceso" = :Proceso
);