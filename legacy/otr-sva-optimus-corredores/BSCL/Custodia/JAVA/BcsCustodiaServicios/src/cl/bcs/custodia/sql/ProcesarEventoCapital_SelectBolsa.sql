SELECT	BO."IdBolsa"														AS "IdBolsa",
		IFNULL(CL."IdSocioDeNegocio",0)										AS "IdSocioNegocio",					
		'CNT'																AS "TipoSocioNegocio",		
		COALESCE(CC."IdPortafolioDefecto",0)								AS "IdPortafolio",
		'R'																	AS "IdTipoDocumentoIdentidad",			
		IFNULL(BO."DocumentoIdentidad",'')									AS "DocumentoIdentidad",				
		IFNULL(UPPER(BO."Nombre"),'')										AS "NombreCliente",					
		CASE IFNULL(CL."DocumentoIdentidadDefecto",'')
			WHEN '' THEN 'PEN'
			ELSE 'PRO'
		END																	AS "IdEstado"
FROM "BSCL.Plataforma.BD::INS.Bolsa" AS BO
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas.UI::ConsultarSocioDeNegocio" AS CL
	ON (BO."DocumentoIdentidad" = CL."DocumentoIdentidadDefecto")
   AND "IdEmpresa" = 		:IdEmpresa
LEFT JOIN  "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente"			AS CC
	ON CL."IdCliente" = CC."IdCliente"
   AND CC."IdEstado" = 'ACT'
WHERE	BO."IdBolsa"			in (:IdBolsa)

GROUP BY 
BO."IdBolsa",
CL."IdSocioDeNegocio",
CC."IdPortafolioDefecto",
BO."DocumentoIdentidad",
BO."Nombre",
CL."DocumentoIdentidadDefecto";
