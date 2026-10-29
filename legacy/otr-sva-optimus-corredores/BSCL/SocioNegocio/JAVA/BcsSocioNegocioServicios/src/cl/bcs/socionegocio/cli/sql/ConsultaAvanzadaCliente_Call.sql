/********************************************************** 
Proposito	: filtrar consulta avanzada de clientes
autor		: EM
fecha		: 16/08/2017
***********************************************************/

CALL "BSCL.SocioNegocio.BD.Servicios::CLI.ConsultaAvanzadaClientes" (

	p_IdEmpresa					=> :p_IdEmpresa		
	,p_IdUsuario				=> :p_IdUsuario	
	,p_IdModulo					=> :p_IdModulo	
	,p_IdEmpleadoConsulta		=> :p_IdEmpleadoConsulta	
	,p_Nombre					=> :p_Nombre
	,p_DocumentoIdentidad		=> :p_DocumentoIdentidad
	,p_IdTipoPersona			=> :p_IdTipoPersona
	,p_IdTipoCliente			=> :p_IdTipoCliente
	,p_IdSegmentoCliente		=> :p_IdSegmentoCliente
	,p_IdEstadoCliente			=> :p_IdEstadoCliente
	,p_IdTipoBloqueoCliente		=> :p_IdTipoBloqueoCliente
	,p_IdTipoBloqueoPortafolio	=> :p_IdTipoBloqueoPortafolio
	,p_IdEmpleadoFiltro			=> :p_IdEmpleadoFiltro
	,p_IdCategoriaInversionista	=> :p_IdCategoriaInversionista
	,p_IdPropositoPortafolio	=> :p_IdPropositoPortafolio
	,p_IdEstadoPortafolio		=> :p_IdEstadoPortafolio
	,p_IdPerfilComercial		=> :p_IdPerfilComercial
	,p_IdPerfilProductos		=> :p_IdPerfilProductos
	,p_IdPerfilCobros			=> :p_IdPerfilCobros
	,p_FechaNacimientoDesde		=> :p_FechaNacimientoDesde
	,p_FechaNacimientoHasta		=> :p_FechaNacimientoHasta
	,p_FechaCreacionDesde		=> :p_FechaCreacionDesde
	,p_FechaCreacionHasta		=> :p_FechaCreacionHasta
	,p_FechaDefuncionDesde		=> :p_FechaDefuncionDesde
	,p_FechaDefuncionHasta		=> :p_FechaDefuncionHasta
	,p_AliasPortafolio			=> :p_AliasPortafolio
	,p_IdPersonaBancoCustodio	=> :p_IdPersonaBancoCustodio
	,p_IdPersonaBrokerExtranjero=> :p_IdPersonaBrokerExtranjero
	,p_BuscaPorFlujoCaja		=> :p_BuscaPorFlujoCaja
	,p_UsoFlujoCaja				=> :p_UsoFlujoCaja
	,p_SaldoFlujoCaja			=> :p_SaldoFlujoCaja
	,p_IdMonedaFlujoCaja		=> :p_IdMonedaFlujoCaja
	,p_CriterioFlujoCaja		=> :p_CriterioFlujoCaja
	,p_BuscaPorFlujoCustodia	=> :p_BuscaPorFlujoCustodia
	,p_TipoOperacionCustodia	=> :p_TipoOperacionCustodia
	,p_SaldoFlujoCustodia		=> :p_SaldoFlujoCustodia
	,p_CriterioFlujoCustodia	=> :p_CriterioFlujoCustodia
	,p_IdInstrumento			=> :p_IdInstrumento
	, p_out_Clientes 				=> $p_out_Clientes
	, p_out_AdvertenciasContador 	=> @p_out_AdvertenciasContador
	, p_out_AdvertenciasDetalle 	=> $p_out_AdvertenciasDetalle
	, p_out_ErroresContador 		=> @p_out_ErroresContador
	, p_out_ErroresDetalle 			=> $p_out_ErroresDetalle


);
