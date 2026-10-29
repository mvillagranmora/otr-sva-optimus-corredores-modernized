SET SCHEMA "BSCL"; 
GO 
DROP VIEW "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteMedioDePagoEntregamos";
GO
CREATE VIEW "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteMedioDePagoEntregamos" AS
((SELECT "IdComprobante", COALESCE(MAX("MedioDePagoEntregamos"),COALESCE(MAX("NombreMedioDePago1"),'SIN DEFINIR')) AS "MedioDePagoEntregamos"
FROM (
	SELECT fpag."IdComprobante", pref."MedioDePagoPref", pref."NombreMedioDePago1",fpag."NombreMedioDePago",
		CASE WHEN fpag."NombreMedioDePago" IS NULL THEN 'SIN DEFINIR'
			WHEN pref."MedioDePagoPref" IS NULL THEN pref."NombreMedioDePago1"
			WHEN fpag."NombreMedioDePago"=pref."MedioDePagoPref" THEN pref."MedioDePagoPref"
			ELSE NULL END AS "MedioDePagoEntregamos"
	FROM (
		-- solo medios de pago usados
		SELECT comp."IdComprobante", COALESCE(fpag."NombreMedioDePago",'SIN DEFINIR') AS "NombreMedioDePago"
		FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" comp
		LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" movcomp
	    ON comp."IdComprobante"=movcomp."IdComprobante"
		LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobanteFormaDePago" fpag
	    ON fpag."IdMovimientoComprobante"=movcomp."IdMovimientoComprobante"
		WHERE comp."IdTipoOperacion" IN ('COMSPT','VENSPT','ANCSPT','ANVSPT') AND (fpag."IdTipoMovimientoSpot"='MOVEGR' OR fpag."IdTipoMovimientoSpot" IS NULL)
		GROUP BY comp."IdComprobante", fpag."IdMedioDePago", fpag."NombreMedioDePago" 
	) fpag LEFT JOIN (
		-- medio pago preferencia
		SELECT MOVCOMP."IdComprobante", first_value(fpag."NombreMedioDePago" ORDER BY fpag."NombreMedioDePago" ASC) AS "NombreMedioDePago1", pref."MedioDePago" AS "MedioDePagoPref"		
		FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobante" movcomp	    
		LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobanteFormaDePago" fpag
	    ON fpag."IdMovimientoComprobante"=movcomp."IdMovimientoComprobante"
		LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" por
	    ON por."IdPortafolio"=movcomp."IdPortafolio"
		LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPreferenciaPagoPortafolio" pref
	    ON pref."IdPortafolio"=por."IdPortafolio" AND pref."PorDefecto"='S' AND pref."IdTipoOperacion"=movcomp."IdTipoOperacion" AND pref."IdMoneda"=fpag."IdMoneda"
		WHERE movcomp."IdTipoOperacion" IN ('COMSPT','VENSPT','ANCSPT','ANVSPT') AND (fpag."IdTipoMovimientoSpot"='MOVEGR' OR fpag."IdTipoMovimientoSpot" IS NULL)
		GROUP BY movcomp."IdComprobante",pref."MedioDePago"		
	) pref
	ON pref."IdComprobante"=fpag."IdComprobante" 
	GROUP BY fpag."IdComprobante",fpag."NombreMedioDePago",pref."MedioDePagoPref",pref."NombreMedioDePago1"	
) GROUP BY "IdComprobante")
UNION
(SELECT comp."IdComprobante",
       CASE
           WHEN comp."MontoLiquidacionFinal" >= 0 THEN
               MAX(fpag."NombreMedioDePago")
           ELSE '' 
       END AS "MedioDePagoEntregamos"       
FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" comp
LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteFormaDePago" fpag
ON fpag."IdComprobante"=comp."IdComprobante"
WHERE comp."IdTipoOperacion" NOT IN ('COMSPT','VENSPT','ANCSPT','ANVSPT')
GROUP BY comp."IdComprobante", comp."MontoLiquidacionFinal"));

GO
GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteMedioDePagoEntregamos" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
