SELECT   
ApoRes."IdSolicitudAporteRescate"
,ApoRes."IdCliente"
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
,COALESCE("FolioContrato", TO_BIGINT(0)) AS "FolioContrato"
,COALESCE("TipoContratoOperacion",'') AS "TipoContratoOperacion"
,COALESCE("FolioContratoOperacion",'') AS "FolioContratoOperacion"
,ApoRes."IdPortafolio" AS "IdPortafolioOrigen"
,TO_DECIMAL(0, 26, 8) AS "PorcentajeCalculado"
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