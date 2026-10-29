	
INSERT INTO "BSCL.Custodia.BD::GBL.AnticiparPrestamo_Proceso_Prestamos"
(
	--Datos del prestamo
	"FolioPrestamo","DevolverCustodia","IdEmpleadoDigitador","FechaAnticipo","IdPais","IdMonedaLiquidacion","IdCondicionLiquidacion",
	"FechaLiquidacion", "FechaPrestamo","FechaVencimientoPrestamo","IdCliente","IdPortafolio","IdTipoProducto","IdTipoOperacion",
	"IdAgente","IdSucursal", "IdMonedaSistema","IdTipoComprobante","FolioComprobante","FechaComprobante","IdBolsa",
	"IdCartera", "FolioCartera", "IdEmisor",
	
	--Instrumento
	"IdTipoInstrumento","IdInstrumento","IdTipoReajuste","IdBoveda",
	
	--Cantidades y Montos
	"Cantidad","PrecioInstrumento","MontoInstrumento","TasaPrima","MontoPrima","MontoAPlazo","MontoTotalPrestamo",
	
	--Subyacente
	"IdTipoInstrumentoSubyacente","IdInstrumentoSubyacente","IdEmisorSubyacente","IdMonedaSubyacente",
	
	--Configuracion del prestamo
	"CobraPrimaAlInicio","MinimoDiasAnticipo","MaximoDiasPrestamo","EsDiaHabilPlazoPrestamo","EsDiaHabilParaCalculoInteres"
	
	
)
			
SELECT
	--Datos del prestamo
	"FolioCartera","DevolverCustodia","IdEmpleadoDigitador","FechaAnticipo","IdPais","IdMonedaLiquidacion",
	"IdCondicionLiquidacion","FechaLiquidacion","FechaOperacion","FechaVencimientoOperacion","IdCliente","IdPortafolio","IdTipoProducto","IdTipoOperacion","IdAgente","IdSucursal",
	"IdMonedaSistema","IdTipoComprobante","FolioComprobante","FechaComprobante","IdBolsa",
	"IdCartera","FolioCartera", "IdEmisor",
	
	--Instrumento
	"IdTipoInstrumento","IdInstrumento","IdTipoReajuste","IdBoveda",
	
	--Cantidades y Montos
	"INGCantidad","INGPrecioMercadoActual","INGMontoATasaMercadoActual","TasaCompra","MontoPrima","INGMontoAPlazo","INGMontoAPlazo",
	
	--Subyacente
	"IdTipoInstrumentoSubyacente","IdInstrumentoSubyacente","IdEmisorSubyacente","IdMonedaSubyacente",
	
	--Configuracion del prestamo
	"CobraPrimaAlInicio","MinimoDiasAnticipo","MaximoDiasPrestamo","EsDiaHabilPlazoPrestamo","EsDiaHabilParaCalculoInteres"

FROM
	(
			SELECT
				--Datos del prestamo
	    		c."FolioCartera",e."DevolverCustodia",e."IdEmpleadoDigitador",e."FechaAnticipo",e."IdPais",e."IdMonedaLiquidacion",
				e."IdCondicionLiquidacion",e."FechaLiquidacion",
				c."FechaOperacion",c."FechaVencimientoOperacion",c."IdCliente",c."IdPortafolio",c."IdTipoProducto",c."IdTipoOperacion",
				c."IdAgente",c."IdSucursal",c."IdMonedaSistema",c."IdTipoComprobante",c."FolioComprobante",c."FechaComprobante",c."IdBolsa",
				c."IdCartera", c."IdEmisor",
				--Instrumento
				c."IdTipoInstrumento",c."IdInstrumento",c."IdTipoReajuste",c."IdBoveda",
				--Cantidades y Montos
				e."Cantidad" AS "INGCantidad", 
				CASE 
					WHEN e."UsaPrecioMedio" = 'S' THEN e."PrecioMedio"
					ELSE COALESCE(e."PrecioInstrumento",s."PrecioSubyacente")
				END AS "INGPrecioMercadoActual",
				ROUND(e."Cantidad" * CASE
											WHEN e."UsaPrecioMedio" = 'S' THEN e."PrecioMedio"
											ELSE COALESCE(e."PrecioInstrumento",s."PrecioSubyacente") 
										END, 0) AS "INGMontoATasaMercadoActual",
				e."TasaAnticipo" AS "TasaCompra",round(e."MontoPrima",0) AS "MontoPrima",ROUND(e."MontoAPlazo", 0) as "INGMontoAPlazo",
				--Subyacente
				s."IdTipoInstrumento" AS "IdTipoInstrumentoSubyacente",s."IdInstrumento" AS "IdInstrumentoSubyacente",s."IdEmisor" AS "IdEmisorSubyacente",
				s."IdMonedaInstrumento" AS "IdMonedaSubyacente",
				--Configuracion del prestamo
				c."CobraPrimaAlInicio",p."MinimoDiasAnticipo",p."MaximoDiasPrestamo",p."EsDiaHabilPlazoPrestamo",p."EsDiaHabilParaCalculoInteres"
			FROM "BSCL.Custodia.BD::GBL.AnticiparPrestamo_Proceso_Paso" AS e
			INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" AS c ON (c."FolioCartera" = e."FolioPrestamo" AND c."IdEstado" = 'VIG')
			INNER JOIN "BSCL.Custodia.BD::CUS.Subyacente" AS s ON (s."FolioCarteraRelacionada" = c."FolioCartera" AND s."IdEstado" = 'VIG')
			INNER JOIN "BSCL.Custodia.BD::PRT.TipoPrestamo" AS p ON (p."IdInstrumentoPrestamo" = c."IdInstrumento")
			
	);
		
