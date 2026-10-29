-- Actualiza estado de formas de pago de movimientos de ingreso egreso
UPDATE IEFP    
   SET "IdEstado" = 'EMI'
FROM "BSCL.Tesoreria.BD::TES.IngresoEgresoFormaDePago" IEFP
INNER JOIN "BSCL.Tesoreria.BD::GBL.IngresoEgresoGenerarComprobante_Proceso" IEG ON IEFP."IdIngresoEgreso" = IEG."IdIngresoEgreso"

WHERE IEFP."IdEstado" <> 'MOD'; -- Registro modificados quedan tal cual