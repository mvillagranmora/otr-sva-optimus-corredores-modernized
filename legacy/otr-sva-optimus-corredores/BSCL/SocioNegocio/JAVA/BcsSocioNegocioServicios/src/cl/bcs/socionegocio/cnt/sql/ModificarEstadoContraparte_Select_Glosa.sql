SELECT
	"DocumentoIdentidad" || ' / ' || "NombreCompleto" AS "Glosa"
FROM "BSCL.SocioNegocio.BD.Vistas::CNT.ConsultarContraparte"
WHERE "IdContraparte" = :IdContraparte
