SELECT 	Cmbte."FolioComprobante" AS "FolioComprobante" 

		,CTOP."NombreEvento" AS "TipoOperacion"
		,CMEP."Agrupacion" AS "Agrupacion"
		,CEP."IndAccion" AS "SumaResta"
		,TO_VARCHAR(Cmbte."FechaComprobante", 'YYYY-MM-DD') AS "FechaComprobante"
		,TO_VARCHAR(MvtoCmbte."FechaLiquidacion", 'YYYY-MM-DD') AS "FechaLiquidacion"		
		,MvtoCmbte."DocumentoIdentidad" AS "DocumentoIdentidad"
		,SUM(MvtoCmbte."MontoMovimientoComprobante") AS "MontoLiquidacionFinal"
FROM "BSCL.Facturacion.BD::FAC.Comprobante" AS Cmbte INNER JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" MvtoCmbte ON MvtoCmbte."IdComprobante" = Cmbte."IdComprobante"
													 INNER JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" As CondicionDeLiquidacion ON CondicionDeLiquidacion."IdCondicionDeLiquidacion" = MvtoCmbte."IdCondicionLiquidacion"
													 INNER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS EstadoPla ON EstadoPla."IdEstado" = MvtoCmbte."IdEstadoFacturacion"
													 INNER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS EstadoPlaCmbte ON EstadoPlaCmbte."IdEstado" = Cmbte."IdEstadoComprobante"   
													 INNER JOIN "BSCL.Plataforma.BD::PLA.Concepto" AS ConceptoPla ON ConceptoPla."IdConcepto" = MvtoCmbte."IdConceptoMovimiento"
													 INNER JOIN "BSCL.Mesa.BD.Vistas::ConsultarTipoOperacionPosicion" CTOP ON Cmbte."IdTipoOperacion" = CTOP."IdTipoOperacion"
			  										 INNER JOIN "BSCL.Mesa.BD.Vistas::ConsultarMetodoEventoPosicion" CMEP ON ctop."NombreEvento" = CMEP."NombreEvento"
			  										 INNER JOIN "BSCL.Mesa.BD.Vistas::ConsultarEventoPosicion" CEP ON ctop."NombreEvento" = CEP."NombreOperacion"
WHERE 	TO_VARCHAR(Cmbte."FechaLiquidacion", 'YYYY-MM-DD') = TO_VARCHAR(:p_FechaPosicion, 'YYYY-MM-DD') 
	AND		Cmbte."IdTipoProducto" = :p_IdTipoProducto 
	AND		Cmbte."IdTipoOperacion" = :p_IdTipoOperacion 
	AND		Cmbte."IdEstadoComprobante" NOT IN ('ANU')
	AND 	MvtoCmbte."EsAPlazo" = 'N' 
	AND		Cmbte."IdPortafolio" = :p_IdPortafolio
	AND		CTOP."IdTipoEstado" = :p_IdTipoEstado
	AND 	Cmbte."IdTipoOperacion" NOT IN  ('ANSIMC','ANSIMV')
	AND 	Cmbte."IdTipoProducto" NOT IN ('INIF','INRF')
GROUP BY Cmbte."FolioComprobante", CTOP."NombreEvento", CMEP."Agrupacion",CEP."IndAccion", Cmbte."FechaComprobante", MvtoCmbte."FechaLiquidacion", Cmbte."FechaComprobante", MvtoCmbte."DocumentoIdentidad" 

UNION ALL 
SELECT Cmbte."FolioComprobante" AS "FolioComprobante" 
		,CTOP."NombreEvento" AS "TipoOperacion"
		,CMEP."Agrupacion" AS "Agrupacion"
		,CEP."IndAccion" AS "SumaResta"
		,TO_VARCHAR(Cmbte."FechaComprobante", 'YYYY-MM-DD') AS "FechaComprobante"
		,TO_VARCHAR(MvtoCmbte."FechaLiquidacion", 'YYYY-MM-DD') AS "FechaLiquidacion"		
		,MvtoCmbte."DocumentoIdentidad" AS "DocumentoIdentidad"
		,SUM(sim."MontoLiquidacion") AS "MontoLiquidacionFinal"
