SELECT count(1) as "Cantidad"
  FROM "BSCL.Plataforma.BD::PAU.ControlProceso"  
 WHERE "FechaSistema" = (SELECT "Valor"
						   FROM "BSCL.Plataforma.BD::PLA.Configuracion"
						  WHERE "IdEmpresa" = :IdEmpresa
							AND "CodigoParametro" = 'PLA.FechaSistema')
   AND COALESCE("ResultadoProceso",'OK') != 'OK'
   AND exists (SELECT 'x'
				 FROM "BSCL.Plataforma.BD::PLA.Configuracion"
				WHERE "CodigoParametro" = 'PAU.PermiteEntregaSistemaConError'
				  AND "IdEmpresa" = :IdEmpresa
				  AND "Valor" = 'N')