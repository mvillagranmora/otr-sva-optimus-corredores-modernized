SELECT 
	"IdEmisor",
	"DocumentoIdentidad" AS "DocumentoIdentidadEmisor",
	"NombreCompleto" AS "NombreClienteEmisor"	
FROM "BSCL.Plataforma.BD::MER.Emisor" EMI
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER ON PER."IdPersona" = EMI."IdPersona"
WHERE EMI."IdEmisor" = ( 
	SELECT EMI."IdEmisor"
	FROM "BSCL.Plataforma.BD::INS.Instrumento" INS
	JOIN "BSCL.Plataforma.BD::MER.Emisor" EMI ON (INS."IdEmisor" = EMI."IdEmisor")
	WHERE INS."IdInstrumento" = :IdInstrumento
)