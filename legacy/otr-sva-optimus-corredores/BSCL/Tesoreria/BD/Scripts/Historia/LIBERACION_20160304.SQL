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
-- LIBERACION SEMANA: 04-03-2016
--



--################################################
--      JVILLAGRAN.AJUSTES, CONTROL TESORERIA
--################################################

-- Cambia nombre de algunos conceptos
UPDATE "BSCL.Plataforma.BD::PLA.Concepto"
SET "Nombre" = 'INGRESO'
WHERE "IdModulo" = 'TES' 
AND "IdConcepto" = 'MOVING'


UPDATE "BSCL.Plataforma.BD::PLA.Concepto"
SET "Nombre" = 'EGRESO'
WHERE "IdModulo" = 'TES' 
AND "IdConcepto" = 'MOVEGR'


-- Crea tabla de control de procesos
CREATE COLUMN TABLE "BSCL.Tesoreria.BD::TES.ProcesoTesoreria" (
 "IdProceso"			   BIGINT 			NOT NULL, 
 "IdEmpresa"  			   BIGINT 			NOT NULL, 
 "Codigo"				   NVARCHAR(40) 	NOT NULL, 
 "FechaProceso"            DATE     		NOT NULL,
 "FechaSistema"            DATE     		NOT NULL,
 "Observaciones"           NVARCHAR(250)    NOT NULL  DEFAULT '',
 "TransaccionesAfectadas"  BIGINT           NOT NULL  DEFAULT 0,
 "IdUsuario"               BIGINT	        NOT NULL,
 "IdEstado"				   NVARCHAR(3) 	    NOT NULL,
PRIMARY KEY ("IdProceso") );


alter table "BSCL.Tesoreria.BD::TES.ProcesoTesoreria"
   add constraint  FK_Usuario_ProcesoTesoreria foreign key ("IdUsuario")
	  references "BSCL.Plataforma.BD::ACC.Usuario" ("IdUsuario") on delete restrict on update restrict;


alter table "BSCL.Tesoreria.BD::TES.ProcesoTesoreria"
   add constraint  FK_Estado_ProcesoTesoreria foreign key ("IdEstado")
	      references "BSCL.Plataforma.BD::PLA.Estado" ("IdEstado") on delete restrict on update restrict;
		  

-- Se crean nuevos conceptos para Ingreso/Egreso(CCLV) 		  
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )
VALUES('INCLC0','','TES','INGRESO POR PAGO DE CCLV CAMARA 0 (RV)','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )
VALUES('EGCLC0','','TES','EGRESO POR PAGO A CCLV CAMARA 0 (RV)','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )
VALUES('INCLC1','','TES','INGRESO POR PAGO DE CCLV CAMARA 1 (PM)','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )
VALUES('EGCLC1','','TES','EGRESO POR PAGO A CCLV CAMARA 1 (PM)','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )
VALUES('INCLC2','','TES','INGRESO POR PAGO DE CCLV CAMARA 2 (PH+SM)','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )
VALUES('EGCLC2','','TES','EGRESO POR PAGO A CCLV CAMARA 2 (PH+SM)','VIG');

INSERT INTO "BSCL.Tesoreria.BD::TES.ConceptoTesoreria" ("IdConceptoTesoreria","IdConcepto","NombreEntidad")  
VALUES(9,'INCLC0','TES.IngresoEgresoCCLV');
INSERT INTO "BSCL.Tesoreria.BD::TES.ConceptoTesoreria" ("IdConceptoTesoreria","IdConcepto","NombreEntidad")  
VALUES(10,'EGCLC0','TES.IngresoEgresoCCLV');
INSERT INTO "BSCL.Tesoreria.BD::TES.ConceptoTesoreria" ("IdConceptoTesoreria","IdConcepto","NombreEntidad")  
VALUES(11,'INCLC1','TES.IngresoEgresoCCLV');
INSERT INTO "BSCL.Tesoreria.BD::TES.ConceptoTesoreria" ("IdConceptoTesoreria","IdConcepto","NombreEntidad")  
VALUES(12,'EGCLC1','TES.IngresoEgresoCCLV');
INSERT INTO "BSCL.Tesoreria.BD::TES.ConceptoTesoreria" ("IdConceptoTesoreria","IdConcepto","NombreEntidad")  
VALUES(13,'INCLC2','TES.IngresoEgresoCCLV');
INSERT INTO "BSCL.Tesoreria.BD::TES.ConceptoTesoreria" ("IdConceptoTesoreria","IdConcepto","NombreEntidad")  
VALUES(14,'EGCLC2','TES.IngresoEgresoCCLV');
		      
--------------------------------------------------
--      FIN <AJUSTES TESORERIA>
--------------------------------------------------
