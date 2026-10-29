SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.CanalAgente.BD.Servicios::ConsultaAvanzadaClientes";

CREATE PROCEDURE

"BSCL.CanalAgente.BD.Servicios::ConsultaAvanzadaClientes" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa				BIGINT,			--ID Empresa destino
	IN  p_IdUsuario				BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo				NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN	p_IdEmpleadoConsulta	BIGINT,
	
	IN	p_Nombre					NVARCHAR(5000)	DEFAULT NULL,
	IN	p_DocumentoIdentidad		NVARCHAR(40)	DEFAULT NULL,
	IN  p_IdTipoPersona				NVARCHAR(2)		DEFAULT NULL,
	IN  p_IdTipoCliente				NVARCHAR(40)	DEFAULT NULL,
	IN	p_IdSegmentoCliente			BIGINT			DEFAULT NULL,
	IN	p_IdEstadoCliente			NVARCHAR(3)		DEFAULT NULL,
	IN	p_IdTipoBloqueoCliente		NVARCHAR(40)	DEFAULT NULL,
	IN	p_IdTipoBloqueoPortafolio	NVARCHAR(40)	DEFAULT NULL,
	IN	p_IdEmpleadoFiltro			BIGINT			DEFAULT NULL,
	IN	p_IdCategoriaInversionista	NVARCHAR(40)	DEFAULT NULL,
	IN	p_IdPropositoPortafolio		NVARCHAR(40)	DEFAULT NULL,
	IN	p_IdEstadoPortafolio		NVARCHAR(3)		DEFAULT NULL,
	IN	p_IdPerfilComercial			NVARCHAR(40)	DEFAULT NULL,
	IN	p_IdPerfilProductos			NVARCHAR(40)	DEFAULT NULL,
	IN	p_IdPerfilCobros			NVARCHAR(40)	DEFAULT NULL,
	IN	p_FechaNacimientoDesde		NVARCHAR(10)	DEFAULT '0001-01-01',
	IN	p_FechaNacimientoHasta		NVARCHAR(10)	DEFAULT '0001-01-01',
	IN	p_FechaCreacionDesde		SECONDDATE		DEFAULT '0001-01-01 00:00:00',
	IN	p_FechaCreacionHasta		SECONDDATE		DEFAULT '0001-01-01 00:00:00',
	IN	p_FechaDefuncionDesde		SECONDDATE		DEFAULT '0001-01-01 00:00:00',
	IN	p_FechaDefuncionHasta		SECONDDATE		DEFAULT '0001-01-01 00:00:00',
	IN	p_AliasPortafolio			NVARCHAR(255)	DEFAULT NULL,
	
	IN	p_BuscaPorFlujoCaja			NVARCHAR(1)		DEFAULT 'N',
	IN	p_UsoFlujoCaja				NVARCHAR(40)	DEFAULT NULL,
	IN	p_SaldoFlujoCaja			DECIMAL(26,8) 	DEFAULT NULL,
	IN	p_IdMonedaFlujoCaja			NVARCHAR(3)		DEFAULT NULL,
	IN	p_CriterioFlujoCaja			NVARCHAR(40)	DEFAULT NULL, --MAYOR QUE, MENOR QUE
	
	IN	p_BuscaPorFlujoCustodia		NVARCHAR(1)		DEFAULT 'N',
	IN	p_TipoOperacionCustodia		NVARCHAR(6)		DEFAULT NULL,
	IN	p_SaldoFlujoCustodia		DECIMAL(26,8) 	DEFAULT NULL,
	IN	p_CriterioFlujoCustodia		NVARCHAR(40)	DEFAULT NULL,
	IN	p_IdInstrumento				BIGINT			DEFAULT NULL,
	IN	p_IdPersonaBancoCustodio	BIGINT			DEFAULT NULL,
	IN	p_IdPersonaBrokerExtranjero	BIGINT			DEFAULT NULL,
	
	OUT p_out_Clientes TABLE(
						"DocumentoIdentidad"		NVARCHAR(40),
						"NombreCompleto"			NVARCHAR(5000),
						"FechaNacimiento"			SECONDDATE,
						"FechaCreacion"				SECONDDATE,
						"FechaDefuncion"			SECONDDATE,
						"IdTipoCliente"				NVARCHAR(40),
						"IdCliente"					BIGINT,
						"IdPortafolio"				BIGINT,
						"NombreTipoCliente"			NVARCHAR(255),
						"IdTipoPersona"				NVARCHAR(2),
						"NombreTipoPersona"			NVARCHAR(255),
						"IdSegmentoCliente"			NVARCHAR(40),
						"NombreSegmentoCliente"		NVARCHAR(255),
						"DescripcionSegmentoCliente" NVARCHAR(520),
						"IdEmpleadoEjecutivo"		BIGINT,
						"CodigoEmpleadoEjecutivo"	NVARCHAR(40),
						"NombreEmpleadoEjecutivo"	NVARCHAR(400),
						"IdCategoriaInversionista"	NVARCHAR(40),
						"NombreCategoriaInversionista"	NVARCHAR(100),
						"IdEstadoCliente"			NVARCHAR(40),
						"NombreEstadoCliente"		NVARCHAR(100),
						"IdEstadoPortafolio"		NVARCHAR(40),
						"NombreEstadoPortafolio"	NVARCHAR(100),
						"CodigoPortafolio"			NVARCHAR(100),
						"DescripcionPortafolio"		NVARCHAR(255),
						"PropositoPortafolio"		NVARCHAR(520),
						"IdTipoBloqueo"				NVARCHAR(40),
						"NombreTipoBloqueo"			NVARCHAR(100),
						"IdPerfilComercial"			NVARCHAR(40),
						"IdPerfilProductos"			NVARCHAR(40),
						"IdPerfilCobros"			NVARCHAR(40),
						"IdPerfilLimites"			NVARCHAR(40),
						"SaldoFlujoCaja"			DECIMAL(26,8),
						"IdMonedaFlujoCaja"			NVARCHAR(3),
						"SaldoFlujoCustodia"		DECIMAL(26,8),
						"IdInstrumento"				BIGINT,
						"Nemotecnico"				NVARCHAR(40),
						"IdPersona"					BIGINT
	),
	
	



		
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador 		int,
	OUT p_out_AdvertenciasDetalle 		table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)) DEFAULT EMPTY,	
	OUT p_out_ErroresContador 			int,
	OUT p_out_ErroresDetalle 			table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520)) DEFAULT EMPTY
	
) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
AS
BEGIN
	
	CALL "BSCL.SocioNegocio.BD.Servicios::CLI.ConsultaAvanzadaClientes" (
		p_IdEmpresa					=> p_IdEmpresa,
		p_IdUsuario					=> p_IdUsuario,
		p_IdModulo					=> p_IdModulo,
		p_IdEmpleadoConsulta		=> p_IdEmpleadoConsulta,
		
		p_Nombre					=> p_Nombre,
		p_DocumentoIdentidad		=> p_DocumentoIdentidad,
		p_IdTipoPersona				=> p_IdTipoPersona,
		p_IdTipoCliente				=> p_IdTipoCliente,
		p_IdSegmentoCliente			=> p_IdSegmentoCliente,
		p_IdEstadoCliente			=> p_IdEstadoCliente,
		p_IdTipoBloqueoCliente		=> p_IdTipoBloqueoCliente,
		p_IdTipoBloqueoPortafolio	=> p_IdTipoBloqueoPortafolio,
		p_IdEmpleadoFiltro			=> p_IdEmpleadoFiltro,
		p_IdCategoriaInversionista	=> p_IdCategoriaInversionista,
		p_IdPropositoPortafolio		=> p_IdPropositoPortafolio,
		p_IdEstadoPortafolio		=> p_IdEstadoPortafolio,
		p_IdPerfilComercial			=> p_IdPerfilComercial,
		p_IdPerfilProductos			=> p_IdPerfilProductos,
		p_IdPerfilCobros			=> p_IdPerfilCobros,
		p_FechaNacimientoDesde		=> p_FechaNacimientoDesde,
		p_FechaNacimientoHasta		=> p_FechaNacimientoHasta,
		p_FechaCreacionDesde		=> p_FechaCreacionDesde,
		p_FechaCreacionHasta		=> p_FechaCreacionHasta,
		p_FechaDefuncionDesde		=> p_FechaDefuncionDesde,
		p_FechaDefuncionHasta		=> p_FechaDefuncionHasta,
		p_AliasPortafolio			=> p_AliasPortafolio,
		
		p_IdPersonaBancoCustodio	=> p_IdPersonaBancoCustodio,
		p_IdPersonaBrokerExtranjero => p_IdPersonaBrokerExtranjero,
		
		p_BuscaPorFlujoCaja			=> p_BuscaPorFlujoCaja,
		p_UsoFlujoCaja				=> p_UsoFlujoCaja,
		p_SaldoFlujoCaja			=> p_SaldoFlujoCaja,
		p_IdMonedaFlujoCaja			=> p_IdMonedaFlujoCaja,
		p_CriterioFlujoCaja			=> p_CriterioFlujoCaja,
		
		p_BuscaPorFlujoCustodia		=> p_BuscaPorFlujoCustodia,
		p_TipoOperacionCustodia		=> p_TipoOperacionCustodia,
		p_SaldoFlujoCustodia		=> p_SaldoFlujoCustodia,
		p_CriterioFlujoCustodia		=> p_CriterioFlujoCustodia,
		p_IdInstrumento				=> p_IdInstrumento,
		
		p_out_Clientes				=> v_out_Clientes,
		p_out_AdvertenciasContador 	=> p_out_AdvertenciasContador,
		p_out_AdvertenciasDetalle 	=> p_out_AdvertenciasDetalle,
		p_out_ErroresContador 		=> p_out_ErroresContador,
		p_out_ErroresDetalle 		=> p_out_ErroresDetalle) ;
	
	
	p_out_Clientes = SELECT C."DocumentoIdentidad",C."NombreCompleto",C."FechaNacimiento",C."FechaCreacion",C."FechaDefuncion",C."IdTipoCliente",C."IdCliente",
						C."IdPortafolio",C."NombreTipoCliente",C."IdTipoPersona",C."NombreTipoPersona",C."IdSegmentoCliente",C."NombreSegmentoCliente",
						C."DescripcionSegmentoCliente",C."IdEmpleadoEjecutivo",C."CodigoEmpleadoEjecutivo",C."NombreEmpleadoEjecutivo",
						C."IdCategoriaInversionista",C."NombreCategoriaInversionista",C."IdEstadoCliente",C."NombreEstadoCliente",
						C."IdEstadoPortafolio",C."NombreEstadoPortafolio",C."CodigoPortafolio",C."DescripcionPortafolio",
						C."PropositoPortafolio",C."IdTipoBloqueoPortafolio" AS "IdTipoBloqueo",C."NombreTipoBloqueoPortafolio" AS "NombreTipoBloqueo",C."IdPerfilComercial",C."IdPerfilProductos",
						C."IdPerfilCobros",C."IdPerfilLimites",C."SaldoFlujoCaja",C."IdMonedaFlujoCaja",C."SaldoFlujoCustodia",
						C."IdInstrumento",C."Nemotecnico",CL."IdPersona"
						FROM :v_out_Clientes C
						INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CL ON CL."IdCliente" = C."IdCliente";
	
END;
