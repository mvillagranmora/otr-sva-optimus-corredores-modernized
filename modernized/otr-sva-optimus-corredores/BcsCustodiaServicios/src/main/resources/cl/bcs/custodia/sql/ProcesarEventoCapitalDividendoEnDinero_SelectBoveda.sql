SELECT BO."IdBoveda"													AS "IdBoveda",
	IFNULL(SN."IdSocioDeNegocio",0)										AS "IdSocioNegocio",					
	'CNT'																AS "TipoSocioNegocio",					
	IFNULL(CC."IdPortafolioDefecto",0)									AS "IdPortafolio",
--	'R'																	AS "IdTipoDocumentoIdentidad",
	BO."TipoDocumentoIdentidad"											AS "IdTipoDocumentoIdentidad",
	BO."DocumentoIdentidad"												AS "DocumentoIdentidad",				
--	IFNULL(PE."RazonSocial",'')											AS "NombreCliente"
	IFNULL(BO."Nombre",'')											AS "NombreCliente",
	CASE IFNULL(BO."DocumentoIdentidad",'')
		WHEN '' THEN 'PEN'
		ELSE 'PRO'
	END																	AS "IdEstado"
FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarBoveda"            AS BO
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio"   AS SN
	ON (BO."IdPersona" = SN."IdPersona")
INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona"							AS PE
	ON (BO."IdPersona" = PE."IdPersona")
LEFT JOIN  "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente"			AS CC
	ON SN."IdCliente" = CC."IdCliente"
   AND CC."IdEstado" = 'ACT'
WHERE   BO."IdBoveda" 			IN (:IdBoveda) 
	and	SN."IdEmpresa" 			= :IdEmpresa  	
GROUP BY 
	BO."IdBoveda",
	SN."IdSocioDeNegocio",
	IFNULL(CC."IdPortafolioDefecto",0),
	BO."TipoDocumentoIdentidad",
	BO."DocumentoIdentidad", 
	--IFNULL(PE."RazonSocial",'')
	IFNULL(BO."Nombre",'')