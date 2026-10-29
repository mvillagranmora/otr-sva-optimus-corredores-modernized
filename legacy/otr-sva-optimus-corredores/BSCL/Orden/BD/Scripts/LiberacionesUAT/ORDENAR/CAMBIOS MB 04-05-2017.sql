SET SCHEMA "BSCL";

ALTER TABLE "BSCL.SocioNegocio.BD::EMP.Empleado" ADD ("RestringeOrdenCliente" NVARCHAR(1) DEFAULT 'S');

ALTER TABLE "BSCL.Orden.BD::ORD.LogCambioTransaccion" ADD (	"EsBilateral"					NVARCHAR(1) NOT NULL DEFAULT 'N');


ALTER TABLE "BSCL.Plataforma.BD::IND.TipoIndicador" ALTER ("IdTipoPersona" NVARCHAR(10) NOT NULL DEFAULT 'NJ')

update "BSCL.Plataforma.BD::IND.TipoIndicador" set "IdTipoPersona" = 'NJ' WHERE "IdTipoPersona" = 'A';


INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3564,'BSCL.Orden.BD.Vistas::ConsultarConfiguracionOrdenes',0,'Q','','','');

INSERT INTO "BSCL.Orden.BD::ORD.ConfiguracionOrdenes"("IdEmpresa","CodigoParametro","Descripcion","Valor")
SELECT 1,'CrearTransaccionDefectoFueraRueda','Opcion Fuera de Rueda encendida por defecto en crear transaccion','S' FROM dummy;

UPDATE "BSCL.Plataforma.BD::PLA.Estado" set "Nombre" = 'ASIGNACION PARCIAL' WHERE "IdEstado" = 'ASP';

alter table "BSCL.Orden.BD::ORD.Transaccion" drop ("IdMonedaLiquidacion");

alter table "BSCL"."BSCL.Orden.BD::ORD.DebugCrearTransacciones" drop ("IdMonedaLiquidacion");

drop procedure "BSCL.Orden.BD.Servicios::CrearTransaccionesMarketDataRV";
commit



CREATE COLUMN TABLE "BSCL.Plataforma.BD::COM_2.DefinicionCobroInstrumento"(
	"IdEmpresa"				BIGINT,
	"CodigoDefinicionCobro"	NVARCHAR(40),
	"IdInstrumento"			BIGINT,
	PRIMARY KEY("IdEmpresa","CodigoDefinicionCobro","IdInstrumento")
);

CREATE COLUMN TABLE "BSCL.Plataforma.BD::COM_2.DefinicionCobroTipoCliente"(
	"IdEmpresa"				BIGINT,
	"CodigoDefinicionCobro"	NVARCHAR(40),
	"IdTipoCliente"			NVARCHAR(40),
	PRIMARY KEY("IdEmpresa","CodigoDefinicionCobro","IdTipoCliente")
);

CREATE COLUMN TABLE "BSCL.Plataforma.BD::COM_2.DefinicionCobroPortafolio"(
	"IdEmpresa"				BIGINT,
	"CodigoDefinicionCobro"	NVARCHAR(40),
	"IdPortafolio"			BIGINT,
	PRIMARY KEY("IdEmpresa","CodigoDefinicionCobro","IdPortafolio")
);

CREATE COLUMN TABLE "BSCL.Plataforma.BD::COM_2.DefinicionCobroCorredoraCorresponsal"(
	"IdEmpresa"					BIGINT,
	"CodigoDefinicionCobro"		NVARCHAR(40),
	"IdCorredoraCorresponsal"	BIGINT,
	PRIMARY KEY("IdEmpresa","CodigoDefinicionCobro","IdCorredoraCorresponsal")
);


INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3556,'BSCL.Plataforma.BD.Vistas::COM_2.ConsultarDefinicionCobroEmisor',0,'Q','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3557,'BSCL.Plataforma.BD.Vistas::COM_2.ConsultarDefinicionCobroInstrumento',0,'Q','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3558,'BSCL.Plataforma.BD.Vistas::COM_2.ConsultarDefinicionCobroTipoCliente',0,'Q','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3559,'BSCL.Plataforma.BD.Vistas::COM_2.ConsultarDefinicionCobroPortafolio',0,'Q','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3560,'BSCL.Plataforma.BD.Vistas::COM_2.ConsultarDefinicionCobroCorredoraCorresponsal',0,'Q','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3561,'BSCL.Plataforma.BD.Vistas::COM_2.ConsultarDefinicionCobroRango',0,'Q','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(20014,'PLUGIN_PLA.jar@cl.bcs.plataforma.com.pe.ModificarDefinicionCobro',0,'J','','','');


CREATE TABLE "BSCL.Plataforma.BD::PLA.BancoCustodio"(
	"IdPersona"						BIGINT,
	"Codigo"						NVARCHAR(40),
	"IdEstado"						NVARCHAR(3),
	"FechaCreacion"					SECONDDATE,
	"FechaUltimaModificacion"		SECONDDATE,
	"IdUsuarioCreador"				BIGINT,
	"IdUsuarioUltimaModificacion"	BIGINT,
	"Borrado" 						NVARCHAR(1) NOT NULL DEFAULT 'N',
	PRIMARY KEY("IdPersona")
);

ALTER TABLE "BSCL.Plataforma.BD::PLA.BancoCustodio" ADD("EsBancoLiquidador" NVARCHAR(1) NOT NULL DEFAULT 'N');

CREATE TABLE "BSCL.Plataforma.BD::PLA.BrokerExtranjero"(
	"IdPersona"	BIGINT,
	"Codigo"	NVARCHAR(40),
	"IdEstado"	NVARCHAR(3),
	"FechaCreacion"					SECONDDATE,
	"FechaUltimaModificacion"		SECONDDATE,
	"IdUsuarioCreador"				BIGINT,
	"IdUsuarioUltimaModificacion"	BIGINT,
	"Borrado" 						NVARCHAR(1) NOT NULL DEFAULT 'N',
	PRIMARY KEY("IdPersona")
);

ALTER TABLE "BSCL.Portafolio.BD::POR.Portafolio" ADD ("IdPersonaBrokerExtranjero" BIGINT NOT NULL DEFAULT 0);



ALTER TABLE "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" ADD ("IdModeloCustodia"			NVARCHAR(40) NOT NULL DEFAULT '');

CREATE TABLE "BSCL.Portafolio.BD::POR.ModeloCustodia"(
	"IdModeloCustodia"		NVARCHAR(40) NOT NULL,
	"NombreModeloCustodia"	NVARCHAR(255) NOT NULL,
	PRIMARY KEY("IdModeloCustodia")
);

INSERT INTO "BSCL.Portafolio.BD::POR.ModeloCustodia"
SELECT 'LOCAL','LOCAL' FROM DUMMY;
INSERT INTO "BSCL.Portafolio.BD::POR.ModeloCustodia"
SELECT 'EXTERIOR','EXTERIOR' FROM DUMMY;

ALTER TABLE "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" ADD ("IdTipoCustodia"			NVARCHAR(40) NOT NULL DEFAULT '');

CREATE TABLE "BSCL.Portafolio.BD::POR.TipoCustodia"(
	"IdTipoCustodia"		NVARCHAR(40) NOT NULL,
	"NombreTipoCustodia"	NVARCHAR(255) NOT NULL,
	PRIMARY KEY("IdTipoCustodia")
);

