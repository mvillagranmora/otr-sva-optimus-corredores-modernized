UPDATE Mov
SET "IdEstado" = 'ANU',
	"IdUsuarioAnulacion" = :IdUsuarioAnulacion,
	"FechaHoraAnulacion" = CURRENT_TIMESTAMP
FROM "BSCL.Tesoreria.BD::TES.IngresoEgreso" As Mov
WHERE Mov."IdIngresoEgreso" = :IdIngresoEgreso;