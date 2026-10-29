set schema bscl;
DROP table #comision;
DROP table #tramos;
create local temporary table #comision(
												"IdInterno" 			BIGINT,
												"IdSerieFondo"			NVARCHAR(40),
												"Descripcion"			NVARCHAR(100),
												"TipoComision"			NVARCHAR(40),
												"FechaVigenciaDesde"	NVARCHAR(10),
												"FechaVigenciaHasta"	NVARCHAR(10),
												"PorcentajeLiberado"	DECIMAL(26,8),
												"Estado"				NVARCHAR(3)
);
create local temporary table #tramos(
												"IdInterno"				BIGINT,
												"IdTramo"				BIGINT,
												"DiasDesde"				BIGINT,
												"DiasHasta"				BIGINT,
												"PorcentajeComision"	DECIMAL(26,8)
);

DELETE FROM  #comision;
DELETE FROM  #tramos;
INSERT INTO #comision VALUES(0, 'CFICCLAT-B','SALIDACOMISIONALTA','SALIDA','2016-12-01','2016-12-31',0, 'ACT');
INSERT INTO #tramos VALUES(0,0,60,90,1.5);
INSERT INTO #tramos VALUES(0,0,30,59,1);
INSERT INTO #tramos VALUES(0,0,0,29,0.8);



call "BSCL.FondoMutuo.BD.Servicios::AdministrarComision" (
	p_IdEmpresa	 => 1,
	p_IdUsuario	 => 21,
	p_IdModulo	 => 'FMM',
	p_UsaReglasNegocio => 'N',
	p_Comision => #comision,
	p_TramoComision => #tramos,
	p_out_Resultado => ?,
	p_out_Tramos => ?,
	p_out_AdvertenciasContador => ?,
	p_out_AdvertenciasDetalle => ?,
	p_out_ErroresContador => ?,
	p_out_ErroresDetalle => ?
);


SELECT * FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarTramoComision";
