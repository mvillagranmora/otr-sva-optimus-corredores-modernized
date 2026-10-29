WITH
   Garantias AS
    (
      SELECT
      GAR."IdCliente"
      ,GAR."IdPortafolio"
      ,GAR."IdMonedaInstrumento" AS "IdMoneda"
      ,GAR."INGCantidad" AS "Monto"
      ,:p_FechaHasta AS "FechaSaldo"
      FROM "BSCL.CuentaInversion.BD::GBL.ConsultarSaldoCuentaInversion_Cuenta_Paso" as CI
      INNER JOIN "BSCL.Custodia.BD::GTA.Garantia" as GAR
            ON CI."IdPortafolio" = GAR."IdPortafolio"
            AND CI."IdMoneda" = GAR."IdMonedaInstrumento"
      WHERE
      		"IdTrabajo" = :IdTrabajo
            AND GAR."IdEstado" = 'VIG'
            AND GAR."IdOrigenGarantia"  = 'CTA'
            AND GAR."IdEmpresa" = :p_IdEmpresa
            AND "FechaOperacion" <= :p_FechaHasta
    )
SELECT "IdCliente","IdPortafolio","IdMoneda",CAST(SUM("Monto") AS DECIMAL(26,8)) AS "Monto","FechaSaldo"
FROM Garantias
GROUP BY "IdCliente","IdPortafolio","FechaSaldo","IdMoneda";
