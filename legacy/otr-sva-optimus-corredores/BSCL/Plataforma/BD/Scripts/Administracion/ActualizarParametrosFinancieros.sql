set schema bscl;
create local temporary table #asdf ("NombreParametro" nvarchar(100) , "Valor" nvarchar(5000));
insert into #asdf
select 'IdEmpresa','1' from dummy;

call "BSCL.Plataforma.BD.Servicios::BAT.AgregarPrograma"
 (
	1,--IN p_IdEmpresa          BIGINT,
	0,--IN p_IdUsuario          BIGINT,
	'PLA',--IN p_IdModulo           NVARCHAR(3),
	0,--IN	p_IdAgenda	 		BIGINT, 
	'UPD_PARAMS_FINANCIEROS_OPEN',--IN	p_IdTipoTarea		NVARCHAR(40),   
	0,--IN	p_Segundos			BIGINT,
	#asdf,--IN  p_TablaParametros table("NombreParametro" nvarchar(100) , "Valor" nvarchar(5000)) DEFAULT EMPTY,
	'S',--IN  p_CreaParam			NVARCHAR(1),
	?,--OUT p_out_IdPrograma		table("IdPrograma" BIGINT),
	?,--OUT p_out_AdvertenciasContador int,
	?,--OUT p_out_AdvertenciasDetalle table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)),	
	?,--OUT p_out_ErroresContador int,
	?--OUT p_out_ErroresDetalle table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520))
	-- Salidas para mensajes de advertencias y errores									
);

commit;


--AGENDA POR SI SE PERDIO
create local temporary table #parametros("NombreParametro" nvarchar(100), "Valor" nvarchar(100));GO
INSERT INTO #parametros Select 'IdEmpresa' as "NombreParametro", '1' as "Valor" from dummy;GO
CALL "BSCL.Plataforma.BD.Servicios::BAT.AgregarAgenda"(
	p_IdEmpresa  =>  1,
	p_IdUsuario  =>  0,
	p_IdModulo   =>  'MER',
	p_IdTipoTarea  =>  'UPD_PARAMS_FINANCIEROS_CLOSE',
	p_EsDemonio    =>  'N'  ,
	p_DiaDeMes    =>  '**',
	p_DiaDeSemana  =>  '*',
	p_Mes      =>  '**',
	p_Hora      =>  '18',
	p_Minuto    =>  '58',
	p_SoloDiasHabiales  => 'S',
	p_SoloPrincipioMes  => 'N',
	p_SoloFinDeMes    => 'N',
	p_habilitado    =>  'S', 
	p_TablaParametros  =>  #parametros,              
	p_IdAgenda    => ?,
	p_IdPrograma    => ?,
	p_out_AdvertenciasContador => ?,
	p_out_AdvertenciasDetalle => ?,
	p_out_ErroresContador => ?,
	p_out_ErroresDetalle => ?              
);
commit;


