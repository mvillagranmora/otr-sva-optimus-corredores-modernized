-- HANA-SOURCE: cl/bcs/cuentaInversion/sql/AnularCargoAbono_Update_EstadoMovimiento.sql
-- H2 rewrite of the HANA multi-table "UPDATE CAM SET .. FROM SRV CA INNER JOIN CAM ON .." : target table updated directly, the
-- join with SRV becomes EXISTS (same rows are updated; the SET value does not depend on SRV).
UPDATE "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" AS CAM
SET "IdEstado" = 'ANU'
WHERE CAM."IdEmpresa" = :p_IdEmpresa
  AND EXISTS (SELECT 1 FROM "BSCL.CuentaInversion.BD::CTA.AnularCargoAbono_SRV_Movimientos_Anular" AS CA
              WHERE CAM."IdCargoAbonoMovimiento" = CA."IdCargoAbonoMovimiento");
