--Entrega los folios de cartera e id , utilizando el movimiento de transito

SELECT CAR."FolioCartera",CAR."IdCartera" 
FROM "BSCL.Custodia.BD::CUS.Cartera" AS CAR
WHERE CAR."IdMovimientoCarteraEnTransito" IN (:IdMovimientoCarteraEnTransito)
AND CAR."IdEstado" ='VIG'