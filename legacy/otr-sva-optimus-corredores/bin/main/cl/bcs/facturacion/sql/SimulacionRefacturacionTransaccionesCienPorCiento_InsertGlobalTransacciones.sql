INSERT INTO "BSCL.Facturacion.BD::GBL.SimulacionRefacturacion_Transaccion" 
(
	"FolioTransaccion" , 
	"Nemotecnico", 
	"FechaSistemaCreacion"
)
VALUES
(
	:FolioTransaccion,
	:Instrumento,
	:FechaSistemaCreacion
);    
	     