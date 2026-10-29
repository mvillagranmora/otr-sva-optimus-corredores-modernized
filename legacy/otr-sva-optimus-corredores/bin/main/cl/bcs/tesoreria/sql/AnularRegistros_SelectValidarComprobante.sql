
SELECT  2 AS "Prioridad",		
	CASE WHEN COALESCE(COM."IdComprobante",0) = 0 THEN 0
		 WHEN (COM."IdEstado" = 'BLO') THEN 0
		 WHEN (COM."IdEstado" = 'ANU') THEN 0
		 
	
		 WHEN ( :p_IdModulo != 'TES' AND COM."IdEstado" = 'LIQ' AND COM."IdConcepto" IN ('CMMESP', 'VNMESP')) THEN 0
		 WHEN ( :p_IdModulo = 'FAC' AND COM."IdEstado" = 'LIQ' AND COM."IdConcepto" NOT IN ('CMMESP', 'VNMESP')) THEN 0
		 
		 ELSE  1
	END AS "Cumple",
	
	CASE WHEN COALESCE(COM."IdComprobante",0) = 0 THEN 'COMPROBANTE_TESORERIA_NO_EXISTE'
		 WHEN (COM."IdEstado" = 'BLO') THEN 'COMPROBANTE_TESORERIA_BLOQUEADO'
		 WHEN (COM."IdEstado" = 'ANU') THEN 'COMPROBANTE_TESORERIA_ANULADO'
		 
	
		 WHEN (:p_IdModulo != 'TES' AND COM."IdEstado" = 'LIQ' AND COM."IdConcepto" IN ('CMMESP', 'VNMESP')) THEN 'COMPROBANTE_TESORERIA_LIQUIDADO_SPOT'
		 WHEN (:p_IdModulo = 'FAC' AND COM."IdEstado" = 'LIQ' AND COM."IdConcepto" NOT IN ('CMMESP', 'VNMESP'))THEN 'COMPROBANTE_TESORERIA_LIQUIDADO'
		 ELSE  ''
	END AS "CodigoError",
	
	'Id' AS "CodigoDato",				
   	CAST(RP."Id" AS NVARCHAR(50)) AS "ValorDato",
   	RP."Id",
	RP."Entidad",
	'N' AS "RequiereAutorizacion"
FROM #AnularRegistros_p_RegistrosAProcesar AS RP		
LEFT JOIN "BSCL.Tesoreria.BD::TES.Comprobante" AS COM ON (COM."IdComprobante" = RP."Id")
WHERE RP."Entidad" = 'TES.Comprobante'

UNION ALL

-- Valida comprobantes que requieren de autorizacion para ser anulados
SELECT  2 AS "Prioridad",		
		CASE 
			 WHEN (COM."IdEstado" = 'LIQ' OR COM."IdEstado" = 'LIP') THEN 0
			 ELSE  1
		END AS "Cumple",
		CASE 
			 WHEN (COM."IdEstado" = 'LIQ' OR COM."IdEstado" = 'LIP') THEN 'COMPROBANTE_LIQUIDADO_REQUIERE_AUTORIZACION'
			 ELSE  ''
		END AS "CodigoError",				
		'Id' AS "CodigoDato",				
       	CAST(RP."Id" AS NVARCHAR(50)) AS "ValorDato",
       	RP."Id",
		RP."Entidad",
		'S' AS "RequiereAutorizacion"
FROM #AnularRegistros_p_RegistrosAProcesar AS RP		
INNER JOIN "BSCL.Tesoreria.BD::TES.Comprobante" AS COM ON (COM."IdComprobante" = RP."Id")	
LEFT JOIN "BSCL.Plataforma.BD::PLA.RequiereAutorizacion" AS AUT ON (AUT."IdConcepto" = 'ANCOTE' AND AUT."IdEmpresa" = COM."IdEmpresa")
WHERE RP."Entidad" = 'TES.Comprobante'
AND COM."IdEvento" < 1
AND COALESCE(AUT."RequiereAutorizacion",'S') = 'S'  -- validar solo cuando se indica ()
----AND :p_EjecutaAutorizacion = 'S'  -- validar solo cuando se indica

UNION ALL

-- Valida estado de autorizacion de comprobantes(para liquidados y liquidados parcial)
SELECT  2 AS "Prioridad",
		CASE
			 WHEN (COM."IdEvento" > 0 AND EVE."IdEstado" = 'RZD') THEN 0
			 WHEN (COM."IdEvento" > 0 AND EVE."IdEstado" = 'VCD') THEN 0
			 WHEN (COM."IdEvento" > 0 AND EVE."IdEstado" = 'VIG') THEN 0					 
			 ELSE  1
		END AS "Cumple",
		CASE 
			 WHEN (COM."IdEvento" > 0 AND EVE."IdEstado" = 'RZD') THEN 'EVENTO_SE_ENCUENTRA_RECHAZADO'
			 WHEN (COM."IdEvento" > 0 AND EVE."IdEstado" = 'VCD') THEN 'EVENTO_SE_ENCUENTRA_VENCIDO'
			 WHEN (COM."IdEvento" > 0 AND EVE."IdEstado" = 'VIG') THEN 'EVENTO_SIN_AUTORIZACION'
			 ELSE  ''
		END AS "CodigoError",		       	
		'IdEvento' AS "CodigoDato",				
       	CAST(RP."Id" AS NVARCHAR(50)) AS "ValorDato",
       	RP."Id",
		RP."Entidad",
		'N' AS "RequiereAutorizacion"
FROM #AnularRegistros_p_RegistrosAProcesar AS RP		
INNER JOIN "BSCL.Tesoreria.BD::TES.Comprobante" AS COM ON (COM."IdComprobante" = RP."Id")		
LEFT JOIN "BSCL.Plataforma.BD::AUT.Evento" AS EVE ON (EVE."IdEvento" = COM."IdEvento")
WHERE RP."Entidad" = 'TES.Comprobante'		
AND (COM."IdEstado" = 'LIQ' OR COM."IdEstado" = 'LIP');