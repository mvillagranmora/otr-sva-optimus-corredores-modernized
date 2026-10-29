SELECT "TipoMensaje","IdMensaje1","IdMensaje2","SEC_ORDEN","NEMO","CANTIDAD","ESTADO"
FROM "BSCL.Orden.BD::ORD.ControlMercado" cm 
LEFT JOIN "<NOMBRE_TABLA>" dma on cm."IdMensaje1" = CAST(dma."SECUENCIA" AS NVARCHAR(40)) AND cm."IdMensaje2" = (dma."FECHA" || dma."IN_SGC")
where SEC_ORDEN = :SEC_ORDEN and FECHA = :FECHA and ABS(SECUENCIA) < ABS(:SECUENCIA) and "Procesado" != 'S' and "Procesado" != 'E'
	
