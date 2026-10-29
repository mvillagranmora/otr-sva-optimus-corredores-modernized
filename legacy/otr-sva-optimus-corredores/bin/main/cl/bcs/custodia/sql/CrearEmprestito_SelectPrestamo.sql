SELECT TOP 1
	CAR."IdCliente"
   ,CAR."IdPortafolio"
   ,CAR."FolioComprobante"
   ,CAR."IdTipoComprobante"
   ,CAR."FechaComprobante"
   ,CAR."IdTipoPrestamo"
   ,CAR."UsaGarantiaEspecifica"
   ,CAR."IdBolsa"
   ,CAR."FolioPrestamo"
   ,CAR."FolioSistemaExterno"
   ,CAR."TipoCustodia"
   ,CAR."ExigirGarantia"
   , CASE WHEN CAR."TipoCustodia"  = 'S'  THEN 'C' 
	 else  'G'   END
	 AS "TipoCustodia"
   ,CAR."IdBoveda"
   ,CAR."IdConcepto"
   ,IFNULL(ATR."ValorIndicador",'N') AS "ConCustodiaPrestatario"
   ,TP."IdTipoInstrumentoEmprestito"
   ,TP."NeteaCarteraSobregiro"
FROM "BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenPrestamoVigente" CAR
LEFT JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" TPO ON TPO."CodigoTipoIndicador" = 'CC'
LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" ATR ON ATR."IdTipoIndicador" = TPO."IdTipoIndicador" AND ATR."IdTipoEntidad" = 'CLIENTE' AND ATR."IdEntidad" = TO_VARCHAR(CAR."IdCliente")
LEFT JOIN "BSCL.Custodia.BD::PRT.TipoPrestamo" TP ON TP."IdTipoPrestamo" = CAR."IdTipoPrestamo"
WHERE "FolioPrestamo" = :p_FolioCarteraPrestamo;