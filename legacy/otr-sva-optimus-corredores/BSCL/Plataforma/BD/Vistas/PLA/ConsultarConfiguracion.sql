SET SCHEMA "BSCL";

DROP VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConfiguracion";
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConfiguracion"  
AS

SELECT "IdEmpresa", "CodigoParametro", "Descripcion", "Valor"
FROM "BSCL.Plataforma.BD::PLA.Configuracion";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConfiguracion" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 


