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
-- LIBERACION SEMANA: 20-05-2016 
--

--################################################
--  JG.DOF
--################################################
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('NUEDOF','','SOC','NUEVA DECLARACIÓN DE ORIGEN DE FONDOS','VIG'); 
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('MODDOF','','SOC','MODIFICACIÓN DECLARACIÓN DE ORIGEN DE FONDOS','VIG'); 
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('NUEDOF','S');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('MODDOF','S');
INSERT INTO "BSCL.Plataforma.BD::AUT.TipoEvento" 
VALUES('NUEDOF',1,'SOC','AUT_DOF_NUE','Creación Declaración Origen de Fondos',1,'S','' ,'' ,0,0,'VIG');
INSERT INTO "BSCL.Plataforma.BD::AUT.TipoEvento" 
VALUES('MODDOF',1,'SOC','AUT_DOF_MOD','Modificación Declaración Origen de Fondos',1,'S','' ,'' ,0,0,'VIG');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (324, 'BSCL.SocioNegocio.BD.Servicios::CLI.ModificarDeclaracionOrigenFondos' , 1,  'P', ''); 
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (325, 'BSCL.SocioNegocio.BD.Servicios::CLI.CrearDeclaracionOrigenFondos' , 1,  'P', ''); 
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoRegistro" ("IdTipoRegistro", "Descripcion") VALUES('CLIENTEDOF', 'DECLARACIÓN DE ORIGEN DE FONDOS');

--################################################
--  MMACHMAR - Limites
--################################################

