-- ########################################################################################
--
--  =========================================================
--  ESTE ARCHIVO CONTIEN LOS SCRIPT HISTORICOS DE LIBERACION
--  =========================================================
--  * Debe incluir los ALTER, DROP(solo definitivos) de Tablas , Secuencias, etc

-- ########################################################################################

SET SCHEMA BSCL;

ALTER TABLE BSCL."BSCL.Portafolio.BD::POR.Portafolio"
  ADD (	"IdMatrizAgrupadorAPV" 		BIGINT			DEFAULT 0 	NULL);GO
  
  