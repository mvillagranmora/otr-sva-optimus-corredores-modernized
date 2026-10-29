UPDATE "BSCL.Custodia.BD::CUS.CuadraturaCircular1518" 
	SET "CantidadDCVCuentaRelacionado" = :CantidadDCVCuentaRelacionado,
		"CantidadDisponibleDCVCuentaRelacionado" = :CantidadDisponibleDCVCuentaRelacionado,
		"CantidadDCVCuentaPropia" = :CantidadDCVCuentaPropia,
		"CantidadDisponibleDCVCuentaPropia" = :CantidadDisponibleDCVCuentaPropia,
		"CantidadDCVCuentaTercero" = :CantidadDCVCuentaTercero,
		"CantidadDisponibleDCVCuentaTercero" = :CantidadDisponibleDCVCuentaTercero, 
		"CantidadSorteoLetras" = :CantidadSorteoLetras 
WHERE "IdCuadraturaCircular1518" = :IdCuadraturaCircular