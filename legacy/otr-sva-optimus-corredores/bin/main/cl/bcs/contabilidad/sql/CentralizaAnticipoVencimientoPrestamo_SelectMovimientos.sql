SELECT
	TO_BIGINT(:IdTipoCentralizacion)								AS "IdTipoCentralizacion"
	,"IdCartera"													AS "IdMovimiento"
	,CASE "IdEstado"
		WHEN 'VCD'			THEN 'PRESTVCT'			
		WHEN 'ANT'			THEN 'PRESTANT'			
		ELSE 'ERROR'			
	END																AS "IdTipoMovimiento"
	,"IdEmpresa"													AS "IdEmpresa"								    
	,"FolioPrestamo"												AS "Numero"
	,"FolioPrestamo"												AS "Linea"
	,"DocumentoIdentidad"											AS "DocumentoIdentidad"  
	,COALESCE("IdPortafolio", 0)									AS "IdPortafolio"
	,TO_BIGINT(YEAR("EGRFechaOperacion"))							AS "Ejercicio"
	,RIGHT('0' || MONTH("EGRFechaOperacion"),2) ||'-'|| 
	TO_BIGINT(YEAR("EGRFechaOperacion"))							AS "Periodo"
    ,"EGRMontoAPlazo"												AS "Monto"
    ,"IdMonedaSubyacente"											AS "IdMoneda"
    ,"IdEstado"														AS "Estado"
	,YEAR(:FechaContable) ||'-'|| 
	RIGHT('0' || MONTH(:FechaContable),2) ||'-'|| 
	RIGHT('0' || DAYOFMONTH(:FechaContable),2)						AS "Fecha"
    
	FROM "BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenPrestamoHistorico"
WHERE
    	"IdEmpresa" 		= :IdEmpresa		AND 
    	"EGRFechaOperacion" = :FechaContable 	AND
    	"IdEstado"			IN ('VCD', 'ANT')	AND
    	"IdClasificacionTipoInstrumento"= :ClasificacionTipoInstrumento;