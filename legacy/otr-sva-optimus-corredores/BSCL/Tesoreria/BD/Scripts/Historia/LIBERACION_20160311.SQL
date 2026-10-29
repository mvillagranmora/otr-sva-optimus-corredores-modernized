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
-- LIBERACION SEMANA: 11-03-2016
--


--################################################
--      JVILLAGRAN <AJUSTES TESORERIA>  
--################################################

-- Agregar campo 'IncluyeNomina' a tabla 'CuentaBanco'
ALTER TABLE "BSCL.Tesoreria.BD::TES.CuentaBanco"
ADD ("IncluyeNomina"     NVARCHAR(1)   NOT NULL DEFAULT 'N');

-- Agregar campo 'GeneraTransferencia' a tabla 'CuentaBanco'
ALTER TABLE "BSCL.Tesoreria.BD::TES.CuentaBanco"
ADD ("GeneraTransferencia"      NVARCHAR(1)     NOT NULL DEFAULT 'N');


drop table  "BSCL.Tesoreria.BD::TES.PreferenciaGiro" cascade;
CREATE COLUMN TABLE "BSCL.Tesoreria.BD::TES.PreferenciaGiro" (
 "IdPreferenciaGiro"      BIGINT 		 NOT NULL,
 "IdConcepto" 			  NVARCHAR(6) 	 NOT NULL,
 "IdMedioDePago"          NVARCHAR(3)    NOT NULL,
 "IdCuentaBanco"		  BIGINT     	 NOT NULL,
 "IdEstado"				  NVARCHAR(3) 	 NOT NULL, 
PRIMARY KEY ("IdPreferenciaGiro") );

alter table "BSCL.Tesoreria.BD::TES.PreferenciaGiro"
   add constraint  FK_MedioDePago_PreferenciaGiro foreign key ("IdMedioDePago")
	      references "BSCL.Plataforma.BD::PAG.MedioDePago" ("IdMedioDePago") on delete restrict on update restrict;

alter table "BSCL.Tesoreria.BD::TES.PreferenciaGiro"
   add constraint  FK_Concepto_PreferenciaGiro foreign key ("IdConcepto")
	      references "BSCL.Tesoreria.BD::TES.ConceptoTesoreria" ("IdConceptoTesoreria") on delete restrict on update restrict;

alter table "BSCL.Tesoreria.BD::TES.PreferenciaGiro"
   add constraint  FK_CuentaBanco_PreferenciaGiro foreign key ("IdCuentaBanco")
	      references "BSCL.Tesoreria.BD::TES.CuentaBanco" ("IdCuentaBanco") on delete restrict on update restrict;

alter table "BSCL.Tesoreria.BD::TES.PreferenciaGiro"
   add constraint  FK_Estado_PreferenciaGiro foreign key ("IdEstado")
	      references "BSCL.Tesoreria.BD::TES.EstadoTesoreria" ("IdEstadoTesoreria") on delete restrict on update restrict;

