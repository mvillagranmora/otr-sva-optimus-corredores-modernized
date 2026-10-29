SELECT TO_BIGINT(:IdTrabajo)    AS "IdTrabajo"
	 , TO_BIGINT(:IdEmpresa)    AS "IdEmpresa"
	 , 'PORTAFOLIO'             AS "IdTipoEntidad"
	 , POR."IdPortafolio"       AS "IdEntidad"
	 , TI."IdTipoIndicador"     AS "IdTipoIndicador"
	 , TI."CodigoTipoIndicador" AS "CodigoTipoIndicador"
FROM "BSCL.Portafolio.BD::POR.Portafolio" POR
	 INNER JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" TI ON (TI."IdTipoEntidad" = 'PORTAFOLIO' AND TI."IdEstado" <> 'INA' AND TI."IdEmpresa" = :IdEmpresa)
WHERE POR."IdPortafolio" IN (:IdsEntidades);
