SELECT ConfRetencion."PorcentajeRetencion"/100 AS "PorcentajeRetencion"
,ConfRetencion."MontoTopeRetencion"
,ConfRetencion."CodigoRetiro"
,ConfRetencion."IdPersonaRetencion"
,ConfRetencion."IdRegimenTributario"
FROM "BSCL.APV.BD::TRA.ConfiguracionRetencionTraspaso" ConfRetencion 
WHERE "IdEstado" = 'VIG'