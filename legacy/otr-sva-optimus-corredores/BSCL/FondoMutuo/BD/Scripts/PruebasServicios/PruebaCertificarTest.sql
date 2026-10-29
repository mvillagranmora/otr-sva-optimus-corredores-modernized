set schema bscl;
DROP table #filtros;
create local temporary table #filtros(
	"IdInterno"					 BIGINT,
	"IdEmisor" 	 				 NVARCHAR(40),
	"IdMoneda"					 NVARCHAR(3),
	"IdSerieFondo"				 NVARCHAR(40),
	"Nacional"			 		 NVARCHAR(1),
	"TipoSolicitud"		 		 NVARCHAR(3),
	"IdTipoMovimientoFondoMutuo" NVARCHAR(10),
	"MoneyMarket"				 NVARCHAR(1),
	"IdCarga"					 BIGINT	
);
DELETE FROM "BSCL.FondoMutuo.BD::FMM.Depuracion";
INSERT INTO #filtros VALUES(0,'','','','N','APO','','N',0);

call "BSCL.FondoMutuo.BD.Servicios::CertificarSolicitudAporteRescateTest" (
	p_IdEmpresa	 => 1,
	p_IdUsuario	 => 21,
	p_IdModulo	 => 'FMM',
	p_SimularOperacion => 'N',
	p_UsaReglasNegocio => 'S',
	p_FechaCertificacion => '2016-12-01',
	p_Filtros => #filtros,
	p_out_Resultado => ?,
	p_out_AdvertenciasContador => ?,
	p_out_AdvertenciasDetalle => ?,
	p_out_ErroresContador => ?,
	p_out_ErroresDetalle => ?
);

SELECT * FROM "BSCL.FondoMutuo.BD::FMM.Depuracion" ORDER BY "FechaHora" ASC;

UPDATE "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate"
SET "Estado" = 'ING',
	"MueveCta" = 'S'
WHERE "IdSolicitudAporteRescate" IN (1380,1381);