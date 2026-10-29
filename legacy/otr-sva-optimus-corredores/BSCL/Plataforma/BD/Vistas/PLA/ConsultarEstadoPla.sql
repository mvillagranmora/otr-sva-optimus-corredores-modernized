SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstadoPla"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstadoPla"  
AS
SELECT EPL."IdEstadoPla",
       EST."IdEstado",
       EST."Nombre" AS "NombreEstado",
       EPL."NombreEntidad",
       EPL."Descripcion"
FROM "BSCL.Plataforma.BD::PLA.EstadoPla" EPL
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" EST ON (EPL."IdEstado" = EST."IdEstado");

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstadoPla" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
