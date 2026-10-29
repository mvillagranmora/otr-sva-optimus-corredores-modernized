SELECT
 	 TO_BIGINT(:IdTipoCentralizacion)			AS "IdTipoCentralizacion"
 	,"IdCartera" 								AS "IdMovimiento"
	,'VALOR_PRESENTE_EMISION_RF'				AS "IdEvento"	
	,'Valor Presente Emision RF'				AS "Nombre"	
	,"EGRMontoMonedaInstrumento"				AS "MontoEvento"
	,"IdMonedaInstrumento" 						AS "CodigoMoneda"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" ConsultarCartera
WHERE 
		"IdEstado"='VCD'
	AND (
			("EGRIdTipoOperacion"		='VCDTIT' AND "EGRFechaOperacion" 	   				= :FechaContable)
		OR  ("EGRIdTipoOperacion"		='VCDPAC' AND "FechaVencimientoInstrumento" 	   	= :FechaContable)	
		)
	AND ("IdTipoOperacion"						<> 'TDEPLA' AND "IdTipoOperacion" <> 'TAUTDI')
    AND "IdClasificacionTipoInstrumento"IN ('RF','IF')
	   
UNION ALL
	    
--2
SELECT 	 
	 TO_BIGINT(:IdTipoCentralizacion)			AS "IdTipoCentralizacion"
	,"IdCartera" 								AS "IdMovimiento"
	,'VALOR_ANTERIOR_MERCADO_RF'				AS "IdEvento"	
	,'Valor Mercado Anterior RF'				AS "Nombre"	
	,"INGMontoATasaMercadoAnterior"				AS "MontoEvento" 
	,"IdMonedaInstrumento" 						AS "CodigoMoneda"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" ConsultarCartera
WHERE 
		"IdEstado"='VCD'
	AND (
			("EGRIdTipoOperacion"		='VCDTIT' AND "EGRFechaOperacion" 	   				= :FechaContable)
		OR  ("EGRIdTipoOperacion"		='VCDPAC' AND "FechaVencimientoInstrumento" 	   	= :FechaContable)	
		)
	AND ("IdTipoOperacion"						<> 'TDEPLA' AND "IdTipoOperacion" <> 'TAUTDI')
   	AND "IdClasificacionTipoInstrumento" 		IN ('RF','IF')
	    
UNION ALL
	   
--3

SELECT 	 
	 TO_BIGINT(:IdTipoCentralizacion)			AS "IdTipoCentralizacion"
	,"IdCartera" 								AS "IdMovimiento"
	,'VALOR_ACTUAL_COMPRA_RF'					AS "IdEvento"	
	,'Valor Compra Actual RF'					AS "Nombre"	
	,"EGRMontoLiquidacion"						AS "MontoEvento"
	,"EGRIdMonedaLiquidacion" 					AS "CodigoMoneda"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" ConsultarCartera
WHERE 
		"IdEstado"='VCD'
	AND (
			("EGRIdTipoOperacion"		='VCDTIT' AND "EGRFechaOperacion" 	   				= :FechaContable)
		OR  ("EGRIdTipoOperacion"		='VCDPAC' AND "FechaVencimientoInstrumento" 	   	= :FechaContable)	
		)
	AND ("IdTipoOperacion"						<> 'TDEPLA' AND "IdTipoOperacion" <> 'TAUTDI')
   	AND "IdClasificacionTipoInstrumento" 		IN ('RF','IF')
	        	
UNION ALL
	    
--4

SELECT 	 
	 TO_BIGINT(:IdTipoCentralizacion)			AS "IdTipoCentralizacion"
	,"IdCartera" 								AS "IdMovimiento"
	,'VALOR_ANTERIOR_COMPRA_RF'					AS "IdEvento"	
	,'Valor Compra Anterior RF'					AS "Nombre"	
	,"INGMontoATasaCompraAnterior"				AS "MontoEvento"
	,"IdMonedaInstrumento" 						AS "CodigoMoneda"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" ConsultarCartera
