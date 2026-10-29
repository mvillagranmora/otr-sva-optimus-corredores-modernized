SELECT
   ConceptosManuales."TipoConcepto"
   ,ConceptosManuales."AfectaRentabilidadTES"
   ,CA."IdUsuario"
   ,CA."FechaOperacion"
   ,CA."IdCliente"
   ,CA."IdPortafolio"
   ,CA."IdCargoAbonoMovimiento"
   ,CA."IdConcepto" 
   ,CA."IdMoneda"
   ,CA."IdTipoMovimiento"
   ,CASE WHEN CA."IdTipoMovimiento" = 'MOVCAR' THEN CA."Monto" ELSE TO_DECIMAL(0,26,8) END "Cargo"
   ,CASE WHEN CA."IdTipoMovimiento" = 'MOVABN' THEN CA."Monto" ELSE TO_DECIMAL(0,26,8) END "Abono"
   ,CA."SaldoPorFechaMovimiento"
   ,CA."IdEmpresa"
FROM "BSCL.CuentaInversion.BD.Vistas::ConsultarTablaCargoAbonoMovimiento" AS CA
INNER JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarConceptosCuentaInversion" AS C ON (C."IdConcepto" = CA."IdConcepto")
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS T ON (T."IdTipoOperacion" = CA."IdTipoOperacion")
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = CA."IdPortafolio" AND Portafolio."IdCliente" = CA."IdCliente"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConceptosManuales" AS ConceptosManuales ON ConceptosManuales."IdConcepto" = CA."IdConcepto"
WHERE
ConceptosManuales."TipoConcepto" = 'MANUAL'
AND ConceptosManuales."AfectaRentabilidadTES" = 'S'
AND CA."IdEmpresa" = :IdEmpresa
AND CA."IdUsuario"=:IdUsuario
AND CA."FechaOperacion" BETWEEN :FechaDesde AND :FechaHasta
--AND CA."IdEstado" = :IdEstado
AND CA."IdPortafolio" IN (:IdPortafolio)
AND CA."IdCliente" = :IdCliente
ORDER BY CA."FechaOperacion"; 