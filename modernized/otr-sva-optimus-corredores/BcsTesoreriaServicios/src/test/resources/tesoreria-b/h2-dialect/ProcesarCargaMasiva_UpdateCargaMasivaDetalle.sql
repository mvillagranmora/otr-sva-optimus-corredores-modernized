-- HANA-SOURCE: cl/bcs/tesoreria/sql/ProcesarCargaMasiva_UpdateCargaMasivaDetalle.sql
-- H2 rewrite of the HANA UPDATE CMD SET .. FROM "CargaMasivaDetalle" AS CMD INNER JOIN "..GBL.ProcesarCargaMasiva_Proceso" AS PROC ON (..) WHERE ..:
-- the join provides PROC.CodigoTipoCargaMasiva (scalar subquery, one row per IdCargaMasiva in the staging table) and filters the rows (EXISTS).
UPDATE "BSCL.Tesoreria.BD::CAR.CargaMasivaDetalle" AS CMD
SET "IdEstado" = (CASE WHEN (COALESCE(:IdIngresoEgreso,0) <> 0) THEN 'PRO' ELSE CMD."IdEstado" END)
   ,"IdMovimientoComprobante" = COALESCE(:IdIngresoEgreso,0)
   ,"ObservacionValidacion" = (CASE
        WHEN ((SELECT MAX(PROC."CodigoTipoCargaMasiva") FROM "BSCL.Tesoreria.BD::GBL.ProcesarCargaMasiva_Proceso" AS PROC WHERE PROC."IdCargaMasiva" = CMD."IdCargaMasiva") = 'CONCILIACION_RECAUDACION_BCP' AND :ExisteMovimiento = 'S')
        THEN 'Movimiento ya Existe con folio  ' || CAST(:IdMovimientoExistente AS NVARCHAR(50))
        ELSE CMD."ObservacionValidacion" END)
WHERE EXISTS (SELECT 1 FROM "BSCL.Tesoreria.BD::GBL.ProcesarCargaMasiva_Proceso" AS PROC WHERE PROC."IdCargaMasiva" = CMD."IdCargaMasiva" AND PROC."IdCargaMasiva" = :IdCargaMasiva)
AND CMD."IdCargaMasivaDetalle" = :IdCargaMasivaDetalle
