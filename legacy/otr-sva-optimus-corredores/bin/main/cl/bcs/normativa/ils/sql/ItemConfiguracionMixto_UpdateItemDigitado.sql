UPDATE "BSCL.Normativa.BD::ILS.ItemConfiguracion"
SET "MayorACero" = :MayorACero, "MenorACero" = :MenorACero, "PermiteCero" = :PermiteCero WHERE "IdConfiguracionItem" = :IdConfiguracionItem;