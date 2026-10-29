SET SCHEMA BSCL;
DROP TABLE #TT;
CREATE LOCAL TEMPORARY TABLE #TT ( "NombreParametro" nvarchar(100) , "Valor" nvarchar(100));

INSERT INTO #TT SELECT 'IdEmpresa','1' FROM DUMMY;

INSERT INTO #TT SELECT 'IdUsuario','0' FROM DUMMY;

INSERT INTO #TT SELECT 'Fecha','2022-02-23' FROM DUMMY;
CALL "BSCL.Plataforma.BD.Servicios::BAT.AgregarPrograma"
(      p_IdEmpresa                  => 1,
       p_IdUsuario                  => 1,
       p_IdModulo                   => 'PLA',
       p_IdAgenda                   => 0,
       p_IdTipoTarea                => 'PATRIMONIO_CUSTODIA_JOB',
       p_Segundos                   => 0,
       p_TablaParametros            => #TT,
       p_CreaParam                  => 'S',
       p_out_IdPrograma             => ?,
       p_out_AdvertenciasContador   => ?,
       p_out_AdvertenciasDetalle  => ?,
       p_out_ErroresContador        => ?,
       p_out_ErroresDetalle         => ?                                          

);

COMMIT;

select "FechaPatrimonio",count(1) from BSCL."BSCL.Custodia.BD::CUS.PatrimonioCustodia" group by "FechaPatrimonio" order by "FechaPatrimonio" desc

/*
FechaPatrimonio;COUNT(1)
17-04-2020;262.585
30-04-2020;260.431
15-05-2020;265.469
x16-05-2020;265.469
x17-05-2020;265.469
18-05-2020;265.680
19-05-2020;265.673
20-05-2020;265.667
22-05-2020;265.829
23-05-2020;265.829
24-05-2020;265.829
25-05-2020;265.958
26-05-2020;266.144
27-05-2020;266.352
28-05-2020;266.899
29-05-2020;267.404
30-05-2020;267.404
31-05-2020;267.404
01-06-2020;267.593
02-06-2020;267.740
03-06-2020;267.957
*/
