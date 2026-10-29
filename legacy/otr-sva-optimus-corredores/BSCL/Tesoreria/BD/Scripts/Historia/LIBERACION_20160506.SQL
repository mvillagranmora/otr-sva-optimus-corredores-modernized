-- ########################################################################################
--
--  =========================================================
--  ESTE ARCHIVO CONTIEN LOS SCRIPT HISTORICOS DE LIBERACION
--  =========================================================
--  * Debe incluir los ALTER de Tablas
--  * Debe incluir nuevas tablas, indices, secuencuias, etc.
--  * Debe incluir insert de datos maestros necesarios para el funcionamiento 
--  NOTA: Este archivo no reemplaza los scripts de creación que deben ir en cada módulo.
-- 
-- ########################################################################################

SET SCHEMA BSCL;
--
--
-- LIBERACION SEMANA: 06-05-2016
--


--##################################################
--       	  <NROJAS> Nuevos campos por Saldo banco
--##################################################

ALTER TABLE "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" ADD ("FechaContable" TIMESTAMP NOT NULL DEFAULT '0001-01-01 00:00:00');
ALTER TABLE "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" ADD ("SaldoContable" DECIMAL(26,8) DEFAULT 0);
ALTER TABLE "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" ADD ("SaldoLiquidacion" DECIMAL(26,8) DEFAULT 0);

ALTER TABLE "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" drop ("FechaMovimiento");

DROP TYPE "BSCL.Tesoreria.BD.Tipos::TES.CartolaCuentaBanco" cascade;
CREATE TYPE "BSCL.Tesoreria.BD.Tipos::TES.CartolaCuentaBanco" AS TABLE  ( 
	 "IdComprobanteFormaDePago"				BIGINT
	,"IdComprobante"						BIGINT
	,"IdTipoMovimiento"						NVARCHAR(6)
	,"NombreTipoMovimiento"					NVARCHAR(100)
	,"IdConcepto"							NVARCHAR(6)
	,"NombreConcepto"						NVARCHAR(100)
	,"IdMedioDePago"						NVARCHAR(3)
	,"NombreMedioDePago"					NVARCHAR(100)
	,"IdModalidadPago"						NVARCHAR(6)
	,"NombreModalidadDePago"				NVARCHAR(100)
	,"IdMoneda"								NVARCHAR(3)
	,"NombreMoneda"							NVARCHAR(100)
	,"Monto"								DECIMAL(26,8)
	,"MontoIngreso"							DECIMAL(26,8)
	,"MontoEgreso"							DECIMAL(26,8)
	,"SaldoContable"						DECIMAL(26,8)
	,"SaldoLiquidacion"						DECIMAL(26,8)
	,"FechaSaldo"							DATE
	,"IdEstado"								NVARCHAR(3)
	,"NombreEstado"							NVARCHAR(100)
	
--INFO CORREDORA
	,"IdPersonaCorredora"					BIGINT
	,"NombreCorredora"						NVARCHAR(300)
	,"TipoPersonaCorredora"					NVARCHAR(3)
	,"DocumentoIdentidadCorredora"			NVARCHAR(40)
	,"IdCuentaBancariaCorredora"			BIGINT
	,"IdBancoCorredora"						BIGINT
	,"NombreBancoCorredora"					NVARCHAR(100)
	,"NombreTipoCuentaBancariaCorredora"	NVARCHAR(100)
	,"NombreCuentaBancariaCorredora"		NVARCHAR(300)
	,"NumeroCuentaBancariaCorredora"		NVARCHAR(100)
	,"IdChequera"							BIGINT
	,"NumeroSerieCorredora"					NVARCHAR(100)
	,"NumeroDocumentoCorredora"				BIGINT
	,"FechaDocumentoPagoCorredora"			TIMESTAMP
	
	
--INFO SOCIO DE NEGOCIO
	,"IdPersonaSocio"						BIGINT
	,"NombreSocio"							NVARCHAR(300)
	,"TipoPersonaSocio"						NVARCHAR(3)
	,"DocumentoIdentidadSocio"				NVARCHAR(40)
	,"IdPersonaRelacionadoSocio"			BIGINT
	,"IdPortafolioRelacionadoSocio"			BIGINT
	,"NombrePortafolioRelacionadoSocio"		NVARCHAR(300)					
	,"IdCuentaBancariaSocio"				BIGINT
	,"IdBancoSocio"							BIGINT
	,"NombreBancoSocio"						NVARCHAR(100)
	,"NombreTipoCuentaBancariaSocio"		NVARCHAR(100)
	,"NombreCuentaBancariaSocio"			NVARCHAR(100)
	,"NumeroCuentaBancariaSocio"			NVARCHAR(100)
	,"NumeroSerieSocio"						NVARCHAR(100)
	,"NumeroDocumentoSocio"					BIGINT
	,"FechaDocumentoPagoSocio"				TIMESTAMP
	
--INFO ADICIONAL
	,"FechaIngresoRegistro"					TIMESTAMP	
	,"FechaContable"						TIMESTAMP
	,"FechaLiquidacion"						TIMESTAMP
	,"FechaGiro"							TIMESTAMP
	,"IdComprobantePartida"					BIGINT
	
);

--##################################################
--       	  <JVILLAGRAN> Ajustes Tesorería
--##################################################

-- Nuevo Estado 'Anulado' para comprobantes
INSERT INTO "BSCL"."BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
SELECT MAX("IdEstadoTesoreria")+1,'ANU','TES.Comprobante' FROM "BSCL"."BSCL.Tesoreria.BD::TES.EstadoTesoreria";

