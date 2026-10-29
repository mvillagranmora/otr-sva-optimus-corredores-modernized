SET SCHEMA "BSCL";
drop TABLE #TT;
CREATE LOCAL TEMPORARY TABLE #TT (
		"IdOrden" BIGINT
);

insert into #TT
select 309711 from dummy;

CALL "BSCL.Orden.BD.Servicios::AnularOrdenMasiva"
(	p_IdEmpresa  	=> 1 ,
	p_IdUsuario  	=> 3 ,
	p_IdModulo  	=> 'ORD' ,
	p_Ordenes => #tt ,
	p_out_InformacionContador => ?,
	p_out_InformacionDetalle => ?,
	p_out_AdvertenciasContador => ?,
	p_out_AdvertenciasDetalle => ?,
	p_out_ErroresContador => ?,
	p_out_ErroresDetalle => ?
);


SELECT * FROM "RUEBAS"."TBSOLICITUDES" WHERE "SEC_ORDEN" = 309707

SELECT "IdBolsaDistribucion",* FROM "BSCL"."BSCL.Orden.BD::ORD.Orden" WHERE "IdOrden" = 30976

update "BSCL"."BSCL.Orden.BD::ORD.Orden" set "IdEstadoOrden"=  'ENR' where "IdOrden" = 309711
/*

SELECT ord."IdOrden", SUM("CantidadAsignada")  as "CantidadAsignaciones"
			FROM #TT res_ord 
			INNER JOIN "BSCL.Orden.BD::ORD.Orden" ord ON res_ord."IdOrden" = ord."IdOrden"
			LEFT OUTER JOIN "BSCL.Orden.BD::ORD.Asignacion" asig ON asig."IdOrden" = ord."IdOrden" and asig."IdEstadoAsignacion" != 'ANU' 
			GROUP BY ord."IdOrden"
*/