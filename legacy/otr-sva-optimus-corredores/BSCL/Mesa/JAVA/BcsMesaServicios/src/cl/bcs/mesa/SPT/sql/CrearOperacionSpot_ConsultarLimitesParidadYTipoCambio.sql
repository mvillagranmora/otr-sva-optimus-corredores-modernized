SELECT
	OPE."NumeroOperacion",
	OPE."IdInstrumento",
	INS."Nemotecnico",
	OPE."IdMonedaPrincipal",
	OPE."IdMonedaSecundaria",
	COALESCE(OPE."TipoCambioCierre",0)	AS "TipoCambioCierre",
	COALESCE(OPE."ParidadCierre",0)		AS "ParidadCierre",
	COALESCE(OPE."TipoCambioCosto",0)	AS "TipoCambioCosto",
	COALESCE(OPE."ParidadCosto",0)		AS "ParidadCosto",
	COALESCE(PYT."ParidadMercado",0)												AS "ParidadMercado",
	COALESCE(PYT."ParidadBID",0)													AS "ParidadBID",
	COALESCE(PYT."ParidadASK",0)													AS "ParidadASK",
	COALESCE(PYT."LimiteSuperiorParidad",0)											AS "LimiteSuperiorParidad",
	COALESCE(PYT."LimiteInferiorParidad",0)											AS "LimiteInferiorParidad",
	COALESCE(PYT."ParidadMercado",0) + COALESCE(PYT."LimiteSuperiorParidad",0) 		AS "LimiteSuperiorPAR",
	COALESCE(PYT."ParidadMercado",0) - COALESCE(PYT."LimiteInferiorParidad",0) 		AS "LimiteInferiorPAR",
	COALESCE(PYT."TipoCambioMercado",0)												AS "TipoCambioMercado",
	COALESCE(PYT."LimiteSuperiorTipoCambio",0)										AS "LimiteSuperiorTipoCambio",
	COALESCE(PYT."LimiteInferiorTipoCambio",0)										AS "LimiteInferiorTipoCambio",
	COALESCE(PYT."TipoCambioMercado",0)+COALESCE(PYT."LimiteSuperiorTipoCambio",0)	AS "LimiteSuperiorTC",
	COALESCE(PYT."TipoCambioMercado",0)-COALESCE(PYT."LimiteInferiorTipoCambio",0)	AS "LimiteInferiorTC"
FROM "BSCL.Mesa.BD::SPT.CrearOperacionSpot_Proceso" AS OPE
--LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" AS INS
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INS
 	ON OPE."IdInstrumento" = INS."IdInstrumento"
LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" AS PYT 
	ON (PYT."IdMoneda" = OPE."IdMonedaPrincipal" AND
		PYT."Fecha" = TO_VARCHAR(OPE."FechaOperacion",'YYYY-MM-DD'))
WHERE OPE."NumeroOperacion" IN (:NumeroOperacion);
