SELECT 

CG."IdGarantia",
CG."IdClasificacionTipoInstrumento",
CG."IdMonedaInstrumento" ,"MontoGarantiaMonedaInstrumento", 
CG."INGCantidad" ,
CG."INGValorRescate",
CG."INGTasaMercadoActual",
CG."INGPrecioMercadoActual",
CG."MontoGarantia",
CG."CorteMinimo",
CG."IdCliente",
CG."IdPortafolio",
CG."INGFechaLiquidacion",
CG."IdContraparteGarantia" ,
CG."IdInstrumento" ,
CG."IdOrigenGarantia",
CG."Nemotecnico",
CG."IdConcepto" ,
CG."EsFungible",
CG."ConCertificadoAutomatico"

FROM #REGISTROSAGRUPADOS R
INNER JOIN "BSCL.Custodia.BD.Vistas::GTA.ConsultarGarantias" CG ON (
((CG."IdCliente" = R."p_Cliente")
AND (CG."IdPortafolio" = R."p_Portafolio")
AND (CG."INGFechaLiquidacion" <= R."p_FechaDeLiquidacion")
AND (CG."IdContraparteGarantia" = R."p_IdContraparteGarantia" )
AND (CG."IdMonedaInstrumento" = R."p_IdMonedaInstrumento")
AND (CG."IdInstrumento" = R."p_IdInstrumento")
AND (CG."IdOrigenGarantia" = R."p_IdOrigenGarantia")
AND (CG."IdConcepto"  = R."p_IdConcepto")
AND (CG."Nemotecnico" = R."p_Nemotecnico")
) ) AND CG."IdEstado" = 'VIG' AND CG."IdOrigenGarantia" IN ('CTA','CUS')