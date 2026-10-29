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
-- <NRM> 
--####################################################################################

 ALTER TABLE "BSCL.Tesoreria.BD::TES.IngresoEgreso" ALTER(
 "IdAgente" 	BIGINT NULL DEFAULT null,
 "IdSucursal" 	BIGINT NULL DEFAULT null
 );


 ALTER TABLE "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" ALTER(
 "IdAgente" 	BIGINT NULL DEFAULT null,
 "IdSucursal" 	BIGINT NULL DEFAULT null
 );

 
 DELETE FROM "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio" in (102,141,142);
 

--####################################################################################
-- <NS> 
--####################################################################################
INSERT INTO "BSCL.Tesoreria.BD::TES.ConceptoTesoreria" ("IdConceptoTesoreria","IdConcepto","NombreEntidad","MultipleConcepto","Origen","Tipo")
SELECT (MAX("IdConceptoTesoreria")+1),'NCECAC','TES.IngresoEgreso','N','FAC','' From "BSCL.Tesoreria.BD::TES.ConceptoTesoreria";

INSERT INTO "BSCL.Tesoreria.BD::TES.ConceptoTesoreria" ("IdConceptoTesoreria","IdConcepto","NombreEntidad","MultipleConcepto","Origen","Tipo")
SELECT (MAX("IdConceptoTesoreria")+1),'NCEVAC','TES.IngresoEgreso','N','FAC','' From "BSCL.Tesoreria.BD::TES.ConceptoTesoreria";

INSERT INTO "BSCL.Tesoreria.BD::TES.ConceptoTesoreria" ("IdConceptoTesoreria","IdConcepto","NombreEntidad","MultipleConcepto","Origen","Tipo")
SELECT (MAX("IdConceptoTesoreria")+1),'NCRVAC','TES.IngresoEgreso','N','FAC','' From "BSCL.Tesoreria.BD::TES.ConceptoTesoreria";

INSERT INTO "BSCL.Tesoreria.BD::TES.ConceptoTesoreria" ("IdConceptoTesoreria","IdConcepto","NombreEntidad","MultipleConcepto","Origen","Tipo")
SELECT (MAX("IdConceptoTesoreria")+1),'NCRCAC','TES.IngresoEgreso','N','FAC','' From "BSCL.Tesoreria.BD::TES.ConceptoTesoreria";
 