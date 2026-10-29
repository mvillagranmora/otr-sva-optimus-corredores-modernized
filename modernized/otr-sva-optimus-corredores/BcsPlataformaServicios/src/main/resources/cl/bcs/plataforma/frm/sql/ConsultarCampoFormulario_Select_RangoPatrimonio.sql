SELECT
	TO_NVARCHAR("IdRangoPatrimonio") AS "IdRangoPatrimonio"
	,"DescripcionRango"
FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPatrimonio"
WHERE TO_NVARCHAR("IdRangoPatrimonio") in (:IdsRangoPatrimonio);