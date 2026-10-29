SET SCHEMA BSCL;

DROP TABLE #TT;

CREATE LOCAL TEMPORARY TABLE #TT ( "NombreParametro" nvarchar(100) , "Valor" nvarchar(100));

INSERT INTO #TT SELECT 'IdEmpresa','1' FROM DUMMY;

INSERT INTO #TT SELECT 'IdUsuario','0' FROM DUMMY;

INSERT INTO #TT SELECT 'FechaDesde','2023-11-16' FROM DUMMY;
INSERT INTO #TT SELECT 'FechaHasta','2023-11-19' FROM DUMMY;

CALL "BSCL.Plataforma.BD.Servicios::BAT.AgregarPrograma"
(      p_IdEmpresa                  => 1,
       p_IdUsuario                  => 1,
       p_IdModulo                   => 'PLA',
       p_IdAgenda                   => 0,
       p_IdTipoTarea                => 'RECALCULO_PATRIMONIO',
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


select "FechaPatrimonio" ,count(1) from "BSCL.CuentaInversion.BD::CTA.PatrimonioCaja" bcibcpc  group by "FechaPatrimonio" 


select top 10 * 
from "BSCL.CuentaInversion.BD::CTA.PatrimonioCaja" bcibcpc 
