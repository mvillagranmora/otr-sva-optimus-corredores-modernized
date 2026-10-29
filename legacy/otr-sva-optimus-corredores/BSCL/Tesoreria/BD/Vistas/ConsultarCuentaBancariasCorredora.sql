SET SCHEMA "BSCL";
GO 
DROP VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarCuentaBancariasCorredora";
GO
CREATE VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarCuentaBancariasCorredora" AS

SELECT DISTINCT 
	  "Banco"."IdBanco"
	 ,"Banco"."NombreBanco"	 
 	 ,"Banco"."CodigoBanco"
	 ,"Banco"."IdPersona"
 	 ,"Banco"."IdTipoBanco"
 	 ,"Banco"."IdEstado"
 	 ,"Banco"."IdEmpresa"
 	 ,"CuentaBanco"."IdMoneda"
 	 ,"CuentaBanco"."IdCuentaBanco"
 	 ,"Banco"."NombreBanco" || ' - ' || "CuentaBanco"."NumeroCuentaBancaria" AS "BancoCuentaBancaria"
FROM "BSCL.Tesoreria.BD::TES.CuentaBanco" AS "CuentaBanco"
  LEFT JOIN "BSCL.Plataforma.BD::BAN.Banco" AS "Banco" ON ("Banco"."IdBanco" = "CuentaBanco"."IdBanco")
WHERE "CuentaBanco"."IdEstado" = 'ACT'

WITH READ ONLY;
GO
GRANT SELECT ON "BSCL.Tesoreria.BD.Vistas::ConsultarCuentaBancariasCorredora" TO  BCS_DESARROLLADOR WITH GRANT OPTION;

