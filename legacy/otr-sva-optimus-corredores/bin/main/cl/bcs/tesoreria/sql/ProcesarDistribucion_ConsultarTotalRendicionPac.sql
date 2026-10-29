SELECT SUM("MontoCargo") AS "SumaTotal"
FROM  "BSCL.Tesoreria.BD::CAR.CargaMasivaDetallePAC"
WHERE "Estado" = 'ACT' and "TipoArchivo" = 'REN';   