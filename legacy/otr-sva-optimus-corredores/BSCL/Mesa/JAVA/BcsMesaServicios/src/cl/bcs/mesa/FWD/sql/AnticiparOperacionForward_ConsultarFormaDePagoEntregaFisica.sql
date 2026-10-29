SELECT
     CPO."IdComprobantePagoOperacion"
    ,CPO."IdOperacion"
    ,CAR."FolioCartera"
FROM "BSCL.Mesa.BD::MES.ComprobantePagoOperacion" CPO
INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" CAR
	ON (CPO."IdTipoProducto" = CAR."IdTipoProducto")
INNER JOIN "BSCL.Mesa.BD::MES.Parametro" AS PAR
	ON (PAR."IdParametro" = CAR."IdModalidadCumplimiento" AND
		PAR."NombreTipoParametro" = 'ModalidadLiquidacion' AND
		PAR."IdEmpresa" = CAR."IdEmpresa" AND
		PAR."ValorParametro" = 'ENTREGA_FISICA')	
WHERE CPO."IdOperacion" IN (:IdCarteraParteAnticipada)
  AND CAR."FolioCartera" IN (:FolioCartera)
  AND CAR."IdOperacion" IN (:IdOperacion)
  AND CAR."IdTipoProducto" = 'MEFW'
  AND CAR."IdEstado" = 'ANT'
  AND CAR."IdEmpresa" = :IdEmpresa;