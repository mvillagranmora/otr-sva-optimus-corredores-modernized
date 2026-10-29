SELECT  
	A."IdAnotacion",
	A."IdPortafolio",
	MOT."IdAtributoAnotacion" as "IdMotivo",
	MOT."NombreAtributoAnotacion" as "NombreMotivo",
	REL."IdAtributoAnotacion" as "IdRelevancia",
	REL."NombreAtributoAnotacion" as "NombreRelevancia",
	AMB."IdAtributoAnotacion" as "IdAmbito",
	AMB."NombreAtributoAnotacion" as "NombreAmbito",
	TO_NVARCHAR(A."FechaAnotacion",'YYYY-MM-DD') AS "FechaAnotacion",
	A."Titulo",
	A."Detalle",
	TO_NVARCHAR(A."FechaIngreso",'YYYY-MM-DD') AS "FechaIngreso",
	TO_NVARCHAR(A."FechaCreacion",'YYYY-MM-DD') AS "FechaCreacion",
	TO_NVARCHAR(A."FechaModificacion",'YYYY-MM-DD') AS "FechaModificacion",
	A."IdUsuarioCreador",
	A."Borrado",
	A."NombreArchivo",
	CASE COALESCE(A."NombreArchivo",'') WHEN '' THEN 'N' ELSE 'S' END "ExisteArchivo",
	TO_NVARCHAR(A."ValidoDesde",'YYYY-MM-DD') AS "ValidoDesde",
	TO_NVARCHAR(A."ValidoHasta",'YYYY-MM-DD') AS "ValidoHasta",
	'OPTIMUS' AS "Origen"
FROM "BSCL.Portafolio.BD::POR.Anotacion" AS A
LEFT JOIN "BSCL.Portafolio.BD::POR.AtributoAnotacion" MOT ON A."IdMotivoAnotacion" = MOT."IdAtributoAnotacion"
LEFT JOIN "BSCL.Portafolio.BD::POR.AtributoAnotacion" REL ON A."IdRelevanciaAnotacion" = REL."IdAtributoAnotacion"
LEFT JOIN "BSCL.Portafolio.BD::POR.AtributoAnotacion" AMB ON A."IdAmbitoAnotacion" = AMB."IdAtributoAnotacion"
WHERE A."IdPortafolio" = :IdPortafolio AND A."Borrado" = 'N';