SET SCHEMA "BSCL";
DROP VIEW "BSCL.Tesoreria.BD.Vistas::IngresoEgresoFormaDePago";

CREATE VIEW "BSCL.Tesoreria.BD.Vistas::IngresoEgresoFormaDePago" AS

SELECT
	  cast(0 as BIGINT)														"NumeroReferencia"
	 ,cast(' ' as NVARCHAR(3))												"IdMedioDePago"
 	 ,cast(' ' as NVARCHAR(6))												"IdModalidadDePago"
	 ,cast(' ' as NVARCHAR(3))												"IdMoneda"
	 ,cast(0 as DECIMAL(26,8))												"Monto"
	 ,cast(0 as BIGINT)														"IdCuentaBancariaCorredora"
	 ,cast(0 as BIGINT)														"IdTarjetaBancariaCorredora"
	 ,cast(0 as BIGINT)														"IdChequera"
	 ,cast(0 as BIGINT)														"NumeroDocumentoCorredora"
	 ,cast('0001-01-01 00:00:00' as TIMESTAMP)								"FechaDocumentoPagoCorredora"
	 ,cast(0 as BIGINT)														"IdCuentaBancariaSocio"
	 ,cast(0 as BIGINT)														"IdBancoSocio"
	 ,cast(' ' as NVARCHAR(100))											"NombreCuentaBancariaSocio"
	 ,cast(' ' as NVARCHAR(100))											"NumeroCuentaBancariaSocio"
	 ,cast(' ' as NVARCHAR(4))												"IdTipoCuentaBancariaSocio"
	 ,cast(' ' as NVARCHAR(100))											"NombreTipoCuentaBancariaSocio"
	 ,cast(0 as BIGINT)														"IdTarjetaBancariaSocio"
	 ,cast(' ' as NVARCHAR(40))												"IdTipoTarjetaBancariaSocio"
	 ,cast(' ' as NVARCHAR(100))											"NombreTipoTarjetaBancariaSocio"
	 ,cast(' ' as NVARCHAR(100))											"NumeroTarjetaBancariaSocio"
	 ,cast(' ' as NVARCHAR(40))												"NumeroSerieSocio"
	 ,cast(0 as BIGINT)														"NumeroDocumentoSocio"
	 ,cast('0001-01-01 00:00:00' as TIMESTAMP)								"FechaDocumentoPagoSocio"
FROM dummy;	

GRANT SELECT ON "BSCL.Tesoreria.BD.Vistas::IngresoEgresoFormaDePago" TO  BCS_DESARROLLADOR WITH GRANT OPTION;

