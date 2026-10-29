
-- Borra de la tabla proceso los registron invalidos
DELETE
FROM "BSCL.Tesoreria.BD::GBL.IngresoEgresoGenerarComprobante_Proceso"
WHERE "IdIngresoEgresoInformado" = :IdIngresoEgresoInformado;
