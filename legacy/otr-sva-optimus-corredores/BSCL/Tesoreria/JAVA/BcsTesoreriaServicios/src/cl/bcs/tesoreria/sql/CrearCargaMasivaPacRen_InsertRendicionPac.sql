insert into "BSCL.Tesoreria.BD::CAR.CargaMasivaDetallePAC"   
	( 
		"IdCargaMasivaDetallePac",
		"IdCargaMasivaPac",
		"CodBanco",
		"CodEmpresa",
		"CodConvenio",
		"TipoRegistro",
		"IdentServicio",
		"InfAdicional",
		"MontoCargo",
		"FechaVencimiento",
		"FechaCargo",
		"EstadoCargo",
		"CodigoRechazo",
		"Estado",
		"TipoArchivo"
	)		
	values 
	(
		"BSCL.Tesoreria.BD.Secuencias::IdCargaMasivaDetallePac".NEXTVAL  --:IdCargaMasivaDetalle
		,:IdCargaMasivaPac
		,:CodigoBanco
		,:CodigoEmpresa 
		,:CodigoConvenio
		,:TipoRegistro
		,:IdentServicio
		,:InfAdicional
		,:MontoCargo
		,:FechaVencimiento
		,:FechaCargo
		,:EstadoCargo
		,:CodigoRechazo	
		,'ACT'
		,'REN'
		
	);