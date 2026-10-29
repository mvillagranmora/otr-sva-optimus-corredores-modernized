SELECT
	TO_NVARCHAR("IdSegmentoCliente") AS "IdSegmentoCliente"
	,"DescripcionSegmento"
FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarTipoSegmento"
WHERE TO_NVARCHAR("IdSegmentoCliente") in (:idsSegmentoCliente);