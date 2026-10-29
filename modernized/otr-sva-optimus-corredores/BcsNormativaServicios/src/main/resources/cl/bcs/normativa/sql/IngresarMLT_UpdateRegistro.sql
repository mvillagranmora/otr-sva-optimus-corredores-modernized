UPDATE  "BSCL.Normativa.BD::SIS.RegistroMandatos" 
SET "IdEstado"='INA'
WHERE "IdSolicitudAporteRescate" IN (:IdSolicitudAporteRescate);