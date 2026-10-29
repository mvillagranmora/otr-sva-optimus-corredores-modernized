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
-- LIBERACION SEMANA: 30-05-2016 
--


INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (343, 'BSCL.Custodia.BD.Servicios::EVC.CertificaSuscripcionOSA' , 1,  'P', 'Certifica Suscripción de Acciones OSA');      

INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ("IdConcepto", "Tipo", "IdModulo", "Nombre", "IdEstado")
VALUES('FOANPR', '', 'FAC', 'FACTURA OTROS CONCEPTOS - ANTICIPO PRESTAMO', 'VIG');


INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (348, 'BSCL.Custodia.BD.Servicios::EVC.EnviarMovimientoFacturarSuscripcionOSA' , 1,  'P', '')

--06-06-2016 eric alarcon se incorpora el registro otro en la tabla profesiones
INSERT INTO "BSCL"."BSCL.Plataforma.BD::REG.Profesion"("IdProfesion","Codigo","Nombre","IdPais","IdEstado","FechaIngreso","FechaCreacion","FechaModificacion" )
VALUES(389,'0','Otro',44,'ACT','0001-01-01 00:00:00.000000000','0001-01-01 00:00:00.000000000','0001-01-01 00:00:00.000000000');


INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion","Codigo","Nombre","Accion","IdTipoProducto","Comprobante","EsOrden","EsSimultanea","EsVentaCorta","EsTransaccion","AccionOperacion","AccionCustodia","IdTipoOperacionRelacionada","RecibePagaPortafolio","EsAnticipo","EsVencimiento","EsAnulacion") VALUES ('SUSOSA','SUSOSA','SUSCRIPCION DE OSA', 'COMPRA','INAC','N','N','N','N','N','INGRESO','INGRESO','','','N','N','N');

INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento", "IdTipoOperacion", "IdTipoInstrumento", "IdEstado", "IdInstrumento", "Nemotecnico") 
VALUES (246,'SUSOSA','ACN','ACT', 0, '');

INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto"("IdConcepto","Tipo","IdModulo","Nombre","IdEstado") VALUES ('FECOSA','CA','FAC','FACTURA ELECTRONICA SUSCRIPCION DE OSA','VIG');

--06-06-2016 se elimina banco ya que se encuentra repetido
delete from "BSCL"."BSCL.Plataforma.BD::BAN.Banco" where "RutBanco" = '97011000-3' and "IdBanco"=30;


INSERT INTO "BSCL"."BSCL.Plataforma.BD::ACC.Acceso"
("IdAcceso", "NombreAcceso", "TipoAcceso", "Padre", "Orden", "IdServicio", "IdElementoHtml", "EsVisibleMenu")
SELECT (MAX("IdAcceso")+1),'ConsultaPrecios', 'SERV', 0, 0, 268, 'Consultar_Precio', 'N' From "BSCL"."BSCL.Plataforma.BD::ACC.Acceso";

INSERT INTO "BSCL"."BSCL.Plataforma.BD::ACC.Acceso"
("IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
SELECT (MAX("IdAcceso")+1),'Cartola Movimientos Custodia','SERV', 0, 0, 291, 'CARTOLA_MOVIMIENTOS', 'N' From "BSCL"."BSCL.Plataforma.BD::ACC.Acceso";

INSERT INTO "BSCL"."BSCL.Plataforma.BD::ACC.Acceso"
("IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
SELECT (MAX("IdAcceso")+1),'Anular Movimiento en Transito','SERV', 0, 0, 15, 'ANULA_MOVIMIENTO_TRANSITO', 'N' From "BSCL"."BSCL.Plataforma.BD::ACC.Acceso";

INSERT INTO "BSCL"."BSCL.Plataforma.BD::ACC.Acceso"
("IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
SELECT (MAX("IdAcceso")+1),'Consulta custodia portafolio','SERV', 0, 0, 22, 'CONSULTA_CUSTODIA_PORTAFOLIO', 'N' From "BSCL"."BSCL.Plataforma.BD::ACC.Acceso";

INSERT INTO "BSCL"."BSCL.Plataforma.BD::ACC.Acceso"
("IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
SELECT (MAX("IdAcceso")+1),'Consultar plazo','SERV', 0, 0, 60, 'CONSULTAR_PLAZO', 'N' From "BSCL"."BSCL.Plataforma.BD::ACC.Acceso";


----------------JPZ INICIO
DROP TABLE "BSCL.Plataforma.BD::INS.RelacionInstrumento" cascade;
DROP TABLE "BSCL.Plataforma.BD::INS.TipoRelacionInstrumento" cascade;

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
	  
----------------JPZ FIN

---ERIC ALARCON 10-06-2016
ALTER TABLE "BSCL.Plataforma.BD::IND.TipoIndicador"  ADD (
   "IdCarga"                BIGINT          DEFAULT  0 NOT NULL,
   "IdIndicadorMigracion"   BIGINT          DEFAULT  0 NOT NULL
   );
---ERIC ALARCON 13-06-2016
ALTER TABLE "BSCL.Plataforma.BD::IND.DetalleIndicador"  ADD (
   "IdCarga"                BIGINT          DEFAULT  0 NOT NULL
   );
--ERIC ALARCON 13-06-2016
ALTER TABLE "BSCL.Plataforma.BD::IND.Indicador"  ADD (
   "IdCarga"                BIGINT          DEFAULT  0 NOT NULL
   );   

   
   
   
INSERT INTO "BSCL.Plataforma.BD::INS.TipoRelacionInstrumento" ("IdTipoRelacionInstrumento","TipoRelacionInstrumento","IdEstado") 
	VALUES ('OPSUAC','Opción de Suscripción Acciones','ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoRelacionInstrumento" ("IdTipoRelacionInstrumento","TipoRelacionInstrumento","IdEstado") 
	VALUES ('PRIEMI','Primera Emisión','ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoRelacionInstrumento" ("IdTipoRelacionInstrumento","TipoRelacionInstrumento","IdEstado") 
	VALUES ('SEGEMI','Segunda Emisión','ACT');