SELECT 
"IdFuente",
"Codigo" 
FROM "BSCL.Plataforma.BD::TAS.Fuente"
WHERE "Codigo" = :Entidad
AND "NombreEntidad" = 'CRITERIO'
AND "IdEstado" = 'ACT';
