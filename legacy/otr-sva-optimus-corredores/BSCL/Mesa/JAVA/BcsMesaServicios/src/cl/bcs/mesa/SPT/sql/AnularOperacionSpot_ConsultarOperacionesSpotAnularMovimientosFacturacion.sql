SELECT
	 CAST (SPT."IdOperacionSpot" AS NVARCHAR) AS "NumeroOperacion"
	,SPT."IdOperacionSpot"
	,SPT."IdCliente"
	,SPT."IdPortafolio"
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
INNER JOIN "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobante" MOVFAC
	ON (SPT."IdComprobanteFacturacion" = MOVFAC."IdMovimientoComprobante")
LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobante" MOVNCR
	ON (MOVFAC."IdComprobante" = MOVNCR."IdComprobanteReferencia" AND
		MOVNCR."IdTipoComprobante" IN ('NCE','NCR','NEO','NCO','NEE'))
WHERE SPT."IdOperacionSpot" IN (:NumeroOperacion)
  AND SPT."IdEmpresa" = :IdEmpresa
  AND SPT."IdComprobanteFacturacion" <> 0
  AND SPT."OperacionFicticia" <> 'S'
  AND (MOVFAC."IdEstadoFacturacion" <> 'FAC' OR (MOVFAC."IdEstadoFacturacion" = 'FAC' AND MOVNCR."IdComprobanteReferencia" IS NOT NULL));
