UPDATE "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate"
SET 
"CostoMonedaLocal" = :CostoMonedaLocal
,"Monto" = :Monto
,"ValorCuotaInversion" = :ValorCuota
,"NumeroCuotas" = :NumeroCuotas
,"ParidadFechaInversion" = :ParidadFechaInversion
,"CostoUF" = :CostoUF
,"ValorUFFechaInversion" = :ValorUFFechaInversion
,"IdMonedaSistema" = :IdMonedaSistema
,"MontoMonedaSistema" = :MontoMonedaSistema
,"MontoInversion" = :Monto
,"NumeroCuotasInversion" = :NumeroCuotas
,"CostoMonedaLocalInversion" = :CostoMonedaLocal
WHERE "IdSolicitudAporteRescate" = :IdSolicitudAporteRescate AND "Estado" IN ('PEN','ING')