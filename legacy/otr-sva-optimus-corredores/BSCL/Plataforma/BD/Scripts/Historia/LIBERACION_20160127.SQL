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


--
--
-- LIBERACION SEMANA: 22-01-2016
--
alter table "BSCL"."BSCL.Facturacion.BD::FAC.MovimientoComprobante" add ("FechaTipoCambio" DATE DEFAULT '0001-01-01');
alter table "BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" add ("Origen" NVARCHAR(10) default 'OBSERVADO'); 


alter table "BSCL"."BSCL.Custodia.BD::CUS.Subyacente"
	add (
		"IdEmisor" 			NVARCHAR(40) default '' not null,
		"FechaVencimiento" 	DATE default '0001-01-01' not null,
		"CodigoIFRS" 		NVARCHAR(40) default '' not null,
		"IdComprobanteRef" 	BIGINT default 0 not null
	);
	
alter table "BSCL"."BSCL.Custodia.BD::GTA.Garantia"
	add (
		"IdEmisor" 			NVARCHAR(40) default '' not null,
		"FechaVencimiento" 	DATE default '0001-01-01' not null,
		"CodigoIFRS" 		NVARCHAR(40) default '' not null,
		"IdComprobanteRef" 	BIGINT default 0 not null
	);
	
alter table "BSCL"."BSCL.Custodia.BD::CUS.Cartera"
	add  ("EsFungible" NVARCHAR(1) default 'N' not null);
	
alter table "BSCL"."BSCL.Custodia.BD::CUS.Cartera"
	Drop  ("Fungible");
	

alter table "BSCL"."BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito"
	add  ("EsFungible" NVARCHAR(1) default 'N' not null);
	
alter table "BSCL"."BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito"
	Drop  ("Fungible");	
	
	
alter table "BSCL"."BSCL.Custodia.BD::GTA.Garantia"
	add  ("FechaVencimientoInstrumento" DATE default '0001-01-01' not null,
	 	  "FechaEmisionInstrumento" DATE default '0001-01-01' not null);
		  
alter table "BSCL"."BSCL.Custodia.BD::CUS.Cartera"
	add  ("FechaVencimientoInstrumento" DATE default '0001-01-01' not null,
	 	  "FechaEmisionInstrumento" DATE default '0001-01-01' not null);
		  
alter table "BSCL"."BSCL.Custodia.BD::CUS.Subyacente"
	add  ("FechaVencimientoInstrumento" DATE default '0001-01-01' not null,
	 	  "FechaEmisionInstrumento" DATE default '0001-01-01' not null,
		  "IdTipoReajuste" 	NVARCHAR(2) default 'NR' not null);
		  
alter table "BSCL"."BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito"
	add  ("FechaVencimientoInstrumento" DATE default '0001-01-01' not null,
		 	  "FechaEmisionInstrumento" DATE default '0001-01-01' not null);
	
select * from "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" order by "IdServicio"
-------------------SCRIPT USUARIOS----------------------------
--Secuencia tabla usuarios
DROP SEQUENCE "BSCL""BSCL.Plataforma.BD.Secuencias::Usuario"	CASCADE;
CREATE SEQUENCE "BSCL"."BSCL.Plataforma.BD.Secuencias::Usuario"
       START     WITH 4
       INCREMENT BY 1
       MINVALUE  1;
--Secuencia tabla rolUsuario
CREATE SEQUENCE "BSCL"."BSCL.Plataforma.BD.Secuencias::RolUsuario"
       START     WITH 4
       INCREMENT BY 1
       MINVALUE  1;
       
--Agregar Campos a la tabla usuarios
ALTER TABLE "BSCL"."BSCL.Plataforma.BD::ACC.Usuario" ADD ("EsAutorizado" NVARCHAR(1) DEFAULT 'N' NOT NULL);       
alter table "BSCL"."BSCL.Plataforma.BD::ACC.Usuario" ADD ("TipoEntidad" NVARCHAR(40), "IdEntidad" BIGINT);

--Agregar IdEstado a la tabla perfiles
alter table "BSCL"."BSCL.Plataforma.BD::ACC.Perfil" ADD ("IdEstado" NVARCHAR(3));
Update "BSCL"."BSCL.Plataforma.BD::ACC.Perfil" set "IdEstado" = 'ACT';

--Agregar campo EsAutorizado a la tabla rolUsuario
ALTER TABLE "BSCL"."BSCL.Plataforma.BD::ACC.RolUsuario" ADD ("EsAutorizado" NVARCHAR(1) DEFAULT 'N' NOT NULL);
Update "BSCL"."BSCL.Plataforma.BD::ACC.RolUsuario" set "EsAutorizado" = 'S';

--Actualizar de estado usuario
Update "BSCL"."BSCL.Plataforma.BD::ACC.Usuario" set "IdEstado" = 'ACT' where "IdEstado" = 'VIG';

--Actualizar tipo usuarios
update "BSCL"."BSCL.Plataforma.BD::ACC.Usuario" set "TipoEntidad" = 'CLI', "IdEntidad" = 10000 WHERE "IdUsuario" = 2;
update "BSCL"."BSCL.Plataforma.BD::ACC.Usuario" set "TipoEntidad" = 'CLI', "IdEntidad" = 10001 WHERE "IdUsuario" = 3;

--Encriptar claves usuario
update "BSCL"."BSCL.Plataforma.BD::ACC.Usuario" set "Clave" = lower(HASH_SHA256(to_binary("Clave")));
update "BSCL"."BSCL.Plataforma.BD::ACC.Usuario" set "Clave" = HASH_SHA256(to_binary("Clave"));

--Agregar estadosUsuarios
insert into "BSCL"."BSCL.Plataforma.BD::PLA.EstadoPla"("IdEstadoPla", "IdEstado", "NombreEntidad", "Descripcion") values(4, 'ACT', 'Usuario', 'Activo');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.EstadoPla"("IdEstadoPla", "IdEstado", "NombreEntidad", "Descripcion") values(5, 'BLO', 'Usuario', 'Bloqueado');

--Agregar servicio de creacion a Tabla Servicios
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) VALUES(112,'BSCL.Plataforma.BD.Servicios::ACC.CrearUsuario',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) VALUES(120,'BSCL.Plataforma.BD.Servicios::ACC.AgregarRolUsuario',0,'P','');

--Agregar Concepto y tipo Evento
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('ENROLU','EU','PLA','ENROLAMIENO USUARIO','VIG');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('AROLUS','RU','PLA','AGREGAR ROL USUARIO','VIG');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::AUT.TipoEvento"("IdTipoEvento","IdEmpresa", "IdModulo", "Codigo", "Nombre", "HorasVigencia", "EtapasSecuenciales", "IdTipoProducto", "IdTipoOperacion", "IdUsuarioSuperAutorizador", "IdRolSuperAutorizador", "IdEstado") 
VALUES('ENROLU',1,'PLA','AUT_USU_ENRO','Enrolamiento de usuario',1,'S','' ,'' ,0,0,'VIG');
insert into "BSCL"."BSCL.Plataforma.BD::AUT.TipoEvento"("IdTipoEvento","IdEmpresa", "IdModulo", "Codigo", "Nombre", "HorasVigencia", "EtapasSecuenciales", "IdTipoProducto", "IdTipoOperacion", "IdUsuarioSuperAutorizador", "IdRolSuperAutorizador", "IdEstado") 
values('AROLUS',1,'PLA','AUT_USU_ROL','Agregar Rol Usuario',1,'S','' ,'' ,0,0,'VIG');
--------------------FIN SCRIPT USUARIOS---------------------------------------
--------------------SCRIPT ROLES----------------------------------------------
--Secuencia tabla Rol
CREATE SEQUENCE "BSCL"."BSCL.Plataforma.BD.Secuencias::Rol"
       START     WITH 3
       INCREMENT BY 1
       MINVALUE  1; 
--Agregar IdEstado a la tabla perfiles
alter table "BSCL"."BSCL.Plataforma.BD::ACC.Perfil" ADD ("IdEstado" NVARCHAR(3));
Update "BSCL"."BSCL.Plataforma.BD::ACC.RolUsuario" set "IdEstado" = 'ACT';
--------------------FIN SCRIPT ROLES---------------------------------------

--OPERACIONES DE VALORES EXTRANJEROS

--CAMBIAR IDPORTAFOLIOCLIENTE POR IDPORTAFOLIO
ALTER TABLE "BSCL.Orden.BD::ORD.Orden" ADD (	"IdInstrumentoSubyacente"              	BIGINT NOT NULL DEFAULT 0);
ALTER TABLE "BSCL.Orden.BD::ORD.Orden" ADD (	"IdEmpleadoPortafolio"					NVARCHAR(40) NOT NULL DEFAULT '');
ALTER TABLE "BSCL.Orden.BD::ORD.Orden" ADD (	"IdEmpleadoDigitador" 					NVARCHAR(40) NOT NULL DEFAULT '');
ALTER TABLE "BSCL.Orden.BD::ORD.Orden" ADD (	"IdTipoPrecio" 							NVARCHAR(1) NOT NULL DEFAULT '');
ALTER TABLE "BSCL.Orden.BD::ORD.Orden" ADD (	"IdTipoTasa" 							NVARCHAR(1) NOT NULL DEFAULT '');
ALTER TABLE "BSCL.Orden.BD::ORD.Orden" ADD (	"IdPorcentajeVisibilidad" 				BIGINT NOT NULL DEFAULT 0);
ALTER TABLE "BSCL.Orden.BD::ORD.Orden" ADD (	"IdTipoVigenciaOrden" 					NVARCHAR(2) NOT NULL DEFAULT '');
ALTER TABLE "BSCL.Orden.BD::ORD.Orden" ADD (	"IdMonedaOrden"							NVARCHAR(3) NOT NULL DEFAULT '');
ALTER TABLE "BSCL.Orden.BD::ORD.Orden" ADD (	"IdMonedaLiquidacionOrden"				NVARCHAR(3) NOT NULL DEFAULT '');
ALTER TABLE "BSCL.Orden.BD::ORD.Orden" ADD (	"IdMonedaLiquidacionCobros"				NVARCHAR(3) NOT NULL DEFAULT '');
ALTER TABLE "BSCL.Orden.BD::ORD.Orden" ADD (	"MontoLocal" 							DECIMAL(26,8) NOT NULL DEFAULT 0);
ALTER TABLE "BSCL.Orden.BD::ORD.Orden" ADD (	"MontoOrden" 							DECIMAL(26,8) NOT NULL DEFAULT 0);
ALTER TABLE "BSCL.Orden.BD::ORD.Orden" ADD (	"MontoLiquidacion"						DECIMAL(26,8) NOT NULL DEFAULT 0 );
ALTER TABLE "BSCL.Orden.BD::ORD.Orden" ADD (	"TipoCambio"							DECIMAL(26,8) NOT NULL DEFAULT 0);
ALTER TABLE "BSCL.Orden.BD::ORD.Orden" ADD (	"IdTipoCustodia" 						NVARCHAR(3) NOT NULL DEFAULT '');
ALTER TABLE "BSCL.Orden.BD::ORD.Orden" ADD (	"IdFormaCustodia" 						NVARCHAR(3) NOT NULL DEFAULT '');

ALTER TABLE "BSCL.Orden.BD::ORD.Orden" DROP (	"IdInstrumentoAsociado" );
ALTER TABLE "BSCL.Orden.BD::ORD.Orden" DROP (	"IdTipoOrden" );
ALTER TABLE "BSCL.Orden.BD::ORD.Orden" DROP (	"IdVigencia" );
ALTER TABLE "BSCL.Orden.BD::ORD.Orden" DROP (	"IdCodigoTipoCustodia" );
ALTER TABLE "BSCL.Orden.BD::ORD.Orden" DROP (	"CodigoAutorizacion" );
ALTER TABLE "BSCL.Orden.BD::ORD.Orden" DROP (	"IdTipoRuteo" );
ALTER TABLE "BSCL.Orden.BD::ORD.Orden" DROP (	"IdCodigoFormaCustodia" );
ALTER TABLE "BSCL.Orden.BD::ORD.Orden" DROP (	"PorcVisible" );
ALTER TABLE "BSCL.Orden.BD::ORD.Orden" DROP (	"IdCodigoTipoPrecio" );
ALTER TABLE "BSCL.Orden.BD::ORD.Orden" DROP (	"IdCodigoTipoTasa" );
ALTER TABLE "BSCL.Orden.BD::ORD.Orden" DROP (	"IdAprobacionCliente" );
ALTER TABLE "BSCL.Orden.BD::ORD.Orden" DROP (	"CodigoAgente" );
ALTER TABLE "BSCL.Orden.BD::ORD.Orden" DROP (	"CodigoUsuarioDigitador" );
ALTER TABLE "BSCL.Orden.BD::ORD.Orden" DROP (	"CodigoCertificador" );
ALTER TABLE "BSCL.Orden.BD::ORD.Orden" DROP (	"IdEstadoNegociacion" );
ALTER TABLE "BSCL.Orden.BD::ORD.Orden" DROP (	"Monto" );

ALTER TABLE "BSCL.Orden.BD::ORD.Asignacion" ADD (	"MontoAsignadoLocal"				DECIMAL(26,8) NOT NULL DEFAULT 0);
ALTER TABLE "BSCL.Orden.BD::ORD.Asignacion" ADD (	"MontoAsignacionLiquidacion"		DECIMAL(26,8) NOT NULL DEFAULT 0);
ALTER TABLE "BSCL.Orden.BD::ORD.Asignacion" ADD (	"TipoCambio" 						DECIMAL(26,8) NOT NULL DEFAULT 0);

ALTER TABLE "BSCL.Orden.BD::ORD.CobroOrden" ADD ("MontoCobroCalculadoLocal" 		DECIMAL(26,8) NOT NULL DEFAULT 0);
ALTER TABLE "BSCL.Orden.BD::ORD.CobroOrden" ADD ("MontoCobroCalculadoLiquidacion"	DECIMAL(26,8) NOT NULL DEFAULT 0);

ALTER TABLE "BSCL.Orden.BD::ORD.CobroAsignacion" ADD ("MontoCobroCalculadoLocal" 		DECIMAL(26,8) NOT NULL DEFAULT 0);
ALTER TABLE "BSCL.Orden.BD::ORD.CobroAsignacion" ADD ("MontoCobroCalculadoLiquidacion"	DECIMAL(26,8) NOT NULL DEFAULT 0);


INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionYMoneda" ( "IdTipoOperacionYMoneda","IdTipoOperacion","IdMoneda","IdEstado" )  
VALUES(25,'COMACX','EUR','ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionYMoneda" ( "IdTipoOperacionYMoneda","IdTipoOperacion","IdMoneda","IdEstado" )  
VALUES(26,'VENACX','EUR','ACT');

INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionYMoneda" ( "IdTipoOperacionYMoneda","IdTipoOperacion","IdMoneda","IdEstado" )  
VALUES(27,'COMACN','USD','ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionYMoneda" ( "IdTipoOperacionYMoneda","IdTipoOperacion","IdMoneda","IdEstado" )  
VALUES(28,'VENACN','USD','ACT');

DROP TABLE "BSCL.Orden.BD::ORD.TipoOrden";
DROP TABLE "BSCL.Orden.BD::ORD.TipoRuteoOrden";

DROP SEQUENCE "BSCL.Orden.BD.Secuencias::TipoOrden"      CASCADE;

INSERT INTO "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento"
("IdBolsaTipoInstrumento","IdTipoInstrumento","IdBolsa","IdBoveda","ExigeGarantia","IdContraparteGarantia","IdEstado" )  
select max("IdBolsaTipoInstrumento")+1,'MIL','BCS','','N',0,'ACT' from "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento";

----- 2016-01-21----------------------------------
alter table "BSCL.CuentaInversion.BD::CTA.CuentaInversion" add ("IdCarga" BIGINT DEFAULT 0 NOT NULL);
alter table "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" add ("IdCarga" BIGINT DEFAULT 0 NOT NULL);

CREATE COLUMN TABLE "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" (
	"IdTipoInstrumentoMonedaLiquidacion"		BIGINT 			not null,
	"IdTipoInstrumento" 						NVARCHAR(3) 	not null default '',
	"IdMoneda"									NVARCHAR(3) 	not null default '',
	"IdMonedaLiquidacion"						NVARCHAR(3) 	not null default '',
	PRIMARY KEY ("IdTipoInstrumentoMonedaLiquidacion")
);
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )
VALUES(1,'BBC','UF','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )
VALUES(2,'BBC','USD','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )
VALUES(3,'BBC','CLP','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(4,'BCD','USD','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(5,'BCP','CLP','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(6,'BCU','UF','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )
VALUES(7,'BEM','UF','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )
VALUES(8,'BEM','USD','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )
VALUES(9,'BEM','CLP','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(10,'CER','UF','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(11,'FRW','CLP','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(12,'LTH','UF','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )
VALUES(13,'LTH','USD','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )
VALUES(14,'LTH','CLP','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(15,'PRC','UF','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(16,'ZER','USD','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(17,'PAC','CLP','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(18,'BCA','UF','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )
VALUES(19,'BCA','USD','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )
VALUES(20,'BCA','CLP','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(21,'BCX','USD','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(22,'BTP','CLP','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(23,'BTU','UF','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(24,'BUS','UF','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(25,'BVL','UF','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(26,'COR','CLP','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(27,'PCX','USD','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(28,'PRD','USD','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(29,'XER','USD','CLP');

INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(30,'ACN','USD','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(31,'ACX','USD','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(32,'ACX','EUR','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(33,'MIL','PEN','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(34,'MIL','COL','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(35,'ACN','CLP','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(36,'MIL','USD','CLP');

--CORRECION SOL PERUANO
INSERT INTO "BSCL.Plataforma.BD::PLA.Moneda" ( "IdMoneda","Codigo","CodigoNumerico","Nombre","Simbolo","Decimales","MultiplicaODivide","RangoSuperiorParidad","RangoInferiorParidad","PorcentajeSuperiorParidad","PorcentajeInferiorParidad","RangoSuperiorTipoCambio","RangoInferiorTipoCambio","PorcentajeSuperiorTipoCambio","PorcentajeInferiorTipoCambio","Orden","IdPais","IdEstado" )  
VALUES('PEN','PEN',24,'SOL PERUANO','PEN',0,'null',0E-8,0E-8,0E-8,0E-8,0E-8,0E-8,0E-8,0E-8,0,1,'ACT');
UPDATE "BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" set "IdMoneda" = 'PEN' where "IdMoneda" = 'SOL'
delete from "BSCL.Plataforma.BD::PLA.Moneda" where "IdMoneda" = 'SOL'
UPDATE "BSCL.Plataforma.BD::PLA.Moneda" set "IdMoneda" = 'COP' where "IdMoneda" = 'PCL'

update "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" set "IdTipoOperacion" = 'COMMIL' where "IdTipoOperacion" = 'COMST';
update "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" set "IdTipoOperacion" = 'VENMIL' where "IdTipoOperacion" = 'VENST';

--FIN OPERACIONES DE VALORES EXTRANJEROS

--DETALLE LOG
ALTER TABLE "BSCL"."BSCL.Plataforma.BD::PLA.DetalleLog" ALTER ("ValorAntiguo" NVARCHAR(250), "ValorNuevo" NVARCHAR(250));
-- FIN DETALLE LOG

-- TABLA DE REGISTROS
DROP TABLE "BSCL.Plataforma.BD::PLA.TipoRegistro";

CREATE COLUMN TABLE "BSCL.Plataforma.BD::PLA.TipoRegistro" (
	 "IdTipoRegistro"	NVARCHAR(40) NOT NULL ,
	 "Descripcion" 		NVARCHAR(100), 
	PRIMARY KEY ("IdTipoRegistro")
);

-- INSERTAR DATOS A REGISTROS
insert into "BSCL.Plataforma.BD::PLA.TipoRegistro" ("IdTipoRegistro", "Descripcion") values('ORDEN', 'ORDEN');
insert into "BSCL.Plataforma.BD::PLA.TipoRegistro" ("IdTipoRegistro", "Descripcion") values('TRANSACCION', 'TRANSACCION');
insert into "BSCL.Plataforma.BD::PLA.TipoRegistro" ("IdTipoRegistro", "Descripcion") values('ASIGNACION', 'ASIGNACION');
insert into "BSCL.Plataforma.BD::PLA.TipoRegistro" ("IdTipoRegistro", "Descripcion") values('CLIENTE', 'CLIENTE');
insert into "BSCL.Plataforma.BD::PLA.TipoRegistro" ("IdTipoRegistro", "Descripcion") values('PORTAFOLIO', 'PORTAFOLIO');
insert into "BSCL.Plataforma.BD::PLA.TipoRegistro" ("IdTipoRegistro", "Descripcion") values('PERSONA', 'PERSONA');
insert into "BSCL.Plataforma.BD::PLA.TipoRegistro" ("IdTipoRegistro", "Descripcion") values('EMPLEADO', 'EMPLEADO');
insert into "BSCL.Plataforma.BD::PLA.TipoRegistro" ("IdTipoRegistro", "Descripcion") values('CONTRAPARTE', 'CONTRAPARTE');

-- FIN REGISTROS

alter table "BSCL"."BSCL.Custodia.BD::CUS.ConceptoCustodia" add(
	 "IdTipoInstrumento" 		 NVARCHAR(3)   not null default '',
	 "IdInstrumento" 			 BIGINT        not null default 0,
	 "IdContraparte"             BIGINT   	   not null default 0,
	 "Descripcion"               NVARCHAR(100) not null default ''
);



INSERT INTO BSCL."BSCL.Custodia.BD::CUS.ConceptoCustodia" ("IdConceptoCustodia", "IdConcepto", "NombreEntidad", "IdClasificacionTipoInstrumento", "IdTipoOperacion", "ConCertificadoAutomatico", "IdTipoInstrumento", "IdInstrumento", "IdContraparte", "Descripcion")
	VALUES(27, 'GAINSP', 'CUS.Garantia', 'RV', '', 'N', 'SIM', 2249, 10008, 'GARANTIA SIMULTANEA EN BCS - SIN CERTIFICAR');

INSERT INTO BSCL."BSCL.Custodia.BD::CUS.ConceptoCustodia" ("IdConceptoCustodia", "IdConcepto", "NombreEntidad", "IdClasificacionTipoInstrumento", "IdTipoOperacion", "ConCertificadoAutomatico", "IdTipoInstrumento", "IdInstrumento", "IdContraparte", "Descripcion")
	VALUES(24, 'GAINFW', 'CUS.Garantia', 'RF', '', 'S', 'FRW', 0, 10008, 'GARANTIA FORWARD EN BCS');

INSERT INTO BSCL."BSCL.Custodia.BD::CUS.ConceptoCustodia" ("IdConceptoCustodia", "IdConcepto", "NombreEntidad", "IdClasificacionTipoInstrumento", "IdTipoOperacion", "ConCertificadoAutomatico", "IdTipoInstrumento", "IdInstrumento", "IdContraparte", "Descripcion")
	VALUES(26, 'GAINSI', 'CUS.Garantia', 'RV', '', 'S', 'SIM', 2249, 10008, 'GARANTIA SIMULTANEA EN BCS');

INSERT INTO BSCL."BSCL.Custodia.BD::CUS.ConceptoCustodia" ("IdConceptoCustodia", "IdConcepto", "NombreEntidad", "IdClasificacionTipoInstrumento", "IdTipoOperacion", "ConCertificadoAutomatico", "IdTipoInstrumento", "IdInstrumento", "IdContraparte", "Descripcion")
	VALUES(25, 'GAINRF', 'CUS.Garantia', 'RF', '', 'S', '', 0, 10008, 'GARANTIA RENTA FIJA EN BCS');

INSERT INTO BSCL."BSCL.Custodia.BD::CUS.ConceptoCustodia" ("IdConceptoCustodia", "IdConcepto", "NombreEntidad", "IdClasificacionTipoInstrumento", "IdTipoOperacion", "ConCertificadoAutomatico", "IdTipoInstrumento", "IdInstrumento", "IdContraparte", "Descripcion")
	VALUES(28, 'GAINVC', 'CUS.Garantia', 'RV', '', 'S', '', 0, 10008, 'GARANTIA VENTA CORTA EN BCS');

INSERT INTO BSCL."BSCL.Custodia.BD::CUS.ConceptoCustodia" ("IdConceptoCustodia", "IdConcepto", "NombreEntidad", "IdClasificacionTipoInstrumento", "IdTipoOperacion", "ConCertificadoAutomatico", "IdTipoInstrumento", "IdInstrumento", "IdContraparte", "Descripcion")
	VALUES(23, 'GAINFU', 'CUS.Garantia', 'DV', '', 'S', 'FDO', 0, 10008, 'GARANTIA FUTURO DOLAR EN BCS');

DROP TABLE "BSCL.Custodia.BD::GTA.ConceptoGarantia";

-- postreras IF
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('PAG','PAGARE','PAGARE','PAGARE','VIG','IF');

INSERT INTO "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento"
("IdBolsaTipoInstrumento","IdTipoInstrumento","IdBolsa","IdBoveda","ExigeGarantia","IdContraparteGarantia","IdEstado" )  
select max("IdBolsaTipoInstrumento")+1,'PAG','BCS','','N',100,'ACT' from "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento";

CREATE COLUMN TABLE "BSCL.Plataforma.BD::INS.InstrumentoTipoReajuste" (
	"IdInstrumento" 		BIGINT 			NOT NULL,
	"IdTipoInstrumento" 	NVARCHAR(3) 	NOT NULL,
	"IdTipoReajuste" 		NVARCHAR(2) 	NOT NULL,
	"CorteMinimo" 			DECIMAL(26,8) 	DEFAULT '0' NOT NULL,
	PRIMARY KEY ("IdInstrumento","IdTipoReajuste")
);
INSERT INTO "BSCL.Plataforma.BD::INS.InstrumentoTipoReajuste"
SELECT "IdInstrumento","IdTipoInstrumento",'NR',5000000 
FROM "BSCL.Plataforma.BD::INS.Instrumento" WHERE "Nemotecnico" = 'PAGARE NR' and "IdTipoInstrumento" = 'PAG';
INSERT INTO "BSCL.Plataforma.BD::INS.InstrumentoTipoReajuste"
SELECT "IdInstrumento","IdTipoInstrumento",'US',5000
FROM "BSCL.Plataforma.BD::INS.Instrumento" WHERE "Nemotecnico" = 'PAGARE R' and "IdTipoInstrumento" = 'PAG';
INSERT INTO "BSCL.Plataforma.BD::INS.InstrumentoTipoReajuste"
SELECT "IdInstrumento","IdTipoInstrumento",'EU',5000
FROM "BSCL.Plataforma.BD::INS.Instrumento" WHERE "Nemotecnico" = 'PAGARE R' and "IdTipoInstrumento" = 'PAG';
INSERT INTO "BSCL.Plataforma.BD::INS.InstrumentoTipoReajuste"
SELECT "IdInstrumento","IdTipoInstrumento",'UF',5000
FROM "BSCL.Plataforma.BD::INS.Instrumento" WHERE "Nemotecnico" = 'PAGARE R' and "IdTipoInstrumento" = 'PAG';

--COMIIF
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
select max("IdTipoOperacionYCondicionDeLiquidacion")+1, 'S','T0','COMIIF','ACT' from "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion";
--VENIIF
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
select max("IdTipoOperacionYCondicionDeLiquidacion")+1, 'S','T0','VENIIF','ACT' from "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion";
--COMIIF
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionYMoneda" ( "IdTipoOperacionYMoneda","IdTipoOperacion","IdMoneda","IdEstado" )  
select max("IdTipoOperacionYMoneda")+1,'COMIIF','CLP','ACT' from "BSCL.Plataforma.BD::INS.TipoOperacionYMoneda";
--VENIIF
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionYMoneda" ( "IdTipoOperacionYMoneda","IdTipoOperacion","IdMoneda","IdEstado" )  
select max("IdTipoOperacionYMoneda")+1,'VENIIF','CLP','ACT' from "BSCL.Plataforma.BD::INS.TipoOperacionYMoneda";





-- ** LIBREACION  Socio de Negocio / Autorizaciones (Inicio) **

 ---ENROLAMIENTO SOCIO DE NEGOCIO
SET SCHEMA "BSCL";
alter table "BSCL.SocioNegocio.BD::PRO.Proveedor" add ( "IdAreaProveedor"	BIGINT	DEFAULT 0,
														"FechaIngreso" 		SECONDDATE  DEFAULT '0001-01-01 00:00:00' NOT NULL ,
														"FechaCreacion" 	SECONDDATE  DEFAULT '0001-01-01 00:00:00' NOT NULL ,
														"FechaModificacion"	SECONDDATE  DEFAULT '0001-01-01 00:00:00' NOT NULL , 
														"EsEnrolado" 		NVARCHAR(1) DEFAULT 'N' NOT NULL);


SET SCHEMA "BSCL";
alter table "BSCL.SocioNegocio.BD::SOC.Enrolamiento" add ("PRO_IdAreaProveedor" BIGINT		DEFAULT 0);
														 

SET SCHEMA "BSCL";														 
														 
drop type "BSCL.SocioNegocio.BD.Tipos::SOC.ConsultarEnrolamiento";													 

CREATE TYPE "BSCL.SocioNegocio.BD.Tipos::SOC.ConsultarEnrolamiento" AS TABLE (
"IdEnrolamiento"			BIGINT,
"IdEmpresa" 				BIGINT,
"IdUsuario" 				BIGINT,
"IdTipoEntidad" 			NVARCHAR(100),
"IdEntidadEnrolada" 		BIGINT,
"IdTipoDocumentoIdentidad"  NVARCHAR(1),
"DocumentoIdentidad" 		NVARCHAR(40),
"Tipo" 						NVARCHAR(2),
"Nombres" 					NVARCHAR(100),
"ApellidoPaterno" 			NVARCHAR(100),
"ApellidoMaterno" 			NVARCHAR(100),
"RazonSocial" 				NVARCHAR(100),
"IdPaisNacimiento" 			BIGINT,
"FechaNacimiento" 			DATE,
"Sexo" 						NVARCHAR(2),
"EstadoCivil" 				NVARCHAR(2),
"IdCiudad" 					BIGINT,
"Telefono" 					NVARCHAR(100),
"Email" 					NVARCHAR(100),
"Direccion" 				NVARCHAR(100),
"FechaCreacion" 			SECONDDATE,
"FechaModificacion" 		SECONDDATE,
"CLI_IdCategoriaInversionista" NVARCHAR(40),
"POR_IdPerfilComercial" 	NVARCHAR(40), 
"EMP_CodigoEmpleado"			NVARCHAR(40),
"EMP_CodigoTipoCargoEmpleado"	NVARCHAR(3),
"EMP_IdEmpleadoSupervisor"		BIGINT,
"EMP_IdSucursal"				BIGINT,
"EMP_FechaInicioCargo"			DATE,
"EMP_FechaTerminoCargo" 		DATE,
"CNT_Codigo" 					NVARCHAR(40),
"CNT_Emisor" 					NVARCHAR(1),
"CNT_Depositos"					NVARCHAR(1),
"CNT_Bolsa" 					NVARCHAR(1),
"CNT_Corredora" 				NVARCHAR(1),
"CNT_CamaraDeCompensacion" 		NVARCHAR(1),
"CNT_Banco" 					NVARCHAR(1),
"PRO_IdAreaProveedor"			BIGINT
 );														 

 
--Insercion de datos  
SET SCHEMA "BSCL";
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(117,'BSCL.SocioNegocio.BD.Servicios::PRO.CrearProveedor',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(118,'BSCL.SocioNegocio.BD.Servicios::PRO.ModificarProveedorDatosGenerales',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(119,'BSCL.SocioNegocio.BD.Servicios::PRO.ModificarEstadoProveedor',0,'P','');
 
 ---EVENTOS DE AUTORIZACION
 -- Insercion de Datos
SET SCHEMA "BSCL";
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('ENRPRO',1,'SOC','AUT_PRO_ENRO','Enrolamiento de Proveedor',1,'S','' ,'' ,0,0,'VIG');
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('MODPRO',1,'SOC','AUT_PRO_MOD','Modificación Datos Generales del Proveedor',1,'S','' ,'' ,0,0,'VIG');
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('CAEPRO',1,'SOC','AUT_PRO_CAE','Cambio de Estado Proveedor',1,'S','' ,'' ,0,0,'VIG');
 
SET SCHEMA "BSCL";
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('ENRPRO','EP','SOC','ENROLAMIENTO DE PROVEEDOR','VIG'); 
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('MODPRO','MP','SOC','MODIFICACION DE PROVEEDOR','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('CAEPRO','MP','SOC','CAMBIO DE ESTADO PROVEEDOR','VIG');
 
 
SET SCHEMA "BSCL";
INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio" ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad" )  VALUES(21,'ACT','PROVEEDOR');
INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio" ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad" )  VALUES(22,'BLO','PROVEEDOR');
 
 --Creacion de secuencia
SET SCHEMA "BSCL";
DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias::TipoAtributo"	CASCADE;

CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::TipoAtributo"
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
       
SET SCHEMA "BSCL";
DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias::Atributo"	CASCADE;

CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::Atributo"
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
       
       

SET SCHEMA "BSCL";
DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias::DetalleAtributo"	CASCADE;       
CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::DetalleAtributo"
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
                     
       
       
 --Modificacion de estructura
SET SCHEMA "BSCL"; --Agregar campo
alter table "BSCL.Plataforma.BD::PLA.Atributo" add ("IdTipoAtributo"   BIGINT  DEFAULT 0  NOT NULL);

SET SCHEMA "BSCL"; --Renombrar campo
RENAME COLUMN "BSCL.Plataforma.BD::PLA.Atributo"."IdEstadoAutorizado" TO "IdEstadoAutorizacion";

SET SCHEMA "BSCL"; --Modificar campo
ALTER TABLE "BSCL.Plataforma.BD::AUT.Evento" ALTER ("NumeroOperacionConcepto" NVARCHAR(40)    default ''      not null);

SET SCHEMA "BSCL"; --Modificar campo
ALTER TABLE "BSCL.Plataforma.BD::PLA.SolicitudCambio" ALTER ("IdEntidadOrigen" NVARCHAR(40)    default ''      not null);


SET SCHEMA "BSCL"; --Agregar campo
alter table "BSCL.Plataforma.BD::PLA.TipoAtributo" add ("IdEstado" NVARCHAR(3) DEFAULT 'ACT' NOT NULL);
SET SCHEMA "BSCL"; --borrar campo
alter table "BSCL.Plataforma.BD::PLA.TipoAtributo"	Drop  ("Comportamiento");	


SET SCHEMA "BSCL";
UPDATE "BSCL.Plataforma.BD::PLA.Servicio" SET "RutaServicio"= 'BSCL.Plataforma.BD.Servicios::ATR.ModificarAtributo' WHERE  "IdServicio"=65

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(122,'BSCL.Plataforma.BD.Servicios::ATR.AgregarTipoAtributo',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(123,'BSCL.Plataforma.BD.Servicios::ATR.ModificarTipoAtributo',0,'P','');



---tesoreria
drop table "BSCL.Tesoreria.BD::TES.IngresoEgresoFormaDePago" cascade;
drop table "BSCL.Tesoreria.BD::TES.IngresoEgreso" cascade;
drop table "BSCL.Tesoreria.BD::TES.Comprobante" cascade;
drop table "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" cascade;
drop table "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" cascade;


CREATE COLUMN TABLE "BSCL.Tesoreria.BD::TES.IngresoEgreso" (
 "IdIngresoEgreso"							BIGINT 			NOT NULL,
 "IdEmpresa" 								BIGINT 			NOT NULL,
 "IdModulo"									NVARCHAR(3)		NOT NULL,
 "IdUsuario" 								BIGINT 			NOT NULL,
 "IdTipoMovimiento"                 		NVARCHAR(6)     NOT NULL,  -- MOVING: Ingreso   MOVEGR: Egreso
 "IdConcepto" 								NVARCHAR(6) 	NOT NULL,
 "IdCaja"									BIGINT 			NOT NULL,
 "IdMoneda" 								NVARCHAR(3) 	NOT NULL,
 "Monto" 									DECIMAL(26,8) 	NOT NULL, 
 "FechaLiquidacion" 						DATE 		    NOT NULL, 
 "FechaIngresoRegistro"             		TIMESTAMP       NOT NULL,
 "FechaMovimiento"                 	 		DATE            NOT NULL,
 "IdTipoPersona"      						NVARCHAR(3) 	NOT NULL,   -- Es el IdTipoSocioDeNegocio (CLI / EMP / PRO / OPE)   
 "IdPersona"								BIGINT 			NOT NULL,
 "NombrePersona"							NVARCHAR(300) 	NOT NULL, 
 "IdTipoDocumentoIdentidad" 	    		NVARCHAR(1) 	NOT NULL,
 "DocumentoIdentidad" 		        		NVARCHAR(40)	NOT NULL,
 "SecuenciaAgrupacion" 						NVARCHAR(40)  	NOT NULL DEFAULT '',
 "Glosa" 									NVARCHAR(260) 	NOT NULL DEFAULT '',
 "IdCliente" 								BIGINT  		NOT NULL DEFAULT 0,  --No necesariamente corresponde al IdPersona registrado
 "NombreCliente" 							NVARCHAR(300) 	NOT NULL DEFAULT '', --No necesariamente corresponde al IdPersona registrado
 "IdPortafolio" 							BIGINT  		NOT NULL DEFAULT 0,  --No necesariamente corresponde al IdPersona registrado
 "NombrePortafolio" 						NVARCHAR(300) 	NOT NULL DEFAULT '', --No necesariamente corresponde al IdPersona registrado
 "IdAgente" 								BIGINT  		NOT NULL DEFAULT 0,  --No necesariamente corresponde al IdPersona registrado
 "IdSucursal" 								BIGINT  		NOT NULL DEFAULT 0,  --No necesariamente corresponde al IdPersona registrado
 "IdCorredoraContraparte" 					BIGINT  		NOT NULL DEFAULT 0,  --No necesariamente corresponde al IdPersona registrado
 "NombreCorredora"							NVARCHAR(300) 	NOT NULL DEFAULT '', --No necesariamente corresponde al IdPersona registrado
 "IdEmpleado" 								BIGINT  		NOT NULL DEFAULT 0,  --No necesariamente corresponde al IdPersona registrado
 "NombreEmpleado"							NVARCHAR(300) 	NOT NULL DEFAULT '', --No necesariamente corresponde al IdPersona registrado
 "IdProveedor" 								BIGINT  		NOT NULL DEFAULT 0,  --No necesariamente corresponde al IdPersona registrado
 "NombreProveedor"							NVARCHAR(300) 	NOT NULL DEFAULT '', --No necesariamente corresponde al IdPersona registrado
 "IdTipoComprobanteFacturacion"				NVARCHAR(3) 	NOT NULL DEFAULT '',
 "IdComprobanteFacturacion"					BIGINT  		NOT NULL DEFAULT 0,
 "FechaComprobanteFacturacion"				TIMESTAMP       NOT NULL DEFAULT '0001-01-01 00:00:00', 
 "FechaLiquidacionComprobanteFacturacion"	SECONDDATE 		NOT NULL DEFAULT '0001-01-01 00:00:00',
 "IdTipoProducto" 							NVARCHAR(4) 	NOT NULL DEFAULT '',
 "IdTipoOperacion" 							NVARCHAR(6) 	NOT NULL DEFAULT '', 
 "IdBolsa" 									NVARCHAR(3) 	NOT NULL DEFAULT '0',
 "FolioTransaccion" 						BIGINT  		NOT NULL DEFAULT 0,
 "FechaTransaccion" 						SECONDDATE 		NOT NULL DEFAULT '0001-01-01 00:00:00',
 "IdMonedaOrigen" 							NVARCHAR(3) 	NOT NULL DEFAULT '',
 "MontoOrigen" 								DECIMAL(26,8) 	NOT NULL DEFAULT 0,
 "FechaParidad" 							SECONDDATE 		NOT NULL DEFAULT '0001-01-01 00:00:00',
 "IdTipoReajuste" 							NVARCHAR(2) 	NOT NULL DEFAULT '',
 "ValorParidad" 							DECIMAL(26,8) 	NOT NULL DEFAULT 0,
 "IdCartera" 								BIGINT  		NOT NULL DEFAULT 0,
 "IdInstrumento" 							BIGINT  		NOT NULL DEFAULT 0,
 "IdTipoInstrumento" 						NVARCHAR(3) 	NOT NULL DEFAULT '',
 "IdClasificacionTipoInstrumento" 			NVARCHAR(3) 	NOT NULL DEFAULT '', 
 "EsBilateral"								NVARCHAR(1) 	NOT NULL DEFAULT 'N', --asociado al cleaning
 "EsTercero"								NVARCHAR(1) 	NOT NULL DEFAULT 'N', --S: la persona es distinta a quién realiza el pago o cobro
 "EsIngresoAutomatico"						NVARCHAR(1) 	NOT NULL DEFAULT 'N', --S: ingreso automático N: ingreso manual
 "AgruparEnComprobante"             		NVARCHAR(1) 	NOT NULL DEFAULT 'S', --S: permite agrupar varios mov. en un comprobante
 "SeparaPorTipoMovimiento"					NVARCHAR(1) 	NOT NULL DEFAULT 'N', --S: permite agrupar mov de Ingreso o Egreso 
 "IdUsuarioBloqueo" 						BIGINT 			NOT NULL DEFAULT 0,
 "FechaHoraBloqueo" 						SECONDDATE 		NOT NULL DEFAULT '0001-01-01 00:00:00',
 "IdCarga" 									BIGINT 			NOT NULL DEFAULT 0,  
 "IdEstado"                   				NVARCHAR(3) 	NOT NULL,
PRIMARY KEY ("IdIngresoEgreso") );


CREATE COLUMN TABLE "BSCL.Tesoreria.BD::TES.IngresoEgresoFormaDePago" (
 "IdIngresoEgresoFormaDePago" 		BIGINT  		NOT NULL,
 "IdIngresoEgreso" 					BIGINT  		NOT NULL,
 "IdTipoMovimiento"                 NVARCHAR(6)     NOT NULL,  -- MOVING: Ingreso   MOVEGR: Egreso
 "SentidoPago"                      NVARCHAR(1)     NOT NULL,  -- C: Corredora      S: Socio Negocio
 "IdMedioDePago" 					NVARCHAR(3) 	DEFAULT '',
 "NombreMedioDePago" 				NVARCHAR(100) 	DEFAULT '', 
 "IdModalidadPago"                  NVARCHAR(6)     DEFAULT '',  -- MODCAJ: Por Caja   MODDEP: Deósito en Cta. Cte
 "NombreModalidadDePago" 			NVARCHAR(100) 	DEFAULT '',
 "IdCuentaBancaria" 				BIGINT  		DEFAULT 0, -- Cuenta del cliente, tercero o corredora,
 "IdTipoCuentaBancaria"  	        NVARCHAR(4)     DEFAULT '',
 "NombreTipoCuentaBancaria"         NVARCHAR(100)   DEFAULT '',  
 "NombreCuentaBancaria" 			NVARCHAR(100) 	DEFAULT '', 
 "NumeroCuentaBancaria"   	        NVARCHAR(100)  	DEFAULT '',
 "IdTarjetaBancaria" 				BIGINT  		DEFAULT 0,
 "IdTipoTarjetaBancaria" 			NVARCHAR(40) 	DEFAULT '',
 "NombreTipoTarjetaBancaria" 		NVARCHAR(100) 	DEFAULT '',
 "NumeroTarjetaBancaria"   	        NVARCHAR(100)  	DEFAULT '',
 "FechaVctoTarjetaBancaria" 		SECONDDATE		DEFAULT '0001-01-01',
 "NumeroCheque"   	        		NVARCHAR(100)  	DEFAULT '',
 "FechaVctoCheque" 					SECONDDATE		DEFAULT '0001-01-01',
 "IdBanco" 							BIGINT  		DEFAULT 0, 
 "NombreBanco" 						NVARCHAR(100) 	DEFAULT '',
 "Sucursal" 						NVARCHAR(250) 	DEFAULT '',
 "IdMoneda" 						NVARCHAR(3) 	NOT NULL,
 "Monto" 							DECIMAL(26,8)  	DEFAULT 0,
 "FechaIngresoRegistro" 			SECONDDATE 		NOT NULL,
 "IdEstado" 						NVARCHAR(3) 	NOT NULL, 
PRIMARY KEY ("IdIngresoEgresoFormaDePago") );


CREATE COLUMN TABLE "BSCL.Tesoreria.BD::TES.Comprobante" (
"IdComprobante"		                BIGINT 			NOT NULL,
"IdEmpresa" 						BIGINT 			NOT NULL,
"IdUsuario" 						BIGINT 			NOT NULL,
"IdTipoMovimiento"				    NVARCHAR(6)		NOT NULL, -- MOVING: Ingreso   MOVEGR: Egreso
"IdConcepto" 						NVARCHAR(6) 	NOT NULL,
"IdCaja"							BIGINT 			NOT NULL,
"IdMoneda" 							NVARCHAR(3) 	NOT NULL,
"Monto" 							DECIMAL(26,8) 	NOT NULL,
"FechaLiquidacion" 					DATE 		    NOT NULL,
"FechaIngresoRegistro"             	TIMESTAMP       NOT NULL,
"FechaMovimiento"                 	DATE            NOT NULL,
"IdTipoPersona"      				NVARCHAR(3) 	NOT NULL, -- Es el IdTipoSocioDeNegocio (CLI / EMP / PRO / OPE)   
"IdPersona"							BIGINT 			NOT NULL,
"NombrePersona"						NVARCHAR(300) 	NOT NULL, 
"IdTipoDocumentoIdentidad" 	    	NVARCHAR(1) 	NOT NULL,
"DocumentoIdentidad" 		        NVARCHAR(40)	NOT NULL,
"SecuenciaAgrupacion" 				NVARCHAR(40)  	NOT NULL DEFAULT '',
"Glosa" 							NVARCHAR(260) 	NOT NULL DEFAULT '',
"EsBilateral"						NVARCHAR(1) 	NOT NULL DEFAULT 'N', --asociado al cleaning 
"TieneIngresosYEgresos"				NVARCHAR(1) 	NOT NULL DEFAULT 'N', --S: indica que el comprobante posee movimientos de ingreoss o egresos 
"IdUsuarioBloqueo" 					BIGINT 			NOT NULL DEFAULT 0,
"FechaHoraBloqueo" 					TIMESTAMP 		NOT NULL DEFAULT '0001-01-01',
"IdUsuarioLiquida" 					BIGINT 			NOT NULL DEFAULT 0,
"FechaHoraLiquida" 					TIMESTAMP 		NOT NULL DEFAULT '0001-01-01',
"IdCarga" 							BIGINT 			NOT NULL DEFAULT 0, 
"IdEstado"                   		NVARCHAR(3) 	NOT NULL,
PRIMARY KEY ("IdComprobante") );


CREATE COLUMN TABLE "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" (
 "IdComprobanteFormaDePago" 		BIGINT  		NOT NULL,
 "IdComprobante" 					BIGINT  		NOT NULL,
 "IdTipoMovimiento"                 NVARCHAR(6)     NOT NULL,  -- MOVING: Ingreso   MOVEGR: Egreso
 "SentidoPago"                      NVARCHAR(1)     NOT NULL,  -- C: Corredora      S: Socio Negocio
 "IdMedioDePago" 					NVARCHAR(3) 	NOT NULL,
 "NombreMedioDePago" 				NVARCHAR(100) 	DEFAULT '', 
 "IdModalidadPago"                  NVARCHAR(6)     NOT NULL,  -- MODCAJ: Por Caja   MODDEP: Deósito en Cta. Cte
 "NombreModalidadDePago" 			NVARCHAR(100) 	DEFAULT '',
 "IdCuentaBancaria" 				BIGINT  		DEFAULT 0, -- Cuenta del cliente, tercero o corredora
 "IdTipoCuentaBancaria"  	        NVARCHAR(4)     NOT NULL,
 "NombreTipoCuentaBancaria"         NVARCHAR(100)   NOT NULL default '' ,  
 "NombreCuentaBancaria" 			NVARCHAR(100) 	DEFAULT '', 
 "NumeroCuentaBancaria"   	        NVARCHAR(100)  	NOT NULL default '',
 "IdTarjetaBancaria" 				BIGINT  		DEFAULT 0,
 "IdTipoTarjetaBancaria" 			NVARCHAR(40) 	DEFAULT '',
 "NombreTipoTarjetaBancaria" 		NVARCHAR(100) 	DEFAULT '',
 "NumeroTarjetaBancaria"   	        NVARCHAR(100)  	NOT NULL default '',
 "FechaVctoTarjetaBancaria" 		SECONDDATE		DEFAULT '0001-01-01',
 "NumeroCheque"   	        		NVARCHAR(100)  	NOT NULL default '',
 "FechaVctoCheque" 					SECONDDATE		DEFAULT '0001-01-01', 
 "IdBanco" 							BIGINT  		DEFAULT 0, 
 "NombreBanco" 						NVARCHAR(100) 	DEFAULT '',
 "Sucursal" 						NVARCHAR(250) 	DEFAULT '', 
 "IdMoneda" 						NVARCHAR(3) 	NOT NULL,
 "Monto" 							DECIMAL(26,8)  	DEFAULT 0,
 "FechaIngresoRegistro" 			SECONDDATE 		NOT NULL,
 "IdUsuarioCertificador"            BIGINT          DEFAULT 0,
 "FechaCertificacion"               TIMESTAMP       DEFAULT '0001-01-01',
 "IdEstado" 						NVARCHAR(3) 	NOT NULL, 
PRIMARY KEY ("IdComprobanteFormaDePago") ); 


CREATE COLUMN TABLE "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" (
"IdComprobanteDetalle"						BIGINT 			NOT NULL,
"IdComprobante"		                		BIGINT 			NOT NULL,
"IdConcepto" 								NVARCHAR(6) 	NOT NULL,
"NroLinea"                          		INTEGER         NOT NULL,
"IdMoneda" 									NVARCHAR(3) 	NOT NULL,
"Monto" 									DECIMAL(26,8) 	NOT NULL,
"SecuenciaAgrupacion" 						NVARCHAR(40)  	NOT NULL DEFAULT '',
"EsTercero"									NVARCHAR(1) 	NOT NULL DEFAULT 'N', --S: si la persona es distinta a quién realiza el pago o cobro
"IdCliente" 								BIGINT  		NOT NULL DEFAULT 0,  --No necesariamente corresponde al IdPersona registrado
"NombreCliente" 							NVARCHAR(300) 	NOT NULL DEFAULT '', --No necesariamente corresponde al IdPersona registrado
"IdPortafolio" 								BIGINT  		NOT NULL DEFAULT 0,  --No necesariamente corresponde al IdPersona registrado
"NombrePortafolio" 							NVARCHAR(300) 	NOT NULL DEFAULT '', --No necesariamente corresponde al IdPersona registrado
"IdAgente" 									BIGINT  		NOT NULL DEFAULT 0,  --No necesariamente corresponde al IdPersona registrado
"IdSucursal" 								BIGINT  		NOT NULL DEFAULT 0,  --No necesariamente corresponde al IdPersona registrado
"IdCorredoraContraparte" 					BIGINT  		NOT NULL DEFAULT 0,  --No necesariamente corresponde al IdPersona registrado
"NombreCorredora"							NVARCHAR(300) 	NOT NULL DEFAULT '', --No necesariamente corresponde al IdPersona registrado
"IdEmpleado" 								BIGINT  		NOT NULL DEFAULT 0,  --No necesariamente corresponde al IdPersona registrado
"NombreEmpleado"							NVARCHAR(300) 	NOT NULL DEFAULT '', --No necesariamente corresponde al IdPersona registrado
"IdProveedor" 								BIGINT  		NOT NULL DEFAULT 0,  --No necesariamente corresponde al IdPersona registrado
"NombreProveedor"							NVARCHAR(300) 	NOT NULL DEFAULT '', --No necesariamente corresponde al IdPersona registrado
"IdTipoComprobanteFacturacion"				NVARCHAR(3) 	NOT NULL DEFAULT '',
"IdComprobanteFacturacion"					BIGINT  		NOT NULL DEFAULT 0,
"FechaComprobanteFacturacion"				TIMESTAMP       NOT NULL DEFAULT '0001-01-01 00:00:00', 
"FechaLiquidacionComprobanteFacturacion"	SECONDDATE 		NOT NULL DEFAULT '0001-01-01 00:00:00',
"IdTipoProducto" 							NVARCHAR(4) 	NOT NULL DEFAULT '',
"IdTipoOperacion" 							NVARCHAR(6) 	NOT NULL DEFAULT '',
"IdBolsa"                           		NVARCHAR(3)     NOT NULL DEFAULT '',
"FolioTransaccion" 							BIGINT  		NOT NULL DEFAULT 0,
"FechaTransaccion" 							SECONDDATE 		NOT NULL DEFAULT '0001-01-01 00:00:00',
"IdMonedaOrigen" 							NVARCHAR(3) 	NOT NULL DEFAULT '',
"MontoOrigen" 								DECIMAL(26,8) 	NOT NULL DEFAULT 0,
"FechaParidad" 								SECONDDATE 		NOT NULL DEFAULT '0001-01-01 00:00:00',
"IdTipoReajuste" 							NVARCHAR(2) 	NOT NULL DEFAULT '',
"ValorParidad" 								DECIMAL(26,8) 	NOT NULL DEFAULT 0,
"IdCartera" 								BIGINT  		NOT NULL DEFAULT 0,
"IdInstrumento" 							BIGINT  		NOT NULL DEFAULT 0,
"IdTipoInstrumento" 						NVARCHAR(3) 	NOT NULL DEFAULT '',
"IdClasificacionTipoInstrumento" 			NVARCHAR(3) 	NOT NULL DEFAULT '',
"IdCarga" 									BIGINT 			NOT NULL DEFAULT 0,
"IdEstado" 									NVARCHAR(3) 	NOT NULL, 
PRIMARY KEY ("IdComprobanteDetalle") );

--Cuenta Inversion
RENAME COLUMN "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento"."IdDetalleComprobante" TO "IdComprobanteDetalle";

--Conceptos CTA
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('CAEGTE','CA','TES','COMPROBANTE EGRESO TESORERIA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('ABIGTE','AB','TES','COMPROBANTE INGRESO TESORERIA','VIG');

INSERT INTO "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion"
VALUES (50,'CAEGTE','CTA.CargoAbonoMovimiento','TES','','','');
INSERT INTO "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion"
VALUES (51,'ABIGTE','CTA.CargoAbonoMovimiento','TES','','','');

--- PERFIL COMERCIAL


SET SCHEMA "BSCL";
UPDATE "BSCL.Plataforma.BD::PLA.Servicio" SET "RutaServicio"= 'BSCL.Plataforma.BD.Servicios::COM.ModificarPerfilCobro' WHERE  "IdServicio"=87



-- ** LIBREACION  Socio de Negocio / Autorizaciones (Termino) **

/*** objetos q estan demás***/
DROP PROCEDURE "BSCL.Custodia.BD.Servicios.Vistas::CUS.ConsultarInformeCoberturaResumen";
DROP VIEW "BSCL"."BSCL.Custodia.BD::GTA.Servicios.ConsultarGarantias";


/***Facturacion**/
INSERT INTO "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion"
VALUES (49,'CATCRF','CTA.CargoAbonoMovimiento','FAC','INRF','COMRF','TOTAL');

INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('CATCRF','CA','CTA','TOTAL FACTURA COMPRA RF','VIG'); 

INSERT INTO "BSCL.Facturacion.BD::FAC.EstadoFacturacion" ( "IdEstadoFacturacion","IdEstado","NombreEntidad" )  
VALUES(22,'ACT','FAC.MovimientoComprobanteFormaDePago');

INSERT INTO "BSCL.Facturacion.BD::FAC.EstadoFacturacion" ( "IdEstadoFacturacion","IdEstado","NombreEntidad" )  
VALUES(23,'ANU','FAC.MovimientoComprobanteFormaDePago');

INSERT INTO  "BSCL"."BSCL.Tesoreria.BD::TES.CajaMoneda"
VALUES(5,1,1,'EUR','MOVING','S','ACT')

alter table "BSCL.Facturacion.BD::FAC.MovimientoComprobanteFormaDePago"  
 add ("IdMoneda" NVARCHAR(3) DEFAULT '');
 
alter table "BSCL.Facturacion.BD::FAC.ComprobanteFormaDePago"  
 add ("IdMoneda" NVARCHAR(3) DEFAULT ''); 

alter table "BSCL.Facturacion.BD::FAC.MovimientoComprobante"
 add ("IdMonedaMovimientoComprobante" NVARCHAR(3) DEFAULT '');
 
alter table "BSCL.Facturacion.BD::FAC.MovimientoComprobante"
 add ("MontoMovimientoComprobante" DECIMAL(26,8) DEFAULT 0);
 
alter table "BSCL.Facturacion.BD::FAC.MovimientoComprobante"
 add ("MontoLiquidacion" DECIMAL(26,8) DEFAULT 0);  
 
alter table "BSCL.Facturacion.BD::FAC.MovimientoComprobante"
 add ("ActualizarCustodiaRelacionado" NVARCHAR(1) DEFAULT 'N' NOT NULL);  
 
 alter table "BSCL.Facturacion.BD::FAC.MovimientoComprobante"
 add ("IdConceptoMovimientoRelacionado" NVARCHAR(6) DEFAULT ''); 
 
alter table "BSCL.Facturacion.BD::FAC.MovimientoComprobanteCobro"
 add ("IdMonedaMovimientoComprobanteCobro" NVARCHAR(3) DEFAULT ''); 
 
alter table "BSCL.Facturacion.BD::FAC.MovimientoComprobanteCobro"
 add ("IdMonedaLiquidacion" NVARCHAR(3) DEFAULT ''); 
 
 alter table "BSCL.Facturacion.BD::FAC.MovimientoComprobanteCobro"
 add ("MontoMovimientoComprobanteCobro" DECIMAL(26,8) DEFAULT 0); 
 
alter table "BSCL.Facturacion.BD::FAC.MovimientoComprobanteCobro"
 add ("MontoLiquidacion" DECIMAL(26,8) DEFAULT 0); 
 
 

drop table "BSCL.Facturacion.BD::FAC.MovimientoComprobanteFormaDePago" cascade;
drop table "BSCL.Facturacion.BD::FAC.ComprobanteFormaDePago" cascade;

create column table "BSCL.Facturacion.BD::FAC.ComprobanteFormaDePago" (
   "IdComprobanteFormaDePago"  BIGINT NOT NULL,
   "IdComprobante"             BIGINT NOT NULL,
   "IdTipoMovimiento"          NVARCHAR(6) NOT NULL,   
   "IdMedioDePago"             NVARCHAR(3) NOT NULL,
   "CodigoMedioDePago"         NVARCHAR(3) DEFAULT '',
   "NombreMedioDePago"         NVARCHAR(100) DEFAULT '',
   "NumeroCheque"              NVARCHAR(40) DEFAULT '0',
   "IdCuentaBancaria"          BIGINT DEFAULT 0,
   "NombreCuentaBancaria"      NVARCHAR(100) DEFAULT '',   
   "IdTipoCuentaBancaria"      NVARCHAR(4) DEFAULT '',
   "NombreTipoCuentaBancaria"  NVARCHAR(100) DEFAULT '',
   "NumeroCuentaBancaria"      BIGINT DEFAULT 0,
   "IdTarjetaBancaria"         BIGINT DEFAULT 0,   
   "IdTipoTarjetaBancaria"     NVARCHAR(4) DEFAULT '',
   "NombreTipoTarjetaBancaria" NVARCHAR(40) DEFAULT '',
   "NumeroTarjetaBancaria"     BIGINT DEFAULT 0,
   "IdModalidadDePago"         NVARCHAR(6) DEFAULT '', 
   "NombreModalidadDePago"     NVARCHAR(100) DEFAULT '', 
   "IdBanco"                   BIGINT DEFAULT 0,
   "CodigoBanco"               NVARCHAR(40) DEFAULT '',
   "NombreBanco"               NVARCHAR(100) DEFAULT '',   
   "Sucursal"                  NVARCHAR(40) DEFAULT '',
   "IdMoneda"                  NVARCHAR(3) DEFAULT '',
   "FechaVctoDocumento"        DATE DEFAULT '0001-01-01',
   "Monto"                     DECIMAL(26,8) NOT NULL,
   "IdEstado"                  NVARCHAR(3) NOT NULL,
   "FechaIngresoRegistro"      TIMESTAMP NOT NULL, 
 primary key ("IdComprobanteFormaDePago")
);

create column table "BSCL.Facturacion.BD::FAC.MovimientoComprobanteFormaDePago" (
   "IdMovimientoComprobanteFormaDePago"   BIGINT NOT NULL,
   "IdMovimientoComprobante"   BIGINT NOT NULL,
   "IdMedioDePago"             NVARCHAR(3) NOT NULL,
   "CodigoMedioDePago"         NVARCHAR(3) DEFAULT '',
   "NombreMedioDePago"         NVARCHAR(100) DEFAULT '',
   "NumeroCheque"              NVARCHAR(40) DEFAULT '0',
   "IdCuentaBancaria"          BIGINT DEFAULT 0,
   "NombreCuentaBancaria"      NVARCHAR(100) DEFAULT '',   
   "IdTipoCuentaBancaria"      NVARCHAR(4) DEFAULT '',
   "NombreTipoCuentaBancaria"  NVARCHAR(100) DEFAULT '',
   "NumeroCuentaBancaria"      BIGINT DEFAULT 0,
   "IdTarjetaBancaria"         BIGINT DEFAULT 0,   
   "IdTipoTarjetaBancaria"     NVARCHAR(4) DEFAULT '',
   "NombreTipoTarjetaBancaria" NVARCHAR(40) DEFAULT '',
   "NumeroTarjetaBancaria"     BIGINT DEFAULT 0,
   "IdModalidadDePago"         NVARCHAR(6) DEFAULT '', 
   "NombreModalidadDePago"     NVARCHAR(100) DEFAULT '', 
   "IdBanco"                   BIGINT DEFAULT 0,
   "CodigoBanco"               NVARCHAR(40) DEFAULT '',
   "NombreBanco"               NVARCHAR(100) DEFAULT '',   
   "Sucursal"                  NVARCHAR(40) DEFAULT '',
   "IdMoneda"                  NVARCHAR(3) DEFAULT '',
   "FechaVctoDocumento"        DATE DEFAULT '0001-01-01',
   "Monto"                     DECIMAL(26,8) NOT NULL,
   "IdEstado"                  NVARCHAR(3) NOT NULL,
   "FechaIngresoRegistro"      TIMESTAMP NOT NULL,
 primary key ("IdMovimientoComprobanteFormaDePago")
);



-- Condiciones Operativas
drop table "BSCL.Plataforma.BD::COM.OperacionUsuario"	 					cascade;
drop table "BSCL.Plataforma.BD::COM.TipoEtapa"								cascade;
drop table "BSCL.Plataforma.BD::COM.EtapaOperacion"							cascade;
drop table "BSCL.Plataforma.BD::COM.EtapaCondicionOperativa" 				cascade;
drop table "BSCL.Plataforma.BD::COM.CondicionOperativa" 					cascade;
--
drop table "BSCL.Plataforma.BD::COM.EtapaGrupoCondicionesOperativas" 		cascade;
drop table "BSCL.Plataforma.BD::COM.GrupoCondicionesOperativas"				cascade;
drop table "BSCL.Plataforma.BD::COM.DetallePlantillaCondicionesOperativas"	cascade;
drop table "BSCL.Plataforma.BD::COM.PlantillaCondicionesOperativas" 		cascade;


/*****************************
PLANTILLA CONDICIONES OPERATIVAS 
*****************************/

/* Maestros */
create column table "BSCL.Plataforma.BD::COM.OperacionUsuario" (
	"IdOperacionUsuario"						BIGINT				not null,
	"Nombre"									NVARCHAR(100)		not null default '',
	"IdTipoOperacion"							NVARCHAR(6)       	not null default '', -- desde INS->TipoOperacion
	"FechaCreacion"								DATE				not null default '0001-01-01 00:00:00',
	--"IdEmpresa"         						BIGINT              not null default 0,  
	primary key ("IdOperacionUsuario")
);

create column table "BSCL.Plataforma.BD::COM.TipoEtapa" (
	"IdTipoEtapa"								BIGINT        	not null, 
	"Nombre"									NVARCHAR(100)	not null default '', 
	"FechaCreacion"								DATE			not null default '0001-01-01 00:00:00',
	primary key ("IdTipoEtapa")
);

create column table "BSCL.Plataforma.BD::COM.EtapaOperacion" (
	"IdEtapaOperacion"							BIGINT       	not null, 
	"IdOperacionUsuario"						BIGINT			not null default 0,
	"IdTipoEtapa"								BIGINT        	not null default 0, 
	"NombreCorto"								NVARCHAR(40)	not null default '', -- para mostrar en check
	"Nombre"									NVARCHAR(100)	not null default '',
	"FechaCreacion"								DATE			not null default '0001-01-01 00:00:00',
	primary key ("IdEtapaOperacion")
);

create column table "BSCL.Plataforma.BD::COM.TipoComportamientoCondicion" (
	"IdTipoComportamientoCondicion"				NVARCHAR(40)	not null,
	"Nombre"									NVARCHAR(100)	not null default '',
	primary key ("IdTipoComportamientoCondicion")
);

create column table "BSCL.Plataforma.BD::COM.TipoCondicion" (
	"IdTipoCondicion"							BIGINT			not null,
	"CodigoTipoCondicion"						NVARCHAR(40)	not null default '',
	"Nombre"									NVARCHAR(100)	not null default '',
	"RequiereAtributoX"							NVARCHAR(1)		not null default 'N',
	"RequiereAtributoY"							NVARCHAR(1)		not null default 'N',	
	"ComportamientoPorDefecto"					NVARCHAR(40)	not null default '', -- ADVERTENCIA, RECHAZO, AUTORIZACION
	"FechaCreacion"								DATE			not null default '0001-01-01 00:00:00',
	primary key ("IdTipoCondicion")
);

create column table "BSCL.Plataforma.BD::COM.CondicionOperativa" (
	"IdCondicionOperativa"						BIGINT			not null,
	"IdTipoCondicion"							BIGINT			not null default 0,
	--"CodigoTipoCondicion"						NVARCHAR(40)	not null default '',
	"IdOperacionUsuario"						BIGINT   		not null default 0,
	"FechaCreacion"								DATE			not null default '0001-01-01 00:00:00',
	"IdEmpresa"         						BIGINT          not null default 0,
	primary key ("IdCondicionOperativa")
);

create column table "BSCL.Plataforma.BD::COM.EtapaCondicionOperativa" (
	"IdEtapaCondicionOperativa"					BIGINT       	not null, 
	"IdEtapaOperacion"							BIGINT       	not null default 0,
	"IdCondicionOperativa"						BIGINT			not null default 0, 
	"IdEstado"					   				NVARCHAR(3)		not null default 'ACT',
	"FechaCreacion"								DATE			not null default '0001-01-01 00:00:00',
	primary key ("IdEtapaCondicionOperativa")
);

-- Plantilla Condiciones Operativas
create column table "BSCL.Plataforma.BD::COM.PlantillaCondicionesOperativas" (
	"IdPlantillaCondicionesOperativas"			NVARCHAR(40)	not null,
	"Descripcion"								NVARCHAR(100)	not null default '',
	"IdCategoriaInversionista"					NVARCHAR(40)	not null default '0',
	"EsPerfilPersonalizado"						NVARCHAR(1)		not null default 'N',
	"TipoEntidadReferencia"						NVARCHAR(40)	not null default '',
	"IdReferencia"								BIGINT			not null default 0,
	"FechaCreacion"								DATE			not null default '0001-01-01 00:00:00',
	"FechaModificacion"							DATE			not null default '0001-01-01 00:00:00',
	"IdEstado"					   				NVARCHAR(3)		not null default 'ACT',
	"PorDefecto"								NVARCHAR(1)		not null default '',
	"IdEmpresa"									BIGINT			not null default 0,
	primary key ("IdPlantillaCondicionesOperativas")
);

create column table "BSCL.Plataforma.BD::COM.DetallePlantillaCondicionesOperativas" (
	"IdDetallePlantillaCondicionesOperativas"	BIGINT          not null, 
	"IdPlantillaCondicionesOperativas"			NVARCHAR(40)	not null default '',
	"IdCanalIngreso"							VARCHAR(3)      not null default '',
	"IdMoneda"									NVARCHAR(3)    	not null default '',
	"IdOperacionUsuario"						NVARCHAR(6)   	not null default '', 
	"FechaCreacion"								DATE			not null default '0001-01-01 00:00:00',
	"IdEstado"					   				NVARCHAR(3)   	not null default 'ACT',
	primary key ("IdDetallePlantillaCondicionesOperativas")
);

create column table "BSCL.Plataforma.BD::COM.GrupoCondicionesOperativas" (
	"IdGrupoCondicionesOperativas"				BIGINT          not null, 
	"IdDetallePlantillaCondicionesOperativas"	NVARCHAR(40)	not null default '',
	"IdCondicionOperativa"						BIGINT			not null default 0,					
	"AtributoX"									NVARCHAR(500)	not null default '',
	"AtributoY"									NVARCHAR(500)	not null default '',	
	"IdComportamiento"							NVARCHAR(40)	not null default '', -- ADVERTENCIA, RECHAZO, AUTORIZACION
	"FechaCreacion"								DATE			not null default '0001-01-01 00:00:00',
	"IdEstado"					   				NVARCHAR(3)   	not null default 'ACT',
	primary key ("IdGrupoCondicionesOperativas")
);

create column table "BSCL.Plataforma.BD::COM.EtapaGrupoCondicionesOperativas" (
	"IdEtapaGrupoCondicionesOperativas"			BIGINT          not null, 
	"IdGrupoCondicionesOperativas"				BIGINT			not null default 0,
	"IdEtapaCondicionOperativa"					BIGINT			not null default 0,
	"FechaCreacion"								DATE			not null default '0001-01-01 00:00:00',
	"IdEstado"					   				NVARCHAR(3)   	not null default 'ACT',
	primary key ("IdEtapaGrupoCondicionesOperativas")
);


/*==============================================================*/
/* SECUENCIAS:                                                  */
/*==============================================================*/

drop sequence "BSCL.Plataforma.BD.Secuencias::CondicionOperativa";
create sequence "BSCL.Plataforma.BD.Secuencias::CondicionOperativa" increment by 1 start with 1000 minvalue 1000 maxvalue 4611686018427387903 no cycle ;

drop sequence "BSCL.Plataforma.BD.Secuencias::DetallePlantillaCondicionesOperativas";
create sequence "BSCL.Plataforma.BD.Secuencias::DetallePlantillaCondicionesOperativas" increment by 1 start with 1000 minvalue 1000 maxvalue 4611686018427387903 no cycle ;

drop sequence "BSCL.Plataforma.BD.Secuencias::EtapaCondicionOperativa";
create sequence "BSCL.Plataforma.BD.Secuencias::EtapaCondicionOperativa" increment by 1 start with 1 minvalue 1 maxvalue 4611686018427387903 no cycle ;

drop sequence "BSCL.Plataforma.BD.Secuencias::EtapaGrupoCondicionesOperativas";
create sequence "BSCL.Plataforma.BD.Secuencias::EtapaGrupoCondicionesOperativas" increment by 1 start with 1000 minvalue 1000 maxvalue 4611686018427387903 no cycle ;

drop sequence "BSCL.Plataforma.BD.Secuencias::EtapaOperacion";
create sequence "BSCL.Plataforma.BD.Secuencias::EtapaOperacion" increment by 1 start with 1000 minvalue 1000 maxvalue 4611686018427387903 no cycle ;

drop sequence "BSCL.Plataforma.BD.Secuencias::GrupoCondicionesOperativas";
create sequence "BSCL.Plataforma.BD.Secuencias::GrupoCondicionesOperativas" increment by 1 start with 1000 minvalue 1000 maxvalue 4611686018427387903 no cycle ;

drop sequence "BSCL.Plataforma.BD.Secuencias::OperacionUsuario";
create sequence "BSCL.Plataforma.BD.Secuencias::OperacionUsuario" increment by 1 start with 1000 minvalue 1000 maxvalue 4611686018427387903 no cycle ;

drop sequence "BSCL.Plataforma.BD.Secuencias::TipoEtapa";
create sequence "BSCL.Plataforma.BD.Secuencias::TipoEtapa" increment by 1 start with 1000 minvalue 1000 maxvalue 4611686018427387903 no cycle ;


----------------------- DATOS ----------------------

drop sequence "BSCL.Plataforma.BD.Secuencias::CondicionOperativa";
create sequence "BSCL.Plataforma.BD.Secuencias::CondicionOperativa" increment by 1 start with 1000 minvalue 1000 maxvalue 4611686018427387903 no cycle ;

drop sequence "BSCL.Plataforma.BD.Secuencias::DetallePlantillaCondicionesOperativas";
create sequence "BSCL.Plataforma.BD.Secuencias::DetallePlantillaCondicionesOperativas" increment by 1 start with 1000 minvalue 1000 maxvalue 4611686018427387903 no cycle ;

drop sequence "BSCL.Plataforma.BD.Secuencias::EtapaCondicionOperativa";
create sequence "BSCL.Plataforma.BD.Secuencias::EtapaCondicionOperativa" increment by 1 start with 1 minvalue 1 maxvalue 4611686018427387903 no cycle ;

drop sequence "BSCL.Plataforma.BD.Secuencias::EtapaGrupoCondicionesOperativas";
create sequence "BSCL.Plataforma.BD.Secuencias::EtapaGrupoCondicionesOperativas" increment by 1 start with 1000 minvalue 1000 maxvalue 4611686018427387903 no cycle ;

drop sequence "BSCL.Plataforma.BD.Secuencias::EtapaOperacion";
create sequence "BSCL.Plataforma.BD.Secuencias::EtapaOperacion" increment by 1 start with 1000 minvalue 1000 maxvalue 4611686018427387903 no cycle ;

drop sequence "BSCL.Plataforma.BD.Secuencias::GrupoCondicionesOperativas";
create sequence "BSCL.Plataforma.BD.Secuencias::GrupoCondicionesOperativas" increment by 1 start with 1000 minvalue 1000 maxvalue 4611686018427387903 no cycle ;

drop sequence "BSCL.Plataforma.BD.Secuencias::OperacionUsuario";
create sequence "BSCL.Plataforma.BD.Secuencias::OperacionUsuario" increment by 1 start with 1000 minvalue 1000 maxvalue 4611686018427387903 no cycle ;

drop sequence "BSCL.Plataforma.BD.Secuencias::TipoEtapa";
create sequence "BSCL.Plataforma.BD.Secuencias::TipoEtapa" increment by 1 start with 1000 minvalue 1000 maxvalue 4611686018427387903 no cycle ;


-- Condiciones Operativas
DELETE FROM "BSCL.Plataforma.BD::COM.OperacionUsuario";
DELETE FROM "BSCL.Plataforma.BD::COM.TipoEtapa";
DELETE FROM "BSCL.Plataforma.BD::COM.EtapaOperacion";
DELETE FROM "BSCL.Plataforma.BD::COM.EtapaCondicionOperativa";
DELETE FROM "BSCL.Plataforma.BD::COM.CondicionOperativa";
DELETE FROM "BSCL.Plataforma.BD::COM.EtapaGrupoCondicionesOperativas";
DELETE FROM "BSCL.Plataforma.BD::COM.GrupoCondicionesOperativas";
DELETE FROM "BSCL.Plataforma.BD::COM.DetallePlantillaCondicionesOperativas";
DELETE FROM "BSCL.Plataforma.BD::COM.PlantillaCondicionesOperativas";
DELETE FROM "BSCL.Plataforma.BD::COM.TipoComportamientoCondicion";
DELETE FROM "BSCL.Plataforma.BD::COM.TipoCondicion" ;

-- PlantillaCondicionesOperativas
INSERT INTO "BSCL.Plataforma.BD::COM.PlantillaCondicionesOperativas" ("IdPlantillaCondicionesOperativas","Descripcion","IdCategoriaInversionista","IdEmpresa")
VALUES ('PRIMERA_PLANTILLA','Esta es una plantilla de pruebas','ARRIESGADO',1);
-- SELECT * FROM "BSCL.Plataforma.BD::COM.PlantillaCondicionesOperativas"


-- DetallePlantillaCondicionesOperativas
INSERT INTO "BSCL.Plataforma.BD::COM.DetallePlantillaCondicionesOperativas" ("IdDetallePlantillaCondicionesOperativas","IdPlantillaCondicionesOperativas","IdCanalIngreso","IdMoneda","IdOperacionUsuario","FechaCreacion","IdEstado")
VALUES (1,'PRIMERA_PLANTILLA','0','0',1,NOW(),'ACT');
INSERT INTO "BSCL.Plataforma.BD::COM.DetallePlantillaCondicionesOperativas" ("IdDetallePlantillaCondicionesOperativas","IdPlantillaCondicionesOperativas","IdCanalIngreso","IdMoneda","IdOperacionUsuario","FechaCreacion","IdEstado")
VALUES (2,'PRIMERA_PLANTILLA','0','0',2,NOW(),'ACT');
INSERT INTO "BSCL.Plataforma.BD::COM.DetallePlantillaCondicionesOperativas" ("IdDetallePlantillaCondicionesOperativas","IdPlantillaCondicionesOperativas","IdCanalIngreso","IdMoneda","IdOperacionUsuario","FechaCreacion","IdEstado")
VALUES (3,'PRIMERA_PLANTILLA','0','0',3,NOW(),'ACT');
-- SELECT * FROM "BSCL.Plataforma.BD::COM.DetallePlantillaCondicionesOperativas"


-- GrupoCondicionesOperativas
INSERT INTO "BSCL.Plataforma.BD::COM.GrupoCondicionesOperativas" ("IdGrupoCondicionesOperativas","IdDetallePlantillaCondicionesOperativas","IdCondicionOperativa","AtributoX","AtributoY","IdComportamiento","IdEstado","FechaCreacion")
VALUES (1,1,1,'','','AUTORIZACION','ACT',NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.GrupoCondicionesOperativas" ("IdGrupoCondicionesOperativas","IdDetallePlantillaCondicionesOperativas","IdCondicionOperativa","AtributoX","AtributoY","IdComportamiento","IdEstado","FechaCreacion")
VALUES (2,1,1,'','','AUTORIZACION','ACT',NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.GrupoCondicionesOperativas" ("IdGrupoCondicionesOperativas","IdDetallePlantillaCondicionesOperativas","IdCondicionOperativa","AtributoX","AtributoY","IdComportamiento","IdEstado","FechaCreacion")
VALUES (3,1,1,'','','AUTORIZACION','ACT',NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.GrupoCondicionesOperativas" ("IdGrupoCondicionesOperativas","IdDetallePlantillaCondicionesOperativas","IdCondicionOperativa","AtributoX","AtributoY","IdComportamiento","IdEstado","FechaCreacion")
VALUES (4,2,1,'','','AUTORIZACION','ACT',NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.GrupoCondicionesOperativas" ("IdGrupoCondicionesOperativas","IdDetallePlantillaCondicionesOperativas","IdCondicionOperativa","AtributoX","AtributoY","IdComportamiento","IdEstado","FechaCreacion")
VALUES (5,2,2,'','','AUTORIZACION','ACT',NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.GrupoCondicionesOperativas" ("IdGrupoCondicionesOperativas","IdDetallePlantillaCondicionesOperativas","IdCondicionOperativa","AtributoX","AtributoY","IdComportamiento","IdEstado","FechaCreacion")
VALUES (6,2,3,'','','AUTORIZACION','ACT',NOW());
-- SELECT * FROM "BSCL.Plataforma.BD::COM.GrupoCondicionesOperativas"

--OperacionUsuario
INSERT INTO "BSCL.Plataforma.BD::COM.OperacionUsuario" ("IdOperacionUsuario","Nombre","IdTipoOperacion","FechaCreacion")
VALUES (1,'Enrolamiento Cliente','',NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.OperacionUsuario" ("IdOperacionUsuario","Nombre","IdTipoOperacion","FechaCreacion")
VALUES (2,'Enrolamiento Persona','',NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.OperacionUsuario" ("IdOperacionUsuario","Nombre","IdTipoOperacion","FechaCreacion")
VALUES (3,'Compra Acciones RV','',NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.OperacionUsuario" ("IdOperacionUsuario","Nombre","IdTipoOperacion","FechaCreacion")
VALUES (4,'Venta Acciones RV','',NOW());
-- SELECT * FROM "BSCL.Plataforma.BD::COM.OperacionUsuario"


-- Tipo Etapa
INSERT INTO "BSCL.Plataforma.BD::COM.TipoEtapa" ("IdTipoEtapa","Nombre","FechaCreacion")
VALUES (1,'Ingreso',NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.TipoEtapa" ("IdTipoEtapa","Nombre","FechaCreacion")
VALUES (2,'Asignación',NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.TipoEtapa" ("IdTipoEtapa","Nombre","FechaCreacion")
VALUES (3,'Envío a Facturar',NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.TipoEtapa" ("IdTipoEtapa","Nombre","FechaCreacion")
VALUES (4,'Generación comprobante Facturación',NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.TipoEtapa" ("IdTipoEtapa","Nombre","FechaCreacion")
VALUES (5,'Facturación DTE',NOW());
-- SELECT * FROM "BSCL.Plataforma.BD::COM.TipoEtapa"


-- Operación Usuario
INSERT INTO "BSCL.Plataforma.BD::COM.EtapaOperacion" ("IdEtapaOperacion","IdOperacionUsuario","IdTipoEtapa","Nombre","NombreCorto","FechaCreacion")
VALUES (1,1,1,'Ingreso de cliente','Ingreso',NOW());
--
INSERT INTO "BSCL.Plataforma.BD::COM.EtapaOperacion" ("IdEtapaOperacion","IdOperacionUsuario","IdTipoEtapa","Nombre","NombreCorto","FechaCreacion")
VALUES (2,2,1,'Ingreso de portafolio','Ingreso',NOW());
--
INSERT INTO "BSCL.Plataforma.BD::COM.EtapaOperacion" ("IdEtapaOperacion","IdOperacionUsuario","IdTipoEtapa","Nombre","NombreCorto","FechaCreacion")
VALUES (3,3,1,'Ingreso de compra de Acciones RV','Ingreso',NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.EtapaOperacion" ("IdEtapaOperacion","IdOperacionUsuario","IdTipoEtapa","Nombre","NombreCorto","FechaCreacion")
VALUES (4,3,2,'Asignación de compra de Acciones RV','Asignación',NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.EtapaOperacion" ("IdEtapaOperacion","IdOperacionUsuario","IdTipoEtapa","Nombre","NombreCorto","FechaCreacion")
VALUES (5,3,3,'Envío a Facturar Compra de Acciones RV','Envío a Facturar',NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.EtapaOperacion" ("IdEtapaOperacion","IdOperacionUsuario","IdTipoEtapa","Nombre","NombreCorto","FechaCreacion")
VALUES (6,3,4,'Generación comprobante Facturación Compra de Acciones RV','Generación comprobante Facturación',NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.EtapaOperacion" ("IdEtapaOperacion","IdOperacionUsuario","IdTipoEtapa","Nombre","NombreCorto","FechaCreacion")
VALUES (7,3,5,'Facturación DTE Compra de Acciones RV','Facturación DTE',NOW());
--
INSERT INTO "BSCL.Plataforma.BD::COM.EtapaOperacion" ("IdEtapaOperacion","IdOperacionUsuario","IdTipoEtapa","Nombre","NombreCorto","FechaCreacion")
VALUES (8,4,1,'Ingreso de venta de Acciones RV','Ingreso',NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.EtapaOperacion" ("IdEtapaOperacion","IdOperacionUsuario","IdTipoEtapa","Nombre","NombreCorto","FechaCreacion")
VALUES (9,4,2,'Asignación de venta de Acciones RV','Asignación',NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.EtapaOperacion" ("IdEtapaOperacion","IdOperacionUsuario","IdTipoEtapa","Nombre","NombreCorto","FechaCreacion")
VALUES (10,4,3,'Envío a Facturar venta de Acciones RV','Envío a Facturar',NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.EtapaOperacion" ("IdEtapaOperacion","IdOperacionUsuario","IdTipoEtapa","Nombre","NombreCorto","FechaCreacion")
VALUES (11,4,4,'Generación comprobante Facturación venta de Acciones RV','Generación comprobante Facturación',NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.EtapaOperacion" ("IdEtapaOperacion","IdOperacionUsuario","IdTipoEtapa","Nombre","NombreCorto","FechaCreacion")
VALUES (12,4,5,'Facturación DTE venta de Acciones RV','Facturación DTE',NOW());
-- SELECT * FROM "BSCL.Plataforma.BD::COM.EtapaOperacion"


-- TipoComportamientoCondicion
INSERT INTO "BSCL.Plataforma.BD::COM.TipoComportamientoCondicion" ("IdTipoComportamientoCondicion","Nombre")
VALUES ('ADVERTENCIA','Advertencia');
INSERT INTO "BSCL.Plataforma.BD::COM.TipoComportamientoCondicion" ("IdTipoComportamientoCondicion","Nombre")
VALUES ('RECHAZO','Rechazo');
INSERT INTO "BSCL.Plataforma.BD::COM.TipoComportamientoCondicion" ("IdTipoComportamientoCondicion","Nombre")
VALUES ('AUTORIZACION','Requiere Autorización');
-- SELECT * FROM "BSCL.Plataforma.BD::COM.TipoComportamientoCondicion"


-- TipoCondicion
INSERT INTO "BSCL.Plataforma.BD::COM.TipoCondicion" ("IdTipoCondicion","CodigoTipoCondicion","Nombre","RequiereAtributoX","RequiereAtributoY","ComportamientoPorDefecto","FechaCreacion")
VALUES (1,'VIGDOCID','Vigencia Documento de Identidad','N','N','AUTORIZACION',NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.TipoCondicion" ("IdTipoCondicion","CodigoTipoCondicion","Nombre","RequiereAtributoX","RequiereAtributoY","ComportamientoPorDefecto","FechaCreacion")
VALUES (2,'PEP','Persona Políticame Expuesta (PEP)','N','N','AUTORIZACION',NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.TipoCondicion" ("IdTipoCondicion","CodigoTipoCondicion","Nombre","RequiereAtributoX","RequiereAtributoY","ComportamientoPorDefecto","FechaCreacion")
VALUES (3,'COMPLIANCE','Cliente en Lista Compliance','N','N','AUTORIZACION',NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.TipoCondicion" ("IdTipoCondicion","CodigoTipoCondicion","Nombre","RequiereAtributoX","RequiereAtributoY","ComportamientoPorDefecto","FechaCreacion")
VALUES (4,'DOF','Requiere declaración origen de Fondos (DOF)','N','N','AUTORIZACION',NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.TipoCondicion" ("IdTipoCondicion","CodigoTipoCondicion","Nombre","RequiereAtributoX","RequiereAtributoY","ComportamientoPorDefecto","FechaCreacion")
VALUES (5,'SALDONEG','Operar Saldo Negativo','N','N','AUTORIZACION',NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.TipoCondicion" ("IdTipoCondicion","CodigoTipoCondicion","Nombre","RequiereAtributoX","RequiereAtributoY","ComportamientoPorDefecto","FechaCreacion")
VALUES (6,'NOMINAL','Opera Nominal','N','N','AUTORIZACION',NOW());
-- SELECT * FROM "BSCL.Plataforma.BD::COM.TipoCondicion"


-- Condicion Operativa
INSERT INTO "BSCL.Plataforma.BD::COM.CondicionOperativa" ("IdCondicionOperativa","IdTipoCondicion","IdOperacionUsuario","FechaCreacion","IdEmpresa")
VALUES (1,1,1,NOW(),1);
INSERT INTO "BSCL.Plataforma.BD::COM.CondicionOperativa" ("IdCondicionOperativa","IdTipoCondicion","IdOperacionUsuario","FechaCreacion","IdEmpresa")
VALUES (2,1,2,NOW(),1);
--
INSERT INTO "BSCL.Plataforma.BD::COM.CondicionOperativa" ("IdCondicionOperativa","IdTipoCondicion","IdOperacionUsuario","FechaCreacion","IdEmpresa")
VALUES (3,2,2,NOW(),1);
INSERT INTO "BSCL.Plataforma.BD::COM.CondicionOperativa" ("IdCondicionOperativa","IdTipoCondicion","IdOperacionUsuario","FechaCreacion","IdEmpresa")
VALUES (4,2,2,NOW(),1);
--
INSERT INTO "BSCL.Plataforma.BD::COM.CondicionOperativa" ("IdCondicionOperativa","IdTipoCondicion","IdOperacionUsuario","FechaCreacion","IdEmpresa")
VALUES (5,3,3,NOW(),1);
INSERT INTO "BSCL.Plataforma.BD::COM.CondicionOperativa" ("IdCondicionOperativa","IdTipoCondicion","IdOperacionUsuario","FechaCreacion","IdEmpresa")
VALUES (6,3,4,NOW(),1);
--
INSERT INTO "BSCL.Plataforma.BD::COM.CondicionOperativa" ("IdCondicionOperativa","IdTipoCondicion","IdOperacionUsuario","FechaCreacion","IdEmpresa")
VALUES (7,4,3,NOW(),1);
INSERT INTO "BSCL.Plataforma.BD::COM.CondicionOperativa" ("IdCondicionOperativa","IdTipoCondicion","IdOperacionUsuario","FechaCreacion","IdEmpresa")
VALUES (8,4,4,NOW(),1);
--
INSERT INTO "BSCL.Plataforma.BD::COM.CondicionOperativa" ("IdCondicionOperativa","IdTipoCondicion","IdOperacionUsuario","FechaCreacion","IdEmpresa")
VALUES (9,5,3,NOW(),1);
--
INSERT INTO "BSCL.Plataforma.BD::COM.CondicionOperativa" ("IdCondicionOperativa","IdTipoCondicion","IdOperacionUsuario","FechaCreacion","IdEmpresa")
VALUES (12,6,4,NOW(),1);
-- SELECT * FROM "BSCL.Plataforma.BD::COM.CondicionOperativa"


-- Etapa Grupo Condiciones Operativas
INSERT INTO "BSCL.Plataforma.BD::COM.EtapaGrupoCondicionesOperativas" ("IdEtapaGrupoCondicionesOperativas","IdGrupoCondicionesOperativas","IdEtapaCondicionOperativa","FechaCreacion")
VALUES(1,6,5,NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.EtapaGrupoCondicionesOperativas" ("IdEtapaGrupoCondicionesOperativas","IdGrupoCondicionesOperativas","IdEtapaCondicionOperativa","FechaCreacion")
VALUES(2,6,6,NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.EtapaGrupoCondicionesOperativas" ("IdEtapaGrupoCondicionesOperativas","IdGrupoCondicionesOperativas","IdEtapaCondicionOperativa","FechaCreacion")
VALUES(3,6,7,NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.EtapaGrupoCondicionesOperativas" ("IdEtapaGrupoCondicionesOperativas","IdGrupoCondicionesOperativas","IdEtapaCondicionOperativa","FechaCreacion")
VALUES(4,6,8,NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.EtapaGrupoCondicionesOperativas" ("IdEtapaGrupoCondicionesOperativas","IdGrupoCondicionesOperativas","IdEtapaCondicionOperativa","FechaCreacion")
VALUES(5,6,9,NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.EtapaGrupoCondicionesOperativas" ("IdEtapaGrupoCondicionesOperativas","IdGrupoCondicionesOperativas","IdEtapaCondicionOperativa","FechaCreacion")
VALUES(6,6,10,NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.EtapaGrupoCondicionesOperativas" ("IdEtapaGrupoCondicionesOperativas","IdGrupoCondicionesOperativas","IdEtapaCondicionOperativa","FechaCreacion")
VALUES(7,6,13,NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.EtapaGrupoCondicionesOperativas" ("IdEtapaGrupoCondicionesOperativas","IdGrupoCondicionesOperativas","IdEtapaCondicionOperativa","FechaCreacion")
VALUES(8,6,14,NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.EtapaGrupoCondicionesOperativas" ("IdEtapaGrupoCondicionesOperativas","IdGrupoCondicionesOperativas","IdEtapaCondicionOperativa","FechaCreacion")
VALUES(9,6,15,NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.EtapaGrupoCondicionesOperativas" ("IdEtapaGrupoCondicionesOperativas","IdGrupoCondicionesOperativas","IdEtapaCondicionOperativa","FechaCreacion")
VALUES(10,6,18,NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.EtapaGrupoCondicionesOperativas" ("IdEtapaGrupoCondicionesOperativas","IdGrupoCondicionesOperativas","IdEtapaCondicionOperativa","FechaCreacion")
VALUES(11,6,19,NOW());



-- Etapa Condicion Operativa
INSERT INTO "BSCL.Plataforma.BD::COM.EtapaCondicionOperativa" ("IdEtapaCondicionOperativa","IdEtapaOperacion","IdCondicionOperativa","IdEstado","FechaCreacion")
SELECT "BSCL"."BSCL.Plataforma.BD.Secuencias::EtapaCondicionOperativa".NEXTVAL,"IdEtapaOperacion","IdCondicionOperativa",'ACT',NOW() FROM "BSCL.Plataforma.BD::COM.CondicionOperativa" CO, "BSCL.Plataforma.BD::COM.EtapaOperacion" EO
WHERE CO."IdOperacionUsuario" = EO."IdOperacionUsuario" order by "IdEtapaOperacion","IdCondicionOperativa";







--****************************************
--       USOS PORTAFOLIO
--****************************************



------------------------CONFIGURACION USOS PORTAFOLIO ----------------------

DROP TABLE "BSCL.Plataforma.BD::PLA.ConfiguracionUsoPortafolio" cascade;

-- Maestro de configuracion de usos de portafolio. Portafolio -< UsoPortafolio >- ConfiguracionUsoPortafolio
create column table "BSCL.Plataforma.BD::PLA.ConfiguracionUsoPortafolio" (
   "CodigoUso"						NVARCHAR(40)	NOT NULL, 
   "IdEmpresa"						BIGINT			NOT NULL,
   "IdTipoRegistro"					NVARCHAR(40) 	NOT NULL DEFAULT '',
   "NombreGrupo"					NVARCHAR(100) 	NOT NULL DEFAULT '',
   "DescripcionUso"					NVARCHAR(40)	NOT NULL DEFAULT '', 
   "IdEstado"						NVARCHAR(3)		NOT NULL DEFAULT 'ACT', 
   "EsObligatorio"					NVARCHAR(1)		NOT NULL DEFAULT 'N',
   "IdTipoDocumento"				NVARCHAR(40)	NOT NULL DEFAULT '', 
   primary key  ("CodigoUso","IdEmpresa")
);

-- DATOS INICIALES DEL MAESTRO

DELETE FROM "BSCL.Plataforma.BD::PLA.ConfiguracionUsoPortafolio";
-- Direccion Fisica
INSERT INTO "BSCL.Plataforma.BD::PLA.ConfiguracionUsoPortafolio" ("CodigoUso","IdEmpresa","IdTipoRegistro","NombreGrupo","DescripcionUso","IdEstado","EsObligatorio","IdTipoDocumento")
VALUES ('CORRESP1',1,'DIRECCIONFISICA','Envío Correspondencia','Particular','ACT','N','');
INSERT INTO "BSCL.Plataforma.BD::PLA.ConfiguracionUsoPortafolio" ("CodigoUso","IdEmpresa","IdTipoRegistro","NombreGrupo","DescripcionUso","IdEstado","EsObligatorio","IdTipoDocumento")
VALUES ('CORRESP2',1,'DIRECCIONFISICA','Envío Correspondencia','Comercial','ACT','N','');
INSERT INTO "BSCL.Plataforma.BD::PLA.ConfiguracionUsoPortafolio" ("CodigoUso","IdEmpresa","IdTipoRegistro","NombreGrupo","DescripcionUso","IdEstado","EsObligatorio","IdTipoDocumento")
VALUES ('CORRESP3',1,'DIRECCIONFISICA','Envío Correspondencia','Legal','ACT','N','');
-- Direccion Electronica
INSERT INTO "BSCL.Plataforma.BD::PLA.ConfiguracionUsoPortafolio" ("CodigoUso","IdEmpresa","IdTipoRegistro","NombreGrupo","DescripcionUso","IdEstado","EsObligatorio","IdTipoDocumento")
VALUES ('EMAIL1',1,'DIRECCIONELECTRONICA','Envío Email','Facturación','ACT','N','');
INSERT INTO "BSCL.Plataforma.BD::PLA.ConfiguracionUsoPortafolio" ("CodigoUso","IdEmpresa","IdTipoRegistro","NombreGrupo","DescripcionUso","IdEstado","EsObligatorio","IdTipoDocumento")
VALUES ('EMAIL2',1,'DIRECCIONELECTRONICA','Envío Email','Cartola','ACT','N','');
INSERT INTO "BSCL.Plataforma.BD::PLA.ConfiguracionUsoPortafolio" ("CodigoUso","IdEmpresa","IdTipoRegistro","NombreGrupo","DescripcionUso","IdEstado","EsObligatorio","IdTipoDocumento")
VALUES ('EMAIL3',1,'DIRECCIONELECTRONICA','Envío Email','Operaciones Generales','ACT','N','');
-- Telefono
INSERT INTO "BSCL.Plataforma.BD::PLA.ConfiguracionUsoPortafolio" ("CodigoUso","IdEmpresa","IdTipoRegistro","NombreGrupo","DescripcionUso","IdEstado","EsObligatorio","IdTipoDocumento")
VALUES ('TELEF1',1,'TELEFONO','Contacto Telefónico','Particular','ACT','N','');
INSERT INTO "BSCL.Plataforma.BD::PLA.ConfiguracionUsoPortafolio" ("CodigoUso","IdEmpresa","IdTipoRegistro","NombreGrupo","DescripcionUso","IdEstado","EsObligatorio","IdTipoDocumento")
VALUES ('TELEF2',1,'TELEFONO','Contacto Telefónico','Comercial','ACT','N','');
INSERT INTO "BSCL.Plataforma.BD::PLA.ConfiguracionUsoPortafolio" ("CodigoUso","IdEmpresa","IdTipoRegistro","NombreGrupo","DescripcionUso","IdEstado","EsObligatorio","IdTipoDocumento")
VALUES ('TELEF3',1,'TELEFONO','Contacto Telefónico','Legal','ACT','N','');
-- Tarjetas Bancarias
INSERT INTO "BSCL.Plataforma.BD::PLA.ConfiguracionUsoPortafolio" ("CodigoUso","IdEmpresa","IdTipoRegistro","NombreGrupo","DescripcionUso","IdEstado","EsObligatorio","IdTipoDocumento")
VALUES ('TRJBANC1',1,'TARJETABANCARIA','Tarjetas Bancarias','Principal','ACT','N','');
-- Cuenta Bancaria
INSERT INTO "BSCL.Plataforma.BD::PLA.ConfiguracionUsoPortafolio" ("CodigoUso","IdEmpresa","IdTipoRegistro","NombreGrupo","DescripcionUso","IdEstado","EsObligatorio","IdTipoDocumento")
VALUES ('CTABANC1',1,'CUENTABANCARIA','Cuentas Bancarias','Principal','ACT','N','');
SELECT * FROM  "BSCL.Plataforma.BD::PLA.ConfiguracionUsoPortafolio";


-------------------------USO PORTAFOLIO-----------------------------------

DROP TABLE 		"BSCL.Portafolio.BD::POR.UsoPortafolio" CASCADE;
DROP SEQUENCE 	"BSCL.Portafolio.BD.Secuencias::UsoPortafolio";

-- Maestro de configuracion de usos de portafolio. Portafolio -< UsoPortafolio >- ConfiguracionUsoPortafolio
CREATE COLUMN TABLE "BSCL.Portafolio.BD::POR.UsoPortafolio" (
   	"IdUsoPortafolio"				BIGINT			NOT NULL, 
   	"IdPortafolio"					BIGINT			NOT NULL 	DEFAULT 0,
   	"CodigoUso"						NVARCHAR(40)	NOT NULL 	DEFAULT '',
   	--"IdEmpresa"						BIGINT			NOT NULL 	DEFAULT 0,
   	"IdRegistro"					NVARCHAR(100)	NOT NULL 	DEFAULT '',
   	"IdEstado"						NVARCHAR(3) 	NOT NULL 	DEFAULT 'ACT',
   	"IdEstadoAutorizacion"			NVARCHAR(3)		NOT NULL	DEFAULT 'PEN',
   	"FechaCreacion" 				SECONDDATE  	NOT NULL 	DEFAULT '0001-01-01 00:00:00',
	"FechaModificacion" 			SECONDDATE 		NOT NULL 	DEFAULT '0001-01-01 00:00:00',
  	PRIMARY KEY ("IdUsoPortafolio")
);

create sequence "BSCL.Portafolio.BD.Secuencias::UsoPortafolio" increment by 1 start with 10000 minvalue 10000 maxvalue 4611686018427387903 no cycle;


----------INSCRIPCION DE SERVICIOS --------------------------

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(124,'BSCL.Portafolio.BD.Servicios::CrearUsoPortafolio',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(125,'BSCL.Portafolio.BD.Servicios::ModificarUsoPortafolio',0,'P','');

-----------------AUTORIZACIONES----------------
--CONCEPTOS
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('MODDUP','MP','POR','MODIFICACION DATOS USO PORTAFOLIO','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('NUEDUP','MP','POR','NUEVO DATO USO PORTAFOLIO','VIG');
-- TIPO EVENTO
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('MODDUP',1,'POR','AUT_POR_MOD','Modificación Datos Uso Portafolio',1,'S','' ,'' ,0,0,'VIG');
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('NUEDUP',1,'POR','AUT_POR_NUE','Creación Datos Uso Portafolio',1,'S','' ,'' ,0,0,'VIG');




drop type   "BSCL.Custodia.BD::CUS.FlujoCustodia";
drop type   "BSCL.Custodia.BD::CUS.SaldoFlujo";

CREATE TYPE "BSCL.Custodia.BD::CUS.FlujoCustodia" AS TABLE  ( 
	 "TipoFlujo"					NVARCHAR(40)
	,"CodDiccionario"				NVARCHAR(100)
	,"Operador"						BIGINT
	,"FuncionOperador"				NVARCHAR(1)
	,"FechaLiquidacion"				DATE
	,"CantidadDisponible"			DECIMAL(26,8)
	,"MontoDisponible"				DECIMAL(26,8)
	,"IdMoneda"						NVARCHAR(3)
	,"IdInstrumento"				BIGINT
	,"Nemo"							NVARCHAR(40)
	,"IdTipoInstrumento"			NVARCHAR(3)
	,"IdTipoOperacion"				NVARCHAR(6)
	,"IdBoveda"						NVARCHAR(3)
	,"IdBolsa"						NVARCHAR(3)
	,"IdMesa"						BIGINT
	,"IdTipoInstrumentoSubyacente"  NVARCHAR(3)
	,"IdOrigenGarantia"				NVARCHAR(3)
	,"Accion"						NVARCHAR(40)
	,"IdTipoInstrumentoGarantizado" NVARCHAR(3)
	,"IdClasificacionTipoInstrumento" NVARCHAR(3)
	,"FolioCartera" 				BIGINT
	,"IdCartera" 					BIGINT
	,"IdEmisor"						NVARCHAR(40)
	,"IdTipoReajuste"				NVARCHAR(2)
	,"EsFungible"					NVARCHAR(1)
	,"FechaEmisionInstrumento"		DATE
	,"FechaVencimientoInstrumento"	DATE
	,"CodigoIFRS"					NVARCHAR(40)
	,"NemoSVS"						NVARCHAR(40)
	,"IdConcepto"					NVARCHAR(6)
	,"INGFecha"						DATE
	,"FechaVencimientoOperacion"	DATE
	,"INGPrecio"					DECIMAL(26,8)
	,"INGTasaCompra"				DECIMAL(26,8)
	,"INGValorRescate"				DECIMAL(26,8)
	,"Cantidad"						DECIMAL(26,8)
	,"IdComprobanteFactRefer"		BIGINT
);

CREATE TYPE "BSCL.Custodia.BD::CUS.SaldoFlujo" AS TABLE  ( 
	 "FechaLiquidacion"				DATE
	,"CantidadDisponible"			DECIMAL(26,8)
	,"MontoDisponible"				DECIMAL(26,8)
	,"IdMoneda"						NVARCHAR(3)
	,"IdInstrumento"				BIGINT
	,"Nemo"							NVARCHAR(40)
	,"IdTipoInstrumento"			NVARCHAR(3)
	,"IdTipoOperacion"				NVARCHAR(6)
	,"IdBoveda"						NVARCHAR(3)
	,"IdBolsa"						NVARCHAR(3)
	,"IdMesa"						BIGINT
	,"IdTipoInstrumentoSubyacente"  NVARCHAR(3)
	,"IdOrigenGarantia"				NVARCHAR(3)
	,"Accion"						NVARCHAR(40)
	,"IdTipoInstrumentoGarantizado" NVARCHAR(3)
	,"IdClasificacionTipoInstrumento" NVARCHAR(3)
	,"FolioCartera" 				BIGINT
	,"IdCartera" 					BIGINT
	,"IdEmisor"						NVARCHAR(40)
	,"IdTipoReajuste"				NVARCHAR(2)
	,"EsFungible"					NVARCHAR(1)
	,"FechaEmisionInstrumento"		DATE
	,"FechaVencimientoInstrumento"	DATE
	,"CodigoIFRS"					NVARCHAR(40)
	,"NemoSVS"						NVARCHAR(40)
	,"IdConcepto"					NVARCHAR(6)
	,"INGFecha"						DATE
	,"FechaVencimientoOperacion"	DATE
	,"INGPrecio"					DECIMAL(26,8)
	,"INGTasaCompra"				DECIMAL(26,8)
	,"INGValorRescate"				DECIMAL(26,8)
	,"Cantidad"						DECIMAL(26,8)
	,"IdComprobanteFactRefer"		BIGINT
);

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") VALUES (44,'CLP','CUS','BEM','',2,'T',2,'T',0,'T',0,'T',0,'T',0,'T',0,'T','ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") VALUES (45,'UF','CUS','BEM','',2,'T',2,'T',4,'T',4,'T',4,'T',4,'T',4,'T','ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") VALUES (46,'USD','CUS','BEM','',2,'T',2,'T',2,'T',2,'T',2,'T',2,'T',2,'T','ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") VALUES (47,'CLP','CUS','PAG','',2,'T',2,'T',0,'T',0,'T',0,'T',0,'T',0,'T','ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") VALUES (48,'UF','CUS','PAG','',2,'T',2,'T',4,'T',4,'T',4,'T',4,'T',4,'T','ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") VALUES (49,'USD','CUS','PAG','',2,'T',2,'T',2,'T',2,'T',2,'T',2,'T',2,'T','ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") VALUES (50,'EUR','CUS','PAG','',2,'T',2,'T',2,'T',2,'T',2,'T',2,'T',2,'T','ACT'); 
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") VALUES (51,'CLP','CUS','PDT','',2,'T',2,'T',0,'T',0,'T',0,'T',0,'T',0,'T','ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") VALUES (52,'UF','CUS','TPR','',2,'T',2,'T',4,'T',4,'T',4,'T',4,'T',4,'T','ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") VALUES (53,'CLP','CUS','PDB','',2,'T',2,'T',0,'T',0,'T',0,'T',0,'T',0,'T','ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") VALUES (54,'UF','CUS','PRB','',2,'T',2,'T',4,'T',4,'T',4,'T',4,'T',4,'T','ACT');

DROP TABLE "BSCL.Plataforma.BD::PLA.ConfiguracionFiltroInterfaz" cascade;
CREATE COLUMN TABLE "BSCL.Plataforma.BD::PLA.ConfiguracionFiltroInterfaz" (
	 "IdConfiguracionFiltroInterfaz" BIGINT CS_FIXED NOT NULL,
	 "Controlador" 	NVARCHAR(100)  DEFAULT '' NOT NULL , --CrearPrestamoModalCtrl
	 "TipoObjeto"	NVARCHAR(50)   DEFAULT '' NOT NULL , --combobox, grilla
	 "NombreObjeto"	NVARCHAR(100)  DEFAULT '' NOT NULL , --tipo-instrumento,combobox-bolsa
	 "IdEstado"		NVARCHAR(3)    DEFAULT '' NOT NULL ,
	 "Filtros"		NVARCHAR(2000) DEFAULT '' NOT NULL ,
	 PRIMARY KEY ("IdConfiguracionFiltroInterfaz")
);

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ConfiguracionFiltroInterfaz" 
("IdConfiguracionFiltroInterfaz","Controlador","TipoObjeto","NombreObjeto","IdEstado","Filtros") 
VALUES (1,'IngresarTituloCtrl','ComboBox','combobox-clasificacion-tipo-instrumento','ACT','
{
    "logic": "or",
    "filters": [
    {"field": "IdClasificacionTipoInstrumento","operator": "eq","value": "RF"},
    {"field": "IdClasificacionTipoInstrumento","operator": "eq","value": "RV"},
    {"field": "IdClasificacionTipoInstrumento","operator": "eq","value": "IF"}
    ]
}');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ConfiguracionFiltroInterfaz" 
("IdConfiguracionFiltroInterfaz","Controlador","TipoObjeto","NombreObjeto","IdEstado","Filtros") 
VALUES (2,'IngresarTituloCtrl','ComboBox','combobox-operacion','ACT','
{
    "logic": "or",
    "filters": [
    {"field": "IdTipoOperacion","operator": "eq","value": "INGTIT"},
    {"field": "IdTipoOperacion","operator": "eq","value": "EGRTIT"},
    {"field": "IdTipoOperacion","operator": "eq","value": "TRATIT"}
    ]
}');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ConfiguracionFiltroInterfaz" 
("IdConfiguracionFiltroInterfaz","Controlador","TipoObjeto","NombreObjeto","IdEstado","Filtros") 
VALUES (3,'IngresarTituloCtrl','ComboBox','combobox-tipo-instrumento','ACT','
{
    "logic": "and",
    "filters": [
    {"field": "IdTipoInstrumento","operator": "neq","value": "SIM"},
    {"field": "IdTipoInstrumento","operator": "neq","value": "PAC"},
    {"field": "IdTipoInstrumento","operator": "neq","value": "PRT"},
    {"field": "IdTipoInstrumento","operator": "neq","value": "EMP"},
    {"field": "IdTipoInstrumento","operator": "neq","value": "MON"},
    {"field": "IdTipoInstrumento","operator": "neq","value": "FRW"}
    ]
}');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ConfiguracionFiltroInterfaz" 
("IdConfiguracionFiltroInterfaz","Controlador","TipoObjeto","NombreObjeto","IdEstado","Filtros") 
VALUES (4,'ConsultarMovimientosEnTransitoCtrl','ComboBox','combobox-tipo-instrumento','ACT','
{
    "logic": "and",
    "filters": [
    {"field": "IdTipoInstrumento","operator": "neq","value": "SIM"},
    {"field": "IdTipoInstrumento","operator": "neq","value": "PAC"},
    {"field": "IdTipoInstrumento","operator": "neq","value": "PRT"},
    {"field": "IdTipoInstrumento","operator": "neq","value": "EMP"},
    {"field": "IdTipoInstrumento","operator": "neq","value": "MON"},
    {"field": "IdTipoInstrumento","operator": "neq","value": "FRW"}
    ]
}');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ConfiguracionFiltroInterfaz" 
("IdConfiguracionFiltroInterfaz","Controlador","TipoObjeto","NombreObjeto","IdEstado","Filtros") 
VALUES (5,'ConsultarMovimientosEnTransitoCtrl','ComboBox','combobox-clasificacion-tipo-instrumento','ACT','
{
    "logic": "or",
    "filters": [
    {"field": "IdClasificacionTipoInstrumento","operator": "eq","value": "RF"},
    {"field": "IdClasificacionTipoInstrumento","operator": "eq","value": "RV"},
    {"field": "IdClasificacionTipoInstrumento","operator": "eq","value": "IF"}
    ]
}');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ConfiguracionFiltroInterfaz" 
("IdConfiguracionFiltroInterfaz","Controlador","TipoObjeto","NombreObjeto","IdEstado","Filtros") 
VALUES (6,'ConsultarMovimientosEnTransitoHistoricoCtrl','ComboBox','combobox-clasificacion-tipo-instrumento','ACT','
{
    "logic": "or",
    "filters": [
    {"field": "IdClasificacionTipoInstrumento","operator": "eq","value": "RF"},
    {"field": "IdClasificacionTipoInstrumento","operator": "eq","value": "RV"},
    {"field": "IdClasificacionTipoInstrumento","operator": "eq","value": "IF"}
    ]
}');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ConfiguracionFiltroInterfaz" 
("IdConfiguracionFiltroInterfaz","Controlador","TipoObjeto","NombreObjeto","IdEstado","Filtros") 
VALUES (7,'ConsultarMovimientosEnTransitoHistoricoCtrl','ComboBox','combobox-tipo-instrumento','ACT','
{
    "logic": "and",
    "filters": [
    {"field": "IdTipoInstrumento","operator": "neq","value": "SIM"},
    {"field": "IdTipoInstrumento","operator": "neq","value": "PAC"},
    {"field": "IdTipoInstrumento","operator": "neq","value": "PRT"},
    {"field": "IdTipoInstrumento","operator": "neq","value": "EMP"},
    {"field": "IdTipoInstrumento","operator": "neq","value": "MON"},
    {"field": "IdTipoInstrumento","operator": "neq","value": "FRW"}
    ]
}');

ALTER TABLE "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" add ("SecuenciaMovimientoMigracion" BIGINT DEFAULT 0 NOT NULL); 
   

-- Table: "BSCL.Normativa.BD::DOC.Esquema"
-- Tabla que contiene datos de especificacion base de todos los
-- tipos de documentos.
-- Representa la raiz en la jerarquia.
create row table "BSCL.Normativa.BD::DOC.Esquema" (
    -- Nombre del esquema (Ej. DJ1891).
    "Esquema" nvarchar(10) not null,

    -- Version del esquema (Ej. 2015).
    "Version" nvarchar(10) not null,

    -- Periodicidad (Ej. "A"nual , "M"ensual, "T"rimestral, "D"iaria)
    "Periodicidad" char(1) not null,

    -- Texto descriptivo del objetivo de la declaracion.
    "Objetivo" nvarchar(100) not null,

    -- Fecha desde la cual se exige presentacion
    "PeriodoInicio" date not null,

    -- Identificador de la entidad reguladora (Ej. SII)
    "IdEntidadReguladora" nvarchar(40) not null,

    -- Fecha limite para presentacion en la entidad reguladora.
    "FechaPresentacionLimite" date not null,

    primary key ("Esquema", "Version")
);

-- Table: "BSCL.Normativa.BD::DOC.Campo"
-- Especificacion de los campos del detalle de un documento,
-- en funcion de su representacion y formatos para el tratamiento
-- generico de los mismos en los programas correspondientes.
create row table "BSCL.Normativa.BD::DOC.Campo" (
    -- ID del registro.
    "IdCampo" bigint not null,

    -- Nombre del esquema.
    "Esquema" nvarchar(10) not null,

    -- Version del esquema.
    "Version" nvarchar(10) not null,

    -- Orden de presentacion del campo.
    "Orden" smallint not null,

    -- Nombre del campo (debe ser unico para esquema/version).
    "Nombre" nvarchar(40) not null,

    -- Texto descriptivo del campo para ser utilizado como titulo.
    -- Debiera ser unico para esquema/version.
    "Titulo" nvarchar(100) not null,

    -- Comentarios acerca del campo.
    "Descripcion" nvarchar(100) not null,

    -- Tipo de dato, segun los manejados por la aplicacion.
    "TipoDato" nvarchar(100) not null,

    -- Largo del campo.
    "Largo" smallint not null,

    -- Valor por defecto del campo.
    "ValorDefecto" nvarchar(100) not null,

    -- Indicador de si el campo es obligatorio (S/N).
    "Obligatorio" char(1) not null,

    -- Indicador de si el campo es editable (S/N).
    "Editable" char(1) not null,

    -- Indicador de si el campo es editable en modo masivo (S/N).
    "ModoMasivo" char(1) not null,

    -- Indicador de si el campo es exportable (S/N).
    "Exportable" char(1) not null,

    -- Indicador de si el campo es visible en la grilla (S/N).
    "Visible" char(1) not null,

    -- Posición de disposicion del campo en archivo de carga.
    "OrdenCargaExterna" smallint not null,

    -- Largo del dato en carga externa.
    "LargoCargaExterna" smallint not null,

    -- Indicador para fijar la columna del campo en la grilla (S/N).
    "FijarColumna" char(1) not null,

    -- Tipo de diccionario relacionado.
    "TipoDiccionario" nvarchar(40) not null default '',

    primary key ("IdCampo")
);

alter table "BSCL.Normativa.BD::DOC.Campo"
   add constraint "FK_Esquema_Campo" foreign key ("Esquema", "Version")
      references "BSCL.Normativa.BD::DOC.Esquema" ("Esquema", "Version")
      on delete restrict on update restrict;

-- Table: "BSCL.Normativa.BD::DOC.Diccionario"
-- Definicion de codigos utilizados en la edicion y presentacion del documento.
-- Sirve para llenar combos.
-- La nomenclatura o codigo es la impuesta por la entidad reguladora.
create row table "BSCL.Normativa.BD::DOC.Diccionario" (
    -- ID del registro.
    "IdDiccionario" bigint not null,

    -- Nombre del esquema.
    "Esquema" nvarchar(10) not null,

    -- Version del esquema.
    "Version" nvarchar(10) not null,

    -- Tipo de diccionario (actua como clasificador).
    -- Ej. CodigoConcepto, Clasific, CodMoneda
    "Tipo" nvarchar(40) not null,

    -- Orden de presentacion en la vista.
    "Orden" smallint not null,

    -- Codigo del par (Codigo, Descripcion).
    "Codigo" nvarchar(40) not null,

    -- Descripcion del par (Codigo, Descripcion).
    "Descripcion" nvarchar(100) not null,

    -- Indicador de si el registro esta activo (S/N).
    "Activo" char(1) not null,

    -- Objetivo ...
    "Objetivo" nvarchar(100) not null,

    -- Producto del Sistema Operacional al cual aplica.
    "Producto" nvarchar(100) not null,

    primary key ("IdDiccionario")
);

alter table "BSCL.Normativa.BD::DOC.Diccionario"
   add constraint "FK_Esquema_Diccionario" foreign key ("Esquema", "Version")
      references "BSCL.Normativa.BD::DOC.Esquema" ("Esquema", "Version")
      on delete restrict on update restrict;

-- Table: "BSCL.Normativa.BD::DOC.Homologacion"
-- Contiene homologación de tipos.
-- Cuando se cargan datos desde un origen operacional se debe aplicar
-- conversiones a los tipos de datos para dejar en formato segun de
-- acuerdo a los codigos definidos por la entidad reguladora.
create row table "BSCL.Normativa.BD::DOC.Homologacion" (
    -- ID del registro.
    "IdHomologacion" bigint not null,

    -- Nombre del esquema.
    "Esquema" nvarchar(10) not null,

    -- Version del esquema.
    "Version" nvarchar(10) not null,

    -- Nombre del campo a homologar.
    "Tipo" nvarchar(40) not null,

    -- Descripcion del campo a homologar.
    "Descripcion" nvarchar(100) not null,

    -- Valor en el sistema de origen.
    "ValorOrigen" nvarchar(100) not null,

    -- Valor en el sistema de destino.
    "ValorDestino" nvarchar(100) not null,

    primary key ("IdHomologacion")
);

alter table "BSCL.Normativa.BD::DOC.Homologacion"
   add constraint "FK_Esquema_Homologacion" foreign key ("Esquema", "Version")
      references "BSCL.Normativa.BD::DOC.Esquema" ("Esquema", "Version")
      on delete restrict on update restrict;

-- Table: "BSCL.Normativa.BD::DOC.Validacion"
-- Definición de las validaciones aplicadas a los documentos.
create row table "BSCL.Normativa.BD::DOC.Validacion" (
    -- ID del registro.
    "IdValidacion" bigint not null,

    -- Nombre del esquema.
    "Esquema" nvarchar(10) not null,

    -- Version del esquema.
    "Version" nvarchar(10) not null,
    -- Tipo de validacion (Ej: VALIDACION, CUADRATURA, NEGOCIO).
    "TipoValidacion" nvarchar(40) not null,

    -- Codigo de identificacion de la validacion (Ej: H1520).
    "CodigoValidacion" nvarchar(40) not null,

    -- Descripcion de la validacion.
    -- Ej: Si Tipo de Fondo es igual a F, entonces Tipo de Operacion debe ser igual a 1.
    "Descripcion" nvarchar(250) not null,

    -- Severidad de la validacion.
    -- "C" Correcta / "A" advertencia / "B" bloqueo.
    "Severidad" char(1) not null,

    -- Expresion de la regla de validacion.
    -- Ej. Si (C2) = F -> (C5) = 1
    "Regla" nvarchar(250) not null,

    primary key ("IdValidacion")
);

alter table "BSCL.Normativa.BD::DOC.Validacion"
   add constraint "FK_Esquema_Validacion" foreign key ("Esquema", "Version")
      references "BSCL.Normativa.BD::DOC.Esquema" ("Esquema", "Version")
      on delete restrict on update restrict;

-- Table: "BSCL.Normativa.BD::DOC.TipoDocumento"
-- Definición de Tipo de Documento,
create row table "BSCL.Normativa.BD::DOC.TipoDocumento" (
    -- Nombre del tipo de documento.
    "IdTipoDocumento" nvarchar(10) not null,

    -- Descripcion del tipo de documento.
    -- Ejemplos:
    --     + Certificados por Cliente (Requiere Firma)
    --     + Declaraciones por Corredor (Confirmacion)
    --     + Ciculares por Corredor (Confirmacion)
    --     + Circulares por Cliente (Firma)
    "Descripcion" nvarchar(250) not null,

    primary key ("IdTipoDocumento")
);

-- Table: "BSCL"."BSCL.Normativa.BD::DOC.Documento"
-- Contiene la edicion del documento.
-- Cada periodo a informar debe inscribir un registro aca.
create row table "BSCL.Normativa.BD::DOC.Documento" (
    -- ID del registro.
    "IdDocumento" bigint not null,

    -- Tipo de documento.
    "IdTipoDocumento" nvarchar(10) not null,

    -- Nombre del esquema.
    "Esquema" nvarchar(10) not null,

    -- Version del esquema.
    "Version" nvarchar(10) not null,

    -- ID de la empresa.
    "IdEmpresa" bigint not null,

    -- Fecha limite de presentacion del documento.
    -- Que pasa cuando es mensual, se van agregando ediciones mensuales?
    "FechaLimite" date not null,

    -- Estado del documento (debe ser consecuente con Estados del Flujo).
    "IdEstadoDocumento" nvarchar(40) not null,

    -- Fecha de creacion del documento.
    "FechaCreacion" seconddate not null,

    -- Fecha de presentacion del documento.
    "FechaPresentacion" seconddate not null,

    -- Cantidad de registros cargados.
    "CantidadRegistrosCargados" bigint not null,

    -- SISTEMA / EXTERNA.
    "FuenteDatos" nvarchar(40) not null,

    -- Carga Inicial, Interfaz Externa.
    "ProcesoFuente" nvarchar(40) not null,

    -- Cliente Titular que presenta el documento (Corredora, Cliente)
    "IdTitularDocumento" bigint not null,

    -- Identificador Tributario o Documento Legal que identifica al cliente (RUT-TAXID)
    "DocumentoIdentidadTitular" nvarchar(40) not null,

    -- Representante Legal del Titular
    "IdRepresentanteLegal" bigint not null,

    -- Identificador Tributario o Documento Legal que identifica al Representante Legal
    "DocumentoIdentidadRepLegal" nvarchar(40) not null,

    "DireccionFisicaTitular" nvarchar(100) not null,

    "DireccionElectronicaTitular" nvarchar(100) not null,

    "TelefonoTitular" nvarchar(100) not null,

    -- Folio de Documento utilizado en las interfaces a enviar al regulador.
    -- Ej: I9153103
    "FolioPresentacionDocumento" nvarchar(100) not null,

    primary key ("IdDocumento")
);

alter table "BSCL.Normativa.BD::DOC.Documento"
   add constraint "FK_TipoDocumento_Documento" foreign key ("IdTipoDocumento")
      references "BSCL.Normativa.BD::DOC.TipoDocumento" ("IdTipoDocumento")
      on delete restrict on update restrict;

alter table "BSCL.Normativa.BD::DOC.Documento"
   add constraint "FK_Esquema_Documento" foreign key ("Esquema", "Version")
      references "BSCL.Normativa.BD::DOC.Esquema" ("Esquema", "Version")
      on delete restrict on update restrict;

-- Table: "BSCL.Normativa.BD::DOC.Detalle"
-- Datos de control de un detalle de documento emitido (Ej: Datos de una Linea de la DJ).
create row table "BSCL.Normativa.BD::DOC.Detalle" (
    -- ID del registro.
    "IdDetalleDocumento" bigint not null,

    -- Identificador Unico de la edicion del Documento.
    "IdDocumento" bigint not null,

    -- VoBo del usuario sobre el registro (S/N).
    "VboUsuario" char(1) not null,

    -- Observacion ingresada por usuario que edito el registro.
    "Observaciones" nvarchar(250) not null,

    -- SISTEMA / EXTERNA.
    "FuenteDatos" nvarchar(40) not null,

    primary key ("IdDetalleDocumento")
);

alter table "BSCL.Normativa.BD::DOC.Detalle"
   add constraint "FK_Documento_Detalle" foreign key ("IdDocumento")
      references "BSCL.Normativa.BD::DOC.Documento" ("IdDocumento")
      on delete restrict on update restrict;

-- Table: "BSCL.Normativa.BD::TRI.DetalleDocumento1891"
-- Contiene los datos de la DJ1891.
-- Permite el ingreso de datos nulos, pues no en todas las
-- ediciones del documento se informan todos los campos o
-- existen campos condicionados entre si.
create row table "BSCL.Normativa.BD::TRI.DetalleDocumento1891" (
    -- ID del registro.
    -- Por convencion, en todas las tablas de este tipo
    -- el nombre de esta columna debe ser ID.
   "ID" bigint not null,

   -- Tipo de Documento a declarar.
   -- Ej: F: Factura / C: Nota de Crédito / D: Nota de Débito / O: Otro.
   "TipoDocumento" nvarchar(10) not null,

   -- Folio de Documento.
   "NumeroDocumento" bigint not null,

   -- Fecha del documento.
   "FechaDocumento" date null,

   -- RUT del emisor.
   "RutEmisor" nvarchar(40) null,

   -- Nemotecnico del instrumento.
   "Nemotecnico" nvarchar(100) null,

   -- RUT del cliente.
   -- Nota: Existen RUT de 9 digitos, 100.000.000-1, no los tolera el SII,
   -- pero deben registrar operacion, aunque la validacion
   "RutCliente" nvarchar(40) null,

   -- Cantidad de acciones transadas.
   "Cantidad" bigint not null,

   -- Monto Compra.
   "MontoCompra" decimal(15,0) not null,

   -- Monto Venta.
   "MontoVenta" decimal(15,0) not null,

   -- Interpretación respecto al tipo Operacion Original.
   -- Ej 01: Acciones/02: Dólares/03: Oro/04: Otras operaciones de futuro/05: Opciones ....
   "TipoTransaccion" nvarchar(6) not null,

   -- Registros para identificación de Operacion origen, útil para cuadraturas y recarga de datos.

   -- Identificador del tipo de operación del registro Base de Comprobantes.
   -- Ej: Compra/Venta.
   "IdTipoOperacion" nvarchar(6) null,

   -- Identificador del tipo de documento.
   "IdTipoProducto" nvarchar(4) null,

   -- Relaciones otras fuentes.

   -- Identificador del Registro Base Origen.
   "IdBaseDocumento" bigint null,

   -- Identificador unico de cliente del registro Base Cliente.
   "IdCliente" bigint null,

   -- Identificador unico de Portafolio del registro Base Cliente.
   "IdPortafolio" bigint null,

   primary key ("ID")
);

create unique index "BSCL.Normativa.BD::TRI.DetalleDocumento1891.IX01"
    on "BSCL.Normativa.BD::TRI.DetalleDocumento1891" ("RutCliente", "TipoDocumento", "NumeroDocumento");

alter table "BSCL.Normativa.BD::TRI.DetalleDocumento1891"
   add constraint "FK_Detalle_DetalleDocumento1891" foreign key ("ID")
      references "BSCL.Normativa.BD::DOC.Detalle" ("IdDetalleDocumento")
      on delete restrict on update restrict;

/*==============================================================*/
/* TIPO DOCUMENTO                                               */
/*==============================================================*/

insert into "BSCL"."BSCL.Normativa.BD::DOC.TipoDocumento"
select  'DECLACORR'                         as "IdTipoDocumento"
        ,'Declaracion Jurada por Corredora' as "Descripcion"
from dummy;

insert into "BSCL"."BSCL.Normativa.BD::DOC.TipoDocumento"
select  'CERTCLI'                           as "IdTipoDocumento"
        ,'Certificado Tributario Cliente'   as "Descripcion"
from dummy;

/*==============================================================*/
/* ESQUEMAS                                                     */
/*==============================================================*/

insert into "BSCL"."BSCL.Normativa.BD::DOC.Esquema"
select  'DJ1891'        as "Esquema"
        ,'2015'         as "Version"
        ,'A'            as "Periodicidad"
        ,''             as "Objetivo"
        ,'1996-02-15'   as "PeriodoInicio"
        ,'SII'          as "IdEntidadReguladora",
        '2015-03-30'    as "FechaPresentacionLimite"
from dummy;

insert into "BSCL"."BSCL.Normativa.BD::DOC.Esquema"
select  'DJ1891'        as "Esquema"
        ,'2016'         as "Version"
        ,'A'            as "Periodicidad"
        ,''             as "Objetivo"
        ,'1996-02-15'   as "PeriodoInicio"
        ,'SII'          as "IdEntidadReguladora",
        '2016-03-30'    as "FechaPresentacionLimite"
from dummy;

/*==============================================================*/
/* DICCIONARIOS                                                 */
/*==============================================================*/

insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select  1, 'DJ1891', '2015', 'TipoProducto', 'INAX',         1, 'Acciones extranjeras en BCS'  , 'S', '', 'Facturacion'      from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select  2, 'DJ1891', '2015', 'TipoProducto', 'INAC',         2, 'Intermediacion acciones'      , 'S', '', 'Facturacion'      from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select  3, 'DJ1891', '2015', 'TipoProducto', 'INCI',         3, 'Intermediacion CFI'           , 'S', '', 'Facturacion'      from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select  4, 'DJ1891', '2015', 'TipoProducto', 'INCM',         4, 'Intermediacion CFM'           , 'S', '', 'Facturacion'      from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select  5, 'DJ1891', '2015', 'TipoProducto', 'INCX',         5, 'Intermedacion CFI Extranjeros', 'S', '', 'Facturacion'      from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select  6, 'DJ1891', '2015', 'TipoProducto', 'INEN',         6, 'Intermediacion ETF nacional'  , 'S', '', 'Facturacion'      from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select  7, 'DJ1891', '2015', 'TipoProducto', 'INEX',         7, 'Intermediacion ETF extranjero', 'S', '', 'Facturacion'      from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select  8, 'DJ1891', '2015', 'TipoProducto', 'INFU',         8, 'Intermediacion futuros'       , 'S', '', 'Facturacion'      from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select  9, 'DJ1891', '2015', 'TipoProducto', 'INFM',         9, 'Intermediacion fondos mutuos' , 'S', '', 'Facturacion'      from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 10, 'DJ1891', '2015', 'TipoProducto', 'INIF',        10, 'Intermediacion IIF'           , 'S', '', 'Facturacion'      from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 11, 'DJ1891', '2015', 'TipoProducto', 'INRF',        11, 'Intermediacion renta fija'    , 'S', '', 'Facturacion'      from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 12, 'DJ1891', '2015', 'TipoProducto', 'INSI',        12, 'Intermediación simultáneas'   , 'S', '', 'Facturacion'      from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 13, 'DJ1891', '2015', 'TipoProducto', 'MEME',        13, 'Mesa moneda extranjera (Spot)', 'S', '', 'MonedaExtranjera' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 14, 'DJ1891', '2015', 'TipoProducto', 'MERF',        14, 'Mesa pactos'                  , 'S', '', 'MesaPactos'       from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 15, 'DJ1891', '2015', 'TipoProducto', 'INMO',        15, 'Intermediación Monetarios'    , 'S', '', 'TipoProducto'     from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 16, 'DJ1891', '2015', 'TipoProducto', 'INST',        16, 'Intermediación Stocks'        , 'S', '', 'TipoProducto'     from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 17, 'DJ1891', '2015', 'TipoTransac',  '00',           1, 'Comisiones                                                                                         ', 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 18, 'DJ1891', '2015', 'TipoTransac',  '01',           2, 'Acciones                                                                                           ', 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 19, 'DJ1891', '2015', 'TipoTransac',  '02',           3, 'Dólares                                                                                            ', 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 20, 'DJ1891', '2015', 'TipoTransac',  '03',           4, 'Oro                                                                                                ', 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 21, 'DJ1891', '2015', 'TipoTransac',  '04',           5, 'Otras Operaciones de Futuro                                                                        ', 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 22, 'DJ1891', '2015', 'TipoTransac',  '05',           6, 'Opciones                                                                                           ', 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 23, 'DJ1891', '2015', 'TipoTransac',  '06',           7, 'Documentos de Renta Fija                                                                           ', 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 24, 'DJ1891', '2015', 'TipoTransac',  '07',           8, 'Títulos Emitidos por Bolsas de Productos Agropecuarios sobre Certificados de Depósitos de Productos', 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 25, 'DJ1891', '2015', 'TipoTransac',  '08',           9, 'Cuotas de Fondos de Inversión                                                                      ', 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 26, 'DJ1891', '2015', 'TipoTransac',  '09',          10, 'Otros Instrumentos de Inversión                                                                    ', 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 27, 'DJ1891', '2015', 'TipoTransac',  '10',          11, 'Aporte y Adquisición de Valores (ETF - Exchange Traded Funds)                                      ', 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 28, 'DJ1891', '2015', 'TipoTransac',  '99',          12, 'Otros que no sean instrumentos de inversión                                                        ', 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 29, 'DJ1891', '2015', 'TipoDocu',     'C',            1, 'Nota de Crédito',           'S', '', 'Facturacion' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 30, 'DJ1891', '2015', 'TipoDocu',     'D',            2, 'Nota de Débito ',           'S', '', 'Facturacion' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 31, 'DJ1891', '2015', 'TipoDocu',     'F',            3, 'Factura        ',           'S', '', 'Facturacion' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 32, 'DJ1891', '2015', 'TipoDocu',     'O',            4, 'Otro           ',           'S', '', 'Facturacion' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 33, 'DJ1891', '2015', 'Origen',       'CARGAEXT',     1, 'Carga Masiva Externa ',     'S', '', ''            from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 34, 'DJ1891', '2015', 'Origen',       'INGMANUAL',    2, 'Ingreso Manual       ',     'S', '', ''            from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 35, 'DJ1891', '2015', 'Origen',       'OPERACIONAL',  3, 'Sebra CB             ',     'S', '', ''            from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 36, 'DJ1891', '2015', 'TipoOper',     'CO',           1, 'Compra           ',         'S', '', ''            from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 37, 'DJ1891', '2015', 'TipoOper',     'CP',           2, 'Compra con Pacto ',         'S', '', ''            from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 38, 'DJ1891', '2015', 'TipoOper',     'CT',           3, 'Compra Término   ',         'S', '', ''            from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 39, 'DJ1891', '2015', 'TipoOper',     'VE',           4, 'Venta            ',         'S', '', ''            from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 40, 'DJ1891', '2015', 'TipoOper',     'VP',           5, 'Venta con Pacto  ',         'S', '', ''            from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 41, 'DJ1891', '2015', 'TipoOper',     'VT',           6, 'Venta Término    ',         'S', '', ''            from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 42, 'DJ1891', '2015', 'VistoBueno',   'N',            1, 'No Revisados por Usuario ', 'S', '', ''            from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 43, 'DJ1891', '2015', 'VistoBueno',   'S',            2, 'Revisados por Usuario    ', 'S', '', ''            from dummy;

/*==============================================================*/
/* HOMOLOGACIONES                                               */
/*==============================================================*/

/*==============================================================*/
/* VALIDACIONES                                                 */
/*==============================================================*/

/*==============================================================*/
/* CAMPOS                                                       */
/*==============================================================*/

insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"( "IdCampo", "Esquema", "Version", "TipoDiccionario", "Orden", "Nombre", "Titulo", "Descripcion", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "Visible", "OrdenCargaExterna", "LargoCargaExterna", "FijarColumna")
select 1 , 'DJ1891', '2015','', 1, 'ID'                       , 'ID'                       , 'Identificador único'  , 'ID'        , 0, '0', 'S', 'N', 'N', 'S',  'S', 0, 0, 'S' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"( "IdCampo", "Esquema", "Version", "TipoDiccionario", "Orden", "Nombre", "Titulo", "Descripcion", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "Visible", "OrdenCargaExterna", "LargoCargaExterna", "FijarColumna")
select 2 , 'DJ1891', '2015','TipoDocu', 2, 'TipoDocumento' , 'Tipo Documento' , 'Tipo del documento'   , 'NEMO'       , 0, '0', 'S', 'S', 'N', 'S',  'S', 0, 0, 'N' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"( "IdCampo", "Esquema", "Version", "TipoDiccionario", "Orden", "Nombre", "Titulo", "Descripcion", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "Visible", "OrdenCargaExterna", "LargoCargaExterna", "FijarColumna")
select 3 , 'DJ1891', '2015','', 3, 'NumeroDocumento', 'Número Documento', 'Número del documento' , 'ID'         , 0, '0', 'S', 'S', 'N', 'S',  'S', 0, 0, 'N' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"( "IdCampo", "Esquema", "Version", "TipoDiccionario", "Orden", "Nombre", "Titulo", "Descripcion", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "Visible", "OrdenCargaExterna", "LargoCargaExterna", "FijarColumna")
select 4 , 'DJ1891', '2015','', 4, 'FechaDocumento', 'Fecha Documento', 'Fecha del documento'  , 'DATE'       , 0, '0', 'S', 'S', 'N', 'S',  'S', 0, 0, 'N' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"( "IdCampo", "Esquema", "Version", "TipoDiccionario", "Orden", "Nombre", "Titulo", "Descripcion", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "Visible", "OrdenCargaExterna", "LargoCargaExterna", "FijarColumna")
select 5 , 'DJ1891', '2015','', 5, 'RutEmisor'            , 'RUT Emisor'     , 'RUT del emisor'       , 'RUT'     , 0, '0', 'S', 'S', 'N', 'S',  'S', 0, 0, 'N' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"( "IdCampo", "Esquema", "Version", "TipoDiccionario", "Orden", "Nombre", "Titulo", "Descripcion", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "Visible", "OrdenCargaExterna", "LargoCargaExterna", "FijarColumna")
select 6 , 'DJ1891', '2015','', 6, 'Nemotecnico'   , 'Nemotécnico'    , 'Nemotécnico del activo', 'NEMO'       , 0, '0', 'S', 'S', 'N', 'S',  'S', 0, 0, 'N' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"( "IdCampo", "Esquema", "Version", "TipoDiccionario", "Orden", "Nombre", "Titulo", "Descripcion", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "Visible", "OrdenCargaExterna", "LargoCargaExterna", "FijarColumna")
select 7 , 'DJ1891', '2015','', 7, 'RutCliente'    , 'RUT Cliente'    , 'RUT del Cliente'      , 'RUT'        , 0, '0', 'S', 'S', 'N', 'S',  'S', 0, 0, 'S' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"( "IdCampo", "Esquema", "Version", "TipoDiccionario", "Orden", "Nombre", "Titulo", "Descripcion", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "Visible", "OrdenCargaExterna", "LargoCargaExterna", "FijarColumna")
select 8 , 'DJ1891', '2015','', 8, 'Cantidad'      , 'Cantidad'       , 'Cantidad'             , 'QTY'        , 0, '0', 'S', 'S', 'N', 'S',  'S', 0, 0, 'N' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"( "IdCampo", "Esquema", "Version", "TipoDiccionario", "Orden", "Nombre", "Titulo", "Descripcion", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "Visible", "OrdenCargaExterna", "LargoCargaExterna", "FijarColumna")
select 9 , 'DJ1891', '2015','', 9, 'MontoCompra'   , 'Monto Compra'   , 'Monto de la compra'   , 'AMOUNT', 0, '0', 'S', 'S', 'N', 'S',  'S', 0, 0, 'N' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"( "IdCampo", "Esquema", "Version", "TipoDiccionario", "Orden", "Nombre", "Titulo", "Descripcion", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "Visible", "OrdenCargaExterna", "LargoCargaExterna", "FijarColumna")
select 10, 'DJ1891', '2015','', 10, 'MontoVenta'    , 'Monto Venta'    , 'Monto de la venta'    , 'AMOUNT', 0, '0', 'S', 'S', 'N', 'S',  'S', 0, 0, 'N' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"( "IdCampo", "Esquema", "Version", "TipoDiccionario", "Orden", "Nombre", "Titulo", "Descripcion", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "Visible", "OrdenCargaExterna", "LargoCargaExterna", "FijarColumna")
select 11, 'DJ1891', '2015','TipoTransac', 11, 'TipoTransaccion', 'Tipo Transacción', 'Tipo de la transacción', 'NEMO'    , 0, '0', 'S', 'S', 'N', 'S',  'S', 0, 0, 'N' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"( "IdCampo", "Esquema", "Version", "TipoDiccionario", "Orden", "Nombre", "Titulo", "Descripcion", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "Visible", "OrdenCargaExterna", "LargoCargaExterna", "FijarColumna")
select 12, 'DJ1891', '2015','TipoOper', 12, 'IdTipoOperacion', 'Tipo Operación' , 'Tipo de la operación' , 'NEMO'   , 0, '0', 'S', 'S', 'N', 'S',  'S', 0, 0, 'N' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"( "IdCampo", "Esquema", "Version", "TipoDiccionario", "Orden", "Nombre", "Titulo", "Descripcion", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "Visible", "OrdenCargaExterna", "LargoCargaExterna", "FijarColumna")
select 13, 'DJ1891', '2015','TipoProducto', 13, 'IdTipoProducto', 'Tipo Producto'  , 'Tipo de producto'     , 'NEMO'   , 0, '0', 'S', 'S', 'N', 'S',  'S', 0, 0, 'N' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"( "IdCampo", "Esquema", "Version", "TipoDiccionario", "Orden", "Nombre", "Titulo", "Descripcion", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "Visible", "OrdenCargaExterna", "LargoCargaExterna", "FijarColumna")
select 14, 'DJ1891', '2015','', 14, 'IdBaseDocumento', 'ID Base'        , 'ID Documento Base'    , 'ID'     , 0, '0', 'S', 'N', 'N', 'S',  'S', 0, 0, 'N' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"( "IdCampo", "Esquema", "Version", "TipoDiccionario", "Orden", "Nombre", "Titulo", "Descripcion", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "Visible", "OrdenCargaExterna", "LargoCargaExterna", "FijarColumna")
select 15, 'DJ1891', '2015','', 15, 'IdCliente'     , 'ID Cliente'     , 'ID del cliente'       , 'ID'     , 0, '0', 'S', 'S', 'N', 'S',  'S', 0, 0, 'N' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"( "IdCampo", "Esquema", "Version", "TipoDiccionario", "Orden", "Nombre", "Titulo", "Descripcion", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "Visible", "OrdenCargaExterna", "LargoCargaExterna", "FijarColumna")
select 16, 'DJ1891', '2015','', 16, 'IdPortafolio'  , 'ID Portafolio'  , 'ID del portafolio'    , 'ID'     , 0, '0', 'S', 'S', 'N', 'S',  'S', 0, 0, 'N' from dummy;

insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"( "IdCampo", "Esquema", "Version", "TipoDiccionario", "Orden", "Nombre", "Titulo", "Descripcion", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "Visible", "OrdenCargaExterna", "LargoCargaExterna", "FijarColumna")
select 17 , 'DJ1891', '2016','', 1, 'ID'                      , 'ID'                       , 'Identificador único'  , 'ID'        , 0, '0', 'S', 'N', 'N', 'S',  'S', 0, 0, 'S' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"( "IdCampo", "Esquema", "Version", "TipoDiccionario", "Orden", "Nombre", "Titulo", "Descripcion", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "Visible", "OrdenCargaExterna", "LargoCargaExterna", "FijarColumna")
select 18 , 'DJ1891', '2016','TipoDocu', 2, 'TipoDocumento' , 'Tipo Documento' , 'Tipo del documento'   , 'NEMO'      , 0, '0', 'S', 'S', 'N', 'S',  'S', 0, 0, 'N' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"( "IdCampo", "Esquema", "Version", "TipoDiccionario", "Orden", "Nombre", "Titulo", "Descripcion", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "Visible", "OrdenCargaExterna", "LargoCargaExterna", "FijarColumna")
select 19 , 'DJ1891', '2016','', 3, 'NumeroDocumento', 'Número Documento', 'Número del documento' , 'ID'        , 0, '0', 'S', 'S', 'N', 'S',  'S', 0, 0, 'N' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"( "IdCampo", "Esquema", "Version", "TipoDiccionario", "Orden", "Nombre", "Titulo", "Descripcion", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "Visible", "OrdenCargaExterna", "LargoCargaExterna", "FijarColumna")
select 20 , 'DJ1891', '2016','', 4, 'FechaDocumento', 'Fecha Documento', 'Fecha del documento'  , 'DATE'      , 0, '0', 'S', 'S', 'N', 'S',  'S', 0, 0, 'N' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"( "IdCampo", "Esquema", "Version", "TipoDiccionario", "Orden", "Nombre", "Titulo", "Descripcion", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "Visible", "OrdenCargaExterna", "LargoCargaExterna", "FijarColumna")
select 21 , 'DJ1891', '2016','', 5, 'RutEmisor'           , 'RUT Emisor'     , 'RUT del emisor'       , 'RUT'     , 0, '0', 'S', 'S', 'N', 'S',  'S', 0, 0, 'N' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"( "IdCampo", "Esquema", "Version", "TipoDiccionario", "Orden", "Nombre", "Titulo", "Descripcion", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "Visible", "OrdenCargaExterna", "LargoCargaExterna", "FijarColumna")
select 22 , 'DJ1891', '2016','', 6, 'Nemotecnico'   , 'Nemotécnico'    , 'Nemotécnico del activo', 'NEMO'      , 0, '0', 'S', 'S', 'N', 'S',  'S', 0, 0, 'N' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"( "IdCampo", "Esquema", "Version", "TipoDiccionario", "Orden", "Nombre", "Titulo", "Descripcion", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "Visible", "OrdenCargaExterna", "LargoCargaExterna", "FijarColumna")
select 23 , 'DJ1891', '2016','', 7, 'RutCliente'    , 'RUT Cliente'    , 'RUT del Cliente'      , 'RUT'       , 0, '0', 'S', 'S', 'N', 'S',  'S', 0, 0, 'S' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"( "IdCampo", "Esquema", "Version", "TipoDiccionario", "Orden", "Nombre", "Titulo", "Descripcion", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "Visible", "OrdenCargaExterna", "LargoCargaExterna", "FijarColumna")
select 24 , 'DJ1891', '2016','', 8, 'Cantidad'      , 'Cantidad'       , 'Cantidad'             , 'QTY'       , 0, '0', 'S', 'S', 'N', 'S',  'S', 0, 0, 'N' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"( "IdCampo", "Esquema", "Version", "TipoDiccionario", "Orden", "Nombre", "Titulo", "Descripcion", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "Visible", "OrdenCargaExterna", "LargoCargaExterna", "FijarColumna")
select 25 , 'DJ1891', '2016','', 9, 'MontoCompra'   , 'Monto Compra'   , 'Monto de la compra'   , 'AMOUNT', 0, '0', 'S', 'S', 'N', 'S',  'S', 0, 0, 'N' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"( "IdCampo", "Esquema", "Version", "TipoDiccionario", "Orden", "Nombre", "Titulo", "Descripcion", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "Visible", "OrdenCargaExterna", "LargoCargaExterna", "FijarColumna")
select 26, 'DJ1891', '2016','', 10, 'MontoVenta'    , 'Monto Venta'    , 'Monto de la venta'    , 'AMOUNT', 0, '0', 'S', 'S', 'N', 'S',  'S', 0, 0, 'N' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"( "IdCampo", "Esquema", "Version", "TipoDiccionario", "Orden", "Nombre", "Titulo", "Descripcion", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "Visible", "OrdenCargaExterna", "LargoCargaExterna", "FijarColumna")
select 27, 'DJ1891', '2016','TipoTransac', 11, 'TipoTransaccion', 'Tipo Transacción', 'Tipo de la transacción', 'NEMO'    , 0, '0', 'S', 'S', 'N', 'S',  'S', 0, 0, 'N' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"( "IdCampo", "Esquema", "Version", "TipoDiccionario", "Orden", "Nombre", "Titulo", "Descripcion", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "Visible", "OrdenCargaExterna", "LargoCargaExterna", "FijarColumna")
select 28, 'DJ1891', '2016','TipoOper', 12, 'IdTipoOperacion', 'Tipo Operación' , 'Tipo de la operación' , 'NEMO'   , 0, '0', 'S', 'S', 'N', 'S',  'S', 0, 0, 'N' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"( "IdCampo", "Esquema", "Version", "TipoDiccionario", "Orden", "Nombre", "Titulo", "Descripcion", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "Visible", "OrdenCargaExterna", "LargoCargaExterna", "FijarColumna")
select 29, 'DJ1891', '2016','TipoProducto', 13, 'IdTipoProducto', 'Tipo Producto'  , 'Tipo de producto'     , 'NEMO'   , 0, '0', 'S', 'S', 'N', 'S',  'S', 0, 0, 'N' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"( "IdCampo", "Esquema", "Version", "TipoDiccionario", "Orden", "Nombre", "Titulo", "Descripcion", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "Visible", "OrdenCargaExterna", "LargoCargaExterna", "FijarColumna")
select 30, 'DJ1891', '2016','', 14, 'IdBaseDocumento', 'ID Base'        , 'ID Documento Base'    , 'ID'     , 0, '0', 'S', 'N', 'N', 'S',  'S', 0, 0, 'N' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"( "IdCampo", "Esquema", "Version", "TipoDiccionario", "Orden", "Nombre", "Titulo", "Descripcion", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "Visible", "OrdenCargaExterna", "LargoCargaExterna", "FijarColumna")
select 31, 'DJ1891', '2016','', 15, 'IdCliente'     , 'ID Cliente'     , 'ID del cliente'       , 'ID'     , 0, '0', 'S', 'S', 'N', 'S',  'S', 0, 0, 'N' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"( "IdCampo", "Esquema", "Version", "TipoDiccionario", "Orden", "Nombre", "Titulo", "Descripcion", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "Visible", "OrdenCargaExterna", "LargoCargaExterna", "FijarColumna")
select 32, 'DJ1891', '2016','', 16, 'IdPortafolio'  , 'ID Portafolio'  , 'ID del portafolio'    , 'ID'     , 0, '0', 'S', 'S', 'N', 'S',  'S', 0, 0, 'N' from dummy;

/*==============================================================*/
/* DOCUMENTOS                                                   */
/*==============================================================*/

insert into "BSCL"."BSCL.Normativa.BD::DOC.Documento"("IdDocumento", "IdTipoDocumento", "Esquema", "Version", "IdEmpresa", "FechaLimite",
            "IdEstadoDocumento", "FechaCreacion", "FechaPresentacion", "CantidadRegistrosCargados", "FuenteDatos", "ProcesoFuente", "IdTitularDocumento", "DocumentoIdentidadTitular",
            "IdRepresentanteLegal", "DocumentoIdentidadRepLegal", "DireccionFisicaTitular", "DireccionElectronicaTitular", "TelefonoTitular", "FolioPresentacionDocumento")
select  1,'DECLACORR','DJ1891','2015',1,'2016-03-31'
       ,'A','','',11,'SISTEMA','CargaInicial' , 1,' 90947000-1',
        2,' 14448678-1',' EL Golf 47', 'contabilidad@imtrust.cl', '56 2 25689854','I9153103' from dummy;

/*==============================================================*/
/* DETALLES                                                     */
/*==============================================================*/

insert into "BSCL"."BSCL.Normativa.BD::DOC.Detalle"
select ROW_NUMBER() OVER (order by "IdComprobanteDetalle") as IdDetalleDocumento,
        1,
        'S',
        '',
        'SISTEMA'
from     "BSCL"."BSCL.Facturacion.BD::FAC.Comprobante" AS COMP
       ,"BSCL"."BSCL.Facturacion.BD::FAC.ComprobanteDetalle" AS DET
        LEFT OUTER JOIN "BSCL_VIRTUAL"."RUE_TBASSOC" AS SOC ON ((LTRIM(RTRIM(DET."NemotecnicoInstrumento")) = LTRIM(RTRIM(SOC."NEMO_SASA"))))
       ,"BSCL"."BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI
       ,"BSCL"."BSCL.SocioNegocio.BD::PER.Persona" AS PER
       ,"BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOC
where COMP."IdComprobante"= DET."IdComprobante"
AND COMP."IdCliente"= CLI."IdCliente"
AND CLI."IdPersona" = PER."IdPersona"
AND PER."IdDocumentoIdentidad" = DOC."IdDocumentoIdentidad";

/*==============================================================*/
/* DETALLES DJ1891                                              */
/*==============================================================*/

insert into "BSCL"."BSCL.Normativa.BD::TRI.DetalleDocumento1891"
("ID", "TipoDocumento", "NumeroDocumento", "FechaDocumento", "RutEmisor", "Nemotecnico",
"RutCliente", "Cantidad", "MontoCompra",    "MontoVenta"   ,"TipoTransaccion", "IdTipoOperacion",
"IdTipoProducto", "IdBaseDocumento", "IdCliente", "IdPortafolio")

select ROW_NUMBER() OVER (order by "IdComprobanteDetalle") as ID,
        'F' as "TipoDocumento",
        COMP."FolioComprobante"         as "NumeroDocumento",
        COMP."FechaEmision"             as "FechaDocumento",
        ifnull(SOC."RUT",'')            as "RutEmisor",
        DET."NemotecnicoInstrumento"    as "Nemotecnico",
        DOC."DocumentoIdentidad"        as "RutCliente",
        DET."Cantidad"                  as "Cantidad",
        (CASE WHEN COMP."IdTipoOperacion" in('COMACN','COMSIM') then DET."Monto" ELSE 0 END) as MontoCompra,
        (CASE WHEN COMP."IdTipoOperacion" in('VENSIM','VENACN') then DET."Monto" ELSE 0 END) as MontoVenta,
        '01'                            as "TipoTransaccion",
        COMP."IdTipoOperacion"          as "IdTipoOperacion",
        COMP."IdTipoProducto"           as "IdTipoProducto",
        ROW_NUMBER () OVER (order by "IdComprobanteDetalle") as "IdBaseDocumento",
        COMP."IdCliente",
        COMP."IdPortafolio"
FROM     "BSCL"."BSCL.Facturacion.BD::FAC.Comprobante" AS COMP
       ,"BSCL"."BSCL.Facturacion.BD::FAC.ComprobanteDetalle" AS DET
        LEFT OUTER JOIN "BSCL_VIRTUAL"."RUE_TBASSOC" AS SOC ON ((LTRIM(RTRIM(DET."NemotecnicoInstrumento")) = LTRIM(RTRIM(SOC."NEMO_SASA"))))
       ,"BSCL"."BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI
       ,"BSCL"."BSCL.SocioNegocio.BD::PER.Persona" AS PER
       ,"BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOC
where COMP."IdComprobante"= DET."IdComprobante"
AND COMP."IdCliente"= CLI."IdCliente"
AND CLI."IdPersona"= PER."IdPersona"
AND PER."IdDocumentoIdentidad" = DOC."IdDocumentoIdentidad";

create sequence "BSCL.Normativa.BD.Secuencias::Detalle"
    start with 1
    increment by 1
    minvalue 1;
    
/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/

commit;


