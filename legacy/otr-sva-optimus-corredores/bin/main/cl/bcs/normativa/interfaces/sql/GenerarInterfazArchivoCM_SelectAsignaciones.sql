SELECT 
	 "FechaOperacion"
	,"ModalidadOperacion" --FALTA
	,"CorrelativoBVL" AS "NumeroOperacion"
	,"CodigoParticipante" --FALTA
	,"TipoDetalle"
	,"CodigoTitular"--FALTA
	,"CantidadCavali"
	,"CantidadFisica" --FALTA
	,"Giro" --FALTA
	,"FlagTrabajador" --FALTA
	,"ComisionContado" --FALTA
	,"ComisionPlazo" --FALTA
	,"FlagPoliza" --FALTA
	,"FlagContado" --FALTA
	,"FlagPlazo" --FALTA
	,"CodigoCustodioValores" --FALTA
	,"CodigoCustodioFondo" --FALTA
	,"InteresContado" --FALTA
	,"InteresPlazo" --FALTA
	,"LiquidacionDTC" --FALTA
	,"FlagDayTrade" --FALTA
	,"FechaVencimientoDocumento"
	,"IndicadorCartaFianza"
	,"MontoCartaFianza"
	,"CodigoBanco"
FROM "BSCL.Normativa.BD.Vistas::Interfaces.ConsultarOperacionCM" 
WHERE "IdEmpresa" = :IdEmpresa AND TO_CHAR("FechaMovimientoComprobante",'YYYY-MM-DD') = TO_CHAR(:FechaDesde,'YYYY-MM-DD')