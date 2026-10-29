SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Mesa.BD.Vistas::ConsultarMovimientosDiarios";
GO
CREATE VIEW "BSCL.Mesa.BD.Vistas::ConsultarMovimientosDiarios" AS 
SELECT   --ANTICIPOS -VENCIMIENTOS - OPERACIONES COMPAC/VENPAC
	OP."FolioOperacion"  AS "Folio",
	OP."DocumentoIdentidad" AS "Rut",
	OP."RazonSocial" AS "Nombre",
	OP."CodigoPortafolio" AS "Cuenta",
	CAR."Nemotecnico" AS "Nemotecnico",
	CAR."IdEmisor" AS "Emisor",
	COALESCE(TRAN."EsFueraRueda",'S') AS "EsFueraRueda", 
	CASE WHEN COALESCE(TRAN."EsFueraRueda",'S') = 'S' THEN COALESCE(NEGFDR."Negociacion", 'UI_FUERA_DE_RUEDA')
	ELSE COALESCE(NEGBUR."Negociacion", 'UI_BURSATIL')
	END	AS "Negociacion",
	OP."IdTipoOperacion" AS "TipoCompromiso",
	CAR."IdConcepto" AS "CodigoMovimiento",
	CASE WHEN CAR."IdClasificacionTipoInstrumento" = 'IF' THEN CAR."INGValorRescate"
	ELSE CAR."Cantidad" 
	END AS "Cantidad",
	OP."MontoOperacionFinal" AS "ValorFinal",
	CAR."IdTipoReajuste" AS "MO",
	CAR."TasaCompra" AS "Tasa",
	CAR."FechaVencimientoInstrumento" AS "FechaVencimientoInstrumento",
	OP."FechaVencimiento" AS "FechaVencimientoCompromiso",
	CAR."MontoCompra" AS "MontoOperacion",
	0 AS "Spread",
	CAR."FolioCartera" AS "FolioComprobanteAdjunto",
	OP."FechaOperacion" AS "FechaOperacion" ,
	CAR."IdClasificacionTipoInstrumento",
	OP."IdEstado",
	OP."IdTipoInstrumento",
	
	OP."TasaAgente" as "TAgente",
	CASE WHEN OP."IdEstado" = 'ANT' THEN TO_DATE(TO_NVARCHAR(OP."FechaLiquidacion",'YYYY-MM-DD'),'YYYY-MM-DD')
		 WHEN OP."IdEstado" = 'LIQ' THEN CAR."FechaVencimientoInstrumento"
		 ELSE OP."FechaOperacion"  END AS "FechaConsulta" 
	FROM "BSCL.Mesa.BD.Vistas::ConsultarOperaciones" OP
LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" AS CAR ON CAR."IdOperacion" = OP."IdOperacion" 
LEFT JOIN "BSCL.Orden.BD::ORD.Transaccion" AS TRAN ON TRAN."Folio" = CAR."FolioExternoReferencia"
LEFT JOIN (SELECT "Descripcion" AS "Negociacion" FROM "BSCL.Plataforma.BD::PLA.Diccionario" WHERE "CodDiccionario" = 'UI_FUERA_DE_RUEDA') AS NEGFDR ON 1 = 1
LEFT JOIN (SELECT "Descripcion" AS "Negociacion" FROM "BSCL.Plataforma.BD::PLA.Diccionario" WHERE "CodDiccionario" = 'UI_BURSATIL') AS NEGBUR ON 1 = 1
WHERE OP."IdEstado" IN ('ANT','LIQ','VIG')
AND CAR."IdClasificacionTipoInstrumento" IN ('RF','IF') AND  OP."IdTipoProducto" ='MERF'
UNION ALL --VENTER/COMTER
SELECT 
	OP."FolioOperacion"  AS "Folio",
	OP."DocumentoIdentidad" AS "Rut",
	OP."RazonSocial" AS "Nombre",
	OP."CodigoPortafolio" AS "Cuenta",
	Op."NombreOperacion" AS "Nemotecnico",
	CAR."IdEmisor" AS "Emisor",
	COALESCE(TRAN."EsFueraRueda",'S') AS "EsFueraRueda", 
	CASE WHEN COALESCE(TRAN."EsFueraRueda",'S') = 'S' THEN COALESCE(NEGFDR."Negociacion", 'UI_FUERA_DE_RUEDA')
	ELSE COALESCE(NEGBUR."Negociacion", 'UI_BURSATIL')
	END	AS "Negociacion",
	OP."IdTipoOperacion" AS "TipoCompromiso",
	CAR."IdConcepto" AS "CodigoMovimiento",
	CASE WHEN CAR."IdClasificacionTipoInstrumento" = 'IF' THEN CAR."INGValorRescate"
	ELSE CAR."Cantidad" 
	END AS "Cantidad",
	OP."MontoOperacionFinal" AS "ValorFinal",
	CAR."IdTipoReajuste" AS "MO",
	CAR."TasaCompra" AS "Tasa",
	CAR."FechaVencimientoInstrumento" AS "FechaVencimientoInstrumento",
	OP."FechaVencimiento" AS "FechaVencimientoCompromiso",
	CAR."MontoCompra" AS "MontoOperacion",
	0 AS "Spread",
	CAR."FolioCartera" AS "FolioComprobanteAdjunto",
	OP."FechaOperacion",
	CAR."IdClasificacionTipoInstrumento",
	OP."IdEstado",
	OP."IdTipoInstrumento",
	OP."TasaAgente" as "TAgente",
	
	OP."FechaOperacion" AS "FechaConsulta"
	FROM "BSCL.Mesa.BD.Vistas::ConsultarOperaciones" OP
LEFT JOIN "BSCL.Custodia.BD::CUS.Reserva" AS RES ON RES."IdOperacion" = OP."IdOperacion" 
LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" AS CAR ON CAR."IdOperacion" = RES."IdReserva" 
LEFT JOIN "BSCL.Orden.BD::ORD.Transaccion" AS TRAN ON TRAN."Folio" = CAR."FolioExternoReferencia"
LEFT JOIN (SELECT "Descripcion" AS "Negociacion" FROM "BSCL.Plataforma.BD::PLA.Diccionario" WHERE "CodDiccionario" = 'UI_FUERA_DE_RUEDA') AS NEGFDR ON 1 = 1
LEFT JOIN (SELECT "Descripcion" AS "Negociacion" FROM "BSCL.Plataforma.BD::PLA.Diccionario" WHERE "CodDiccionario" = 'UI_BURSATIL') AS NEGBUR ON 1 = 1
WHERE  OP."IdTipoProducto" ='MERF' 
			AND OP."FolioOperacion" !=0 AND OP."IdEstado" = 'VIG' AND OP."IdTipoOperacion" IN('VENTER','COMTER')
			AND CAR."IdClasificacionTipoInstrumento" IN ('RF','IF')	
GO
GRANT SELECT ON "BSCL.Mesa.BD.Vistas::ConsultarMovimientosDiarios" TO BCS_DESARROLLADOR WITH GRANT OPTION;