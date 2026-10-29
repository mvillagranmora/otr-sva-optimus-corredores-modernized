SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarConfiguracionTesoreria"; 
 
CREATE VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarConfiguracionTesoreria" As 

SELECT  "CONTES"."IdEmpresa" 
       ,"CONTES"."CodigoParametro"
       ,"CONTES"."Descripcion" 
       ,"CONTES"."Valor" 
FROM "BSCL.Tesoreria.BD::TES.ConfiguracionTesoreria" AS "CONTES";

GRANT SELECT ON "BSCL.Tesoreria.BD.Vistas::ConsultarConfiguracionTesoreria" TO BCS_DESARROLLADOR WITH GRANT OPTION; 