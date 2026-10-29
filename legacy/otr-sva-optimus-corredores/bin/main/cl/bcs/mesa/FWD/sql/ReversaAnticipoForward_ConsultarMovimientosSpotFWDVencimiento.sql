SELECT
	FWD."IdCartera" AS "IdCartera"
	,FWD."IdPortafolio" AS "IdPortafolio"
	,FWD."FolioOperacion" AS "FolioOperacion"
	,FWD."ModalidadLiquidacion" AS "ModalidadLiquidacion"
	,FWD."IdEstado" AS "IdEstadoFWD"
	,OSP."IdOperacionSpot" AS "IdOperacionSPOT"
	,OSP."IdTipoOperacion" AS "IdTipoOperacionSPOT"
	,OSP."Estado" AS "EstadoSPOT"
	,OSP."IdComprobanteFacturacion" AS "IdComprobanteFacturacion"
FROM "BSCL.Mesa.BD.Vistas::FWD.UIConsultarMovimientosFWDVencimiento" FWD
INNER JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" IND
	ON FWD."IdCliente" = IND."IdCliente" AND FWD."IdPortafolio" = IND."IdPortafolio"
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI
	ON FWD."IdCliente" = CLI."IdCliente"
INNER JOIN "BSCL.Mesa.BD::SPT.OperacionSpot" OSP
	ON OSP."FolioOrigenFW" = FWD."IdOperacion"
WHERE FWD."IdCartera" = :IdCartera
AND IND."CodigoTipoIndicador" = 'GEN_OCCINV';