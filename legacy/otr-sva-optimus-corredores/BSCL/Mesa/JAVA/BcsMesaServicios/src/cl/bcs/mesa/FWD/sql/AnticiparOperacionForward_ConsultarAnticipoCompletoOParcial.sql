SELECT
	 CAR."IdCartera"
	,CAR."FolioCartera"
	,CAR."IdOperacion"
	,CAR."IdTipoOperacion"
	,CAR."IdTipoProducto"
	,CAR."INGMontoMonedaPrincipal" AS "MontoMonedaPrincipal"
	,CAR."INGMontoMonedaSecundaria" AS "MontoMonedaSecundaria"
	,CAR."FechaLiquidacion"
	,CAR."FechaOperacion"
	,CAR."IdModalidadCumplimiento"
	,PAR."ValorParametro" AS "ModalidadLiquidacionValor"
FROM "BSCL.Custodia.BD::CUS.Cartera" CAR
INNER JOIN "BSCL.Mesa.BD::MES.Parametro" AS PAR
	ON (PAR."IdParametro" = CAR."IdModalidadCumplimiento" AND
		PAR."NombreTipoParametro" = 'ModalidadLiquidacion' AND
		PAR."IdEmpresa" = CAR."IdEmpresa")
WHERE CAR."IdOperacion" IN (:IdOperacion)
  AND CAR."FolioCartera" IN (:FolioCartera)
  AND CAR."IdTipoProducto" = 'MEFW'
  AND CAR."IdEstado" = 'VIG'
  AND CAR."IdEmpresa" = :IdEmpresa;
