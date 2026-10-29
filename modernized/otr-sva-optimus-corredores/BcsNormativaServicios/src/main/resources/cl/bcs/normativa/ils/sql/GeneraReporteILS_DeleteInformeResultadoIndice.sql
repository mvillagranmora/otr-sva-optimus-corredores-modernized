DELETE FROM "BSCL.Normativa.BD::ILS.InformeResultadoIndice" 
WHERE "IdTipoInforme" IN (:IdTipoInforme) AND "FechaContable" = :FechaContable;