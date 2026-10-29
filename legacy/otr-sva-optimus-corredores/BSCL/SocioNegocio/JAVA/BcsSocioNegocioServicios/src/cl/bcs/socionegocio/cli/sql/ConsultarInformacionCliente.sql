SELECT 
    Per."IdPersona"															AS "IdPersona",
    Eje."IdPersona" 														AS "IdPersonaEjecutivo",
    Per."DocumentoIdentidad" 												AS "DocumentoIdentidadCliente", 
    Per."NombreCompleto" 													AS "NombreCliente", 
    Per."Tipo"         														AS "IdTipoPersona",
    TPer."Descripcion" 														AS "NombreTipoPersona", 
    CASE WHEN :p_IdTipoPersona = 'J' AND COALESCE(Repre."NombreCompleto",'') <> ''
    	THEN COALESCE(TO_VARCHAR(Repre."FechaNacimiento", 'DD-MM-YYYY'),'1900-01-01')
    	ELSE COALESCE(TO_VARCHAR(Per."FechaNacimiento", 'DD-MM-YYYY'),'1900-01-01')
    	END																	AS "FechaNacimiento",
	COALESCE(TCarg."NombreTipoCargoEmpleado",'')							AS "Cargo",
    Eje."NombreCompleto" 													AS "NombreEjecutivo", 
    DirElec."Direccion"														AS "DireccionElectronica",
    COALESCE(Repre."DocumentoIdentidad",'') 								AS "DocumentoIdentidadRepLegal",
	COALESCE(Repre."NombreCompleto",'') 									AS "NombreRepLegal",
    Cli."IdCliente"															AS "IdCliente",
    CarHis."TotalPatrimonio"												AS "Patrimonio"
FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS Per
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarTipoPersona" AS TPer
	ON TPer."IdTipoPersona" = Per."Tipo"
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS Cli 
	ON Cli."IdPersona" = PER."IdPersona"
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS Eje
	ON Eje."IdPersona" = Cli."IdPersonaEjecutivo"
INNER JOIN "BSCL.SocioNegocio.BD::PER.DireccionElectronica" AS DirElec
	ON DirElec."IdDireccionElectronica" = Per."IdDireccionElectronicaPrincipal"
LEFT JOIN "BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado" AS TCarg
	ON (TCarg."CodigoTipoCargoEmpleado" = Per."CodigoTipoCargoEmpleado")
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS Repre
	ON Per."IdPersonaRepresentante" = Repre."IdPersona"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DireccionElectronica" AS RepreDir
	ON RepreDir."IdDireccionElectronica" = Repre."IdDireccionElectronicaPrincipal"   
LEFT JOIN "BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado" AS TCargR
	ON (TCargR."CodigoTipoCargoEmpleado" = Repre."CodigoTipoCargoEmpleado")
LEFT JOIN "BSCL.Normativa.BD::CAR.CartolaHistorica" AS CarHis
	ON CarHis."IdCliente" = Cli."IdCliente" AND CarHis."Fecha" = :p_FechaSistema
WHERE 
	(Cli."IdPersonaEjecutivo"	= :p_IdPersonaEjecutivo	OR :p_IdPersonaEjecutivo = 0 ) AND  
	(Per."Tipo"					= :p_IdTipoPersona  	OR :p_IdTipoPersona 	 = '') 
ORDER BY Per."NombreCompleto" DESC;