WHERE 
		"IdEstado"='VCD'
	AND (
			("EGRIdTipoOperacion"		='VCDTIT' AND "EGRFechaOperacion" 	   				= :FechaContable)
		OR  ("EGRIdTipoOperacion"		='VCDPAC' AND "FechaVencimientoInstrumento" 	   	= :FechaContable)	
		)
	AND ("IdTipoOperacion"						<> 'TDEPLA' AND "IdTipoOperacion" <> 'TAUTDI')
   	AND "IdClasificacionTipoInstrumento" 		IN ('RF','IF')
	        	
UNION ALL

--5
	    
SELECT 	
	 TO_BIGINT(:IdTipoCentralizacion)			AS "IdTipoCentralizacion"
	,"IdCartera" 								AS "IdMovimiento"
	,'MAYOR_VALOR_MERCADO_RF'					AS "IdEvento"	
	,'Mayor Valor Mercado RF'					AS "Nombre"	
	,CASE WHEN ("EGRMontoMonedaInstrumento" -"INGMontoATasaMercadoAnterior") > 0 
		THEN ("EGRMontoMonedaInstrumento" - "INGMontoATasaMercadoAnterior")
		ELSE 0
	END 										AS "MontoEvento"
	,"IdMonedaInstrumento" 						AS "CodigoMoneda"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" ConsultarCartera
WHERE 
		"IdEstado"='VCD'
	AND (
			("EGRIdTipoOperacion"		='VCDTIT' AND "EGRFechaOperacion" 	   				= :FechaContable)
		OR  ("EGRIdTipoOperacion"		='VCDPAC' AND "FechaVencimientoInstrumento" 	   	= :FechaContable)	
		)
	AND ("IdTipoOperacion"						<> 'TDEPLA' AND "IdTipoOperacion" <> 'TAUTDI')
   	AND "IdClasificacionTipoInstrumento" 		IN ('RF','IF')
	        	
UNION ALL

--6 
	       
SELECT 	
	 TO_BIGINT(:IdTipoCentralizacion)			AS "IdTipoCentralizacion"
	,"IdCartera" 								AS "IdMovimiento"
	,'MENOR_VALOR_MERCADO_RF'					AS "IdEvento"	
	,'Menor Valor Mercado RF'					AS "Nombre"	
	,ABS(("EGRMontoMonedaInstrumento" - "INGMontoATasaMercadoAnterior"))
						  						AS "MontoEvento"
	,"IdMonedaInstrumento" 						AS "CodigoMoneda"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" ConsultarCartera
WHERE 
		"IdEstado"='VCD'
	AND (
			("EGRIdTipoOperacion"		='VCDTIT' AND "EGRFechaOperacion" 	   				= :FechaContable)
		OR  ("EGRIdTipoOperacion"		='VCDPAC' AND "FechaVencimientoInstrumento" 	   	= :FechaContable)	
		)
	AND ("IdTipoOperacion"						<> 'TDEPLA' AND "IdTipoOperacion" <> 'TAUTDI')
   	AND "IdClasificacionTipoInstrumento" 		IN ('RF','IF')
	        	
UNION ALL

--7

 SELECT	
	 TO_BIGINT(:IdTipoCentralizacion)			AS "IdTipoCentralizacion"
	,"IdCartera" 								AS "IdMovimiento"
	,'MAYOR_VALOR_COMPRA_RF'					AS "IdEvento"	
	,'Mayor Valor Compra RF'					AS "Nombre"	
	,CASE WHEN ("EGRMontoMonedaInstrumento" - "INGMontoCompraAnteriorMonedaInstrumento") > 0
		THEN ("EGRMontoMonedaInstrumento" - "INGMontoCompraAnteriorMonedaInstrumento")
		ELSE 0 
	 END 										AS "MontoEvento"
	,"IdMonedaInstrumento" 			AS "CodigoMoneda"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" ConsultarCartera
