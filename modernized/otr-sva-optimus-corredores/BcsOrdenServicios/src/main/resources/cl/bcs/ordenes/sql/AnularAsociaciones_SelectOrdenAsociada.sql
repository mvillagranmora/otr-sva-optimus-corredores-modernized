SELECT "IdOrdenAsociada"
    ,"IdOrdenN"
    ,"IdOrdenM"
    ,"FechaIngresoRegistroCorta"
    ,cast("FechaIngresoRegistro" AS DATE)
    ,"ModalidadCalculo"
    ,"Monto"
    ,"Cantidad"
    ,"Precio"
    ,"IdUsuario"
    ,"IdOrdenMOrigen"
    
FROM "BSCL.Orden.BD::ORD.OrdenAsociada" 
WHERE "IdOrdenAsociada" IN (:IdOrdenAsociada)
