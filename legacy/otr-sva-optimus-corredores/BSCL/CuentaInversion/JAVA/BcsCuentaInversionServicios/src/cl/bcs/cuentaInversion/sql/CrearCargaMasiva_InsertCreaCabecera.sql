INSERT INTO "BSCL.CuentaInversion.BD::CAR.CargaMasiva"
	(
			"IdCargaMasiva", 
			"CodigoTipoCargaMasiva", 
			"IdEmpresa", 
			"IdUsuario", 
			"CantidadRegistros", 
			"MontoTotal", 
			"NombreArchivo", 
			"FechaCarga", 
			"FechaProceso", 
			"FechaIngresoRegistro", 
			"IdEstado", 
			"IdMoneda", 
			"IdConcepto"
	)	
			
	values 
	(
		 :IdCargaMasiva		
		,:CodigoTipoCargaMasiva
		,:IdEmpresa
		,:IdUsuario
		,:CantidadRegistros
		,:MontoTotal --Se deje en blanco por que puede haber mas de una moneda
		,:NombreArchivo
		,:FechaCarga
		,:FechaProceso
		,CURRENT_TIMESTAMP
		,'ING'
		,:IdMoneda --Se deje en blanco por que puede haber mas de una moneda
		,:IdConcepto
	);

