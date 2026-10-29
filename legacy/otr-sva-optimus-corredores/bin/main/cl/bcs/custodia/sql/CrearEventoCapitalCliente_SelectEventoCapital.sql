SELECT EC."IdEventoCapital",EC."NumeroDividendo",EC."Secuencia",EC."Nemo",EC."IdEstado",EC."ISFUT",EC."TasaEfectiva",
TEC."IdTipoOperacion"
FROM "BSCL.Custodia.BD::CUS.EventoCapital" AS EC
INNER JOIN "BSCL.Custodia.BD::CUS.TipoEventoCapital" AS TEC ON (TEC."IdTipoEventoCapital" = EC."IdTipoEventoCapital")
WHERE EC."NumeroDividendo" IN (:p_NumeroDividendo) AND EC."IdEmpresa"=:p_IdEmpresa;