insert into "BSCL.Plataforma.BD::AUT.TipoEvento" ("IdTipoEvento","IdEmpresa", "IdModulo","Codigo","Nombre","HorasVigencia","EtapasSecuenciales","IdTipoProducto","IdTipoOperacion","IdUsuarioSuperAutorizador","IdRolSuperAutorizador","IdEstado")
values('MLICTP',1,'PLA','AUT_LICTP_MOD','MODIFICACIÓN LIMITE CUENTA PROPIA',1,'S','' ,'' ,0,0,'VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('MLICTP','LI','PLA','MODIFICACIÓN LIMITE CUENTA PROPIA','VIG'); 
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion")
values('MLICTP','S');

----Agregar campos
SET SCHEMA "BSCL";
ALTER TABLE "BSCL.Plataforma.BD::LIM.Limite" ADD ("IdClasificacionRiesgo" BIGINT not null default '0');
ALTER TABLE "BSCL.Plataforma.BD::LIM.Limite" ADD ("IdTipoInstrumento" NVARCHAR(3) not null default '');

-- TipoLimite para los limites de Cuenta Propia Emisor
INSERT INTO "BSCL.Plataforma.BD::LIM.TipoLimite"("IdTipoLimite","IdEmpresa","Nombre","EsCuentaPropia","IdComportamiento","IdGrupoLimite","Categoria","IdEstado","IdTipoEvento","Prioridad")
VALUES('LIMCTPEMISOR',1,'LÍMITE CUENTA PROPIA EMISOR','N','AUT','RV','OPERACION','ACT','LIECTP',1);

-- inscripcion de Servicios
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(318,'BSCL.Plataforma.BD.Servicios::LIM.ModificarLimiteCtaPropia',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(319,'BSCL.Plataforma.BD.Servicios::LIM.AgregarLimiteEmisor',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(320,'BSCL.Plataforma.BD.Servicios::LIM.ModificarLimiteEmisor',0,'P','');

-- valor defecto sistema
delete from "BSCL"."BSCL.Plataforma.BD::PLA.ValorDefectoSistema" where "IdValorDefectoSistema" like 'IdEstadoLimiteCuentaPropiaRV%';
delete from "BSCL"."BSCL.Plataforma.BD::PLA.ValorDefectoSistema" where "IdValorDefectoSistema" like 'IdTipoComportamientoLimCtaProRV%';
delete from "BSCL"."BSCL.Plataforma.BD::PLA.ValorDefectoSistema" 
where "IdValorDefectoSistema" in ('IdMonedaLimiteCuentaPropiaRVMargenTasas','IdTipoComportamientoLimCtaProCartera');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ValorDefectoSistema" ("IdValorDefectoSistema","Nombre","Valor","IdEmpresa","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEstado") 
VALUES('IdEntidadClasificadoraRiesgoLimite','Entidad Clasificadora Riesgo Límite ','STDANDPOOR',1,3,NOW(),NOW(),'ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ValorDefectoSistema" ("IdValorDefectoSistema","Nombre","Valor","IdEmpresa","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEstado") 
VALUES('IdEstadoLimiteCuentaPropia','Estado Límite Cuenta Propia','ACT',1,3,NOW(),NOW(),'ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ValorDefectoSistema" ("IdValorDefectoSistema","Nombre","Valor","IdEmpresa","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEstado") 
VALUES('IdEstadoEmisorLimiteCuentaPropia','Estado Emisor Límite Cuenta Propia','ACT',1,3,NOW(),NOW(),'ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ValorDefectoSistema" ("IdValorDefectoSistema","Nombre","Valor","IdEmpresa","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEstado") 
VALUES('IdMonedaLimiteCuentaPropia','Moneda Límite Cuenta Propia','ACT',1,3,NOW(),NOW(),'ACT');

SET SCHEMA "BSCL";
DROP TABLE "BSCL.Plataforma.BD::LIM.LimiteClasificacionRiesgo";
DROP VIEW "BSCL.Plataforma.BD.Vistas::LIM.ConsultarLimitesClasificacionRiesgo";


--################################################
-- JG - Regulariza Piezas varias
--################################################
DROP PROCEDURE  "BSCL.Plataforma.BD.Servicios::COM.RechazaAutorizacionPreferenciaPago";
DROP PROCEDURE  "BSCL.SocioNegocio.BD.Servicios::RequiereAutorizacionPortafolio";
DROP PROCEDURE  "BSCL.SocioNegocio.BD.Servicios::CLI.CrearPersona";
UPDATE "BSCL.Plataforma.BD::PLA.Servicio" SET "RutaServicio" = 'BSCL.SocioNegocio.BD.Servicios::PER.CrearPersona'
WHERE "RutaServicio" = 'BSCL.SocioNegocio.BD.Servicios::CLI.CrearPersona';
DROP PROCEDURE  "BSCL.SocioNegocio.BD.Servicios::CrearTipoRelacionDestino";
DROP PROCEDURE "BSCL.SocioNegocio.BD.Servicios::ConsultarSocioDeNegocioDetallado";
DROP VIEW "BSCL.Cliente.BD::CLI.Servicios.ConsultarEstadoCliente";  
DROP VIEW "BSCL.Cliente.BD::CLI.Servicios.ConsultarSegmentoCliente";

DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias::DetallePlantillaCondicionesOperativas";
DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias::EtapaCondicionOperativa";
DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias::TipoEtapa";
DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias::CondicionOperativa";
DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias::EtapaOperacion";
DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias::EtapaGrupoCondicionesOperativas";
DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias::GrupoCondicionesOperativas";
DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias::OperacionUsuario";

--################################################
--  JPZ INICIO
--################################################
DROP TABLE "BSCL.Plataforma.BD::INS.TipoRelacionInstrumento" cascade;
DROP TABLE "BSCL.Plataforma.BD::INS.RelacionInstrumento" cascade;


CREATE COLUMN TABLE "BSCL.Plataforma.BD::INS.TipoRelacionInstrumento"(
	"IdTipoRelacionInstrumento"	NVARCHAR(6)	NOT NULL,
	"TipoRelacionInstrumento"	NVARCHAR(100)	NOT NULL DEFAULT '',
	"IdEstado"					NVARCHAR(3)		DEFAULT 'ACT' NOT NULL,
  	PRIMARY KEY ("IdTipoRelacionInstrumento")
);


CREATE COLUMN TABLE "BSCL.Plataforma.BD::INS.RelacionInstrumento" (
	"IdRelacionInstrumento" 					BIGINT 			not null,
	"IdTipoRelacionInstrumento"					NVARCHAR(6) 	default '' 				not null,
	"IdClasificacionTipoInstrumento"			NVARCHAR(3) 	default ''				not null,
	"IdTipoInstrumento"  		 				NVARCHAR(3)   	default ''				not null ,
	"IdInstrumento"								BIGINT			default '0' 			not null,
	"Nemo" 										NVARCHAR(40)	default '' 				not null,
	"IdClasificacionTipoInstrumentoRelacionado"	NVARCHAR(3) 	default ''				not null,	
	"IdTipoInstrumentoRelacionado"  			NVARCHAR(3)   	default ''				not null ,
	"IdInstrumentoRelacionado"					BIGINT			default '0' 			not null,
	"NemoRelacionado"							NVARCHAR(40)	default '' 				not null,
	"Descripcion"								NVARCHAR(100)	default '' 				not null,
	"FechaInicioRelacion"						DATE            default '0001-01-01' 	not null,
	"FechaFinRelacion"							DATE            default '0001-01-01' 	not null,
	"IdUsuario" 								BIGINT			default '0' 			not null,
	"IdEstado" 									NVARCHAR(3) 	default '' 				not null,
	 PRIMARY KEY ("IdRelacionInstrumento")
);
--
	alter table "BSCL.Plataforma.BD::INS.RelacionInstrumento"
	   add constraint "FK_IdTipoRelacionInstrumento_RelacionInstrumento" foreign key ("IdTipoRelacionInstrumento")
	        references "BSCL.Plataforma.BD::INS.TipoRelacionInstrumento" ("IdTipoRelacionInstrumento") on delete restrict on update restrict;

	alter table "BSCL.Plataforma.BD::INS.RelacionInstrumento"
	   add constraint "FK_ClasificacionTipoInstrumento_RelacionInstrumento" foreign key ("IdClasificacionTipoInstrumento")
	        references "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" ("IdClasificacionTipoInstrumento") on delete restrict on update restrict;

	alter table "BSCL.Plataforma.BD::INS.RelacionInstrumento"
	   add constraint "FK_TipoInstrumento_RelacionInstrumento" foreign key ("IdTipoInstrumento")
	        references "BSCL.Plataforma.BD::INS.TipoInstrumento" ("IdTipoInstrumento") on delete restrict on update restrict;

	alter table "BSCL.Plataforma.BD::INS.RelacionInstrumento"
	   add constraint "FK_ClasificacionTipoInstrumentoRelacionado_RelacionInstrumento" foreign key ("IdClasificacionTipoInstrumentoRelacionado")
	        references "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" ("IdClasificacionTipoInstrumento") on delete restrict on update restrict;

	alter table "BSCL.Plataforma.BD::INS.RelacionInstrumento"
	   add constraint "FK_TipoInstrumentoRelacionado_RelacionInstrumento" foreign key ("IdTipoInstrumentoRelacionado")
	        references "BSCL.Plataforma.BD::INS.TipoInstrumento" ("IdTipoInstrumento") on delete restrict on update restrict;

	alter table "BSCL.Plataforma.BD::INS.RelacionInstrumento"
	   add constraint "FK_Estado_RelacionInstrumento" foreign key ("IdEstado")
	      references "BSCL.Plataforma.BD::PLA.Estado" ("IdEstado") on delete restrict on update restrict;		     
	  
INSERT INTO "BSCL.Plataforma.BD::INS.TipoRelacionInstrumento" ("IdTipoRelacionInstrumento","TipoRelacionInstrumento","IdEstado") 
		VALUES ('OPSUAC','Opción de Suscripción Acciones','ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoRelacionInstrumento" ("IdTipoRelacionInstrumento","TipoRelacionInstrumento","IdEstado") 
		VALUES ('PRIEMI','Primera Emisión','ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoRelacionInstrumento" ("IdTipoRelacionInstrumento","TipoRelacionInstrumento","IdEstado") 
		VALUES ('SEGEMI','Segunda Emisión','ACT');
	  
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('ABDVEP','AB','CTA','DIVIDENDO EMISION PAGADA','VIG');

DELETE FROM "BSCL.Plataforma.BD::PLA.Servicio"
WHERE "IdServicio" IN (321,322);

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (321, 'BSCL.Plataforma.BD.Servicios::PLA.CrearRelacionInstrumento' , 1,  'P', ''); 

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (322, 'BSCL.Plataforma.BD.Servicios::PLA.ModificarRelacionInstrumento' , 1,  'P', ''); 

--################################################
--  JPZ FIN
--################################################


--################################################
--  JG. Preferencia General Sobregiro Cta Inv.
--################################################
insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
	"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
	"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (42, 'PORTAFOLIO', 1, 'N', 'CHECKBOX', 'Permite sobregiro cuenta inversión','GEN_SOBCTA', 'GRUPOGENERAL', 'PRE', 'ACT', 0, 0, 0, 'N' );


