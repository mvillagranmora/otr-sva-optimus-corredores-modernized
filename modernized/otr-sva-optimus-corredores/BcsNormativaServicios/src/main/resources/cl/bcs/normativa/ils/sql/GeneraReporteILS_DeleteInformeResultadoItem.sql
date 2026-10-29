DELETE FROM "BSCL.Normativa.BD::ILS.InformeResultadoItem" 
WHERE "IdTipoInforme" IN (:IdTipoInforme) AND "FechaContable" = :FechaContable;