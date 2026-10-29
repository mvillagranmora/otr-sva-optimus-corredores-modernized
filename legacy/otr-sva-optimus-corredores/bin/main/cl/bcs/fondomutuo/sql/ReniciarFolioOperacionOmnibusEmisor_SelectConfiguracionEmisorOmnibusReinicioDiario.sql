SELECT
"IdEmisor"
,"FolioOperacion"
FROM "BSCL.FondoMutuo.BD::FMM.ConfiguracionEmisorOmnibus"
WHERE "ReiniciaFolioDiariamente" = 'S';