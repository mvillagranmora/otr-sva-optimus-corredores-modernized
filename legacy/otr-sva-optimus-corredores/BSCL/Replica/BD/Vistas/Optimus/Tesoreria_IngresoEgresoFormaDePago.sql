SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Replica.BD.Vistas::Optimus.Tesoreria_IngresoEgresoFormaDePago"; 


CREATE VIEW "BSCL.Replica.BD.Vistas::Optimus.Tesoreria_IngresoEgresoFormaDePago"
AS

SELECT 
IEFDP."IdIngresoEgresoFormaDePago", 
IEFDP."IdIngresoEgreso", 
IEFDP."IdTipoMovimiento", 
IEFDP."IdMedioDePago", 
IEFDP."NombreMedioDePago", 
IEFDP."NombreModalidadDePago", 
IEFDP."IdCuentaBancariaCorredora", 
IEFDP."IdTipoCuentaBancariaCorredora", 
IEFDP."NombreTipoCuentaBancariaCorredora", 
IEFDP."NombreCuentaBancariaCorredora", 
"NumeroCuentaBancariaCorredora", 
IEFDP."IdTarjetaBancariaCorredora", 
"IdTipoTarjetaBancariaCorredora", 
"NombreTipoTarjetaBancariaCorredora", 
"NumeroTarjetaBancariaCorredora", 
"IdChequera", 
"NumeroDocumentoCorredora", 
"FechaDocumentoPagoCorredora", 
"IdCuentaBancariaSocio", 
"IdBancoSocio", 
"IdTipoCuentaBancariaSocio", 
"NombreTipoCuentaBancariaSocio", 
"NombreCuentaBancariaSocio", 
"NumeroCuentaBancariaSocio", 
"IdTarjetaBancariaSocio", 
"IdTipoTarjetaBancariaSocio", 
"NombreTipoTarjetaBancariaSocio", 
"NumeroTarjetaBancariaSocio", 
"FechaVctoTarjetaBancariaSocio", 
"NumeroSerieSocio", 
"NumeroDocumentoSocio", 
"FechaDocumentoPagoSocio", 
IEFDP."IdMoneda", 
IEFDP."Monto", 
IEFDP."FechaIngresoRegistro", 
IEFDP."IdEstado", 
"IdModalidadDePago",
IE."FechaMovimiento"
FROM "BSCL.Tesoreria.BD::TES.IngresoEgresoFormaDePago" IEFDP
LEFT JOIN "BSCL.Tesoreria.BD::TES.IngresoEgreso" IE ON IE."IdIngresoEgreso" = IEFDP."IdIngresoEgreso";

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Optimus.Tesoreria_IngresoEgresoFormaDePago" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
