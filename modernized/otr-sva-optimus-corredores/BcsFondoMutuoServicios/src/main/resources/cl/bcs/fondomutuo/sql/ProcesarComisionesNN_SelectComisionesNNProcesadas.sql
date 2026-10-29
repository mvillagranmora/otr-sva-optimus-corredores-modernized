SELECT CP."IdCartera", CP."FolioCartera", CP."FechaDesde", CP."FechaHasta", CP."FechaProcesamiento", CP."IdEmpresa", CP."Estado", CP."IdSecuenciaRescateNN", CP."IdCliente", CLI."DocumentoIdentidad"
FROM "BSCL.Custodia.BD::CUS.ComisionNNProcesada" AS CP
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLI ON (CLI."IdCliente" = CP."IdCliente")
WHERE CP."IdCartera" IN (:IdCartera) AND CP."FechaDesde" = :FechaDesde AND CP."FechaHasta" = :FechaHasta AND CP."IdEmpresa" = :IdEmpresa AND CP."Estado" = 'PRO';
