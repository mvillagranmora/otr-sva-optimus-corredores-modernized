-- Aportes en transito por reinversion (se usa Fecha Curse(FechaAceptacion) para filtrar)

WITH FFMM as (
	SELECT 
	    (
		    CASE 
		    	WHEN SOL."FechaAceptacion" >= :FechaHasta THEN TO_DATE(SOL."FechaAceptacion") 
		    	ELSE TO_DATE(:FechaDesde) 
		    END
	    ) AS "FechaLiquidacion"

	    
		,SOL."IdPortafolio"
		,SOL."IdMoneda" AS "IdMoneda"
		,TO_DECIMAL(0,26,8) as "Monto"
		,CASE WHEN SOL."IdOperacionSpot" > 0 THEN
		 	  CASE 	WHEN SAL."IdMoneda" = SOL."IdMonedaPrincipal" THEN SOL."MontoMonedaPrincipal"
		 	   	   	WHEN SAL."IdMoneda" = SOL."IdMonedaSecundaria" THEN SOL."MontoMonedaSecundaria"
		 	  		ELSE SOL."CostoMonedaLocal"
		 	  END
		ELSE SOL."CostoMonedaLocal"
		END AS "MontoAporte"
		,TO_DECIMAL(0,26,8) AS "MontoRescate"
		,'APORTES_EN_TRANSITO_POR_REINVERSION' AS "ItemCalculo"
		
	FROM "BSCL.Plataforma.BD::GBL.SaldoFlujoCaja_Proceso_Paso" SAL
	INNER JOIN "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescateFlujoCaja" AS SOL
		ON (SAL."IdPortafolio" = SOL."IdPortafolio" AND
			SAL."IdMoneda" = SOL."IdMoneda" AND
			SOL."EstadoSolicitud" IN ('ING','PEN') AND
			SOL."TipoSolicitud" = 'APO' AND
			SOL."IdTipoMovimientoFondoMutuo" = 'APOREINV' AND  -- Aportes por reinversion en transito
			SOL."IdEmpresa" = :IdEmpresa AND
			SOL."FechaAceptacion" <= :FechaHasta)			
	)
	
SELECT "IdPortafolio" || '_' || "IdMoneda" AS "Id","FechaLiquidacion","IdPortafolio","IdMoneda",SUM("Monto") as "Monto",SUM("MontoAporte") as "MontoCompra",SUM("MontoRescate") as "MontoVenta","ItemCalculo"
,:TipoCalculoFlujoCaja AS "TipoCalculoFlujoCaja"
FROM FFMM 
GROUP BY "FechaLiquidacion","IdPortafolio","IdMoneda","ItemCalculo";



