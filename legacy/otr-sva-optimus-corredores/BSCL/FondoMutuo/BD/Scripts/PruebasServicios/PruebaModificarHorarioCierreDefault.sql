set schema bscl;
DROP table #horarios;
create local temporary table #horarios(
												"IdInterno"								BIGINT,
											 	"HorarioCierreAporteComercial"			NVARCHAR(8),
											 	"HorarioCierreAporteBackOffice"			NVARCHAR(8),
											 	"HorarioCierreRescateComercial"			NVARCHAR(8),
											 	"HorarioCierreRescateBackOffice"		NVARCHAR(8),
											 	"HorarioCierreReinversionComercial"		NVARCHAR(8),
											 	"HorarioCierreReinversionBackOffice"	NVARCHAR(8),
											 	"HorarioCierreLiquidezComercial"		NVARCHAR(8),
											 	"HorarioCierreLiquidezBackOffice"		NVARCHAR(8)
);

DELETE FROM  #horarios;
INSERT INTO #horarios VALUES(0,'12:00:00','13:00:00','11:00:00','14:00:00','10:00:00','15:00:00','08:00:00','16:00:00');


select * from #horarios;



call "BSCL.FondoMutuo.BD.Servicios::ModificarHorarioCierreDefault" (
	p_IdEmpresa	 => 1,
	p_IdUsuario	 => 21,
	p_IdModulo	 => 'FMM',
	p_UsaReglasNegocio => 'S',
	p_Horario => #horarios,
	p_out_Resultado => ?,
	p_out_AdvertenciasContador => ?,
	p_out_AdvertenciasDetalle => ?,
	p_out_ErroresContador => ?,
	p_out_ErroresDetalle => ?
);


SELECT * FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarHorarioCierreDefault";
