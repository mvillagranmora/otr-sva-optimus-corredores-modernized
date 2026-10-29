SELECT 
	FWD."IdCartera" AS "IdCartera"
	,FWD."ModalidadLiquidacion" AS "ModalidadLiquidacion"
	,FWD."IdEstado" AS "IdEstado"
	,IND."ValorIndicador" AS "OperaCuentaInversion"
FROM "BSCL.Mesa.BD.Vistas::FWD.UIConsultarMovimientosFWDVencimiento" FWD
INNER JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" IND
	ON FWD."IdCliente" = IND."IdCliente" AND FWD."IdPortafolio" = IND."IdPortafolio"
WHERE "IdCartera" = :IdCartera
AND IND."CodigoTipoIndicador" = 'GEN_OCCINV';