drop table  "BSCL.Tesoreria.BD::TES.NominaTransferencia"  cascade;
CREATE COLUMN TABLE "BSCL.Tesoreria.BD::TES.NominaTransferencia" (
 "IdNominaTransferencia"     	BIGINT 		  NOT NULL, 
 "IdEmpresa"  			     	BIGINT 		  NOT NULL,  
 "IdComprobantePartidaEgreso"	BIGINT 		  NOT NULL DEFAULT 0,
 "FechaMovimiento"				TIMESTAMP     NOT NULL DEFAULT '0001-01-01 00:00:00',  --Fecha sistema
 "FechaIngresoRegistro"			TIMESTAMP	  NOT NULL DEFAULT '0001-01-01 00:00:00', --CurrentDate
 "FechaLiquidacion"				DATE 		  NOT NULL DEFAULT '0001-01-01', -- 
 "FechaRecepcion"				DATE 		  NOT NULL DEFAULT '0001-01-01', -- Fecha en que el banco recibio con exito la nomina
 "IdMedioDePago"                NVARCHAR(3)   NOT NULL,
 "IdModalidadDePago"            NVARCHAR(6)   NOT NULL,  -- MODCAJ: Por Caja   MODDEP: Deósito en Cta. Cte
 "IdCuentaBanco" 				BIGINT        DEFAULT 0, -- Corredora
 "IdTipoCuentaBanco"  	    	NVARCHAR(4)   NOT NULL,
 "IdMoneda" 					NVARCHAR(3)   NOT NULL,
 "Monto" 						DECIMAL(26,8) DEFAULT 0, 
 "NombreArchivo"	            NVARCHAR(100) NOT NULL DEFAULT '', 
 "IdFormatoArchivo"				BIGINT 		  NOT NULL DEFAULT 0,
 "IdFormatoNomina"				BIGINT 		  NOT NULL DEFAULT 0,
 "EsNomina"                     NVARCHAR(1)   NOT NULL DEFAULT 'N',
 "EsTransferencia"              NVARCHAR(1)   NOT NULL DEFAULT 'N',
 "IdUsuario"                    BIGINT	      NOT NULL,
 "IdEstado"				        NVARCHAR(3)   NOT NULL, 
PRIMARY KEY ("IdNominaTransferencia") );


/* Table: BSCL.Tesoreria.BD::TES.NominaTransferencia */
alter table "BSCL.Tesoreria.BD::TES.NominaTransferencia"
   add constraint  FK_Empresa_NominaTransferencia foreign key ("IdEmpresa")
	      references "BSCL.Plataforma.BD::PLA.Empresa" ("IdEmpresa") on delete restrict on update restrict;

alter table "BSCL.Tesoreria.BD::TES.NominaTransferencia"
   add constraint  FK_MedioDePago_NominaTransferencia foreign key ("IdMedioDePago")
	      references "BSCL.Plataforma.BD::PAG.MedioDePago" ("IdMedioDePago") on delete restrict on update restrict;

alter table "BSCL.Tesoreria.BD::TES.NominaTransferencia"
   add constraint  FK_CuentaBanco_NominaTransferencia foreign key ("IdCuentaBanco")
	      references "BSCL.Tesoreria.BD::TES.CuentaBanco" ("IdCuentaBanco") on delete restrict on update restrict;

alter table "BSCL.Tesoreria.BD::TES.NominaTransferencia"
	   add constraint  FK_TipoCuentaBancaria_NominaTransferencia foreign key ("IdTipoCuentaBanco")
		      references "BSCL.Plataforma.BD::BAN.TipoCuentaBancaria" ("IdTipoCuentaBancaria") on delete restrict on update restrict;

alter table "BSCL.Tesoreria.BD::TES.NominaTransferencia"
	   add constraint  FK_Moneda_NominaTransferencia foreign key ("IdMoneda")
		      references "BSCL.Plataforma.BD::PLA.Moneda" ("IdMoneda") on delete restrict on update restrict;

alter table "BSCL.Tesoreria.BD::TES.NominaTransferencia" 
   add constraint  FK_Usuario_NominaTransferencia foreign key ("IdUsuario")
      references "BSCL.Plataforma.BD::ACC.Usuario" ("IdUsuario") on delete restrict on update restrict;
     
alter table "BSCL.Tesoreria.BD::TES.NominaTransferencia"
   add constraint  FK_Estado_NominaTransferencia foreign key ("IdEstado")
	      references "BSCL.Plataforma.BD::PLA.Estado" ("IdEstado") on delete restrict on update restrict;


drop table  "BSCL.Tesoreria.BD::TES.NominaTransferenciaDetalle"  cascade;
CREATE COLUMN TABLE "BSCL.Tesoreria.BD::TES.NominaTransferenciaDetalle" (
 "IdNominaTransferenciaDetalle" BIGINT 		  NOT NULL,
 "IdNominaTransferencia"     	BIGINT 		  NOT NULL,
 "IdComprobante"				BIGINT 		  NOT NULL,
 "Monto" 						DECIMAL(26,8) DEFAULT 0,
 "IdUsuario"                    BIGINT	      NOT NULL,
 "IdEstado"				        NVARCHAR(3)   NOT NULL, 
PRIMARY KEY ("IdNominaTransferenciaDetalle") );


