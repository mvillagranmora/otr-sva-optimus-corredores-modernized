SELECT PRT."FolioSistemaExterno"
FROM "BSCL.Custodia.BD::CUS.Cartera" EMP
INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" PRT ON PRT."IdTipoInstrumento" = 'PRT' 
													AND PRT."IdEmpresa" = EMP."IdEmpresa" 
													AND PRT."IdEstado" NOT IN ('MOD', 'ANT') 
													AND PRT."FolioCartera" = EMP."FolioCarteraRelacionada"
WHERE EMP."IdEstado" = 'ANU'
	AND EMP."IdTipoInstrumento" = 'EMP'
	AND EMP."IdEmpresa" = :p_IdEmpresa
	AND EMP."INGFechaOperacion" <= :p_FechaOperacion AND EMP."FechaVencimientoOperacion" > :p_FechaOperacion;