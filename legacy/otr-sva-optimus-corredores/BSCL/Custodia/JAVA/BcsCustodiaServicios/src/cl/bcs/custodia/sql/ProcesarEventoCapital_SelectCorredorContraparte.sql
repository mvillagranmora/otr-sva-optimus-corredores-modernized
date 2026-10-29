WITH portafolio AS (
	SELECT "IdCliente", "IdPortafolio", ROW_NUMBER() OVER (PARTITION BY "IdCliente" ORDER BY "IdPortafolio" ASC) AS rn
	FROM "BSCL.Portafolio.BD::POR.Portafolio"
)SELECT
		CORRE."IdCorredora" 							AS "IdCorredorContraparte",
		COALESCE(SN."IdSocioDeNegocio", 0)				AS "IdSocioNegocio",
		'CNT'											AS "TipoSocioNegocio",		
		CAST(COALESCE(POR."IdPortafolio", 0) AS BIGINT)				AS "IdPortafolio", 
		'R'												AS "IdTipoDocumentoIdentidad",
		COALESCE(CORRE."DocumentoIdentidad",'')			AS "DocumentoIdentidad",
		COALESCE(CORRE."NombreCompleto",'')  			AS "NombreCorredor",
		CASE COALESCE(SN."DocumentoIdentidadDefecto",'')
			WHEN '' THEN 'PEN'
			ELSE 'PRO'
		END												AS "IdEstado"
FROM "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredora" AS CORRE
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas.UI::ConsultarSocioDeNegocio" AS SN ON (CORRE."DocumentoIdentidad" = SN."DocumentoIdentidadDefecto")
LEFT JOIN portafolio POR ON POR."IdCliente" = SN."IdCliente" AND POR."RN" = 1
WHERE  CORRE."IdCorredora"	IN (:IdCorredorContraparte)
AND    SN."IdEmpresa" = 	:IdEmpresa
GROUP BY 
CORRE."IdCorredora",
SN."IdSocioDeNegocio",
CORRE."DocumentoIdentidad",
CORRE."NombreCompleto",
SN."DocumentoIdentidadDefecto",
POR."IdPortafolio";
