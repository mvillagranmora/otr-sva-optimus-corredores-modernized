SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::PND.ConsultarEstadosAsuntosPendientes"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::PND.ConsultarEstadosAsuntosPendientes"  
AS
SELECT
       "IdEstadoPla",
       "IdEstado",
       "NombreEntidad",
       "Descripcion"
       
FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstadoPla"
WHERE "NombreEntidad" = 'ASUNTOPEND';

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PND.ConsultarEstadosAsuntosPendientes" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 