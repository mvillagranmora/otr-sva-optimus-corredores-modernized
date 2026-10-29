SELECT DISTINCT 
"IdPortafolio",
"IdCliente"
FROM "BSCL.Custodia.BD::CUS.Cartera"
WHERE "IdPortafolio" = :IdPortafolio
AND "IdClasificacionTipoInstrumento" = :IdClasificacionTipoInstrumento
AND (:IdTipoInstrumento IS NULL OR "IdTipoInstrumento" = :IdTipoInstrumento)
AND (
        :Nemotecnico IS NULL
     OR ( :IdClasificacionTipoInstrumento = 'IF' AND "NemoSVS"      = :Nemotecnico )
     OR ( :IdClasificacionTipoInstrumento <> 'IF' AND "Nemotecnico" = :Nemotecnico )
    );