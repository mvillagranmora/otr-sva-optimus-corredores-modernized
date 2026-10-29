SELECT 
IND."ValorIndicador"
,IND."CodigoTipoIndicador"
FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" IND					
WHERE  (IND."IdCliente"=:p_IdEntidad)
AND (:p_IdEmpresa = IND."IdEmpresa")
Order by "IdTipoIndicador";		