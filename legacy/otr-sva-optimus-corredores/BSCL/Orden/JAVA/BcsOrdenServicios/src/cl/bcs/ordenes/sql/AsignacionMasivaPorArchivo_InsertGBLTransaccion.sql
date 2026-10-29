
INSERT INTO "BSCL.Orden.BD::GBL.AsignacionMasivaPorArchivo_Entrada"
(
	"FolioTransaccion",
	"FechaTransaccion",
	"Nemotecnico",		
	"RutCliente",
	"CodigoPortafolio",	
	"CantidadAsignar"		
)
VALUES 
(
	:FolioTransaccion,
	:FechaTransaccion,
	:Nemotecnico,
	:RutCliente,
	:CodigoPortafolio,
	:CantidadAsignar	
);

