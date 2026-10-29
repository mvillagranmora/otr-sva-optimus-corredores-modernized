SELECT 
 DOC."IdEmpresa" 
,DOC."IdCliente" 
,PER."IdPersona" 
,PER."Nombres" 
,PER."ApellidoPaterno" 
,CLI."IdTipoCliente" 
,TPER."Descripcion" AS "TipoPersona"
,DOC."IdTipoDocumento"
,TPO."NombreDocumento" 
,TPO."CodigoArchivo" AS "CodPlantilla"
,TPO."RequiereFirmaCliente" 
,ANX."IdAnexo" 
,ANX."IdEstado" AS "EstadoAnexo"
FROM "BSCL.Plataforma.BD::DOC.TipoDocumento" TPO
INNER JOIN "BSCL.Plataforma.BD::DOC.Documento" DOC ON TPO."IdTipoDocumento" = DOC."IdTipoDocumento"
INNER JOIN "BSCL.Plataforma.BD::DOC.Anexo" ANX ON ANX."IdDocumento" = DOC."IdDocumento" AND ANX."IdAnexo" = DOC."IdUltimoAnexo" 
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON CLI."IdCliente" = DOC."IdCliente"
INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON PER."IdPersona" = CLI."IdPersona"
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarTipoPersona" AS TPER ON TPER."IdTipoPersona" = PER."Tipo"
WHERE CLI."IdEstado" = 'ACT'
AND PER."IdEstado" = 'ACT'
AND TPO."RequiereFirmaCliente" =:p_RequiereFirmaCliente
---AND DOC."IdTipoDocumento"=:p_IdTipoDocumento
---AND TPER."Descripcion" IN (:p_TipoPersona) ;