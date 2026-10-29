SELECT
	 CAR."IdCartera"
	,CAR."IdOperacion"
	,CAR."FolioCartera"
	,CAR."IdMonedaPrincipal"
	,CAR."IdMonedaSecundaria"
	,CAR."EGRIdTipoOperacion"
	,CAR."INGMontoMonedaPrincipal"
	,CAR."INGMontoMonedaSecundaria"
	,CAR."EGRMontoMonedaPrincipal"
	,CAR."EGRMontoMonedaSecundaria"
	,CAR."FechaOperacion"
	,CAR."ValorTipoCambioCierre"
	,CAR."ValorParidadCierre"
	,CASE WHEN CAR."PlazoEnDiasOperacion" > 0 THEN CAR."PlazoEnDiasOperacion"
		  WHEN CAR."FechaVencimientoOperacion" > TO_DATE('0001-01-01','YYYY-MM-DD') THEN DAYS_BETWEEN(CAR."FechaOperacion", CAR."FechaVencimientoOperacion")
		  ELSE 0 END AS "Plazo"
	,CAR."FechaVencimientoOperacion"
	,TO_DATE(TO_VARCHAR(OPE."FechaLiquidacion",'YYYY-MM-DD'),'YYYY-MM-DD') AS "FechaLiquidacion"
	,CAR."ValorTipoCambioCosto"
	,CAR."EGRIdSpotAsociado"
	,CAR."IdEstado"
	,CAR."EGRMontoARecibirAnticipo"
FROM "BSCL.Custodia.BD::CUS.Cartera" AS CAR
INNER JOIN "BSCL.Mesa.BD::MES.Operacion" AS OPE
	ON (OPE."IdOperacion" = CAR."IdOperacion" AND
		OPE."IdTipoProducto" = CAR."IdTipoProducto")
WHERE CAR."FolioCartera" IN (:FolioCartera)
  AND CAR."IdTipoProducto" = 'MEFW'
  AND CAR."IdEstado" IN ('VIG')
  AND OPE."IdEmpresa" = :IdEmpresa;
