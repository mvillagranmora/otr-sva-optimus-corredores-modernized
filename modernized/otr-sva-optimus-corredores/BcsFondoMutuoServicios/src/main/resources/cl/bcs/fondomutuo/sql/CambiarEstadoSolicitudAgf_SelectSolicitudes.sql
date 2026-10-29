SELECT 
"IdInterfazAgf"
,"IdSolicitudAporteRescateRelacionado"
,"IdEstado"
FROM "BSCL.FondoMutuo.BD::FMM.InterfazAgf"
WHERE "IdInterfazAgf" IN (:IdInterfazAgf) AND "IdEmpresa" = :IdEmpresa