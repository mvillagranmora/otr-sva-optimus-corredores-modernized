SELECT 
	--RANK() over (PARTITION BY COALESCE("IdCodigoContabilidadPadre",'') ORDER BY "IdCodigoContabilidad") AS "Id",
	S."IdCodigoContabilidad",
	COALESCE(S."IdCodigoContabilidadPadre",'') AS "IdCodigoContabilidadPadre",
	S."MontoDebe" ,	 
	S."MontoHaber",
	--"FechaContable",												
	S."Nivel",
	H."Imputable",
	H."NombreCuenta",
	COALESCE(H."TipoCuenta",'') AS "TipoCuenta"
FROM "BSCL.Normativa.BD::ILS.p_SaldoCuentaContable" AS S
	LEFT  JOIN "BSCL.Contabilidad.BD::ITZ.HomologaCuenta" AS H ON (H."CuentaContabilidad" = S."IdCodigoContabilidad")
--WHERE S."Nivel" <= 3--"IdCodigoContabilidadPadre" = '110000000000'
ORDER BY S."IdCodigoContabilidadPadre","IdCodigoContabilidad";

--SELECT * FROM "BSCL.Contabilidad.BD::ITZ.HomologaCuenta"
--SELECT count(*) FROM "BSCL.Normativa.BD::ILS.p_SaldoCuentaContable" 