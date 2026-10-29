SELECT
	CA."IdCliente"
   ,CA."IdPortafolio"
   ,SEC_MOVIMIENTO AS "IdCargoAbonoMovimiento"
   ,'' AS "IdConcepto"
   ,'' AS "NombreConcepto"
   ,'' AS "Origen"
   ,'FAC' AS "IdModulo"
   ,'' AS "IdTipoOperacion"
   ,'' AS "NombreTipoOperacion"
   ,CA."IdMoneda"
   ,CARGO AS "Cargo"
   ,ABONO AS "Abono"
   ,0 AS "SaldoPorFechaMovimiento"
   ,:IdEmpresa AS "IdEmpresa"
FROM "BSCL.Normativa.BD::CAR.MovimientosCajaSebra" AS CA
INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" P ON CA."IdPortafolio" = P."IdPortafolio"
WHERE
CA."FECHAOPERACION" BETWEEN :FechaDesde AND :FechaHasta
