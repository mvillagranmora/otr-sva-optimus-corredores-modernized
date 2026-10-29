SELECT
IND."ValorIndicador"
,IND."CodigoTipoIndicador"
FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPersona" IND					
WHERE  (IND."IdPersona"=:p_IdEntidad  )
		AND (:p_IdEmpresa = IND."IdEmpresa")
Order by "IdTipoIndicador"	;			
	