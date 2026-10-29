SELECT	Cmbte."FolioComprobante" AS "FolioComprobante"
		,CTOP."NombreEvento" AS "TipoOperacion"
		,CMEP."Agrupacion" AS "Agrupacion"
		,CEP."IndAccion" AS "SumaResta"
		,TO_VARCHAR(Cmbte."FechaComprobante", 'YYYY-MM-DD')AS "FechaComprobante"
		,TO_VARCHAR(Cmbte."FechaLiquidacion", 'YYYY-MM-DD') AS "FechaLiquidacion"
		,Cmbte."DocumentoIdentidad"
		,MvtoCmbte."Monto" AS "MontoLiquidacionFinal"
FROM "BSCL.Facturacion.BD::FAC.Comprobante" AS Cmbte INNER JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" MvtoCmbte ON MvtoCmbte."IdComprobante" = Cmbte."IdComprobante" 
													 INNER JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" As CondicionDeLiquidacion ON CondicionDeLiquidacion."IdCondicionDeLiquidacion" = MvtoCmbte."IdCondicionLiquidacion" 
													 INNER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS EstadoPla ON EstadoPla."IdEstado" = MvtoCmbte."IdEstadoFacturacion" 
													 INNER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS EstadoPlaCmbte ON EstadoPlaCmbte."IdEstado" = Cmbte."IdEstadoComprobante" 
													 INNER JOIN "BSCL.Plataforma.BD::PLA.Concepto" AS ConceptoPla ON ConceptoPla."IdConcepto" = MvtoCmbte."IdConceptoMovimiento"
													 INNER JOIN "BSCL.Mesa.BD.Vistas::ConsultarTipoOperacionPosicion" CTOP ON Cmbte."IdTipoOperacion" = CTOP."IdTipoOperacion"
              										 INNER JOIN "BSCL.Mesa.BD.Vistas::ConsultarMetodoEventoPosicion" CMEP ON ctop."NombreEvento" = CMEP."NombreEvento"
              										 INNER JOIN "BSCL.Mesa.BD.Vistas::ConsultarEventoPosicion" CEP ON ctop."NombreEvento" = CEP."NombreOperacion"
WHERE 	TO_VARCHAR(Cmbte."FechaLiquidacion", 'YYYY-MM-DD') = TO_VARCHAR(:p_FechaPosicion, 'YYYY-MM-DD') AND
		Cmbte."IdTipoProducto" = :p_IdTipoProducto AND
		Cmbte."IdTipoOperacion" = :p_IdTipoOperacion AND		
		Cmbte."IdEstadoComprobante" NOT IN ('ANU') AND
		Cmbte."IdCondicionLiquidacion" <> 'T0' AND
		Cmbte."IdPortafolio" = :p_IdPortafolio AND
		CTOP."IdTipoEstado" = :p_IdTipoEstado AND
		Cmbte."IdTipoProducto"!='INSI'	
		
GROUP BY Cmbte."FolioComprobante",CTOP."NombreEvento", CMEP."Agrupacion",CEP."IndAccion", Cmbte."FechaComprobante", Cmbte."FechaLiquidacion", Cmbte."DocumentoIdentidad", MvtoCmbte."Monto"
UNION ALL 
SELECT sim."EGRFolioComprobante"  AS "FolioComprobante" 
		,CTOP."NombreEvento" AS "TipoOperacion"
		,CMEP."Agrupacion" AS "Agrupacion"
		,CEP."IndAccion" AS "SumaResta"
		,TO_VARCHAR(sim."EGRFechaComprobante" , 'YYYY-MM-DD') AS "FechaComprobante"
		,TO_VARCHAR(sim."EGRFechaLiquidacion" , 'YYYY-MM-DD') AS "FechaLiquidacion"		
		,sim."DocumentoIdentidad" AS "DocumentoIdentidad"
		,sim."MontoLiquidacion" AS "MontoLiquidacionFinal"
FROM "BSCL.Custodia.BD.Vistas::SIM.ConsultarCarteraSimultanea" sim
INNER JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" As CondicionDeLiquidacion ON CondicionDeLiquidacion."IdCondicionDeLiquidacion" = sim."IdCondicionLiquidacion"
INNER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS EstadoPla ON EstadoPla."IdEstado" = sim."IdEstado" 
INNER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS EstadoPlaCmbte ON EstadoPlaCmbte."IdEstado" = sim."IdEstado"  
INNER JOIN "BSCL.Plataforma.BD::PLA.Concepto" AS ConceptoPla ON ConceptoPla."IdConcepto" = sim."EGRIdConcepto" 
INNER JOIN "BSCL.Mesa.BD.Vistas::ConsultarTipoOperacionPosicion" CTOP ON sim."IdTipoOperacion" = CTOP."IdTipoOperacion"
INNER JOIN "BSCL.Mesa.BD.Vistas::ConsultarMetodoEventoPosicion" CMEP ON ctop."NombreEvento" = CMEP."NombreEvento"
INNER JOIN "BSCL.Mesa.BD.Vistas::ConsultarEventoPosicion" CEP ON ctop."NombreEvento" = CEP."NombreOperacion"
WHERE TO_VARCHAR("EGRFechaLiquidacion", 'YYYY-MM-DD') = TO_VARCHAR(:p_FechaPosicion, 'YYYY-MM-DD') AND
sim."IdTipoProducto" = :p_IdTipoProducto AND
sim."EGRIdTipoOperacion" = :p_IdTipoOperacion AND
sim."IdEstado"  NOT IN ('ANU') AND
sim."IdPortafolio" = :p_IdPortafolio
