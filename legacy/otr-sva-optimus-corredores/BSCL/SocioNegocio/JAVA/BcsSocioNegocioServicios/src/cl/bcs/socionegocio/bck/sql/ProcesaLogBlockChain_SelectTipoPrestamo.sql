SELECT "ISIN","IdInstrumento","Nemotecnico","IdClasificacionTipoInstrumento","IdTipoInstrumento" 
FROM "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" WHERE  "IdTipoInstrumento" = 'PRT'