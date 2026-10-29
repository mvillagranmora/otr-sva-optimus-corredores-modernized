	SELECT DISTINCT "IdInstrumento"
    FROM "BSCL.Custodia.BD::CUS.Cartera" 					AS CAR
 	LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio"			AS Portafolio	 	ON Portafolio."IdPortafolio" = CAR."IdPortafolio"
    LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" 	AS MatrizAgrupador 	ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
    WHERE 
        CAR."IdEmpresa" 					= :p_IdEmpresa
    AND CAR."IdEstado" 						= 'VIG'
    AND CAR."IdTipoInstrumento" 			   NOT IN ('PRT','EMP','SIM','PAC','REP','RRV','RRF','FWD','FDO','FIP','FBC','FIC','FUF','IOA','IOE','IOI','IOD','IOB','IOC','IOU','IOM','IOF','FBO','DER')
	
    <<CASE.CAR.p_IdCliente>>
	<<CASE.CAR.p_IdPortafolio>>
	<<CASE.CAR.p_IdClasificacionTipoInstrumento>>
	<<CASE.CAR.p_IdTipoInstrumento>> 
	<<CASE.CAR.p_IdInstrumento>>
	<<CASE.CAR.p_Nemotecnico>>
    <<FECHACONSULTA.CAR.p_TipoFechaConsulta>>
    <<COALESCE.MatrizAgrupador.p_IdProductoAgrupador>>