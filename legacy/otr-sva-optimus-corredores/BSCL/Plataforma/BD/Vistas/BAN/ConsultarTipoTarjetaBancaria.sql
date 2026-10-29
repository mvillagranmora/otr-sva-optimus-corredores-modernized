SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::BAN.ConsultarTipoTarjetaBancaria"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::BAN.ConsultarTipoTarjetaBancaria"  
AS
SELECT "TipoTarjetaBancaria_$0"."IdTipoTarjetaBancaria" , "TipoTarjetaBancaria_$0"."NombreTipoTarjetaBancaria" 
FROM "BSCL.Plataforma.BD::BAN.TipoTarjetaBancaria" AS "TipoTarjetaBancaria_$0";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::BAN.ConsultarTipoTarjetaBancaria" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 