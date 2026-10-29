UPDATE ORDEN
SET "GeneraSpot"= 'N'
FROM "BSCL.Mesa.BD::SPT.OperacionSpot" SPOT 
LEFT JOIN "BSCL.Orden.BD::ORD.Asignacion" ASIG ON (ASIG."IdOperacionSpot" = SPOT."IdOperacionSpot")
LEFT JOIN "BSCL.Orden.BD::ORD.Orden" ORDEN ON (ORDEN."IdOrden" = ASIG."IdOrden")  
WHERE SPOT."IdOperacionSpot" IN (:IdOperacionSpot);