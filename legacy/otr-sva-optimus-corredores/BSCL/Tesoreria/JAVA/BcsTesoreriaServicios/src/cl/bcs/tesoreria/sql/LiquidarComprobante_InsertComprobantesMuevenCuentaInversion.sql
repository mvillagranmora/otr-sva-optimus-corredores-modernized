
------------------------------------------------------------------------------------------------------------
-- Calcula y obtiene datos los comprobantes de tesoreria que generaran movimientos en cuenta inversion
------------------------------------------------------------------------------------------------------------

INSERT INTO "BSCL.Tesoreria.BD::GBL.LiquidarComprobantes_Servicio_ComprobantesMuevenCuentaInversion"

SELECT DISTINCT 
 	 	 		 TC."IdComprobanteInformado" AS "IdComprobante" 
 	 	 		,TCD."IdComprobanteDetalle"
				,TFP."IdComprobanteFormaDePagoInformado" AS "IdComprobanteFormaDePago"				   
				,TC."AfectaCuentaInversion"
				,TC."FechaMovimiento"
				,CASE WHEN TC."IdConcepto" = 'MULCPT' THEN TCD."IdConcepto"							  
					  ELSE TC."IdConcepto"
				  END AS "IdConcepto"
				,TC."IdPersona"
			    ,TC."NombrePersona"
			    ,TC."TitularPago"
			    ,TC."IdTipoPersonaTitularPago"
			    ,TC."IdPersonaTitularPago"
			    
			    ,com."IdMoneda"			    
				,TCD."Monto"
				  
			    ,CP."IdPortafolio"
				
				-- OPERACION  |  NEGOCIO | TESORERIA
				---------------------------------------	
				--  Venta     |  Abono   | (EGR) Cargo
				--  Compra    |  Cargo   | (ING) Abono
 	 
				,(CASE WHEN TC."IdConcepto" <> 'MULCPT' AND TC."IdTipoMovimiento" = 'MOVING' THEN 'MOVABN'
					   WHEN TC."IdConcepto" <> 'MULCPT' AND TC."IdTipoMovimiento" = 'MOVEGR' THEN 'MOVCAR'
					   WHEN TC."IdConcepto" = 'MULCPT' AND TCD."IdTipoMovimiento" = 'MOVING' THEN 'MOVABN' 
					   WHEN TC."IdConcepto" = 'MULCPT' AND TCD."IdTipoMovimiento" = 'MOVEGR' THEN 'MOVCAR'
				   END) AS "IdTipoMovimientoCtaInvCliente"     
				,(CASE WHEN TC."IdConcepto" <> 'MULCPT' AND TC."IdTipoMovimiento" = 'MOVING' THEN 'MOVCAR'
					   WHEN TC."IdConcepto" <> 'MULCPT' AND TC."IdTipoMovimiento" = 'MOVEGR' THEN 'MOVABN'
					   WHEN TC."IdConcepto" = 'MULCPT' AND TCD."IdTipoMovimiento" = 'MOVING' THEN 'MOVCAR' 
					   WHEN TC."IdConcepto" = 'MULCPT' AND TCD."IdTipoMovimiento" = 'MOVEGR' THEN 'MOVABN'
				   END) AS "IdTipoMovimientoCtaInvCorredora"			 
				,(CASE WHEN TC."IdConcepto" = 'MULCPT' THEN SUBSTRING(TCD."IdTipoMovimiento", 4, 3)
					   ELSE SUBSTRING(TC."IdTipoMovimiento", 4, 3)
				   END) AS "IdTipoComprobante" -- ING / EGR
				,'' as "IdTipoMovimientoAsociado"
			    ,0 as "IdMovimientoAsociado"
	FROM "BSCL.Tesoreria.BD::GBL.LiquidarComprobante_TablaComprobante" AS TC
	INNER JOIN "BSCL.Tesoreria.BD::TES.Comprobante" as com ON (COM."IdComprobante" = TC."IdComprobanteInformado")
	INNER JOIN "BSCL.Tesoreria.BD::GBL.LiquidarComprobante_TablaComprobanteDetalle" AS TCD ON (TCD."IdComprobanteInformado" = TC."IdComprobanteInformado")


	LEFT JOIN 
	(	-- Toma solo la primera forma de pago, sino se duplican los registros entre el detalle y las formas de pago
	 	SELECT "IdComprobanteInformado", MAX("IdComprobanteFormaDePagoInformado") AS "IdComprobanteFormaDePagoInformado"
	 	FROM "BSCL.Tesoreria.BD::GBL.LiquidarComprobante_TablaComprobanteFormaDePago"
	 	GROUP BY "IdComprobanteInformado"
	) AS TFP  ON (TFP."IdComprobanteInformado" = TC."IdComprobanteInformado")

	
	LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS CP ON (CP."IdPortafolio" = TC."IdPortafolioTitularPago")
	WHERE COM."IdEstado" = CASE WHEN :LiquidacionParcialEnviaACTA = 'N' THEN 'LIQ' 
		 					 	WHEN :LiquidacionParcialEnviaACTA = 'S' AND TC."IdConcepto" = 'MULCPT' THEN 'LIQ'
		 						ELSE COM."IdEstado" 
  	 				       END
  	AND TC."IdComprobante" > 0 				       
	AND NOT (TCD."IdTipoMovimiento" = 'MOVEGR' AND COALESCE(TCD."SecuenciaRelacionada",0) > 0)  -- Se excluye los egresos relacionados con un ingreso (SPOT)

	
		   
	   
