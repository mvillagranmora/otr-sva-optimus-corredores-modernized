SELECT
	"TipoDocumentoIdentidad" AS "TipoDocumentoIdentidad",
	"DocumentoIdentidad" AS "DocumentoIdentidad"
FROM "BSCL.SocioNegocio.BD.Vistas::CNT.ConsultarContraparte"
WHERE "IdContraparte" = :IdContraparte
