SET SCHEMA "BSCL";
DROP PROCEDURE "BSCL.Orden.BD.Servicios::ConsultarTransaccionesAnuladasModificadas";

CREATE PROCEDURE
-- 
--  Proposito: lo que dice el nombre
--  Autor: Renzo Ulloa
--  Fecha Creacion: 14-01-2017
--   
"BSCL.Orden.BD.Servicios::ConsultarTransaccionesAnuladasModificadas" (
	IN p_IdEmpresa          BIGINT,
	IN p_IdUsuario          BIGINT,
	IN p_IdModulo           NVARCHAR(3),
	
	
	IN p_FechaConsulta		NVARCHAR(10),
	
	OUT p_out_Transacciones 	TABLE(
			"IdTipoInstrumento"			NVARCHAR(3),
			"Nemotecnico"				NVARCHAR(40),
			"FechaLogCambio"			SECONDDATE,
			"IdBolsa"					NVARCHAR(3),
			"FechaTransaccion"			DATE,
			"FolioTransaccion"			NVARCHAR(40),
			"CodigoCorredorComprador"	NVARCHAR(40),
			"CodigoCorredorVendedor"	NVARCHAR(40),
			"TipoLogCambio"				NVARCHAR(40),
			"CantidadTransaccion"		DECIMAL(26,8),
			"PrecioTransaccion"			DECIMAL(26,8),
			"MontoTransaccion"			DECIMAL(26,8),
			"TasaTransaccion"			DECIMAL(26,8),
			"Motivo"					NVARCHAR(5000),
			"Observaciones"				NVARCHAR(5000),
			"IdMoneda"					NVARCHAR(3)
		
	
	) DEFAULT EMPTY,
	
	
	OUT p_out_InformacionContador  		INT,
	OUT p_out_InformacionDetalle   		TABLE ("IdInterno" BIGINT, "IdOperacion" BIGINT,"Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000)) DEFAULT EMPTY, 
	OUT p_out_AdvertenciasContador 		INT,
	OUT p_out_AdvertenciasDetalle 		TABLE ("IdInterno" BIGINT, "IdOperacion" BIGINT,"Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(5000))  DEFAULT EMPTY,	
	OUT p_out_ErroresContador 			INT,
	OUT p_out_ErroresDetalle 			TABLE ("IdInterno" BIGINT, "IdOperacion" BIGINT,"Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000))  DEFAULT EMPTY	
	
	
	 
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER
	DEFAULT SCHEMA "BSCL"
	--READS SQL DATA WITH RESULT VIEW "BSCL.Orden.BD.Vistas::ConsultarTransaccionesAnuladasModificadas" 
	
AS
BEGIN
	
	p_out_Transacciones = 
	WITH FF AS (
		SELECT "IdTransaccion"
		FROM "BSCL.Orden.BD::ORD.LogCambioTransaccion" cam
		WHERE cam."TipoLogCambio" != 'N' AND cam."FechaSistemaCreacion" = :p_FechaConsulta
		GROUP BY "IdTransaccion"
		HAVING COUNT(1) >= 1
	)
	SELECT	cam."IdTipoInstrumento",cam."Nemotecnico",cam."FechaLogCambio",cam."IdBolsa",cam."FechaSistemaCreacion" as "FechaTransaccion",cam."Folio" as "FolioTransaccion",
			CASE tipoope."AccionOrden" WHEN 'COMPRA' THEN corrCodLocal."CodigoCorredora" WHEN 'VENTA' THEN corrCodContraparte."CodigoCorredora" END AS "CodigoCorredorComprador",
			CASE tipoope."AccionOrden" WHEN 'COMPRA' THEN corrCodContraparte."CodigoCorredora" WHEN 'VENTA' THEN corrCodLocal."CodigoCorredora" END AS "CodigoCorredorVendedor",
			CASE cam."TipoLogCambio" WHEN 'N' THEN 'NUEVO' WHEN 'A' THEN 'ANULACION' WHEN 'M' THEN 'MODIFICACION' END AS "TipoLogCambio",
			cam."Cantidad" as "CantidadTransaccion",cam."Precio" as "PrecioTransaccion",cam."Monto" as "MontoTransaccion",cam."Tasa" as "TasaTransaccion",
			cam."Motivo",cam."Observaciones",cam."IdMoneda"
	FROM FF 
	LEFT JOIN "BSCL.Orden.BD::ORD.LogCambioTransaccion" cam ON cam."IdTransaccion" = FF."IdTransaccion"
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" tipoope on tipoope."IdTipoOperacion" = cam."IdTipoOperacion"
	LEFT JOIN "BSCL.Plataforma.BD::INS.Corredora" corrContraparte on corrContraparte."IdCorredora" = cam."IdCorredorContraparte"
	LEFT JOIN "BSCL.Plataforma.BD::INS.CorredoraCodigos" corrCodContraparte on corrCodContraparte."IdCorredora" = corrContraparte."IdCorredora" AND corrCodContraparte."IdBolsa" = cam."IdBolsa"
	LEFT JOIN "BSCL.Plataforma.BD::INS.Corredora" corrLocal on corrLocal."IdCorredora" = cam."IdCorredorLocal"
	LEFT JOIN "BSCL.Plataforma.BD::INS.CorredoraCodigos" corrCodLocal on corrCodLocal."IdCorredora" = corrLocal."IdCorredora" AND corrCodLocal."IdBolsa" = cam."IdBolsa"
	ORDER BY "FolioTransaccion","FechaLogCambio";


END