WHERE 
		"IdEstado"='VCD'
	AND (
			("EGRIdTipoOperacion"		='VCDTIT' AND "EGRFechaOperacion" 	   				= :FechaContable)
		OR  ("EGRIdTipoOperacion"		='VCDPAC' AND "FechaVencimientoInstrumento" 	   	= :FechaContable)	
		)
	AND ("IdTipoOperacion"						<> 'TDEPLA' AND "IdTipoOperacion" <> 'TAUTDI')
   	AND "IdClasificacionTipoInstrumento" 		IN ('RF','IF')

UNION ALL

--8

SELECT 	 
	 TO_BIGINT(:IdTipoCentralizacion)			AS "IdTipoCentralizacion"
	,"IdCartera" 								AS "IdMovimiento"
	,'MENOR_VALOR_COMPRA_RF'					AS "IdEvento"	
	,'Menor Valor Compra RF'					AS "Nombre"	
	,ABS(("EGRMontoMonedaInstrumento" - "INGMontoCompraAnteriorMonedaInstrumento"))
												AS "MontoEvento"		
	,"IdMonedaInstrumento" 						AS "CodigoMoneda"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" ConsultarCartera
WHERE 
		"IdEstado"='VCD'
	AND (
			("EGRIdTipoOperacion"		='VCDTIT' AND "EGRFechaOperacion" 	   				= :FechaContable)
		OR  ("EGRIdTipoOperacion"		='VCDPAC' AND "FechaVencimientoInstrumento" 	   	= :FechaContable)	
		)
	AND ("IdTipoOperacion"						<> 'TDEPLA' AND "IdTipoOperacion" <> 'TAUTDI')
   	AND "IdClasificacionTipoInstrumento" 		IN ('RF','IF')
	  
UNION ALL

--9
	
SELECT
	 TO_BIGINT(:IdTipoCentralizacion)			AS "IdTipoCentralizacion"
	,"IdCartera" 								AS "IdMovimiento"
	,'MONTO_PRESENTE_EMISION_RF'				AS "IdEvento"	
	,'Monto Presente Emision RF'				AS "Nombre"	
	,"EGRMonto"									AS "MontoEvento"
	,"IdMonedaInstrumento" 						AS "CodigoMoneda"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" ConsultarCartera
WHERE 
		"IdEstado"='VCD'
	AND (
			("EGRIdTipoOperacion"		='VCDTIT' AND "EGRFechaOperacion" 	   				= :FechaContable)
		OR  ("EGRIdTipoOperacion"		='VCDPAC' AND "FechaVencimientoInstrumento" 	   	= :FechaContable)	
		)
	AND ("IdTipoOperacion"						<> 'TDEPLA' AND "IdTipoOperacion" <> 'TAUTDI')
   	AND "IdClasificacionTipoInstrumento" 		IN ('RF','IF') 
	
UNION ALL

--10

SELECT 
	 TO_BIGINT(:IdTipoCentralizacion)			AS "IdTipoCentralizacion"
	,"IdCartera" 								AS "IdMovimiento"
	,'MONTO_ANTERIOR_MERCADO_RF'				AS "IdEvento"	
	,'Monto Mercado Anterior RF'				AS "Nombre"	
	,"INGMontoATasaMercadoAnterior"				AS "MontoEvento"
	,"IdMonedaInstrumento" 						AS "CodigoMoneda"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" ConsultarCartera
WHERE 
		"IdEstado"								='VCD'
	AND (
			("EGRIdTipoOperacion"		='VCDTIT' AND "EGRFechaOperacion" 	   				= :FechaContable)
		OR  ("EGRIdTipoOperacion"		='VCDPAC' AND "FechaVencimientoInstrumento" 	   	= :FechaContable)	
		)
	AND ("IdTipoOperacion"						<> 'TDEPLA' AND "IdTipoOperacion" <> 'TAUTDI')
	AND "IdClasificacionTipoInstrumento" 		IN ('RF','IF')
	     
UNION ALL

