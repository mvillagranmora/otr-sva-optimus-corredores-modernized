
SELECT "BSCL.Orden.BD.Secuencias::MarketData_IdInterno".NEXTVAL as "IdInterno",cm."TipoMensaje",
cm."IdMensaje1",cm."IdMensaje2"

FROM "BSCL.Orden.BD::ORD.ControlMercado" cm 
WHERE cm."Procesado" = 'N' and cm."TipoMensaje" IN (:tiposMensajeBolsaElectronica);
