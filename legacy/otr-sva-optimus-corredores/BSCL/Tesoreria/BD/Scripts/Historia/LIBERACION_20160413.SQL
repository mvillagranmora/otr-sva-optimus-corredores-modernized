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
-- LIBERACION SEMANA: 08-04-2016
--


--################################################
--      NROJAS <Cambio de Nombres en modelo>  
--################################################

RENAME COLUMN "BSCL.Tesoreria.BD::TES.IngresoEgreso"."IdCartera" TO "FolioCartera";
RENAME COLUMN "BSCL.Tesoreria.BD::TES.IngresoEgreso"."IdMonedaOrigen" TO "IdMonedaOperacion";
RENAME COLUMN "BSCL.Tesoreria.BD::TES.IngresoEgreso"."MontoOrigen" TO "MontoOperacion";


RENAME COLUMN "BSCL.Tesoreria.BD::TES.ComprobanteDetalle"."IdCartera" TO "FolioCartera";
RENAME COLUMN "BSCL.Tesoreria.BD::TES.ComprobanteDetalle"."IdMonedaOrigen" TO "IdMonedaOperacion";
RENAME COLUMN "BSCL.Tesoreria.BD::TES.ComprobanteDetalle"."MontoOrigen" TO "MontoOperacion";


--################################################
--      JVILLAGRAN <Cambio modelo>  
--################################################

ALTER TABLE "BSCL"."BSCL.Tesoreria.BD::TES.ComprobanteDetalle"
DROP ("FolioTransaccion");

ALTER TABLE "BSCL"."BSCL.Tesoreria.BD::TES.ComprobanteDetalle"
DROP ("FechaTransaccion");


ALTER TABLE "BSCL"."BSCL.Tesoreria.BD::TES.IngresoEgreso"
DROP ("FolioTransaccion");

ALTER TABLE "BSCL"."BSCL.Tesoreria.BD::TES.IngresoEgreso"
DROP ("FechaTransaccion");


ALTER TABLE "BSCL"."BSCL.Tesoreria.BD::TES.ConceptoTesoreria"
ADD("Tipo" NVARCHAR(2)   NOT NULL DEFAULT '');


Update "BSCL"."BSCL.Tesoreria.BD::TES.ConceptoTesoreria"
Set "Tipo" = 'IN'
Where "IdConcepto" IN ('VCTSIC', 'ANTSIC', 'FACMSM', 'FACMAC', 'FECMSM', 'FECMAC','INCLC0','INCLC1','INCLC2');

Update "BSCL"."BSCL.Tesoreria.BD::TES.ConceptoTesoreria"
Set "Tipo" = 'EG'
Where "IdConcepto" IN ('ANTSIV','VCTSIV','FAVTSM','FAVNAC','FEVTSM','FEVNAC','EGCLC0','EGCLC1','EGCLC2');



INSERT INTO "BSCL.Tesoreria.BD::TES.ConceptoTesoreria" ("IdConceptoTesoreria","IdConcepto","NombreEntidad","MultipleConcepto","Origen","Tipo")
SELECT (MAX("IdConceptoTesoreria")+1),'FEPRES','TES.IngresoEgreso','N','FAC','' From "BSCL.Tesoreria.BD::TES.ConceptoTesoreria";
INSERT INTO "BSCL.Tesoreria.BD::TES.ConceptoTesoreria" ("IdConceptoTesoreria","IdConcepto","NombreEntidad","MultipleConcepto","Origen","Tipo")
SELECT (MAX("IdConceptoTesoreria")+1),'FOPRES','TES.IngresoEgreso','N','FAC','' From "BSCL.Tesoreria.BD::TES.ConceptoTesoreria";
INSERT INTO "BSCL.Tesoreria.BD::TES.ConceptoTesoreria" ("IdConceptoTesoreria","IdConcepto","NombreEntidad","MultipleConcepto","Origen","Tipo")
SELECT (MAX("IdConceptoTesoreria")+1),'FECMAX','TES.IngresoEgreso','N','FAC','IN' From "BSCL.Tesoreria.BD::TES.ConceptoTesoreria";
INSERT INTO "BSCL.Tesoreria.BD::TES.ConceptoTesoreria" ("IdConceptoTesoreria","IdConcepto","NombreEntidad","MultipleConcepto","Origen","Tipo")
SELECT (MAX("IdConceptoTesoreria")+1),'FEVNAX','TES.IngresoEgreso','N','FAC','EG' From "BSCL.Tesoreria.BD::TES.ConceptoTesoreria";
INSERT INTO "BSCL.Tesoreria.BD::TES.ConceptoTesoreria" ("IdConceptoTesoreria","IdConcepto","NombreEntidad","MultipleConcepto","Origen","Tipo")
SELECT (MAX("IdConceptoTesoreria")+1),'FECMMI','TES.IngresoEgreso','N','FAC','IN' From "BSCL.Tesoreria.BD::TES.ConceptoTesoreria";
INSERT INTO "BSCL.Tesoreria.BD::TES.ConceptoTesoreria" ("IdConceptoTesoreria","IdConcepto","NombreEntidad","MultipleConcepto","Origen","Tipo")
SELECT (MAX("IdConceptoTesoreria")+1),'FEVNMI','TES.IngresoEgreso','N','FAC','EG' From "BSCL.Tesoreria.BD::TES.ConceptoTesoreria";
INSERT INTO "BSCL.Tesoreria.BD::TES.ConceptoTesoreria" ("IdConceptoTesoreria","IdConcepto","NombreEntidad","MultipleConcepto","Origen","Tipo")
SELECT (MAX("IdConceptoTesoreria")+1),'FECMRF','TES.IngresoEgreso','N','FAC','IN' From "BSCL.Tesoreria.BD::TES.ConceptoTesoreria";
INSERT INTO "BSCL.Tesoreria.BD::TES.ConceptoTesoreria" ("IdConceptoTesoreria","IdConcepto","NombreEntidad","MultipleConcepto","Origen","Tipo")
SELECT (MAX("IdConceptoTesoreria")+1),'FEVNRF','TES.IngresoEgreso','N','FAC','EG' From "BSCL.Tesoreria.BD::TES.ConceptoTesoreria";
INSERT INTO "BSCL.Tesoreria.BD::TES.ConceptoTesoreria" ("IdConceptoTesoreria","IdConcepto","NombreEntidad","MultipleConcepto","Origen","Tipo")
SELECT (MAX("IdConceptoTesoreria")+1),'FECMIF','TES.IngresoEgreso','N','FAC','IN' From "BSCL.Tesoreria.BD::TES.ConceptoTesoreria";
INSERT INTO "BSCL.Tesoreria.BD::TES.ConceptoTesoreria" ("IdConceptoTesoreria","IdConcepto","NombreEntidad","MultipleConcepto","Origen","Tipo")
SELECT (MAX("IdConceptoTesoreria")+1),'FEVNIF','TES.IngresoEgreso','N','FAC','EG' From "BSCL.Tesoreria.BD::TES.ConceptoTesoreria";


