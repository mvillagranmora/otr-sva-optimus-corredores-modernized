UPDATE IEFP
	SET "IdEstado" = CASE WHEN :p_IdModulo != 'MRF' THEN 'ING' ELSE 'ANU' END  
FROM "BSCL.Tesoreria.BD::TES.IngresoEgresoFormaDePago" AS IEFP
INNER JOIN #AnularRegistros_ComprobantesAnular AS CA ON (CA."IdIngresoEgresoOrigen" = IEFP."IdIngresoEgreso")
WHERE IEFP."IdEstado" <> 'ANU'

AND IEFP."IdEstado" <> 'MOD';