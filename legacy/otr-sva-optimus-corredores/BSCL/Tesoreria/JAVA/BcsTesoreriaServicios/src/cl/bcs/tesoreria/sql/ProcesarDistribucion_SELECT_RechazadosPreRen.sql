	SELECT DISTINCT
	 LMP."IdentServicio"
	,LMP."IdMandatoPAC" 
	,LMP."Glosa" 

	FROM "BSCL.Tesoreria.BD::PAC.LogMandatoPAC"				AS LMP
	INNER JOIN "BSCL.Tesoreria.BD::CAR.CargaMasivaDetallePAC" 	AS CMP ON (CMP."IdentServicio" = LMP."IdentServicio")

	WHERE 
		LMP."PreRendicion" != 'S' 
	AND LMP."FechaProceso"  = CURRENT_DATE