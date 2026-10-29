SELECT 
"CodigoTabla",
"CampoFiltro",
"DiasEnLinea",
"Desfase",
"SoloLimpia"
FROM "BSCL.Custodia.BD::CUS.ConfiguracionDataHistorica" 
WHERE "IdEmpresa" = :p_IdEmpresa
AND   "MigraTabla" = 'S'
ORDER BY "Orden" DESC;



