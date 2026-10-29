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
-- LIBERACION SEMANA: 13-05-2016 
--



-- JPZ INICIO

CREATE COLUMN TABLE "BSCL.Plataforma.BD::INS.TipoRelacionInstrumento"(
	"IdTipoRelacionInstrumento"	NVARCHAR(6)		NOT NULL,
	"TipoRelacionInstrumento"	NVARCHAR(100)	NOT NULL DEFAULT '',
	"IdEstado"					NVARCHAR(3)		DEFAULT 'ACT' NOT NULL,
  	PRIMARY KEY ("IdTipoRelacionInstrumento")
);

INSERT INTO "BSCL.Plataforma.BD::INS.TipoRelacionInstrumento" ("IdTipoRelacionInstrumento","TipoRelacionInstrumento","IdEstado") 
		VALUES ('OPSUAC','Opción de Suscripción Acciones','ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoRelacionInstrumento" ("IdTipoRelacionInstrumento","TipoRelacionInstrumento","IdEstado") 
		VALUES ('PRIEMI','Primera Emisión','ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoRelacionInstrumento" ("IdTipoRelacionInstrumento","TipoRelacionInstrumento","IdEstado") 
		VALUES ('SEGEMI','Segunda Emisión','ACT');

-- JPZ FIN

-- CGO INICIO

CREATE COLUMN TABLE "BSCL.Plataforma.BD::INS.RelacionInstrumento" (
	"IdRelacionInstrumento" 		BIGINT 			not null,
	"IdTipoRelacionInstrumento"		NVARCHAR(6) 	default '' 				not null,
	"IdTipoInstrumento"  		 	NVARCHAR(3)   	default ''				not null ,
	"IdInstrumento"					BIGINT			default '0' 			not null,
	"Nemo" 							NVARCHAR(40)	default '' 				not null,
	"IdTipoInstrumentoRelacionado"  NVARCHAR(3)   	default ''				not null ,
	"IdInstrumentoRelacionado"		BIGINT			default '0' 			not null,
	"NemoRelacionado"				NVARCHAR(40)	default '' 				not null,
	"Descripcion"					NVARCHAR(100)	default '' 				not null,
	"FechaInicioRelacion"			DATE            default '0001-01-01' 	not null,
	"FechaFinRelacion"				DATE            default '0001-01-01' 	not null,
	"IdUsuario" 					BIGINT			default '0' 			not null,
	"IdEstado" 						NVARCHAR(3) 	default '' 				not null,
	 PRIMARY KEY ("IdRelacionInstrumento")
);

	alter table "BSCL.Plataforma.BD::INS.RelacionInstrumento"
	   add constraint "FK_IdTipoRelacionInstrumento_RelacionInstrumento" foreign key ("IdTipoRelacionInstrumento")
	        references "BSCL.Plataforma.BD::INS.TipoRelacionInstrumento" ("IdTipoRelacionInstrumento") on delete restrict on update restrict;


	alter table "BSCL.Plataforma.BD::INS.RelacionInstrumento"
	   add constraint "FK_TipoInstrumento_RelacionInstrumento" foreign key ("IdTipoInstrumento")
	        references "BSCL.Plataforma.BD::INS.TipoInstrumento" ("IdTipoInstrumento") on delete restrict on update restrict;

	alter table "BSCL.Plataforma.BD::INS.RelacionInstrumento"
	   add constraint "FK_TipoInstrumentoRelacionado_RelacionInstrumento" foreign key ("IdTipoInstrumentoRelacionado")
	        references "BSCL.Plataforma.BD::INS.TipoInstrumento" ("IdTipoInstrumento") on delete restrict on update restrict;

			
	alter table "BSCL.Plataforma.BD::INS.RelacionInstrumento"
	   add constraint "FK_Estado_RelacionInstrumento" foreign key ("IdEstado")
	      references "BSCL.Plataforma.BD::PLA.Estado" ("IdEstado") on delete restrict on update restrict;		     


CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::RelacionInstrumento"

       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;       
-- CGO FIN

--################################################
--    JVILLAGRAN - Tesoreria
--################################################

-- Servicio anulación de comprobantes
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(313,'BSCL.Tesoreria.BD.Servicios::AnularRegistros',0,'P','Anulación de movimientos y comprobantes de tesorería');

-- Concepto para autorización de anulación de comprobantes
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('ANCOTE','TA','TES','ANULACION COMPROBANTE TESORERIA LIQUIDADO','VIG'); 

-- Evento para autorización de anulación de comprobantes
INSERT INTO "BSCL"."BSCL.Plataforma.BD::AUT.TipoEvento" ("IdTipoEvento","IdEmpresa","IdModulo","Codigo","Nombre","HorasVigencia","EtapasSecuenciales","IdTipoProducto","IdTipoOperacion","IdUsuarioSuperAutorizador","IdRolSuperAutorizador","IdEstado")
VALUES ('ANCOTE',1,'TES','AUT_ANU_COM_TES','ANULACION COMPROBANTE TESORERIA LIQUIDADO',1,'S','' ,'' ,0,0,'VIG');

-- Autorización de anulación de comprobantes
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") 
VALUES('ANCOTE','S');


--################################################
--    CHERNANDEZ- Comprobante Ordenes
--################################################
CREATE COLUMN TABLE "BSCL.Plataforma.BD::PLA.LocalizacionMoneda" (
	 "IdLocalizacion" 		BIGINT NOT NULL ,
	 "Pais" 				NVARCHAR(10),
	 "IdMoneda"				NVARCHAR(3),
	 "SeparadorMillones"	INT,
	 PRIMARY KEY ("IdLocalizacion")
);
INSERT INTO "BSCL.Plataforma.BD::PLA.LocalizacionMoneda"("IdLocalizacion", "Pais","IdMoneda","SeparadorMillones") VALUES(1,'CL','CLP',3);
INSERT INTO "BSCL.Plataforma.BD::PLA.LocalizacionMoneda"("IdLocalizacion", "Pais","IdMoneda","SeparadorMillones") VALUES(2,'US','USD',2);
INSERT INTO "BSCL.Plataforma.BD::PLA.LocalizacionMoneda"("IdLocalizacion", "Pais","IdMoneda","SeparadorMillones") VALUES(3,'UK','EUR',3);
 

 
 --################################################
--  JG.Se incluye código en Detalle Indicador.
--################################################

ALTER TABLE "BSCL.Plataforma.BD::IND.DetalleIndicador" 
ADD("Codigo"	NVARCHAR(40) NOT NULL DEFAULT ''); -- TODO: IMPLICA MODIFICAR UI. Pend.
 
 --################################################
--  JG.Se incluye código en Detalle Indicador.
--################################################

ALTER TABLE "BSCL.Plataforma.BD::IND.DetalleIndicador" 
ADD("Codigo"	NVARCHAR(40) NOT NULL DEFAULT ''); -- TODO: IMPLICA MODIFICAR UI. Pend.
 
 DELETE FROM "BSCL.Plataforma.BD::IND.DetalleIndicador"  WHERE "IdTipoIndicador" in (5,35,40);
 	--DETALLE!
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (1,5,'BANCO','Banco');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (2,5,'CIASGURO','Compañía Seguro');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (3,5,'AFP','AFP');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (4,5,'AGF','AGF');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (5,5,'INTERVALORES','Intermediario Valores');
	--
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (1,35,'MENSUAL','Mensual');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (2,35,'SEMESTRAL','Semestral');
	--
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (1,40,'OE','Orden Escrita');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (2,40,'OV','Orden Verbal');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (3,40,'OD','Operador Directo');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (4,40,'OR','Sistema de Ruteo');
	/*INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (5,40,'ADMCARTERA','Administración Cartera');*/
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (5,40,'OM','Orden por otro medio');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (6,40,'TM','Cualquier medio');
	
	-- MMACHMAR - Detalle Indicador
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (1,21,'NORMAL','NORMAL');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (2,21,'CONTRATO','CONTRATO Seguro');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (3,21,'CONVENIO','CONVENIO');
	
	update "BSCL"."BSCL.Plataforma.BD::IND.TipoIndicador" set "ValorIndicadorDefecto" = '1' where "IdTipoIndicador" = 21;
	update "BSCL"."BSCL.Plataforma.BD::IND.Indicador" set "ValorIndicador" = '1' where "IdTipoIndicador" = 21;
 
--################################################
--  CFLORES.Tipo de Limite.
--################################################

SET SCHEMA "BSCL"; ---Agregar campo
ALTER TABLE "BSCL.Plataforma.BD::LIM.TipoLimite" 
ADD("IdTipoEvento"       NVARCHAR(6)   not null Default '',
	"Prioridad"        	BIGINT        not null DEFAULT 0); 
 
SET SCHEMA "BSCL"; ---Agregar campo
ALTER TABLE "BSCL.Plataforma.BD::LIM.EvaluacionLimites"
ADD("IdTipoEvento"       NVARCHAR(6)   not null Default '',
	"Prioridad"        	BIGINT        not null DEFAULT 0);


SET SCHEMA "BSCL"; ---Agregar campo
ALTER TABLE "BSCL.Plataforma.BD::LIM.LimiteEmisor"
ADD("FechaIngreso"       		SECONDDATE          default '0001-01-01 00:00:00' not null,
   "FechaCreacion"     			SECONDDATE          default '0001-01-01 00:00:00' not null,
   "FechaModificacion"  		SECONDDATE          default '0001-01-01 00:00:00' not null,
   "IdEstadoAutorizacion" 		NVARCHAR(3)         default 'PEN' not null,
   "Borrado" 					NVARCHAR(1) DEFAULT 'N',
   "IdUsuarioCreador"   		BIGINT  DEFAULT 0);

SET SCHEMA "BSCL"; ---Agregar secuencia
CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::LIM.IdLimiteEmisor"
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;	
       

SET SCHEMA "BSCL"; ---Borrar Vista
DROP VIEW  "BSCL.Plataforma.BD.Vistas::LIM.ConsultarLimites"; 

SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::LIM.ConsultarLimitesEmisor";  

SET SCHEMA "BSCL";
DELETE FROM "BSCL.Plataforma.BD::LIM.TipoLimite";

INSERT INTO "BSCL.Plataforma.BD::LIM.TipoLimite"("IdTipoLimite","IdEmpresa","Nombre","EsCuentaPropia","IdComportamiento","IdGrupoLimite","Categoria","IdEstado","IdTipoEvento","Prioridad")
VALUES('LIMPORTPRODUCTO',1,'LÍMITE PORTAFOLIO PRODUCTO','N','AUT','PORTAFOLIO','OPERACION','ACT','LIPPRO',1);
INSERT INTO "BSCL.Plataforma.BD::LIM.TipoLimite"("IdTipoLimite","IdEmpresa","Nombre","EsCuentaPropia","IdComportamiento","IdGrupoLimite","Categoria","IdEstado","IdTipoEvento","Prioridad")
VALUES('LIMPORTSOBREGIRO',1,'LÍMITE PORTAFOLIO SOBREGIRO','N','AUT','PORTAFOLIO','OPERACION','ACT','LIPSGR',2);

 
 SET SCHEMA "BSCL";
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" ("IdTipoEvento","IdEmpresa", "IdModulo","Codigo","Nombre","HorasVigencia","EtapasSecuenciales","IdTipoProducto","IdTipoOperacion","IdUsuarioSuperAutorizador","IdRolSuperAutorizador","IdEstado")
values('LIPPRO',1,'PLA','AUT_LIM_LIPPRO','Evaluación Limite Producto Portafolio',1,'S','' ,'' ,0,0,'VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('LIPPRO','LI','PLA','EVALUACION LIMITE PRODUCTO PORTAFOLIO','VIG'); 
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion")
values('LIPPRO','S');

insert into "BSCL.Plataforma.BD::AUT.TipoEvento" ("IdTipoEvento","IdEmpresa", "IdModulo","Codigo","Nombre","HorasVigencia","EtapasSecuenciales","IdTipoProducto","IdTipoOperacion","IdUsuarioSuperAutorizador","IdRolSuperAutorizador","IdEstado")
values('LIPSGR',1,'PLA','AUT_LIM_LIPSGR','Evaluación Limite Sobregiro',1,'S','' ,'' ,0,0,'VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('LIPSGR','LI','PLA','EVALUACION LIMITE SOBREGIRO','VIG'); 
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion")
values('LIPSGR','S');


insert into "BSCL.Plataforma.BD::AUT.TipoEvento" ("IdTipoEvento","IdEmpresa", "IdModulo","Codigo","Nombre","HorasVigencia","EtapasSecuenciales","IdTipoProducto","IdTipoOperacion","IdUsuarioSuperAutorizador","IdRolSuperAutorizador","IdEstado")
values('NLICTP',1,'PLA','AUT_NLICTP_NUE','Nuevo Limite Cuenta Propia',1,'S','' ,'' ,0,0,'VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('NLICTP','LI','PLA','NUEVO LIMITE CUENTA PROPIA','VIG'); 
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion")
values('NLICTP','N');


INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(316,'BSCL.Plataforma.BD.Servicios::LIM.CrearLimiteCtaPropia',0,'P','');



--################################################
--  MMACHMAR. Valores Defecto Sistema
--################################################

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ValorDefectoSistema" ("IdValorDefectoSistema","Nombre","Valor","IdEmpresa","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEstado") 
VALUES('IdMonedaLimiteLineaSobregiro','Moneda Límite Línea Sobregiro','CLP',1,3,NOW(),NOW(),'ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ValorDefectoSistema" ("IdValorDefectoSistema","Nombre","Valor","IdEmpresa","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEstado") 
VALUES('IdEstadoLimiteLineaSobregiro','Estado Límite Línea Sobregiro','ACT',1,3,NOW(),NOW(),'ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ValorDefectoSistema" ("IdValorDefectoSistema","Nombre","Valor","IdEmpresa","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEstado") 
VALUES('IdMonedaLimiteLineaSobregiro','Moneda Límite Línea Sobregiro','CLP',1,3,NOW(),NOW(),'ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ValorDefectoSistema" ("IdValorDefectoSistema","Nombre","Valor","IdEmpresa","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEstado") 
VALUES('IdEstadoLimiteLineaSobregiro','Estado Límite Línea Sobregiro','ACT',1,3,NOW(),NOW(),'ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ValorDefectoSistema" ("IdValorDefectoSistema","Nombre","Valor","IdEmpresa","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEstado") 
VALUES('IdEstadoLimiteCuentaPropiaRVPlazo','Estado Límite Cuenta Propia RV Plazo','ACT',1,3,NOW(),NOW(),'ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ValorDefectoSistema" ("IdValorDefectoSistema","Nombre","Valor","IdEmpresa","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEstado") 
VALUES('IdEstadoLimiteCuentaPropiaRVEmisor','Estado Límite Cuenta Propia RV Emisor','ACT',1,3,NOW(),NOW(),'ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ValorDefectoSistema" ("IdValorDefectoSistema","Nombre","Valor","IdEmpresa","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEstado") 
VALUES('IdEstadoLimiteCuentaPropiaRVClasifRiesgo','Estado Límite Cuenta Propia RV Emisor','ACT',1,3,NOW(),NOW(),'ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ValorDefectoSistema" ("IdValorDefectoSistema","Nombre","Valor","IdEmpresa","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEstado") 
VALUES('IdEstadoLimiteCuentaPropiaRVMargenTasas','Estado Límite Cuenta Propia RV Margen Tasas','ACT',1,3,NOW(),NOW(),'ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ValorDefectoSistema" ("IdValorDefectoSistema","Nombre","Valor","IdEmpresa","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEstado") 
VALUES('IdMonedaLimiteCuentaPropiaRVMargenTasas','Moneda Límite Cuenta Propia RV Margen Tasas','CLP',1,3,NOW(),NOW(),'ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ValorDefectoSistema" ("IdValorDefectoSistema","Nombre","Valor","IdEmpresa","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEstado") 
VALUES('IdTipoComportamientoLimCtaProRVPlazo','Tipo Comportamiento Límite Cuenta Propia RV Plazo','AUTORIZACION',1,3,NOW(),NOW(),'ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ValorDefectoSistema" ("IdValorDefectoSistema","Nombre","Valor","IdEmpresa","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEstado") 
VALUES('IdTipoComportamientoLimCtaProRVEmisor','Tipo Comportamiento Límite Cuenta Propia RV Emisor','AUTORIZACION',1,3,NOW(),NOW(),'ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ValorDefectoSistema" ("IdValorDefectoSistema","Nombre","Valor","IdEmpresa","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEstado") 
VALUES('IdTipoComportamientoLimCtaProRVClasRies','Tipo Comportamiento Límite Cuenta Propia RV Clasificacion Riesgo','AUTORIZACION',1,3,NOW(),NOW(),'ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ValorDefectoSistema" ("IdValorDefectoSistema","Nombre","Valor","IdEmpresa","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEstado") 
VALUES('IdTipoComportamientoLimCtaProRVMargTasas','Tipo Comportamiento Límite Cuenta Propia RV Margen Tasas','AUTORIZACION',1,3,NOW(),NOW(),'ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ValorDefectoSistema" ("IdValorDefectoSistema","Nombre","Valor","IdEmpresa","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEstado") 
VALUES('IdTipoComportamientoLimCtaProCartera','Tipo Comportamiento Límite Cuenta Propia Cartera','AUTORIZACION',1,3,NOW(),NOW(),'ACT');

update "BSCL"."BSCL.Plataforma.BD::PLA.ValorDefectoSistema";
set "IdUsuarioCreador" = 3;

UPDATE "BSCL"."BSCL.Plataforma.BD::PLA.ValorDefectoSistema"
SET "Nombre" = 'Estado Límite Cuenta Propia RV Clasificacion Riesgo'
WHERE "IdValorDefectoSistema" = 'IdEstadoLimiteCuentaPropiaRVClasifRiesgo';



--##################################################
--  VARIACODE - Agregar Detalle de tipos de eventos
--##################################################
DROP TABLE "BSCL.Plataforma.BD::EVT.FormaDespacho";
DROP TABLE "BSCL.Plataforma.BD::EVT.TiempoDespacho";


create column table "BSCL.Plataforma.BD::EVT.FormaDespacho" (
   "IdFormaDespacho"            BIGINT        not null,  --ID Unico
   "Nombre"      				NVARCHAR(40)  not null,  --FK  ConfiguracionEvento
   "NombreAbreviado"            NVARCHAR(20)  not null,  --ACT/INA
   primary key ("IdFormaDespacho")

);

create column table "BSCL.Plataforma.BD::EVT.TiempoDespacho" (
   "IdTiempoDespacho"                       BIGINT              not null,  --ID Unico
   "Nombre"                                 NVARCHAR(40)              not null,  --FK  ConfiguracionEvento
   "CodigoFormaDespacho"                    NVARCHAR(20)         not null,  --ACT/INA
  primary key ("IdTiempoDespacho")

);

create column table BSCL."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" (
    "IdTipoEventoDetalle"   BIGINT        	  not null, 
    "NombreCampo"     		NVARCHAR(40)      not null,   
    "CodigoTipoEvento"       NVARCHAR(40)      not null,
    primary key ("IdTipoEventoDetalle")
);

alter table "BSCL.Plataforma.BD::EVT.TipoEventoDetalle"
add constraint "FK_TipoEventoDetalle_TipoEvento" foreign key ("CodigoTipoEvento")
references "BSCL.Plataforma.BD::EVT.TipoEvento" ("CodigoTipoEvento") on delete restrict on update restrict;

--##################################################
--  VARIACODE - FIN
--##################################################

--##################################################
-- JG.Regularización piezas que ya no existen
--##################################################
DELETE FROM "BSCL.Plataforma.BD::PLA.Servicio" WHERE "RutaServicio" in (
'BSCL.Plataforma.BD.Servicios::COM.CrearModeloFacturacion',
'BSCL.Plataforma.BD.Servicios::COM.CrearPlantillaCondicionesOperativas',
'BSCL.Plataforma.BD.Servicios::COM.ModificarModeloFacturacion',
'BSCL.Portafolio.BD.Servicios::ModificarUsoPortafolio',
'BSCL.Plataforma.BD.Servicios::ConsultarAtributoMantenedorDato',
'BSCL.Plataforma.BD.Servicios::ConsultarAtributoMantenedorDatoPrueba');

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::ConsultarAtributoMantenedorDatoPrueba";
DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::ConsultarAtributoMantenedorDato";

---------------------PEWPEWPEW----------------------------

delete from  bscl."BSCL.Plataforma.BD::PLA.Servicio" where "IdServicio" = 264;

----------------------------------------------------------
-------------------FIX Ingresar cliente----------------------------
INSERT INTO "BSCL"."BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(274,'Buscar Documento Identidad', 'BTN', 39, 3, 53, 'formCrearCrearCliente_documento_identidad', 'S');
----------------------------------------------------------
