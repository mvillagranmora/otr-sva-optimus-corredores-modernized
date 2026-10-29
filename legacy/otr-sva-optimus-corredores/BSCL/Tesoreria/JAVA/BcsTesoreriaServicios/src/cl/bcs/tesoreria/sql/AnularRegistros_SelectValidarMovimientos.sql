WITH MRF_Validacion AS (
    SELECT
        RP."Id",
        RP."Entidad",
        CASE 
            WHEN COALESCE(IE."IdModulo",'') = 'MRF'        
                AND :p_IdModulo <> 'MRF'               
                AND COALESCE(ConfTes."Valor", 'N') = 'N'
            THEN 0
            ELSE 1
        END AS "Cumple"
    FROM #AnularRegistros_p_RegistrosAProcesar AS RP
    LEFT JOIN "BSCL.Tesoreria.BD::TES.IngresoEgreso" AS IE ON (IE."IdIngresoEgreso" = RP."Id")
    LEFT JOIN "BSCL.Tesoreria.BD::TES.ConfiguracionTesoreria" AS ConfTes ON ConfTes."CodigoParametro" = 'AnularOperacionMesa'
    WHERE RP."Entidad" = 'TES.IngresoEgreso'
)

-- Valida que Movimientos Existan y estado de estos
SELECT  2 AS "Prioridad",
	CASE WHEN COALESCE(IE."IdIngresoEgreso",0) = 0 THEN 0
		 WHEN (IE."IdEstado" = 'BLO') THEN 0
		 WHEN (IE."IdEstado" = 'ANU') THEN 0
		 WHEN (IE."IdEstado" = 'EMI' AND IE."IdModulo" != 'MRF') THEN 0
		 ELSE  1
	END AS "Cumple",
	
	CASE WHEN COALESCE(IE."IdIngresoEgreso",0) = 0 THEN 'MOVIMIENTO_TESORERIA_NO_EXISTE'
		 WHEN (IE."IdEstado" = 'BLO') THEN 'MOVIMIENTO_TESORERIA_BLOQUEADO'
		 WHEN (IE."IdEstado" = 'ANU') THEN 'MOVIMIENTO_TESORERIA_ANULADO'
		 WHEN (IE."IdEstado" = 'EMI') THEN 'MOVIMIENTO_TESORERIA_EMITIDO'
		 ELSE  ''
	END AS "CodigoError",				
	'IdIngresoEgreso' AS "CodigoDato",
	CAST(RP."Id" as NVARCHAR(50)) AS "ValorDato",
	RP."Id",
	RP."Entidad",
	'N' AS "RequiereAutorizacion"
FROM #AnularRegistros_p_RegistrosAProcesar as RP
LEFT JOIN "BSCL.Tesoreria.BD::TES.IngresoEgreso" AS IE ON (IE."IdIngresoEgreso" = RP."Id")
WHERE RP."Entidad" = 'TES.IngresoEgreso'
UNION ALL

-- Valida movimientos que requieren de autorizacion para ser anulados
SELECT  2 AS "Prioridad",
	CASE 
		 WHEN (IE."IdEstado" = 'ING') THEN 0
		 ELSE  1
	END AS "Cumple",
	
	CASE 
		 WHEN (IE."IdEstado" = 'ING') THEN 'ANULAR_MOVIMIENTO_REQUIERE_AUTORIZACION'
		 ELSE  ''
	END AS "CodigoError",					
	'Id' AS "CodigoDato",
	CAST(RP."Id" as NVARCHAR(50)) AS "ValorDato",
	RP."Id",
	RP."Entidad",
	'S' AS "RequiereAutorizacion"
FROM #AnularRegistros_p_RegistrosAProcesar as RP
INNER JOIN "BSCL.Tesoreria.BD::TES.IngresoEgreso" AS IE ON (IE."IdIngresoEgreso" = RP."Id")
INNER JOIN "BSCL.Plataforma.BD::PLA.RequiereAutorizacion" AS AUT ON (AUT."IdConcepto" = 'ANMOTE' AND AUT."IdEmpresa" = IE."IdEmpresa")
WHERE RP."Entidad" = 'TES.IngresoEgreso'
AND IE."IdEvento" < 1
AND COALESCE(AUT."RequiereAutorizacion",'S') = 'S' 
UNION ALL

-- Valida estado de autorizacion de movimientos
SELECT  2 AS "Prioridad",
		CASE
			 WHEN (IE."IdEvento" > 0 AND EVE."IdEstado" = 'VCD') THEN 0
			 WHEN (IE."IdEvento" > 0 AND EVE."IdEstado" = 'VIG') THEN 0					 
			 ELSE  1
		END AS "Cumple",
		CASE 
			 WHEN (IE."IdEvento" > 0 AND EVE."IdEstado" = 'VCD') THEN 'EVENTO_SE_ENCUENTRA_VENCIDO'
			 WHEN (IE."IdEvento" > 0 AND EVE."IdEstado" = 'VIG') THEN 'EVENTO_SIN_AUTORIZACION'
			 ELSE  ''
		END AS "CodigoError",		       	
		'IdEvento' AS "CodigoDato",				
       	CAST(RP."Id" AS NVARCHAR(50)) AS "ValorDato",
       	RP."Id",
		RP."Entidad",
		'N' AS "RequiereAutorizacion"
FROM #AnularRegistros_p_RegistrosAProcesar AS RP
INNER JOIN "BSCL.Tesoreria.BD::TES.IngresoEgreso" AS IE ON (IE."IdIngresoEgreso" = RP."Id")
LEFT JOIN "BSCL.Plataforma.BD::AUT.Evento" AS EVE ON (EVE."IdEvento" = IE."IdEvento")
WHERE RP."Entidad" = 'TES.IngresoEgreso'
AND IE."IdEstado" = 'ING'
UNION ALL

-- Valida que la anulación de movimientos provenga del módulo que generó el movimiento
SELECT  2 AS "Prioridad",
    "Cumple",
    CASE WHEN "Cumple" = 0 THEN 'MOVIMIENTO_TESORERIA_GENERADO_MESA_RF_NO_ANULAR' ELSE '' END AS "CodigoError",
    'IdIngresoEgreso' AS "CodigoDato",
    CAST("Id" AS NVARCHAR(50)) AS "ValorDato",
    "Id",
    "Entidad",
    'N' AS "RequiereAutorizacion"
FROM MRF_Validacion;