/* Table: BSCL.Tesoreria.BD::TES.NominaTransferenciaDetalle */
alter table "BSCL.Tesoreria.BD::TES.NominaTransferenciaDetalle"
   add constraint  FK_NominaTransferencia_NominaTransferenciaDetalle foreign key ("IdNominaTransferencia")
	      references "BSCL.Tesoreria.BD::TES.NominaTransferencia" ("IdNominaTransferencia") on delete restrict on update restrict;

alter table "BSCL.Tesoreria.BD::TES.NominaTransferenciaDetalle"
   add constraint  FK_Comprobante_NominaTransferenciaDetalle foreign key ("IdComprobante")
	      references "BSCL.Tesoreria.BD::TES.Comprobante" ("IdComprobante") on delete restrict on update restrict;

alter table "BSCL.Tesoreria.BD::TES.NominaTransferenciaDetalle" 
   add constraint  FK_Usuario_NominaTransferenciaDetalle foreign key ("IdUsuario")
      references "BSCL.Plataforma.BD::ACC.Usuario" ("IdUsuario") on delete restrict on update restrict;
      
alter table "BSCL.Tesoreria.BD::TES.NominaTransferenciaDetalle"
   add constraint  FK_Estado_NominaTransferenciaDetalle foreign key ("IdEstado")
	      references "BSCL.Plataforma.BD::PLA.Estado" ("IdEstado") on delete restrict on update restrict;


CREATE SEQUENCE "BSCL.Tesoreria.BD.Secuencias::PreferenciaGiro"
	   START     WITH 1
       INCREMENT BY 1
       MINVALUE  1; 
       
CREATE SEQUENCE "BSCL.Tesoreria.BD.Secuencias::NominaTransferencia"
	   START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
       
CREATE SEQUENCE "BSCL.Tesoreria.BD.Secuencias::NominaTransferenciaDetalle"
	   START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;   


-- Nuevos Conceptos para comprobantes
INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
SELECT (MAX("IdEstadoTesoreria")+1),'GIR','TES.Comprobante' From "BSCL.Tesoreria.BD::TES.EstadoTesoreria"; -- Girado Completamente
INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
SELECT (MAX("IdEstadoTesoreria")+1),'GIP','TES.Comprobante' From "BSCL.Tesoreria.BD::TES.EstadoTesoreria"; -- Girado Parcialmente

INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
SELECT (MAX("IdEstadoTesoreria")+1),'ING','TES.NominaTransferencia' From "BSCL.Tesoreria.BD::TES.EstadoTesoreria";
INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
SELECT (MAX("IdEstadoTesoreria")+1),'ANU','TES.NominaTransferencia' From "BSCL.Tesoreria.BD::TES.EstadoTesoreria";
INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
SELECT (MAX("IdEstadoTesoreria")+1),'RCP','TES.NominaTransferencia' From "BSCL.Tesoreria.BD::TES.EstadoTesoreria";
INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
SELECT (MAX("IdEstadoTesoreria")+1),'ING','TES.NominaTransferenciaDetalle' From "BSCL.Tesoreria.BD::TES.EstadoTesoreria";
INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
SELECT (MAX("IdEstadoTesoreria")+1),'ANU','TES.NominaTransferenciaDetalle' From "BSCL.Tesoreria.BD::TES.EstadoTesoreria";
INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
SELECT (MAX("IdEstadoTesoreria")+1),'RCP','TES.NominaTransferenciaDetalle' From "BSCL.Tesoreria.BD::TES.EstadoTesoreria";

--------------------------------------------------
--      FIN <AJUSTES TESORERIA>
--------------------------------------------------
