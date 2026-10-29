SELECT
	'Estado' AS "TipoFiltro",
	ESDN."IdEstado" AS "ValorFiltro"
FROM "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio" AS ESDN
WHERE ESDN."NombreEntidad" = 'EMPLEADO';