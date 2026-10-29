SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Custodia.BD.Vistas::GTA.ConsultarCoberturaGarantiaDetalle";
GO
CREATE VIEW "BSCL.Custodia.BD.Vistas::GTA.ConsultarCoberturaGarantiaDetalle" AS 

SELECT
	GAR."IdGarantia",
	GAR."FechaIngresoRegistro",
	GAR."FechaLiquidacion",
	GAR."NombreInstrumento",
	GAR."Nemotecnico",
	GAR."INGCantidad",
	CASE
		WHEN GAR."IdOrigenGarantia" = 'CUS' THEN GAR."INGCantidad"
		WHEN GAR."IdOrigenGarantia" = 'CTA' THEN 1				
		WHEN GAR."IdOrigenGarantia" = 'ESP' THEN 1                
	END	AS "CantidadGarantia",   -- Igual que GTA.ConsultarGarantias
	GAR."INGMontoATasaCompraActual",	
	CASE
		WHEN GAR."IdOrigenGarantia" = 'CUS' THEN GAR."INGMonto"   			   -- MontoGarantia = INGMontoATasaGarantiaActual --> Es el Monto valorizado
		WHEN GAR."IdOrigenGarantia" = 'CTA' THEN GAR."INGCantidad"				   -- Para garantias en dinero no se valoriza
		WHEN GAR."IdOrigenGarantia" = 'ESP' THEN GAR."INGMontoATasaCompraActual"   -- Para garantias en especies no se valoriza
	END	AS "MontoValorizado",	-- Igual que GTA.ConsultarGarantias
	GAR."NombreMonedaInstrumento",
	GAR."IdOrigenGarantia",
	GAR."FolioGarantia",
	GAR."FolioCarteraGarantizada",
	GAR."NombreTipoInstrumento",
	GAR."NemoGarantizado",
	GAR."RazonSocialContraparte",
	GAR."NombreConcepto",
	GAR."IdConcepto",
	GAR."NombreEstado",
	GAR."IdPortafolio",
	GAR."IdCliente",
	GAR."NombreCliente",	
	GAR."DocumentoIdentidad",		
	GAR."CodigoPortafolio",
	GAR."PropositoPortafolio",
	GAR."IdMonedaInstrumento",
	GAR."NombreOrigenGarantia",
	CASE  WHEN GAR."IdConcepto" IN ('GARDEV') THEN 'DER'
	ELSE 
		CASE WHEN GAR."IdTipoInstrumentoGarantizado" IS NULL THEN '' ELSE GAR."IdTipoInstrumentoGarantizado" END
	END AS "IdTipoInstrumentoGarantizado",
	CASE  WHEN GAR."IdConcepto" IN ('GARDEV') THEN 'DERIVADOS'
	ELSE 
		CASE WHEN GAR."IdTipoInstrumentoGarantizado" IS NULL THEN '' ELSE INS."Nombre" END
	END AS "NombreTipoInstrumentoGarantizado",
	GAR."IdInstrumentoCarteraGarantizada",
	GAR."IdContraparteGarantia",
	GAR."IdEstado",
	GAR."EsEspecifica",               -- Especifica(Reportes/Vta. Corta) -> Cada operacion tiene sus propias garantias) -- No especificas(Simultaneas)
	GAR."IdOperacionOrigenGarantia",  -- Folio de la operacion(orden) que originó la garantia (solo para especificas)
	GAR."IdBolsaOperacionOrigen",
	GAR."IdClasificacionTipoInstrumento",
	GAR."IdTipoReajuste",
	CASE 
		WHEN GAR."IdOrigenGarantia" IN ('ESP','CTA') THEN  
			(CAST(COALESCE(CON."Valor",'100') AS BIGINT)/100)*COALESCE(GAR."MontoGarantia",0) 		-- Si garantia es de tipo Especies o Efectivo
	 	ELSE 
	 		COALESCE(GAR."INGMonto",0) * (COALESCE(COB."CoberturaCorredora", MAXCR."CoberturaCorredora", 0)/100)					-- Si garantia es de tipo Custodia   -- MontoGarantia = INGMonto --> Es el Monto valorizado
 	END AS "MontoCoberturaEntidad",
	COALESCE(COB."CoberturaCorredora", MAXCR."CoberturaCorredora", 0) AS "PorcentajeCoberturaEntidad",	 
	CASE 
		WHEN GAR."IdOrigenGarantia" IN ('ESP','CTA') THEN 
			(CAST(COALESCE(CON."Valor",'100') AS BIGINT)/100)*COALESCE(GAR."MontoGarantia",0) 		-- Si garantia es de tipo Especies o Efectivo
	 	ELSE
	 		COALESCE(GAR."INGMonto",0) * (COALESCE(COB."CoberturaCorredora", MAXCR."CoberturaCorredora", 0)/100) 			-- Si garantia es de tipo Custodia   -- MontoGarantia = INGMonto --> Es el Monto valorizado
	END AS "MontoCoberturaCorredora",
	COALESCE(COB."CoberturaCorredora", MAXCR."CoberturaCorredora", 0) AS "PorcentajeCoberturaCorredora",
	GAR."PrecioGarantia",
	GAR."IdInstrumento",
	GAR."IdTipoInstrumento"
