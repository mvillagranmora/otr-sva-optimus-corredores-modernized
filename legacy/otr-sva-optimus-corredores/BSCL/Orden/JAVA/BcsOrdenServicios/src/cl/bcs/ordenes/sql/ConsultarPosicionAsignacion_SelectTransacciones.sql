select 
	tran."IdInstrumento",tran."IdInstrumentoSubyacente",tran."IdTipoOperacion",tran."EsVentaCorta",tran."IdTipoInstrumento",
	IFNULL(sum(
		CASE tran_tipope."AccionOrden" 
			WHEN 'COMPRA' THEN (CASE WHEN tran."EsFungible" = 'S' THEN tran."ValorRescate" ELSE tran."Cantidad" END) 
			WHEN 'VENTA' THEN 0 ELSE 0 END),0) 
	- IFNULL(sum(
		CASE tran_tipope."AccionOrden" 
			WHEN 'COMPRA' THEN (CASE WHEN tran."EsFungible" = 'S' THEN asi."ValorRescateAsignado" ELSE asi."CantidadAsignada" END) 
			WHEN 'VENTA' THEN 0 ELSE 0 END),0)AS "TransadoCompra",
	
		
	IFNULL(sum(
		CASE tran_tipope."AccionOrden" 
			WHEN 'VENTA' THEN (CASE WHEN tran."EsFungible" = 'S' THEN tran."ValorRescate" ELSE tran."Cantidad" END) 
			WHEN 'COMPRA' THEN 0 ELSE 0 END),0) 			
	- IFNULL(sum(
		CASE tran_tipope."AccionOrden" 
			WHEN 'VENTA' THEN (CASE WHEN tran."EsFungible" = 'S' THEN asi."ValorRescateAsignado" ELSE asi."CantidadAsignada" END) 
			WHEN 'COMPRA' THEN 0 ELSE 0 END),0) AS "TransadoVenta"
	
from "BSCL.Orden.BD::ORD.Transaccion" tran
LEFT JOIN "BSCL.Orden.BD::ORD.Asignacion" asi ON (asi."IdTransaccion" = tran."IdTransaccion")
inner join "BSCL.Plataforma.BD::INS.TipoOperacion" tran_tipope on tran."IdTipoOperacion" = tran_tipope."IdTipoOperacion"
WHERE tran."EsAPlazo" = 'N' AND tran."IdEstado" = 'ING' and tran."FechaSistemaCreacion" = :FechaConsulta
group by tran."IdInstrumento",tran."IdInstrumentoSubyacente",tran."IdTipoOperacion",tran."EsVentaCorta",tran."IdTipoInstrumento"