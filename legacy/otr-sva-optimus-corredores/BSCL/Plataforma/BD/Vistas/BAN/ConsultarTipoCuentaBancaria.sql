SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::BAN.ConsultarTipoCuentaBancaria"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::BAN.ConsultarTipoCuentaBancaria"  
AS
SELECT "TipoCuentaBancaria_$0"."IdTipoCuentaBancaria" , "TipoCuentaBancaria_$0"."NombreTipoCuentaBancaria" , "Pais_$1"."IdPais" , "Pais_$1"."CodigoPais" FROM  ( "BSCL.Plataforma.BD::BAN.TipoCuentaBancaria" AS "TipoCuentaBancaria_$0" LEFT OUTER JOIN "BSCL.Plataforma.BD::REG.Pais" AS "Pais_$1" ON  ( "Pais_$1"."IdPais" = "TipoCuentaBancaria_$0"."IdPais" )  );

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::BAN.ConsultarTipoCuentaBancaria" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 