--DELETE FROM "BSCL.Portafolio.BD::POR.TipoCustodia";
INSERT INTO "BSCL.Portafolio.BD::POR.TipoCustodia"
SELECT 'PROPIA','PROPIA' FROM DUMMY;
INSERT INTO "BSCL.Portafolio.BD::POR.TipoCustodia"
SELECT 'MATRIZCCB','MATRIZ CREDIBOLSA' FROM DUMMY;
INSERT INTO "BSCL.Portafolio.BD::POR.TipoCustodia"
SELECT 'BANCOCUSTODIO','BANCO CUSTODIO' FROM DUMMY;
INSERT INTO "BSCL.Portafolio.BD::POR.TipoCustodia"
SELECT 'CUSTODIOGLOBAL','CUSTODIO GLOBAL' FROM DUMMY;

ALTER TABLE "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" ADD ("IdTipoCuentaCustodia"		NVARCHAR(40) NOT NULL DEFAULT '');

CREATE TABLE "BSCL.Portafolio.BD::POR.TipoCuentaCustodia"(
	"IdTipoCuentaCustodia"		NVARCHAR(40) NOT NULL,
	"NombreTipoCuentaCustodia"	NVARCHAR(255) NOT NULL,
	PRIMARY KEY("IdTipoCuentaCustodia")
);

--DELETE FROM "BSCL.Portafolio.BD::POR.TipoCuentaCustodia";
INSERT INTO "BSCL.Portafolio.BD::POR.TipoCuentaCustodia"
SELECT 'DEPOSITANTE','DEPOSITANTE' FROM DUMMY;
INSERT INTO "BSCL.Portafolio.BD::POR.TipoCuentaCustodia"
SELECT 'MANDANTE','MANDANTE INDIVIDUAL RUT' FROM DUMMY;
INSERT INTO "BSCL.Portafolio.BD::POR.TipoCuentaCustodia"
SELECT 'OMNIBUS','OMNIBUS' FROM DUMMY;

ALTER TABLE "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" ADD ("IdPersonaBancoCustodio"	BIGINT NOT NULL DEFAULT 0);
ALTER TABLE "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" ADD ("IdPersonaBancoLiquidador"	BIGINT NOT NULL DEFAULT 0);

ALTER TABLE "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" ADD ("LiquidaDividendos"		NVARCHAR(1) NOT NULL DEFAULT 'N');
ALTER TABLE "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" ADD ("LiquidaCupones"		NVARCHAR(1) NOT NULL DEFAULT 'N');
ALTER TABLE "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" ADD ("LiquidaVencimientos"	NVARCHAR(1) NOT NULL DEFAULT 'N');
ALTER TABLE "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" ADD ("LiquidaPrepagos"		NVARCHAR(1) NOT NULL DEFAULT 'N');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3516,'BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstadoBrokerExtranjero',0,'Q','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3517,'BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstadoBancoCustodio',0,'Q','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3510,'BSCL.Plataforma.BD.Vistas::PLA.ConsultarBancoCustodio',0,'Q','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3511,'BSCL.Plataforma.BD.Vistas::PLA.ConsultarBrokerExtranjero',0,'Q','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3512,'BSCL.Plataforma.BD.Servicios::PLA.ModificarBancoCustodio',0,'P','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3513,'BSCL.Plataforma.BD.Servicios::PLA.ModificarBrokerExtranjero',0,'P','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3514,'BSCL.Plataforma.BD.Servicios::PLA.CrearBancoCustodio',0,'P','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3515,'BSCL.Plataforma.BD.Servicios::PLA.CrearBrokerExtranjero',0,'P','','','');c

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3519,'BSCL.Portafolio.BD.Vistas::ConsultarModeloCuentaCustodio',0,'Q','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3520,'BSCL.Portafolio.BD.Vistas::ConsultarTipoCuentaCustodia',0,'Q','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3521,'BSCL.Portafolio.BD.Vistas::ConsultarTipoCustodiaPortafolio',0,'Q','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3522,'BSCL.Plataforma.BD.Vistas::PLA.ConsultarBancoLiquidador',0,'Q','','','');


