SELECT
CASE PERSONA."Tipo" 
			WHEN 'N' THEN PERSONA."Nombres" ||' '|| PERSONA."ApellidoPaterno" || ' ' || PERSONA."ApellidoMaterno"			
			ELSE PERSONA."RazonSocial"
		END  as "NombreCompleto"
,DOCUMENTO_IDENTIDAD."DocumentoIdentidad"
,PERSONA."IdPersona"
,TELEFONO."IdTelefono"
,TELEFONO."IdPais"
,TELEFONO."IdTipoTelefono"
,TELEFONO."Numero"
,TELEFONO."Anexo"
,TELEFONO."CodigoProposito"
,TELEFONO."Comentario"
,TELEFONO."IdEstado"
FROM "BSCL.SocioNegocio.BD::PER.Telefono" AS TELEFONO
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PERSONA ON PERSONA."IdPersona" = TELEFONO."IdPersona"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOCUMENTO_IDENTIDAD ON PERSONA."IdDocumentoIdentidad" = DOCUMENTO_IDENTIDAD."IdDocumentoIdentidad"
WHERE TELEFONO."IdTelefono" IN (:IdTelefono);