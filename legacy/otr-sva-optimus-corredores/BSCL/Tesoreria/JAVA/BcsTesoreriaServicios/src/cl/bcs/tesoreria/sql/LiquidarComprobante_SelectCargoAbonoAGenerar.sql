
-------------------------------------------------------------
-- Obtiene y procesa los datos para el envio a cuenta inversion
-------------------------------------------------------------


	-- Afectar la cuenta inversion de la corredora ("AfectaCuentaInversion" = 'COR' OR "AfectaCuentaInversion" = '')
	 SELECT
	 			 deta."IdComprobanteDetalle" as "NumeroCargoAbono" -- a cta inversion se envia por cada detalle
	 			,COR."IdPortafolioCorredora" as "IdPortafolio"
	 	 		,COM."IdMoneda"
	 	 		,COM."IdTipoMovimientoCtaInvCorredora" as "IdTipoMovimiento" 
	 	 		,CASE WHEN deta."IdConcepto" = 'INGBON' THEN 'ABOBON' /*APV - Se modifica concepto para la Cta Inv*/
	 	 		 ELSE COM."IdConcepto" 
	 	 		 END AS "IdConcepto"
	 	 		 ,'Liquidacion Comprobante Tesoreria (' || CAST(orig."IdComprobante" AS NVARCHAR(20)) || ')' as "Descripcion"
	 	 		 
	 	 		,'T0' as "IdCondicionDeLiquidacion"
	 	 		,:FechaMovimiento as "FechaCondicionDeLiquidacion"
	 	 		,COM."Monto"
	 	 		,CAST(0 AS BIGINT) as "IdEjecutivo"
	 			,CAST(0 AS BIGINT) as "IdSucursal"
	 	 		,deta."IdTipoProducto"
	 	 		,CASE 
	 	 			WHEN COALESCE(deta."IdComprobanteOrigen",0) <> 0 THEN deta."IdTipoOperacion"
	 	 			ELSE ''
	 	 		END AS "IdTipoOperacion"
			    ,deta."IdComprobanteOrigen" AS "IdOperacion"
			    ,CAST(orig."FechaMovimiento"  AS NVARCHAR(40)) as "FechaOperacion"
			    ,CAST(orig."FechaLiquidacion" AS NVARCHAR(40)) as "FechaLiquidacionOperacion"
			    ,CAST(orig."FechaLiquidacion" AS NVARCHAR(40)) as "FechaVencimientoOperacion"
			    
			    ,deta."IdMonedaOperacion"
			    ,orig."Monto" as "MontoOperacion"
			    ,null as "TipoCambioOperacion"
			    ,null as "IdContraparte"
			    ,COM."IdTipoComprobante" as "IdTipoComprobante"
			    ,COM."IdComprobante" as "IdComprobante"
			    ,null as "FolioComprobante"	
			    ,COM."IdComprobanteDetalle" as "IdComprobanteDetalle"
			    ,COM."IdComprobanteFormaDePago" as "IdComprobanteFormaDePago"
			    ,COM."IdTipoMovimientoAsociado"
			    ,COM."IdMovimientoAsociado"
			    ,COM."FechaMovimiento" as "FechaComprobante"
			    ,null as "IdPersonaPagoCobro"
			    ,null as "IdInstrumento"
			    ,null as "IdTipoInstrumento"
			    ,null as "Nemo"
			    ,null as "EsIndicadorBilateral"
			    ,'N' as "EsCargoAbonoPorTraspaso"
			    ,null as "IdPortafolioTraspaso"
			    ,'N' as "GeneroContraparteEnTesoreria"
			    ,'S' as "CertificadoAutomatico"
	FROM "BSCL.Tesoreria.BD::GBL.LiquidarComprobantes_Servicio_ComprobantesMuevenCuentaInversion" as COM	

 	LEFT JOIN "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" deta ON deta."IdComprobante" = COM."IdComprobante" AND deta."IdComprobanteDetalle" = COM."IdComprobanteDetalle"
 	 
	LEFT JOIN "BSCL.Tesoreria.BD::TES.Comprobante" orig ON orig."IdComprobante" = COM."IdComprobante"
	LEFT JOIN
	(    -- Datos de la corredora         
          SELECT  CC."IdPortafolioDefecto" as "IdPortafolioCorredora"
		   FROM "BSCL.Plataforma.BD::PLA.Empresa" AS E
		  INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" AS CSN ON CSN."IdSocioDeNegocio" = E."IdSocioDeNegocio"
		  INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CC ON CC."IdCliente" = CSN."IdCliente"		  
	 	  WHERE E."IdEmpresa" = :IdEmpresa	 	  
	) as COR ON (1=1)
	WHERE (COM."AfectaCuentaInversion" = 'COR' OR COM."AfectaCuentaInversion" = '')
	AND COALESCE(COR."IdPortafolioCorredora",0) > 0



