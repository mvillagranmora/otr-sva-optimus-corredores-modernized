WITH
	Cartera AS
	(
		SELECT
			'Cartera' AS "TipoCus"
		   ,CAR."IdCliente"
		   ,CAR."IdPortafolio"
		   ,CAR."IdClasificacionTipoInstrumento"
		   ,CAR."IdTipoInstrumento"
		   ,CAR."IdInstrumento"
		   --------------------------------------------------------------------------------------------
		   ,CAR."IdMonedaInstrumento" AS "IdMonedaOperacion"
		   ,CAR."INGMontoCompraActualMonedaInstrumento" AS "MontoCompraOperacion"
		   ,FLOOR(CASE WHEN CAR."IdClasificacionTipoInstrumento" = 'DV' AND CAR."IdTipoInstrumento" = 'FWD' THEN CAR."INGMontoValorRazonablePasivo" - CAR."INGMontoValorRazonableActivo" ELSE  CAR."INGMontoATasaMercadoActual" END) AS "MontoMercadoOperacion"
		FROM "BSCL.Custodia.BD::CUS.Cartera" CAR
		LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = CAR."IdPortafolio" AND Portafolio."IdCliente" = CAR."IdCliente"
		LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
		WHERE
		CAR."IdCliente" IN (:IdCliente)
		AND	CAR."IdClasificacionTipoInstrumento" IN ('RV','RF','IF','FM','DV')
		AND CAR."IdEstado" = 'VIG'
		AND CAR."IdEmpresa" = :IdEmpresa
		AND CAR."INGFechaLiquidacion" <= :FechaHasta
		AND (:IdProductoAgrupador IS NULL OR MAP(MatrizAgrupador."IdProductoAgrupador",NULL,0,MatrizAgrupador."IdProductoAgrupador") = :IdProductoAgrupador)
	),
	Garantia AS
	(
		SELECT
			'Garantia' AS "TipoCus"
		   ,CAR."IdCliente"
		   ,CAR."IdPortafolio"
		   ,GAR."IdClasificacionTipoInstrumento"
		   ,GAR."IdTipoInstrumento"
		   ,GAR."IdInstrumento"
		   --------------------------------------------------------------------------------------------
		   ,GAR."IdMonedaInstrumento" AS "IdMonedaOperacion"
		   ,GAR."INGMontoGarantiaActualMonedaInstrumento" AS "MontoCompraOperacion"
		   ,GAR."INGMontoMercadoActualMonedaInstrumento" AS "MontoMercadoOperacion"
		FROM "BSCL.Custodia.BD::GTA.Garantia" GAR
		INNER JOIN
		(
			SELECT
				RANK() over (partition by CAR."FolioCartera" order by CAR."IdCartera" desc) as RANK
			   ,CAR."FolioCartera"
			   ,CAR."IdCliente"
			   ,CAR."IdPortafolio"
			FROM "BSCL.Custodia.BD::CUS.Cartera" CAR
			LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = CAR."IdPortafolio" AND Portafolio."IdCliente" = CAR."IdCliente"
			LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
			WHERE
			CAR."IdCliente" IN (:IdCliente)
			AND	CAR."IdEmpresa" = :IdEmpresa
			AND (:IdProductoAgrupador IS NULL OR MAP(MatrizAgrupador."IdProductoAgrupador",NULL,0,MatrizAgrupador."IdProductoAgrupador") = :IdProductoAgrupador)
		) AS CAR ON CAR."FolioCartera" = GAR."FolioCarteraOrigen"
		WHERE
			GAR."IdClasificacionTipoInstrumento" IN ('RV','RF','IF','FM','DV')
		AND GAR."IdEstado" = 'VIG'
		AND GAR."INGFechaLiquidacion" <= :FechaHasta
		AND CAR.RANK = 1
	),
	Subyacente AS
	(
		SELECT
			'Subyacente' AS "TipoCus"
		   ,CAR."IdCliente"
		   ,CAR."IdPortafolio"
		   ,SUB."IdClasificacionTipoInstrumento"
		   ,CAR."IdTipoInstrumento"
		   ,SUB."IdInstrumento"
		   ,CAR."NombreTipoInstrumento" AS "NombreTipoInstrumento"
		   --------------------------------------------------------------------------------------------
		   ,CASE WHEN SUB."IdTipoReajuste" <> '' THEN SUB."IdTipoReajuste" ELSE SUB."IdMonedaInstrumento" END AS "IdMonedaOperacion"
		   ,CASE WHEN SUB."INGMontoATasaSubyacenteActual" > 0 THEN SUB."INGMontoATasaSubyacenteActual" ELSE SUB."INGMontoATasaMercadoActual" END AS "MontoCompraOperacion"
		   ,CASE WHEN SUB."INGMontoATasaMercadoActual" > 0 THEN SUB."INGMontoATasaMercadoActual" ELSE SUB."INGMontoATasaSubyacenteActual" END AS "MontoMercadoOperacion"
		FROM "BSCL.Custodia.BD::CUS.Subyacente" SUB
		INNER JOIN
		(
			SELECT
				RANK() over (partition by CAR."FolioCartera" order by CAR."IdCartera" desc) as RANK
			   ,CAR."FolioCartera"
			   ,CAR."IdCliente"
			   ,CAR."IdPortafolio"
			   ,CAR."IdTipoInstrumento"
			   ,INS."NombreTipoInstrumento"
			   ,CAR."INGFechaLiquidacion"
			FROM "BSCL.Custodia.BD::CUS.Cartera" CAR
			LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = CAR."IdPortafolio" AND Portafolio."IdCliente" = CAR."IdCliente"
			LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
			LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" INS ON INS."IdInstrumento" = CAR."IdInstrumento"
			WHERE
			CAR."IdCliente" IN (:IdCliente)
			AND	CAR."IdEmpresa" = :IdEmpresa
			AND (:IdProductoAgrupador IS NULL OR MAP(MatrizAgrupador."IdProductoAgrupador",NULL,0,MatrizAgrupador."IdProductoAgrupador") = :IdProductoAgrupador)
		) AS CAR ON CAR."FolioCartera" = SUB."FolioCarteraRelacionada"
		WHERE
			SUB."IdClasificacionTipoInstrumento" IN ('RV','RF','IF','FM','DV')
		AND SUB."IdEstado" = 'VIG'
		AND CAR."INGFechaLiquidacion" <= :FechaHasta
		AND CAR.RANK = 1
	)
	SELECT 
	CUS."IdCliente"
   ,CUS."IdPortafolio"
   ,CASE
   		WHEN INS."IdClasificacionTipoInstrumento" = 'RV' AND INS."IdTipoInstrumento" = 'SIM' AND :LocalizacionSistema = 'PE' THEN INS."IdTipoInstrumento"
   		WHEN INS."IdClasificacionTipoInstrumento" = 'RV' THEN INS."IdClasificacionTipoInstrumento"
		WHEN INS."IdClasificacionTipoInstrumento" = 'RF' AND INS."IdTipoInstrumento" IN ('PRT','EMP') THEN INS."IdTipoInstrumento"
		WHEN INS."IdClasificacionTipoInstrumento" = 'RF' AND INS."IdTipoInstrumento" NOT IN ('PRT','EMP') THEN INS."IdClasificacionTipoInstrumento"
   		WHEN INS."IdClasificacionTipoInstrumento" = 'IF' AND :LocalizacionSistema = 'CL' THEN INS."IdClasificacionTipoInstrumento"
   		WHEN INS."IdClasificacionTipoInstrumento" = 'FM' AND :LocalizacionSistema = 'CL' THEN INS."IdClasificacionTipoInstrumento"
   		WHEN INS."IdClasificacionTipoInstrumento" = 'DV' AND INS."IdTipoInstrumento" = 'FWD' THEN INS."IdTipoInstrumento"
   		WHEN INS."IdClasificacionTipoInstrumento" = 'DV' THEN INS."IdClasificacionTipoInstrumento"
	END AS "Grupo"
   ,CASE WHEN CUS."NombreTipoInstrumento" = '' THEN INS."NombreTipoInstrumento" ELSE CUS."NombreTipoInstrumento" END as "Item"
   ,CUS."IdMonedaOperacion"
   ,SUM("MontoMercadoOperacion") AS "MontoMonedaOperacion"
	FROM (SELECT
		"TipoCus"
	   ,"IdCliente"
	   ,"IdPortafolio"
	   ,"IdClasificacionTipoInstrumento"
	   ,"IdTipoInstrumento"
	   ,'' AS "NombreTipoInstrumento"
	   ,"IdInstrumento"
	   ,"IdMonedaOperacion"
	   ,"MontoCompraOperacion"
	   ,"MontoMercadoOperacion"
	FROM Cartera
	UNION ALL
	SELECT
		"TipoCus"
	   ,"IdCliente"
	   ,"IdPortafolio"
	   ,"IdClasificacionTipoInstrumento"
	   ,"IdTipoInstrumento"
	   ,'' AS "NombreTipoInstrumento"
	   ,"IdInstrumento"
	   ,"IdMonedaOperacion"
	   ,"MontoCompraOperacion"
	   ,"MontoMercadoOperacion"
	FROM Garantia
	UNION ALL
	SELECT
		"TipoCus"
	   ,"IdCliente"
	   ,"IdPortafolio"
	   ,"IdClasificacionTipoInstrumento"
	   ,"IdTipoInstrumento"
	   ,"NombreTipoInstrumento"
	   ,"IdInstrumento"
	   ,"IdMonedaOperacion"
	   ,"MontoCompraOperacion"
	   ,"MontoMercadoOperacion"
	FROM Subyacente) AS CUS
	INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" INS ON INS."IdInstrumento" = CUS."IdInstrumento"
	GROUP BY
		CUS."IdCliente"
	   ,CUS."IdPortafolio"
	   ,INS."IdClasificacionTipoInstrumento"
	   ,INS."IdTipoInstrumento"
	   ,CASE WHEN CUS."NombreTipoInstrumento" = '' THEN INS."NombreTipoInstrumento" ELSE CUS."NombreTipoInstrumento" END
	   ,INS."NombreTipoInstrumento"
	   ,CUS."IdMonedaOperacion";