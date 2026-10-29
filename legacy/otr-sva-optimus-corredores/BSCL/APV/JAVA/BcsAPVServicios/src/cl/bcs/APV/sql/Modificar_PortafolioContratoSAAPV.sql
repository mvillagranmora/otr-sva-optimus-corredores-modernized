UPDATE "BSCL.APV.BD::SCS.PortafolioContratoSAAPV"
SET 
"IdEstado" = :IdEstado,
"OrigenAhorro" = :OrigenAhorro,
"MontoPesos" = :MontoPesos,
"MontoUF" 	= :MontoUF,
"PorcentajeRegimenImponible" = :PorcentajeRegimenImponible,
"Pago" = :Pago,
"PeriodoInicio" = :PeriodoInicio,
"PeriodoTermino" = :PeriodoTermino,
"IdPortafolio" = :IdPortafolio
WHERE
"IdContratoSAAPV" = :IdContratoSAAPV;
