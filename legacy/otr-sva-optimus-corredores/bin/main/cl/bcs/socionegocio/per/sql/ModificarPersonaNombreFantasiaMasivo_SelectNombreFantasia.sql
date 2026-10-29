SELECT
CASE PERSONA."Tipo" 
			WHEN 'N' THEN PERSONA."Nombres" ||' '|| PERSONA."ApellidoPaterno" || ' ' || PERSONA."ApellidoMaterno"			
			ELSE PERSONA."RazonSocial"
		END  as "NombreCompleto"
,DOCUMENTO_IDENTIDAD."DocumentoIdentidad"
,NOMBREFANTASIA."IdPersona"
,NOMBREFANTASIA."IdNombreFantasia"
,NOMBREFANTASIA."DescripcionNombreFantasia"
,NOMBREFANTASIA."IdPersona"
,NOMBREFANTASIA."FechaCreacion"
,NOMBREFANTASIA."FechaModificacion"
,NOMBREFANTASIA."FechaIngreso"
,NOMBREFANTASIA."IdEstado"
,NOMBREFANTASIA."IdEstadoAutorizacion"
,NOMBREFANTASIA."Borrado"
,NOMBREFANTASIA."IdUsuarioCreador"

FROM "BSCL.SocioNegocio.BD::PER.NombreFantasia" AS NOMBREFANTASIA
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PERSONA ON PERSONA."IdPersona" = NOMBREFANTASIA."IdPersona"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOCUMENTO_IDENTIDAD ON PERSONA."IdDocumentoIdentidad" = DOCUMENTO_IDENTIDAD."IdDocumentoIdentidad"
WHERE NOMBREFANTASIA."IdNombreFantasia" IN (:IdNombreFantasia);