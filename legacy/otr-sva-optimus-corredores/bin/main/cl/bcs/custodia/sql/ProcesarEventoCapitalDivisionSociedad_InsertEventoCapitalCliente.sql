INSERT INTO "BSCL.Custodia.BD::CUS.EventoCapitalCliente"
	   	(
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
			"NumeroDividendo",
			"Origen")
SELECT 
	"BSCL.Custodia.BD.Secuencias::EventoCapitalCliente".NEXTVAL,
	EVC."IdEventoCapital",
	EVC."IdTipoEventoCapital",
	EVC."Secuencia",
	IFNULL(SOC."IdSocioDeNegocio",0),
	CASE 
		WHEN SOC."IdCliente" > 0 		THEN 'CLI'
		WHEN SOC."IdProveedor" > 0 		THEN 'PRO'
		WHEN SOC."IdEmpleado" > 0 		THEN 'EMP'
		WHEN SOC."IdContraparte" > 0 	THEN 'CNT'
	END	,
	IFNULL(FT."IdPortafolio",0),
	'R',
	IFNULL(CL."DocumentoIdentidad",''),		
	IFNULL(CL."NombreCompleto",''),
	EVC."IdClasificacionTipoInstrumento",
	EVC."IdTipoInstrumento",
	EVC."IdInstrumento",
	EVC."Nemo",
	EVC."IdInstrumentoNuevo",
	EVC."NemoNuevo",
	TO_DATE(EVC."FechaLimite", 'YYYY-MM-DD'),
	TO_DATE(EVC."FechaPago", 'YYYY-MM-DD'),	
	SUM(IFNULL(FT."Cantidad",0)),
	CASE FT."IdTipoInstrumento"
        WHEN 'SIM' THEN
          CASE FT."Nemotecnico"   
            WHEN 'SIMULTANEA VENTA'   THEN 'SIV'
            WHEN 'SIMULTANEA COMPRA'  THEN 'SIC'
            ELSE 'SIM'
          END
        WHEN 'PRT' THEN 'PRT'
        WHEN 'EMP' THEN 'EMP'
        ELSE 'CUS'
    END,
	SUM(IFNULL(FT."Cantidad",0)) * EVC."FactorNuevo",
	0,
	FT."PrecioCompra",
	IFNULL(FT."PrecioCompra",0) * EVC."Factor",
	0,
	(SUM(IFNULL(FT."Cantidad",0)) * EVC."FactorNuevo") * (FT."PrecioCompra" * EVC."FactorNuevo"),
	EVC."IdMoneda",
	0,
	COALESCE(EJE."IdEmpleadoEjecutivoPortafolio",0),
	COALESCE(EJE."CodigoEmpleado",''),
	COALESCE(EJE."NombreCompletoEjecutivo",''),
	COALESCE(EJE."CodigoSucursal",''),
	Current_UTCTIMESTAMP,
	Current_UTCTIMESTAMP,
	:IdUsuario,
	'PRO',
	CASE FT."IdBoveda"
		WHEN '' THEN 'COR'
		ELSE FT."IdBoveda"
	END,
	Case FT."IdBolsa" 
		WHEN '' THEN 'BCS'
		ELSE IFNULL(FT."IdBolsa",'BCS')
	END,
	EVC."NumeroDividendo",
	'SIS'
FROM  "BSCL.Custodia.BD::CUS.Cartera"	AS FT
LEFT JOIN 	"BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente"				AS CL
	ON (FT."IdCliente" = CL."IdCliente")
LEFT JOIN 	"BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" 		AS SOC
	ON (CL."IdCliente" = SOC."IdCliente")
LEFT JOIN 	"BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivoPortafolio"	AS EJE
	ON (FT."IdPortafolio" = EJE."IdPortafolio")
LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapital"								AS EVC
	ON EVC."IdEventoCapital" = :IdEventoCapital
WHERE	FT."IdEmpresa" = :IdEmpresa  
		AND FT."IdInstrumento" = :IdInstrumento
		AND FT."IdCliente" > 0
		AND IFNULL(SOC."IdSocioDeNegocio",0) > 0
		AND FT."FechaOperacion" <= :FechaLimite
		AND FT."IdEstado" = 'VIG'
GROUP BY
	EVC."IdEventoCapital",
	EVC."IdTipoEventoCapital",
	EVC."Secuencia",
	CASE FT."IdTipoInstrumento"
        WHEN 'SIM' THEN
          CASE FT."Nemotecnico"   
            WHEN 'SIMULTANEA VENTA'   THEN 'SIV'
            WHEN 'SIMULTANEA COMPRA'  THEN 'SIC'
            ELSE 'SIM'
          END
        WHEN 'PRT' THEN 'PRT'
        WHEN 'EMP' THEN 'EMP'
        ELSE 'CUS'
    END,
	SOC."IdSocioDeNegocio",
	CASE 
		WHEN SOC."IdCliente" > 0 		THEN 'CLI'
		WHEN SOC."IdProveedor" > 0 		THEN 'PRO'
		WHEN SOC."IdEmpleado" > 0 		THEN 'EMP'
		WHEN SOC."IdContraparte" > 0 	THEN 'CNT'
	END	,
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
		WHEN '' THEN 'COR'
		ELSE FT."IdBoveda"
	END,
	Case FT."IdBolsa" 
		when '' then 'BCS'
		ELSE IFNULL(FT."IdBolsa",'BCS')										
	END;