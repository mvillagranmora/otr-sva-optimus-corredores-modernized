SELECT 
	"IdSolicitudParametros",
	"TipoParametro",
	"Valor"
FROM "BSCL.Normativa.BD::NOR.SolicitudParametros" 
WHERE "TipoParametro" = 'CLAINS' AND "IdSolicitudCircular" = :IdSolicitudCircular;