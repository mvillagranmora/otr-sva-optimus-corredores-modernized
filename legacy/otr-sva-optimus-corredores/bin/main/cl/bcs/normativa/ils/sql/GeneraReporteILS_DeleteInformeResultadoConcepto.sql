DELETE FROM "BSCL.Normativa.BD::ILS.InformeResultadoConcepto" 
WHERE "IdTipoInforme" IN (:IdTipoInforme) AND "FechaContable" = :FechaContable;
