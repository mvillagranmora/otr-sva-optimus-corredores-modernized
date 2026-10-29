SELECT
	 CAST (SPT."IdOperacionSpotOrigen" AS NVARCHAR) AS "NumeroOperacion"
	,SPT."IdOperacionSpot"
	,SPT."IdCliente"
	,SPT."IdPortafolio"
	,SPT."FechaOperacion"
	,SPT."IdMesa"
	,SPT."IdComprobanteFacturacion"
	,SPT."IdTipoOperacion"
	,INS."Nemotecnico"
	,SPT."MontoMonedaSecundaria"
	,SPT."IdMonedaPrincipal"
	,SPT."TipoCambioCierre"
	,SPT."Estado"
FROM "BSCL.Mesa.BD::SPT.OperacionSpot" SPT
INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" INS
	ON (SPT."IdInstrumento" = INS."IdInstrumento")
WHERE SPT."IdOperacionSpotOrigen" IN (:NumeroOperacion)
  AND SPT."Estado" IN ('VIG','PEN') 
  AND SPT."IdEmpresa" = :IdEmpresa;