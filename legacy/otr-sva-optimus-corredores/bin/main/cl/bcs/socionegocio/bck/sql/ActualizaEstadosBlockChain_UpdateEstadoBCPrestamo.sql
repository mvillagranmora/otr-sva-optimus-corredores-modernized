UPDATE "BSCL.Custodia.BD::CUS.Cartera"
SET    "EstadoAnticipo" =:Estado, "EstadoLiquidacion" =:EstadoLiquidado
WHERE  "FolioCartera"=:FolioPrestamo; 