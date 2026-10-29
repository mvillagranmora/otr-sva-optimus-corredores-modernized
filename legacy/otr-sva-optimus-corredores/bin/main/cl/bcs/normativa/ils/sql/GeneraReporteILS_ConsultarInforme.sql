SELECT 	NOR_ilsInformeTipo."IdTipoInforme","IdEmpresa","Nombre","IdIndice","Orden"
FROM "BSCL.Normativa.BD::ILS.InformeTipo" AS NOR_ilsInformeTipo
	INNER JOIN "BSCL.Normativa.BD::ILS.InformeConfiguracion" AS NOR_ilsInformeConfiguracion ON 
			NOR_ilsInformeTipo."IdTipoInforme" 			= NOR_ilsInformeConfiguracion."IdTipoInforme"
WHERE NOR_ilsInformeTipo."IdTipoInforme" IN (:p_IdTipoInforme) AND NOR_ilsInformeTipo."IdEmpresa" = :p_IdEmpresa AND NOR_ilsInformeTipo."Estado"= 'VIG';