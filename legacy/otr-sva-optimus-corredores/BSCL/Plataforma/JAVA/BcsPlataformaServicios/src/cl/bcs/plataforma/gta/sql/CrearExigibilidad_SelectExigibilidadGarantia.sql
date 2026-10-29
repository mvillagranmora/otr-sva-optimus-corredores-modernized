SELECT
 EXG."IdTipoInstrumento"
,EXG."IdBolsa"
,EXG."IdTipoInstrumentoSubyacente"
,EXG."IdTipoClasificacionRiesgo"
,INS."Nemotecnico"
,EXG."ExigibilidadEntidad"
,EXG."ExigibilidadCorredora"
,EXG."IdExigibilidadGarantia"
,EXG."IdInstrumento"
,EXG."IdEntidadClasificadoraRiesgo"
,EXG."ClasificacionRiesgo"
from "BSCL.Plataforma.BD::GTA.ExigibilidadGarantia" AS EXG
INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INS ON INS."IdInstrumento" = EXG."IdInstrumento"
WHERE "IdExigibilidadGarantia"IN (:ListaExigibilidad)