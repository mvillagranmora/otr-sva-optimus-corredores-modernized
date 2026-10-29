SET SCHEMA "BSCL";

INSERT INTO "BSCL.Plataforma.BD::BAT.TipoPrograma"("IdTipoPrograma","Descripcion","MaximoIntentos","Modulo","NombreClasePrograma","TipoClasePrograma","Nodo","EsPauta")
VALUES('UPD_PARAMS_FINANCIEROS_OPEN', 'Actualizar parametros financieros y paridades en apertura', 1,'MER','cl.bcs.plataforma.jobs.ActualizarParametrosFinancieros','JAVA','','N');
INSERT INTO "BSCL.Plataforma.BD::BAT.TipoPrograma"("IdTipoPrograma","Descripcion","MaximoIntentos","Modulo","NombreClasePrograma","TipoClasePrograma","Nodo","EsPauta")
VALUES('UPD_PARAMS_FINANCIEROS_CLOSE', 'Actualizar parametros financieros y paridades en cierre', 1,'MER','cl.bcs.plataforma.jobs.ActualizarParametrosFinancieros','JAVA','','N');

DROP TABLE #param;
create local temporary table #param ("NombreParametro" nvarchar(100) , "Valor" nvarchar(100));
insert into #param values('IdEmpresa','1');

CALL "BSCL.Plataforma.BD.Servicios::BAT.AgregarAgenda"(
	p_IdEmpresa          => 1,
	p_IdUsuario          => 0,
	p_IdModulo          => 'PLA',
	p_IdTipoTarea		=> 'UPD_PARAMS_FINANCIEROS_OPEN',
	p_EsDemonio			=> 'N',  
	p_DiaDeMes			=> '**',
	p_DiaDeSemana		=> '*',
	p_Mes				=> '**',
	p_Hora				=> '10',
	p_Minuto			=> '**',
	p_SoloDiasHabiales	=> 'S',
	p_SoloPrincipioMes	=> 'N',
	p_SoloFinDeMes		=> 'N',
	p_habilitado		=> 'S',
	p_TablaParametros	=> #TT,
	
	p_IdAgenda					=> ?,
	p_IdPrograma				=> ?,
	p_out_AdvertenciasContador 	=> ?,
	p_out_AdvertenciasDetalle 	=> ?,
	p_out_ErroresContador 		=> ?,	
	p_out_ErroresDetalle		=> ?)
	
	CALL "BSCL.Plataforma.BD.Servicios::BAT.AgregarAgenda"(
	p_IdEmpresa          => 1,
	p_IdUsuario          => 0,
	p_IdModulo          => 'PLA',
	p_IdTipoTarea		=> 'UPD_PARAMS_FINANCIEROS_CLOSE',
	p_EsDemonio			=> 'N',  
	p_DiaDeMes			=> '**',
	p_DiaDeSemana		=> '*',
	p_Mes				=> '**',
	p_Hora				=> '18',
	p_Minuto			=> '**',
	p_SoloDiasHabiales	=> 'S',
	p_SoloPrincipioMes	=> 'N',
	p_SoloFinDeMes		=> 'N',
	p_habilitado		=> 'S',
	p_TablaParametros	=> #TT,
	
	p_IdAgenda					=> ?,
	p_IdPrograma				=> ?,
	p_out_AdvertenciasContador 	=> ?,
	p_out_AdvertenciasDetalle 	=> ?,
	p_out_ErroresContador 		=> ?,	
	p_out_ErroresDetalle		=> ?)




COMMIT;
