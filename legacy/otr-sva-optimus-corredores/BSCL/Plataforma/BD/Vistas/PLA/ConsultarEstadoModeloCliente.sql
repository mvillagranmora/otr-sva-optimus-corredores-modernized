SET SCHEMA "BSCL"; 
 GO
DROP VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstadoModeloCliente"; 
 GO
CREATE VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstadoModeloCliente"  
AS
SELECT 
      "IdEstado",
      "Nombre"
From "BSCL.Plataforma.BD::PLA.Estado"
WHERE "IdEstado" = 'ACT' OR "IdEstado" = 'INA';
GO
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstadoModeloCliente" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 