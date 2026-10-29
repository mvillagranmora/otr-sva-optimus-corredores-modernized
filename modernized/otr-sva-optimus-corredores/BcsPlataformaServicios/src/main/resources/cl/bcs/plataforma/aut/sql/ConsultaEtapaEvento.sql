select 
	"IdEtapaEvento",
	"IdEvento",
	"IdEtapa",
	"Codigo",
	"Nombre",
	"Orden",
	"TipoAutorizacion",
	"MinimoAutorizan",
	"IdUsuarioCambioEstado",
	"IdRolCambioEstado",
	"FechaHoraCambioEstado",
	"IdEstado"
from "BSCL.Plataforma.BD::AUT.EtapaEvento"
WHERE "IdEvento" IN  ( :ListaIdEventos ) 
ORDER BY "IdEvento","Orden" ASC