SET SCHEMA "BSCL";

--delete FROM "BSCL"."BSCL.Plataforma.BD::PLA.DetalleExtractorDeArchivo";
--delete FROM "BSCL"."BSCL.Plataforma.BD::PLA.ExtractorDeArchivo";
--commit
--SELECT * FROM "BSCL"."BSCL.Plataforma.BD::PLA.ExtractorDeArchivo"
--update "BSCL"."BSCL.Plataforma.BD::PLA.ExtractorDeArchivo" set "Procesado" = 'N' where "IdEstado" = 'ERR';
--commit;

select * from "BSCL"."BSCL.Plataforma.BD::BAT.TipoPrograma" where "IdTipoPrograma" like '%EXTRACTOR_ARCHIVOS%';
--IdTipoPrograma;Descripcion;MaximoIntentos;Modulo;NombreClasePrograma;TipoClasePrograma;Nodo;EsPauta
--EXTRACTOR_ARCHIVOS;Extractor de Archivos;1;Plataforma;cl.bcs.plataforma.jobs.ExtractorArchivosJob;JAVA;;N

select * from "BSCL"."BSCL.Plataforma.BD::BAT.TipoPrograma" where "IdTipoPrograma" like '%PROCESADOR_ARCHIVOS_RUEBAS%';
--IdTipoPrograma;Descripcion;MaximoIntentos;Modulo;NombreClasePrograma;TipoClasePrograma;Nodo;EsPauta
--PROCESADOR_ARCHIVOS_RUEBAS;Procesador Archivos Ruebas;1;Plataforma;cl.bcs.plataforma.jobs.ProcesadorArchivosJob;JAVA;;N

DROP TABLE #TT;
CREATE LOCAL TEMPORARY TABLE #TT (
	"NombreParametro" nvarchar(100) , "Valor" nvarchar(100)
);

--DESA
INSERT INTO #TT
SELECT 'idEmpresa','1' FROM DUMMY UNION ALL
SELECT 'ftpHostname','161.238.90.38' FROM DUMMY UNION ALL
SELECT 'ftpUser','sftpusu1' FROM DUMMY UNION ALL
SELECT 'ftpPassword','sftpusu1' FROM DUMMY UNION ALL
SELECT 'schema','BSCL' FROM DUMMY UNION ALL
SELECT 'folder','OPTIMUS_DES/SALIDA' FROM DUMMY;

--QA
INSERT INTO #TT
SELECT 'idEmpresa','1' FROM DUMMY UNION ALL
SELECT 'ftpHostname','161.238.90.38' FROM DUMMY UNION ALL
SELECT 'ftpUser','sftpusu1' FROM DUMMY UNION ALL
SELECT 'ftpPassword','sftpusu1' FROM DUMMY UNION ALL
SELECT 'schema','BSCL' FROM DUMMY UNION ALL
SELECT 'folder','OPTIMUS_QA/SALIDA' FROM DUMMY;

--UAT
INSERT INTO #TT
SELECT 'idEmpresa','1' FROM DUMMY UNION ALL
SELECT 'ftpHostname','161.238.90.38' FROM DUMMY UNION ALL
SELECT 'ftpUser','sftpusu1' FROM DUMMY UNION ALL
SELECT 'ftpPassword','sftpusu1' FROM DUMMY UNION ALL
SELECT 'schema','BSCL' FROM DUMMY UNION ALL
SELECT 'folder','OPTIMUS_UAT/SALIDA' FROM DUMMY;

--MB
INSERT INTO #TT
SELECT 'idEmpresa','1' FROM DUMMY UNION ALL
SELECT 'ftpHostname','161.238.70.105' FROM DUMMY UNION ALL  --MB 
SELECT 'ftpUser','Optimus' FROM DUMMY UNION ALL
SELECT 'ftpPassword','Bolsa123' FROM DUMMY UNION ALL
SELECT 'schema','BSCL' FROM DUMMY UNION ALL
SELECT 'folder','Optimus' FROM DUMMY;

