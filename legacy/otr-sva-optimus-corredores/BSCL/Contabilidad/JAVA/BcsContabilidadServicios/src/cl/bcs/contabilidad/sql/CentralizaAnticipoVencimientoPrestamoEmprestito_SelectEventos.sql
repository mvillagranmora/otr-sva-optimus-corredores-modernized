SELECT 	 
	TO_BIGINT(:IdTipoCentralizacion)                            AS "IdTipoCentralizacion"
	,"IdCartera" 												AS "IdMovimiento"
	,CASE ConsultarCartera."EGRIdConcepto"
		WHEN 'VENPRT' THEN 'MONTO_VENCIMIENTO_PRESTAMO'
		WHEN 'VENEMP' THEN 'MONTO_VENCIMIENTO_EMPRESTITO'
		WHEN 'ANTPRT' THEN 'MONTO_ANTICIPO_PRESTAMO'
		WHEN 'ANTEMP' THEN 'MONTO_ANTICIPO_EMPRESTITO'
		ELSE '??????????'			
	 END						 								AS "IdEvento"
	 ,CASE ConsultarCartera."EGRIdConcepto"
		WHEN 'VENPRT' THEN 'Monto Vencimiento Prestamo'
		WHEN 'VENEMP' THEN 'Monto Vencimiento Emprestito'
		WHEN 'ANTPRT' THEN 'Monto Anticipo Prestamo'
		WHEN 'ANTEMP' THEN 'Monto Anticipo Emprestito'
		ELSE '??????'			
	  END														AS "Nombre"
	 ,CASE ConsultarCartera."EGRIdConcepto"
		WHEN 'VENPRT' THEN ConsultarCartera."EGRMonto"
		WHEN 'VENEMP' THEN ConsultarCartera."EGRMonto"
		WHEN 'ANTPRT' THEN ConsultarCartera."EGRMonto"--"EGRMontoARecibirAnticipo"
		WHEN 'ANTEMP' THEN ConsultarCartera."EGRMonto"--"EGRMontoARecibirAnticipo"
		ELSE 0			
	  END														AS "MontoEvento"
	 ,"IdMonedaInstrumento" 									AS "CodigoMoneda"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" ConsultarCartera
WHERE ConsultarCartera."IdEmpresa" 				= :IdEmpresa 
	AND "EGRFechaOperacion" 	   				= :FechaContable
	AND "EGRIdConcepto"							IN ('VENPRT','VENEMP','ANTPRT','ANTEMP')
	AND "IdClasificacionTipoInstrumento" 		= 'RV' 
	AND "IdTipoInstrumento" 					IN ('PRT','EMP');
	        