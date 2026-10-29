SELECT "IdTipoIndicador","CodigoTipoIndicador"
FROM "BSCL.Plataforma.BD::IND.TipoIndicador" 
WHERE "IdTipoEntidad" = :IdTipoEntidad
AND "GrupoIndicador" = :GrupoIndicador;