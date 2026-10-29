SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::ACC.ConsultarEstadosUsuario"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::ACC.ConsultarEstadosUsuario"  
AS
SELECT
       "IdEstadoPla",
       "IdEstado",
       "NombreEntidad",
       "Descripcion"
       
FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstadoPla"
WHERE "NombreEntidad" = 'USUARIO';

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::ACC.ConsultarEstadosUsuario" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 