FROM "BSCL.Custodia.BD.Vistas::SIM.ConsultarCarteraSimultanea" sim
INNER JOIN  "BSCL.Facturacion.BD::FAC.Comprobante" AS Cmbte ON sim."EGRFolioComprobante"  = Cmbte."IdComprobante"  
INNER JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" MvtoCmbte ON MvtoCmbte."IdComprobante" = Cmbte."IdComprobante"
INNER JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" As CondicionDeLiquidacion ON CondicionDeLiquidacion."IdCondicionDeLiquidacion" = MvtoCmbte."IdCondicionLiquidacion"
INNER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS EstadoPla ON EstadoPla."IdEstado" = MvtoCmbte."IdEstadoFacturacion"
INNER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS EstadoPlaCmbte ON EstadoPlaCmbte."IdEstado" = Cmbte."IdEstadoComprobante"   
INNER JOIN "BSCL.Plataforma.BD::PLA.Concepto" AS ConceptoPla ON ConceptoPla."IdConcepto" = MvtoCmbte."IdConceptoMovimiento"
INNER JOIN "BSCL.Mesa.BD.Vistas::ConsultarTipoOperacionPosicion" CTOP ON Cmbte."IdTipoOperacion" = CTOP."IdTipoOperacion"
INNER JOIN "BSCL.Mesa.BD.Vistas::ConsultarMetodoEventoPosicion" CMEP ON ctop."NombreEvento" = CMEP."NombreEvento"
INNER JOIN "BSCL.Mesa.BD.Vistas::ConsultarEventoPosicion" CEP ON ctop."NombreEvento" = CEP."NombreOperacion"
WHERE TO_VARCHAR(sim."EGRFechaLiquidacion", 'YYYY-MM-DD') = TO_VARCHAR(:p_FechaPosicion, 'YYYY-MM-DD') AND
sim."IdTipoProducto" = :p_IdTipoProducto AND
sim."EGRIdTipoOperacion" = :p_IdTipoOperacion AND
sim."EGRIdTipoOperacion" NOT IN( 'ANCSIM','ANVSIM') AND
sim."IdEstado"  NOT IN ('ANU','VCD','LIQ') AND
sim."IdPortafolio" = :p_IdPortafolio
GROUP BY Cmbte."FolioComprobante", CTOP."NombreEvento", CMEP."Agrupacion",CEP."IndAccion", Cmbte."FechaComprobante", MvtoCmbte."FechaLiquidacion", Cmbte."FechaComprobante", MvtoCmbte."DocumentoIdentidad" 
UNION ALL 
SELECT 	Cmbte."FolioComprobante" AS "FolioComprobante" 

		,CTOP."NombreEvento" AS "TipoOperacion"
		,CMEP."Agrupacion" AS "Agrupacion"
		,CEP."IndAccion" AS "SumaResta"
		,TO_VARCHAR(Cmbte."FechaComprobante", 'YYYY-MM-DD') AS "FechaComprobante"
		,TO_VARCHAR(MvtoCmbte."FechaLiquidacion", 'YYYY-MM-DD') AS "FechaLiquidacion"		
		,MvtoCmbte."DocumentoIdentidad" AS "DocumentoIdentidad"
		,SUM(MvtoCmbte."MontoMovimientoComprobante") AS "MontoLiquidacionFinal"
FROM "BSCL.Facturacion.BD::FAC.Comprobante" AS Cmbte INNER JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" MvtoCmbte ON MvtoCmbte."IdComprobante" = Cmbte."IdComprobante"
													 INNER JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" As CondicionDeLiquidacion ON CondicionDeLiquidacion."IdCondicionDeLiquidacion" = MvtoCmbte."IdCondicionLiquidacion"
													 INNER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS EstadoPla ON EstadoPla."IdEstado" = MvtoCmbte."IdEstadoFacturacion"
													 INNER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS EstadoPlaCmbte ON EstadoPlaCmbte."IdEstado" = Cmbte."IdEstadoComprobante"   
													 INNER JOIN "BSCL.Plataforma.BD::PLA.Concepto" AS ConceptoPla ON ConceptoPla."IdConcepto" = MvtoCmbte."IdConceptoMovimiento"
													 INNER JOIN "BSCL.Mesa.BD.Vistas::ConsultarTipoOperacionPosicion" CTOP ON Cmbte."IdTipoOperacion" = CTOP."IdTipoOperacion"
			  										 INNER JOIN "BSCL.Mesa.BD.Vistas::ConsultarMetodoEventoPosicion" CMEP ON ctop."NombreEvento" = CMEP."NombreEvento"
			  										 INNER JOIN "BSCL.Mesa.BD.Vistas::ConsultarEventoPosicion" CEP ON ctop."NombreEvento" = CEP."NombreOperacion"
WHERE 	TO_VARCHAR(Cmbte."FechaComprobante", 'YYYY-MM-DD') = TO_VARCHAR(:p_FechaPosicion, 'YYYY-MM-DD') AND
		TO_VARCHAR(Cmbte."FechaLiquidacion", 'YYYY-MM-DD') = TO_VARCHAR(:p_FechaPosicion, 'YYYY-MM-DD')  
	AND		Cmbte."IdTipoProducto" = :p_IdTipoProducto 
	AND		Cmbte."IdTipoOperacion" = :p_IdTipoOperacion 
	AND		Cmbte."IdEstadoComprobante" NOT IN ('ANU')
	AND 	MvtoCmbte."EsAPlazo" = 'N' 
	AND		Cmbte."IdPortafolio" = :p_IdPortafolio
	AND		CTOP."IdTipoEstado" = :p_IdTipoEstado
	AND 	Cmbte."IdTipoOperacion" NOT IN  ('ANSIMC','ANSIMV')
	AND Cmbte."IdTipoProducto" IN ('INIF','INRF')
GROUP BY Cmbte."FolioComprobante", CTOP."NombreEvento", CMEP."Agrupacion",CEP."IndAccion", Cmbte."FechaComprobante", MvtoCmbte."FechaLiquidacion", Cmbte."FechaComprobante", MvtoCmbte."DocumentoIdentidad" 
