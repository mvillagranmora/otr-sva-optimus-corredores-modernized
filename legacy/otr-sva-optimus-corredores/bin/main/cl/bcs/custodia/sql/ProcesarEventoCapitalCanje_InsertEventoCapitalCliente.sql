INSERT INTO "BSCL.Custodia.BD::CUS.EventoCapitalCliente"(
		"IdEventoCapitalCliente",			
		"IdEventoCapital",			
		"IdTipoEventoCapital",		
		"Secuencia",			
		"IdSocioNegocio",					
		"TipoSocioNegocio",			   		
		"IdPortafolio",				
		"IdTipoDocumentoIdentidad",	
		"DocumentoIdentidad",	
		"NombreCliente",				
		"IdClasificacionTipoInstrumento",	
		"IdTipoInstrumento",		
		"IdInstrumento",			
		"Nemo",					
		"IdInstrumentoNuevo",
		"NemoNuevo",						
		"FechaCierre",						
		"FechaPago",				
		"CantidadDerecho",		
		"TipoCartera",						
		"CantidadNueva",					
		"MontoCapital",
		"Precio",						
		"PrecioNuevo",						
		"MontoInteres",				
		"Total",				
		"IdMoneda",						
		"SecuenciaCupon",
		"IdEmpleadoEjecutivoPortafolio",
		"CodigoUsuarioAgente",				
		"NombreAgente",				
		"CodigoSucursal",		
		"FechaHoraIngresoRegistro",
		"FechaHoraUltimaActualizacion",		
		"IdUsuario",						
		"IdEstado",
		"IdBoveda",
		"IdBolsa",
		"IdTipoOperacionGarantia",
		"IdConceptoGarantia",
		"IdTipoReajuste",
		"CodigoCorredorContraparte",
		"TipoOperacion",
		"NumeroDividendo",
		"Origen")
	SELECT
	    "BSCL.Custodia.BD.Secuencias::EventoCapitalCliente".NEXTVAL,																					--	IdEventoCapitalCliente
		EVC."IdEventoCapital",																															--	IdEventoCapital
		EVC."IdTipoEventoCapital",																														--	IdTipoEventoCapital
		EVC."Secuencia",																																--	Secuencia
		IFNULL(SOC."IdSocioDeNegocio",0),																												--	IdSocioNegocio
		'',																																				--	TipoSocioNegocio
		IFNULL(FT."IdPortafolio",0),																													--	IdPortafolio
		'R',--																																			--	IdTipoDocumentoIdentidad
		IFNULL(CL."DocumentoIdentidad",''),																												--	DocumentoIdentidad			
		IFNULL(CL."NombreCompleto",''),																													--	NombreCliente
		EVC."IdClasificacionTipoInstrumento",																											--	IdClasificacionTipoInstrumento
		EVC."IdTipoInstrumento",																														--  IdTipoInstrumento
		EVC."IdInstrumento", 																															--	IdInstrumento
		EVC."Nemo",																																		--	Nemo
		EVC."IdInstrumentoNuevo",																														--	IdInstrumentoNuevo		
		EVC."NemoNuevo",																																--  NemoNuevo
		TO_DATE(EVC."FechaLimite", 'YYYY-MM-DD'),																										--	FechaCierre
		TO_DATE(EVC."FechaPago", 'YYYY-MM-DD'),																											--	FechaPago
		SUM(IFNULL(FT."Cantidad",0)),																													-- Cantidad Derecho
		'CUS',
		--IMPMBIOPT-687 punto 1 : no debe permitir asignar cantidades menor al entero. Se incorpora FLOOR en la division
		FLOOR(SUM(IFNULL(FT."Cantidad",0)) * EVC."FactorNuevo" / EVC."Factor"),																			--	CantidadNueva		
		0,																																				--	MontoCapital
		IFNULL(FT."PrecioCompra",0),																													-- 	Precio
		CASE  
			WHEN EVC."Factor" > EVC."FactorNuevo" THEN 	(IFNULL(FT."PrecioCompra",0) * EVC."Factor")
			ELSE 			(IFNULL(FT."PrecioCompra",0) / EVC."FactorNuevo")
		END, 																																			-- 	Precio Nuevo
		0,																																				--	MontoInteres
		CASE 
			WHEN EVC."Factor" > EVC."FactorNuevo" THEN 	(SUM(IFNULL(FT."Cantidad",0)) / EVC."Factor") * (FT."PrecioCompra" * EVC."Factor")
			ELSE 			(SUM(IFNULL(FT."Cantidad",0)) * EVC."FactorNuevo") * (FT."PrecioCompra" / EVC."FactorNuevo")
		END,																																			--	Total
		EVC."IdMoneda",																																	--	IdMoneda
		0,																																				--	SecuenciaCupon
		COALESCE(EJE."IdEmpleadoEjecutivoPortafolio",0),																								-- IdEmpleadoEjecutivoPortafolio
		COALESCE(EJE."CodigoEmpleado",''),																												--	"CodigoUsuarioAgente"
		COALESCE(EJE."NombreCompletoEjecutivo",''),																										--	"NombreAgente"
		COALESCE(EJE."CodigoSucursal",''),																												--	"CodigoSucursal"
		Current_UTCTIMESTAMP,														 																	--	FechaHoraIngresoRegistro
		Current_UTCTIMESTAMP,														 																	--	FechaHoraUltimaActualizacion
		:IdUsuario,																																		--	IdUsuario
		'PRO',																																			--	IdEstado
		CASE FT."IdBoveda"
			WHEN '' THEN 'CIA'
			ELSE FT."IdBoveda"
		END,																																			--  IdBoveda
		CASE FT."IdBolsa" 
			when '' then 'BCS'
			ELSE IFNULL(FT."IdBolsa",'BCS')																												--  IdBolsa
		END,
		'',																																				--"IdTipoOperacionGarantia"
		'',																																				--"IdConceptoGarantia"
		'',																																				--"IdTipoReajuste"
		'',																																				--"CodigoCorredorContraparte"
		'',																																				--"TipoOperacion"
		EVC."NumeroDividendo",																															--NumeroDividendo
		'SIS'
	FROM  "BSCL.Custodia.BD::CUS.Cartera"	AS FT
		LEFT JOIN 	"BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente"				AS CL
			ON (FT."IdCliente" = CL."IdCliente")
		LEFT JOIN 	"BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" 		AS SOC
			ON (CL."IdCliente" = SOC."IdCliente")
		LEFT JOIN 	"BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivoPortafolio"	AS EJE
			ON (FT."IdPortafolio" = EJE."IdPortafolio")
		LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapital"								AS EVC
			ON EVC."IdEventoCapital"				= :IdEventoCapital
	WHERE	FT."IdEmpresa" 						= :IdEmpresa  
		AND FT."IdInstrumento"					= :IdInstrumento
		AND FT."IdCliente" 						> 0
		AND IFNULL(SOC."IdSocioDeNegocio",0)	> 0
		AND FT."FechaOperacion" 				<= :FechaLimite
		AND FT."IdEstado" 						= 'VIG'
	GROUP BY
		EVC."IdEventoCapital",
		EVC."IdTipoEventoCapital",
		EVC."Secuencia",
		SOC."IdSocioDeNegocio",
		FT."IdPortafolio",
		CL."DocumentoIdentidad",
		CL."NombreCompleto",
		EVC."IdClasificacionTipoInstrumento",
		EVC."IdTipoInstrumento",
		EVC."IdInstrumento",
		EVC."Nemo",
		EVC."IdInstrumentoNuevo",
		EVC."NemoNuevo",
		EVC."FechaLimite",
		EVC."FechaPago",
		EVC."IdUsuario",
		EVC."NumeroDividendo",
		EVC."Factor",
		EVC."FactorNuevo",
		FT."PrecioCompra",
		EVC."IdMoneda",
		COALESCE(EJE."IdEmpleadoEjecutivoPortafolio",0),
		COALESCE(EJE."CodigoEmpleado",''),
		COALESCE(EJE."NombreCompletoEjecutivo",''),
		COALESCE(EJE."CodigoSucursal",''),		
		CASE FT."IdBoveda"
			WHEN '' THEN 'CIA'
			ELSE FT."IdBoveda"
		END,
		CASE FT."IdBolsa" 
			WHEN '' then 'BCS'
			ELSE IFNULL(FT."IdBolsa",'BCS')										
		END;