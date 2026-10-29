SELECT
	TO_BIGINT(:IdTipoCentralizacion)                    						AS "IdTipoCentralizacion",
	"FolioEmprestito"															AS "IdMovimiento",
	'EMPRESTINI'																AS "IdTipoMovimiento",
	"IdEmpresa"         														AS "IdEmpresa",									    
	"FolioEmprestito"															AS "Numero",
	"FolioEmprestito" 															AS "Linea",
	YEAR(:FechaContable) ||'-'|| 
	RIGHT('0' || MONTH(:FechaContable),2) ||'-'|| 
	RIGHT('0' || DAYOFMONTH(:FechaContable),2) 									AS "Fecha",
	"DocumentoIdentidad"														AS "DocumentoIdentidad",  
	"IdPortafolioPrestamo"														AS "IdPortafolio",
	TO_BIGINT(YEAR("FechaLiquidacion"))											AS "Ejercicio",
	RIGHT('0' || MONTH("FechaLiquidacion"),2) ||'-'|| 
	TO_BIGINT(YEAR("FechaLiquidacion"))											AS "Periodo",
	
    "MontoInstrumento"															AS "Monto",
    "IdMonedaSubyacente" 														AS "IdMoneda",
    "IdEstadoSubyacente"														AS "Estado"
FROM "BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenEmprestitoVigente" 
WHERE
   	"IdEmpresa" 		= :IdEmpresa		AND 
    "FechaLiquidacion"	= :FechaContable 	AND
    "IdEstado"			= 'VIG'				AND
    "IdClasificacionTipoInstrumento" = :IdClasificacionTipoInstrumento
GROUP BY 
	"FolioEmprestito"
    ,"IdEmpresa"
    ,"DocumentoIdentidad"
    ,"IdPortafolioPrestamo"
    ,"FechaLiquidacion"
    ,"MontoInstrumento"
    ,"IdMonedaSubyacente"
    ,"IdEstadoSubyacente";