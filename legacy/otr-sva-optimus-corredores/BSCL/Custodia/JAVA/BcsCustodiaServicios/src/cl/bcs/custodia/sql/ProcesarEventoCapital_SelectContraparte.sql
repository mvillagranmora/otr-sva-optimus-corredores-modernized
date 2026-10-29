
SELECT
		CNT."IdContraparte",
		CASE
		  WHEN :p_EntidadEmisorDestinoGtia = 'S' THEN COALESCE(MAX(SocNegEmisor."IdSocioDeNegocio"), 0)
		ELSE COALESCE(MAX(SocNeg."IdSocioDeNegocio"), 0)
		END                                             AS "IdSocioNegocio",
		'CLI'											AS "TipoSocioNegocio",		
		COALESCE(MAX(Portafolio."IdPortafolio"),0)      AS "IdPortafolio",
		COALESCE(MAX(DocInd."IdTipoDocumentoIdentidad"),'')  AS "IdTipoDocumentoIdentidad",
		CASE
		  WHEN :p_EntidadEmisorDestinoGtia = 'S' THEN COALESCE(Max(PersonaEmisor."DocumentoIdentidad"), '')
		ELSE COALESCE(Max(DocInd."DocumentoIdentidad"), '')--COALESCE(CNT."DocumentoIdentidad",'')
		END                                             AS "DocumentoIdentidad",		
		CASE
		  WHEN :p_EntidadEmisorDestinoGtia = 'S' THEN COALESCE(MAX(PersonaEmisor."RazonSocial"),'')
		ELSE COALESCE(MAX(Persona."RazonSocial"),'')--COALESCE(CNT."NombreCompleto",'')  				
		END                                            AS "NombreCliente",
		CASE COALESCE(SN."DocumentoIdentidadDefecto",'')
			WHEN '' THEN 'PEN'
			ELSE 'PRO'
		END												AS "IdEstado"
FROM "BSCL.SocioNegocio.BD.Vistas::CNT.ConsultarContraparte" AS CNT
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas.UI::ConsultarSocioDeNegocio" AS SN ON (CNT."DocumentoIdentidad" = SN."DocumentoIdentidadDefecto")
LEFT JOIN  "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente"	AS CC ON (SN."IdCliente" = CC."IdCliente" AND CC."IdEstado" = 'ACT')
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" AS Instru ON Instru."IdInstrumento" = :p_IdInstrumento 
LEFT JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" AS Emisor ON Emisor."IdEmisor" = Instru."IdEmisor"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS PersonaEmisor ON PersonaEmisor."IdPersona" = Emisor."IdPersona"
LEFT JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" SocNegEmisor ON PersonaEmisor."IdPersona" = SocNegEmisor."IdPersona"
LEFT JOIN "BSCL.SocioNegocio.BD::CNT.Contraparte" AS Contraparte ON Contraparte."IdContraparte" = CNT."IdContraparte"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS Persona ON Persona."IdPersona" = Contraparte."IdPersona"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DocInd ON DocInd."IdDocumentoIdentidad" = Persona."IdDocumentoIdentidad"
LEFT JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS SocNeg ON SocNeg."IdPersona" = Persona."IdPersona"
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdCliente" = SocNeg."IdCliente"

WHERE  CNT."IdContraparte"	IN (:IdContraparte)
AND    CNT."IdEmpresa" = 	:IdEmpresa

GROUP BY 
CNT."IdContraparte",
CNT."IdSocioDeNegocio",
CC."IdPortafolioDefecto",
CNT."DocumentoIdentidad",
Emisor."DocumentoIdentidad",
CNT."NombreCompleto",
Emisor."NombreCompleto",
SN."DocumentoIdentidadDefecto";
