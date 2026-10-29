UPDATE "BSCL.Plataforma.BD::FRM.CampoFormulario"
SET "CantidadMinima" = :CantidadMinima
,"Requerido" = :Requerido
,"RegeneraContratos" = :RegeneraContratos
,"ValorDefecto" = :ValorDefecto
WHERE "CodigoFormulario" = :CodigoFormulario AND "CodigoCampo" = :CodigoCampo AND "IdTipoCanal" = :IdTipoCanal;