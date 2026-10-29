SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarEventoCapitalClienteFormaDePago";
GO
CREATE VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarEventoCapitalClienteFormaDePago"
AS 
SELECT
	 Fp."IdEventoCapitalClienteFormaDePago"
	,Fp."IdEventoCapitalCliente"
	,Fp."IdPreferenciaPagoPortafolio"
	--,Fp."IdTipoMovimiento"	
	,(CASE Fp."IdTipoMovimiento"
		WHEN 'E' THEN 'EGRESO'
		WHEN 'I' THEN 'INGRESO'
		ELSE ''
	END) AS "IdTipoMovimiento"
	,Fp."IdModalidadPago"
	,Fp."IdMedioDePago"
	,Fp."NombreMedioDePago"
	,Fp."IdBanco"
	,Fp."NombreBanco"
	,Fp."IdTipoCuentaBancaria"
	,Fp."NombreTipoCuentaBancaria"
	,Fp."IdCuentaBancaria"
	,Fp."NombreCuentaBancaria"
	,Fp."NumeroCuentaBancaria"
	,Fp."IdTipoTarjetaBancaria"
	,Fp."NumeroTarjetaBancaria"
	,Fp."NumeroMedioDePago"
	,Fp."Sucursal"
	,Fp."FechaVctoDocumento"
	,Ec."IdMoneda"
	,Fp."Monto"
	,Fp."FechaHoraIngresoRegistro"
	,Fp."IdUsuarioIngreso"
	,Fp."IdEstado"
	,Ec."IdPortafolio"
	,Cp."CodigoPortafolio"
	,Ec."DocumentoIdentidad"
	,Ec."NombreCliente"
FROM "BSCL.Custodia.BD::CUS.EventoCapitalClienteFormaDePago" Fp
	INNER JOIN "BSCL.Custodia.BD::CUS.EventoCapitalCliente" Ec
		ON Fp."IdEventoCapitalCliente" = Ec."IdEventoCapitalCliente"
	INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" Cp
		ON Cp."IdPortafolio" = Ec."IdPortafolio"
	    AND Cp."IdEstado" = 'ACT';
GO
GRANT SELECT ON "BSCL.Custodia.BD.Vistas::CUS.ConsultarEventoCapitalClienteFormaDePago" TO BCS_DESARROLLADOR WITH GRANT OPTION; 