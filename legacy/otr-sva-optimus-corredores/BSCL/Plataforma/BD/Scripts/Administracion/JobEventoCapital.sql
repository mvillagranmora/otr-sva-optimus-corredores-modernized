SELECT bscl."BSCL.Custodia.BD.Secuencias::EventoCapital".nextval FROM dummy
SELECT bscl."BSCL.Custodia.BD.Secuencias::EventoCapital".CURRVAL FROM dummy

select top 20 * from "BSCL.Custodia.BD::CUS.EventoCapital" order by "FechaHoraIngresoRegistro" desc



MAX(IdEventoCapital)
6.803.219

BSCL.Custodia.BD.Secuencias::EventoCapital.NEXTVAL
6.803.220


CREATE LOCAL TEMPORARY TABLE #TablaParametros (
    "NombreParametro" nvarchar(100) , "Valor" nvarchar(100)
);GO

 



INSERT INTO #TablaParametros ("NombreParametro", "Valor")
SELECT 'IdEmpresa','1' FROM DUMMY;GO
INSERT INTO #TablaParametros ("NombreParametro", "Valor")
SELECT 'IdUsuario',0 FROM DUMMY;GO
INSERT INTO #TablaParametros ("NombreParametro", "Valor")
SELECT 'IdModulo','CUS' FROM DUMMY;GO
INSERT INTO #TablaParametros ("NombreParametro", "Valor")
SELECT 'FechaSistema','2020-05-18' FROM DUMMY;GO

 


UPDATE #TablaParametros
SET "Valor"='2020-04-29'
WHERE "NombreParametro"='FechaSistema'
            
SELECT  * FROM #TablaParametros;
--DELETE  FROM #TablaParametros;



CALL BSCL."BSCL.Plataforma.BD.Servicios::BAT.AgregarPrograma"
 (
    p_IdEmpresa                 => 1,
    p_IdUsuario                 => 0,
    p_IdModulo                  => 'CUS',
    p_IdAgenda                  => 0,
    p_IdTipoTarea               => 'CARGAEVENTOCAPITALDESDEBURSATIL',
    p_Segundos                  => 0,
    p_TablaParametros           => #TablaParametros,
    p_CreaParam                 => 'S',
    p_out_IdPrograma            => ?,
    p_out_AdvertenciasContador  => ?,
    p_out_AdvertenciasDetalle   => ?,
    p_out_ErroresContador       => ?,
    p_out_ErroresDetalle        => ?                            
);GO


COMMIT;