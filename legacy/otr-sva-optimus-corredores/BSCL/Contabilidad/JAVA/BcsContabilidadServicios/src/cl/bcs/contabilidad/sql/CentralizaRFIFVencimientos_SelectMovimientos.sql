SELECT
	TO_BIGINT(:IdTipoCentralizacion)						AS "IdTipoCentralizacion",
	ConsultarCartera."IdCartera"							AS "IdMovimiento",
	CASE ConsultarCartera."IdClasificacionTipoInstrumento"
		WHEN 'IF'	
			THEN CASE "IdTipoOperacion" 
					WHEN 'TDEPLA' THEN 'TDEPLA' 
					WHEN 'TAUTDI' THEN 'TAUTDI'  
					ELSE  'VECTOIF' 
				END
						
		WHEN 'RF' 	
			THEN 'VECTORF'
		ELSE '??????'				
	END														AS "IdTipoMovimiento",
	ConsultarCartera."IdEmpresa"         					AS "IdEmpresa",									    
	ConsultarCartera."FolioCartera"							AS "Numero",
	ConsultarCartera."FolioCartera" 						AS "Linea",
	YEAR(ConsultarCartera."EGRFechaOperacion") ||'-'|| RIGHT('0' || MONTH(ConsultarCartera."EGRFechaOperacion"),2) ||'-'|| RIGHT('0' || DAYOFMONTH(ConsultarCartera."EGRFechaOperacion"),2) AS "Fecha",
	ConsultarCartera."DocumentoIdentidad"					AS "DocumentoIdentidad",  
	ConsultarCartera."IdPortafolio"							AS "IdPortafolio",
	TO_BIGINT(YEAR(ConsultarCartera."EGRFechaOperacion"))	AS "Ejercicio",
	RIGHT('0' || MONTH(ConsultarCartera."EGRFechaOperacion"),2) ||'-'|| YEAR(ConsultarCartera."EGRFechaOperacion") 	AS "Periodo",
	ABS(ConsultarCartera."EGRMonto")						AS "Monto",
	ConsultarCartera."IdMonedaSistema"						AS "IdMoneda",                                 
	ConsultarCartera."IdEstado"								AS "Estado"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" ConsultarCartera
WHERE 
	--ConsultarCartera."IdEmpresa" 	= :p_IdEmpresa
	"IdEstado"='VCD'
	AND (
			("EGRIdTipoOperacion"		='VCDTIT' AND "EGRFechaOperacion" 	   				= :FechaContable)
		OR  ("EGRIdTipoOperacion"		='VCDPAC' AND "FechaVencimientoInstrumento" 	   	= :FechaContable)	
		)
		
   	AND "IdClasificacionTipoInstrumento" 	IN ('RF','IF'); 