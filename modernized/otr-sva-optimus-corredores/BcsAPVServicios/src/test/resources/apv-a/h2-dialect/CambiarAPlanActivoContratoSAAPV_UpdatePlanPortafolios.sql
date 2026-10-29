-- HANA-SOURCE: apv-a/h2-dialect/hana/CambiarAPlanActivoContratoSAAPV_UpdatePlanPortafolios.sql
-- H2 equivalent of the HANA "UPDATE POR SET ... FROM GBL INNER JOIN POR": every portfolio listed in the global table whose matrix is one of
-- 1,2,3,19,4,5,6 moves to its active-plan matrix (7,8,9,20,10,11,12). A portfolio listed more than once is updated once (the join would
-- repeat the same assignment). Same parameters (none), same effect.
UPDATE "BSCL.Portafolio.BD::POR.Portafolio" AS POR
SET "IdMatrizAgrupadorAPV" = CASE WHEN POR."IdMatrizAgrupadorAPV" = 1 THEN 7
                                  WHEN POR."IdMatrizAgrupadorAPV" = 2 THEN 8
                                  WHEN POR."IdMatrizAgrupadorAPV" = 3 THEN 9
                                  WHEN POR."IdMatrizAgrupadorAPV" = 19 THEN 20
                                  WHEN POR."IdMatrizAgrupadorAPV" = 4 THEN 10
                                  WHEN POR."IdMatrizAgrupadorAPV" = 5 THEN 11
                                  WHEN POR."IdMatrizAgrupadorAPV" = 6 THEN 12
                                  ELSE 999 END
WHERE POR."IdMatrizAgrupadorAPV" IN (1,2,3,19,4,5,6)
  AND EXISTS (SELECT 1 FROM "BSCL.APV.BD::GBL.PortafoliosCambioPlanActivo" GBLPOR WHERE GBLPOR."IdPortafolio" = POR."IdPortafolio");
