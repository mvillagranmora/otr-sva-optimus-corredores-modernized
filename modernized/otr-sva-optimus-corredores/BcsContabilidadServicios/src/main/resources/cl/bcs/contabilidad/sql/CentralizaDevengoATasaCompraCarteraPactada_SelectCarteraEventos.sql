-- Raw CarteraValorizadaIFRF rows behind the legacy event query (same FROM / WHERE as the legacy
-- _SelectEventos.sql of this type, which joins nothing); ROUND, the sign branches and the type's
-- row filter are applied by DevengoCalculator.
SELECT
	CVIFRF."IdValorizacion"
	,CVIFRF."IdCartera"
	,CVIFRF."IdEmpresa"
	,CVIFRF."IdTipoOperacion"
	,CVIFRF."IdClasificacionTipoInstrumento"
	,CVIFRF."FolioOperacion"
	,CVIFRF."FolioCartera"
	,CVIFRF."IdPortafolio"
	,CVIFRF."FechaOperacion"
	,CVIFRF."IdMonedaInstrumento"
	,CVIFRF."MontoCompra"
	,CVIFRF."MontoPactado"
	,CVIFRF."MontoActual"
	,CVIFRF."MontoProximo"
	,CVIFRF."MontoInteres"
	,CVIFRF."MontoInteresCorrido"
	,CVIFRF."MontoReajuste"
FROM "BSCL.Custodia.BD::VAL.CarteraValorizadaIFRF" CVIFRF
WHERE CVIFRF."IdInforme" = :p_IdInforme
AND CVIFRF."IdEmpresa" = :p_IdEmpresa
