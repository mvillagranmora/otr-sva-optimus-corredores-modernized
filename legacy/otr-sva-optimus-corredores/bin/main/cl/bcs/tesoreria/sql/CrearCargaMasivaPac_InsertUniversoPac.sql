insert into "BSCL.Tesoreria.BD::CAR.CargaMasivaDetallePAC" 
	(
		"IdCargaMasivaDetallePac",		
		"IdCargaMasivaPac",
		"CodBanco",
		"CodEmpresa",
		"CodConvenio",
		"TipoRegistro",
		"IdentServicio",
		"FechaGeneracion",
		"FechaInicioServ",
		"Estado",
		"TipoArchivo" 
	)		
	values 
	(
		"BSCL.Tesoreria.BD.Secuencias::IdCargaMasivaDetallePac".NEXTVAL --:IdCargaMasivaDetalle
		,:IdCargaMasivaPac
		,:CodigoBanco
		,:CodigoEmpresa
		,:CodigoConvenio
		,:TipoRegistro
		,:IdentServicio
		,:FechaGeneracion
		,:FechaInicioServ
		,'ACT'
		,'UNI'
	);