SELECT  "IdPortafolio",
		IFNULL(SUM("CantidadNueva"),0) AS "CantidadTramite"
FROM "BSCL.Custodia.BD::CUS.EventoCapitalCliente"	
WHERE 
	"IdTipoEventoCapital" 	= 'SU'								AND
	"Secuencia" 			= :Secuencia						AND
	"IdSocioNegocio"		= :IdSocioNegocio					AND
	"IdPortafolio"			= :IdPortafolio						AND
	"IdInstrumentoNuevo"	= :IdInstrumento					AND
--	"IdEstado"				IN ('VIG','CER','CRT','ENV')
	"IdEstado"				IN ('VIG','CRT')
GROUP BY "IdPortafolio";