UNION ALL
	



	-- Afectar la cuenta inversion de los clientes( "AfectaCuentaInversion" = 'CLI' OR "AfectaCuentaInversion" = '')
	SELECT
				 deta."IdComprobanteDetalle" as "NumeroCargoAbono" -- a cta inversion se envia por cada detalle
				,CASE 
					WHEN IngEgr."IdModulo" = 'TES' AND IngEgr."IdTipoMovimiento" = 'MOVEGR' AND IngEgr."IdTipoPersona" = 'CLI'  AND IngEgr."IdTipoCanalIngreso" = 'OPT'
						THEN IngEgr."IdPortafolio"
						ELSE COM."IdPortafolio"
				 END AS "IdPortafolio"
	 	 		,COM."IdMoneda"
	 	 		,COM."IdTipoMovimientoCtaInvCliente" as "IdTipoMovimiento"
	 	 		,CASE WHEN deta."IdConcepto" = 'INGBON' THEN 'ABOBON' /*APV - Se modifica concepto para la Cta Inv*/
	 	 		 ELSE COM."IdConcepto" 
	 	 		 END AS "IdConcepto"
	 	 		,'Liquidacion Comprobante Tesoreria (' || CAST(orig."IdComprobante" AS NVARCHAR(20)) || ')' as "Descripcion"
	 	 		,'T0' as "IdCondicionDeLiquidacion"
	 	 		,:FechaMovimiento as "FechaCondicionDeLiquidacion"
	 	 		,COM."Monto"
	 	 		,CAST(0 AS BIGINT) as "IdEjecutivo"
	 			,CAST(0 AS BIGINT) as "IdSucursal"
	 	 		,deta."IdTipoProducto"
	 	 		,CASE 
	 	 			WHEN COALESCE(deta."IdComprobanteOrigen",0) <> 0 THEN deta."IdTipoOperacion"
	 	 			ELSE ''
	 	 		END AS "IdTipoOperacion"
			    ,deta."IdComprobanteOrigen" AS "IdOperacion"
			    
			    ,CAST(orig."FechaMovimiento"  AS NVARCHAR(40)) as "FechaOperacion"
			    ,CAST(orig."FechaLiquidacion"  AS NVARCHAR(40)) as "FechaLiquidacionOperacion"
			    ,CAST(orig."FechaLiquidacion"  AS NVARCHAR(40)) as "FechaVencimientoOperacion"
			    
			    ,deta."IdMonedaOperacion"
			    ,orig."Monto" as "MontoOperacion"
			    ,null as "TipoCambioOperacion"
			    ,null as "IdContraparte"			    
			    ,COM."IdTipoComprobante" as "IdTipoComprobante"
			    ,COM."IdComprobante" as "IdComprobante"
			    ,null as "FolioComprobante"	
			    ,COM."IdComprobanteDetalle" as "IdComprobanteDetalle"
			    ,COM."IdComprobanteFormaDePago" as "IdComprobanteFormaDePago"
			    ,COM."IdTipoMovimientoAsociado"
			    ,COM."IdMovimientoAsociado"
			    ,COM."FechaMovimiento" as "FechaComprobante"			    
			    ,COM."IdPersonaTitularPago" as "IdPersonaPagoCobro"
			    ,null as "IdInstrumento"
			    ,null as "IdTipoInstrumento"
			    ,null as "Nemo"
			    ,null as "EsIndicadorBilateral"
			    ,'N' as "EsCargoAbonoPorTraspaso"
			    ,null as "IdPortafolioTraspaso"
			    ,'N' as "GeneroContraparteEnTesoreria"
			    ,'S' as "CertificadoAutomatico"
	FROM "BSCL.Tesoreria.BD::GBL.LiquidarComprobantes_Servicio_ComprobantesMuevenCuentaInversion" as COM

	LEFT JOIN "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" deta ON deta."IdComprobante" = COM."IdComprobante" AND deta."IdComprobanteDetalle" = COM."IdComprobanteDetalle"
	
	LEFT JOIN "BSCL.Tesoreria.BD::TES.Comprobante" orig ON orig."IdComprobante" = COM."IdComprobante"
	
	-- OBTIENE DATOS DEL EGRESO ORIGINAL CON TERCERO RELACIONADO
	LEFT JOIN ( SELECT 
				COM."IdComprobante", 
				CDO."IdIngresoEgreso", 
				IE."IdPortafolio", 
				IE."IdModulo", 
				IE."IdTipoMovimiento", 
				IE."IdTipoPersona", 
				IE."IdPortafolioTitularPago", 
				IE."IdTipoCanalIngreso"
				FROM 		"BSCL.Tesoreria.BD::TES.Comprobante"  				AS COM
				INNER JOIN 	"BSCL.Tesoreria.BD::TES.ComprobanteDetalle" 		AS CD 	ON (CD."IdComprobante" 			= COM."IdComprobante")
				INNER JOIN 	"BSCL.Tesoreria.BD::TES.ComprobanteDetalleOrigen" 	AS CDO 	ON (CDO."IdComprobanteDetalle"	= CD."IdComprobanteDetalle")
				INNER JOIN 	"BSCL.Tesoreria.BD::TES.IngresoEgreso" 				AS IE 	ON (IE."IdIngresoEgreso" 		= CDO."IdIngresoEgreso")
				WHERE 	
					IE."IdModulo" 				 = 'TES' 				AND 
					IE."IdTipoMovimiento" 		 = 'MOVEGR' 			AND 
					IE."IdTipoPersona" 			 = 'CLI' 				AND 
					IE."IdPortafolioTitularPago" <> IE."IdPortafolio" 	AND 
					IE."IdTipoCanalIngreso" = 'OPT')							AS
					IngEgr
	ON  IngEgr."IdComprobante" = COM."IdComprobante"
	
	WHERE (COM."AfectaCuentaInversion"  = 'CLI' OR COM."AfectaCuentaInversion" = '')
	AND (COM."IdTipoPersonaTitularPago" = 'CLI' OR COM."IdTipoPersonaTitularPago" = '')
	AND CASE WHEN IngEgr."IdModulo" = 'TES' AND IngEgr."IdTipoMovimiento" = 'MOVEGR' AND IngEgr."IdTipoPersona" = 'CLI'  AND IngEgr."IdTipoCanalIngreso" = 'OPT'
		THEN 1
		ELSE COALESCE(COM."IdPortafolio",0)
		END > 0


