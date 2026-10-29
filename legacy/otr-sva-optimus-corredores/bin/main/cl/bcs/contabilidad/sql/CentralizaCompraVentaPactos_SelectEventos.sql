-- Lee Comprobantes solicitudes de ingreso y egreso liquidados
SELECT 
	 TO_BIGINT(:IdTipoCentralizacion)							AS "IdTipoCentralizacion"
	, ConsultaSubyacente."IdSubyacente"							AS "IdMovimiento"
	,'MONTO_PAPEL'										AS "IdEvento"
	,'Monto Papel' 								AS "Nombre"
	,ConsultaSubyacente."MontoSubyacente"		AS "MontoEvento"
	,"IdMonedaSistema"							AS "CodigoMoneda"
	FROM "BSCL.Custodia.BD.Vistas::MES.ConsultarCarteraPacto"			CCP
	INNER  JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarTablaSubyacente"	ConsultaSubyacente
	ON (CCP."FolioPacto" = ConsultaSubyacente."FolioCarteraRelacionada")
	WHERE 
		CCP."IdEstado" 		= 'VIG' 									AND 
		CCP."FechaPacto" 	= :FechaContable 							AND 
		"IdMonedaSistema"   = ConsultaSubyacente."IdMonedaInstrumento"
UNION 
SELECT 
	  TO_BIGINT(:IdTipoCentralizacion)							AS "IdTipoCentralizacion"
	, ConsultaSubyacente."IdSubyacente"							AS "IdMovimiento"
	,'VALOR_PAPEL'												AS "IdEvento"
	,'Valor Papel' 												AS "Nombre"
	,ConsultaSubyacente."MontoSubyacenteMonedaInstrumento"		AS "MontoEvento"
	,ConsultaSubyacente."IdMonedaInstrumento"					AS "CodigoMoneda"
	FROM "BSCL.Custodia.BD.Vistas::MES.ConsultarCarteraPacto"			CCP
	INNER  JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarTablaSubyacente"	ConsultaSubyacente
	ON (CCP."FolioPacto" = ConsultaSubyacente."FolioCarteraRelacionada")
	WHERE 
		CCP."IdEstado" = 'VIG' 				AND 
		CCP."FechaPacto" = :FechaContable 	AND 
		"IdMonedaSistema"   <> ConsultaSubyacente."IdMonedaInstrumento"	
UNION
SELECT 
	  TO_BIGINT(:IdTipoCentralizacion)					AS "IdTipoCentralizacion"
	, ConsultaSubyacente."IdSubyacente"			AS "IdMovimiento"
	,'MONTO_INICIAL_PACTO'						AS "IdEvento"
	,'Monto Inicial Pacto' 						AS "Nombre"
	,"MontoOperacionInicial"					AS "MontoEvento"
	,"IdMonedaPrincipal"						AS "CodigoMoneda"
	FROM "BSCL.Mesa.BD::MES.Operacion"								MES		
	LEFT JOIN "BSCL.Custodia.BD.Vistas::MES.ConsultarCarteraPacto"		CCP
	ON (MES."IdOperacion" = CCP."IdOperacion")
	INNER  JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarTablaSubyacente"	ConsultaSubyacente
	ON (CCP."FolioPacto" = ConsultaSubyacente."FolioCarteraRelacionada")
	WHERE 
		CCP."IdEstado" = 'VIG' 			AND 
		CCP."FechaPacto" = :FechaContable
	GROUP BY ConsultaSubyacente."IdSubyacente", "MontoOperacionInicial", "IdMonedaPrincipal"
UNION
SELECT 
	TO_BIGINT(:IdTipoCentralizacion)			AS "IdTipoCentralizacion"
	,ConsultaSubyacente."IdSubyacente"			AS "IdMovimiento"
	,'MONTO_FINAL_PACTO'						AS "IdEvento"
	,'Monto Final Pacto' 						AS "Nombre"
	,"MontoOperacionFinal"						AS "MontoEvento"
	,"IdMonedaPrincipal"						AS "CodigoMoneda"
	FROM "BSCL.Mesa.BD::MES.Operacion"								MES		
	LEFT JOIN "BSCL.Custodia.BD.Vistas::MES.ConsultarCarteraPacto"		CCP
	ON (MES."IdOperacion" = CCP."IdOperacion")
	INNER  JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarTablaSubyacente"	ConsultaSubyacente
	ON (CCP."FolioPacto" = ConsultaSubyacente."FolioCarteraRelacionada")
	WHERE 
		CCP."IdEstado" = 'VIG' 				AND 
		CCP."FechaPacto" = :FechaContable
	GROUP BY ConsultaSubyacente."IdSubyacente", "MontoOperacionFinal", "IdMonedaPrincipal"	

