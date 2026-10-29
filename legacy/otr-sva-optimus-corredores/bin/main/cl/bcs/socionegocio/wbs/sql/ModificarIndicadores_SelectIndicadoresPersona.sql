SELECT 
IND."IdIndicador"
,IND."IdTipoEntidad"
,IND."IdTipoIndicador"
,IND."IdEmpresa"
,IND."ValorIndicador"
,IND."CodigoTipoIndicador"
,CLI."IdCliente"
,IND."IdPersona"
,PER."Tipo" AS "TipoPersona"
,CLI."TipoEnrolamiento"
FROM 
"BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPersona" IND
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona"  PER ON (PER."IdPersona" = IND."IdPersona")
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON CLI."IdPersona" = PER."IdPersona"
WHERE  IND."IdPersona" in (:IdPersonas)
AND IND."IdUso" IN (:IdUso)
AND IND."IdEmpresa" IN (:IdEmpresa);