SELECT TO_BIGINT(:IdTrabajo)    AS "IdTrabajo"
	 , TO_BIGINT(:IdEmpresa)    AS "IdEmpresa"
	 , 'CLIENTE'                AS "IdTipoEntidad"
	 , CLI."IdCliente"          AS "IdEntidad"
	 , TI."IdTipoIndicador"     AS "IdTipoIndicador"
	 , TI."CodigoTipoIndicador" AS "CodigoTipoIndicador"
FROM "BSCL.SocioNegocio.BD::CLI.Cliente" CLI
	 INNER JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" TI ON (TI."IdTipoEntidad" = 'CLIENTE' AND TI."IdEstado" <> 'INA' AND TI."IdEmpresa" = :IdEmpresa)
WHERE CLI."IdCliente" IN (:IdsEntidades);
