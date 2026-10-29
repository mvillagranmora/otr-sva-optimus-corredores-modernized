SET SCHEMA "BSCL";

CALL "BSCL.CanalAgente.BD.Servicios::ConsultaAvanzadaClientes" (
	p_IdEmpresa => 1,
	p_IdUsuario => 4,
	p_IdEmpleadoConsulta => 7293,
	p_IdModulo => 'PLA' ,
	
	p_BuscaPorFlujoCustodia		=> 'S',
	p_TipoOperacionCustodia		=> 'VENACN',
	p_FlujoCustodiaMinimo		=> 150000,
	p_IdInstrumento				=> 122,
	
	/*p_BuscaPorFlujoCaja => 'S',
	p_UsoFlujoCaja		=> 'ORDENES_DE_COMPRA',
	p_FlujoCajaMinimo	=> 0,
	p_IdMonedaFlujoCaja	=> 'CLP',*/
	
	p_out_Clientes => ?,
	p_out_AdvertenciasContador => ?,
	p_out_AdvertenciasDetalle  => ?,
	p_out_ErroresContador 	 => ?,
	p_out_ErroresDetalle 		 => ?
	);

	
