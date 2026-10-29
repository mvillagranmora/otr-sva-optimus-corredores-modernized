SET SCHEMA "BSCL";

DROP VIEW "BSCL.Plataforma.BD.Vistas::TAS.ConsultarFuenteVectorPrecio";

CREATE VIEW "BSCL.Plataforma.BD.Vistas::TAS.ConsultarFuenteVectorPrecio" AS
SELECT 
"Codigo",
"Descripcion", 
"IdEstado" 
FROM "BSCL.Plataforma.BD::TAS.Fuente"
WHERE "NombreEntidad" = 'CRITERIO';

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::TAS.ConsultarFuenteVectorPrecio" TO BCS_DESARROLLADOR WITH GRANT OPTION;
