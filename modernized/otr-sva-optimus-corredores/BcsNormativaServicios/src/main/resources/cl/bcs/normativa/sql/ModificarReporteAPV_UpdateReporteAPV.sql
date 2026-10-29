UPDATE "BSCL.Normativa.BD::NOR.ReporteAPV"
SET 
    "SaldoAcumulado"              = :SaldoAcumulado,
    "SaldoRegimenTributarioA"     = :SaldoRegimenTributarioA,
    "SaldoRegimenTributarioB"     = :SaldoRegimenTributarioB,
    "SaldoBonificacionEstatal"    = :SaldoBonificacionEstatal,
    "SaldoAPVCEmpleador"          = :SaldoAPVCEmpleador,
    "SaldoAPVCTrabajadorA"        = :SaldoAPVCTrabajadorA,
    "SaldoAPVCTrabajadorB"        = :SaldoAPVCTrabajadorB,
    "SaldoAPVCBonificacionEstatal"= :SaldoAPVCBonificacionEstatal,
    "FechaModificacion"           = :FechaModificacion,
    "IdUsuarioModificacion"       = :IdUsuarioModificacion
WHERE 
    "IdReporteAPV" = :IdReporteAPV;