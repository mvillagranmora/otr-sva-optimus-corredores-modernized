--

SELECT 
	MovComp."IdMovimientoComprobante" 						AS "IdMovimientoComprobante", 
	Spot."IdOperacionSpot" 									AS "IdOperacionSpot", 
	COALESCE(FacSpot."IdEstadoFacturacion",Spot."Estado") 	AS "Estado"
FROM 
			"BSCL.Facturacion.BD::FAC.MovimientoComprobante" 	AS MovComp
INNER JOIN 	"BSCL.Orden.BD::ORD.Asignacion" 					AS Asig 	ON (MovComp."IdAsignacion" = Asig."IdAsignacion")	
INNER JOIN 	"BSCL.Mesa.BD::SPT.OperacionSpot"  					AS Spot     ON (Asig."IdOperacionSpot" = Spot."IdOperacionSpot")    
LEFT  JOIN 	"BSCL.Facturacion.BD::FAC.MovimientoComprobante" 	AS FacSpot	ON ( Spot."IdComprobanteFacturacion" = FacSpot."IdMovimientoComprobante")  
WHERE 
	MovComp."IdMovimientoComprobante" IN (:idsMovimientoComprobante)

