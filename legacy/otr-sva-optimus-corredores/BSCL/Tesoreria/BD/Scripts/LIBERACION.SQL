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
-- LIBERACION SEMANA: 20-06-2016
--

--####################################################################################
-- <NROJAS> Ajustes Tesorería
--####################################################################################


INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio"("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES(139,'BSCL.Tesoreria.BD.Servicios.CAJ::CrearCajaProducto',0,'P','');


INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (1046,'BSCL.Tesoreria.BD.Vistas::ConsultarCajaMonedaUsuario', 1,  'Q', '');


 
--####################################################################################
-- <JVILLAGRAN> Ajustes Tesorería
--####################################################################################

-- Ajuste en la ruta del servicio ModificarConfiguracionChequera
UPDATE "BSCL"."BSCL.Plataforma.BD::PLA.Servicio"
SET "RutaServicio" = 'BSCL.Tesoreria.BD.Servicios.CHE::ModificarConfiguracionChequera'
WHERE  "IdServicio" = 189;


UPDATE "BSCL"."BSCL.Plataforma.BD::PLA.Servicio"
SET "RutaServicio" = 'BSCL.Tesoreria.BD.Servicios.BAN::EliminarCuentaBancaria'
WHERE  "IdServicio" = 163;


INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio"("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES(1234,'BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa',0,'Q','Consulta datos de la empresa');


Update "BSCL"."BSCL.Plataforma.BD::ACC.Acceso" 
SET "IdElementoHtml" = 'SERVICIO_LIQUIDACION_SALDO' 
WHERE "TipoAcceso" = 'SERV'
AND "IdServicio" = '1003';


Update "BSCL"."BSCL.Plataforma.BD::ACC.Acceso" 
SET "IdElementoHtml" = 'LiquidarSaldo' 
WHERE "TipoAcceso" = 'MENU2'
AND "NombreAcceso" = 'UI_LIQUIDACION_DE_SALDOS'
AND "Padre" = 4;


INSERT INTO "BSCL"."BSCL.Tesoreria.BD::TES.ConfiguracionTesoreria"
VALUES (1,'UNIDADMEDIDAIMPRESIONCHEQUES','Unidad de medida para configuración de cheques para impresión','Cm');

-- Cambio estructura configuración de chequera(para aceptar decimales) 
drop table "BSCL.Tesoreria.BD::TES.ConfiguracionChequera" cascade;

CREATE COLUMN TABLE "BSCL.Tesoreria.BD::TES.ConfiguracionChequera" (
 "IdConfiguracionChequera"	BIGINT			NOT NULL,
 "NombreConfChequera" 		NVARCHAR(100)	NOT NULL,
 "AnchoCheque"				DECIMAL(10,4)	NOT NULL,
 "AltoCheque"				DECIMAL(10,4)	NOT NULL, 
 "SigloImpreso"            	NVARCHAR(1) 	NOT NULL DEFAULT 'N',  -- S / N 
 "MontoFila"				DECIMAL(10,4)	NOT NULL,
 "MontoColumna"				DECIMAL(10,4)   NOT NULL,
 "ProvinciaFila"			DECIMAL(10,4)	NOT NULL,
 "ProvinciaColumna"			DECIMAL(10,4)	NOT NULL,
 "MesFila"					DECIMAL(10,4)	NOT NULL,
 "MesColumna"				DECIMAL(10,4)	NOT NULL,
 "AnioFila"					DECIMAL(10,4)	NOT NULL,
 "AnioColumna"				DECIMAL(10,4)	NOT NULL,
 "ExtendidoAFila"			DECIMAL(10,4)	NOT NULL,
 "ExtendidoAColumna"		DECIMAL(10,4)	NOT NULL,
 "PrimeraCantidadFila"		DECIMAL(10,4)	NOT NULL,
 "PrimeraCantidadColumna"	DECIMAL(10,4)	NOT NULL,
 "SegundaCantidadFila"		DECIMAL(10,4)	NOT NULL,
 "SegundaCantidadColumna"	DECIMAL(10,4)	NOT NULL, 
PRIMARY KEY ("IdConfiguracionChequera") );



INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Configuracion"
VALUES(1,'CodigoCCLV','IdPersona de Contraparte Central(CCLV)',179630);



