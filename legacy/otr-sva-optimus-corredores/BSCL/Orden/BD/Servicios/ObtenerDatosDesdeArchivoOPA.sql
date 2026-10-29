SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Orden.BD.Servicios::ObtenerDatosDesdeArchivoOPA";

CREATE PROCEDURE
--
--  Proposito: Servicio para cargar datos de opas
--  Autor: TEAM ORDENES F**K YEAH
--  Fecha Creacion: 22/03/2016
--
"BSCL.Orden.BD.Servicios::ObtenerDatosDesdeArchivoOPA" (
	-- Parametros entrada comunes en todos los servicios privados
	IN p_IdEmpresa				BIGINT,			--ID Empresa destino
	IN p_IdUsuario				BIGINT,
	IN p_IdModulo				NVARCHAR(3),	--ID del Modulo que lo invoca
	IN p_IdTransaccion			BIGINT,
	-- Parametros entrada del servicio privado
	IN p_DataArchivo			TABLE (	"IdInterno"				BIGINT,	
										"OPARUT"				NVARCHAR(40),
										"OPACODIGOPORTAFOLIO"	NVARCHAR(40),
										"OPACANTIDAD"			DECIMAL(26,8)
										),
										
	OUT p_out_Clientes			TABLE (
		"IdInterno" 						bigint,
		"IdCliente"							BIGINT,
		"IdPortafolio"						BIGINT,
		"NombreCompleto"					NVARCHAR(302),	
		"DocumentoIdentidad"				NVARCHAR(40),	
		"CodigoPortafolio"					NVARCHAR(100),	
		"IdMoneda"							NVARCHAR(3), 	
		"CantidadSimulacionCliente" 		DECIMAL(29,8), 
		"ValorRescateSimulacionCliente" 	DECIMAL(26,8),
		"PorcentajeSimulacion" 				DECIMAL(26,8)
		),
	
	OUT p_out_SimAsignacion		TABLE (		
									"IdInterno"				BIGINT,
									"IdMovimiento"			BIGINT,
									"FechaSimulacion"		TIMESTAMP,
									"FechaIngresoRegistro"	TIMESTAMP,
									"IdTransaccion"			BIGINT,
									"FolioTransaccion"		NVARCHAR(40),
									"FolioCartera"			BIGINT,
									"IdPortafolio"			BIGINT,
									"CodigoPortafolio"		NVARCHAR(100),
									"IdCliente"				BIGINT,
									"IdLibroOrdenes"		BIGINT,
									"IdInstrumento"			BIGINT,
									"Instrumento"			NVARCHAR(40),
									"IdTipoInstrumento"		NVARCHAR(3),
									"IdMonedaInstrumento"	NVARCHAR(3),
									"IdOrden"				BIGINT,
									"DocumentoIdentidad"	NVARCHAR(40),
									"CantidadAsignacion"	DECIMAL(26,8),
									"ValorRescateAsignacion"DECIMAL(26,8),
									"PorcentajeAsignacion"	DECIMAL(26,8),
									"PrecioSimulacion"		DECIMAL(26,8),
									"IdTipoProducto"		NVARCHAR(4),
									"IdTipoOperacion"		NVARCHAR(6),
									"IdTipoCanalIngreso"	NVARCHAR(3),
									"IdBolsa"				NVARCHAR(3),
									"IdFormaCustodia"		NVARCHAR(3),
									"Glosa"					NVARCHAR(500),
									"TasaVentaCorta"		NVARCHAR(500),
									"PlazoVentaCorta"		NVARCHAR(500)),
		
	OUT p_out_cobrosAsignacion	TABLE(
									"IdInterno"					BIGINT,
									"IdInternoCobro"			BIGINT,
									"IdPortafolio"				BIGINT,
									"IdBolsa" 					NVARCHAR(10),
									"IdTipoProducto"			NVARCHAR(4),
									"IdTipoOperacion"			NVARCHAR(6),
									"IdTipoCanalIngreso"		NVARCHAR(4),
									"IdCobro"					NVARCHAR(10),
									"ValorCobro"				DECIMAL(26,8),
									"FormaCobro"				NVARCHAR(1),
									"ValorTopeMinimoOrden"		DECIMAL(26,8),
									"IdMoneda"					NVARCHAR(3),
									"CodigoCobroClasificacion"	NVARCHAR(40),
									"PermiteCambiarValor"		NVARCHAR(1)),
	
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador  	 	int,
	OUT p_out_AdvertenciasDetalle  	 	 	table ("IdInterno"	BIGINT,"Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)),
	OUT p_out_ErroresContador  	 	 	 	int,
	OUT p_out_ErroresDetalle  	 	 	 	table ("IdInterno"	BIGINT,"Codigo" nvarchar(40),"Descripcion" nvarchar(5000))
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
	DECLARE v_out_ErroresDetalle  		TABLE ("IdInterno" BIGINT,"Codigo" nvarchar(100),"CodigoDato1" nvarchar(100), "ValorDato1" nvarchar(100)) DEFAULT
										SELECT 0 as "IdInterno",'' as "Codigo",'' as "CodigoDato1",'' as "ValorDato1" from dummy where 1=2;
	DECLARE v_out_AdvertenciasDetalle  	TABLE ("IdInterno" BIGINT,"Codigo" nvarchar(100),"RequiereAutorizacion" NVARCHAR(1),"CodigoDato1" nvarchar(100), "ValorDato1" nvarchar(100)) DEFAULT
										SELECT 0 as "IdInterno",'' as "Codigo",'N' as "RequiereAutorizacion",'' as "CodigoDato1",'' as "ValorDato1" from dummy where 1=2;
	DECLARE v_out_AdvertenciasContador_matriz,v_out_ErroresContador_matriz INT;
	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA
	BEGIN
		--salida/error/advertencia controlado por este servicio
		select count(1) into p_out_ErroresContador 
		from :v_out_ErroresDetalle;
		select count(1) into p_out_AdvertenciasContador 
		from :v_out_AdvertenciasDetalle;
		
		v_TablaCodigosMensajes = SELECT "IdInterno","Codigo",
								"CodigoDato1",
								"ValorDato1" ,
								'' as "CodigoDato2",
								'' as "ValorDato2" 
								FROM :v_out_ErroresDetalle
								UNION ALL
								SELECT "IdInterno","Codigo",
								"CodigoDato1",
								"ValorDato1",
								'' as "CodigoDato2",
								'' as "ValorDato2"  
								FROM :v_out_AdvertenciasDetalle;
		
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_TablaCodigosMensajes,v_out_ErroresDetalleMensajes);
		
		p_out_ErroresDetalle = 	SELECT errores."IdInterno",errores."Codigo" as "Codigo", msj."Descripcion"
								FROM :v_out_ErroresDetalle errores
								INNER JOIN :v_out_ErroresDetalleMensajes msj on errores."IdInterno" = msj."IdInterno" and errores."Codigo" = msj."Codigo";
								
		p_out_AdvertenciasDetalle = SELECT adv."IdInterno",adv."Codigo" as "Codigo",
									adv."RequiereAutorizacion",
									msj."Descripcion"
								FROM :v_out_AdvertenciasDetalle adv
								INNER JOIN :v_out_ErroresDetalleMensajes msj on adv."IdInterno" = msj."IdInterno" and adv."Codigo" = msj."Codigo";
				 					 	
	END;
	
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;

	BEGIN
	
	DECLARE	v_FechaSistemaCreacion TIMESTAMP;
	
	CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema" (:p_IdEmpresa,v_FechaSistemaCreacion);
	
	v_transaccion = select * from "BSCL.Orden.BD.Vistas::ConsultarTransaccion" where "IdTransaccion" = p_IdTransaccion;
	
	v_Clientes_base = 
		select data."IdInterno",data."OPARUT",data."OPACODIGOPORTAFOLIO", doc."IdPersona", cli."IdCliente",port."IdPortafolio",cli."NombreCompleto",doc."DocumentoIdentidad",port."CodigoPortafolio",
			'CLP' as "IdMoneda",data."OPACANTIDAD" as "CantidadSimulacionCliente",0.0 as "ValorRescateSimulacionCliente",0.0 as "PorcentajeSimulacion"
		from :p_DataArchivo data
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDocumentoIdentidad" doc on doc."NombreTipoDocumentoIdentidad" = 'RUT' and doc."DocumentoIdentidad" = LTRIM(RTRIM(data."OPARUT"))
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" cli on doc."IdPersona" = cli."IdPersona"
		LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" port on cli."IdCliente" = port."IdCliente" and port."CodigoPortafolio" = LTRIM(RTRIM(data."OPACODIGOPORTAFOLIO"));
	
	v_out_ErroresDetalle =  select "IdInterno",'CLIENTE_NO_EXISTE' AS "Codigo", 'IdCliente' as "CodigoDato1" ,"OPARUT" as "ValorDato1"
							from :v_Clientes_base where "IdPersona" is null or "IdCliente" is null
							UNION ALL
							select "IdInterno",'PORTAFOLIO_NO_EXISTE' AS "Codigo", 'IdPortafolio' as "CodigoDato1" ,"OPARUT" || '-' || "OPACODIGOPORTAFOLIO" as "ValorDato1"
							from :v_Clientes_base where "IdPersona" is not null and "IdCliente" is not null and "IdPortafolio" is null;

	p_out_Clientes = 	select "IdInterno","IdCliente", "IdPortafolio", "NombreCompleto", "DocumentoIdentidad", "CodigoPortafolio", "IdMoneda",  
								"CantidadSimulacionCliente" ,"ValorRescateSimulacionCliente", "PorcentajeSimulacion"
						from :v_Clientes_base where "IdCliente" is not null and "IdPortafolio" is not null;

	v_out_Asignacion = select 	tran."IdTransaccion" as "IdMovimiento",
								:v_FechaSistemaCreacion AS "FechaSimulacion",
								CURRENT_TIMESTAMP AS "FechaIngresoRegistro",
								tran."IdTransaccion" as "IdTransaccion", 
								tran."FolioTransaccion" as "FolioTransaccion",
								0 as "FolioCartera",
								cli."IdPortafolio" as "IdPortafolio",
								cli."CodigoPortafolio" as "CodigoPortafolio",
								cli."IdCliente" as "IdCliente",
								tran."IdLibroOrdenes" as "IdLibroOrdenes",
								tran."IdInstrumento" as "IdInstrumento",
								tran."Instrumento" as "Instrumento",
								tran."IdTipoInstrumento" as "IdTipoInstrumento",
								tran."IdMonedaInstrumento" as "IdMonedaInstrumento",
								0 as "IdOrden",
								cli."DocumentoIdentidad" as "DocumentoIdentidad", 
								cli."CantidadSimulacionCliente" as "CantidadAsignacion",
								0 as "ValorRescateAsignacion",
								0 as "PorcentajeAsignacion",
								0 as "PrecioSimulacion",
								tran."IdTipoProducto",
								tran."IdTipoOperacion",
								'TRA' as "IdTipoCanalIngreso",
								tran."IdBolsa",
								'CUS' as "IdFormaCustodia",
								'' as "Glosa",
								0 as "TasaVentaCorta",
								0 as "PlazoVentaCorta"
								
								from :v_transaccion tran,:p_out_Clientes cli;
	
	p_out_SimAsignacion = select row_number() over (order by "IdTransaccion") as "IdInterno",asig.*
								from :v_out_Asignacion asig;
								
								
	v_in_Portafolios = SELECT "IdInterno","IdPortafolio","IdTipoProducto","IdTipoOperacion","IdTipoCanalIngreso" as "IdCanalIngreso","IdBolsa",null as "IdCorredorCorresponsal" 
						FROM :p_out_SimAsignacion;
	
	SELECT 'v_in_Portafolios',* FROM :v_in_Portafolios;
				
	CALL "BSCL.Orden.BD.Servicios::ConsultarCobrosPortafolios"(
		p_IdEmpresa				=> :p_IdEmpresa,
		p_IdUsuario				=> :p_IdUsuario,
		p_IdModulo				=> :p_IdModulo,
		p_Portafolios			=> :v_in_Portafolios,
		p_out_MatrizOperacional	=> v_out_MatrizOperacional,
		
		p_out_AdvertenciasContador	=> v_out_AdvertenciasContador_matriz,
		p_out_AdvertenciasDetalle	=> v_out_AdvertenciasDetalle_matriz,
		p_out_ErroresContador		=> v_out_ErroresContador_matriz,
		p_out_ErroresDetalle		=> v_out_ErroresDetalle_matriz);
	
	SELECT 'v_out_MatrizOperacional',* FROM :v_out_MatrizOperacional;
							 
	p_out_cobrosAsignacion = select asig."IdInterno" , row_number() OVER (order by asig."IdInterno","CodigoCobroClasificacion","IdCobro") as "IdInternoCobro" ,asig."IdPortafolio", asig."IdBolsa" ,asig."IdTipoProducto" ,asig."IdTipoOperacion",asig."IdTipoCanalIngreso","IdCobro","ValorCobro",
								"FormaCobro","ValorTopeMinimoOrden","IdMoneda", "CodigoCobroClasificacion" ,"PermiteCambiarValor"
								from :p_out_SimAsignacion as asig 
								inner join :v_out_MatrizOperacional mat 
								on asig."IdInterno" = mat."IdInterno";

	SIGNAL SALIDA_CONTROLADA;
	
	END;
END;