--10-1
SELECT 
	 TO_BIGINT(:IdTipoCentralizacion)			AS "IdTipoCentralizacion"
	,"IdCartera" 								AS "IdMovimiento"
	,'MONTO_VENCIMIENTO_IF'						AS "IdEvento"	
	,'Monto Vencimiento'						AS "Nombre"	
	,CASE WHEN Instrumento."Nemotecnico" = 'PAGARE NR' 
		THEN "EGRValorRescate" 
		ELSE "INGMontoATasaMercadoActual"		END AS "MontoEvento"
	,CASE WHEN ConsultarCartera."IdTipoReajuste" = 'USD' 
		THEN ConsultarCartera."IdMonedaSistema" 
		ELSE "IdMonedaInstrumento" 				END AS "CodigoMoneda"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" ConsultarCartera
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" Instrumento ON Instrumento."IdInstrumento" = ConsultarCartera."IdInstrumento"
WHERE 
		ConsultarCartera."IdEstado"								='VCD'
	AND (
			("EGRIdTipoOperacion"		='VCDTIT' AND "EGRFechaOperacion" 	   				= :FechaContable)
		OR  ("EGRIdTipoOperacion"		='VCDPAC' AND "FechaVencimientoInstrumento" 	   	= :FechaContable)	
		)
	AND ("IdTipoOperacion"						<> 'TDEPLA' AND "IdTipoOperacion" <> 'TAUTDI')
	AND "IdClasificacionTipoInstrumento" 		IN ('RF','IF')
	     
UNION ALL
	   	
--11
	   	    
SELECT 	 
	 TO_BIGINT(:IdTipoCentralizacion)			AS "IdTipoCentralizacion"
	,"IdCartera" 								AS "IdMovimiento"
	,'MONTO_ACTUAL_COMPRA_RF'					AS "IdEvento"	
	,'Monto Compra Actual RF'					AS "Nombre"	
	,"EGRMonto"									AS "MontoEvento" 
	,CASE WHEN "IdTipoReajuste" = 'USD' 
		THEN "IdMonedaSistema" 
		ELSE "IdMonedaInstrumento" 				END AS "CodigoMoneda"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" ConsultarCartera
WHERE 
		"IdEstado"='VCD'
	AND (
			("EGRIdTipoOperacion"		='VCDTIT' AND "EGRFechaOperacion" 	   				= :FechaContable)
		OR  ("EGRIdTipoOperacion"		='VCDPAC' AND "FechaVencimientoInstrumento" 	   	= :FechaContable)	
		)
	AND ("IdTipoOperacion"						<> 'TDEPLA' AND "IdTipoOperacion" <> 'TAUTDI')
   	AND "IdClasificacionTipoInstrumento" 		IN ('RF','IF') 
	     
UNION ALL

--12

SELECT 	 
	 TO_BIGINT(:IdTipoCentralizacion)			AS "IdTipoCentralizacion"
	,"IdCartera" 								AS "IdMovimiento"
	,'MONTO_ANTERIOR_COMPRA_RF'					AS "IdEvento"	
	,'Monto Compra Anterior RF'					AS "Nombre"	
	,"INGMontoATasaCompraAnterior"				AS "MontoEvento"
	,CASE WHEN "IdTipoReajuste" = 'USD' 
		THEN "IdMonedaSistema" 
		ELSE "IdMonedaInstrumento" 				END AS "CodigoMoneda"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" ConsultarCartera
WHERE 
		"IdEstado"='VCD'
	AND (
			("EGRIdTipoOperacion"		='VCDTIT' AND "EGRFechaOperacion" 	   				= :FechaContable)
		OR  ("EGRIdTipoOperacion"		='VCDPAC' AND "FechaVencimientoInstrumento" 	   	= :FechaContable)	
		)
	AND ("IdTipoOperacion"						<> 'TDEPLA' AND "IdTipoOperacion" <> 'TAUTDI')
	AND "IdClasificacionTipoInstrumento" 		IN ('RF','IF')

UNION ALL 
	   
--13

