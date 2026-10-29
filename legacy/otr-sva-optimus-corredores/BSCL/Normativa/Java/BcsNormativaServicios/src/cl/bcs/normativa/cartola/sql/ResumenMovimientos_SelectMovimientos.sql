SELECT
	CA."IdCliente"
   ,CA."IdPortafolio"
   ,CA."IdCargoAbonoMovimiento"
   ,CA."IdConcepto"
   ,C."NombreConcepto"
   ,C."Origen"
   ,CA."IdModulo"
   ,CA."IdTipoOperacion"
   ,T."Nombre" AS "NombreTipoOperacion"
   ,CA."IdMoneda"
   ,CASE WHEN CA."IdTipoMovimiento" = 'MOVCAR' THEN CA."Monto" ELSE TO_DECIMAL(0,26,8) END "Cargo"
   ,CASE WHEN CA."IdTipoMovimiento" = 'MOVABN' THEN CA."Monto" ELSE TO_DECIMAL(0,26,8) END "Abono"
   ,"SaldoPorFechaMovimiento"
   ,CA."IdEmpresa"
FROM "BSCL.CuentaInversion.BD.Vistas::ConsultarTablaCargoAbonoMovimiento" AS CA
INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" P ON CA."IdPortafolio" = P."IdPortafolio"
LEFT JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarConceptosCuentaInversion" AS C ON (C."IdConcepto" = CA."IdConcepto")
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS T ON (T."IdTipoOperacion" = CA."IdTipoOperacion")
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = CA."IdPortafolio" AND Portafolio."IdCliente" = CA."IdCliente"
LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
WHERE
CA."IdEmpresa" = :IdEmpresa
AND CA."FechaSistema" BETWEEN :FechaDesde AND :FechaHasta
AND CA."IdEstado" = 'CRT'
AND (:IdProductoAgrupador IS NULL OR MAP(MatrizAgrupador."IdProductoAgrupador",NULL,0,MatrizAgrupador."IdProductoAgrupador") = :IdProductoAgrupador)
AND CA."IdConcepto" NOT IN ('CAMIGR','ABMIGR','ABMIGI','CAMIGI')