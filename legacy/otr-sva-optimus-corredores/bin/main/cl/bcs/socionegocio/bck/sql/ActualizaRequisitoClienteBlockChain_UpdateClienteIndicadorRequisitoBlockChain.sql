UPDATE  "BSCL.Plataforma.BD::IND.Indicador"
SET   "ValorIndicador" = :ValorIndicador
	, "FechaModificacion" = :FechaHoraSistema
WHERE "IdEntidad" = :IdEntidad 
AND "IdTipoIndicador" = :IdTipoIndicador
AND "IdTipoEntidad" = :IdTipoEntidad
AND "IdEmpresa" = :IdEmpresa;