INSERT INTO "BSCL.Plataforma.BD::PLA.EstadoPla"("IdEstadoPla","IdEstado","NombreEntidad","Descripcion")
SELECT 24,'ACT','BROKEREXTRANJERO','' FROM DUMMY;
INSERT INTO "BSCL.Plataforma.BD::PLA.EstadoPla"("IdEstadoPla","IdEstado","NombreEntidad","Descripcion")
SELECT 25,'INA','BROKEREXTRANJERO','' FROM DUMMY;
INSERT INTO "BSCL.Plataforma.BD::PLA.EstadoPla"("IdEstadoPla","IdEstado","NombreEntidad","Descripcion")
SELECT 22,'ACT','BANCOCUSTODIO','' FROM DUMMY;
INSERT INTO "BSCL.Plataforma.BD::PLA.EstadoPla"("IdEstadoPla","IdEstado","NombreEntidad","Descripcion")
SELECT 23,'INA','BANCOCUSTODIO','' FROM DUMMY;

CREATE COLUMN TABLE "BSCL.Plataforma.BD::COM_2.DefinicionCobroRango" (
	"IdEmpresa"				BIGINT NOT NULL,
	"CodigoDefinicionCobro"	NVARCHAR(40) NOT NULL,
	"Hasta"			DECIMAL(26,8) NOT NULL,
	"Valor"					DECIMAL(26,8) NOT NULL,
	PRIMARY KEY ("IdEmpresa","CodigoDefinicionCobro","Hasta")
);


CREATE COLUMN TABLE "BSCL.Plataforma.BD::COM_2.DefinicionCobroEmisor" (
	"IdEmpresa"				BIGINT NOT NULL,
	"CodigoDefinicionCobro"	NVARCHAR(40) NOT NULL,
	"IdEmisor"				NVARCHAR(40) NOT NULL,
	PRIMARY KEY("IdEmpresa","CodigoDefinicionCobro","IdEmisor")
);

ALTER TABLE "BSCL.Plataforma.BD::COM_2.DefinicionCobro" ADD ("PorcentualRango_IdMonedaRangoMonto" NVARCHAR(3) NULL);
ALTER TABLE "BSCL.Plataforma.BD::COM_2.DefinicionCobro" ADD ("PorcentualRango_IdTipoRangoCobro" NVARCHAR(40) NULL);

ALTER TABLE "BSCL.Plataforma.BD::COM_2.DefinicionCobro" ADD ("Otros_IncluyeExcluyeEmisoresCheck" NVARCHAR(1) NULL);
ALTER TABLE "BSCL.Plataforma.BD::COM_2.DefinicionCobro" ADD ("Otros_IncluyeExcluyeInstrumentosCheck" NVARCHAR(1) NULL);
ALTER TABLE "BSCL.Plataforma.BD::COM_2.DefinicionCobro" ADD ("Otros_IncluyeExcluyeTiposDeClienteCheck" NVARCHAR(1) NULL);
ALTER TABLE "BSCL.Plataforma.BD::COM_2.DefinicionCobro" ADD ("Otros_IncluyeExcluyeClientesCheck" NVARCHAR(1) NULL);
ALTER TABLE "BSCL.Plataforma.BD::COM_2.DefinicionCobro" ADD ("Otros_IncluyeExcluyeCorresponsalesCheck" NVARCHAR(1) NULL);

ALTER TABLE "BSCL.Plataforma.BD::COM_2.DefinicionCobro" ADD ("Otros_IncluyeExcluyeEmisoresValor" NVARCHAR(1) NULL);
ALTER TABLE "BSCL.Plataforma.BD::COM_2.DefinicionCobro" ADD ("Otros_IncluyeExcluyeInstrumentosValor" NVARCHAR(1) NULL);
ALTER TABLE "BSCL.Plataforma.BD::COM_2.DefinicionCobro" ADD ("Otros_IncluyeExcluyeTiposDeClienteValor" NVARCHAR(1) NULL);
ALTER TABLE "BSCL.Plataforma.BD::COM_2.DefinicionCobro" ADD ("Otros_IncluyeExcluyeClientesValor" NVARCHAR(1) NULL);
ALTER TABLE "BSCL.Plataforma.BD::COM_2.DefinicionCobro" ADD ("Otros_IncluyeExcluyeCorresponsalesValor" NVARCHAR(1) NULL);


