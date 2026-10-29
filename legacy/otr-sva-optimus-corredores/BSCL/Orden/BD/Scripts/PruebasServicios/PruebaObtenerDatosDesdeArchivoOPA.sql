SET SCHEMA "BSCL";

drop TABLE #TT;
CREATE LOCAL TEMPORARY TABLE #TT (
		"IdInterno" 	BIGINT,
		"OPARUT"				NVARCHAR(40),
		"OPACODIGOPORTAFOLIO"	NVARCHAR(40),
		"OPACANTIDAD"			DECIMAL(26,8)		
);
	
INSERT INTO #TT
SELECT 1,'15731790-3','1',300
FROM DUMMY;

CALL "BSCL"."BSCL.Orden.BD.Servicios::ObtenerDatosDesdeArchivoOPA"(1,4,'ORD',70270961,#tt,?,?,?,?,?,?,?);

'v_in_Portafolios';IdInterno;IdPortafolio;IdTipoProducto;IdTipoOperacion;IdCanalIngreso;IdBolsa;IdCorredorCorresponsal
v_in_Portafolios;1;343.710;INAC;COMACN;TRA;BCS;?