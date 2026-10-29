SET SCHEMA "BSCL";
drop TABLE #cobros;
CREATE LOCAL TEMPORARY TABLE #cobros (
		"IdOperacion"				BIGINT,
		"IdCobro" 					NVARCHAR(10),
		"ValorCobroPerfil"			DECIMAL(26,8),
		"MontoMinimo" 				DECIMAL(26,8),
		"IdMoneda" 					NVARCHAR(3),
		"FormaCobro" 				NVARCHAR(1),
		"CodigoCobroClasificacion" 	NVARCHAR(40),
		"Cantidad" 					DECIMAL(26,8),
		"MontoOperacion"			DECIMAL(26,8),
		"ValorRescate"				DECIMAL(26,8),
		"DiasPlazo"					BIGINT,
		"PermiteCambiarValor"		NVARCHAR(1)
);
drop TABLE #CobrosModificados;
CREATE LOCAL TEMPORARY TABLE #CobrosModificados (
		"IdOperacion" 			BIGINT,
		"IdCobro" 				NVARCHAR(40),
		"ValorCobroAplicado"	DECIMAL(26,8)
);

insert into #cobros
select 0,'IVA',19,0,'CLP','V','IMPUESTO',15000,94218000,0,0,'N' from dummy;

insert into #cobros
select 0,'GFOD',5000,0,'CLP','F','GASTO',15000,94218000,0,0,'N' from dummy;

insert into #cobros
select 0,'DEFI',2000,0,'CLP','V','DERECHO',15000,94218000,0,0,'N' from dummy;

insert into #cobros
select 0,'COVA',0.5,0,'CLP','V','COMISION',15000,94218000,0,0,'N' from dummy;

CALL "BSCL.Plataforma.BD.Servicios::COM.CalcularCobros"(
    p_IdEmpresa            	=> 1,
    p_Cobros		 		=> #cobros,
    p_CobrosModificados		=> #CobrosModificados ,
    p_out_Cobros  => ?);   
    
    CALL "BSCL.Plataforma.BD.Servicios::COM.CalcularCobros2"(
    p_IdEmpresa            	=> 1,
    p_Cobros		 		=> #cobros,
    p_CobrosModificados		=> #CobrosModificados ,
    p_out_Cobros  => ?);   

rollback
