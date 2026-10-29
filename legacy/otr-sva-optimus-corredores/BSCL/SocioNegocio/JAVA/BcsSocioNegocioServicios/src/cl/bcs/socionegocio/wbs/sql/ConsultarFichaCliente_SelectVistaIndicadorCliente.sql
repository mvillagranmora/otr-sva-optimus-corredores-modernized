SELECT 
IND."IdCliente"
,COALESCE(IND."ValorIndicador",'') AS "ValorIndicador"
,COALESCE(IND."CodigoTipoIndicador",'') AS "CodigoTipoIndicador"
,COALESCE(IND."GrupoIndicador",'') AS "GrupoIndicador"
FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" IND					
WHERE  (IND."IdCliente" IN(:p_IdClientes))
AND (:p_IdEmpresa = IND."IdEmpresa")
Order by "IdTipoIndicador";		