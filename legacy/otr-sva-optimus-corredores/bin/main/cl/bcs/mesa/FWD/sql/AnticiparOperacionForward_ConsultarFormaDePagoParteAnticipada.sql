SELECT
     CPO."IdComprobantePagoOperacion"
    ,CPO."IdEmpresa"
    ,CPO."IdOperacion"
    ,CPO."IdIngresoEgreso"
    ,CPO."IdAbono"
    ,CPO."IdCargo"
    ,CPO."IdTipoProducto"
    ,CPO."RecibePaga"
    ,CPO."IdFormaDePago"
    ,CPO."IdModalidadPago"
    ,CPO."IdMoneda"
    ,CAR."IdMonedaPrincipal"
    ,CAR."IdMonedaSecundaria"
    --,CPO."IdMonedaLiquidacion"
    ,CPO."Monto"
    --,CPO."MontoLiquidacion"
    ,CAR."EGRMontoMonedaPrincipal" AS "MontoMonedaPrincipal"
    ,CAR."EGRMontoMonedaSecundaria" AS "MontoMonedaSecundaria"
    ,CPO."FechaIngresoComprobantePagoOperacion"
    ,CPO."FechaLiquidacionComprobantePagoOperacion"
    ,CPO."IdEstado"
    ,CPO."AfectarTesoreria"
    ,CAR."IdCartera"
    ,CAR."IdOperacion" AS "IdOperacionCartera"
    ,CAR."FolioCartera"
FROM "BSCL.Mesa.BD::MES.ComprobantePagoOperacion" CPO
INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" CAR
	ON (CPO."IdOperacion" = CAR."IdCartera" AND
		CPO."IdTipoProducto" = CAR."IdTipoProducto")
WHERE CAR."IdOperacion" IN (:IdOperacion)
  AND CAR."FolioCartera" IN (:FolioCartera)
  AND CAR."IdTipoProducto" = 'MEFW'
  AND CAR."IdEstado" = 'ANT'
  AND CAR."IdEmpresa" = :IdEmpresa;
