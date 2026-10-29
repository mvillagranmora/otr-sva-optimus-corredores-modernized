SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::ACC.ConsultarEstadosRol"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::ACC.ConsultarEstadosRol"  
AS
SELECT
       "IdEstadoPla",
       "IdEstado",
       "NombreEntidad",
       "Descripcion"
       
FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstadoPla"
WHERE "NombreEntidad" = 'ROL';

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::ACC.ConsultarEstadosRol" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 