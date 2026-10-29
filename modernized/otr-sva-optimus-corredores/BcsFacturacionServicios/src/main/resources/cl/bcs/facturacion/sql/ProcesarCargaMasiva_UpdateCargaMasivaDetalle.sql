UPDATE CMD
SET 
	CMD."IdEstado" = 'PRO',   
	CMD."IdMovimientoComprobante" = COALESCE(:IdMovimientoComprobante,0)  -- Id del movimiento generado
FROM "BSCL.Facturacion.BD::CAR.CargaMasivaDetalle" AS CMD
WHERE CMD."IdCargaMasivaDetalle" = (:NumeroMvtoCmbte);