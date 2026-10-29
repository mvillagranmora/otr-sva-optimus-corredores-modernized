-- HANA-SOURCE: cl/bcs/tesoreria/sql/ProcesarCargaMasiva_UpdateCargaMasiva.sql
-- H2 rewrite of the HANA UPDATE CM SET .. FROM "CargaMasiva" AS CM INNER JOIN "..GBL.ProcesarCargaMasiva_Proceso" AS PROC ON (..) WHERE PROC.IdCargaMasiva = :IdCargaMasiva:
-- the join only filters (PROC contributes no value to the SET), so it becomes an EXISTS; SECONDDATE (HANA) is TIMESTAMP in H2.
UPDATE "BSCL.Tesoreria.BD::CAR.CargaMasiva" AS CM
SET "IdEstado" = 'PRO'
   ,"FechaProceso" = CAST(:FechaProceso AS TIMESTAMP)
   ,"IdMoneda" = :IdMoneda
   ,"IdConcepto" = :IdConcepto
WHERE EXISTS (SELECT 1 FROM "BSCL.Tesoreria.BD::GBL.ProcesarCargaMasiva_Proceso" AS PROC WHERE PROC."IdCargaMasiva" = CM."IdCargaMasiva" AND PROC."IdCargaMasiva" = :IdCargaMasiva)
