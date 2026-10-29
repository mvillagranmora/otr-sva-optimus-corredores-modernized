SELECT WIS."IdEntidad"
	 , WIS."IdTipoEntidad"
	 , WIS."IdTipoIndicador"
	 , WIS."CodigoTipoIndicador"
	 , I."ValorIndicador"
	 , I."IdIndicador"
	 , TI."IdEstado"
	 , TI."IdTipoPersona"
	 , TI."ClaseDato"
FROM "BSCL.SocioNegocio.BD::SOC.ConsultarFichaSocioNegocio_IndicadorEntidad" WIS
	 LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" I
			   ON (
					   I."IdTipoIndicador" = WIS."IdTipoIndicador"
					   AND I."IdTipoEntidad" = WIS."IdTipoEntidad"
					   AND I."IdEntidad" = WIS."IdEntidad"
				   )
	 LEFT JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" TI ON TI."IdTipoIndicador" = WIS."IdTipoIndicador"
WHERE WIS."IdTrabajo" = :IdTrabajo
  AND WIS."IdTipoEntidad" = :IdTipoEntidad
  AND TI."IdTipoIndicador" IS NOT NULL;