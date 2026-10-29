SELECT 	
DETALLE."IdCargaSolicitudesAporteRescate" AS "IdCarga", 
DETALLE."IdDetalleCargaSolicitudesAporteRescate" AS  "IdDetalleCarga", 
DETALLE."IdSolicitudAporteRescate",
DETALLE."TipoMovimiento",
DETALLE."FechaMovimiento", 
DETALLE."FechaValorCuota",
DETALLE."IdSerieFondo",
DETALLE."NumeroCuotas",
DETALLE."ValorCuota",
DETALLE."Monto",
DETALLE."MontoComision",
DETALLE."IdMoneda",
DETALLE."IdMonedaPago",
DETALLE."IdMedioDePago",
DETALLE."IdSerieFondoDestino",
DETALLE."IdCliente",
DETALLE."IdPortafolio",
DETALLE."IdEmisor",
DETALLE."IndicadorMLT",
DETALLE."CalculoPorMontoCuota",
DETALLE."EsActualizacion",
DETALLE."IdRegimenTributarioOrigen",
DETALLE."IdRegimenTributarioDestino"
FROM "BSCL.FondoMutuo.BD::FMM.DetalleCargaSolicitudesAporteRescate" AS DETALLE 
WHERE DETALLE."IdCargaSolicitudesAporteRescate" IN (:IdCarga) 
AND DETALLE."FechaCarga" = :FechaSistema AND DETALLE."Eliminado" = 'N' AND DETALLE."IdEmpresa" = :IdEmpresa AND DETALLE."Estado" = 'PRO'
AND (DETALLE."EstadoProceso" = 'PEN' OR DETALLE."EstadoProceso" = 'ERR') 