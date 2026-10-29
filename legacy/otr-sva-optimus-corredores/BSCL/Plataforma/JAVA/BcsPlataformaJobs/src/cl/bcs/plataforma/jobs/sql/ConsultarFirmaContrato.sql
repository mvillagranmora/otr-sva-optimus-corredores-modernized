SELECT 
 DOC."IdEmpresa" 
,DOC."IdCliente" 
,DOC."IdDocumento" 
,PER."IdPersona" 
,PER."Nombres" 
,PER."ApellidoPaterno" 
,CLI."IdTipoCliente" 
,TPER."Descripcion" AS "TipoPersona"
,DOC."IdTipoDocumento"
,TPER."IdTipoPersona" 
,TPO."NombreDocumento" 
,TPO."CodigoArchivo" AS "CodPlantilla"
,TPO."RequiereFirmaCliente" 
,TPO."NumeroFirmantesCliente" 
,TPO."NumeroFirmantesEmpresa" 
,ANX."IdAnexo" 
,ANX."IdEstado" AS "EstadoAnexo"
,DCID."IdTipoDocumentoIdentidad" 
,DCID."DocumentoIdentidad"
FROM "BSCL.Plataforma.BD::DOC.TipoDocumento" TPO
INNER JOIN "BSCL.Plataforma.BD::DOC.Documento" DOC ON TPO."IdTipoDocumento" = DOC."IdTipoDocumento"
INNER JOIN "BSCL.Plataforma.BD::DOC.Anexo" ANX ON ANX."IdDocumento" = DOC."IdDocumento" AND ANX."IdAnexo" = DOC."IdUltimoAnexo" 
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON CLI."IdCliente" = DOC."IdCliente"
INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON PER."IdPersona" = CLI."IdPersona"
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarTipoPersona" AS TPER ON TPER."IdTipoPersona" = PER."Tipo"
INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DCID ON  DCID."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad" 
WHERE CLI."IdEstado" = 'ACT'
AND PER."IdEstado" = 'ACT'
AND DOC."IdTipoDocumento" =:IdTipoDocumento
AND TPER."IdTipoPersona"IN(:IdTipoPersona);
				 