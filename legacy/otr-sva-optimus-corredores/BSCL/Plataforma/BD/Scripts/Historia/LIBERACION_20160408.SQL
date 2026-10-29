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

--**** NO BORRAR - AUMENTAR EN CADA LIBERACION ***
UPDATE "BSCL.Plataforma.BD::PLA.Configuracion" SET "Valor" ='20160406' WHERE "CodigoParametro" =  'Version';
commit;

--
--
-- LIBERACION SEMANA: 07-04-2016 (Se adelanta un día)
--

--################################################
--    KH - Modelo Instrumento
--################################################
ALTER TABLE "BSCL"."BSCL.Plataforma.BD::INS.ConfiguracionUsoPrecio" DROP ("CodigoUsoPrecio");
ALTER TABLE "BSCL"."BSCL.Plataforma.BD::INS.ConfiguracionUsoPrecio" ADD  ("CodigoUsoPrecio" NVARCHAR(40) not null default '');

ALTER TABLE "BSCL"."BSCL.Plataforma.BD::INS.BolsaInstrumentoPrecios" ADD("PrecioMedio" DECIMAL(26,8)  default 0 not null);
ALTER TABLE "BSCL"."BSCL.Plataforma.BD::INS.BolsaInstrumentoPrecios" ADD("PrecioMercadoAnterior" DECIMAL(26,8)  default 0 not null);


---se cae
--- CARGAR VALORES POR DEFECTO
delete from "BSCL"."BSCL.Plataforma.BD::INS.ConfiguracionUsoPrecio";
--("IdConfiguracionUsoPrecio","CodigoUsoPrecio","IdModulo","IdTipoOperacion","IdConcepto","Descripcion","TipoPrecioDefault","FuentePrecioDefault","IdClasifiacionTipoInstrumento")

