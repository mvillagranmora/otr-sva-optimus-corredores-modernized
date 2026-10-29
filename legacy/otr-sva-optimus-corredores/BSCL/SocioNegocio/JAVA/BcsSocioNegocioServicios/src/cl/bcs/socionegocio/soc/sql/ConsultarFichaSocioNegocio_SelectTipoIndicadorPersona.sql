SELECT TO_BIGINT(:IdTrabajo)    AS "IdTrabajo"
	 , TO_BIGINT(:IdEmpresa)    AS "IdEmpresa"
	 , 'PERSONA'                AS "IdTipoEntidad"
	 , PER."IdPersona"          AS "IdEntidad"
	 , TI."IdTipoIndicador"     AS "IdTipoIndicador"
	 , TI."CodigoTipoIndicador" AS "CodigoTipoIndicador"
FROM "BSCL.SocioNegocio.BD::PER.Persona" PER
	 INNER JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" TI ON (TI."IdTipoEntidad" = 'PERSONA' AND TI."IdEstado" <> 'INA' AND TI."IdEmpresa" = :IdEmpresa)
WHERE PER."IdPersona" IN (:IdsEntidades);