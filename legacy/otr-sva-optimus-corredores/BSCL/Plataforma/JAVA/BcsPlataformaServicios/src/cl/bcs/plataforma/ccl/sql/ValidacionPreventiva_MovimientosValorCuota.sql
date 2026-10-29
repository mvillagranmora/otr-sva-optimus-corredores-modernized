select count(1) as "Valor5"
from "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" C
left join "BSCL.FondoMutuo.BD::FMM.ValorCuotaHistorico" V ON C."IdSerieFondo" = V."IdSerieFondo" and C."FechaValorCuota" = V."FechaCierre" and V."EsUltimaCargaDiaria" = 'S'
WHERE c."Estado" = 'CRT' and V."ValorCuota" is null and "FechaCertificacion" >= '2020-09-01'
and c."IdTipoMovimientoFondoMutuo" not in ('APODIV','APODIVCFI','INGGTA','RETTIT','INGTIT');