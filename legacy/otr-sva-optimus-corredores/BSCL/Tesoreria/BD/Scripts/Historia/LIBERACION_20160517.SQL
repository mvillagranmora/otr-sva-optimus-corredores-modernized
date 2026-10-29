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


--####################################################################################
-- <JVILLAGRAN> Liquidación de Comprobantes
--####################################################################################

ALTER TABLE "BSCL"."BSCL.Tesoreria.BD::TES.Comprobante"
ADD ("IdEvento"    BIGINT   DEFAULT 0);

DROP PROCEDURE "BSCL"."BSCL.Tesoreria.BD.Reglas::ServicioCumpleRequisitosAnularComprobantes";
DROP PROCEDURE "BSCL"."BSCL.Tesoreria.BD.Servicios::AnularComprobantes";

DROP PROCEDURE "BSCL.Tesoreria.BD.Servicios::AnularMovimientosTesoreria";
DROP PROCEDURE "BSCL.Tesoreria.BD.Reglas::ServicioCumpleRequisitosAnularMovimientosTesoreria";
DROP PROCEDURE "BSCL.Tesoreria.BD.Reglas::ConceptoExiste";
DROP PROCEDURE "BSCL.Tesoreria.BD.Servicios::CertificarMovimientosTesoreria";


--####################################################################################
-- <NRM> 
--####################################################################################

DELETE FROM "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio" in (141,142);


--####################################################################################
-- <NROJAS> Nueva tabla por configuracion de saldos y actualización de datos
--####################################################################################

DROP TABLE "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago" CASCADE;
CREATE COLUMN TABLE "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago" (
 "IdTipoMovimientoMedioDePago"		BIGINT			NOT NULL,
 "IdTipoMovimiento"				    NVARCHAR(6)		NOT NULL,
 "IdMedioDePago" 					NVARCHAR(3) 	NOT NULL,
 "AfectaSaldoEnEstado"				NVARCHAR(100),
 "DesplegarEnCombobox"				NVARCHAR(1),
PRIMARY KEY ("IdTipoMovimientoMedioDePago") );


DROP SEQUENCE "BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago"		CASCADE;
CREATE SEQUENCE "BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago"
	   START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
   

UPDATE "BSCL.Plataforma.BD::PAG.MedioDePago" SET "TipoMedioDePago" = 'CHE' WHERE "IdMedioDePago" = 'EBA';
INSERT INTO "BSCL.Plataforma.BD::PAG.MedioDePago" ( "IdMedioDePago","TipoMedioDePago","Nombre","IdEstado" ) 
VALUES('TRA','TRA','Transferencia','ACT');
INSERT INTO "BSCL.Plataforma.BD::PAG.MedioDePago" ( "IdMedioDePago","TipoMedioDePago","Nombre","IdEstado" ) 
VALUES('VVE','VVI','Vale Vista Electronico','ACT'); 

INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVING','CHE','CRT;DEP;LIQ','S');
INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVING','VVI','CRT;LIQ','S');
INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVING','BDE','CRT;LIQ','S');
INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVING','EFE','CRT;DEP;LIQ','S');
INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVING','CIN','ING;CRT;LIQ','S');
INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVING','TLR','ING;CRT;LIQ','S');
INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVING','TSW','ING;CRT;LIQ','S');
INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVING','TDP','ING;CRT;LIQ','S');
INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVING','EBA','CRT;LIQ','S');
INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVING','TRA','ING;CRT;LIQ','S');
INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVING','VVE','ING;CRT;LIQ','S');
INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVEGR','CHE','EMI;CRT;LIQ','S');
INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVEGR','VVI','EMI;CRT;LIQ','S');
INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVEGR','BDE',NULL,'N');
INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVEGR','EFE','ING;EMI;CRT;LIQ','S');
INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVEGR','CIN','ING;EMI;CRT;LIQ','S');
INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVEGR','TLR','ING;EMI;CRT;LIQ','S');
INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVEGR','TSW','ING;EMI;CRT;LIQ','S');
INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVEGR','TDP','ING;EMI;CRT;LIQ','S');
INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVEGR','EBA',NULL,'N');
INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVEGR','TRA','ING;EMI;CRT;LIQ','S');
INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVEGR','VVE','ING;EMI;CRT;LIQ','S');

INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
VALUES(48,'EMI','TES.ComprobanteFormaDePago');
INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
VALUES(49,'CRT','TES.ComprobanteFormaDePago');

UPDATE "BSCL.Tesoreria.BD::TES.EstadoTesoreria" SET "IdEstado" = 'EMI' 
 WHERE "NombreEntidad" = 'TES.DepositoDetalle' AND "IdEstado" = 'DEP';


DELETE FROM "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio" = 102;


ALTER TABLE "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" ADD ("IdNominaTransferencia" BIGINT);
ALTER TABLE "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" ADD ("IdDeposito" BIGINT);


--####################################################################################
-- <NSM>
--####################################################################################
INSERT INTO "BSCL.Tesoreria.BD::TES.ConceptoTesoreria" ("IdConceptoTesoreria","IdConcepto","NombreEntidad","MultipleConcepto","Origen","Tipo")
SELECT (MAX("IdConceptoTesoreria")+1),'FOANEM','TES.IngresoEgreso','N','FAC','' From "BSCL.Tesoreria.BD::TES.ConceptoTesoreria";
INSERT INTO "BSCL.Tesoreria.BD::TES.ConceptoTesoreria" ("IdConceptoTesoreria","IdConcepto","NombreEntidad","MultipleConcepto","Origen","Tipo")
SELECT (MAX("IdConceptoTesoreria")+1),'FOVEEM','TES.IngresoEgreso','N','FAC','' From "BSCL.Tesoreria.BD::TES.ConceptoTesoreria";
INSERT INTO "BSCL.Tesoreria.BD::TES.ConceptoTesoreria" ("IdConceptoTesoreria","IdConcepto","NombreEntidad","MultipleConcepto","Origen","Tipo")
SELECT (MAX("IdConceptoTesoreria")+1),'FOPRIM','TES.IngresoEgreso','N','FAC','' From "BSCL.Tesoreria.BD::TES.ConceptoTesoreria";
INSERT INTO "BSCL.Tesoreria.BD::TES.ConceptoTesoreria" ("IdConceptoTesoreria","IdConcepto","NombreEntidad","MultipleConcepto","Origen","Tipo")
SELECT (MAX("IdConceptoTesoreria")+1),'NCEACS','TES.IngresoEgreso','N','FAC','' From "BSCL.Tesoreria.BD::TES.ConceptoTesoreria";
INSERT INTO "BSCL.Tesoreria.BD::TES.ConceptoTesoreria" ("IdConceptoTesoreria","IdConcepto","NombreEntidad","MultipleConcepto","Origen","Tipo")
SELECT (MAX("IdConceptoTesoreria")+1),'NCRACS','TES.IngresoEgreso','N','FAC','' From "BSCL.Tesoreria.BD::TES.ConceptoTesoreria";
INSERT INTO "BSCL.Tesoreria.BD::TES.ConceptoTesoreria" ("IdConceptoTesoreria","IdConcepto","NombreEntidad","MultipleConcepto","Origen","Tipo")
SELECT (MAX("IdConceptoTesoreria")+1),'NCEAVS','TES.IngresoEgreso','N','FAC','' From "BSCL.Tesoreria.BD::TES.ConceptoTesoreria";
INSERT INTO "BSCL.Tesoreria.BD::TES.ConceptoTesoreria" ("IdConceptoTesoreria","IdConcepto","NombreEntidad","MultipleConcepto","Origen","Tipo")
SELECT (MAX("IdConceptoTesoreria")+1),'NCRAVS','TES.IngresoEgreso','N','FAC','' From "BSCL.Tesoreria.BD::TES.ConceptoTesoreria";