UNION ALL
	



	-- Afectar la cuenta inversion de los clientes( "AfectaCuentaInversion" = 'CLI' OR "AfectaCuentaInversion" = '') para ingresosEgresos que provienen de un rescate por comision NN
	SELECT
				 deta."IdComprobanteDetalle" as "NumeroCargoAbono" -- a cta inversion se envia por cada detalle
				,CASE 
					WHEN IngEgr."IdModulo" = 'FAC' AND IngEgr."IdTipoMovimiento" = 'MOVEGR' AND IngEgr."IdTipoPersona" = 'CLI'  AND IngEgr."IdTipoCanalIngreso" = 'FMM'
						THEN IngEgr."IdPortafolio"
						ELSE COM."IdPortafolio"
				 END AS "IdPortafolio"
	 	 		,COM."IdMoneda"
	 	 		,COM."IdTipoMovimientoCtaInvCliente" as "IdTipoMovimiento"
	 	 		,CASE WHEN deta."IdConcepto" = 'INGBON' THEN 'ABOBON' /*APV - Se modifica concepto para la Cta Inv*/
	 	 		 ELSE COM."IdConcepto" 
	 	 		 END AS "IdConcepto"
	 	 		,'Liquidacion Comprobante Tesoreria (' || CAST(orig."IdComprobante" AS NVARCHAR(20)) || ')' as "Descripcion"
	 	 		,'T0' as "IdCondicionDeLiquidacion"
	 	 		,:FechaMovimiento as "FechaCondicionDeLiquidacion"
	 	 		,COM."Monto"
	 	 		,CAST(0 AS BIGINT) as "IdEjecutivo"
	 			,CAST(0 AS BIGINT) as "IdSucursal"
	 	 		,deta."IdTipoProducto"
	 	 		,CASE 
	 	 			WHEN COALESCE(deta."IdComprobanteOrigen",0) <> 0 THEN deta."IdTipoOperacion"
	 	 			ELSE ''
	 	 		END AS "IdTipoOperacion"
			    ,deta."IdComprobanteOrigen" AS "IdOperacion"
			    
			    ,CAST(orig."FechaMovimiento"  AS NVARCHAR(40)) as "FechaOperacion"
			    ,CAST(orig."FechaLiquidacion"  AS NVARCHAR(40)) as "FechaLiquidacionOperacion"
			    ,CAST(orig."FechaLiquidacion"  AS NVARCHAR(40)) as "FechaVencimientoOperacion"
			    
			    ,deta."IdMonedaOperacion"
			    ,orig."Monto" as "MontoOperacion"
			    ,null as "TipoCambioOperacion"
			    ,null as "IdContraparte"			    
			    ,COM."IdTipoComprobante" as "IdTipoComprobante"
			    ,COM."IdComprobante" as "IdComprobante"
			    ,null as "FolioComprobante"	
			    ,COM."IdComprobanteDetalle" as "IdComprobanteDetalle"
			    ,COM."IdComprobanteFormaDePago" as "IdComprobanteFormaDePago"
			    ,COM."IdTipoMovimientoAsociado"
			    ,COM."IdMovimientoAsociado"
			    ,COM."FechaMovimiento" as "FechaComprobante"			    
			    ,COM."IdPersonaTitularPago" as "IdPersonaPagoCobro"
			    ,null as "IdInstrumento"
			    ,null as "IdTipoInstrumento"
			    ,null as "Nemo"
			    ,null as "EsIndicadorBilateral"
			    ,'N' as "EsCargoAbonoPorTraspaso"
			    ,null as "IdPortafolioTraspaso"
			    ,'N' as "GeneroContraparteEnTesoreria"
			    ,'S' as "CertificadoAutomatico"
	FROM "BSCL.Tesoreria.BD::GBL.LiquidarComprobantes_Servicio_ComprobantesMuevenCuentaInversion" as COM

	LEFT JOIN "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" deta ON deta."IdComprobante" = COM."IdComprobante" AND deta."IdComprobanteDetalle" = COM."IdComprobanteDetalle"
	
	LEFT JOIN "BSCL.Tesoreria.BD::TES.Comprobante" orig ON orig."IdComprobante" = COM."IdComprobante"
	
	-- OBTIENE DATOS DEL EGRESO ORIGINAL CON TERCERO RELACIONADO
	LEFT JOIN ( SELECT 
				COM."IdComprobante", 
				CDO."IdIngresoEgreso", 
				IE."IdPortafolio", 
				IE."IdModulo", 
				IE."IdTipoMovimiento", 
				IE."IdTipoPersona", 
				IE."IdPortafolioTitularPago", 
				IE."IdTipoCanalIngreso"
				FROM 		"BSCL.Tesoreria.BD::TES.Comprobante"  				AS COM
				INNER JOIN 	"BSCL.Tesoreria.BD::TES.ComprobanteDetalle" 		AS CD 	ON (CD."IdComprobante" 			= COM."IdComprobante")
				INNER JOIN 	"BSCL.Tesoreria.BD::TES.ComprobanteDetalleOrigen" 	AS CDO 	ON (CDO."IdComprobanteDetalle"	= CD."IdComprobanteDetalle")
				INNER JOIN 	"BSCL.Tesoreria.BD::TES.IngresoEgreso" 				AS IE 	ON (IE."IdIngresoEgreso" 		= CDO."IdIngresoEgreso")
				WHERE 	
					IE."IdModulo" 				 = 'FAC' 				AND 
					IE."IdTipoMovimiento" 		 = 'MOVEGR' 			AND 
					IE."IdTipoPersona" 			 = 'CLI' 				AND 
					IE."IdPortafolioTitularPago" <> IE."IdPortafolio" 	AND 
					IE."IdTipoCanalIngreso" = 'FMM')							AS
					IngEgr
	ON  IngEgr."IdComprobante" = COM."IdComprobante"
	
	WHERE COM."AfectaCuentaInversion"  IN ('CLI','')
	AND COM."IdTipoPersonaTitularPago" IN ('PER','')
	AND CASE WHEN IngEgr."IdModulo" = 'FAC' AND IngEgr."IdTipoMovimiento" = 'MOVEGR' AND IngEgr."IdTipoPersona" = 'CLI'  AND IngEgr."IdTipoCanalIngreso" = 'FMM'
		THEN 1
		ELSE COALESCE(COM."IdPortafolio",0)
		END > 0


