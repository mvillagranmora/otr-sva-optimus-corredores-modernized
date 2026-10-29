SELECT "Codigo" FROM "BSCL.Plataforma.BD::TAS.TipoTasa" 
WHERE "IdEstado" = 'ACT'
AND "IdEmpresa" = :IdEmpresa
AND "IdTipoTasa" = :TipoRegistro
AND "IdTipoTasa" <> 'CURVA';