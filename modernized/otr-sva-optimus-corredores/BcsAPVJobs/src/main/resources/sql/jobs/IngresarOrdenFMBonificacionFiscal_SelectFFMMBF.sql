SELECT   ApoRes."IdCliente"
,PortaBonif."IdPortafolio"
,"TipoSolicitud"
,"IdSerieFondo"
,"CalculoPorMontoCuota"
,"Monto"
,"NumeroCuotas"
,"IdMoneda"
,"IdMedioDePago"
,"IdPreferenciaPagoPortafolio"
,"Visado"
,"SolicitudBatch"
,"OrigenOperacion"
,ApoRes."IdRegimenTributario"
,"IndicadorMLT"
,"IdSerieFondoDestino"
,"IdEmisorDestino"
,"IdAutorizadoOrigen"
,"Porcentaje"
,"IdMonedaPago"
,"IdMedioDePago"
,"IdPreferenciaPagoPortafolio"
,"IdTipoMovimientoFondoMutuo"
,COALESCE(ApoRes."FolioContrato", TO_BIGINT(0)) AS "FolioContrato"
,COALESCE(ApoRes."TipoContratoOperacion",'') AS "TipoContratoOperacion"
,COALESCE(ApoRes."FolioContratoOperacion",'') AS "FolioContratoOperacion"
,ApoRes."IdSolicitudAporteRescate"
,ApoRes."Glosa"
FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" ApoRes
INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" PortaBonif
ON ApoRes."IdCliente" = PortaBonif."IdCliente"
AND PortaBonif."TienePropositoAPV" = 'S'
AND PortaBonif."IdEstado" = 'ACT'
INNER JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" MatrizBonif
ON PortaBonif."IdMatrizAgrupadorAPV" = MatrizBonif."IdMatrizAgrupadorAPV"
INNER JOIN "BSCL.APV.BD::REC.RegimenTributario" RegimenBonif
ON RegimenBonif."IdRegimenTributario" = MatrizBonif."IdRegimenTributario"
AND RegimenBonif."Codigo" = 'BF'
WHERE "IdSolicitudAporteRescate" in (:IdsAporteRescate)