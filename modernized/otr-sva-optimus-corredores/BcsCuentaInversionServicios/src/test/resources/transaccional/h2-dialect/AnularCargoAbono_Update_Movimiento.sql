-- HANA-SOURCE: cl/bcs/cuentaInversion/sql/AnularCargoAbono_Update_Movimiento.sql
-- H2 rewrite of the HANA 3-table UPDATE ... FROM SRV CA INNER JOIN LIS ON (..) INNER JOIN CAM ON (..): scalar subquery for the new
-- value (MAX picks one value if several (CA,LIS) pairs match the same CAM, HANA picks one arbitrarily) + EXISTS for the row filter.
UPDATE "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" AS CAM
SET "SaldoLibroInventario" = (
      SELECT MAX(CAM."SaldoLibroInventario" + LIS."MontoCalce")
        FROM "BSCL.CuentaInversion.BD::CTA.AnularCargoAbono_SRV_Movimientos_Anular" AS CA
       INNER JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarLibroInventarioDeSaldos" AS LIS
          ON (LIS."IdCargoAbonoMovimiento" = CA."IdCargoAbonoMovimiento" OR LIS."IdCargoAbonoMovimientoCalce" = CA."IdCargoAbonoMovimiento")
       WHERE CAM."IdCargoAbonoMovimiento" = CASE WHEN LIS."IdCargoAbonoMovimiento" = CA."IdCargoAbonoMovimiento" THEN LIS."IdCargoAbonoMovimientoCalce"
                                                 WHEN LIS."IdCargoAbonoMovimientoCalce" = CA."IdCargoAbonoMovimiento" THEN LIS."IdCargoAbonoMovimiento" END)
WHERE CAM."IdEstado" <> 'ANU'
  AND EXISTS (
      SELECT 1
        FROM "BSCL.CuentaInversion.BD::CTA.AnularCargoAbono_SRV_Movimientos_Anular" AS CA
       INNER JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarLibroInventarioDeSaldos" AS LIS
          ON (LIS."IdCargoAbonoMovimiento" = CA."IdCargoAbonoMovimiento" OR LIS."IdCargoAbonoMovimientoCalce" = CA."IdCargoAbonoMovimiento")
       WHERE CAM."IdCargoAbonoMovimiento" = CASE WHEN LIS."IdCargoAbonoMovimiento" = CA."IdCargoAbonoMovimiento" THEN LIS."IdCargoAbonoMovimientoCalce"
                                                 WHEN LIS."IdCargoAbonoMovimientoCalce" = CA."IdCargoAbonoMovimiento" THEN LIS."IdCargoAbonoMovimiento" END);
