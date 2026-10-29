SELECT
	CP."IdPersona", 
	SOC."IdSocioDeNegocio", 
	CP."DocumentoIdentidad",
	CP."TipoDocumentoIdentidad",
	SOC."IdContraparte",
	C."IdCorredora",
	C."CodigoInscripcionRegulador",
	C."NombreCorto",
	CP."RazonSocial"
FROM BSCL."BSCL.Plataforma.BD::INS.Corredora" C 
LEFT JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" SOC ON SOC."IdPersona" = C."IdPersona" 
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" CP  ON SOC."IdPersona" = CP."IdPersona"
WHERE C."Borrado" = 'N' AND C."IdEstado" = 'ACT';