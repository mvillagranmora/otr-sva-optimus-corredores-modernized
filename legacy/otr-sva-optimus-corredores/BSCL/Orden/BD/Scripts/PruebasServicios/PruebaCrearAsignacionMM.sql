SET SCHEMA "BSCL";
drop TABLE #TT;
CREATE LOCAL TEMPORARY TABLE #TT (
		"IdInterno" BIGINT, 
		"IdOrden" BIGINT, 
		"IdTransaccion" BIGINT, 
		"Cantidad" DECIMAL(26,8), 
		"ValorRescate" DECIMAL(26,8)
);


INSERT INTO #TT ("IdInterno","IdOrden","IdTransaccion","Cantidad","ValorRescate")
SELECT 0,305172,42317081,150000,0 from dummy;


--SELECT "IdAsignacionResultado" as "IdInterno", "IdOrdenPaso" as "IdOrden", 
--"IdTransaccionPaso" as "IdTransaccion", "CantidadAsignada" as "Cantidad", "ValorRescate" 
--from "BSCL"."BSCL.Orden.BD::ORD.AsignacionResultado" 
--LEFT JOIN "BSCL"."BSCL.Orden.BD::ORD.Transaccion"on "IdTransaccionPaso" = "IdTransaccion"where "IdPrograma" = 83907;

--select * from #TT
-- IdAsignacionResultado;IdOrdenPaso;IdTransaccionPaso;CantidadAsignada;ValorRescate
--737.429;70.440;49.804;3.000;0
--SET 'DEPURACION' = 'S'; 
CALL "BSCL"."BSCL.Orden.BD.Servicios::CrearAsignacionMM"(1,1,'ORD','',#tt,?,?,?,?,?,?);


-- select "EsAPlazo","IdTransaccionPlazo",* from "BSCL"."BSCL.Orden.BD::ORD.Transaccion" where "IdTransaccion" in (49828,49827)