UNION ALL


	-- Afectar la cuenta inversion de los clientes que forman una parte de una partida de Ingresos(PARING) / Egresos(PAREGR) 
	SELECT	 
	             IE."IdIngresoEgreso" as "NumeroCargoAbono"
	 	 		,IE."IdPortafolioTitularPago" AS "IdPortafolio"
	 	 		,IE."IdMoneda"
	 	 		,COM."IdTipoMovimientoCtaInvCliente" as "IdTipoMovimiento"
	 	 		,CASE WHEN deta."IdConcepto" = 'INGAPV' THEN 'ABOAPV' /*APV - Se modifica concepto para la Cta Inv*/
	 	 			  WHEN deta."IdConcepto" = 'ITRAPV' THEN 'ABOAPV' /*APV - Se modifica concepto para la Cta Inv*/
	 	 		      WHEN deta."IdConcepto" = 'EGRAPV' THEN 'CARAPV' /*APV - Se modifica concepto para la Cta Inv*/
	 	 		      WHEN deta."IdConcepto" = 'ETRAPV' THEN 'CARAPV' /*APV - Se modifica concepto para la Cta Inv*/
	 	 		      WHEN deta."IdConcepto" = 'INGBON' THEN 'ABOBON' /*APV - Se modifica concepto para la Cta Inv*/ 
	 	 		      ELSE IE."IdConcepto"
	 	 		 END AS "IdConcepto"
	 	 		,CASE
	 	 			WHEN orig."TipoComprobante" = 'PARING' THEN 'LIQUIDACION_PARTIDA_DE_INGRESO_TESORERIA'
	 	 			WHEN orig."TipoComprobante" = 'PAREGR' THEN 'LIQUIDACION_PARTIDA_DE_EGRESO_TESORERIA'
	 	 			ELSE ''
	 	 		END	 as "Descripcion"
	 	 		,'T0' as "IdCondicionDeLiquidacion"
	 	 		,:FechaMovimiento as "FechaCondicionDeLiquidacion"
	 	 		,IE."Monto"
	 	 		---,0 as "IdEjecutivo"
	 	 		,CAST(0 AS BIGINT) as "IdEjecutivo"
	 			,CAST(0 AS BIGINT) as "IdSucursal"
	 	 		,IE."IdTipoProducto"
	 	 		,IE."IdTipoOperacion"
			    ,IE."IdOperacion" AS "IdOperacion"		
			    
			    ,CAST(IE."FechaOperacion"  AS NVARCHAR(40)) as "FechaOperacion"
			    ,CAST(orig."FechaLiquidacion"  AS NVARCHAR(40)) as "FechaLiquidacionOperacion"
			    ,CAST(orig."FechaLiquidacion"  AS NVARCHAR(40)) as "FechaVencimientoOperacion"
			    
			    ,IE."IdMonedaOperacion"
			    ,IE."Monto" AS "MontoOperacion"
			    ,null as "TipoCambioOperacion"
			    ,null as "IdContraparte"			    
			    ,COM."IdTipoComprobante" as "IdTipoComprobante"
			    ,COM."IdComprobante" as "IdComprobante"
			    ,null as "FolioComprobante"	
			    ,COM."IdComprobanteDetalle" as "IdComprobanteDetalle"
			    ,COM."IdComprobanteFormaDePago" as "IdComprobanteFormaDePago"
			    ,IE."IdTipoMovimiento" AS "IdTipoMovimientoAsociado"
			    ,IE."IdIngresoEgreso" AS "IdMovimientoAsociado"
			    ,COM."FechaMovimiento" as "FechaComprobante"			    
			    ,IE."IdPersonaTitularPago" as "IdPersonaPagoCobro"
			    ,null as "IdInstrumento"
			    ,null as "IdTipoInstrumento"
			    ,null as "Nemo"
			    ,null as "EsIndicadorBilateral"
			    ,'N' as "EsCargoAbonoPorTraspaso"
			    ,null as "IdPortafolioTraspaso"
			    ,'N' as "GeneroContraparteEnTesoreria"
			    ,'S' as "CertificadoAutomatico"
	FROM "BSCL.Tesoreria.BD::GBL.LiquidarComprobantes_Servicio_ComprobantesMuevenCuentaInversion" AS COM
	LEFT JOIN "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" AS deta ON (deta."IdComprobante" = COM."IdComprobante" AND deta."IdComprobanteDetalle" = COM."IdComprobanteDetalle")
	LEFT JOIN "BSCL.Tesoreria.BD::TES.Comprobante" AS orig ON (orig."IdComprobante" = COM."IdComprobante")		   
	LEFT JOIN "BSCL.Tesoreria.BD::TES.ComprobanteDetalleOrigen" AS CDO ON (CDO."IdComprobanteDetalle" = deta."IdComprobanteDetalle")		   
	LEFT JOIN "BSCL.Tesoreria.BD::TES.IngresoEgreso" AS IE ON (IE."IdIngresoEgreso" = CDO."IdIngresoEgreso")
	WHERE (COM."AfectaCuentaInversion" = 'CLI' OR COM."AfectaCuentaInversion" = '')
	AND COALESCE(IE."IdPortafolioTitularPago",0) > 0
	AND orig."TipoComprobante" IN ('PARING','PAREGR');  -- Solo partidas de ingresos/egreso generadas desde movimientos		   






	