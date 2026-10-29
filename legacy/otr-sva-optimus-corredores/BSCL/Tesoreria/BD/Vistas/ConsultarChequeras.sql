SET SCHEMA "BSCL";

DROP VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarChequeras";

CREATE VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarChequeras" AS

SELECT
	 "Chequera"."IdChequera"
	 ,"Chequera"."FechaRetiro"
	 ,"Chequera"."Serie"
	 ,"Chequera"."CantidadCheques"
	 ,"Chequera"."Remanente"
	 ,"Chequera"."NumeroChequeDesde"
	 ,"Chequera"."NumeroChequeHasta"
	 ,"Chequera"."NumeroChequeActual"
	 ,"CuentaBanco"."IdCuentaBanco"
	 ,"CuentaBanco"."IdEmpresa"
	 ,"CuentaBanco"."IdBanco"
	 ,"Banco"."NombreBanco"	 
 	 ,"Banco"."CodigoBanco"
 	 ,"CuentaBanco"."NombreCuentaBancaria"
	 ,"CuentaBanco"."IdTipoCuentaBancaria"
	 ,"TipoCuentaBancaria"."NombreTipoCuentaBancaria"
	 ,"CuentaBanco"."IdMoneda"
	 ,"Moneda"."Nombre" AS "NombreMoneda"	 
	 ,"CuentaBanco"."NumeroCuentaBancaria"
	 ,"Chequera"."IdEstado"
	 ,"Estado"."Nombre" AS "NombreEstado"
	 ,"Chequera"."IdConfiguracionChequera"
	 ,"ConfigChequera"."NombreConfChequera"
	   
FROM "BSCL.Tesoreria.BD::TES.Chequera" AS "Chequera"
INNER JOIN "BSCL.Tesoreria.BD::TES.CuentaBanco" AS "CuentaBanco" ON ("Chequera"."IdCuentaBanco" = "CuentaBanco"."IdCuentaBanco")
LEFT OUTER JOIN "BSCL.Plataforma.BD::BAN.Banco" AS "Banco" ON ("Banco"."IdBanco" = "CuentaBanco"."IdBanco")
LEFT OUTER JOIN "BSCL.Plataforma.BD::BAN.TipoCuentaBancaria" AS "TipoCuentaBancaria" ON ("TipoCuentaBancaria"."IdTipoCuentaBancaria" = "CuentaBanco"."IdTipoCuentaBancaria")
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS "Moneda" ON ("Moneda"."IdMoneda" = "CuentaBanco"."IdMoneda")
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS "Estado" ON ("Estado"."IdEstado" = "Chequera"."IdEstado")
LEFT OUTER JOIN "BSCL.Tesoreria.BD::TES.ConfiguracionChequera" AS "ConfigChequera" ON ("ConfigChequera"."IdConfiguracionChequera" = "Chequera"."IdConfiguracionChequera")


WITH READ ONLY;

GRANT SELECT ON "BSCL.Tesoreria.BD.Vistas::ConsultarChequeras" TO  BCS_DESARROLLADOR WITH GRANT OPTION;