SELECT
IND."IdPersona"
,COALESCE(IND."ValorIndicador",'') AS "ValorIndicador"
,COALESCE(IND."CodigoTipoIndicador",'') AS "CodigoTipoIndicador"
,COALESCE(IND."GrupoIndicador",'') AS "GrupoIndicador"
FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPersona" IND					
WHERE  (IND."IdPersona" IN (:p_IdPersonas)  )
		AND (:p_IdEmpresa = IND."IdEmpresa")
Order by "IdTipoIndicador"	;			
	