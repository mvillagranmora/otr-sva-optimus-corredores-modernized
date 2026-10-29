SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Facturacion.BD.Vistas::ConsultarConfiguracionFacturacion"; 
 
CREATE VIEW "BSCL.Facturacion.BD.Vistas::ConsultarConfiguracionFacturacion" As 
SELECT "ConfiguracionFacturacion_$0"."IdEmpresa", 
       "ConfiguracionFacturacion_$0"."CodigoParametro", 
       "ConfiguracionFacturacion_$0"."Descripcion", 
       "ConfiguracionFacturacion_$0"."Valor" 
FROM "BSCL.Facturacion.BD::FAC.ConfiguracionFacturacion" AS "ConfiguracionFacturacion_$0";

GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::ConsultarConfiguracionFacturacion" TO BCS_DESARROLLADOR WITH GRANT OPTION; 
