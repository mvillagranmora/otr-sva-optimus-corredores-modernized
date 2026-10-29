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
-- LIBERACION SEMANA: 21-04-2016
--

--################################################
--       	  <NROJAS> AJUSTES TESORERIA
--################################################

drop table "BSCL.Tesoreria.BD::TES.ComprobanteDetalleIngresoEgreso" cascade;
drop table "BSCL.Tesoreria.BD::TES.ComprobanteDetalleOrigen" cascade;

CREATE COLUMN TABLE "BSCL.Tesoreria.BD::TES.ComprobanteDetalleOrigen" (
"IdComprobanteDetalleOrigen"	    BIGINT 			NOT NULL,
"IdComprobanteDetalle"				BIGINT 			NOT NULL,
"IdIngresoEgreso"					BIGINT,
"IdComprobante"					    BIGINT,
PRIMARY KEY ("IdComprobanteDetalleOrigen"));

   
alter table "BSCL.Tesoreria.BD::TES.ComprobanteDetalleOrigen"
   add constraint  FK_IngresoEgreso_ComprobanteDetalleOrigen foreign key ("IdIngresoEgreso")
	      references "BSCL.Tesoreria.BD::TES.IngresoEgreso" ("IdIngresoEgreso") on delete restrict on update restrict;

alter table "BSCL.Tesoreria.BD::TES.ComprobanteDetalleOrigen"
   add constraint  FK_Comprobante_ComprobanteDetalleOrigen foreign key ("IdComprobante")
	      references "BSCL.Tesoreria.BD::TES.Comprobante" ("IdComprobante") on delete restrict on update restrict;
	      

--################################################
--       	  Nuevos campos en comprobante para asociarlo a las partidas
--################################################
ALTER TABLE "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" ADD ("IdComprobantePartida" 	 BIGINT);
ALTER TABLE "BSCL.Tesoreria.BD::TES.Comprobante" ADD ("GeneradoDesdeMovimientos" NVARCHAR(1) NOT NULL default 'S');


--################################################
--       	  <JVILLAGRAN> AJUSTES TESORERIA
--################################################

-- Nuevos Parámetros de Configuración
INSERT INTO "BSCL"."BSCL.Tesoreria.BD::TES.ConfiguracionTesoreria" ("IdEmpresa","CodigoParametro","Descripcion","Valor")
VALUES (1,'LIQUIDARPORCOMPROBANTE','LIQUIDAR POR COMPROBANTE O POR FORMA DE PAGO','N');

INSERT INTO "BSCL"."BSCL.Tesoreria.BD::TES.ConfiguracionTesoreria" ("IdEmpresa","CodigoParametro","Descripcion","Valor")
VALUES (1,'LIQUIDARVERIFICARSALDOCAJA','AL LIQUIDAR VERIFICAR SALDO CAJA CTA. INVERSION','N');


-- Nuevo Estado 'Liquidación Parcial'
INSERT INTO "BSCL"."BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
SELECT MAX("IdEstadoTesoreria")+1,'LIP','TES.Comprobante' FROM "BSCL"."BSCL.Tesoreria.BD::TES.EstadoTesoreria";


-- Nuevas columnas para control de liquidación parcial
ALTER TABLE "BSCL"."BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" ADD ("IdUsuarioLiquidacion"   BIGINT          DEFAULT 0);
ALTER TABLE "BSCL"."BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" ADD ("FechaHoraLiquidacion"   TIMESTAMP       DEFAULT '0001-01-01');

ALTER TABLE "BSCL"."BSCL.Tesoreria.BD::TES.Comprobante" DROP ("IdUsuarioLiquida");
ALTER TABLE "BSCL"."BSCL.Tesoreria.BD::TES.Comprobante" DROP ("FechaHoraLiquida");


-- Nueva tabla para monedas habilitadas en tesoreria
CREATE COLUMN TABLE "BSCL.Tesoreria.BD::TES.MonedaTesoreria" (
	 "IdMonedaTesoreria" 			BIGINT ,
	 "IdMoneda" 					NVARCHAR(3) NOT NULL ,
	 "IdEstado" 					NVARCHAR(3),
	PRIMARY KEY ("IdMonedaTesoreria")
);

alter table "BSCL.Tesoreria.BD::TES.MonedaTesoreria"
   add constraint  FK_Moneda_MonedaTesoreria foreign key ("IdMoneda")
	      references "BSCL.Plataforma.BD::PLA.Moneda" ("IdMoneda") on delete restrict on update restrict;

alter table "BSCL.Tesoreria.BD::TES.MonedaTesoreria"
   add constraint  FK_Estado_MonedaTesoreria foreign key ("IdEstado")
	      references "BSCL.Plataforma.BD::PLA.Estado" ("IdEstado") on delete restrict on update restrict;


INSERT INTO "BSCL"."BSCL.Tesoreria.BD::TES.MonedaTesoreria"("IdMonedaTesoreria", "IdMoneda", "IdEstado")
VALUES(1,'CLP','ACT');

INSERT INTO "BSCL"."BSCL.Tesoreria.BD::TES.MonedaTesoreria"("IdMonedaTesoreria", "IdMoneda", "IdEstado")
VALUES(2,'USD','ACT');

INSERT INTO "BSCL"."BSCL.Tesoreria.BD::TES.MonedaTesoreria"("IdMonedaTesoreria", "IdMoneda", "IdEstado")
VALUES(3,'EUR','ACT');

--################################################
--       	  <NROJAS> Ajuste de datos 
--################################################

UPDATE "BSCL.Plataforma.BD::PLA.Empresa" SET "IdSocioDeNegocio" = 243144 WHERE "IdEmpresa" = 1;

UPDATE "BSCL.Portafolio.BD::POR.Portafolio" SET "OperaContraCuentaInversion" = 'S' 
WHERE "IdCliente" = 247214 AND "IdPortafolio" = 140276;

DELETE FROM "BSCL.Tesoreria.BD::TES.EstadoTesoreria" WHERE "NombreEntidad" = 'TES.ComprobanteFormaDePago' AND "IdEstado" = 'CRT';

ALTER TABLE "BSCL"."BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" DROP ("IdUsuarioCertificador");
ALTER TABLE "BSCL"."BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" DROP ("FechaCertificacion");

--################################################
--       	  <JPZ> Conceptos
--################################################
INSERT INTO "BSCL.Tesoreria.BD::TES.ConceptoTesoreria" ("IdConceptoTesoreria","IdConcepto","NombreEntidad","MultipleConcepto","Origen","Tipo")
SELECT (MAX("IdConceptoTesoreria")+1),'ABDIMT','TES.IngresoEgreso','N','CUS','IN' From "BSCL.Tesoreria.BD::TES.ConceptoTesoreria";


--################################################
--       	  <NROJAS> Type para el uso de la cartola cuenta banco 
--################################################
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
	,"Saldo"								DECIMAL(26,8)
	,"SaldoSimulado"						DECIMAL(26,8)
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
	,"FechaGiro"							TIMESTAMP
	,"IdComprobantePartida"					BIGINT
	,"FechaHoraLiquidacion"					TIMESTAMP
);