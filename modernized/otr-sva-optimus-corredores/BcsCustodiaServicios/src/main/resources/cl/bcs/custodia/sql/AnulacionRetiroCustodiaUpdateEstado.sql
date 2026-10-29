UPDATE "BSCL.Custodia.BD::CUS.Cartera" SET
  "IdEstado" = COALESCE(:IdEstado,"IdEstado")
WHERE "IdCartera" = (:IdCartera);