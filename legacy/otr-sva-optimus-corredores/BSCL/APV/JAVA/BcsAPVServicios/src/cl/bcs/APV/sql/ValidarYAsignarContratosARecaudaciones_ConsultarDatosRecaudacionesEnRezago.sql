SELECT 
RECAU."IdRecaudacionCliente",
RECAU."FolioRecaudacion",
RECAU."RutCliente",
TO_BIGINT(RECAU."RecMonto") AS "MontoRecaudacion",
RECAU."TipoRegimen",
RECAU."Regimen",
RECAU."RutEmpleador"
FROM "BSCL.APV.BD.Vistas::REC.ConsultarRecaudadoDistribuido" AS RECAU
WHERE RECAU."CodigoEstado" = 'REZ' AND RECAU."FechaDeposito" BETWEEN :p_FechaDesde AND :p_FechaHasta;