SELECT 	 
	 TO_BIGINT(:IdTipoCentralizacion)			AS "IdTipoCentralizacion"
	,"IdCartera" 								AS "IdMovimiento"
	,'MONTO_MAYOR_VALOR_MERCADO_RF'				AS "IdEvento"	
	,'Monto Mayor Valor Mercado RF'				AS "Nombre"	
	,CASE WHEN ("EGRMonto" -"INGMontoATasaMercadoAnterior") > 0 
		THEN ("EGRMonto" - "INGMontoATasaMercadoAnterior")
		ELSE 0
	 END 										AS "MontoEvento"
	,"IdMonedaInstrumento" 						AS "CodigoMoneda"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" ConsultarCartera
WHERE 
		"IdEstado"='VCD'
	AND (
			("EGRIdTipoOperacion"		='VCDTIT' AND "EGRFechaOperacion" 	   				= :FechaContable)
		OR  ("EGRIdTipoOperacion"		='VCDPAC' AND "FechaVencimientoInstrumento" 	   	= :FechaContable)	
		)
	AND ("IdTipoOperacion"						<> 'TDEPLA' AND "IdTipoOperacion" <> 'TAUTDI')
   	AND "IdClasificacionTipoInstrumento" 		IN ('RF','IF')
	    
UNION ALL
	    
--14
 
SELECT 	 
	 TO_BIGINT(:IdTipoCentralizacion)			AS "IdTipoCentralizacion"
	,"IdCartera" 								AS "IdMovimiento"
	,'MONTO_MENOR_VALOR_MERCADO_RF'				AS "IdEvento"	
	,'Monto Menor Valor Mercado RF'				AS "Nombre"	
	,ABS(("EGRMonto" - "INGMontoATasaMercadoAnterior"))
												AS "MenorValorMercado"				
	,"IdMonedaInstrumento" 						AS "CodigoMoneda"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" ConsultarCartera
WHERE 
		"IdEstado"='VCD'
	AND (
			("EGRIdTipoOperacion"		='VCDTIT' AND "EGRFechaOperacion" 	   				= :FechaContable)
		OR  ("EGRIdTipoOperacion"		='VCDPAC' AND "FechaVencimientoInstrumento" 	   	= :FechaContable)	
		)
	AND ("IdTipoOperacion"						<> 'TDEPLA' AND "IdTipoOperacion" <> 'TAUTDI')
   	AND "IdClasificacionTipoInstrumento" 		IN ('RF','IF') 
	   
UNION ALL
	   
--15
  
SELECT 	 
	 TO_BIGINT(:IdTipoCentralizacion)			AS "IdTipoCentralizacion"
	,"IdCartera" 								AS "IdMovimiento"
	,'MONTO_MAYOR_VALOR_COMPRA_RF'				AS "IdEvento"	
	,'Monto Mayor Valor Compra RF'				AS "Nombre"	
	,CASE WHEN ("EGRMonto" - "EGRMonto") > 0 
		THEN ("EGRMonto" - "INGMontoATasaCompraAnterior") --***
		ELSE 0
	 END 										AS "MontoEvento"
	,"IdMonedaInstrumento" 						AS "CodigoMoneda"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" ConsultarCartera
WHERE 
		"IdEstado"='VCD'
	AND (
			("EGRIdTipoOperacion"		='VCDTIT' AND "EGRFechaOperacion" 	   				= :FechaContable)
		OR  ("EGRIdTipoOperacion"		='VCDPAC' AND "FechaVencimientoInstrumento" 	   	= :FechaContable)	
		)
	AND ("IdTipoOperacion"						<> 'TDEPLA' AND "IdTipoOperacion" <> 'TAUTDI')
   	AND "IdClasificacionTipoInstrumento" 		IN ('RF','IF') 
	        	
UNION ALL 
	   
--16
 
SELECT 	
	 TO_BIGINT(:IdTipoCentralizacion)			AS "IdTipoCentralizacion"
	,"IdCartera" 								AS "IdMovimiento"
	,'MONTO_MENOR_VALOR_COMPRA_RF'				AS "IdEvento"	
	,'Monto Menor Valor Compra RF'				AS "Nombre"	
	,ABS("EGRMonto" - "INGMontoATasaCompraAnterior")   --***
												AS "MontoEvento"
	,"IdMonedaInstrumento" 						AS "CodigoMoneda"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" ConsultarCartera
