SET SCHEMA "BSCL";


CALL "BSCL.Orden.BD.Servicios::ConsultarCustodiaGarantizableSimultanea" (
	p_IdEmpresa					 => 1313,
	p_IdUsuario					 => 25,
	p_IdModulo					 => 'ORD',
	p_IdCliente 				 => 167307,
	p_IdPortafolio				 => 189419,
	p_IdBolsa					 => 'BCS',
	p_DiasLiquidacion			 => 0,
	p_IdInstrumentoAGarantizar	 => 2249,
	
	p_out_CustodiaGarantia		=> ?,
	p_out_AdvertenciasContador	=> ?,
	p_out_AdvertenciasDetalle	=> ?,
	p_out_ErroresContador		=> ?,
	p_out_ErroresDetalle		=> ?)

	select * from "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" where "IdServicio" = 3151
	
	
	IdServicio;RutaServicio;UsoServicio;TipoServicio;Descripcion;TablasTempIN;TablasTempOUT
3.151;BSCL.Orden.BD.Servicios::CrearOrdenes;53;P;;p_Ordenes=BSCL.Orden.BD::ORD.CrearOrdenes_Entrada_Ordenes;p_CobrosModificados=BSCL.Orden.BD::ORD.CrearOrdenes_Entrada_CobrosModificados;p_Garantias=BSCL.Orden.BD::ORD.CrearOrdenes_Entrada_Garantias;;P_OUT_RESULTADO=BSCL.Orden.BD::ORD.CrearOrdenes_Salida_Resultado;P_OUT_COBROS=BSCL.Orden.BD::ORD.CrearOrdenes_Salida_Cobros;P_OUT_GARANTIAS=BSCL.Orden.BD::ORD.CrearOrdenes_Salida_Garantias;P_OUT_INFORMACIONDETALLE=BSCL.Orden.BD::ORD.CrearOrdenes_Salida_InformacionDetalle;P_OUT_ADVERTENCIASDETALLE=BSCL.Orden.BD::ORD.CrearOrdenes_Salida_AdvertenciasDetalle;P_OUT_ERRORESDETALLE=BSCL.Orden.BD::ORD.CrearOrdenes_Salida_ErroresDetalle

rollback