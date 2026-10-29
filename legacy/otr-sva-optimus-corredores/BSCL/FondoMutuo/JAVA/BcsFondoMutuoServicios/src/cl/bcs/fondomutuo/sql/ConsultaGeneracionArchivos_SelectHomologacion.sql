SELECT 
	"IdHomologacion",
	"Entidad",
	"TipoValor",
	"Descripcion",
	"ValorOrigen",
	"ValorOrigen2",
	"ValorOrigen3",
	"ValorDestino"
FROM "BSCL.FondoMutuo.BD::FMM.Homologacion"
WHERE "Entidad" = 'FONDOMUTUO' AND "TipoValor" IN (:TipoValor);