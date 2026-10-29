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
SELECT 'folder','OPTIMUS_UAT/SALIDA' FROM DUMMY;
SELECT * FROM #tt;


SELECT * FROM "BSCL"."BSCL.Plataforma.BD::BAT.ParametrosAgenda"

CALL "BSCL.Plataforma.BD.Servicios::BAT.AgregarPrograma"
 (
	1,
	25,
	'PLA',
	0,
	'EXTRACTOR_ARCHIVOS',
	0,
	#TT,
	'S',
	?,
	?,
	?,
	?,
	?							
);

select * from "BSCL"."BSCL.Plataforma.BD::MER.CargaInstrumentosCuotasFondo"

delete from #TT;
INSERT INTO #TT
SELECT 'IdEmpresa','1' FROM DUMMY;
CALL "BSCL.Plataforma.BD.Servicios::BAT.AgregarPrograma"
 (
	1,
	4,
	'PLA',
	0,
	'PROCESADOR_ARCHIVOS_RUEBAS',
	0,
	#TT,
	'S',
	?,
	?,
	?,
	?,
	?							
);
commit;
rollback;
SELECT * FROM "BSCL"."BSCL.Plataforma.BD::BAT.TipoPrograma"


 select * from "BSCL"."BSCL.Plataforma.BD::BAT.TipoPrograma"
