SELECT	BO."IdBolsa"														AS  "IdBolsa",
		IFNULL(CL."IdSocioDeNegocio",0)										AS "IdSocioNegocio",					
		'CNT'																AS "TipoSocioNegocio",					
		CAST(0 AS BigInt)													AS "IdPortafolio",						
		'R'																	AS "IdTipoDocumentoIdentidad",			
		IFNULL(BO."DocumentoIdentidad",'')									AS "DocumentoIdentidad",				
		IFNULL(UPPER(BO."Nombre"),'')										AS "NombreCliente",					
		CASE IFNULL(CL."DocumentoIdentidadDefecto",'')
			WHEN '' THEN 'PEN'
			ELSE 'PRO'
		END																	AS "IdEstado"
FROM "BSCL.Plataforma.BD::INS.Bolsa"							AS BO
/*	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CNT.ConsultarContraparte" 	AS CL
		ON (BO."DocumentoIdentidad" = CL."DocumentoIdentidad")*/
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas.UI::ConsultarSocioDeNegocio" AS CL
		ON (BO."DocumentoIdentidad" = CL."DocumentoIdentidadDefecto")
	   AND "IdEmpresa" = 		:IdEmpresa
WHERE	BO."IdBolsa"			in (:IdBolsa);