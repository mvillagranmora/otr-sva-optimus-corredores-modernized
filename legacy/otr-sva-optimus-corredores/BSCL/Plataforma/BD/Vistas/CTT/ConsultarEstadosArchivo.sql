SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::CTT.ConsultarEstadosArchivo"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::CTT.ConsultarEstadosArchivo"  
AS
SELECT
       "IdEstadoPla",
       "IdEstado",
       "NombreEntidad",
       "Descripcion"
       
FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstadoPla"
WHERE "NombreEntidad" = 'ARCHIVO';

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::CTT.ConsultarEstadosArchivo" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