UNION ALL


SELECT 
 	 	 		 TC."IdComprobanteInformado" AS "IdComprobante" 	 	 	
 	 	 		,TCD."IdComprobanteDetalle"
				,TFP."IdComprobanteFormaDePagoInformado" AS "IdComprobanteFormaDePago"
				,TC."AfectaCuentaInversion"
				,TC."FechaMovimiento"
				,CASE WHEN TC."IdConcepto" = 'MULCPT' THEN TCD."IdConcepto" ELSE TC."IdConcepto" END AS "IdConcepto"
				,TC."IdPersona"
			    ,TC."NombrePersona"
			    ,TC."TitularPago"
			    ,TC."IdTipoPersonaTitularPago"
			    ,TC."IdPersonaTitularPago"			    
			    ,com."IdMoneda"			    
			    ,TCD."Monto"				  
			    ,CP."IdPortafolio"
				,CRCTA."IdTipoMovimiento" AS "IdTipoMovimientoCtaInvCliente"
				,CASE WHEN CRCTA."IdTipoMovimiento" = 'MOVABN' THEN 'MOVCAR'
					  WHEN CRCTA."IdTipoMovimiento" = 'MOVCAR' THEN 'MOVABN'
				  END AS "IdTipoMovimientoCtaInvCorredora"
				,(CASE WHEN TC."IdConcepto" = 'MULCPT' THEN SUBSTRING(TCD."IdTipoMovimiento", 4, 3)
					   ELSE SUBSTRING(TC."IdTipoMovimiento", 4, 3)
				   END) AS "IdTipoComprobante"
				,(CASE WHEN TC."IdConcepto" = 'MULCPT' THEN TCD."IdTipoMovimiento" ELSE TC."IdTipoMovimiento" END) AS "IdTipoMovimientoAsociado"
			    ,TC."IdComprobanteInformado" as "IdMovimientoAsociado"
	FROM "BSCL.Tesoreria.BD::GBL.LiquidarComprobante_TablaComprobante" AS TC
	INNER JOIN "BSCL.Tesoreria.BD::TES.Comprobante" as com ON (COM."IdComprobante" = TC."IdComprobanteInformado")
	INNER JOIN "BSCL.Tesoreria.BD::GBL.LiquidarComprobante_TablaComprobanteDetalle" AS TCD ON (TCD."IdComprobanteInformado" = TC."IdComprobanteInformado")
	

	LEFT JOIN 
	(	-- Toma solo la primera forma de pago, sino se duplican los registros entre el detalle y las formas de pago
	 	SELECT "IdComprobanteInformado", MAX("IdComprobanteFormaDePagoInformado") AS "IdComprobanteFormaDePagoInformado"
	 	FROM "BSCL.Tesoreria.BD::GBL.LiquidarComprobante_TablaComprobanteFormaDePago"
	 	GROUP BY "IdComprobanteInformado"
	) AS TFP  ON (TFP."IdComprobanteInformado" = TC."IdComprobanteInformado")
	
	
	LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS CP ON (CP."IdPortafolio" = TC."IdPortafolioTitularPago")
	LEFT JOIN (SELECT 'MOVABN' as "IdTipoMovimiento" FROM DUMMY UNION ALL SELECT 'MOVCAR' as "IdTipoMovimiento" FROM DUMMY)  AS CRCTA ON (1=1)
	 	   
	WHERE COM."IdEstado" = CASE WHEN :LiquidacionParcialEnviaACTA = 'N' THEN 'LIQ' 
				 					 WHEN :LiquidacionParcialEnviaACTA = 'S' AND TC."IdConcepto" = 'MULCPT' THEN 'LIQ'
				 					 ELSE COM."IdEstado" 
				 		   END
	AND TC."IdComprobante" > 0				 		   
	AND (TCD."IdTipoMovimiento" = 'MOVEGR' AND COALESCE(TCD."SecuenciaRelacionada",0) > 0);  -- Incluye los egresos relacionados con un ingreso (SPOT)
	   

