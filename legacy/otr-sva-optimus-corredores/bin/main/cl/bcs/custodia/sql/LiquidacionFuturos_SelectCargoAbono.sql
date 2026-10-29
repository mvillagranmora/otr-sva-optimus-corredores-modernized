
WITH FUTUROS AS (
Select 	 
	FUT."NumeroCargoAbono"														AS "NumeroCargoAbono",
	FUT."IdPortafolio"															AS "IdPortafolio",		
   	FUT."IdCondicionDeLiquidacion"												AS "IdCondicionDeLiquidacion",	
	FUT."FechaCondicionDeLiquidacion"											AS "FechaCondicionDeLiquidacion",
	CASE 
		WHEN :TipoVencimiento = 'D' THEN 
			(FUT."MontoATasaMercadoActual" - FUT."MontoATasaMercadoAnterior")	
		ELSE 
			(FUT."MontoATasaMercadoActual" - FUT."PrecioSubyacente") -- PENDIENTE P*Q*LP
		END																		AS "Monto",
	TO_BIGINT(0)																AS "IdEjecutivo",
	TO_BIGINT(0)																AS "IdSucursal",
	''																			AS "IdTipoProducto",			
	FUT."NumeroCargoAbono"														AS "IdOperacion",
	FUT."FechaOperacion"														AS "FechaOperacion",			
	FUT."FechaLiquidacionOperacion"												AS "FechaLiquidacionOperacion",	
	FUT."FechaVencimientoOperacion"												AS "FechaVencimientoOperacion",	
	''																			AS "IdMonedaOperacion",			
	TO_DECIMAL(0,26,8) 															AS "MontoOperacion",			
	TO_DECIMAL(0,26,8) 															AS "TipoCambioOperacion",		
	TO_BIGINT(0)																AS "IdContraparte",				
	FUT."IdTipoComprobante"														AS "IdTipoComprobante",			
	TO_BIGINT(0)																AS "IdComprobante",				
	FUT."FolioComprobante"														AS "FolioComprobante",
	TO_BIGINT(0)																AS "IdComprobanteDetalle",		
	TO_BIGINT(0)																AS "IdComprobanteFormaDePago",
	FUT."TipoMovimientoAsociado" 												AS "IdTipoMovimientoAsociado",
	TO_BIGINT(1)																AS "IdMovimientoAsociado", 
	FUT."FechaComprobante"														AS "FechaComprobante",			
	TO_BIGINT(0)																AS "IdPersonaPagoCobro",		
	FUT."IdInstrumento"															AS "IdInstrumento",				
	FUT."IdTipoInstrumento"														AS "IdTipoInstrumento",
	FUT."Nemotecnico"															AS "Nemotecnico",
	''																			AS "EsIndicadorBilateral",		
	'N'																			AS "EsCargoAbonoPorTraspaso",	
	TO_BIGINT(0)																AS "IdPortafolioTraspaso",		
	'N'																			AS "GeneroContraparteEnTesoreria",
	'S'																			AS "CertificadoAutomatico",
	:TipoVencimiento															AS "TipoVencimiento"	
FROM
	"BSCL.Custodia.BD.Vistas::DER.ConsultarCarteraFuturos"		FUT
WHERE 
	((FUT."FechaVencimientoOperacion" > :FechaSistema AND 'D' = :TipoVencimiento ) OR
	 (FUT."FechaVencimientoOperacion" = :FechaSistema AND 'V'= :TipoVencimiento ))

)

SELECT 
	"NumeroCargoAbono"				AS "NumeroCargoAbono",
	"IdPortafolio"					AS  "IdPortafolio",
	ABS("Monto")					AS	"Monto",
	CASE 
		WHEN "Monto"  > 0 
			THEN 'MOVABN' 
			ELSE 'MOVCAR' 
	END 							AS "IdTipoMovimiento",
	
	CASE
		WHEN "TipoVencimiento" = 'D' THEN 
		CASE
			WHEN "Monto"  > 0 
				THEN 	'ABLFDI' 
				ELSE 	'CALFDI'
			END	
		WHEN "TipoVencimiento" = 'V' THEN
		CASE 
			WHEN "Monto"  > 0 
				THEN 	'ABLFVE' 
				ELSE 	'CALFVE'
			END	
		END							AS "IdConcepto",
	CASE
		WHEN "TipoVencimiento" = 'D' THEN 
			Concat('Liquidación Diaria Futuros, Instrumento : ' , "Nemotecnico")	
		ELSE
			Concat('Liquidación Vencimiento Futuros, Instrumento : ' , "Nemotecnico")
		END
									AS "Descripcion",		
	CASE 
		WHEN "Monto"  > 0  
			THEN 	'LIFURE' 
			ELSE 	'LIFUPA'
	END 							AS "IdTipoOperacion",
	"FechaOperacion"				AS "FechaOperacion",
	"FechaLiquidacionOperacion"		AS "FechaLiquidacionOperacion",	
	"FechaVencimientoOperacion"		AS "FechaVencimientoOperacion",	
   	"IdCondicionDeLiquidacion"		AS "IdCondicionDeLiquidacion",	
	"FechaCondicionDeLiquidacion"	AS "FechaCondicionDeLiquidacion",	
	"IdMonedaOperacion"				AS "IdMonedaOperacion",
	"MontoOperacion"				AS "MontoOperacion",
	"TipoCambioOperacion"			AS "TipoCambioOperacion",
	"IdContraparte"					AS "IdContraparte",
	"IdTipoComprobante"				AS "IdTipoComprobante",
	"IdComprobante"					AS "IdComprobante",
	"FolioComprobante"				AS "FolioComprobante",
	"IdOperacion"					AS "IdOperacion",
	"IdComprobanteDetalle"			AS "IdComprobanteDetalle",
	"IdComprobanteFormaDePago" 		AS "IdComprobanteFormaDePago",
	"IdTipoMovimientoAsociado"		AS "IdTipoMovimientoAsociado",
	"IdMovimientoAsociado" 			AS "IdMovimientoAsociado",
	"FechaComprobante"				AS "FechaComprobante",
	"IdPersonaPagoCobro"			AS "IdPersonaPagoCobro",
	"IdInstrumento"					AS "IdInstrumento",
	"IdTipoInstrumento"				AS "IdTipoInstrumento",
	"Nemotecnico"					AS "Nemotecnico",
	"EsIndicadorBilateral"			AS "EsIndicadorBilateral",
	"EsCargoAbonoPorTraspaso" 		AS "EsCargoAbonoPorTraspaso",
	"IdPortafolioTraspaso"			AS "IdPortafolioTraspaso",
	"GeneroContraparteEnTesoreria"	AS "GeneroContraparteEnTesoreria",
	"CertificadoAutomatico"			AS "CertificadoAutomatico"	
	
FROM FUTUROS 
 
	
