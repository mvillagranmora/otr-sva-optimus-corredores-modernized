UPDATE "BSCL.Orden.BD::ORD.Transaccion"
SET 
	--"MontoRetencion" = "MontoRetencion" + COALESCE(:MontoRetencion,0),
	"MontoRetencion" =  COALESCE(:MontoRetencion,0),
	"MarcaIGK"       = 'S'	
WHERE "IdTransaccion" = :IdTransaccion;