insert into "BSCL"."BSCL.Plataforma.BD::INS.ConfiguracionUsoPrecio" ("IdConfiguracionUsoPrecio","CodigoUsoPrecio","IdModulo","IdTipoOperacion","IdConcepto","Descripcion","TipoPrecioDefault","FuentePrecioDefault","IdClasificacionTipoInstrumento") values(1,'ORDENFUTURO','ORD','','','Ingreso Orden Futuro','CIERRE','BCS','DV');
insert into "BSCL"."BSCL.Plataforma.BD::INS.ConfiguracionUsoPrecio" ("IdConfiguracionUsoPrecio","CodigoUsoPrecio","IdModulo","IdTipoOperacion","IdConcepto","Descripcion","TipoPrecioDefault","FuentePrecioDefault","IdClasificacionTipoInstrumento") values(2,'ORDENACCIONES','ORD','','','Ingreso Orden Acciones','CIERRE','BCS','RV');
insert into "BSCL"."BSCL.Plataforma.BD::INS.ConfiguracionUsoPrecio" ("IdConfiguracionUsoPrecio","CodigoUsoPrecio","IdModulo","IdTipoOperacion","IdConcepto","Descripcion","TipoPrecioDefault","FuentePrecioDefault","IdClasificacionTipoInstrumento") values(3,'ORDENSIMULTANEA','ORD','','','Ingreso Orden Simultanea','CIERRE','BCS','RV');
insert into "BSCL"."BSCL.Plataforma.BD::INS.ConfiguracionUsoPrecio" ("IdConfiguracionUsoPrecio","CodigoUsoPrecio","IdModulo","IdTipoOperacion","IdConcepto","Descripcion","TipoPrecioDefault","FuentePrecioDefault","IdClasificacionTipoInstrumento") values(4,'VALCARTERARV','CUS','','','Valorizar Cartera RV','PROMEDIO','BCS','RV');
insert into "BSCL"."BSCL.Plataforma.BD::INS.ConfiguracionUsoPrecio" ("IdConfiguracionUsoPrecio","CodigoUsoPrecio","IdModulo","IdTipoOperacion","IdConcepto","Descripcion","TipoPrecioDefault","FuentePrecioDefault","IdClasificacionTipoInstrumento") values(5,'VALCARTERARF','CUS','','','Valorizar Cartera RF','PROMEDIO','BCS','RF');
insert into "BSCL"."BSCL.Plataforma.BD::INS.ConfiguracionUsoPrecio" ("IdConfiguracionUsoPrecio","CodigoUsoPrecio","IdModulo","IdTipoOperacion","IdConcepto","Descripcion","TipoPrecioDefault","FuentePrecioDefault","IdClasificacionTipoInstrumento") values(6,'VALGARANTIA','CUS','','','Valorizar Garantía','VECTOR','BCS','RV');
insert into "BSCL"."BSCL.Plataforma.BD::INS.ConfiguracionUsoPrecio" ("IdConfiguracionUsoPrecio","CodigoUsoPrecio","IdModulo","IdTipoOperacion","IdConcepto","Descripcion","TipoPrecioDefault","FuentePrecioDefault","IdClasificacionTipoInstrumento") values(7,'INGRESOGTIA','CUS','','','Ingreso Garantia','VECTOR','BCS','RV');
insert into "BSCL"."BSCL.Plataforma.BD::INS.ConfiguracionUsoPrecio" ("IdConfiguracionUsoPrecio","CodigoUsoPrecio","IdModulo","IdTipoOperacion","IdConcepto","Descripcion","TipoPrecioDefault","FuentePrecioDefault","IdClasificacionTipoInstrumento") values(8,'INGRESOTITULORV','CUS','','','Ingreso Titulo RV','CIERRE','BCS','RV');
insert into "BSCL"."BSCL.Plataforma.BD::INS.ConfiguracionUsoPrecio" ("IdConfiguracionUsoPrecio","CodigoUsoPrecio","IdModulo","IdTipoOperacion","IdConcepto","Descripcion","TipoPrecioDefault","FuentePrecioDefault","IdClasificacionTipoInstrumento") values(9,'INGRESOTITULORF','CUS','','','Ingreso Titulo RF','CIERRE','BCS','RF');
insert into "BSCL"."BSCL.Plataforma.BD::INS.ConfiguracionUsoPrecio" ("IdConfiguracionUsoPrecio","CodigoUsoPrecio","IdModulo","IdTipoOperacion","IdConcepto","Descripcion","TipoPrecioDefault","FuentePrecioDefault","IdClasificacionTipoInstrumento") values(10,'CONSULTACARTERARV','CUS','','','Consulta Cartera RV','CIERRE','BCS','RV');
insert into "BSCL"."BSCL.Plataforma.BD::INS.ConfiguracionUsoPrecio" ("IdConfiguracionUsoPrecio","CodigoUsoPrecio","IdModulo","IdTipoOperacion","IdConcepto","Descripcion","TipoPrecioDefault","FuentePrecioDefault","IdClasificacionTipoInstrumento") values(11,'CONSULTACARTERARF','CUS','','','Consulta Cartera RF','CIERRE','BCS','RF');
---se cae


--################################################
--    CSI - Contraparte CrediCorp
--################################################




--################################################
--    JG - Cambios Contraparte
--################################################

RENAME COLUMN "BSCL.Plataforma.BD::INS.CorredoraBolsa"."Codigo" 	TO "CodigoCorredora";
RENAME COLUMN "BSCL.Plataforma.BD::INS.CorredoraBolsa"."CodigoSVS" 	TO "CodigoInscripcion";


--UPDATE "BSCL"."BSCL.SocioNegocio.BD::CNT.Contraparte" SET "CodigoInscripcion"='97' where  "IdContraparte" = 10009;

INSERT INTO "BSCL.Plataforma.BD::PLA.Configuracion" ( "IdEmpresa","CodigoParametro","Descripcion","Valor" )  
VALUES(1,'SegundosZonaHoraria','Segundos Zona horaria del sistema respecto UTC','-10800');

--################################################
--       MMACHMAR - Portafolio - Preferencias Facturación
--################################################

