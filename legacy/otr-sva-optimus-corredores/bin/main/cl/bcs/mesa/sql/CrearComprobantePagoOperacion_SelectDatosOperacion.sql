SELECT 
	"IdOperacion"
   ,"IdTipoOperacion"
FROM "BSCL.Mesa.BD::MES.Operacion"
WHERE "IdOperacion" IN(:IdOperacion);