SELECT "IdInstrumento"
	,"Nemotecnico"
	,"IdClasificacionTipoInstrumento"
	,"IdTipoInstrumento"
	,"CodigoClasificacionTipoInstrumento"
FROM "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" 
WHERE "Nemotecnico" IN ( :p_Nemo ) ;
