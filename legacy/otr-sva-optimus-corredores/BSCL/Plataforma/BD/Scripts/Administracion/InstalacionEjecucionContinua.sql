SET SCHEMA "BSCL";

INSERT INTO "BSCL.Plataforma.BD::BAT.TipoPrograma"("IdTipoPrograma","Descripcion","MaximoIntentos","Modulo","NombreClasePrograma","TipoClasePrograma","Nodo","EsPauta")
SELECT 'EjecucionContinuaSQL' as "IdTipoPrograma",'EjecucionContinuaSQL' AS "Descripcion",1 AS "MaximoIntentos",'Plataforma' AS "Modulo",
'cl.bcs.plataforma.jobs.EjecucionContinuaSQL' AS"NombreClasePrograma",'JAVA' AS "TipoClasePrograma",'D' as "Nodo",'N' AS "EsPauta" FROM DUMMY;


DROP TABLE #TT;
CREATE LOCAL TEMPORARY TABLE #TT (
	"NombreParametro" nvarchar(100) , "Valor" nvarchar(100)
);

CALL "BSCL.Plataforma.BD.Servicios::BAT.AgregarAgenda"(
	p_IdEmpresa          => 1,
	p_IdUsuario          => 0,
	p_IdModulo          => 'PLA',
	p_IdTipoTarea		=> 'EjecucionContinuaSQL',
	p_EsDemonio			=> 'S',  
	p_DiaDeMes			=> '**',
	p_DiaDeSemana		=> '*',
	p_Mes				=> '**',
	p_Hora				=> '**',
	p_Minuto			=> '01',
	p_SoloDiasHabiales	=> 'N',
	p_SoloPrincipioMes	=> 'N',
	p_SoloFinDeMes		=> 'N',
	p_habilitado		=> 'S',
	p_TablaParametros	=> #TT,
	
	p_IdAgenda					=> ?,
	p_IdPrograma				=> ?,
	p_out_AdvertenciasContador 	=> ?,
	p_out_AdvertenciasDetalle 	=> ?,
	p_out_ErroresContador 		=> ?,	
	p_out_ErroresDetalle		=> ?);
	
	commit;

	
	