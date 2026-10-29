
INSERT INTO "BSCL.Custodia.BD::GBL.AnticiparEmprestito_Proceso_Emprestitos"
(
	--DATOS RECIBIDOS
	"FolioPrestamo","FolioEmprestito","DevolverCustodia","IdEmpleadoDigitador","FechaAnticipo","PlazoAlVencimiento",
	"UtilizaDatosEnviados",
	--EMPRESTITO OPERACION
    "CobraPrimaAlInicio","IdCliente","IdPortafolio","FechaEmprestito","FechaVencimientoEmprestito","IdTipoProducto","IdTipoOperacion",
	"IdPais","IdMonedaSistema","IdMonedaLiquidacion","IdCondicionLiquidacion","FechaLiquidacion","IdTipoComprobante",
	"FolioComprobante","FechaComprobante","IdAgente","IdSucursal","IdBolsa",
	"IdCartera","FolioCartera",
	    
	--INSTRUMENTO
	"IdTipoInstrumento","IdInstrumento","IdEmisor","IdBoveda","IdTipoReajuste",
	--MONTO
	"CantidadAnticipo","PrecioInstrumentoAnticipo","MontoInstrumentoAnticipo","TasaPremioAnticipo","MontoPremioAnticipo",
	"MontoAPlazoAnticipo","MontoPremio",
	--SUBYACENTE
	"IdClasificacionTipoInstrumentoSubyacente","IdTipoInstrumentoSubyacente","IdInstrumentoSubyacente","IdEmisorSubyacente",
	"IdMonedaSubyacente",
	--PRESTAMO RELACIONADO
	"IdClientePrestamo","IdPortafolioPrestamo"
)
SELECT
    		--DATOS RECIVIDOS
    		"FolioPrestamo","FolioEmprestito","DevolverCustodia","IdEmpleadoDigitador","FechaAnticipo","PlazoAlVencimiento","UtilizaDatosEnviados",
    		--EMPRESTITO
			--OPERACION
			"CobraPrimaAlInicio","IdCliente","IdPortafolio","FechaEmprestito","FechaVencimientoEmprestito","IdTipoProducto",
			"IdTipoOperacion","IdPais","IdMonedaSistema","IdMonedaLiquidacion","IdCondicionLiquidacion","FechaLiquidacion",
			"IdTipoComprobante","FolioComprobante","FechaComprobante","IdAgente","IdSucursal","IdBolsa","IdCartera","FolioCartera",
    		--INSTRUMENTO
			"IdTipoInstrumento","IdInstrumento","IdEmisor","IdBoveda","IdTipoReajuste",
			--MONTO
			COALESCE("Cantidad","CantidadEmprestito"),COALESCE("PrecioInstrumento","PrecioInstrumentoEmprestito"),
			CASE WHEN ROUND(COALESCE("MontoInstrumento","MontoInstrumentoEmprestito",0), 0) = 0 THEN 
					ROUND((COALESCE("PrecioInstrumento","PrecioInstrumentoEmprestito") * COALESCE("Cantidad","CantidadEmprestito")), 0)
				 ELSE ROUND(COALESCE("MontoInstrumento","MontoInstrumentoEmprestito",0), 0)
			END,
			COALESCE("TasaPremio","TasaPremioEmprestito"),
			CASE WHEN "UtilizaDatosEnviados" = 'S' THEN ROUND("MontoPremio",0) ELSE NULL END,
			CASE WHEN "UtilizaDatosEnviados" = 'S' THEN ROUND("MontoAPlazo",0) ELSE NULL END,
			ROUND("MontoPremioEmprestito",0),
			--SUBYACENTE
		    "IdClasificacionTipoInstrumentoSubyacente","IdTipoInstrumentoSubyacente","IdInstrumentoSubyacente","IdEmisorSubyacente",
		    "IdMonedaSubyacente",
			--PRESTAMO RELACIONADO
			"IdClientePrestamo","IdPortafolioPrestamo"
		FROM (
				SELECT
					ROW_NUMBER() over (PARTITION BY c."FolioCartera" ORDER BY c."FolioCartera" DESC,c."IdTipoProducto" DESC) AS "Posicion",
					cr."FolioCartera" AS "FolioPrestamo",c."FolioCartera" AS "FolioEmprestito",d."DevolverCustodia",d."IdEmpleadoDigitador",
					d."FechaAnticipo",d."PlazoAlVencimiento",
					CASE WHEN COALESCE(d."Cantidad",0) > 0 AND
							  COALESCE(d."MontoInstrumento",0) > 0 AND
							  COALESCE(d."TasaPremio",0) > 0 AND
							  COALESCE(d."MontoPremio",0) > 0 AND
							  d."UsaPrecioMedio" <> 'S' AND
							  COALESCE(d."MontoAPlazo",0) > 0 THEN 'S'
						 ELSE 'N'
					END AS "UtilizaDatosEnviados",
					--Datos enviados
					CASE WHEN d."UsaPrecioMedio" = 'S' THEN d."PrecioMedio" ELSE d."PrecioInstrumento" END as "PrecioInstrumento",
					d."Cantidad",
					ROUND(CASE WHEN d."UsaPrecioMedio" = 'S' THEN d."Cantidad"*d."PrecioMedio" ELSE d."MontoInstrumento" END, 0) as "MontoInstrumento",
					d."TasaPremio",ROUND(d."MontoPremio",0) as "MontoPremio",ROUND(d."MontoAPlazo",0) as "MontoAPlazo",
					------------------------------------------------------
					--OPERACION
					c."CobraPrimaAlInicio",c."IdCliente",c."IdPortafolio",c."FechaOperacion" AS "FechaEmprestito",c."FechaVencimientoOperacion" AS "FechaVencimientoEmprestito",
					c."IdTipoProducto",c."IdTipoOperacion",d."IdPais",c."IdMonedaSistema",d."IdMonedaLiquidacion",d."IdCondicionLiquidacion",
					d."FechaLiquidacion",c."IdTipoComprobante",c."FolioComprobante",c."FechaComprobante",
					c."IdAgente",c."IdSucursal",c."IdBolsa",c."IdCartera",c."FolioCartera",
					--INSTRUMENTO
					c."IdTipoInstrumento",c."IdInstrumento",c."IdEmisor",c."IdBoveda",c."IdTipoReajuste",
					--MONTO
					c."INGCantidad" AS "CantidadEmprestito",
					CASE WHEN d."UsaPrecioMedio" = 'S' THEN d."PrecioMedio" ELSE COALESCE(s."INGPrecioSubyacente",s."PrecioSubyacente") END AS "PrecioInstrumentoEmprestito",
					ROUND(CASE WHEN d."UsaPrecioMedio" = 'S' THEN c."INGCantidad" * d."PrecioMedio" ELSE c."INGCantidad" * COALESCE(s."INGPrecioSubyacente",s."PrecioSubyacente") END,0) AS "MontoInstrumentoEmprestito",
					
					c."TasaCompra" AS "TasaPremioEmprestito",
					ROUND(c."INGPrecioAPlazo",0)  AS"MontoPremioEmprestito",
					--SUBYACENTE
				    s."IdClasificacionTipoInstrumento" AS "IdClasificacionTipoInstrumentoSubyacente",s."IdTipoInstrumento" AS "IdTipoInstrumentoSubyacente",
				    s."IdInstrumento" AS "IdInstrumentoSubyacente",s."IdEmisor" AS "IdEmisorSubyacente",s."IdMonedaInstrumento" AS "IdMonedaSubyacente",
					--PRESTAMO RELACIONADO
					cr."IdCliente" AS "IdClientePrestamo",cr."IdPortafolio" AS "IdPortafolioPrestamo"
				
				
				FROM	"BSCL.Custodia.BD::GBL.AnticiparEmprestito_Proceso_Paso" AS d  ---- tabla de proceso de paso
				
				INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" c ON (c."FolioCartera" = d."FolioEmprestito" AND c."IdEstado" = 'VIG')  -- Cartera del emprestito
				INNER JOIN "BSCL.Custodia.BD::CUS.Subyacente" s ON (s."FolioCarteraRelacionada" = c."FolioCartera" AND s."IdEstado" = 'VIG')
				INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" cr ON (cr."FolioCartera" = c."FolioCarteraRelacionada" AND cr."IdEstado" = 'VIG')  -- Cartera del prestamo
				
		)
		WHERE "Posicion" = 1;
		
		