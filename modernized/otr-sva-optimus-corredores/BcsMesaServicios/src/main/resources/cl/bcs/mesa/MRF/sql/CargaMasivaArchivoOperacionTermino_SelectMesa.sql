SELECT
MESA."IdCliente"
,MESA."IdPortafolio"
FROM "BSCL.Mesa.BD::MES.Mesa" AS MESA
WHERE MESA."IdMesa" = :IdMesa;