CREATE COLUMN TABLE "BSCL.Plataforma.BD::COM_2.PerfilCobros" (
	"IdPerfilCobros" 				NVARCHAR(40) NOT NULL,
	"DescripcionPerfilCobros" 		NVARCHAR(100) NOT NULL,
	"EsPerfilPersonalizado" 		NVARCHAR(1) NOT NULL,
	"FechaCreacion" 				SECONDDATE NOT NULL,
	"FechaModificacion" 			SECONDDATE NOT NULL,
	"IdEstado" 						NVARCHAR(3) NOT NULL,
	"IdEmpresa" 					BIGINT NOT NULL,
	"TipoEntidadReferencia" 		NVARCHAR(40) NOT NULL,
	"IdReferencia" 					BIGINT NOT NULL DEFAULT 0,
	"FechaIngreso" 					SECONDDATE NOT NULL DEFAULT '0001-01-01 00:00:00',
	"IdEstadoAutorizacion" 			NVARCHAR(3) NOT NULL,
	"IdUsuarioCreador"				BIGINT NULL,
	PRIMARY KEY ("IdEmpresa","IdPerfilCobros")
) ;

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3544,'BSCL.Plataforma.BD.Vistas::COM_2.ConsultarPerfilCobros',0,'Q','','','');


INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3545,'BSCL.Plataforma.BD.Servicios::COM_2.CrearPerfilCobros',0,'P','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3546,'BSCL.Plataforma.BD.Servicios::COM_2.ModificarPerfilCobros',0,'P','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3547,'BSCL.Plataforma.BD.Servicios::COM_2.ConfirmarModificarPerfilCobros',0,'P','','','');

CREATE COLUMN TABLE "BSCL.Plataforma.BD::COM_2.DetallePerfilCobro" (
	"IdDetallePerfilCobros"	BIGINT NOT NULL,
	"IdEmpresa"				BIGINT NOT NULL,
	"IdPerfilCobros"		NVARCHAR(40) NOT NULL,
	"IdTipoProducto"		NVARCHAR(4) NOT NULL,
	"IdTipoOperacion"		NVARCHAR(6) NOT NULL,
	"IdTipoCanalIngreso"	NVARCHAR(3) NULL,
	"IdBolsa"				NVARCHAR(3) NULL,
	"CodigoDefinicionCobro"	NVARCHAR(40) NOT NULL,
	
	"FechaCreacion"			SECONDDATE NOT NULL DEFAULT '0001-01-01',
	"FechaIngresoRegistro"	SECONDDATE NOT NULL DEFAULT CURRENT_TIMESTAMP,
	"IdEstadoAutorizacion"	NVARCHAR(3) NOT NULL,
	"IdUsuarioCreador"		BIGINT NOT NULL,
	"Borrado"				NVARCHAR(1) NOT NULL DEFAULT 'N',
	"IdUsuarioBorra"		BIGINT NULL,
	
	PRIMARY KEY ("IdDetallePerfilCobros")
);

CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::COM_2.DetallePerfilCobro"
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;

CREATE UNIQUE INDEX "idx_COM_2_DetallePerfilCobro" ON "BSCL.Plataforma.BD::COM_2.DetallePerfilCobro"("IdTipoProducto","IdPerfilCobro","IdTipoOperacion","IdBolsa","IdTipoCanalIngreso","CodigoDefinicionCobro","Borrado");

