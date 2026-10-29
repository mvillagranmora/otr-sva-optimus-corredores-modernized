UPDATE "BSCL.Custodia.BD::HIS.ControlFusionCartera"
SET "IdEstado" = :IdEstado, "Resultado" = :Resultado, "FechaOperacion" = CURRENT_TIMESTAMP
WHERE "IdControlFusion" = :IdControlFusion;