SELECT  SOL."IdPortafolio",CAST (COALESCE(MIN(SOL."FechaValorCuota"),CURRENT_DATE) AS VARCHAR) AS "FechaValorCuota"
FROM  "BSCL.Normativa.BD::SIS.RegistroMandatos" MAN 
INNER JOIN "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" SOL ON SOL."IdSolicitudAporteRescate"=MAN."IdSolicitudAporteRescate" AND SOL."IdEmpresa"=MAN."IdEmpresa"
WHERE MAN."IdEstado"='ING' and SOL."IdEmpresa"=:IdEmpresa
GROUP BY SOL."IdPortafolio";