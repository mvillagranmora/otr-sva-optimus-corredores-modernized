SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::NOR.ConsultarConfiguracion";
 
CREATE VIEW "BSCL.Normativa.BD.Vistas::NOR.ConsultarConfiguracion" 
AS

SELECT "IdEmpresa", "CodigoParametro", "Descripcion", "Valor", "IdNormativa"
FROM "BSCL.Normativa.BD::NOR.Configuracion";

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::NOR.ConsultarConfiguracion" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 


