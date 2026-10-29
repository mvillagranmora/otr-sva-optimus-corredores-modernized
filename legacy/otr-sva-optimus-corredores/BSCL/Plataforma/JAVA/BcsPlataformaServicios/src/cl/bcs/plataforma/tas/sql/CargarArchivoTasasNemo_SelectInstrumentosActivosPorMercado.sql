SELECT INS."Nemotecnico" FROM "BSCL.Plataforma.BD.Vistas::TAS.ConsultarInstrumentoCustodiaParaTasas" INS
WHERE INS."IdClasificacionTipoInstrumento" = :Mercado 
AND INS."Nemotecnico" IN (:Nemo);