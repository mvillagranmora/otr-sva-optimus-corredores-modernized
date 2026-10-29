SELECT 
	"IdIngresoEgreso"
	,"FechaLiquidacion"
	,"NombreConcepto"     
	,"NombreMedioDePago"     
	,"NombreTipoMovimiento"
	,"IdMoneda"
	,"Monto"
	,"IdEstado"
FROM "BSCL.Tesoreria.BD.Vistas::ConsultarIngresoEgreso"
WHERE "FechaLiquidacion" = :FechaLiquidacion
