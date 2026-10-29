SELECT 
"CodigoTabla",
"CampoFiltro",
"DiasEnLinea",
"Desfase",
"SoloLimpia"
FROM "BSCL.Custodia.BD::CUS.ConfiguracionDataHistorica" 
WHERE "IdEmpresa" = :p_IdEmpresa
AND   "LimpiaTabla" = 'S'
ORDER BY "Orden" ASC;