WHERE 
		"IdEstado"='VCD'
	AND (
			("EGRIdTipoOperacion"		='VCDTIT' AND "EGRFechaOperacion" 	   				= :FechaContable)
		OR  ("EGRIdTipoOperacion"		='VCDPAC' AND "FechaVencimientoInstrumento" 	   	= :FechaContable)	
		)
	AND ("IdTipoOperacion"						<> 'TDEPLA' AND "IdTipoOperacion" <> 'TAUTDI')
	AND "IdClasificacionTipoInstrumento" 		IN ('RF','IF')

UNION ALL    	
	   
--17  'TDEPLA' y 'TAUTDI'

SELECT 	 
	 TO_BIGINT(:IdTipoCentralizacion)			AS "IdTipoCentralizacion"
	,"IdCartera" 								AS "IdMovimiento"
	,'MONTO_TOTAL_VCTO'							AS "IdEvento"	
	,'Monto Total Vencimiento'					AS "Nombre"	
	,"EGRMonto"									AS "MontoEvento"
	,"IdMonedaInstrumento" 						AS "CodigoMoneda"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" ConsultarCartera
WHERE 
			"IdEstado"='VCD'
	AND (
			("EGRIdTipoOperacion"		='VCDTIT' AND "EGRFechaOperacion" 	   				= :FechaContable)
		OR  ("EGRIdTipoOperacion"		='VCDPAC' AND "FechaVencimientoInstrumento" 	   	= :FechaContable)	
		)
	AND ("IdTipoOperacion"						= 'TDEPLA' OR "IdTipoOperacion" = 'TAUTDI')
   	AND "IdClasificacionTipoInstrumento" 		IN ('RF','IF') 	

UNION ALL

SELECT
 	 TO_BIGINT(:IdTipoCentralizacion)			AS "IdTipoCentralizacion"
 	,"IdCartera" 								AS "IdMovimiento"
	,'VALOR_VENCIMIENTO_RF'				AS "IdEvento"	
	,'Valor Vencimiento RF'				AS "Nombre"	
	,"EGRMontoMonedaInstrumento"				AS "MontoEvento"
	,"IdMonedaInstrumento" 						AS "CodigoMoneda"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" ConsultarCartera
WHERE 
		"IdEstado"='VCD'
	AND (
			("EGRIdTipoOperacion"		='VCDTIT' AND "EGRFechaOperacion" 	   				= :FechaContable)
		OR  ("EGRIdTipoOperacion"		='VCDPAC' AND "FechaVencimientoInstrumento" 	   	= :FechaContable)	
		)
	AND ("IdTipoOperacion"						<> 'TDEPLA' AND "IdTipoOperacion" <> 'TAUTDI')
      	AND "IdClasificacionTipoInstrumento"IN ('RF')
   	
UNION ALL

SELECT
 	 TO_BIGINT(:IdTipoCentralizacion)			AS "IdTipoCentralizacion"
 	,"IdCartera" 								AS "IdMovimiento"
	,'VALOR_VENCIMIENTO_IF'				AS "IdEvento"	
	,'Valor Vencimiento IF'				AS "Nombre"	
	,"EGRMontoMonedaInstrumento"				AS "MontoEvento"
	,"IdMonedaInstrumento" 						AS "CodigoMoneda"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" ConsultarCartera
WHERE 
		"IdEstado"='VCD'
	AND (
			("EGRIdTipoOperacion"		='VCDTIT' AND "EGRFechaOperacion" 	   				= :FechaContable)
		OR  ("EGRIdTipoOperacion"		='VCDPAC' AND "FechaVencimientoInstrumento" 	   	= :FechaContable)	
		)
	AND ("IdTipoOperacion"						<> 'TDEPLA' AND "IdTipoOperacion" <> 'TAUTDI')
      	AND "IdClasificacionTipoInstrumento"IN ('IF')
   	
