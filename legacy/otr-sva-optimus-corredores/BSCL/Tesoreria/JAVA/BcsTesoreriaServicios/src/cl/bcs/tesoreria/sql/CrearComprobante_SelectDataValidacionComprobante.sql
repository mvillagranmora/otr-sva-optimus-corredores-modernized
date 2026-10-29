-- Obtiene datos de los nuevos comprobantes para validaciones
 SELECT  CG."NumeroReferencia"
		,CG."IdComprobante"
		,COALESCE(CG."IdCaja",0) AS "IdCaja"
		,CG."IdMoneda"
		,CG."IdTipoPersona"
		,COALESCE(CON."GeneraComprobanteAutomaticamente", 'N') AS "GeneraComprobanteAutomaticamente"
		,COALESCE(TFP."IdMonedaFormaDePago",'') as "IdMonedaFormaDePago"
		,COALESCE(TCG."CantidadTipoComprobante",0) as "CantidadTipoComprobante"
    	,COALESCE(TCG."CantidadMoneda",0) as "CantidadMoneda"
    	,COALESCE(TCG."CantidadFechaLiquidacion",0) as "CantidadFechaLiquidacion"    		
    	,COALESCE(TFP."CantidadFormasDePago",0) as "CantidadFormasDePago"
    	,COALESCE(TFP."CantidadMonedaFormaDePago",0) as "CantidadMonedaFormaDePago"
    	,COALESCE(FPC."CantidadConModalidadCaja",0) as "CantidadConModalidadCaja"
    	,COALESCE(TCG."TotalMonto",0) as "TotalMonto"    		
    	,COALESCE(TFP."TotalMontoFormaDePago",0) as "TotalMontoFormaDePago"
	 		  
 FROM "BSCL.Tesoreria.BD::GBL.CrearComprobante_Comprobante" as CG
 INNER JOIN (SELECT  "IdComprobante"
	  				,COUNT(DISTINCT "TipoComprobante") as "CantidadTipoComprobante"
			    	,COUNT(DISTINCT "IdTipoMovimiento") as "CantidadIdTipoMovimiento"
			    	,COUNT(DISTINCT "IdConcepto") as "CantidadConcepto" 
			    	,COUNT(DISTINCT "IdMoneda") as "CantidadMoneda"
			    	,CASE WHEN COUNT(DISTINCT "IdMoneda") = 1 THEN MAX("IdMoneda") END as "IdMoneda"
			    	,COUNT(DISTINCT "FechaLiquidacion") as "CantidadFechaLiquidacion"
			    	,SUM("Monto") as "TotalMonto"			  
			   FROM "BSCL.Tesoreria.BD::GBL.CrearComprobante_Comprobante"
	  		  GROUP BY "IdComprobante"
	  		) AS TCG ON TCG."IdComprobante" = CG."IdComprobante"
  LEFT JOIN (SELECT  "IdComprobante"
	  				,COUNT(DISTINCT "IdComprobante") as "CantidadFormasDePago"
	 				,COUNT(DISTINCT "IdMoneda") as "CantidadMonedaFormaDePago"
	 				,CASE WHEN COUNT(DISTINCT "IdMoneda") = 1 THEN MAX("IdMoneda") ELSE '' END as "IdMonedaFormaDePago"
	  				,SUM("Monto") as "TotalMontoFormaDePago"			  
			   FROM "BSCL.Tesoreria.BD::GBL.CrearComprobante_ComprobanteFormaDePago"
	  		  GROUP BY "IdComprobante"
	 		) AS TFP ON TFP."IdComprobante" = CG."IdComprobante"
  LEFT JOIN (SELECT  "IdComprobante" 
  				     ,COUNT(DISTINCT "IdComprobante") as "CantidadConModalidadCaja"			 
			   FROM "BSCL.Tesoreria.BD::GBL.CrearComprobante_ComprobanteFormaDePago"
			   WHERE "IdModalidadDePago" = 'MODCAJ'
	  		  GROUP BY "IdComprobante"
	 		) AS FPC ON FPC."IdComprobante" = CG."IdComprobante"
  LEFT JOIN "BSCL.Tesoreria.BD::TES.ConceptoTesoreria" AS CON ON (CON."IdConcepto" = CG."IdConcepto") 		
GROUP BY "NumeroReferencia",CG."IdComprobante","CantidadTipoComprobante", "CantidadMoneda","CantidadFechaLiquidacion",
	  	 "CantidadFormasDePago",CG."IdTipoPersona","CantidadMonedaFormaDePago","CantidadConModalidadCaja",CG."IdMoneda",
	  	 "IdMonedaFormaDePago","TotalMonto","TotalMontoFormaDePago",COALESCE(CG."IdCaja",0),COALESCE(CON."GeneraComprobanteAutomaticamente", 'N');
	