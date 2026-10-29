SELECT  
"ValorCuota"
,"IdInstrumento"
FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarValorCuota"
WHERE "IdInstrumento" IN (:IdInstrumento) AND "IdEmpresa" = :IdEmpresa