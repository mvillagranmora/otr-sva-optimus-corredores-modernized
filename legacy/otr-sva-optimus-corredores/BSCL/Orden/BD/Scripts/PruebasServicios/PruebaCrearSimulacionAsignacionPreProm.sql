SET SCHEMA "BSCL";
drop TABLE #TT;
CREATE LOCAL TEMPORARY TABLE #TT (
		"IdMovimiento" 	BIGINT,
		"IdTransaccion"	BIGINT,
		"Cantidad" DECIMAL (26,8),
		"Monto" DECIMAL (26,8),
		"ValorRescate" DECIMAL (26,8)
		);

drop TABLE #TT2;
CREATE LOCAL TEMPORARY TABLE #TT2 (
		"IdPortafolio"	BIGINT, 
		"Cantidad" DECIMAL (26,8), 
		"Porcentaje" DECIMAL (26,8)
		
);

INSERT INTO #TT ("IdMovimiento","IdTransaccion","Cantidad","Monto","ValorRescate")
SELECT 70285832 as "IdMovimiento",70285832 as "IdTransaccion",5000 as "Cantidad",0 as "Monto",0 as "ValorRescate" from dummy;

INSERT INTO #TT2 ("IdPortafolio","Cantidad","Porcentaje")
SELECT 343710 as "IdPortafolio",0 as "Cantidad",20 as "Porcentaje" from dummy;
INSERT INTO #TT2 ("IdPortafolio","Cantidad","Porcentaje")
SELECT 1450060 as "IdPortafolio",0 as "Cantidad",50 as "Porcentaje" from dummy;
INSERT INTO #TT2 ("IdPortafolio","Cantidad","Porcentaje")
SELECT 1450057 as "IdPortafolio",0 as "Cantidad",30 as "Porcentaje" from dummy;

rollback

CALL "BSCL.Orden.BD.Servicios::CrearSimulacionAsignacionPostrera" (
	p_IdEmpresa 			=> 1,
	p_IdTipo 				=> 'ASIG',
	p_Origen 				=> '',
	p_Transacciones 		=> #TT,
	p_Portafolios 				=> #TT2,
	p_out_SimAsignacion 		=> ?);
	
CALL "BSCL.Orden.BD.Servicios::CrearSimulacionAsignacionPreProm" (
	p_IdEmpresa 			=> 1,
	p_IdTipo 				=> 'ASIG',
	p_Origen 				=> '',
	p_Transacciones 		=> #TT,
	p_Portafolios 				=> #TT2,
	p_out_SimAsignacion 		=> ?);
	
	
						
