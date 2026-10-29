SELECT
	FWD."IdCartera" AS "IdCartera"
	,FWD."IdPortafolio" AS "IdPortafolio"
	,FWD."FolioOperacion" AS "FolioOperacion"
	,FWD."ModalidadLiquidacion" AS "ModalidadLiquidacion"
	,FWD."IdEstado" AS "IdEstadoFWD"
	,COM."IdComprobante" AS "IdComprobante"
	,COM."IdComprobanteFormaDePago" AS "IdComprobanteFormaDePago"
	,COM."IdEstado" AS "IdEstadoComprobante"
FROM "BSCL.Mesa.BD.Vistas::FWD.UIConsultarMovimientosFWDVencimiento" FWD
INNER JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" IND
	ON FWD."IdCliente" = IND."IdCliente" AND FWD."IdPortafolio" = IND."IdPortafolio"
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI
	ON FWD."IdCliente" = CLI."IdCliente"
INNER JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteFormaDePagoOrigen" COM
	ON COM."IdOperacion" = FWD."FolioOperacion"
WHERE FWD."IdCartera" = :IdCartera
AND IND."CodigoTipoIndicador" = 'GEN_OCCINV';