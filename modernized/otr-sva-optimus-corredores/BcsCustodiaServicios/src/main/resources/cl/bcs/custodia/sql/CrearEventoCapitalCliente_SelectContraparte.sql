SELECT
		CNT."IdContraparte",
		COALESCE(CNT."IdSocioDeNegocio", 0)				AS "IdSocioNegocio",
		COALESCE(CC."IdCliente", 0)						AS "IdCliente",
		'CNT'											AS "TipoSocioNegocio",		
		COALESCE(CC."IdPortafolioDefecto",0)			AS "IdPortafolio",
		COALESCE(CNT."TipoDocumentoIdentidad",'R')		AS "IdTipoDocumentoIdentidad",
		COALESCE(CNT."DocumentoIdentidad",'')			AS "DocumentoIdentidad",		
		COALESCE(CNT."NombreCompleto",'')				AS "NombreCompleto",
		CASE COALESCE(SN."DocumentoIdentidadDefecto",'')
			WHEN '' THEN 'PEN'
			ELSE 'PRO'
		END												AS "IdEstado",
		COALESCE(CC."IdSucursalEjecutivo",0) 			AS "IdSucursalEjecutivo",
		COALESCE(CC."CodigoEmpleadoEjecutivo",'') 		AS "CodigoEmpleadoEjecutivo",
		COALESCE(CC."NombreEmpleadoEjecutivo",'') 		AS "NombreEmpleadoEjecutivo"
FROM "BSCL.SocioNegocio.BD.Vistas::CNT.ConsultarContraparte" AS CNT
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas.UI::ConsultarSocioDeNegocio" AS SN ON (CNT."DocumentoIdentidad" = SN."DocumentoIdentidadDefecto")
LEFT JOIN  "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente"	AS CC ON (SN."IdCliente" = CC."IdCliente" AND CC."IdEstado" = 'ACT')
WHERE  CNT."IdContraparte" IN  (:p_IdContraparte)
AND    CNT."IdEmpresa" = :p_IdEmpresa;