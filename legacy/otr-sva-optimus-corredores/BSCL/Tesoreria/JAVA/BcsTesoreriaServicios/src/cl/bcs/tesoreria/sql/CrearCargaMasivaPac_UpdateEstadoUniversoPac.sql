UPDATE "BSCL.Tesoreria.BD::CAR.CargaMasivaDetallePAC" 
SET    "Estado" = 'INA'     
WHERE  "TipoArchivo" = :tipo 
and "Estado" = 'ACT'; 
