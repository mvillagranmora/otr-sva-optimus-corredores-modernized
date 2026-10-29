SELECT 
   	REP."IdReporteAPV",
   	REP."DocumentoIdentidad",
   	REP."NombreCliente",
	HOM_CRT."ValorDestino" AS "CodigoRegimenTributario",
	REP."NombreRegimenTributario",
    HOM_TP."ValorDestino" AS "TipoProducto",
	REP."IdPlanTipoInversion",
	HOM_PLAN."ValorDestino" AS "NombrePlanTipoInversion",
    REP."FechaCierre",
    REP."SaldoAcumulado",
	REP."SaldoRegimenTributarioA",
	REP."SaldoRegimenTributarioB",
	REP."SaldoBonificacionEstatal",
	REP."SaldoAPVCEmpleador",
	REP."SaldoAPVCTrabajadorA",
	REP."SaldoAPVCTrabajadorB",
	REP."SaldoAPVCBonificacionEstatal"
FROM "BSCL.Normativa.BD::NOR.ReporteAPV" REP
LEFT JOIN "BSCL.Normativa.BD::NOR.Homologacion" HOM_CRT ON HOM_CRT."ValorOrigen" = REP."CodigoRegimenTributario" AND HOM_CRT."TipoValor" = 'CodigoRegimenTributario'AND HOM_CRT."Entidad" = 'REPORTE-APV'
LEFT JOIN "BSCL.Normativa.BD::NOR.Homologacion" HOM_TP ON HOM_TP."ValorOrigen" = REP."TipoProducto" AND HOM_TP."TipoValor" = 'TipoProducto' AND HOM_TP."Entidad" = 'REPORTE-APV'
LEFT JOIN "BSCL.Normativa.BD::NOR.Homologacion" HOM_PLAN ON HOM_PLAN."ValorOrigen" = REP."NombrePlanTipoInversion" AND HOM_PLAN."TipoValor" = 'NombrePlanTipoInversion' AND HOM_PLAN."Entidad" = 'REPORTE-APV'
WHERE REP."FechaCierre" = :FechaCierre
ORDER BY REP."IdReporteAPV";