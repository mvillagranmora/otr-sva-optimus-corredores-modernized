SELECT
	TPO."IdTipoEntidad",
	TPO."IdTipoIndicador",
	'' AS "ModeloCliente",
	TPO."IdEmpresa",
	'' as "ValorIndicador",
	CASE WHEN "RequiereAutorizacion" = 'N' THEN 'ACT' ELSE 'PEN' END AS "IdEstadoAutorizacion"
FROM "BSCL.Plataforma.BD::IND.TipoIndicador" TPO
INNER JOIN "BSCL.Plataforma.BD::DOC.TipoDocumento" DOC ON TPO."CodigoTipoIndicador" = DOC."CodigoTipoIndicador" AND DOC."IdTipoDocumento" = :p_IdTipoDocumento
WHERE TPO."IdTipoEntidad" = 'CLIENTE'
AND TPO."IdUso" = 'REQ'
AND TPO."IdEstado" = 'ACT'
AND TPO."IdEmpresa" = :p_IdEmpresa;