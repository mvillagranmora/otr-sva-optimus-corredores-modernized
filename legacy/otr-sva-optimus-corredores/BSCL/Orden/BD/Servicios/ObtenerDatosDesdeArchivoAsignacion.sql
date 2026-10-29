SET SCHEMA "BSCL";
GO
DROP PROCEDURE  "BSCL.Orden.BD.Servicios::ObtenerDatosDesdeArchivoAsignacion";
GO
CREATE PROCEDURE
--
--  Proposito: Servicsio para obtener desde un archivo los datos de las grillas Transacciones, Clientes/Ordenes y Asignaciones.
--  Autor: MMACHMAR - BCS
--  Fecha Creacion: 22/03/2016
--
"BSCL.Orden.BD.Servicios::ObtenerDatosDesdeArchivoAsignacion" (
	-- Parametros entrada comunes en todos los servicios privados
	IN p_IdEmpresa				BIGINT,			--ID Empresa destino
	IN p_IdModulo				NVARCHAR(3),	--ID del Modulo que lo invoca

	-- Parametros entrada del servicio privado
	IN p_IdTipoAsignacion		NVARCHAR(40),
	IN p_DataArchivo			TABLE (	"FOLIOTRANSACCION"	NVARCHAR(40),
										"INSTRUMENTO"		NVARCHAR(40),
										"DOCUMENTOIDENTIDAD"NVARCHAR(40),
										"PORTAFOLIO"		NVARCHAR(100),
										"ORDEN"				BIGINT,
										"CANTIDADASIGNADA"	DECIMAL(29,8),
										"MensajeValidacion" NVARCHAR(200)),
	
	-- Parametros salida del servicio privado
	OUT p_out_Transacciones		TABLE (	
		"IdEmpresa"									BIGINT,
		"IdTransaccion"								BIGINT,			"FolioTransaccion"								NVARCHAR(40),
		"FechaSistemaCreacion"						DATE,			"HoraSistemaCreacion"							TIME,
		"FechaOperacion"							TIMESTAMP,		"FechaOperacionTransaccion"						TIMESTAMP,
		"FechaLiquidacion"							DATE,			"FechaEmision"									DATE,
		"IdEmisor"									NVARCHAR(40),	"EsVentaCorta"									NVARCHAR(1),
		"FechaVencimiento"							DATE,			"IdSistemaBursatil"								BIGINT,
		"IndicadorOrdenDirecta"						NVARCHAR(2),	"EsCuentaPropia"								NVARCHAR(1),
		"EsFungible"								NVARCHAR(1),
		"EsAPlazo"									NVARCHAR(1),	"CodigoOperadorContraparte"						NVARCHAR(40),
		"CodigoOperadorCorredor"					NVARCHAR(40),	"CantidadTransaccion"							DECIMAL(26,8),
		"MontoTransaccion"							DECIMAL(26,8),	"PrecioTransaccion"								DECIMAL(26,8),
		"Tasa"										DECIMAL(26,8),	"Plazo"											BIGINT,
		"IdInstrumento"								BIGINT,
		"Instrumento"								NVARCHAR(40),	"IdMonedaInstrumento"							NVARCHAR(3),
		"IdInstrumentoSubyacente"					BIGINT,			"InstrumentoSubyacente"							NVARCHAR(40),
		"IdTipoInstrumentoSubyacente"				NVARCHAR(3),	"NombreTipoInstrumentoSubyacente"				NVARCHAR(100),
		"IdClasificacionTipoInstrumentoSubyacente"	NVARCHAR(3),	"NombreClasificacionTipoInstrumentoSubyacente"	NVARCHAR(100),
		"IdMonedaInstrumentoSubyacente"				NVARCHAR(3),	"IdTipoInstrumento"								NVARCHAR(3),
		"NombreTipoInstrumento"						NVARCHAR(100),	"IdClasificacionTipoInstrumento"				NVARCHAR(3),
		"NombreClasificacionTipoInstrumento"		NVARCHAR(100),	"ValorRescateTransaccion"						DECIMAL(26,8),
		"IdEstadoTransaccion"							NVARCHAR(3),
		"EstadoTransaccion"							NVARCHAR(100),	"IdTransaccionPlazo"							BIGINT,
		"IdTransaccionOd"							BIGINT,			"IdEmpleado"									BIGINT,
		"IdTipoOperacion"							NVARCHAR(6),	"NombreTipoOperacion"							NVARCHAR(100),
		"AccionOrden"						NVARCHAR(40),	"IdTipoProducto"								NVARCHAR(4),
		"IdCondicionDeLiquidacion"					NVARCHAR(2),	"NombreCondicionDeLiquidacion"					NVARCHAR(100),
		"FechaParidad"									DATE,
		"ValorParidad"								DECIMAL(26,8),	"FechaTipoCambio"								DATE,
		"ValorTipoCambio"							DECIMAL(26,8),	"IdCorredorContraparte"							BIGINT,
		"IdUsuarioReserva"							BIGINT,			"FechaIngresoOferta"							TIMESTAMP,
		"IdBolsa"									NVARCHAR(3),	"IdLibroOrdenes"								BIGINT,
		"NombreBolsa"								NVARCHAR(100),	"CantidadDisponibleTransaccion"					DECIMAL(29,8),
		"CantidadAsignadaTransaccion"				DECIMAL(29,8),
		"FechaModificacionOperacion"				TIMESTAMP,		"Reservado"										NVARCHAR(1),
		"Bloqueado"									NVARCHAR(1),	"IdInstrumentoUI"								BIGINT,
		"InstrumentoUI"								NVARCHAR(40),
		"NombreLibroOrdenes" 						NVARCHAR(100),	"ValorRescateAsignadoTransaccion"				DECIMAL(29,8),
		"CantidadSimulacionTransaccion"				DECIMAL(29,8),	"ValorRescateDisponibleTransaccion"				DECIMAL(26,8),
		"ValorRescateSimulacionTransaccion"			DECIMAL(26,8)),
	
	OUT p_out_Ordenes			TABLE (
		"IdOrden" 						BIGINT,			"FechaRecepcion"					TIMESTAMP,		"FechaIngresoRegistro"	TIMESTAMP,
		"FechaIngresoRegistroCorta"		DATE,			"FechaSolicitudAnulacion"			TIMESTAMP,		"FechaAnulacion"		TIMESTAMP,
		"FechaOperacionOrden"			TIMESTAMP,		"FechaVigencia"						DATE,			"FechaVencimiento"		DATE,
		"FechaEmision"					DATE,			"FechaLiquidacion"					DATE,			"FechaAceptacion"		SECONDDATE,
		"FechaAutorizacion"				DATE,			"IdTipoVigenciaOrden"				NVARCHAR(2),	"Glosa"					NVARCHAR(520),
		"IdEstadoOrden"					NVARCHAR(3),	"EstadoOrden"						NVARCHAR(100),	"CantidadOrden"			DECIMAL(26,8),
		"MontoOrden"					DECIMAL(26,8),	"IdFormaCustodia"		NVARCHAR(3),
		"PorcentajeVisibilidad"			NVARCHAR(100),	"ValorRescate"						DECIMAL(26,8),	"IdTipoReajuste"		NVARCHAR(3),
		"PrecioOrden"					DECIMAL(26,8),	"PrecioMercado"						DECIMAL(26,8),	"Tasa"					DECIMAL(26,8),
		"Plazo"								BIGINT,			"EsVentaCorta"			NVARCHAR(1),
		"IndicadorFacturacionInmediata"	NVARCHAR(1),	"IdTipoPrecio"						NVARCHAR(1),	"TipoPrecio"			NVARCHAR(100), 
		"IdTipoTasa"					NVARCHAR(1),	"ValorCaptacion"					DECIMAL(26,8),
		"IdCondicionDeLiquidacion"		NVARCHAR(2),	"NombreCondicionDeLiquidacion"		NVARCHAR(100),	"IdTipoOperacion"		NVARCHAR(6),
		"IdCliente" 					BIGINT, 		"RazonSocial"						NVARCHAR(100),	"NombreCompleto"		NVARCHAR(302),
		"DocumentoIdentidad"			NVARCHAR(40),	"IdPortafolio"						BIGINT,			"CodigoPortafolio"		NVARCHAR(100),
		"IdGrupoCobro"					BIGINT,			"IdTipoCanalIngreso"				NVARCHAR(3),	"NombreCanal"			NVARCHAR(100),
		"IdLibroOrdenes"				BIGINT,			"IdEmisor"							NVARCHAR(40),	"IdEmpresa"				BIGINT,
		"IdBolsa"						NVARCHAR(3),	"NombreBolsa"						NVARCHAR(100),	"IdInstrumento"			BIGINT,
		"Instrumento"					NVARCHAR(40),	"IdTipoInstrumento"					NVARCHAR(3),	"NombreTipoInstrumento"	NVARCHAR(100),
		"IdClasificacionTipoInstrumento"NVARCHAR(3),	"NombreClasificacionTipoInstrumento"NVARCHAR(100),
		"IdInstrumentoSubyacente"		BIGINT,			"InstrumentoSubyacente"				NVARCHAR(40),
		"IdTipoInstrumentoSubyacente"	NVARCHAR(3),	"NombreTipoInstrumentoSubyacente"	NVARCHAR(100),
		"IdClasificacionTipoInstrumentoSubyacente"		NVARCHAR(3), "NombreClasificacionTipoInstrumentoSubyacente"					NVARCHAR(100),
		"IdEstadoAsignacion"			NVARCHAR(3),	"EstadoOrdenAsignacion"				NVARCHAR(100),	"Tamano"				BIGINT,
		"IdMonedaInstrumento"			NVARCHAR(3),	"IdMonedaOrden"						NVARCHAR(3),	"NombreTipoOperacion"	NVARCHAR(100),
		"AccionOrden"			NVARCHAR(40),	"NombreLibroOrdenes"				NVARCHAR(100),	"IdInstrumentoUI"		BIGINT,
		"InstrumentoUI"					NVARCHAR(40),	"CantidadDisponibleOrden"			DECIMAL(29,8),	"CantidadAsignadaOrden"	DECIMAL(29,8),
		"CantidadAsignadaArchivo"			DECIMAL(29,8),
		"CantidadSimulacionOrden"		DECIMAL(29,8)),
	
	OUT p_out_Clientes			TABLE (
		"IdLibroOrdenes" 		BIGINT,			"IdCliente"					BIGINT,			"IdEstado"					NVARCHAR(3),
		"IdPersona"				BIGINT,			"DocumentoIdentidad"		NVARCHAR(40),	"TipoDocumentoIdentidad"	NVARCHAR(1),
		"RazonSocial"			NVARCHAR(100),	"NombreCompleto"			NVARCHAR(302),
		"IdPortafolio"				BIGINT,
		"CodigoPortafolio"		NVARCHAR(100),	"Proposito"					NVARCHAR(520),	"IdEstadoPortafolio"		NVARCHAR(3),
		"DescripcionPortafolio"	NVARCHAR(100),	
		"CantidadSimulacionCliente" DECIMAL(29,8), "IdMoneda"				NVARCHAR(3), 	"ValorRescateSimulacionCliente" DECIMAL(26,8)),
	
	OUT p_out_Asignacion	TABLE (
		"FechaSimulacion"			TIMESTAMP,		"FechaIngresoRegistro"	TIMESTAMP,		"IdTransaccion"			BIGINT,
		"FolioTransaccion"			NVARCHAR(40),	"IdPortafolio"			BIGINT,			"CodigoPortafolio"		NVARCHAR(100),
		"IdCliente"					BIGINT,			"IdLibroOrdenes"		BIGINT,			"IdInstrumento"			BIGINT,
		"Instrumento"				NVARCHAR(40),	"IdTipoInstrumento"		NVARCHAR(3),	"IdMonedaInstrumento"	NVARCHAR(3),
		"IdOrden"					BIGINT,			"DocumentoIdentidad"	NVARCHAR(40),	"CantidadAsignacion"	DECIMAL(29,8),
		"ValorRescateAsignacion"	DECIMAL(26,8),	"PorcentajeAsignacion"	DECIMAL(26,8),	"PrecioSimulacion"		DECIMAL(26,8),
		"CantidadAsignadaSimulacion"DECIMAL(29,8),	"ValorRescateAsignadoSimulacion" DECIMAL(26,8),"PrecioPromedio" DECIMAL(26,8)),
	
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador  	 	int,
	OUT p_out_AdvertenciasDetalle  	 	 	table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)),
	OUT p_out_ErroresContador  	 	 	 	int,
	OUT p_out_ErroresDetalle  	 	 	 	table ("Codigo" nvarchar(40),"Descripcion" nvarchar(5000))
)

LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA "BSCL"
AS
BEGIN

	--------------------
	--DECLARE Manejo de Errores
	--------------------
	DECLARE SALIDA_CONTROLADA CONDITION;
	DECLARE v_errorcuenta INT DEFAULT 0;
	DECLARE v_errorhashmsg table ("Codigo" nvarchar(40),"Valor" nvarchar(520));
	DECLARE v_erroroutmsg  nvarchar(520);
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA
	BEGIN
		--salida/error/advertencia controlado por este servicio
		select count(1) into p_out_ErroresContador
		from :p_out_ErroresDetalle;
		select count(1) into p_out_AdvertenciasContador
		from :p_out_AdvertenciasDetalle;
	END;

	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt nvarchar(40);
		IF ::SQL_ERROR_CODE > 10000 THEN
			--error pseudo-controlado pero desde un Sub-SP
			call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);
			p_out_ErroresDetalle = select * from :p_out_ErroresDetalle UNION ALL
								   select v_errorExt,::SQL_ERROR_MESSAGE from dummy;
			select count(1) into p_out_ErroresContador
			from :p_out_ErroresDetalle;
		ELSE
			--Error interno no manejado  (El log será captado por la capa de XS control)
			RESIGNAL;
		END IF;
	END;
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;

	BEGIN
		--------------------
		--Variables
		--------------------
		DECLARE	v_FechaSistemaCreacion	TIMESTAMP;
		DECLARE	v_countTransacciones	BIGINT;
		DECLARE	v_countOrdenes			BIGINT;
		DECLARE	v_countClientes			BIGINT;
		DECLARE	v_countAsignacion		BIGINT;
		
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema" (:p_IdEmpresa,v_FechaSistemaCreacion);
		
		
		---------- Consulta Transacciones ----------
		v_Transacciones = SELECT
							tra."IdEmpresa",			 	tra."IdTransaccion",				tra."FolioTransaccion", 
							tra."FechaSistemaCreacion",		tra."HoraSistemaCreacion",			tra."FechaOperacion",
							tra."FechaOperacionTransaccion",tra."FechaLiquidacion",				tra."FechaEmision",
							tra."IdEmisor",				 	tra."EsVentaCorta",					tra."FechaVencimiento",
							tra."IdSistemaBursatil",	 	tra."IndicadorOrdenDirecta",		tra."EsCuentaPropia",
							tra."EsFungible",			 	tra."EsAPlazo", 
							tra."CodigoOperadorContraparte", tra."CodigoOperadorCorredor",			tra."CantidadTransaccion",
							tra."MontoTransaccion",		 	tra."PrecioTransaccion",			tra."Tasa",
							tra."Plazo",				 	tra."IdInstrumento",				
							tra."Instrumento",			 	tra."IdMonedaInstrumento",			tra."IdInstrumentoSubyacente",
							tra."InstrumentoSubyacente",	tra."IdTipoInstrumentoSubyacente",	tra."NombreTipoInstrumentoSubyacente",
							tra."IdClasificacionTipoInstrumentoSubyacente",	tra."NombreClasificacionTipoInstrumentoSubyacente",
							tra."IdMonedaInstrumentoSubyacente",			tra."IdTipoInstrumento",
							tra."NombreTipoInstrumento",					tra."IdClasificacionTipoInstrumento",
							tra."NombreClasificacionTipoInstrumento",		tra."ValorRescateTransaccion",
							tra."IdEstadoTransaccion",		tra."EstadoTransaccion", 
							tra."IdTransaccionPlazo",			tra."IdTransaccionOd",			tra."IdEmpleado",
							tra."IdTipoOperacion", 				tra."NombreTipoOperacion",		tra."AccionOrden",
							tra."IdTipoProducto", 				tra."IdCondicionDeLiquidacion",	tra."NombreCondicionDeLiquidacion",	
							tra."FechaParidad",				tra."ValorParidad",
							tra."FechaTipoCambio",				tra."ValorTipoCambio",			tra."IdCorredorContraparte",
							tra."IdUsuarioReserva",				tra."FechaIngresoOferta",		tra."IdBolsa",
							tra."IdLibroOrdenes", 				tra."NombreBolsa",				tra."CantidadDisponibleTransaccion",
							tra."CantidadAsignadaTransaccion",
							tra."FechaModificacionOperacion",	tra."Reservado",				tra."Bloqueado",
							tra."IdInstrumentoUI",				tra."InstrumentoUI",
							tra."NombreLibroOrdenes",			tra."ValorRescateAsignadoTransaccion",
							p_da."CANTIDADASIGNADA" AS "CantidadSimulacionTransaccion",
							cast(0 as DECIMAL(26,8)) AS "ValorRescateDisponibleTransaccion",
							cast(0 as DECIMAL(26,8)) AS "ValorRescateSimulacionTransaccion"
						  FROM "BSCL.Orden.BD.Vistas::ConsultarTransaccionUI" AS tra
						  INNER JOIN :p_DataArchivo p_da ON (
						  	tra."IdEmpresa"			= p_IdEmpresa				AND
						 	tra."FolioTransaccion"	= p_da."FOLIOTRANSACCION"	AND
						 	tra."Instrumento"		= p_da."INSTRUMENTO"		AND
						 	p_da."MensajeValidacion"= 'OK');
		
		
		IF (p_IdTipoAsignacion = 'ASIGNACION_MANUAL') THEN
			---------- Consulta Ordenes ---------
			v_Ordenes =	SELECT
							ord."IdOrden",								ord."FechaRecepcion",			ord."FechaIngresoRegistro", 
							ord."FechaIngresoRegistroCorta",			ord."FechaSolicitudAnulacion",	ord."FechaAnulacion", 
							ord."FechaOperacionOrden",					ord."FechaVigencia",			ord."FechaVencimiento", 
							ord."FechaEmision",							ord."FechaLiquidacion",			ord."FechaAceptacion", 
							ord."FechaAutorizacion",					ord."IdTipoVigenciaOrden",		ord."GlosaOrden" as "Glosa", 
							ord."IdEstadoOrden",						ord."EstadoOrden",				ord."CantidadOrden", 
							ord."MontoOrden",							ord."IdFormaCustodia", 
							ord."PorcentajeVisibilidad",				ord."ValorRescate",				ord."IdTipoReajuste",
							ord."PrecioOrden",							ord."PrecioMercado",			0 AS "Tasa", 
							0 AS "Plazo",								ord."EsVentaCorta",
							ord."IndicadorFacturacionInmediata",		ord."IdTipoPrecio",				ord."TipoPrecio", 
							ord."IdTipoTasa",							ord."ValorCaptacion",			ord."IdCondicionDeLiquidacion", 
							ord."NombreCondicionDeLiquidacion",			ord."IdTipoOperacion",			ord."IdCliente", 
							ord."RazonSocial",							ord."NombreCompleto",			ord."DocumentoIdentidad", 
							ord."IdPortafolio",							ord."CodigoPortafolio",			ord."IdGrupoCobro", 
							ord."IdTipoCanalIngreso",					ord."NombreCanal",				ord."IdLibroOrdenes", 
							ord."IdEmisor",								ord."IdEmpresa",				ord."IdBolsa", 
							ord."NombreBolsa",							ord."IdInstrumento",			ord."Instrumento", 
							ord."IdTipoInstrumento",					ord."NombreTipoInstrumento",
							ord."IdClasificacionTipoInstrumento",		ord."NombreClasificacionTipoInstrumento",
							ord."IdInstrumentoSubyacente",				ord."InstrumentoSubyacente",
							ord."IdTipoInstrumentoSubyacente",			ord."NombreTipoInstrumentoSubyacente",
							ord."IdClasificacionTipoInstrumentoSubyacente", ord."NombreClasificacionTipoInstrumentoSubyacente",
							ord."IdEstadoAsignacion",
							ord."EstadoOrdenAsignacion",				ord."Tamano",					ord."IdMonedaInstrumento",
							ord."IdMonedaOrden",						
							ord."NombreTipoOperacion",					ord."AccionOrden",
							ord."NombreLibroOrdenes",					ord."IdInstrumentoUI",			ord."InstrumentoUI",
							ord."CantidadDisponibleOrden",				ord."CantidadAsignadaOrden",
							p_da."CANTIDADASIGNADA" AS "CantidadAsignadaArchivo",
							--cast(0 as DECIMAL(26,8)) AS "CantidadSimulacionOrden"
							p_da."CANTIDADASIGNADA" AS "CantidadSimulacionOrden"
						FROM "BSCL.Orden.BD.Vistas::ConsultarOrdenPorAsignar" AS ord
						INNER JOIN :p_DataArchivo p_da ON (
							ord."IdEmpresa"			= p_IdEmpresa		AND
							ord."IdOrden"			= p_da."ORDEN"		AND
							ord."Instrumento"		= p_da."INSTRUMENTO"AND
							ord."CodigoPortafolio"	= p_da."PORTAFOLIO" AND
							ord."DocumentoIdentidad"= p_da."DOCUMENTOIDENTIDAD" AND
							p_da."MensajeValidacion"= 'OK');
			
			
			---------- Consulta Asignacion ---------
			v_Asignacion =	SELECT
								v_FechaSistemaCreacion			as "FechaSimulacion",		CURRENT_UTCTIMESTAMP 		as "FechaIngresoRegistro",
								tra."IdTransaccion"				as "IdTransaccion",			tra."FolioTransaccion"		as "FolioTransaccion",
								ord."IdPortafolio"				as "IdPortafolio",			ord."CodigoPortafolio"		as "CodigoPortafolio",
								ord."IdCliente"					as "IdCliente",				ord."IdLibroOrdenes"		as "IdLibroOrdenes",
								ord."IdInstrumento"				as "IdInstrumento",			ord."Instrumento"			as "Instrumento",
								ord."IdTipoInstrumento"			as "IdTipoInstrumento",		ord."IdMonedaInstrumento"	as "IdMonedaInstrumento",
								ord."IdOrden"					as "IdOrden",				ord."DocumentoIdentidad"	as "DocumentoIdentidad",
								ord."CantidadAsignadaArchivo"	as "CantidadAsignacion",	cast(0 as DECIMAL(26,8))	as "ValorRescateAsignacion",
								cast(0 as DECIMAL(26,8))		as "PorcentajeAsignacion",	tra."PrecioTransaccion"		as "PrecioSimulacion",
								tra."CantidadSimulacionTransaccion" as"CantidadAsignadaSimulacion",
								cast(0 as DECIMAL(26,8)) 		as "ValorRescateAsignadoSimulacion",
								cast(0 as DECIMAL(26,8))		as "PrecioPromedio"
							FROM :p_DataArchivo AS p_da
							LEFT JOIN :v_Transacciones	AS tra ON (p_da."FOLIOTRANSACCION"	= tra."FolioTransaccion")
							LEFT JOIN :v_Ordenes 		AS ord ON (p_da."ORDEN"				= ord."IdOrden")
							WHERE
								tra."IdLibroOrdenes"		= ord."IdLibroOrdenes"		AND
								tra."IdInstrumento"			= ord."IdInstrumento"		AND
								tra."IdTipoInstrumento"		= ord."IdTipoInstrumento"	AND
								tra."IdMonedaInstrumento"	= ord."IdMonedaInstrumento"
							ORDER BY tra."IdTransaccion";
			
		ELSEIF (p_IdTipoAsignacion = 'ASIGNACION_POSTRERA') THEN
			---------- Consulta Clientes ---------
			v_Clientes = SELECT
							cli."IdLibroOrdenes",			cli."IdCliente",				cli."IdEstado", 
							cli."IdPersona",				cli."DocumentoIdentidad",		cli."TipoDocumentoIdentidad", 
							cli."RazonSocial",				cli."NombreCompleto",			
							cli."IdPortafolio", 
							cli."CodigoPortafolio",			cli."Proposito",				cli."IdEstadoPortafolio", 
							cli."DescripcionPortafolio",
							
							p_da."CANTIDADASIGNADA" AS "CantidadSimulacionCliente",
							'' AS "IdMoneda", --dejar el campo correcto
							cast(0 as DECIMAL(26,8)) 		as "ValorRescateSimulacionCliente"
						FROM "BSCL.Orden.BD.Vistas::ConsultarClientesLibroOrdenes" AS cli
						INNER JOIN :p_DataArchivo p_da ON (
							cli."CodigoPortafolio"	= p_da."PORTAFOLIO"			AND
							cli."DocumentoIdentidad"= p_da."DOCUMENTOIDENTIDAD" AND
							p_da."MensajeValidacion"= 'OK');
			
			---------- Consulta Asignacion ---------
			v_Asignacion =	SELECT DISTINCT
								v_FechaSistemaCreacion			as "FechaSimulacion",		CURRENT_UTCTIMESTAMP 		as "FechaIngresoRegistro",
								tra."IdTransaccion"				as "IdTransaccion",			tra."FolioTransaccion"		as "FolioTransaccion",
								cli."IdPortafolio"				as "IdPortafolio",			cli."CodigoPortafolio"		as "CodigoPortafolio",
								cli."IdCliente"					as "IdCliente",				tra."IdLibroOrdenes"		as "IdLibroOrdenes",
								tra."IdInstrumento"				as "IdInstrumento",			tra."Instrumento"			as "Instrumento",
								tra."IdTipoInstrumento"			as "IdTipoInstrumento",		tra."IdMonedaInstrumento"	as "IdMonedaInstrumento",
								cast(0 as BIGINT)				as "IdOrden",				cli."DocumentoIdentidad"	as "DocumentoIdentidad",
								tra."CantidadAsignadaTransaccion"	as "CantidadAsignacion",cast(0 as DECIMAL(26,8))	as "ValorRescateAsignacion",
								cast(0 as DECIMAL(26,8))		as "PorcentajeAsignacion",	cast(0 as DECIMAL(26,8))	as "PrecioSimulacion",
								tra."CantidadSimulacionTransaccion" as"CantidadAsignadaSimulacion",
								cast(0 as DECIMAL(26,8))		as "ValorRescateAsignadoSimulacion",
								cast(0 as DECIMAL(26,8))		as "PrecioPromedio"
							FROM :p_DataArchivo AS p_da
							LEFT JOIN :v_Transacciones	AS tra ON (p_da."FOLIOTRANSACCION"	= tra."FolioTransaccion")
							LEFT JOIN :v_Clientes 		AS cli ON (
								p_da."PORTAFOLIO"			= cli."CodigoPortafolio" AND
								p_da."DOCUMENTOIDENTIDAD"	= cli."DocumentoIdentidad")
							WHERE
								tra."IdLibroOrdenes" = cli."IdLibroOrdenes"
							ORDER BY tra."IdTransaccion";
		END IF;
		
		--------------------
		--Validaciones
		--------------------
		
		BEGIN
			-- Verifica existan Transacciones
			SELECT COUNT(1) INTO v_countTransacciones FROM :v_Transacciones;
			IF(v_countTransacciones = 0) THEN
				p_out_ErroresDetalle = select 'ARCHIVO_NO_ENTREGA_TRANSACCIONES' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
				SIGNAL SALIDA_CONTROLADA;
			END IF;
			
			IF (p_IdTipoAsignacion = 'ASIGNACION_MANUAL') THEN
				-- Verifica existan Ordenes
				SELECT COUNT(1) INTO v_countOrdenes FROM :v_Ordenes;
				IF(v_countOrdenes = 0) THEN
					p_out_ErroresDetalle = select 'ARCHIVO_NO_ENTREGA_ORDENES' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
					SIGNAL SALIDA_CONTROLADA;
				END IF;
			ELSEIF (p_IdTipoAsignacion = 'ASIGNACION_POSTRERA') THEN
				-- Verifica existan Clientes
				SELECT COUNT(1) INTO v_countClientes FROM :v_Clientes;
				IF(v_countClientes = 0) THEN
					p_out_ErroresDetalle = select 'ARCHIVO_NO_ENTREGA_CLIENTES' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
					SIGNAL SALIDA_CONTROLADA;
				END IF;
			END IF;
			
			-- Verifica exista Asignacion
			SELECT COUNT(1) INTO v_countAsignacion FROM :v_Asignacion;
			IF(v_countAsignacion = 0) THEN
				p_out_ErroresDetalle = select 'ARCHIVO_NO_ENTREGA_ASIGNACION' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
				SIGNAL SALIDA_CONTROLADA;
			END IF;
		END;
		
		--------------------
		--Reglas Negocio 
		--------------------		
		
		--------------------
		--Lógica de Negocio 
		--------------------
		BEGIN
			p_out_Transacciones = SELECT * FROM :v_Transacciones;
			
			IF (p_IdTipoAsignacion = 'ASIGNACION_MANUAL') THEN
				p_out_Ordenes	= SELECT * FROM :v_Ordenes;
			ELSEIF (p_IdTipoAsignacion = 'ASIGNACION_POSTRERA') THEN
				p_out_Clientes	= SELECT * FROM :v_Clientes;
			END IF;
			
			p_out_Asignacion = SELECT * FROM :v_Asignacion;
		END;
		
		--------------------
		--FIN SP 
		--------------------
	END;
END;
GO