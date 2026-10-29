SELECT
	TO_BIGINT(:IdTipoCentralizacion)                    					AS "IdTipoCentralizacion",
	CAR."FolioCartera"														AS "IdMovimiento",
	'EMPRESTINI'															AS "IdTipoMovimiento",
	CAR."IdEmpresa"         												AS "IdEmpresa",									    
	CAR."FolioCartera"														AS "Numero",
	"FolioCartera" 															AS "Linea",
	YEAR(:FechaContable) ||'-'|| 
	RIGHT('0' || MONTH(:FechaContable),2) ||'-'|| 
	RIGHT('0' || DAYOFMONTH(:FechaContable),2) 								AS "Fecha",
	CLI."DocumentoIdentidad"												AS "DocumentoIdentidad",  
	CAR."IdPortafolio"														AS "IdPortafolio",
	TO_BIGINT(YEAR(CAR."FechaLiquidacion"))									AS "Ejercicio",
	RIGHT('0' || MONTH(CAR."FechaLiquidacion"),2) ||'-'|| 
	TO_BIGINT(YEAR(CAR."FechaLiquidacion"))									AS "Periodo",
	
    CAR."MontoCompra"														AS "Monto",
    SUB."IdMonedaInstrumento" 												AS "IdMoneda",
    CAR."IdEstado"															AS "Estado"
FROM "BSCL.Custodia.BD::HIS.Cartera" CAR
LEFT JOIN "BSCL.Custodia.BD::HIS.Subyacente" SUB ON SUB."FolioCarteraRelacionada" = CAR."FolioCartera"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI ON CLI."IdCliente"= CAR."IdCliente" 
WHERE
    CAR."IdEmpresa" 					= :IdEmpresa		AND 
    CAR."FechaLiquidacion"	 			= :FechaContable 	AND
    CAR."FechaRespaldo"					= :FechaContable	AND
    SUB."FechaRespaldo"					= :FechaContable	AND
    CAR."IdEstado"						= 'VIG'				AND
    CAR."IdTipoInstrumento" 			IN ('EMP','ERF')	AND
    CAR."IdClasificacionTipoInstrumento"= :IdClasificacionTipoInstrumento
    
GROUP BY 
	CAR."FolioCartera"
    ,CAR."IdEmpresa"
    ,CLI."DocumentoIdentidad"
    ,CAR."IdPortafolio"
    ,CAR."FechaLiquidacion"
    ,CAR."MontoCompra"
    ,SUB."IdMonedaInstrumento"
    ,CAR."IdEstado";