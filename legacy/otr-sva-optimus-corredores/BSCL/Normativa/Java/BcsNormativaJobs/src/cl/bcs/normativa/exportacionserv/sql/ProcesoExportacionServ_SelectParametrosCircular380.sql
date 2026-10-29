SELECT 
	"IdSolicitudParametros",
	"TipoParametro",
	"Valor"
FROM "BSCL.Normativa.BD::NOR.SolicitudParametros" 
WHERE "TipoParametro" = 'CLI_OFICIAL' AND "IdSolicitudCircular" = :IdSolicitudCircular;