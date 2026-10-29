SELECT PRT."FolioSistemaExterno"
FROM "BSCL.Custodia.BD::CUS.Cartera" PRT 
WHERE PRT."IdEstado" = 'ANU'
	AND PRT."IdTipoInstrumento" = 'PRT'
	AND PRT."IdEmpresa" = :p_IdEmpresa
	AND PRT."INGFechaOperacion" = :p_FechaOperacion;