SELECT sum(LENGTH("IdValorCobro") + "IdDetallePerfilCobros" +LENGTH("DescripcionPerfilCobros") ) FROM "_SYS_BIC"."BSCL.Plataforma.BD.Analytics/PerfilCobro"

SELECT COUNT(1) FROM "_SYS_BIC"."BSCL.Plataforma.BD.Analytics/PerfilProducto"

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp"  )
SELECT cast("IdDetallePerfilProductos" AS varchar(200)),'Plataforma_PerfilProducto', 'I', 0,  CURRENT_TIMESTAMP  
FROM  "_SYS_BIC"."BSCL.Plataforma.BD.Analytics/PerfilCobro";


SELECT "Table","Action", count(1)
FROM BSCL."BSCL.Plataforma.BD::PLA.Replica.WorkStack"
WHERE
"Stamp" >= '2020-08-20' AND
"Status" = 0-- AND "Action" = 'D'
GROUP BY "Table","Action"

SELECT *
FROM BSCL."BSCL.Plataforma.BD::PLA.Replica.WorkStack"
WHERE
"Stamp" >= '2020-08-20' AND
"Status" = 0 --"Action" = 'D'
AND "Table" = 'SocioNegocio_IndicadorCliente'