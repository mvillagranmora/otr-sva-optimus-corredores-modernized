UPDATE CAR
SET CAR."IdEstado" = 'VCD'
FROM "BSCL.Custodia.BD::CUS.Cartera" CAR
INNER JOIN "BSCL.Mesa.BD::MES.Parametro" AS PAR
	ON (PAR."IdParametro" = CAR."IdModalidadCumplimiento" AND
		PAR."NombreTipoParametro" = 'ModalidadLiquidacion' AND
		PAR."IdEmpresa" = CAR."IdEmpresa")
WHERE CAR."IdCartera" IN (:IdCartera)
  AND CAR."IdTipoProducto" = 'MEFW';
