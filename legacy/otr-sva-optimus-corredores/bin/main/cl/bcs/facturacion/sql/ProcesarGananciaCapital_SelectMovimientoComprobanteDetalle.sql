SELECT mcdt."IdMovimientoComprobanteDetalle"
FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobante" 	mct
	INNER JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobanteDetalle" mcdt 	ON (mcdt."IdMovimientoComprobante"=mct."IdMovimientoComprobante")
	INNER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" oper 	ON (oper."IdTipoOperacion"=mct."IdTipoOperacion" AND oper."IdTipoProducto"=mct."IdTipoProducto" AND oper."GeneraRetencionCmbte"='S')
	INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" 	cli 	ON (cli."IdCliente"=mct."IdCliente" AND  cli."IdTipoCliente" != 'INSTITUCIONAL')
	INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" 	por 	ON (mct."IdPortafolio"=por."IdPortafolio" AND  por."Proposito" NOT IN  ('PROPOSITOAPV','REGAPV'))
	LEFT JOIN "BSCL.Plataforma.BD::INS.MarketMaker" 	MKM ON (MKM."IdInstrumento"=mct."IdInstrumento" AND MKM."IdEstado"='ACT')
	INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" 	ins 	ON (ins."IdInstrumento"=mct."IdInstrumento" 
																		AND  ( (ins."AfectoArt107" !='S' AND ins."PresenciaBursatilAjustada">=25) OR (ins."AfectoArt107" !='S' AND COALESCE(MKM."IdInstrumento",0)>0) ) )
WHERE mcdt."IdMovimientoComprobanteDetalle" IN (:IdMovimientoComprobanteDetalle) and mct."IdComprobanteReferencia"=0;