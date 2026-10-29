-- HANA-SOURCE: cl/bcs/cuentaInversion/sql/AnularCargoAbono_Update_EventoCargoAbono.sql
-- HANA-LITERAL: p_IdEmpresa=1
-- H2 rewrite of "UPDATE CAM SET .. FROM (SELECT :IdEvento AS .., :NumeroOperacionConcepto AS .. FROM DUMMY) EVT INNER JOIN CAM ON .."
-- (the legacy replaces :p_IdEmpresa textually by the number before executing: AnularCargoAbono.java:285, hence the HANA-LITERAL line).
-- The one-row derived table is folded into the WHERE clause; parameters keep their order.
UPDATE "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" AS CAM
SET "IdEvento" = :IdEvento
WHERE CAM."IdCargoAbonoMovimiento" = :NumeroOperacionConcepto
  AND CAM."IdEmpresa" = :p_IdEmpresa;