RENAME COLUMN "BSCL"."BSCL.Plataforma.BD::COM.ModeloFacturacion"."SeparaFacturaPorTransaccion" TO "SeparaLineaFacturaPorTransaccion";
RENAME COLUMN "BSCL"."BSCL.Plataforma.BD::COM.ModeloFacturacion"."FusionaFacturaPorPrecio" TO "FusionaLineaFacturaPorPrecio";

ALTER TABLE "BSCL.Plataforma.BD::COM.ModeloFacturacion" 
ADD("OperaContraCuentaInversion" NVARCHAR(1) NOT NULL DEFAULT 'N');

ALTER TABLE "BSCL.Plataforma.BD::COM.ModeloFacturacion" 
ADD("NeteaIngresosEgresosTesoreria" NVARCHAR(1) NOT NULL DEFAULT 'N');

--################################################
--       cflores - Modificacion de INDICADORES
--################################################
SET SCHEMA "BSCL";	

insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('MODIND',1,'PLA','AUT_IND_MOD','Modificación de Indicador',1,'S','' ,'' ,0,0,'VIG');	

INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('MODIND','MC','PLA','MODIFICACION DE INDICADOR','VIG'); 

insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion")
values('MODIND','N');


---SE CAE

--################################################
--       nrojas - NUEVOS CONCEPTOS
--################################################
DELETE FROM "BSCL.Plataforma.BD::PLA.Concepto" WHERE "IdConcepto" in (
 'INCORV'
,'EGVERV'
,'ANTSIN'
,'ANTSEG'
,'ANTSIT'
,'ANTSET'
,'VCTSIN'
,'VCTSEG'
,'VCTSET'
,'VCTSIT'
);

INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )
VALUES('FACMSM','','FAC','FACTURA COMPRA DE SIMULTANEA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )
VALUES('FAVTSM','','FAC','FACTURA VENTA DE SIMULTANEA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )
VALUES('FACMAC','','FAC','FACTURA COMPRA DE ACCIONES NACIONALES','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )
VALUES('FAPRES','','FAC','FACTURA DE PRESTAMO','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )
VALUES('FAEMPR','','FAC','FACTURA DE EMPRESTITO','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )
VALUES('FAVNAC','','FAC','FACTURA VENTA DE ACCIONES NACIONALES','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )
VALUES('FECMSM','','FAC','FACTURA ELECTRONICA COMPRA DE SIMULTANEA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )
VALUES('FEVTSM','','FAC','FACTURA ELECTRONICA VENTA DE SIMULTANEA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )
VALUES('FECMAC','','FAC','FACTURA ELECTRONICA COMPRA DE ACCIONES NACIONALES','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )
VALUES('FEVNAC','','FAC','FACTURA ELECTRONICA VENTA DE ACCIONES NACIONALES','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('VCTSIC','IT','CUS','VENCIMIENTO SIMULTANEA COMPRA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('VCTSIV','ET','CUS','VENCIMIENTO SIMULTANEA VENTA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('ANTSIC','IT','CUS','ANTICIPO SIMULTANEA COMPRA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('ANTSIV','ET','CUS','ANTICIPO SIMULTANEA VENTA','VIG');
UPDATE "BSCL.Custodia.BD::CUS.ConceptoCustodia" SET "IdConcepto" = 'ANTSIV' WHERE "IdConcepto" = 'ANTSET';
UPDATE "BSCL.Custodia.BD::CUS.ConceptoCustodia" SET "IdConcepto" = 'ANTSIC' WHERE "IdConcepto" = 'ANTSIT';
UPDATE "BSCL.Custodia.BD::CUS.ConceptoCustodia" SET "IdConcepto" = 'VCTSIC' WHERE "IdConcepto" = 'VCTSET';
UPDATE "BSCL.Custodia.BD::CUS.ConceptoCustodia" SET "IdConcepto" = 'VCTSIV' WHERE "IdConcepto" = 'VCTSIT';

--SE CAE



DELETE FROM "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio" IN (208,229,237,238);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
	VALUES (208, 'BSCL.Custodia.BD.Servicios::EVC.CrearEventoCapitalCliente' , 1,  'P', 'Crea un Evento Capital Cliente');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
	VALUES (229, 'BSCL.Custodia.BD.Servicios::EVC.ModificarEventoCapitalCliente' , 1,  'P', 'Modifica un Evento Capital Cliente');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
	VALUES (237, 'BSCL.Custodia.BD.Servicios::EVC.EliminarEventoCapitalCliente' , 1,  'P', 'Elimina un Evento Capital Cliente');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
	VALUES (238, 'BSCL.Custodia.BD.Servicios::EVC.ConsultaEventoCapital' , 1,  'P', 'Consulta un Evento Capital');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(265,'BSCL.Custodia.BD.Servicios::GTA.ConsultarCustodiaParaGarantia2',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(267,'BSCL.Custodia.BD.Servicios::GTA.IngresarGarantiasUI',0,'P','');

update "BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" set "TipoCambioMercado" = 1,"ParidadMercado" = 1
where "IdMoneda" ='CLP';

SET SCHEMA "BSCL";


/*==============================================================*/
/* Drop Tables:                                                 */
/*==============================================================*/

DROP TABLE "BSCL.Plataforma.BD::IND.Indicador" 					CASCADE;
DROP TABLE "BSCL.Plataforma.BD::IND.TipoIndicador" 				CASCADE;
DROP TABLE "BSCL.Plataforma.BD::IND.DetalleIndicador" 			CASCADE;
DROP TABLE "BSCL.Plataforma.BD::IND.EstadoTipoIndicador"		CASCADE;
DROP TABLE "BSCL.Plataforma.BD::IND.TipoDatoTipoIndicador"		CASCADE;

DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias::TipoIndicador"	CASCADE;
DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias::Indicador"		CASCADE;
DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias::DetalleIndicador"	CASCADE;

 */
/*==============================================================*/
/* Create Table:                                                */
/*==============================================================*/

create column table "BSCL.Plataforma.BD::IND.Indicador" (
   "IdIndicador"        BIGINT		         not null,
   "IdEntidad"          BIGINT               not null,
   "IdTipoEntidad"      NVARCHAR(40)         not null,
   "IdTipoIndicador"  	BIGINT  			 DEFAULT 0  NOT NULL,
   "ModeloCliente"		NVARCHAR(40)		 not null, -- <TipoCliente>
   "IdEmpresa"			BIGINT				 not null,
   "ValorIndicador"     NVARCHAR(255)        not null,
   "FechaIngreso"       SECONDDATE           default '0001-01-01 00:00:00' not null,
   "FechaCreacion"      SECONDDATE           default '0001-01-01 00:00:00' not null,
   "FechaModificacion"  SECONDDATE           default '0001-01-01 00:00:00' not null,
   "IdEstadoAutorizacion" NVARCHAR(3)          not null,
   primary key  ("IdEntidad", "IdTipoEntidad", "IdIndicador", "IdEmpresa", "ModeloCliente")
);

create column table "BSCL.Plataforma.BD::IND.TipoIndicador" (
   "IdTipoIndicador"        BIGINT      	NOT NULL,
   "IdTipoEntidad"      	NVARCHAR(40) 	NOT NULL,
   "IdEmpresa"				BIGINT			NOT NULL,
   "ValorIndicadorDefecto"  NVARCHAR(255)  	NOT NULL, -- <TipoCliente>
   "ClaseDato"         		NVARCHAR(40)  	NOT NULL,
   "NombreTipoIndicador"    NVARCHAR(255)  	NOT NULL,   
   "GrupoIndicador"       	NVARCHAR(40)    		,
   "CantidadDecimales"		BIGINT			NOT NULL,
   "LongitudMinima"			BIGINT			NOT NULL,
   "LongitudMaxima"			BIGINT			NOT NULL,
   "IdUso"					NVARCHAR(3) 	DEFAULT 'IND' 	NOT NULL, -- IND: Indicador, REQ: Requisito
   "IdEstado" 				NVARCHAR(3) 	DEFAULT 'ACT' 	NOT NULL,
   "RequiereAutorizacion" 	NVARCHAR(1)		DEFAULT 'N' 	NOT NULL,
   primary key  ("IdTipoIndicador", "IdTipoEntidad", "IdEmpresa")
);

CREATE COLUMN TABLE "BSCL.Plataforma.BD::IND.DetalleIndicador" (
   "IdDetalleIndicador"         BIGINT      	NOT NULL,
   "IdTipoIndicador"      	    BIGINT		  	NOT NULL,
   "DetalleIndicador"           NVARCHAR(255)  	NOT NULL,
   primary key  ("IdDetalleIndicador", "IdTipoIndicador")
);

CREATE COLUMN TABLE "BSCL.Plataforma.BD::IND.EstadoTipoIndicador" (
 	"IdEstadoTipoIndicador" 	BIGINT CS_FIXED NOT NULL ,
 	"IdEstado" 					NVARCHAR(3) 	DEFAULT '' NOT NULL ,
	"NombreEntidad" 			NVARCHAR(100) 	DEFAULT '' NOT NULL , 
	PRIMARY KEY ("IdEstadoTipoIndicador")
);
 
CREATE COLUMN TABLE "BSCL.Plataforma.BD::IND.TipoDatoTipoIndicador" (
 	"IdTipoDatoTipoIndicador"	BIGINT CS_FIXED NOT NULL ,
 	"TipoDato" 					NVARCHAR(20) 	NOT NULL,
 	"NombreTipoDato" 			NVARCHAR(20) 	NOT NULL,
PRIMARY KEY ("IdTipoDatoTipoIndicador")) ;

/*==============================================================*/
/* SECUENCIAS:                                                  */
/*==============================================================*/
CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::TipoIndicador"
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
       
CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::Indicador"
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
              
CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::DetalleIndicador"
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;

/*==============================================================*/
/* Tipos:                                                       */
/*==============================================================*/


/*==============================================================*/
/* constraint:                                                  */
/*==============================================================*/


/*==============================================================*/
/* Index:                                                       */
/*==============================================================*/


/* Table: */

--create index "INX01_Evento" on "BSCL.Plataforma.BD::AUT.Evento"("IdTipoOperacion","IdConcepto","NumeroOperacionConcepto","FechaOperacionConcepto","IdEstado");



INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(269,'BSCL.Custodia.BD.Servicios::GTA.RetirarGarantiasUI',0,'P','');

--################################################
--    CA - DOCUMENTOS
--################################################

drop table "BSCL.Plataforma.BD::DOC.TipoDocumento" cascade;
drop table "BSCL.Plataforma.BD::DOC.Anexo" cascade;
drop table "BSCL.Plataforma.BD::DOC.Documento" cascade;

drop sequence "BSCL.Plataforma.BD.Secuencias::Documento";
drop sequence "BSCL.Plataforma.BD.Secuencias::Anexo";

/*==============================================================*/
/* Create Table:                                                */
/*==============================================================*/
create column table "BSCL.Plataforma.BD::DOC.TipoDocumento" (
   "IdTipoDocumento"  		NVARCHAR(40)               	not null,
   "IdEmpresa"    			BIGINT                		not null,
   "NombreDocumento"  		NVARCHAR(100)               not null,
   "NumeroFirmantesEmpresa" INT             default '1' not null,
   "NumeroFirmantesCliente" INT             default '1' not null,
   "Clasificacion" 			NVARCHAR(100)    			not null,
   "ValidaFechaVigencia"    NVARCHAR(1)                 		,
   primary key ("IdTipoDocumento","IdEmpresa")
);

create column table "BSCL.Plataforma.BD::DOC.Anexo" (
   	"IdAnexo"  					BIGINT  							not null,
   	"IdDocumento"            	BIGINT  							not null,
   	"IdEstado"					NVARCHAR(3)							NOT NULL,
   	"NumeroFirmantesEmpresa"    INT  			DEFAULT '0' 		not null,
   	"NumeroFirmantesCliente"    INT				DEFAULT '0' 		not null,
   	"NombreFirmanteEmpresa1"    NVARCHAR(100)      			,
    "NombreFirmanteEmpresa2"    NVARCHAR(100)       		,
    "NombreFirmanteEmpresa3"    NVARCHAR(100)       		,
    "NombreFirmanteCliente1"    NVARCHAR(100)       		,
    "NombreFirmanteCliente2"    NVARCHAR(100)         		,
    "NombreFirmanteCliente3"    NVARCHAR(100)         		,
   	"FirmaEmpresa1" 			NVARCHAR(1)     DEFAULT 'N'			not null,
   	"FirmaEmpresa2"     		NVARCHAR(1)     DEFAULT 'N'			not null,
   	"FirmaEmpresa3"     		NVARCHAR(1)     DEFAULT 'N'			not null,
   	"FirmaCliente1"           	NVARCHAR(1)     DEFAULT 'N'			not null,
   	"FirmaCliente2"           	NVARCHAR(1)     DEFAULT 'N'			not null,
   	"FirmaCliente3"          	NVARCHAR(1)		DEFAULT 'N'			not null,
   	"IdUsuarioModifica"      	BIGINT         								,
	"FechaIngreso"           	SECONDDATE          				not null,
	"FechaModificacion"         SECONDDATE   default '0001-01-01'	not null,
	"FechaVigencia"           	SECONDDATE          		,
	"DocumentoFisicoReferencia"	NVARCHAR(100)				,		
   primary key ("IdAnexo")
);

create column table "BSCL.Plataforma.BD::DOC.Documento" (
   "IdDocumento"  		BIGINT               not null,
   "IdTipoDocumento"    NVARCHAR(40)         not null,
   "IdEmpresa"          BIGINT               default '0' not null,
   "IdCliente"          BIGINT               default '0' not null,
   "IdUltimoAnexo" 		BIGINT               default '0' not null,
   primary key ("IdDocumento")
);



/*==============================================================*/
/* constraint:                                                  */
/*==============================================================*/

/* Table: */

alter table "BSCL.Plataforma.BD::DOC.Documento"
   add constraint "FK_TipoDocumento" foreign key ("IdTipoDocumento", "IdEmpresa")
      references "BSCL.Plataforma.BD::DOC.TipoDocumento" ("IdTipoDocumento", "IdEmpresa") on delete restrict on update restrict;

/* Table: */

alter table "BSCL.Plataforma.BD::DOC.Documento"
   add constraint "FK_Cliente" foreign key ("IdCliente")
      references "BSCL"."BSCL.SocioNegocio.BD::CLI.Cliente" ("IdCliente") on delete restrict on update restrict;

/*==============================================================*/
/* Index:                                                       */
/*==============================================================*/

CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::Documento"
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
       
CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::Anexo"
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;


commit;

--################################################
--    JG - REQUISITOS OPERATIVOS
--################################################

SET SCHEMA "BSCL";
	
/*==============================================================*/
/* Drop Tables:                                                 */
/*==============================================================*/

DROP TABLE "BSCL.Plataforma.BD::REQ.CondicionOperativa" 		CASCADE;
DROP TABLE "BSCL.Plataforma.BD::REQ.EstadoCondicionOperativa"	CASCADE;
DROP TABLE "BSCL.Plataforma.BD::REQ.TipoEtapa" 					CASCADE;
DROP TABLE "BSCL.Plataforma.BD::REQ.TipoComportamiento" 		CASCADE;
DROP TABLE "BSCL.Plataforma.BD::REQ.TipoOperacionUsuario" 		CASCADE;
DROP TABLE "BSCL.Plataforma.BD::REQ.RequisitoOperativo" 		CASCADE;
DROP TABLE "BSCL.Plataforma.BD::REQ.ModeloRequisitoOperativo" 	CASCADE;

DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias::IdRequisitoOperativo";

/*==============================================================*/
/* Create Table:                                                */
/*==============================================================*/


/*****************************
PLANTILLA CONDICIONES OPERATIVAS 
*****************************/

create column table "BSCL.Plataforma.BD::REQ.CondicionOperativa" (
	"IdCondicionOperativa"			NVARCHAR(40)	NOT NULL,
	"Nombre"						NVARCHAR(100)	NOT NULL DEFAULT '',
	"RequiereAtributo1"				NVARCHAR(1)		NOT NULL DEFAULT 'N',
	"RequiereAtributo2"				NVARCHAR(1)		NOT NULL DEFAULT 'N',	
	"IdTipoComportamientoDefecto"	NVARCHAR(40)	NOT NULL DEFAULT '', -- ADVERTENCIA, RECHAZO, AUTORIZACION
	"IdUsuarioCreador"				BIGINT			NOT NULL DEFAULT 0,
	"FechaCreacion" 				SECONDDATE  	NOT NULL DEFAULT '0001-01-01 00:00:00',
	"FechaModificacion" 			SECONDDATE  	NOT NULL DEFAULT '0001-01-01 00:00:00',  
	"IdEmpresa"         			BIGINT          NOT NULL DEFAULT 0,  
	"IdEstado" 						NVARCHAR(3) 	DEFAULT 'ACT' NOT NULL,
	primary key ("IdCondicionOperativa")
);

CREATE COLUMN TABLE  "BSCL.Plataforma.BD::REQ.EstadoCondicionOperativa" (
 	"IdEstadoCondicionOperativa" 	BIGINT  	 	NOT NULL,
 	"IdEstado" 						NVARCHAR(3) 	NOT NULL,
 PRIMARY KEY ("IdEstadoCondicionOperativa")
);
 

create column table "BSCL.Plataforma.BD::REQ.TipoEtapa" (
	"IdTipoEtapa"					NVARCHAR(40)	NOT NULL,
	"Nombre"						NVARCHAR(100)	NOT NULL DEFAULT '',
	"IdUsuarioCreador"				BIGINT			NOT NULL DEFAULT 0,
	"FechaCreacion" 				SECONDDATE  	NOT NULL DEFAULT '0001-01-01 00:00:00',
	"FechaModificacion" 			SECONDDATE  	NOT NULL DEFAULT '0001-01-01 00:00:00',  
	"IdEmpresa"         			BIGINT          NOT NULL DEFAULT 0,  
	"IdEstado" 						NVARCHAR(3) 	DEFAULT 'ACT' NOT NULL,
	primary key ("IdTipoEtapa")
);

create column table "BSCL.Plataforma.BD::REQ.TipoComportamiento" (
	"IdTipoComportamiento"			NVARCHAR(40)	NOT NULL, -- ADVERTENCIA, RECHAZO, AUTORIZACION
	"Nombre"						NVARCHAR(100)	NOT NULL DEFAULT '',
	"IdUsuarioCreador"				BIGINT			NOT NULL DEFAULT 0,
	"FechaCreacion" 				SECONDDATE  	NOT NULL DEFAULT '0001-01-01 00:00:00',
	"FechaModificacion" 			SECONDDATE  	NOT NULL DEFAULT '0001-01-01 00:00:00',  
	"IdEmpresa"         			BIGINT          NOT NULL DEFAULT 0,  
	"IdEstado" 						NVARCHAR(3) 	DEFAULT 'ACT' NOT NULL,
	primary key ("IdTipoComportamiento")
);

create column table "BSCL.Plataforma.BD::REQ.TipoOperacionUsuario" (
	"IdTipoOperacionUsuario"			NVARCHAR(40)	NOT NULL,
	"Nombre"						NVARCHAR(100)	NOT NULL DEFAULT '',
	"IdUsuarioCreador"				BIGINT			NOT NULL DEFAULT 0,
	"FechaCreacion" 				SECONDDATE  	NOT NULL DEFAULT '0001-01-01 00:00:00',
	"FechaModificacion" 			SECONDDATE  	NOT NULL DEFAULT '0001-01-01 00:00:00',  
	"IdEmpresa"         			BIGINT          NOT NULL DEFAULT 0,  
	"IdEstado" 						NVARCHAR(3) 	DEFAULT 'ACT' NOT NULL,
	primary key ("IdTipoOperacionUsuario")
);

create column table "BSCL.Plataforma.BD::REQ.RequisitoOperativo" (
	"IdRequisitoOperativo"			BIGINT			NOT NULL,
	"IdModeloRequisitoOperativo"	NVARCHAR(40)	NOT NULL DEFAULT '', -- POR EL MOMENTO SOLO UNO, EL BASE DE LA CORREDORA.
	"IdCondicionOperativa"			NVARCHAR(40)	NOT NULL DEFAULT '',
	"IdTipoEtapa"					NVARCHAR(40)	NOT NULL DEFAULT '',
	"IdTipoComportamiento"			NVARCHAR(40)	NOT NULL DEFAULT '',
	"IdTipoOperacionUsuario"		NVARCHAR(40)	NOT NULL DEFAULT '',
	"IdTipoCanalIngreso"			VARCHAR(3)      NOT NULL DEFAULT '0', -- 0 todos.
	"IdMoneda"						NVARCHAR(3)    	NOT NULL DEFAULT '0', -- 0 todos.
	"ValorAtributo1"				NVARCHAR(255)	NOT NULL DEFAULT '',
	"ValorAtributo2"				NVARCHAR(255)	NOT NULL DEFAULT '',	
	"IdUsuarioCreador"				BIGINT			NOT NULL DEFAULT 0,
	"FechaCreacion" 				SECONDDATE  	NOT NULL DEFAULT '0001-01-01 00:00:00',
	"FechaModificacion" 			SECONDDATE  	NOT NULL DEFAULT '0001-01-01 00:00:00',  
	"IdEmpresa"         			BIGINT          NOT NULL DEFAULT 0,  
	"IdEstado" 						NVARCHAR(3) 	DEFAULT 'ACT' NOT NULL,
	primary key ("IdRequisitoOperativo")
);

create column table "BSCL.Plataforma.BD::REQ.ModeloRequisitoOperativo" (
	"IdModeloRequisitoOperativo"	NVARCHAR(40)	NOT NULL DEFAULT '', -- POR EL MOMENTO SOLO UNO.
	"Descripcion"					NVARCHAR(100)	NOT NULL DEFAULT '',	
	"IdUsuarioCreador"				BIGINT			NOT NULL DEFAULT 0,
	"FechaCreacion" 				SECONDDATE  	NOT NULL DEFAULT '0001-01-01 00:00:00',
	"FechaModificacion" 			SECONDDATE  	NOT NULL DEFAULT '0001-01-01 00:00:00',  
	"IdEmpresa"         			BIGINT          NOT NULL DEFAULT 0,  
	"IdEstado" 						NVARCHAR(3) 	DEFAULT 'ACT' NOT NULL,
	primary key ("IdModeloRequisitoOperativo")
);

/*==============================================================*/
/* SECUENCIAS:                                                  */
/*==============================================================*/

CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::IdRequisitoOperativo"
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;

/*==============================================================*/
/* Tipos:                                                       */
/*==============================================================*/


/*==============================================================*/
/* constraint:                                                  */
/*==============================================================*/


/*==============================================================*/
/* Index:                                                       */
/*==============================================================*/

/* Table: */

--create index "INX01_Evento" on "BSCL.Plataforma.BD::AUT.Evento"("IdTipoOperacion","IdConcepto","NumeroOperacionConcepto","FechaOperacionConcepto","IdEstado");

/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit;


