INSERT INTO "BSCL.Custodia.BD::CAR.CargaMasiva"
	(
		"IdCargaMasiva",
		"IdEmpresa",
		"IdUsuario",
		"CantidadRegistros",
		"NombreArchivo",
		"FechaCarga",
		"CodigoTipoCargaMasiva"
	)		
	VALUES 
	(
		:IdCargaMasiva
        ,:IdEmpresa
        ,:IdUsuario
        ,:CantidadRegistros
        ,:NombreArchivo
        ,CURRENT_UTCTIMESTAMP
        ,:CodigoTipoCargaMasiva

	);