ALTER TABLE "BSCL.Plataforma.BD::COM_2.DetallePerfilCobro" ADD CONSTRAINT "fk_COM2_DetallePerfilCobro_IdBolsa" FOREIGN KEY("IdBolsa") REFERENCES "BSCL.Plataforma.BD::INS.Bolsa"("IdBolsa") ON DELETE RESTRICT;
ALTER TABLE "BSCL.Plataforma.BD::COM_2.DetallePerfilCobro" ADD CONSTRAINT "fk_COM2_DetallePerfilCobro_IdTipoCanalIngreso" FOREIGN KEY("IdTipoCanalIngreso") REFERENCES "BSCL.Plataforma.BD::PLA.TipoCanalIngreso"("IdTipoCanalIngreso") ON DELETE RESTRICT;
--TODO LAS DEMAS FKs

--INSERT INTO "BSCL.Plataforma.BD::COM_2.DetallePerfilCobro"
--select 1,'HOLA','INAC',null,null,null,'ASDF',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,'ING',1 from dummy
--commit

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3548,'BSCL.Plataforma.BD.Vistas::COM_2.ConsultarDetallePerfilCobros',0,'Q','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3551,'BSCL.Plataforma.BD.Vistas::COM_2.ConsultarTipoOperacionCobro',0,'Q','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3552,'BSCL.Plataforma.BD.Vistas::COM_2.ConsultarTipoCanalIngresoCobro',0,'Q','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3553,'BSCL.Plataforma.BD.Vistas::COM_2.ConsultarBolsaCobro',0,'Q','','','');

ALTER TABLE "BSCL.Plataforma.BD::PLA.TipoCanalIngreso" ADD ("PermiteIngresoOrdenes"	NVARCHAR(1) NOT NULL DEFAULT 'N');
UPDATE "BSCL.Plataforma.BD::PLA.TipoCanalIngreso" SET "PermiteIngresoOrdenes" = 'S' WHERE "IdTipoCanalIngreso" IN ('AGE','CLI','TRA');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3553,'BSCL.Plataforma.BD.Vistas::COM_2.ConsultarBolsaCobro',0,'Q','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(20012,'PLUGIN_PLA.jar@cl.bcs.plataforma.com.pe.CrearDetallePerfilCobro',0,'J','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(20013,'PLUGIN_PLA.jar@cl.bcs.plataforma.com.pe.EliminarDetallePerfilCobro',0,'J','','','');

ALTER TABLE "BSCL.Portafolio.BD::POR.Portafolio" ADD ("IdPerfilCobrosPeru" NVARCHAR(40) NOT NULL DEFAULT 0);
ALTER TABLE "BSCL.Portafolio.BD::POR.Portafolio" ADD ("IdPerfilCobrosPeruExtra" NVARCHAR(40) NOT NULL DEFAULT 0);

UPDATE "BSCL.Portafolio.BD::POR.Portafolio" SET "IdPerfilCobrosPeruExtra" = 'EXT_' || "IdPortafolio";


INSERT INTO "BSCL.Plataforma.BD::COM_2.PerfilCobros"("IdPerfilCobros","DescripcionPerfilCobros","EsPerfilPersonalizado","FechaCreacion",
"FechaModificacion","IdEstado","IdEmpresa","TipoEntidadReferencia","IdReferencia","FechaIngreso","IdEstadoAutorizacion","IdUsuarioCreador")
SELECT "IdPerfilCobrosPeruExtra" as "IdPerfilCobros","IdPerfilCobrosPeruExtra" AS "DescripcionPerfilCobros",'S' AS "EsPerfilPersonalizado",
CURRENT_TIMESTAMP AS "FechaCreacion",CURRENT_TIMESTAMP AS "FechaModificacion",'ACT' AS "IdEstado",cli."IdEmpresa",'' as "TipoEntidadReferencia",0 as "IdReferencia",
CURRENT_TIMESTAMP AS "FechaIngreso",'AUT' AS "IdEstadoAutorizacion",0 "IdUsuarioCreador"
FROM "BSCL.Portafolio.BD::POR.Portafolio" por
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" cli on cli."IdCliente" = por."IdCliente";

ALTER TABLE "BSCL.Plataforma.BD::COM.PerfilComercialBase" ADD ("IdPerfilCobrosPeru"	NVARCHAR(40) NOT NULL DEFAULT '');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3555,'BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarPortafoliosCotitulares',0,'Q','','','');

insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad","IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
  "GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (1051, 'PORTAFOLIO', 1, '', 'COMBO', 'Orden Especial/Ordinaria por Defecto','ORDDEFECTOESPORD', 
'GRUPOORDENES', 'PRE', 'ACT', 0, 0, 99, 'N' );

INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
VALUES (1,1051,'ESPECIAL','ESPECIAL');
INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
VALUES (2,1051,'ORDINARIA','ORDINARIA');


ALTER TABLE "BSCL.Plataforma.BD::COM_2.DefinicionCobro" ADD("IdTipoProducto" NVARCHAR(4) NULL)

-- FIN PERU



INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3542,'BSCL.Plataforma.BD.Servicios::INS.ConsultarInstrumentoPrecioPuntas',0,'P','','','');

UPDATE "BSCL.Plataforma.BD::INS.TipoOperacion" set "AccionOrden" = null where "IdTipoOperacion" in ('ANUAPU','ANURES','ANUPRE','ANUAPOFM');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3518,'BSCL.Portafolio.BD.Vistas.UI::ConsultarPortafolio',0,'Q','','','');

update "BSCL"."BSCL.Plataforma.BD::AUT.TipoEvento" set "SPRechaza" = 'BSCL.Portafolio.BD.Servicios::RechazaAutorizacionCuentaCustodio'  where "IdTipoEvento" = 'MCTCUP';
update "BSCL"."BSCL.Plataforma.BD::AUT.TipoEvento" set "SPRechaza" = 'BSCL.Portafolio.BD.Servicios::RechazaAutorizacionCuentaCustodio'  where "IdTipoEvento" = 'NPCCUS';

ALTER TABLE "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ADD("EsPorDefecto"	NVARCHAR(1) NOT NULL DEFAULT 'N');

UPDATE "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion"
SET "EsPorDefecto" = 'S'
WHERE "IdMoneda" = 'CLP' AND "IdTipoInstrumento" = 'ACN';


UPDATE "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion"
SET "EsPorDefecto" = 'S'
WHERE "IdMoneda" = 'PEN' AND "IdTipoInstrumento" = 'MIL';

--LIBERAR TABLAS DE CREAR SIMULACION ASIGNACION
--LIBERAR TABLAS DE CREAR ORDENES
--LIBERAR TABLAS MARKET DATA
--liberar tablas crear palos market data

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3525,'BSCL.Orden.BD.Servicios::ConsultarRequisitosBasicosCrearOrdenes',0,'P','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3530,'BSCL.Plataforma.BD.Vistas::COM_2.ConsultarDefinicionCobro',0,'Q','','','');

DELETE FROM "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio" = 13020;

ALTER TABLE "BSCL.Plataforma.BD::INS.BolsaInstrumentoPrecio" DROP ("PrecioMercado");
ALTER TABLE "BSCL.Plataforma.BD::INS.BolsaInstrumentoPrecio" ADD ("Variacion" DECIMAL(26,8) NOT NULL DEFAULT 0);
ALTER TABLE "BSCL.Plataforma.BD::INS.BolsaInstrumentoPrecio" DROP ("PuntaCompra");
ALTER TABLE "BSCL.Plataforma.BD::INS.BolsaInstrumentoPrecio" DROP ("PuntaVenta");

CREATE TABLE "BSCL.Plataforma.BD::INS.BolsaInstrumentoPuntas"(
	"IdInstrumento"					BIGINT			NOT NULL,
	"IdBolsa" 						NVARCHAR(3) 	NOT NULL,
	"IdCondicionDeLiquidacion"		NVARCHAR(2)		NOT NULL,
	"Fecha" 						DATE 			NOT NULL,
	"FechaActualizacion"			SECONDDATE		NOT NULL DEFAULT CURRENT_TIMESTAMP,
	"PuntaCompra"					DECIMAL(26,8)	NOT NULL DEFAULT 0,			
	"PuntaVenta"					DECIMAL(26,8)	NOT NULL DEFAULT 0,
	PRIMARY KEY("IdInstrumento","IdBolsa","IdCondicionDeLiquidacion","Fecha")		
);


INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(20011,'PLUGIN_PLA.jar@cl.bcs.plataforma.com.pe.CrearDefinicionCobro',0,'J','','','');

ALTER TABLE "BSCL.Orden.BD::ORD.Orden" ADD ("IdSucursalEmpleadoPortafolio"	BIGINT NULL);
ALTER TABLE "BSCL.Orden.BD::ORD.Orden" ADD ("IdSucursalEmpleadoDigitador"	BIGINT NULL);

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(20019,'PLUGIN_ORD.jar@cl.bcs.ordenes.CrearOrdenes',0,'J','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Configuracion"("IdEmpresa","CodigoParametro","Descripcion","Valor")
SELECT 1,'PLA.CallBatchURL','','http://localhost:8085/BSCL/Plataforma/GWJAVA/PLA/CallBatch' FROM DUMMY;

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Configuracion"("IdEmpresa","CodigoParametro","Descripcion","Valor")
SELECT 1,'PLA.CallBatchToken','','asdf' FROM DUMMY;

LIBERAR TABLAS DE CREAR ORDENES
LIBERAR TABLAS DE CREAR SIMULACION ASIGNACION
LIBERAR TABLAS DE MIGRACION
LIBERAR TABLAS MARKET DATA CREAR TRANSACCIONES

INSERT INTO "BSCL.Orden.BD::ORD.ConfiguracionOrdenes"("IdEmpresa","CodigoParametro","Descripcion","Valor")
SELECT 1,'IdBovedaDefecto','Boveda por defecto para asignacion','DCV' from dummy;

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
SELECT 3587,'BSCL.Orden.BD.Vistas::ConsultarIndicadoresPortafolioOrdenes',0,'Q','','','' FROM DUMMY;

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(20021,'PLUGIN_ORD.jar@cl.bcs.ordenes.CalcularMontoYCobrosOrden',0,'J','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(20024,'PLUGIN_ORD.jar@cl.bcs.ordenes.CrearOrdenesPostreras',0,'J','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(20025,'PLUGIN_ORD.jar@cl.bcs.ordenes.CrearAsignaciones',0,'J','','','');


ALTER TABLE "BSCL.Orden.BD::ORD.Orden" ADD ("TasaReporte" DECIMAL(26,8) NOT NULL DEFAULT 0);
ALTER TABLE "BSCL.Orden.BD::ORD.Orden" ADD ("PlazoReporte" INTEGER NOT NULL DEFAULT 0);
ALTER TABLE "BSCL.Orden.BD::ORD.Orden" ADD ("BaseReporte" INTEGER NOT NULL DEFAULT 0);
ALTER TABLE "BSCL.Orden.BD::ORD.Orden" ADD ("EsConfirmada" NVARCHAR(1) NOT NULL DEFAULT 'S');

ALTER TABLE "BSCL.Orden.BD::ORD.CrearOrdenes_Entrada_Ordenes" ADD ("TasaReporte" DECIMAL(26,8) NOT NULL DEFAULT 0);
ALTER TABLE "BSCL.Orden.BD::ORD.CrearOrdenes_Entrada_Ordenes" ADD ("PlazoReporte" INTEGER NOT NULL DEFAULT 0);
ALTER TABLE "BSCL.Orden.BD::ORD.CrearOrdenes_Entrada_Ordenes" ADD ("BaseReporte" INTEGER NOT NULL DEFAULT 0);
ALTER TABLE "BSCL.Orden.BD::ORD.CrearOrdenes_Entrada_Ordenes" ADD ("EsConfirmada" NVARCHAR(1) NOT NULL DEFAULT 'S');


COMMIT;




