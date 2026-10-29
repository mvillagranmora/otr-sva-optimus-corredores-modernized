UPDATE "BSCL.Tesoreria.BD::PAC.LogMandatoPAC" 
SET "FechaProceso" = CURRENT_TIMESTAMP, 
"FechaSistema" = :FechaSistema, 
"Usuario" = :Usuario, 
"EstadoProcesamiento" = :EstadoProcesamiento, 
"PreRendicion" = :PreRendicion, 
"Glosa" = :Glosa
WHERE "IdMandatoPAC" = :IdMandatoPAC;