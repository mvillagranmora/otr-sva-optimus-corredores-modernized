/* Obtiene los documentos de las anotaciones */
SELECT 
A."IdAnotacion",
A."IdCliente",
A."FechaAnotacion", 
A."Titulo", 
A."Detalle", 
A."FechaIngreso", 
A."FechaCreacion", 
A."FechaModificacion", 
A."IdUsuarioCreador", 
A."Borrado",
A."Archivo",
A."NombreArchivo", 
CASE COALESCE(A."NombreArchivo",'') WHEN '' THEN 'N' ELSE 'S' END "ExisteArchivo", 
A."ValidoDesde", 
A."ValidoHasta" 
FROM "BSCL.SocioNegocio.BD::CLI.Anotacion" A
WHERE A."IdAnotacion" = ?;