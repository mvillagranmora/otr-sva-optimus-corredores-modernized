SET SCHEMA "BSCL";
GO 
DROP VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarIngresoEgresoFormaDePago";
GO 
CREATE VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarIngresoEgresoFormaDePago" AS

SELECT
	 IE."FechaLiquidacion"
	,IE."IdEstado" As "IdEstadoIngresoEgreso"
	,FP."IdIngresoEgresoFormaDePago"
	,FP."IdIngresoEgreso"
	,FP."IdTipoMovimiento"
	,FP."IdMedioDePago"
	,FP."NombreMedioDePago"
	,FP."IdModalidadDePago"
	,FP."NombreModalidadDePago"	
	,FP."IdMoneda"
	,FP."Monto"	
	,(CASE WHEN FP."IdTipoMovimiento" = 'MOVEGR' THEN FP."Monto" ELSE 0 END) AS "MontoEgreso"
	,(CASE WHEN FP."IdTipoMovimiento" = 'MOVING' THEN FP."Monto" ELSE 0 END) AS "MontoIngreso"
	,(CASE WHEN FP."IdTipoMovimiento" = 'MOVING' THEN FP."Monto" ELSE -1*FP."Monto" END) AS "MontoConSigno"		
	,FP."FechaIngresoRegistro"
	,FP."IdEstado"
    ,EST."Nombre" AS "NombreEstado"
    
	-- FP Corredora
	,BancoCorredora."NombreBanco" AS "NombreBancoCorredora"
	,FP."IdCuentaBancariaCorredora"
	,FP."IdTipoCuentaBancariaCorredora"
	,FP."NombreTipoCuentaBancariaCorredora"
	,FP."NombreCuentaBancariaCorredora"
	,FP."NumeroCuentaBancariaCorredora"
	,FP."IdTarjetaBancariaCorredora"
	,FP."IdTipoTarjetaBancariaCorredora"
	,FP."NombreTipoTarjetaBancariaCorredora"
	,FP."NumeroTarjetaBancariaCorredora"
	,FP."IdChequera"
	,FP."NumeroDocumentoCorredora"
	,CHE."Serie"   AS "NumeroSerieCorredora"
	,FP."FechaDocumentoPagoCorredora"
	
	-- FP Socio de Negocio
	,FP."IdCuentaBancariaSocio"
	,FP."IdBancoSocio"
	,BancoSocio."NombreBanco" AS "NombreBancoSocio"
	,FP."IdTipoCuentaBancariaSocio"
	,FP."NombreTipoCuentaBancariaSocio"
	,FP."NombreCuentaBancariaSocio"
	,FP."NumeroCuentaBancariaSocio"
	,FP."IdTarjetaBancariaSocio"
	,FP."IdTipoTarjetaBancariaSocio"
	,FP."NombreTipoTarjetaBancariaSocio"
	,FP."NumeroTarjetaBancariaSocio"
	,FP."FechaVctoTarjetaBancariaSocio"
	,FP."NumeroSerieSocio"
	,FP."NumeroDocumentoSocio"
	,FP."FechaDocumentoPagoSocio"
	 
FROM "BSCL.Tesoreria.BD::TES.IngresoEgresoFormaDePago" AS FP
INNER JOIN "BSCL.Tesoreria.BD::TES.IngresoEgreso" AS IE ON (IE."IdIngresoEgreso" = FP."IdIngresoEgreso")
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS EST ON (FP."IdEstado" = EST."IdEstado")
LEFT OUTER JOIN "BSCL.Tesoreria.BD::TES.CuentaBanco" AS CtaBcoCorredora ON (CtaBcoCorredora."IdCuentaBanco" = FP."IdCuentaBancariaCorredora")
LEFT OUTER JOIN "BSCL.Plataforma.BD::BAN.Banco" AS BancoCorredora ON (BancoCorredora."IdBanco" = CtaBcoCorredora."IdBanco")
LEFT OUTER JOIN "BSCL.Plataforma.BD::BAN.Banco" AS BancoSocio ON (BancoSocio."IdBanco" = FP."IdBancoSocio")
LEFT OUTER JOIN "BSCL.Tesoreria.BD::TES.Chequera" AS CHE ON (CHE."IdChequera" = FP."IdChequera")
WITH READ ONLY;
GO 
GRANT SELECT ON "BSCL.Tesoreria.BD.Vistas::ConsultarIngresoEgresoFormaDePago" TO  BCS_DESARROLLADOR WITH GRANT OPTION;

