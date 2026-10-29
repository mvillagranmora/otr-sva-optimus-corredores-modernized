SELECT 
	"IdEmisor",
	"DocumentoIdentidad" as "DocumentoIdentidadEmisor",
	"NombreCompleto" as "NombreClienteEmisor"	
FROM "BSCL.Plataforma.BD::MER.Emisor" EMI
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER on PER."IdPersona" = EMI."IdPersona"
where EMI."IdEmisor" = (select EMI."IdEmisor" from "BSCL.Plataforma.BD::INS.Instrumento" INS
						JOIN "BSCL.Plataforma.BD::MER.Emisor"  EMI on (INS."IdEmisor" = EMI."IdEmisor")
						where INS."IdInstrumento" = :p_IdInstrumento)