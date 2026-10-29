SELECT 
    CASE WHEN IMP."IdPais" IS NULL OR IMP."IdEstado" <> 'ACT' THEN 'N' ELSE 'S' END AS "AplicaImpuesto",
    CASE WHEN GASTO."IdPais" IS NULL OR GASTO."AplicarGastoTransferencia" <> 'S' THEN 'N' ELSE 'S' END AS "AplicaGastoTransferencia",
    PER."IdPaisResidencia",
    EMI."IdEmisor",
    IMP."PorcentajeImpuestoDefecto"
FROM "BSCL.Plataforma.BD::MER.Emisor" EMI
INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON PER."IdPersona" = EMI."IdPersona"
LEFT JOIN "BSCL.Plataforma.BD::PLA.PaisImpuestoDividendoExtranjero" IMP ON IMP."IdPais" = PER."IdPaisResidencia"
LEFT JOIN "BSCL.Plataforma.BD::PLA.PaisGastoTransferencia" GASTO	ON GASTO."IdPais" = PER."IdPaisResidencia"
WHERE EMI."IdEmisor" IN (:ids);