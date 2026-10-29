INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp"  )
SELECT cast(cast("IdPersona" AS varchar(200)) || ':' || cast("IdTipoIndicador" AS varchar(200)) AS varchar(200)),'SocioNegocio_IndicadorPersona', 'A', 0,  CURRENT_TIMESTAMP  
FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPersonaAnalitica"
WHERE "IdTipoIndicador" in(1027,1028,20000,20001,1,3,5,7,478,480,481,482,484,486,487,1029,20034)


SELECT "IdTipoIndicador",COUNT(1) 
FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPersonaAnalitica"
GROUP BY "IdTipoIndicador"


SELECT "Table","Action", count(1)
FROM BSCL."BSCL.Plataforma.BD::PLA.Replica.WorkStack"
WHERE
"Stamp" >= '2021-07-11 18:00:00' AND
"Status" = 0-- AND "Action" = 'D'
GROUP BY "Table","Action"

SELECT "Table","Action", count(1)
FROM BSCL."BSCL.Plataforma.BD::PLA.Replica.WorkStack"
WHERE
"Stamp" >= '2021-07-11 18:00:00' AND
"Action" = 'M' and
"Status" = 2-- AND "Action" = 'D'
GROUP BY "Table","Action"

DELETE FROM BSCL."BSCL.Orden.BD::ORD.ControlMercado" WHERE "FechaIngresoRegistro" <= '2021-07-01'

DELETE FROM BSCL."BSCL.Plataforma.BD::PLA.Replica.WorkStack"
WHERE
"Stamp" >= '2020-08-20' AND
"Status" = 0 AND "Action" = 'I'
AND "Table" = 'Custodia_CarteraRVHis'

commit

