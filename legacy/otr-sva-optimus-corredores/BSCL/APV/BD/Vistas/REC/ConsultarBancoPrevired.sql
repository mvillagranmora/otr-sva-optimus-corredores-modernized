SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.APV.BD.Vistas::REC.ConsultarBancoPrevired"; 
 
CREATE VIEW "BSCL.APV.BD.Vistas::REC.ConsultarBancoPrevired" AS 
SELECT  
BANCOPREVIRED."CodigoBancoPrevired"
,BANCO."IdBanco"
,BANCO."NombreBanco"
,BANCO."CodigoBanco"
,TIPOBANCO."Nombre" AS "NombreTipoBanco"
,CUENTABANCO."IdCuentaBanco"
,CUENTABANCO."NombreCuentaBancaria"
,CUENTABANCO."NumeroCuentaBancaria"
,CUENTABANCO."IdMoneda"
,CUENTABANCO."IdTipoCuentaBancaria"
,TIPOCUENTABANCARIA."NombreTipoCuentaBancaria"
FROM "BSCL.APV.BD::REC.CodigoBancoPrevired" AS BANCOPREVIRED
LEFT JOIN "BSCL.Tesoreria.BD::TES.CuentaBanco" AS CUENTABANCO ON BANCOPREVIRED."IdCuentaBanco" = CUENTABANCO."IdCuentaBanco"
LEFT JOIN "BSCL.Plataforma.BD::BAN.Banco" AS BANCO ON BANCO."IdBanco" = CUENTABANCO."IdBanco"
LEFT JOIN "BSCL.Plataforma.BD::BAN.TipoBanco" AS TIPOBANCO ON (TIPOBANCO."IdTipoBanco" = BANCO."IdTipoBanco")
LEFT JOIN "BSCL.Plataforma.BD::BAN.TipoCuentaBancaria" AS TIPOCUENTABANCARIA ON TIPOCUENTABANCARIA."IdTipoCuentaBancaria" = CUENTABANCO."IdTipoCuentaBancaria";
		
GRANT SELECT ON "BSCL.APV.BD.Vistas::REC.ConsultarBancoPrevired" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 