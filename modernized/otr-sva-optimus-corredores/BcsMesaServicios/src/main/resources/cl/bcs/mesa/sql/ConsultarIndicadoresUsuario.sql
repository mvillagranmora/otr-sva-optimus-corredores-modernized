WITH
	IndicadorUsuario AS(
		SELECT
			 INDEMP."ValorIndicador"
			,INDEMP."IdEntidad" AS "IdEmpleado"
			,COALESCE(USU."IdUsuario",0) AS "IdUsuario"
			,COALESCE(USU."CodigoUsuario",'') AS "CodigoUsuario"
			,ROW_NUMBER() OVER (PARTITION BY INDEMP."ValorIndicador" ORDER BY USU."IdUsuario" DESC) AS "POS2"
		FROM (
			SELECT
				 COALESCE(IND."ValorIndicador",'') AS "ValorIndicador"
				,COALESCE(IND."IdEntidad",0) AS "IdEntidad"
				,ROW_NUMBER() OVER (PARTITION BY IND."ValorIndicador" ORDER BY IND."IdEntidad" DESC) AS "POS1"
			FROM "BSCL.Plataforma.BD::IND.TipoIndicador" TIP
			LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" IND
				ON (TIP."IdTipoIndicador" = IND."IdTipoIndicador" AND
					IND."ValorIndicador" IN (:ValorIndicador))
			LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" EMP
				ON (IND."IdEntidad" = EMP."IdEmpleado" AND
					EMP."IdEmpresa" = :IdEmpresa AND
					EMP."Borrado" = 'N')
			WHERE TIP."IdTipoEntidad" = 'EMPLEADO'
			  AND TIP."GrupoIndicador" = 'GENERAL'
			  AND TIP."CodigoTipoIndicador" = 'CODIGO_DATATEC'
			  AND TIP."IdEmpresa" = :IdEmpresa
			  AND TIP."IdEstado" != 'INA'
		) INDEMP
		LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" USU
			ON (INDEMP."IdEntidad" = USU."IdEntidad" AND
				USU."IdEmpresa" = :IdEmpresa AND
				USU."IdEstado" = 'ACT')
		WHERE INDEMP."POS1" = 1
	)
SELECT
	 IND."ValorIndicador"
	,IND."IdEmpleado"
	,IND."IdUsuario"
	,IND."CodigoUsuario"
FROM IndicadorUsuario IND
WHERE IND."POS2" = 1;
