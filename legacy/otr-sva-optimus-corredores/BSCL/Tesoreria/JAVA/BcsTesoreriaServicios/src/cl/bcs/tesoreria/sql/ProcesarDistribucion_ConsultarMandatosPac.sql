SELECT DISTINCT

	 LMP."IdentServicio"											AS "IdentServicio"
	,PAC."IdMandatoPAC"												AS "IdMandatoPAC"
	,PAC."IdCliente"												AS "IdCliente"
	,PAC."IdPortafolio"												AS "IdPortafolio"
	,PAC."FolioCSC"													AS "FolioCSC"
	,PAC."FolioSAAPV"												AS "FolioSAAPV"
	,PAC."Estado" 													AS "EstadoPAC"
	,IFNULL(SAP."IdEstado",'')										AS "EstadoSAAPV"
	,IFNULL(ESS."Nombre", '')										AS "EstadoLargoSAAPV"
	,IFNULL(CSC."Estado", '')										AS "EstadoCSC"
	,IFNULL(ESC."Nombre", '')										AS "EstadoLargoCSC"
	,CLI."NombreCompleto"											AS "NombreCompleto"
	,CLI."DocumentoIdentidad"										AS "DocumentoIdentidad"
	,CLI."NombreEstado" 											AS "EstadoCliente"
	,POR."IdEstado" 												AS "EstadoPortafolio"
	,PAC."EstadoCargo" 												AS "EstadoCargo"
	,CASE WHEN (IFNULL(CSC."MontoPesos",0) = 0) 
		THEN IFNULL(CSC."MontoUF",0) 
		ELSE CSC."MontoPesos" 
	 END 															AS "MontoCSC"
	,CASE WHEN (IFNULL(POC."MontoPesos",0) = 0) 
		THEN IFNULL(POC."MontoUF",0) 
		ELSE POC."MontoPesos" 	
	 END 															AS "MontoSAPV"
	,CASE WHEN (CSC."MontoPesos" = 0) 			
		THEN 'UF' 					
		ELSE 'CLP'				
	 END 															AS "MonedaCSC"
	,CASE WHEN (POC."MontoPesos" = 0) 			
		THEN 'UF' 					
		ELSE 'CLP' 				
	 END 															AS "MonedaSAPV"
	
	FROM "BSCL.Tesoreria.BD::PAC.LogMandatoPAC" 					AS LMP
	LEFT JOIN "BSCL.Tesoreria.BD::PAC.MandatoPAC"   				AS PAC ON (LMP."IdentServicio" 		= PAC."IdentServicio")
	LEFT JOIN "BSCL.FondoMutuo.BD::CSC.ContratoSuscripcionCuotas"	AS CSC ON (CSC."FolioCSC" 			= PAC."FolioCSC"
																				AND "CSC"."Estado"	   NOT IN('ANU','RVC')
																				AND "CSC"."TipoPago" 		= 'PAC')	
	LEFT JOIN "BSCL.APV.BD::SCS.ContratoSAAPV" 				 		AS SAP ON (SAP."FolioContratoSAAPV" = CAST("PAC"."FolioSAAPV" AS VARCHAR)
																				AND "SAP"."IdEstado"	   NOT IN('ANU','RVC'))
	LEFT JOIN  "BSCL.Plataforma.BD::PLA.Estado"				AS ESC ON (ESC."IdEstado" 			= CSC."Estado")
	LEFT JOIN "BSCL.APV.BD::SCS.PortafolioContratoSAAPV" 			AS POC ON (POC."IdContratoSAAPV" 	= SAP."IdContratoSAAPV")
	LEFT JOIN "BSCL.APV.BD::APV.Estado"								AS ESS ON (SAP."IdEstado" 			= ESS."Codigo")
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" 	AS CLI ON (PAC."IdCliente" 			= CLI."IdCliente")
	LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" 					AS POR ON (POR."IdPortafolio" 		= PAC."IdPortafolio")

	WHERE 	PAC."EstadoCargo"			= 'ACT'
		AND LMP."EstadoProcesamiento" 	= 'PEN'
		AND LMP."PreRendicion"			= 'S';