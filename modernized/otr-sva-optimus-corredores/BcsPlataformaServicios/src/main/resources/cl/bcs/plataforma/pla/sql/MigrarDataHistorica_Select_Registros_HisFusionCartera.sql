SELECT "IdFusion", "FechaFusion", "CantidadFusion", "TasaFusion", "ValorRescateFusion", "IdClasificacionTipoInstrumento", "MontoFusion", "MontoValorizado", "IdCarteraResultante", "IdModulo", "IdEmpresa", "IdUsuario"
FROM "BSCL.Custodia.BD::HIS.FusionCartera"
WHERE CAST("FechaFusion" AS DATE) BETWEEN :p_Desde AND :p_Hasta
ORDER BY "IdFusion", "FechaFusion" DESC
LIMIT :p_Limit OFFSET :p_Offset;
