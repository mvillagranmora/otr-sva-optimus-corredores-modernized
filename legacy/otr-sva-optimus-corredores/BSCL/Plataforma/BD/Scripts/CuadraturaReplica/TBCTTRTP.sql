
	select 
	sebra.cant,optimus.cant,* 	
	from 
	(
		SELECT RUT_CLI,SEC_RUT_CLI,NEMO,SUM(CANTIDAD) cant
		FROM BSCL."2022_05_31_TBCTTRTP" 
		WHERE "COD_PER" = '2022-05' 
		GROUP BY RUT_CLI,SEC_RUT_CLI,NEMO
	) as sebra
	,
	(
		SELECT RUT_CLI,SEC_RUT_CLI,NEMO,SUM(CANTIDAD) cant 
		FROM BSCL."BSCL.Replica.BD.Vistas::Sebra.TBCTTRTP"
		WHERE "COD_PER" = '2022-05' 
		GROUP BY RUT_CLI,SEC_RUT_CLI,NEMO
	) as optimus
	where sebra.RUT_CLI = optimus.RUT_CLI 
	AND sebra.SEC_RUT_CLI = optimus.SEC_RUT_CLI 
	AND sebra.NEMO = optimus.NEMO
	AND sebra.cant != optimus.cant
	

--MONTO
	select *
	from 
	(
		SELECT RUT_CLI,SEC_RUT_CLI,NEMO,SUM(MONTO) mont 
		FROM BSCL."2022_05_31_TBCTTRTP" 
		WHERE "COD_PER" = '2022-05' 
		GROUP BY RUT_CLI,SEC_RUT_CLI,NEMO
	) as sebra
	,
	(
		SELECT RUT_CLI,SEC_RUT_CLI,NEMO,SUM(MONTO) mont 
		FROM BSCL."BSCL.Replica.BD.Vistas::Sebra.TBCTTRTP"
		WHERE "COD_PER" = '2022-05' 
		GROUP BY RUT_CLI,SEC_RUT_CLI,NEMO
	) as optimus
	where sebra.RUT_CLI = optimus.RUT_CLI 
	AND sebra.SEC_RUT_CLI = optimus.SEC_RUT_CLI 
	AND sebra.NEMO = optimus.NEMO
	AND sebra.mont != optimus.mont
	AND ABS(sebra.mont-optimus.mont) > 40
		

--PRECIO
	select *
	from 
	(
		SELECT RUT_CLI,SEC_RUT_CLI,NEMO,AVG(PRECIO) precio,avg(tasa) as tasa
		FROM BSCL."2022_05_31_TBCTTRTP" 
		WHERE "COD_PER" = '2022-05' 
		GROUP BY RUT_CLI,SEC_RUT_CLI,NEMO
	) as sebra
	,
	(
		SELECT RUT_CLI,SEC_RUT_CLI,NEMO,AVG(PRECIO) precio,avg(tasa) as tasa
		FROM BSCL."BSCL.Replica.BD.Vistas::Sebra.TBCTTRTP"
		WHERE "COD_PER" = '2022-05' 
		GROUP BY RUT_CLI,SEC_RUT_CLI,NEMO
	) as optimus
	where sebra.RUT_CLI = optimus.RUT_CLI 
	AND sebra.SEC_RUT_CLI = optimus.SEC_RUT_CLI 
	AND sebra.NEMO = optimus.NEMO
	AND (ABS(sebra.precio-optimus.precio) > 0.0001 or ABS(sebra.tasa-optimus.tasa) > 0.0001)
	
	
	select * from BSCL."BSCL.Replica.BD.Vistas::Sebra.TBCTTRTP"