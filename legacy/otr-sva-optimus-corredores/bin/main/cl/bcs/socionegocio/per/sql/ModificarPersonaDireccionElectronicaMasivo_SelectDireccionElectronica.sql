SELECT
CASE PERSONA."Tipo" 
			WHEN 'N' THEN PERSONA."Nombres" ||' '|| PERSONA."ApellidoPaterno" || ' ' || PERSONA."ApellidoMaterno"			
			ELSE PERSONA."RazonSocial"
		END  as "NombreCompleto"
,DOCUMENTO_IDENTIDAD."DocumentoIdentidad"
,PERSONA."IdPersona"
,DIRECCION_ELECTRONICA."IdDireccionElectronica"
,DIRECCION_ELECTRONICA."Direccion"
,DIRECCION_ELECTRONICA."CodigoProposito"
,DIRECCION_ELECTRONICA."Comentario"
,DIRECCION_ELECTRONICA."IdEstado"
,DIRECCION_ELECTRONICA."Predeterminado"
FROM "BSCL.SocioNegocio.BD::PER.DireccionElectronica" AS DIRECCION_ELECTRONICA
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PERSONA ON PERSONA."IdPersona" = DIRECCION_ELECTRONICA."IdPersona"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOCUMENTO_IDENTIDAD ON PERSONA."IdDocumentoIdentidad" = DOCUMENTO_IDENTIDAD."IdDocumentoIdentidad"
WHERE DIRECCION_ELECTRONICA."IdDireccionElectronica" IN (:IdDireccionElectronica);