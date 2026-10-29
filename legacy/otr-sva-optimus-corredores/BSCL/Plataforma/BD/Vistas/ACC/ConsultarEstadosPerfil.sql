SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::ACC.ConsultarEstadosPerfil"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::ACC.ConsultarEstadosPerfil"  
AS
SELECT
       "IdEstadoPla",
       "IdEstado",
       "NombreEntidad",
       "Descripcion"
       
FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstadoPla"
WHERE "NombreEntidad" = 'PERFIL';

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::ACC.ConsultarEstadosPerfil" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 