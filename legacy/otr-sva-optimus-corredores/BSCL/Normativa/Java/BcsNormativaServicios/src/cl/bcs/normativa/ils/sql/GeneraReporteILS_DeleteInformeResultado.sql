DELETE FROM "BSCL.Normativa.BD::ILS.InformeResultado" 
WHERE "IdTipoInforme" IN (:IdTipoInforme) AND "FechaContable" = :FechaInforme;
