UPDATE MAN SET MAN."IdEstado"='PRO'
FROM  "BSCL.Normativa.BD::SIS.RegistroMandatos" MAN 
INNER JOIN "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" SOL ON SOL."IdSolicitudAporteRescate"=MAN."IdSolicitudAporteRescate" AND SOL."IdEmpresa"=MAN."IdEmpresa"
WHERE MAN."IdEstado"='ING' and SOL."IdEmpresa"=:IdEmpresa
AND  MAN."IdSolicitudAporteRescate" IN (:IdSolicitudAporteRescate);