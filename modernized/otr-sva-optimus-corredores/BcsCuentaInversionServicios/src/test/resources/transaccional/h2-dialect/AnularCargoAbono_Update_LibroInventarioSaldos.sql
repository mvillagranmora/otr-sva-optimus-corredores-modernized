-- HANA-SOURCE: cl/bcs/cuentaInversion/sql/AnularCargoAbono_Update_LibroInventarioSaldos.sql
-- H2 rewrite of the HANA multi-table UPDATE ... FROM SRV CA INNER JOIN LibroInventarioDeSaldos LIS ON (..OR..): EXISTS instead of the join.
UPDATE "BSCL.CuentaInversion.BD::CTA.LibroInventarioDeSaldos" AS LIS
SET "IdEstado" = 'ANU'
WHERE EXISTS (SELECT 1 FROM "BSCL.CuentaInversion.BD::CTA.AnularCargoAbono_SRV_Movimientos_Anular" AS CA
              WHERE (LIS."IdCargoAbonoMovimiento" = CA."IdCargoAbonoMovimiento" OR LIS."IdCargoAbonoMovimientoCalce" = CA."IdCargoAbonoMovimiento"));
