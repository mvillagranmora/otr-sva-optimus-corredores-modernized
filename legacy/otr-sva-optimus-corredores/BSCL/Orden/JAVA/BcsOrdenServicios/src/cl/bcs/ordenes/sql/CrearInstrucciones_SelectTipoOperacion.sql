SELECT  "IdTipoOperacion","Nombre", "Accion","IdTipoProducto" 
FROM "BSCL.Plataforma.BD::INS.TipoOperacion" 
WHERE "IdTipoProducto" IN ('IRRV','IRRF','IRFI')