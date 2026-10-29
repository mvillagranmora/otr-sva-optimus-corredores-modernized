SELECT -- VENTAS Y COMPRAS A TERMINO CON F.LIQ POSTERIOR A FECHA CC:
		CAR."EGRIdTipoOperacion" AS "IdTipoOperacion"
		,CASE WHEN CAR."EGRIdTipoOperacion" = 'COMTER' THEN MCET."IdPortafolioRelacionado" ELSE MCET."IdPortafolio" END AS "IdPortafolio"
		,CAR."IdPortafolio" AS "IdPortafolioRelacionado"
		,CASE WHEN CAR."EGRIdTipoOperacion" = 'COMTER' THEN MCET."IdClienteRelacionado" ELSE MCET."IdCliente" END AS "IdCliente"
		,CAR."IdCliente" AS "IdClienteRelacionado"
  		,'VIG' AS "IdEstado"
  		,CAR."EGRCantidad" AS "Cantidad"
  		,CAR."IdInstrumento" AS "IdInstrumento"
FROM "BSCL.Custodia.BD::CUS.Cartera" AS CAR
INNER JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS MCET ON MCET."IdMovimientoCarteraEnTransito" = CAR."EGRIdMovimientoCarteraEnTransito"
WHERE CAR."IdClasificacionTipoInstrumento"   = 'RF'
AND (CAR."INGFechaPagoCorteCupon" BETWEEN :FechaCorteCuponDesde AND :FechaCorteCuponHasta)
AND (CAR."EGRFechaLiquidacion" >= :FechaCorteCuponDesde AND CAR."EGRCantidad" <> 0 AND CAR."EGRRetiroPorFusion" = 'N')  
AND CAR."INGFechaCorteCupon" < CAR."EGRFechaLiquidacion"
AND CAR."IdTipoInstrumento" != 'SIM'
AND CAR."IdClasificacionTipoInstrumento" != 'DV'
AND CAR."IdEstado" != 'ANU'
AND CAR."EGRIdTipoOperacion" IN ('VENTER','COMTER')
AND CAR."IdInstrumento" IN (CASE WHEN (COALESCE(:IdInstrumento,'') = '') THEN CAR."IdInstrumento" ELSE :IdInstrumento END)