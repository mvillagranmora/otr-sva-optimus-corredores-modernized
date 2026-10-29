SELECT
	TO_BIGINT(:IdTipoCentralizacion)                    						AS "IdTipoCentralizacion",
	"FolioPrestamo"																AS "IdMovimiento",
	'PRESTINI'																	AS "IdTipoMovimiento",
	"IdEmpresa"         														AS "IdEmpresa",									    
	"FolioPrestamo"																AS "Numero",
	"FolioPrestamo" 															AS "Linea",
	YEAR(:FechaContable) ||'-'|| 
	RIGHT('0' || MONTH(:FechaContable),2) ||'-'|| 
	RIGHT('0' || DAYOFMONTH(:FechaContable),2) 									AS "Fecha",
	"DocumentoIdentidad"														AS "DocumentoIdentidad",  
	"IdPortafolio"																AS "IdPortafolio",
	TO_BIGINT(YEAR("FechaLiquidacion"))											AS "Ejercicio",
	RIGHT('0' || MONTH("FechaLiquidacion"),2) ||'-'|| 
	TO_BIGINT(YEAR("FechaLiquidacion"))											AS "Periodo",
	
    "MontoInstrumento"															AS "Monto",
    "IdMonedaSubyacente" 														AS "IdMoneda",
    "IdEstado"																	AS "Estado"
FROM "BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenPrestamoVigente" 
WHERE
    	"IdEmpresa" 						= :IdEmpresa		AND 
    	"FechaLiquidacion" 					= :FechaContable 	AND
    	"IdEstado"							= 'VIG'				AND
		"IdClasificacionTipoInstrumento"	= :IdClasificacionTipoInstrumento		
GROUP BY
		"FolioPrestamo",
		"IdEmpresa",									    
		"FolioPrestamo",
		"FolioPrestamo",
		"DocumentoIdentidad",  
		"IdPortafolio",
		"FechaLiquidacion",
	    "MontoInstrumento",
	    "IdMonedaSubyacente",
	    "IdEstado";