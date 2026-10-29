SELECT 
CTB_itzPreComprobante."IdPreComprobante" AS "JdtNum",
REPLACE(CTB_itzPreComprobante."FechaContable", '-', '') AS "ReferenceDate",
REPLACE(REPLACE(REPLACE(CTB_itzContabilidadTipo."Nombre", ' - ' ,'-'),'CART ', ''), 'PACTEADA', 'PACT')  || '-' || TRIM(CAST("IdPreComprobante" AS NVARCHAR(20))) AS "Memo",
REPLACE(CTB_itzPreComprobante."FechaContable", '-', '') AS "DueDate",
REPLACE(CTB_itzPreComprobante."FechaContable", '-', '') AS "TaxDate"
FROM "BSCL.Contabilidad.BD::ITZ.PreComprobante" CTB_itzPreComprobante
LEFT OUTER JOIN "BSCL.Contabilidad.BD::ITZ.ContabilidadTipo" AS CTB_itzContabilidadTipo ON 
CTB_itzContabilidadTipo."IdTipoContabilidad" = CTB_itzPreComprobante."IdTipoContabilidad" 
WHERE "IdPreComprobante" IN (:IdPrecomprobante)