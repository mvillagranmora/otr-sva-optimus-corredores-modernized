SELECT 
	"IdAnotacion",
	"IdCliente",
	"IdAmbito",
	"NombreAmbito" AS "Ambito",
	"IdMotivo",
	"NombreMotivo" AS "Motivo",
	"IdRelevancia",
	"NombreRelevancia" AS "Relevancia",
	"Titulo",
	TO_NVARCHAR("FechaAnotacion",'YYYY-MM-DD') AS "FechaIngreso",
	TO_NVARCHAR("ValidoDesde",'YYYY-MM-DD') AS "ValidacionDesde",
	TO_NVARCHAR("ValidoHasta",'YYYY-MM-DD') AS "ValidacionHasta",
	"ExisteArchivo" AS "TieneAdjunto",
	COALESCE("NombreArchivo",'') AS "NombreArchivo"
FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarAnotacionesCliente"
WHERE "IdCliente" IN (:IdClientes)  AND "Borrado" = 'N' AND
(COALESCE(:Motivos,'') = '' OR "IdMotivo" IN (:Motivos)) AND
(COALESCE(:Relevancias,'') = '' OR "IdRelevancia" IN (:Relevancias)) AND
(COALESCE(:Ambitos,'') = '' OR "IdAmbito" IN (:Ambitos)) AND
(COALESCE(:TieneAdjunto,'') = '' OR "ExisteArchivo" IN (:TieneAdjunto))
ORDER BY "IdCliente" ASC;