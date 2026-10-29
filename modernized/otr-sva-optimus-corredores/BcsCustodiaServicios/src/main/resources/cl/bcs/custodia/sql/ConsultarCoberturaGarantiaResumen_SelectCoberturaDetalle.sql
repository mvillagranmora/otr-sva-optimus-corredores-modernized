SELECT 
	 GAR."NombreCliente"
	,GAR."DocumentoIdentidad"
	,GAR."IdCliente"
	,GAR."IdPortafolio"
	,GAR."PropositoPortafolio"
	,GAR."FolioCarteraGarantizada"
	,GAR."IdTipoInstrumentoGarantizado"                 -- Ej.: SIM
	,GAR."NombreTipoInstrumentoGarantizado"
	,GAR."IdInstrumentoCarteraGarantizada"              -- Ej.: 999 (SIMVEN)
	,GAR."IdContraparteGarantia"                        -- Entidad Destino 
	,GAR."RazonSocialContraparte"  						-- Entidad Destino
	,GAR."EsEspecifica"                                 -- Especifica(Reportes/Vta. Corta) -> Cada operación tiene sus propias garantias) -- No específicas(Simultáneas)			
	,COALESCE(GAR."IdOperacionOrigenGarantia",0) AS "IdOperacionOrigenGarantia"	    -- Folio de la operacion(orden) que originó la garantía (solo para específicas)
	,GAR."MontoCoberturaEntidad" 
	,GAR."MontoCoberturaCorredora"
	,GAR."IdBolsaOperacionOrigen" as "IdBolsa"
	,GAR."IdConcepto"
	,COALESCE (ORD."IdMonedaLiquidacionOrden",'')as "IdMonedaLiquidacionOperacion"
FROM "BSCL.Custodia.BD.Vistas::GTA.ConsultarCoberturaGarantiaDetalle" AS GAR
LEFT JOIN "BSCL.Orden.BD::ORD.Orden" AS ORD ON ORD."IdOrden" = GAR."IdOperacionOrigenGarantia" 
WHERE GAR."IdCliente"	 					= (CASE WHEN COALESCE(:p_IdCliente,0) <> 0 THEN :p_IdCliente ELSE GAR."IdCliente" END)
AND	  GAR."IdPortafolio" 					= (CASE WHEN COALESCE(:p_IdPortafolio,0) <> 0 THEN :p_IdPortafolio ELSE GAR."IdPortafolio" END)	
AND   GAR."IdEstado"						= 'VIG'