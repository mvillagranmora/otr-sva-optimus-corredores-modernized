SET SCHEMA "BSCL";
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstadoMoneda";
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstadoMoneda"
AS
SELECT 
       EST."IdEstado",
       EST."Nombre" AS "NombreEstado",
       EPL."NombreEntidad",
       EPL."Descripcion"
FROM "BSCL.Plataforma.BD::PLA.EstadoPla" EPL
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" EST ON (EPL."IdEstado" = EST."IdEstado")
WHERE EPL."NombreEntidad" = 'MONEDA';

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstadoMoneda" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 