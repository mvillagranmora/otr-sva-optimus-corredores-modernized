
INSERT INTO "BSCL.Normativa.BD::SIS.ProcesoMandatos"
("IdEmpresa",  "FechaIngreso", "IdEstado", "FolioCartera", "IdCartera", "IdSolicitudAporteRescate", "TipoSolicitud"
,"IdPortafolio","INGMontoATasaMercadoActual","CostoInversionEnFactorAjuste","PRO_CostoInversionEnFactorAjuste","EGRCostoInversionEnFactorAjuste","PRO_EGRCostoInversionEnFactorAjuste"
,"PRO_CostoTotalRescatado","MontoTotalRescatado","IdAgrupador","FechaCertificacion")
VALUES(:IdEmpresa,  CURRENT_UTCTIMESTAMP, :IdEstado, :FolioCartera, :IdCartera, :IdSolicitudAporteRescate, :TipoSolicitud
,:IdPortafolio,:INGMontoATasaMercadoActual,:CostoInversionEnFactorAjuste,:PRO_CostoInversionEnFactorAjuste,:EGRCostoInversionEnFactorAjuste,:PRO_EGRCostoInversionEnFactorAjuste
,:PRO_CostoTotalRescatado,:MontoTotalRescatado, :IdAgrupador,:FechaCertificacion);