set schema bscl;
DROP table #series;
create local temporary table #series(
												"IdInterno"					BIGINT,
											 	"IdSerieFondo" 		 		NVARCHAR(40),
											 	"HorarioCierreComercial"	NVARCHAR(8),
											 	"HorarioCierreBackOffice"	NVARCHAR(8),
											 	"UsaHorarioDefault"			NVARCHAR(1),
											 	"TipoSolicitud"				NVARCHAR(3)	
);

DELETE FROM  #series;
INSERT INTO #series VALUES(0,'FMLIQUIDEZ_A','13:00:00','11:00:00','N','APO');


select * from #series;



call "BSCL.FondoMutuo.BD.Servicios::ModificarHorarioCierreInstrumento" (
	p_IdEmpresa	 => 1,
	p_IdUsuario	 => 21,
	p_IdModulo	 => 'FMM',
	p_UsaReglasNegocio => 'S',
	p_SerieFondo => #series,
	p_out_Resultado => ?,
	p_out_AdvertenciasContador => ?,
	p_out_AdvertenciasDetalle => ?,
	p_out_ErroresContador => ?,
	p_out_ErroresDetalle => ?
);


SELECT * FROM "BSCL.FondoMutuo.BD::FMM.SerieDeFondo";
