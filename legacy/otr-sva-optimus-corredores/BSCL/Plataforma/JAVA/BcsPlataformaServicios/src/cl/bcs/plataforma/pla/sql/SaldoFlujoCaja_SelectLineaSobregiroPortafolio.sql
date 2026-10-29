WITH LIM as (
	SELECT POR."IdPortafolio",LIM."IdMoneda",LIM."MontoTotal", :FechaDesde as "FechaDesde",:FechaHasta as "FechaHasta"
	/*FROM "BSCL.Portafolio.BD::POR.Portafolio" POR
    LEFT JOIN "BSCL.Plataforma.BD::LIM.Limite" LIM ON 'EXT_' || POR."IdPortafolio" = LIM."IdPlantillaLimite"
    INNER JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" IND 
     	     ON IND."IdPortafolio" = POR."IdPortafolio" 
     	    AND IND."CodigoTipoIndicador" = 'GEN_SOBCTA'
     	    AND IND."ValorIndicador" = 'S'
    WHERE POR."IdPortafolio" IN  (:IdPortafolio)
    AND :FechaDesde = :FechaDesde
    AND :FechaHasta = :FechaHasta
    AND :IdEmpresa = :IdEmpresa
    AND LIM."IdTipoLimite" = 'LIMPORTSOBREGIRO'
    AND POR."IdEstado" = 'ACT'
    AND LIM."IdMoneda" IN (:IdMoneda)*/
    FROM "BSCL.Plataforma.BD::GBL.SaldoFlujoCaja_Proceso_Paso" SAL
	INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR
		ON (SAL."IdPortafolio" = POR."IdPortafolio")
	LEFT JOIN "BSCL.Plataforma.BD::LIM.Limite" LIM
		ON ('EXT_' || POR."IdPortafolio" = LIM."IdPlantillaLimite" AND
			SAL."IdMoneda" = LIM."IdMoneda")
    INNER JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" IND
    	ON (IND."IdPortafolio" = POR."IdPortafolio" AND
    		IND."CodigoTipoIndicador" = 'GEN_SOBCTA' AND
    		IND."ValorIndicador" = 'S')
    WHERE :FechaDesde = :FechaDesde
    AND :FechaHasta = :FechaHasta
    AND :IdEmpresa = :IdEmpresa
    AND LIM."IdTipoLimite" = 'LIMPORTSOBREGIRO'
    AND LIM."IdEstado" = 'ACT'
    AND POR."IdEstado" = 'ACT'
	)
SELECT "IdPortafolio" || '_' || "IdMoneda" AS "Id",
	   "IdPortafolio",
	   TO_DATE("FechaDesde",'YYYY-MM-DD') AS "FechaLiquidacion",
	   "IdMoneda",
	   SUM("MontoTotal") as "Monto",
	   'LINEA_SOBREGIRO' AS "ItemCalculo"
		,:TipoCalculoFlujoCaja AS "TipoCalculoFlujoCaja"
FROM LIM GROUP BY "IdMoneda","IdPortafolio","IdMoneda","FechaDesde";