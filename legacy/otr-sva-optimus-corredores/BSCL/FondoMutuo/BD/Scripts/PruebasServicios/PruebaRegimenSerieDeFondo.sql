SET SCHEMA BSCL;
DROP TABLE #regimen;
create local temporary table #regimen(
    								"IdInterno"				BIGINT,
    								"IdRegimenFondoMutuo"	NVARCHAR(40),
    								"IdSerieFondo"			NVARCHAR(40)
    							);


INSERT INTO #regimen("IdInterno","IdRegimenFondoMutuo","IdSerieFondo")
SELECT 0,'ART108','PRUEBA_245' FROM DUMMY;

CALL "BSCL.FondoMutuo.BD.Servicios::CrearRegimenSerieDeFondo"(
	--VARIABLES OBLIGATORIAS
     p_IdEmpresa 				=> 1,
     p_RegimenSerieFondo 		=> #regimen,
     p_out_Resultado			=> ?
)