FROM "BSCL.Custodia.BD.Vistas::GTA.ConsultarGarantiasSaldos" AS GAR
LEFT JOIN "BSCL.Plataforma.BD::GTA.CoberturaGarantia" AS COB ON 
	(	     COB."IdEntidadDestinoGarantia" = GAR."IdEntidadDestinoGarantia"
		 AND (GAR."IdConcepto" IN ('GARDEV', 'GARDVA') OR COB."IdTipoInstrumentoExigeGarantia" = GAR."IdTipoInstrumentoGarantizado")
	 	 AND COB."IdInstrumentoExigeGarantia" = GAR."IdInstrumentoCarteraGarantizada" 
		 AND COB."IdTipoInstrumentoComoGarantia"  = GAR."IdTipoInstrumento"
		 AND COB."ClasificacionRiesgo" = GAR."ClasificacionRiesgo"
		 AND COB."IdTipoClasificacionRiesgo" = GAR."IdTipoClasificacionRiesgo"
		 AND COB."IdEntidadClasificadoraRiesgo" = GAR."IdEntidadClasificadoraRiesgo" 
		 AND COB."IdEstado" = 'ACT'
	)
LEFT JOIN (
	SELECT 
	    CB."IdEstado", CB."IdInstrumentoExigeGarantia", CB."CoberturaCorredora", 
	    CB."CoberturaEntidad", CB."IdTipoClasificacionRiesgo", CB."IdEntidadDestinoGarantia", CB."IdTipoInstrumentoComoGarantia", 
	    CB."IdTipoInstrumentoExigeGarantia", TCR."IdEntidadClasificadoraRiesgo",  CB."ClasificacionRiesgo"
	FROM "BSCL.Plataforma.BD::GTA.CoberturaGarantia" CB
	INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumentoClasificacionRiesgo" AS TCR ON
	    TCR."IdTipoInstrumento" = cb."IdTipoInstrumentoComoGarantia"  AND 
	    TCR."IdTipoClasificacionRiesgo" = CB."IdTipoClasificacionRiesgo"  AND 
	    TCR."IdEntidadClasificadoraRiesgo" = CB."IdEntidadClasificadoraRiesgo" AND 
	    TCR."EntidadClasificadoraRiesgoPrincipal" = 'S' AND 
	    TCR."IdEstado" = 'ACT'
	INNER JOIN (
	    SELECT *,
	    ROW_NUMBER() OVER (
	        PARTITION BY "IdTipoClasificacionRiesgo", "IdEntidadClasificadoraRiesgo"
	        ORDER BY "Nivel" DESC
	    ) AS rn
	    FROM "BSCL.Plataforma.BD::INS.ClasificacionRiesgo"
	) AS CR ON 
	    CR."ClasificacionRiesgo" = CB."ClasificacionRiesgo" AND 
	    CB."IdTipoClasificacionRiesgo" = CR."IdTipoClasificacionRiesgo" AND 
	    CR."IdEntidadClasificadoraRiesgo" = CB."IdEntidadClasificadoraRiesgo" AND CR.rn = 1
) AS MAXCR ON 
	MAXCR."IdEntidadDestinoGarantia" = GAR."IdEntidadDestinoGarantia" AND
 	MAXCR."IdInstrumentoExigeGarantia" = GAR."IdInstrumentoCarteraGarantizada" AND
	MAXCR."IdTipoInstrumentoComoGarantia"  = GAR."IdTipoInstrumento" AND
	MAXCR."IdTipoClasificacionRiesgo" = GAR."IdTipoClasificacionRiesgo" AND
	MAXCR."IdEstado" = 'ACT'
LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" AS CON ON
	(	    CON."CodigoParametro" = 'PLA.CoberturaEfectivoEspecies'
		AND CON."IdEmpresa" = GAR."IdEmpresa"	)
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS INS ON
	GAR."IdTipoInstrumentoGarantizado" = INS."IdTipoInstrumento"
WITH READ ONLY;
GO 
GRANT SELECT ON "BSCL.Custodia.BD.Vistas::GTA.ConsultarCoberturaGarantiaDetalle" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
