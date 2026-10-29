SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Orden.BD.Servicios::MIG.CrearSimulacionAsignacion";
GO
CREATE PROCEDURE
-- 
--  Proposito: Generar Simulacion de Asignacion
--  Autor: PROJO - BCS
--  Fecha Creacion: 08/02/2015
--  FAlta calculo Pre PROm
"BSCL.Orden.BD.Servicios::MIG.CrearSimulacionAsignacion" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa					BIGINT,			--ID Empresa destino
	IN  p_IdUsuario					BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo					NVARCHAR(3),	--ID del Modulo que lo invoca	

	IN	p_IdTipoAsignacion			NVARCHAR(40),	--Tipo asignacion a ejecutar
	IN	p_IdTipo					NVARCHAR(40) default 'ASIG',	--Tipo operacion a ejecutar(simulacion asignacion (ASIG) o simulacion refacturacion (REFACT))
	IN	p_Origen					NVARCHAR(40)--origen de la llamamda a refacturar
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
	
AS
BEGIN
	BEGIN
		
		DECLARE v_IdBovedaDefecto NVARCHAR(3) := 'DCV';
		SELECT 1 FROM DUMMY;
		--Algoritmo de asignacion (Llamar los sp de asignacion)
		v_Transacciones = SELECT * FROM "BSCL.Orden.BD::ORD.CrearSimulacionAsignacion_Entrada_Transacciones";
		v_Portafolios = SELECT * FROM "BSCL.Orden.BD::ORD.CrearSimulacionAsignacion_Entrada_Portafolios";
		v_Ordenes = SELECT * FROM "BSCL.Orden.BD::ORD.CrearSimulacionAsignacion_Entrada_Ordenes";
		v_Carteras = SELECT * FROM "BSCL.Orden.BD::ORD.CrearSimulacionAsignacion_Entrada_Carteras";
				
		if p_IdTipoAsignacion = 'ASIGNACION_POSTRERA' then
			begin
				CALL "BSCL.Orden.BD.Servicios::CrearSimulacionAsignacionPostrera" (p_IdEmpresa,:p_IdTipo,:p_Origen,:v_Transacciones,:v_Portafolios,v_out_SimAsignacion);
			end;
		elseif p_IdTipoAsignacion = 'ASIGNACION_PRECIO_PROMEDIO' then
			begin
				CALL "BSCL.Orden.BD.Servicios::CrearSimulacionAsignacionPreProm" (p_IdEMpresa,:p_IdTipo,:p_Origen,:v_Transacciones,:v_Portafolios,v_out_SimAsignacion);
			end;
		elseif p_IdTipoAsignacion = 'ASIGNACION_PROPORCIONAL' then
			begin
				CALL "BSCL.Orden.BD.Servicios::CrearSimulacionAsignacionPropCrono" (p_IdEMpresa,:p_IdTipo,:p_Origen,:v_Transacciones,:v_Ordenes,v_out_SimAsignacion);
			end;
		elseif p_IdTipoAsignacion = 'ASIGNACION_MANUAL_ORDENES' then
			begin
				CALL "BSCL.Orden.BD.Servicios::CrearSimulacionAsignacionManual" (p_IdEMpresa,:p_IdTipo,:p_Origen,:v_Transacciones,:v_Ordenes,v_out_SimAsignacion);
			end;
		elseif p_IdTipoAsignacion = 'ASIGNACION_MANUAL_CARTERAS' then
			begin
				CALL "BSCL.Orden.BD.Servicios::CrearSimulacionAsignacionManualCartera" (p_IdEMpresa,:p_IdTipo,:p_Origen,:v_Transacciones,:v_Carteras,v_out_SimAsignacion);
			end;
		elseif p_IdTipoAsignacion = 'ASIGNACION_CARTERAS' then
			begin
				CALL "BSCL.Orden.BD.Servicios::CrearSimulacionAsignacionCartera" (p_IdEMpresa,:p_IdTipo,:p_Origen,:v_Transacciones,:v_Carteras,v_out_SimAsignacion);
			end;
		elseif p_IdTipoAsignacion = 'ASIGNACION_MANUAL_CLIENTES' then
			begin
				CALL "BSCL.Orden.BD.Servicios::CrearSimulacionAsignacionManualCliente" (p_IdEMpresa,:p_IdTipo,:p_Origen,:v_Transacciones,:v_Portafolios,v_out_SimAsignacion);
			end;
		elseif p_IdTipoAsignacion = 'ASIGNACION_CRONOLOGICA' then
			begin
				CALL "BSCL.Orden.BD.Servicios::CrearSimulacionAsignacionCronologica" (p_IdEMpresa,:p_IdTipo,:p_Origen,:v_Transacciones,:v_Ordenes,v_out_SimAsignacion);
			end;
		end if;
		
		v_MonedaLiquidacion = SELECT "IdInterno","IdMonedaLiquidacion" as "IdMonedaLiquidacionOrden"
								FROM :v_out_SimAsignacion sim
								LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" tip on sim."IdMonedaInstrumento" = tip."IdMoneda" AND sim."IdTipoInstrumento" = tip."IdTipoInstrumento" 
								AND tip."EsPorDefecto" = 'S';
		
		SELECT "IdBovedaDefecto" INTO v_IdBovedaDefecto FROM "BSCL.Orden.BD.Vistas::ConsultarConfiguracionOrdenesPivot" WHERE "IdEmpresa" = p_IdEmpresa;
		
		INSERT INTO "BSCL.Orden.BD::ORD.CrearSimulacionAsignacion_Salida_Simulacion"
			("IdInterno","IdMovimiento","FechaSimulacion","FechaIngresoRegistro","IdTransaccion","FolioTransaccion","FechaTransaccion",
			"FolioCartera","IdPortafolio","CodigoPortafolio","IdCliente",
			"IdLibroOrdenes","IdInstrumento","Instrumento","IdTipoInstrumento",
			"IdMonedaInstrumento","IdOrden","NombreCompleto","DocumentoIdentidad","CantidadAsignacion",
			"ValorRescateAsignacion","PorcentajeAsignacion","PrecioSimulacion","IdTipoProducto",
			"IdTipoOperacion","IdTipoCanalIngreso","IdBolsa","IdClasificacionTipoInstrumento","IdMonedaLiquidacionOrden", /*,"IdFormaCustodia","IdBoveda","Glosa","TasaVentaCorta", "PlazoVentaCorta","PagoCobroCuentaInversion"*/
			"EsFungible","CantidadTransaccion", "MontoTransaccion","MontoAsignacion")
		SELECT sim."IdInterno",sim."IdMovimiento",sim."FechaSimulacion",sim."FechaIngresoRegistro",sim."IdTransaccion",sim."FolioTransaccion",sim."FechaTransaccion",
			sim."FolioCartera",sim."IdPortafolio",sim."CodigoPortafolio",sim."IdCliente",
			sim."IdLibroOrdenes",sim."IdInstrumento",sim."Instrumento",sim."IdTipoInstrumento",
			sim."IdMonedaInstrumento",sim."IdOrden",sim."NombreCompleto",sim."DocumentoIdentidad",ROUND(sim."CantidadAsignacion") as "CantidadAsignacion",
			sim."ValorRescateAsignacion",sim."PorcentajeAsignacion",sim."PrecioSimulacion",sim."IdTipoProducto",
			sim."IdTipoOperacion",sim."IdTipoCanalIngreso",sim."IdBolsa",tip."IdClasificacionTipoInstrumento",/*,
			CASE WHEN tip."IdClasificacionTipoInstrumento" = 'RV' AND ind."OperaCustodiaRV" = 'S' THEN 'CUS'
			WHEN tip."IdClasificacionTipoInstrumento" = 'RF' AND ind."OperaCustodiaRFIF" = 'S' THEN 'CUS'
			WHEN tip."IdClasificacionTipoInstrumento" = 'IF' AND ind."OperaCustodiaRFIF" = 'S' THEN 'CUS'
			ELSE 'NOM' END AS "IdFormaCustodia",
			:v_IdBovedaDefecto as "IdBoveda",'' as "Glosa", 0 as "TasaVentaCorta", 0 as "PlazoVentaCorta",
			,
			CASE ind."OperaCuentaInversion" WHEN 'S' THEN 'S' ELSE 'N' END AS "PagoCobroCuentaInversion"*/
			COALESCE(mon."IdMonedaLiquidacionOrden",sim."IdMonedaInstrumento") as "IdMonedaLiquidacionOrden",
			
			TRA."EsFungible",
			TRA."Cantidad" AS "CantidadTransaccion",
			TRA."Monto" AS "MontoTransaccion",
			((TRA."Monto" * sim."CantidadAsignacion") / TRA."Cantidad") AS "MontoAsignacion" -- Regla de 3: ((Monto Palo x Cantidad Asignada)/Cantidad Palo) 
			
	
			
		FROM :v_out_SimAsignacion sim
		LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" tip on sim."IdTipoInstrumento" = tip."IdTipoInstrumento"
		--LEFT JOIN "BSCL.Orden.BD.Vistas::ConsultarIndicadoresPortafolioOrdenes" ind on ind."IdPortafolio" = sim."IdPortafolio"
		LEFT JOIN :v_MonedaLiquidacion mon on sim."IdInterno" = mon."IdInterno"
		LEFT JOIN "BSCL.Orden.BD::ORD.Transaccion" AS TRA ON (TRA."IdTransaccion" = sim."IdTransaccion");		
		
	END;	
END;
GO