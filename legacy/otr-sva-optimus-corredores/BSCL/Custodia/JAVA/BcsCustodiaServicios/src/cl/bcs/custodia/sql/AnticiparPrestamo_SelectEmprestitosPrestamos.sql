
SELECT
	PRO."FolioPrestamo",
	CAR."FolioCartera" AS "FolioEmprestito",
	PRO."FechaAnticipo",
	PRO."IdPais",
	PRO."IdMonedaLiquidacion",
	PRO."IdCondicionLiquidacion",
	PRO."PrecioInstrumento",
    PRO."FechaLiquidacion",
    PRO."DevolverCustodia",
    PRO."IdEmpleadoDigitador"
    
FROM "BSCL.Custodia.BD::GBL.AnticiparPrestamo_Proceso_Prestamos" AS PRO
INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" AS CAR ON (CAR."FolioCarteraRelacionada" = PRO."FolioPrestamo" AND CAR."IdEstado" = 'VIG');
		