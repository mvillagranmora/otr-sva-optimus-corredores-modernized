
-------------------------------------------------------------------------------------
-- Obtiene y graba mas datos de las formas de pago de los comprobantes a liquidar
-------------------------------------------------------------------------------------

INSERT INTO "BSCL.Tesoreria.BD::GBL.LiquidarComprobante_TablaComprobanteFormaDePago"
(	
	   	 "IdInterno"							--BIGINT,
	   	,"IdComprobanteInformado"				--BIGINT,
	   	,"IdComprobanteFormaDePagoInformado"	--BIGINT,
	   	,"IdComprobanteFormaDePago"				--BIGINT,
	   	,"IdCuentaBancariaCorredora"			--BIGINT,
		,"IdEstadoFormaDePago" 					--NVARCHAR(3),
		,"IdEstadoComprobante" 					--NVARCHAR(3),
		,"TipoMedioDePago"						--NVARCHAR(40),
		,"LiquidacionParcialEnviaACTA"			--NVARCHAR(40),
		,"EstadosPosibleTipoMovimientoMedioDePago" 	--NVARCHAR(40),
		,"AfectaSaldoEnEstado" 					--NVARCHAR(40),
		,"IdMoneda"								--NVARCHAR(3),
		,"Monto"								--DECIMAL(26,8),
		,"IdDeposito"							--BIGINT,
		,"IdEstadoDepositoDetalle"  			--NVARCHAR(3),
		,"FechaLiquidacion"   					--DATE,
		,"DiasRetencion"  						--BIGINT,
		,"IdMedioDePago"  						--NVARCHAR(3),
		,"IdCaja"  								--BIGINT
		
) 
SELECT  	
			TC."IdComprobante" AS "IdInterno"
		   ,TC."IdComprobante" AS "IdComprobanteInformado"
		   ,COALESCE(CFP."IdComprobanteFormaDePagoInformado",0) AS "IdComprobanteFormaDePagoInformado" 
		   ,COALESCE(CFP."IdComprobanteFormaDePago",0) AS "IdComprobanteFormaDePago" 
		   ,COALESCE(CFP."IdCuentaBancariaCorredora",0) AS "IdCuentaBancariaCorredora"
		   ,COALESCE(CFP."IdEstadoFormaDePago",'') AS "IdEstadoFormaDePago" 
		   ,COALESCE(COM."IdEstado",'') AS "IdEstadoComprobante"
		   ,COALESCE(CFG."TipoMedioDePago",'') AS "TipoMedioDePago" 
		   ,CFG."LiquidacionParcialEnviaACTA"
		   ,CFG."EstadosPosibleTipoMovimientoMedioDePago"
		   ,COALESCE(CFG."AfectaSaldoEnEstado",'') AS "AfectaSaldoEnEstado" 
		   ,COALESCE(CFP."IdMoneda",'') AS "IdMoneda" 
		   ,COALESCE(CFP."Monto",0) AS "Monto" 
		   ,COALESCE(CFP."IdDeposito",0) AS "IdDeposito" 
		   ,COALESCE(CFP."IdEstadoDepositoDetalle",'') AS "IdEstadoDepositoDetalle" 
		   ,ADD_DAYS (TO_DATE (:FechaMovimiento, 'YYYY-MM-DD'), COALESCE(CFP."DiasRetencion",0)) AS "FechaLiquidacion" -- Le suma dias de retencion
		   ,COALESCE(CFP."DiasRetencion",0) AS "DiasRetencion" 
		   ,COALESCE(CFP."IdMedioDePago",'') AS "IdMedioDePago" 
		   ,COALESCE(COM."IdCaja",0) AS "IdCaja" 

 
 
  FROM "BSCL.Tesoreria.BD::GBL.LiquidarComprobante_TablaComprobante" AS TC
  
  
	  LEFT JOIN "BSCL.Tesoreria.BD::TES.Comprobante" AS COM ON (COM."IdComprobante" = TC."IdComprobante")	  
		
	  LEFT JOIN (SELECT  CFP."IdComprobante"
	  					,COALESCE(TCFP."IdComprobanteFormaDePago",CFP."IdComprobanteFormaDePago") AS "IdComprobanteFormaDePagoInformado"
	  					,CFP."IdComprobanteFormaDePago"
	  					,CFP."IdTipoMovimiento"
	  					,CFP."IdMedioDePago"
						,CFP."IdCuentaBancariaCorredora"
						,CFP."IdEstado" AS "IdEstadoFormaDePago"
						,CFP."IdMoneda"
		   				,CFP."Monto"
		   				,DD."IdDeposito"
		   				,DD."IdEstado" AS "IdEstadoDepositoDetalle"
		   				,CFP."DiasRetencion"
	  			   FROM "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" AS CFP
	  			   LEFT JOIN "BSCL.Tesoreria.BD::TES.DepositoDetalle" AS DD ON DD."IdComprobanteFormaDePago" = CFP."IdComprobanteFormaDePago" AND DD."IdEstado" <> 'ANU'
	  			   LEFT JOIN "BSCL.Tesoreria.BD::GBL.LiquidarComprobante_FormasDePagoALiquidar" AS TCFP ON (TCFP."IdComprobante" = CFP."IdComprobante")	
	  			  WHERE CFP."IdEstado" NOT IN ('ANU','MOD')
	  			) as CFP ON (CFP."IdComprobante" = TC."IdComprobante") AND (CFP."IdComprobanteFormaDePagoInformado" = CFP."IdComprobanteFormaDePago")
	  LEFT JOIN (SELECT  TMMP."IdTipoMovimiento"
	 					,TMMP."IdMedioDePago"
	 					,MP."TipoMedioDePago"
	 					,CFIG."Valor" AS "LiquidacionParcialEnviaACTA" 					
	 					,TMMP."EstadosPosibleTipoMovimientoMedioDePago"
	 					,TMMP."AfectaSaldoEnEstado"	 					
	 			   FROM "BSCL.Plataforma.BD::PAG.MedioDePago" as MP
	 			  INNER JOIN "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago" as TMMP ON (TMMP."IdMedioDePago" = MP."IdMedioDePago"),
	 			  "BSCL.Tesoreria.BD::TES.ConfiguracionTesoreria" as CFIG
	 			  WHERE "CodigoParametro" = 'LIQUIDACIONPARCIALENVIAACTA'
	 			) AS CFG ON CFG."IdTipoMovimiento" = CFP."IdTipoMovimiento" AND CFG."IdMedioDePago" = CFP."IdMedioDePago"
	 WHERE CFP."IdEstadoFormaDePago" <> (CASE WHEN CFG."LiquidacionParcialEnviaACTA" = 'S' THEN 'LIQ' ELSE '' END);
	 
	 
	 