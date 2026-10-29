SELECT 
"IdLibroOrdenes", 
"IdEmpresa", 
"LibroPorDefecto"
FROM "BSCL.Orden.BD.Vistas::ConsultarLibroOrdenes"
WHERE "IdEmpresa" = :p_IdEmpresa
AND "LibroPorDefecto" = 'S'
LIMIT 1;