UNION
SELECT 
	  TO_BIGINT(:IdTipoCentralizacion)					AS "IdTipoCentralizacion"
	, ConsultaSubyacente."IdSubyacente"					AS "IdMovimiento"
	,'MONTO_DIFERENCIA'									AS "IdEvento"
	,'Monto Diferencia' 								AS "Nombre"
	,("MontoOperacionFinal"	-"MontoOperacionInicial")	AS "MontoEvento"
	,"IdMonedaPrincipal"								AS "CodigoMoneda"
	FROM "BSCL.Mesa.BD::MES.Operacion"								MES		
	LEFT JOIN "BSCL.Custodia.BD.Vistas::MES.ConsultarCarteraPacto"		CCP
	ON (MES."IdOperacion" = CCP."IdOperacion")
	INNER  JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarTablaSubyacente"	ConsultaSubyacente
	ON (CCP."FolioPacto" = ConsultaSubyacente."FolioCarteraRelacionada")
	WHERE 
		CCP."IdEstado" = 'VIG' 			AND 
		CCP."FechaPacto" = :FechaContable
	GROUP BY ConsultaSubyacente."IdSubyacente", "MontoOperacionFinal", "MontoOperacionInicial", "IdMonedaPrincipal"
UNION
SELECT 
	  TO_BIGINT(:IdTipoCentralizacion)			AS "IdTipoCentralizacion"
	, ConsultaSubyacente."IdSubyacente"			AS "IdMovimiento"
	,'VALOR_INICIAL_PACTO'						AS "IdEvento"
	,'Valor Inicial Pacto' 						AS "Nombre"
	,"MontoOperacionInicialMoneda"				AS "MontoEvento"
	,"IdMonedaSecundaria"						AS "CodigoMoneda"
	FROM "BSCL.Mesa.BD::MES.Operacion"								MES		
	LEFT JOIN "BSCL.Custodia.BD.Vistas::MES.ConsultarCarteraPacto"		CCP
	ON (MES."IdOperacion" = CCP."IdOperacion")
	INNER  JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarTablaSubyacente"	ConsultaSubyacente
	ON (CCP."FolioPacto" = ConsultaSubyacente."FolioCarteraRelacionada")
	WHERE 
		CCP."IdEstado" = 'VIG' 				AND 
		CCP."FechaPacto" = :FechaContable 	AND 
		"IdMonedaSistema"   <> ConsultaSubyacente."IdMonedaInstrumento"
	GROUP BY ConsultaSubyacente."IdSubyacente", "MontoOperacionInicialMoneda", "IdMonedaSecundaria"
UNION
SELECT 
	  TO_BIGINT(:IdTipoCentralizacion)			AS "IdTipoCentralizacion"
	, ConsultaSubyacente."IdSubyacente"			AS "IdMovimiento"
	,'VALOR_FINAL_PACTO'						AS "IdEvento"
	,'Valor Final Pacto' 						AS "Nombre"
	,"MontoOperacionFinalMoneda"				AS "MontoEvento"
	,"IdMonedaSecundaria"						AS "CodigoMoneda"
	FROM "BSCL.Mesa.BD::MES.Operacion"								MES		
	LEFT JOIN "BSCL.Custodia.BD.Vistas::MES.ConsultarCarteraPacto"		CCP
	ON (MES."IdOperacion" = CCP."IdOperacion")
	INNER  JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarTablaSubyacente"	ConsultaSubyacente
	ON (CCP."FolioPacto" = ConsultaSubyacente."FolioCarteraRelacionada")
	WHERE 
		CCP."IdEstado" = 'VIG' 				AND 
		CCP."FechaPacto" = :FechaContable 	AND 
		"IdMonedaSistema"   <> ConsultaSubyacente."IdMonedaInstrumento" 
	GROUP BY ConsultaSubyacente."IdSubyacente", "MontoOperacionFinalMoneda", "IdMonedaSecundaria"	
UNION
SELECT 
	  TO_BIGINT(:IdTipoCentralizacion)					AS "IdTipoCentralizacion"
	, ConsultaSubyacente."IdSubyacente"								AS "IdMovimiento"
	,'VALOR_DIFERENCIA'												AS "IdEvento"
	,'Valor Diferencia' 											AS "Nombre"
	,("MontoOperacionFinalMoneda" - "MontoOperacionInicialMoneda")	AS "MontoEvento"
	,"IdMonedaSecundaria"											AS "CodigoMoneda"
	FROM "BSCL.Mesa.BD::MES.Operacion"								MES		
	LEFT JOIN "BSCL.Custodia.BD.Vistas::MES.ConsultarCarteraPacto"		CCP
	ON (MES."IdOperacion" = CCP."IdOperacion")
	INNER  JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarTablaSubyacente"	ConsultaSubyacente
	ON (CCP."FolioPacto" = ConsultaSubyacente."FolioCarteraRelacionada")
	WHERE 
		CCP."IdEstado" = 'VIG' 				AND 
		CCP."FechaPacto" = :FechaContable 	AND 
		"IdMonedaSistema"   <> ConsultaSubyacente."IdMonedaInstrumento" 
	GROUP BY ConsultaSubyacente."IdSubyacente", "MontoOperacionFinalMoneda", "MontoOperacionInicialMoneda", "IdMonedaSecundaria"	;