/*INSERT INTO "BSCL"."BSCL.Plataforma.BD::ACC.Usuario"
("IdUsuario","IdEmpresa","CodigoUsuario","Clave","FechaIngreso","FechaVigencia","FechaUltimaConexion","DiasVencimientoClave","Conectado","Hora","Terminal","IdEstado","IntentosConexion","FechaBloqueoTemporal","TipoEntidad","IdEntidad","EsAutorizado","DebeCambiarClave","UltimoCambioClave","CodigoMenu")
SELECT 0,1,'SISOPT','000',CURRENT_TIMESTAMP,'2050-01-01',CURRENT_TIMESTAMP,999999,'N','00:00:00','','ACT',0,'0001-01-01','SIS',0,'S','N',CURRENT_TIMESTAMP,''
FROM DUMMY;
*/

SELECT * FROM "BSCL.Plataforma.BD::BAT.Agenda"
--delete from "BSCL.Plataforma.BD::BAT.Agenda" where "IdAgenda" = 10019

CALL "BSCL.Plataforma.BD.Servicios::BAT.AgregarAgenda"(
	p_IdEmpresa          => 1,
	p_IdUsuario          => 0,
	p_IdModulo          => 'PLA',
	p_IdTipoTarea		=> 'EXTRACTOR_ARCHIVOS',
	p_EsDemonio			=> 'N',  
	p_DiaDeMes			=> '**',
	p_DiaDeSemana		=> '*',
	p_Mes				=> '**',
	p_Hora				=> '08',
	p_Minuto			=> '45',
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
	
CALL "BSCL.Plataforma.BD.Servicios::BAT.AgregarAgenda"(
	p_IdEmpresa          => 1,
	p_IdUsuario          => 0,
	p_IdModulo          => 'PLA',
	p_IdTipoTarea		=> 'EXTRACTOR_ARCHIVOS',
	p_EsDemonio			=> 'N',  
	p_DiaDeMes			=> '**',
	p_DiaDeSemana		=> '*',
	p_Mes				=> '**',
	p_Hora				=> '18',
	p_Minuto			=> '30',
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
	p_out_ErroresDetalle		=> ?)

COMMIT;
{
----------------------------------------------------------------------------------------------------------------------
--PARA PROBAR
set schema "BSCL";

DROP TABLE #TT;
CREATE LOCAL TEMPORARY TABLE #TT (
	"NombreParametro" nvarchar(100) , "Valor" nvarchar(100)
);


INSERT INTO #TT
SELECT 'idEmpresa','1' FROM DUMMY UNION ALL
SELECT 'ftpHostname','161.238.90.38' FROM DUMMY UNION ALL
SELECT 'ftpUser','sftpusu1' FROM DUMMY UNION ALL
SELECT 'ftpPassword','sftpusu1' FROM DUMMY UNION ALL
SELECT 'schema','BSCL' FROM DUMMY UNION ALL
SELECT 'folder','OPTIMUS_QA/SALIDA' FROM DUMMY;


--MB
INSERT INTO #TT
SELECT 'idEmpresa','1' FROM DUMMY UNION ALL
SELECT 'ftpHostname','161.238.70.105' FROM DUMMY UNION ALL  --MB 
SELECT 'ftpUser','Optimus' FROM DUMMY UNION ALL
SELECT 'ftpPassword','Bolsa123' FROM DUMMY UNION ALL
SELECT 'schema','BSCL' FROM DUMMY UNION ALL
SELECT 'folder','optimus' FROM DUMMY;

CALL "BSCL.Plataforma.BD.Servicios::BAT.AgregarPrograma"
 (
	p_IdEmpresa 				=> 1,
	p_IdUsuario					=> 0,
	p_IdModulo 					=> 'PLA',
	p_IdAgenda 					=> 0,
	p_IdTipoTarea 				=> 'EXTRACTOR_ARCHIVOS',
	p_Segundos 					=> 0,
	p_TablaParametros 			=> #TT,
	p_CreaParam 				=> 'S',
	p_out_IdPrograma 			=> ?,
	p_out_AdvertenciasContador 	=> ?,
	p_out_AdvertenciasDetalle 	=> ?,
	p_out_ErroresContador 		=> ?,
	p_out_ErroresDetalle		=> ?							
);

commit;



