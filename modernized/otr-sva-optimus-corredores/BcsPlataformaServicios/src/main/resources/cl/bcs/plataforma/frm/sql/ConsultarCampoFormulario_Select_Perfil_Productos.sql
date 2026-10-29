SELECT
"IdPerfilProductos", 
"DescripcionPerfilProductos"
FROM "BSCL.Plataforma.BD.Vistas::COM.ConsultarPerfilProductos"
WHERE "IdPerfilProductos" in (:IdsPerfilProductos);