SELECT "IdInstrumento","Nemotecnico" 
FROM "BSCL.Plataforma.BD::INS.Instrumento" i
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" t on t."IdTipoInstrumento" = i."IdTipoInstrumento"
where t."IdClasificacionTipoInstrumento" = 'RV';

