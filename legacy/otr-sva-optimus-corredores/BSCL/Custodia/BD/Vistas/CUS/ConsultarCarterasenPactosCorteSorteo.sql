SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarterasenPactosCorteSorteo";
CREATE VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarterasenPactosCorteSorteo"
AS 

	SELECT 
		CAR."FolioCartera"						AS "FolioCartera",
		CAR."Nemotecnico"						AS "Nemo",
		CAR."FechaOperacion"					AS "FechaOperacion",
		CAR."FechaVencimientoOperacion"			AS "FechaVencimientoOperacion",
		CAR."PlazoEnDiasOperacion"				AS "PlazoEnDiasOperacion",
		CAR."IdCliente"							AS "IdCliente",
		CAR."IdPortafolio"						AS "IdPortafolio",
		SUB."FolioCarteraRelacionada"			AS "FolioCarteraRelacionada",
		SUB."IdClasificacionTipoInstrumento"	AS "IdClasificacionTipoInstrumento",
		SUB."IdInstrumento"						AS "IdInstrumento",
		SUB."IdTipoInstrumento"					AS "IdTipoInstrumento",
		SUB."Nemotecnico"						AS "Nemotecnico",
		SUB."IdEmisor"							AS "IdEmisor",
		ECA."IdEventoCapital"					AS "IdEventoCapital",
		TP."Nombre"           					AS "TipoEventoCapital",
		ECA."Secuencia"							AS "Secuencia",
		IFNULL(CL."DocumentoIdentidad",'')		AS "DocumentoIdentidad",
		SOC."IdSocioDeNegocio"					AS "IdSocioNegocio",
		CL."Nombres"							AS "NombreCliente",
		ECA."FechaPago"							AS "FechaPago",
		CAR."INGFechaOperacion"					AS "INGFechaOperacion" 
	FROM
		"BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera"							AS CAR
	LEFT JOIN 	
		"BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSubyacenteBasica"	AS SUB
	ON (CAR."FolioCartera" = SUB."FolioCarteraRelacionada")
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente"				AS CL
	ON (CAR."IdCliente" = CL."IdCliente")
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" 		AS SOC
	ON (CL."IdCliente" = SOC."IdCliente")
	LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarEventoCapital"				AS ECA
	ON (SUB."IdInstrumento" = ECA."IdInstrumento")
		 		
	INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarTipoEventoCapital" 		AS TP
    ON ECA."IdTipoEventoCapital" = TP."IdTipoEventoCapital"    
    
	WHERE 
		CAR."IdClasificacionTipoInstrumento" 	= 	'RF' 
	AND CAR."IdTipoInstrumento" 				= 	'PAC'
	AND ECA."IdTipoEventoCapital"	IN ('CC','SL');
	


GRANT SELECT ON "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarterasenPactosCorteSorteo" TO BCS_DESARROLLADOR WITH GRANT OPTION;
