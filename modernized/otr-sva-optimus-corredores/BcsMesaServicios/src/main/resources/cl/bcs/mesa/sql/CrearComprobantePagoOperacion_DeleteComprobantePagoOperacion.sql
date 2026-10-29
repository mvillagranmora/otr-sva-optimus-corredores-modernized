DELETE FROM "BSCL.Mesa.BD::MES.ComprobantePagoOperacion" comp 
WHERE comp."IdOperacion" in(:IdOperacion);    