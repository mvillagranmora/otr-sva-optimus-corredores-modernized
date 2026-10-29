SET SCHEMA "BSCL";
GO 
DROP VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarBancoCuentaBancariaCorredora";
GO
CREATE VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarBancoCuentaBancariaCorredora" AS

SELECT DISTINCT 
	  "Banco"."IdBanco"
	 ,"Banco"."NombreBanco"	 
 	 ,"Banco"."CodigoBanco"
	 ,"Banco"."IdPersona"
 	 ,"Banco"."IdTipoBanco"
 	 ,"Banco"."IdEstado"
 	 ,"CuentaBanco"."IdMoneda"
FROM "BSCL.Tesoreria.BD::TES.CuentaBanco" AS "CuentaBanco"
LEFT JOIN "BSCL.Plataforma.BD::BAN.Banco" AS "Banco" ON ("Banco"."IdBanco" = "CuentaBanco"."IdBanco")

WITH READ ONLY;
GO
GRANT SELECT ON "BSCL.Tesoreria.BD.Vistas::ConsultarBancoCuentaBancariaCorredora" TO  BCS_DESARROLLADOR WITH GRANT OPTION;



