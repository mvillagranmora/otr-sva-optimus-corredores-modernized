SELECT
     MES."NombreMesa"
    ,MES."IdEstadoMesa"
    ,MDL."IdEstado" AS "IdEstadoModulo"
    ,MDL."NombreModulo"
    ,COALESCE(CRI."CantidadCriterios",0)		AS "CantidadCriterios"
    ,COALESCE(PARDO."TipoCambioMercado",0)		AS "TipoCambioDO"
    ,COALESCE(PARUF."TipoCambioMercado",0)		AS "TipoCambioUF"
    ,COALESCE(PARUSD."TipoCambioMercado",0)		AS "TipoCambioUSD"
    ,COALESCE(PAREUR."TipoCambioMercado",0)		AS "TipoCambioEUR"
    ,COALESCE(PAREUR."ParidadMercado",0)		AS "ParidadMercadoEUR" 
    ,COALESCE(PARUSD."PrecioSpotCompra",0)		AS "PrecioSpotCompraUSD"
    ,COALESCE(PARUSD."PrecioSpotVenta",0)		AS "PrecioSpotVentaUSD"
    ,COALESCE(PARUSD."PrecioSpotPromedio",0)	AS "PrecioSpotPromedioUSD"
FROM DUMMY
LEFT JOIN (
		SELECT MES."Nombre" AS "NombreMesa", MES."IdEstadoMesa", MES."IdEmpresa"
		FROM "BSCL.Mesa.BD::MES.Mesa" MES
		INNER JOIN "BSCL.Mesa.BD::MES.TipoProductoMesaModulo" TPMM
			ON (MES."IdTipoProducto" = TPMM."IdTipoProductoMesa" AND
				TPMM."IdEstado" = 'ACT' AND
				MES."IdEmpresa" = TPMM."IdEmpresa")
		WHERE TPMM."IdTipoProductoMesa" = 'MEFW'
		  AND MES."IdEstadoRegistro" = 'ACT'
		  AND MES."IdEmpresa" = :IdEmpresa
	) MES
	ON (MES."IdEmpresa" = :IdEmpresa)
LEFT JOIN (
		SELECT MODULO."IdEmpresa", MODULO."CodigoModulo", MODULO."IdEstado", MODULOPLA."Nombre" AS "NombreModulo"
		FROM "BSCL.Plataforma.BD::PAU.Modulo" MODULO
		LEFT JOIN "BSCL.Plataforma.BD::PLA.Modulo" MODULOPLA
			ON (MODULOPLA."IdModulo" = MODULO."CodigoModulo")
	) MDL
	ON (MDL."IdEmpresa" = MES."IdEmpresa" AND
		MDL."CodigoModulo" = 'MFW')
LEFT JOIN (
		SELECT CST."IdEmpresa", COUNT(1) AS "CantidadCriterios"
		FROM "BSCL.Plataforma.BD::TAS.CriterioSeleccionTasas" CST
		INNER JOIN "BSCL.Plataforma.BD::TAS.Fuente" FUE
			ON (CST."CodigoFuente" = FUE."Codigo" AND
				CST."IdEmpresa" = FUE."IdEmpresa" AND
				FUE."NombreEntidad" = 'MANTENEDOR')
		WHERE CST."IdEmpresa" = :IdEmpresa
		  AND CST."Borrado" = 'N'
		  AND CST."IdClasificacionTipoInstrumento" = 'DV'
		  AND CST."IdTipoInstrumento" = 'FWD'
		GROUP BY CST."IdEmpresa"
	) CRI
	ON (MES."IdEmpresa" = CRI."IdEmpresa")
LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PARDO
	ON (PARDO."IdMoneda" = 'DO' AND
		TO_CHAR(PARDO."Fecha",'YYYY-MM-DD') = :FechaProceso AND
		PARDO."IdEstado" = 'ACT')
LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PARUF
	ON (PARUF."IdMoneda" = 'UF' AND
		TO_CHAR(PARUF."Fecha",'YYYY-MM-DD') = :FechaProceso AND
		PARUF."IdEstado" = 'ACT')
LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PARUSD
	ON (PARUSD."IdMoneda" = 'USD' AND
		TO_CHAR(PARUSD."Fecha",'YYYY-MM-DD') = :FechaProceso AND
		PARUSD."IdEstado" = 'ACT')
LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PAREUR
	ON (PAREUR."IdMoneda" = 'EUR' AND
		TO_CHAR(PAREUR."Fecha",'YYYY-MM-DD') = :FechaProceso AND
		PAREUR."IdEstado" = 'ACT');
