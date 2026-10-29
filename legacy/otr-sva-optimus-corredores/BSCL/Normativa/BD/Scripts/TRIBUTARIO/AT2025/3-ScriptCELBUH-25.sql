SET SCHEMA BSCL;

UPDATE "BSCL.Normativa.BD::DOC.Campo" 
SET "OcultoEnGrilla" ='S', "OcultoEnMantenedor"='S', "OrdenCargaExterna"=0,
"Editable"='N', "Exportable"='N', "ModoMasivo"='N'
WHERE "Esquema" ='DJ1922' AND "Version" >= 2024
AND "Nombre" IN ('DiferenciaPorRescateOEnajenacion','DifPorRescateOEnajenacionCFIAcogido107');

UPDATE "BSCL.Normativa.BD::DOC.Campo" 
SET "OrdenCargaExterna"= "OrdenCargaExterna"-1 
WHERE "Esquema" ='DJ1922' AND "Version" >= 2024 
AND "OrdenCargaExterna"=26;

UPDATE "BSCL.Normativa.BD::DOC.Campo" 
SET "OrdenCargaExterna"= "OrdenCargaExterna"-2 
WHERE "Esquema" ='DJ1922' AND "Version" >= 2024 
AND "OrdenCargaExterna">=28;

COMMIT;