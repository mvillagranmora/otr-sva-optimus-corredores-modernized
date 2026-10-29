INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp"  )
SELECT cast("IdOrden" AS varchar(200)),'Orden_Orden', 'A', 0,  CURRENT_TIMESTAMP 
FROM "_SYS_BIC"."BSCL.Orden.BD.Analytics/Asignacion";


SELECT "Table","Action", min("Stamp"),count(1)
FROM BSCL."BSCL.Plataforma.BD::PLA.Replica.WorkStack"
WHERE
"Stamp" >= '2020-09-20' AND
"Status" = 0-- AND "Action" = 'D'
GROUP BY "Table","Action"

SELECT *
FROM BSCL."BSCL.Plataforma.BD::PLA.Replica.WorkStack"
WHERE
"Stamp" >= '2020-08-27' AND
"Status" = 0-- AND "Action" = 'D'
AND "Table" = 'Orden_Asignacion'

SELECT * FROM  bscl."BSCL.Replica.BD.Vistas::ReplicaOrden_Asignacion" WHERE IdAsignacion = 76980

SELECT "IdTransaccion",*  
FROM "BSCL.Orden.BD::ORD.Asignacion" A
--INNER JOIN "BSCL.Orden.BD::ORD.Transaccion" T ON T."IdTransaccion" = A."IdTransaccion"
--INNER JOIN "BSCL.Orden.BD::ORD.Orden" O ON O."IdOrden" = A."IdOrden"
--INNER JOIN "BSCL.Orden.BD::ORD.LibroOrdenes" LO ON LO."IdLibroOrdenes" = O."IdLibroOrdenes" AND LO."IdEmpresa" = T."IdEmpresa"
--LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" I ON I."IdInstrumento" = T."IdInstrumentoSubyacente"
--INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AC ON AC."IdCliente" = O."IdCliente"
--INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" P ON P."IdPortafolio" = O."IdPortafolioCliente"
--LEFT JOIN "BSCL.Plataforma.BD::INS.CorredoraCodigos" CC ON CC."IdCorredora" = T."IdCorredorContraparte" AND CC."IdBolsa" = T."IdBolsa"
CROSS JOIN (select top 1 "Valor" from "BSCL"."BSCL.Plataforma.BD::PLA.Configuracion" WHERE "CodigoParametro" = 'PLA.SegundosZonaHoraria') AS CONFIGURACION
WHERE "IdAsignacion" = 76980

SELECT  * FROM _SYS_BIC."BSCL.Orden.BD.Analytics/Asignacion" WHERE "IdAsignacion" = 76980


SELECT * FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescateAnalitica"
WHERE "IdSolicitudAporteRescate" = 11349985


DELETE FROM BSCL."BSCL.Plataforma.BD::PLA.Replica.WorkStack"
WHERE "Table" IN ('CuentaInversion_SaldoFechaEfectiva','CuentaInversion_SaldoFechaMovimiento')
