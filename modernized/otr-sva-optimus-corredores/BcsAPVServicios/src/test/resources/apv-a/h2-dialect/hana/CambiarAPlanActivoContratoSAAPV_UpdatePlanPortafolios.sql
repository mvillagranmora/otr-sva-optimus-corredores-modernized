UPDATE POR 
SET POR."IdMatrizAgrupadorAPV" =   CASE WHEN POR."IdMatrizAgrupadorAPV" = 1 THEN 7
                                    WHEN POR."IdMatrizAgrupadorAPV" = 2 THEN 8
                                    WHEN POR."IdMatrizAgrupadorAPV" = 3 THEN 9
                                    WHEN POR."IdMatrizAgrupadorAPV" = 19 THEN 20
                                    WHEN POR."IdMatrizAgrupadorAPV" = 4 THEN 10
                                    WHEN POR."IdMatrizAgrupadorAPV" = 5 THEN 11
                                    WHEN POR."IdMatrizAgrupadorAPV" = 6 THEN 12
                                    ELSE 999 END                                   
FROM "BSCL.APV.BD::GBL.PortafoliosCambioPlanActivo" AS GBLPOR
INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR
ON GBLPOR."IdPortafolio" = POR."IdPortafolio"
WHERE POR."IdMatrizAgrupadorAPV" IN (1,2,3,19,4,5,6);