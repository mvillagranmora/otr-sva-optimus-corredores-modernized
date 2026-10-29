SELECT "IdPortafolio","IdMedioDePago","IdTipoOperacion","IdPreferenciaPagoPortafolio"
FROM "BSCL.Portafolio.BD.Vistas::ConsultarPreferenciaPagoPortafolio"
WHERE "IdPortafolio" IN (:IdPortafolio) AND "IdMedioDePago" IN (:IdMedioDePago) AND "IdTipoOperacion" IN ('COMACN','VENACN')