SELECT "IdPortafolio", "OperaLineaSobregiro" 
FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarIndicadoresPortafolioFondoMutuo"
WHERE "IdPortafolio" IN (:IdPortafolio)