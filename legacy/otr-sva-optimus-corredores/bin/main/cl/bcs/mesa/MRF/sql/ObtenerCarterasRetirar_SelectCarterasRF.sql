WITH CARTERAS as (
SELECT
CARTERA."IdCartera"
,CARTERA."FolioCartera"
,CARTERA."Nemotecnico" AS "Nemo"
,CARTERA."IdPortafolio"
,CARTERA."IdBoveda"
,CARTERA."INGCantidad" - COALESCE(RESERVA_TOTAL."INGCantidad",0)  AS "Saldo"
FROM "BSCL.Custodia.BD::CUS.Cartera" CARTERA
LEFT JOIN (
    SELECT RESERVA."FolioCarteraOrigen"
        ,SUM(RESERVA."IngCantidad") AS "INGCantidad"
    FROM "BSCL.Custodia.BD::CUS.Reserva" AS RESERVA 
    WHERE RESERVA."IdEstado"='VIG' AND RESERVA."EgrCantidad" = 0
    GROUP BY RESERVA."FolioCarteraOrigen"
    ) AS RESERVA_TOTAL ON RESERVA_TOTAL."FolioCarteraOrigen" = CARTERA."FolioCartera" 
WHERE CARTERA."IdEmpresa" = :IdEmpresa
AND CARTERA."Nemotecnico" IN (:Nemotecnico)
AND CARTERA."IdPortafolio" IN (:IdPortafolio)
AND CARTERA."IdEstado" = 'VIG'
)
SELECT 
"IdCartera"
,"FolioCartera"
,"Nemo"
,"IdPortafolio"
,"IdBoveda"
,"Saldo"
FROM CARTERAS
WHERE "Saldo" > 0
ORDER BY "FolioCartera" ASC