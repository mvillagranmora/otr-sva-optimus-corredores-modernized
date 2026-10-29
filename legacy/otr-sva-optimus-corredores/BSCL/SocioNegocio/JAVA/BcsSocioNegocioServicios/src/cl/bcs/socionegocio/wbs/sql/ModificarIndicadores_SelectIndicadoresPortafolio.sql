SELECT 
IND."IdIndicador"
,IND."IdTipoEntidad"
,IND."IdTipoIndicador"
,IND."IdEmpresa"
,IND."ValorIndicador"
,IND."CodigoTipoIndicador"
,CLI."IdCliente"
,IND."IdPersona"
,IND."ModeloCliente" 
,PER."Tipo" AS "TipoPersona"
,CLI."TipoEnrolamiento"
,IND."IdPortafolio"
FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio"  IND
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona"  PER ON (PER."IdPersona" = IND."IdPersona")
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON CLI."IdPersona" = PER."IdPersona"
WHERE IND."IdPortafolio"  in (:IdPortafolio)
AND IND."IdUso" IN (:IdUso)
AND IND."IdEmpresa" IN (:IdEmpresa)