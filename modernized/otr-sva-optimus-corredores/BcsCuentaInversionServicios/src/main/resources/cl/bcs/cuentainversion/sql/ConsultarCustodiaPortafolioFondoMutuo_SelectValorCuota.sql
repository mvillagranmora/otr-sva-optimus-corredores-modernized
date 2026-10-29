SELECT "IdSerieFondo","ValorCuota",TO_VARCHAR("FechaValorCuota",'YYYY-MM-DD') AS "FechaValorCuota" FROM (
		SELECT "IdSerieFondo","ValorCuota","FechaValorCuota",
			row_number() over (partition by "IdSerieFondo","FechaValorCuota" order by "IdSerieFondo","FechaValorCuota" DESC, "row_numAux" asc) AS "row_num"
		FROM (
			SELECT VC."IdSerieFondo",VC."ValorCuota",VC."FechaCierre" AS "FechaValorCuota", -1 AS "row_numAux"
			FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarValorCuota" VC
			WHERE VC."IdEmpresa" = :IdEmpresa AND VC."IdSerieFondo" IN (:IdSerieFondo) AND 	VC."FechaCierre" = :FechaConsulta
			UNION ALL

			SELECT "IdSerieFondo","ValorCuota","FechaValorCuota","row_numAux"
			FROM
			(
				SELECT
					VH."IdSerieFondo",VH."ValorCuota",VH."FechaVigencia" AS "FechaValorCuota",
					row_number() over (partition by VH."IdSerieFondo",VH."FechaVigencia" order by VH."IdSerieFondo", VH."FechaVigencia", VH."FechaIngresoRegistro"  desc) AS "row_numAux"
				FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarValorCuotaHistorico" AS VH
				WHERE VH."IdEmpresa" = :IdEmpresa AND VH."IdSerieFondo" IN (:IdSerieFondo) AND 	VH."FechaVigencia" = :FechaConsulta
			)
			WHERE "row_numAux" = 1
		) AS tb1
	) AS tb2 WHERE "row_num" = 1
	AND "ValorCuota" > 0
