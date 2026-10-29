set schema bscl;

create local temporary table #filtros ("IdInterno"					 BIGINT,
											"Aporte" 	 				 NVARCHAR(1),
											"Rescate"					 NVARCHAR(1),
											"MoneyMarket"				 NVARCHAR(1),
											"AporteReinversion"			 NVARCHAR(1),
											"RescateReinversion"		 NVARCHAR(1),
											"IdCarga"					 BIGINT	);
											
insert into #filtros values(0,'S','N','N','N','N',1);
insert into #filtros values(1,'N','S','S','S','S',2);

create local temporary table #seriefondo ("IdInterno"					 BIGINT,
											"IdSerieFondo"				 NVARCHAR(40));
insert into #seriefondo values(68,'TEST1');
insert into #seriefondo values(70,'TEST2');

call "BSCL.FondoMutuo.BD.Servicios::CrearProgramaCertificacionBatch" (
	p_IdEmpresa	 => 1,
	p_IdUsuario	 => 21,
	p_IdModulo	 => 'FMM',
	p_SimularOperacion => 'N',
	p_UsaReglasNegocio => 'S',
	p_FechaCertificacion => '2016/10/10',
	p_Filtros => #filtros,
	p_SerieFondo => #seriefondo,
	p_out_Resultado => ?,
	p_out_AdvertenciasContador => ?,
	p_out_AdvertenciasDetalle => ?,
	p_out_ErroresContador => ?,
	p_out_ErroresDetalle => ?
);

DROP TABLE #filtros;
DROP TABLE #seriefondo;

select * from "BSCL"."BSCL.Plataforma.BD::BAT.Programa" ORDER BY "IdPrograma" DESC;
