INSERT INTO "BSCL.Mesa.BD::GBL.NotificarCartaLiquidacion_Proceso_Paso"(
"IdTrabajo","IdCartera", "IdOperacion", "FolioOperacion","FechaOperacion","IdEstado")
VALUES (:IdTrabajo,:IdCartera,:IdOperacion,:FolioOperacion,TO_DATE(:FechaOperacion,'YYYY-MM-DD'),:IdEstado);

