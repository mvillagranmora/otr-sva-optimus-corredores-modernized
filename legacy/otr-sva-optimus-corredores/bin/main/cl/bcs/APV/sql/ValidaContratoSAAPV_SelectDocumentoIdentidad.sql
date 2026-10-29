SELECT
	PER."IdPersona",
	DOC."DocumentoIdentidad"
FROM
	"BSCL.SocioNegocio.BD::PER.Persona" PER
INNER JOIN 
	"BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DOC 
	ON DOC."IdPersona" = PER."IdPersona"
	AND DOC."Predeterminado" = 'S'
WHERE
	PER."Tipo" = :TipoPersona
	AND DOC."DocumentoIdentidad" = :DocumentoIdentidad;