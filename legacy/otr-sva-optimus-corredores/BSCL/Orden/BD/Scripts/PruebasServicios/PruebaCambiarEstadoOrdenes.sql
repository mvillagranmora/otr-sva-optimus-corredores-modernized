SET SCHEMA "BSCL";
drop TABLE #TT2;
CREATE LOCAL TEMPORARY TABLE #TT2(
	"IdOrden" BIGINT,
	"Estado"	NVARCHAR(10)
);

INSERT INTO #TT2 ("IdOrden","Estado")
SELECT 47806,'ANULAR' FROM DUMMY;
--INSERT INTO #TT2 ("IdOrden","Estado")
--SELECT 10000000,'ANULAR' FROM DUMMY;

CALL "BSCL"."BSCL.Orden.BD.Servicios::CambiarEstadoOrdenes" (
	p_IdEmpresa                        => 1,
    p_IdUsuario                        => 1,
    p_IdModulo                         => 'ORD',
	p_Ordenes						  	=> #TT2,
	
	p_out_AdvertenciasContador			=> ?,
	p_out_AdvertenciasDetalle			=> ?,
	p_out_ErroresContador				=> ?,
	p_out_ErroresDetalle				=> ?
	
	
	);
	
	--select * from "BSCL"."BSCL.Orden.BD::ORD.Orden" where "IdOrden" = 10000000
	select * from "BSCL"."BSCL.Orden.BD::ORD.Orden" where "IdOrden" = 1