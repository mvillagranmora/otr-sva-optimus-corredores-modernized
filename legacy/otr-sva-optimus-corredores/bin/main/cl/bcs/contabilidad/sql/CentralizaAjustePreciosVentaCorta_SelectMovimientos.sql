SELECT 
	TO_BIGINT(:IdTipoCentralizacion)                    				AS "IdTipoCentralizacion",
	ConsultarPrestamo."IdCartera"										AS "IdMovimiento",
	'CARTERARV'															AS "IdTipoMovimiento",
	ConsultarPrestamo."IdEmpresa"         								AS "IdEmpresa",									    
	ConsultarPrestamo."FolioPrestamo"									AS "Numero",
	TO_BIGINT(0)														AS "Linea",
	YEAR(:FechaContable) ||'-'|| 
	RIGHT('0' || MONTH(:FechaContable),2) ||'-'|| 
	RIGHT('0' || DAYOFMONTH(:FechaContable),2) 							AS "Fecha",
	ConsultarPrestamo."DocumentoIdentidad"								AS "DocumentoIdentidad",  
	ConsultarPrestamo."IdPortafolio"									AS "IdPortafolio",
	TO_BIGINT(YEAR(ConsultarPrestamo."INGFechaOperacion"))				AS "Ejercicio",
	RIGHT('0' || MONTH(ConsultarPrestamo."INGFechaOperacion"),2) ||'-'|| 
	YEAR(ConsultarPrestamo."INGFechaOperacion") 						AS "Periodo",
	ABS(ConsultarPrestamo."INGMontoInstrumento")						AS "Monto",
	ConsultarPrestamo."IdMonedaSistema"									AS "IdMoneda",                            
	ConsultarPrestamo."IdEstado"										AS "Estado"
FROM "BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenPrestamoVigente" ConsultarPrestamo
WHERE "IdEmpresa" 				=:IdEmpresa
	AND "INGFechaOperacion" 	<= :FechaContable 
	AND "EGRCantidadPrestamo" 	= 0
    AND "IdClasificacionTipoInstrumento" = 'RV'
    AND "IdTipoInstrumento" IN ('PRT', 'EMP')
--    AND "DocumentoIdentidad" 	= :DocumentoIdentidad
    AND "IdEstado" 				= 'VIG';
