set schema bscl;
DROP table #filtros_prueba_certificacion;
create local temporary table #filtros_prueba_certificacion(
	"IdEmisor" 	 				 NVARCHAR(40),
	"IdMoneda"					 NVARCHAR(3),
	"IdSerieFondo"				 NVARCHAR(40),
	"Nacional"			 		 NVARCHAR(1),
	"TipoSolicitud"		 		 NVARCHAR(3),
	"IdTipoMovimientoFondoMutuo" NVARCHAR(10),
	"MoneyMarket"				 NVARCHAR(1),
	"IdCarga"					 BIGINT	
);

INSERT INTO #filtros_prueba_certificacion VALUES('','','','N','RES','','N',0);

call "BSCL.FondoMutuo.BD.Servicios::CertificarPorFiltrosSolicitudAporteRescate" (
	p_IdEmpresa	 => 1,
	p_IdUsuario	 => 21,
	p_IdModulo	 => 'FMM',
	p_SimularOperacion => 'N',
	p_UsaReglasNegocio => 'S',
	p_FechaCertificacion => '2016-12-05',
	p_Filtros => #filtros_prueba_certificacion,
	p_out_Resultado => ?,
	p_out_AdvertenciasContador => ?,
	p_out_AdvertenciasDetalle => ?,
	p_out_ErroresContador => ?,
	p_out_ErroresDetalle => ?
);
