SELECT 
	  Spot."NumeroOperacion"
    , TO_BIGINT(:IdEmpresa) AS "IdEmpresa"
	, Spot."IdTipoOperacion"
	, Spot."IdInstrumento"
	, Instrumento."Nemotecnico"
	, Spot."IdMonedaPrincipal"
	, Spot."IdMonedaSecundaria"
	, Spot."IdMonedaSecundaria"
	, :MonedaLocal AS "MonedaLocal"
	, PuntaDefault."CheckPrecioPorTramo"
	, COALESCE(Spot."MontoMonedaPrincipal",0) AS "MontoMonedaPrincipal"
	, COALESCE(Spot."MontoMonedaSecundaria",0) AS"MontoMonedaSecundaria" 
	, COALESCE(Spot."TipoCambioCierre",0) AS "TipoCambioCierre"
	

	, COALESCE(Spot."TipoCambioCosto",0) AS "TipoCambioCosto"
	
	
	, COALESCE(Spot."ParidadCierre",0) AS "ParidadCierre"
	, COALESCE(Spot."ParidadCosto",0) AS "ParidadCosto"
	, COALESCE(PAR."ParidadMercado",0) AS "ParidadMercado"
	, PuntaDefault."PuntaCompra"                   
	, PuntaDefault."PuntaVenta"
	, PuntaPorTramo."ValorCompra"
	, PuntaPorTramo."ValorVenta"
	, Spot."Margen"
FROM "BSCL.Mesa.BD::SPT.CrearOperacionSpot_Proceso" Spot
--INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" Instrumento
INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" Instrumento
 	ON Spot."IdInstrumento" = Instrumento."IdInstrumento"
INNER JOIN (
	SELECT
		 SPOT."NumeroOperacion"
		,PTA."IdPuntas"
		,MAX(PPT."MontoPorTramo") AS "MaxMonto"
		,MIN(PPT_MENOR."MontoPorTramo") AS "MinMonto"
	FROM "BSCL.Mesa.BD::SPT.CrearOperacionSpot_Proceso" AS SPOT
	INNER JOIN "BSCL.Mesa.BD::SPT.Puntas" AS PTA
		ON (PTA."IdMonedaPunta" = SPOT."IdMonedaPrincipal" AND 
			PTA."IdEmpresa" = :IdEmpresa AND 
			PTA."Estado" = 'VIG')
	LEFT JOIN "BSCL.Mesa.BD::SPT.PuntasPorTramo" AS PPT
		ON (PTA."IdPuntas" = PPT."IdPuntas"	AND 
			PPT."Canal" = :Canal			AND 
			PPT."IdEmpresa" = :IdEmpresa	AND 
			PPT."Estado" = 'VIG'			AND 
			(PPT."MontoPorTramo" <= SPOT."MontoMonedaPrincipal"))
	INNER JOIN "BSCL.Mesa.BD::SPT.PuntasPorTramo" AS PPT_MENOR
		ON (PTA."IdPuntas" = PPT_MENOR."IdPuntas" AND 
			PPT_MENOR."Canal" = :Canal AND 
			PPT_MENOR."IdEmpresa" = :IdEmpresa	AND 
			PPT_MENOR."Estado" = 'VIG')
	GROUP BY PTA."IdPuntas",SPOT."NumeroOperacion") AS PuntaCorrespondiente
	ON Spot."NumeroOperacion" = PuntaCorrespondiente."NumeroOperacion"
INNER JOIN "BSCL.Mesa.BD::SPT.PuntasPorTramo" AS PuntaPorTramo
	ON (PuntaCorrespondiente."IdPuntas" = PuntaPorTramo."IdPuntas" AND
	    COALESCE(PuntaCorrespondiente."MaxMonto",PuntaCorrespondiente."MinMonto") = PuntaPorTramo."MontoPorTramo" AND
		PuntaPorTramo."Canal" = :Canal AND 
		PuntaPorTramo."IdEmpresa" = :IdEmpresa	AND 
		PuntaPorTramo."Estado" = 'VIG')
INNER JOIN "BSCL.Mesa.BD::SPT.Puntas" AS PuntaDefault
	ON (PuntaPorTramo."IdPuntas" = PuntaDefault."IdPuntas" AND 
		PuntaDefault."IdEmpresa" = :IdEmpresa AND 
		PuntaDefault."Estado" = 'VIG')
LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PAR
	ON (PAR."IdMoneda" = Spot."IdMonedaPrincipal" AND PAR."Fecha" = Spot."FechaOperacion")
WHERE Spot."NumeroOperacion" IN (:NumeroOperacion);
