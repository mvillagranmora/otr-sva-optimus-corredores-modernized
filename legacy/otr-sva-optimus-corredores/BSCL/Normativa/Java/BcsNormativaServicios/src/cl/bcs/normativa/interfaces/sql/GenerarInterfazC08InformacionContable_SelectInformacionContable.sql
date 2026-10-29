 SELECT i."CuentaContabilidad" ,
        COALESCE(h."NombreCuenta", 'NO HOMOLOGADO') AS "NombreCuenta",
        COALESCE(h."TipoCuenta", 'X') AS "TipoCuenta",        
		i."C08CodigoSBIF" AS "SBIF",
		i."C08Imprime" AS "Imprime",
		i."C08Codigo" AS "Codigo",
		i."C08Banda" AS "Banda",
		COALESCE(s."MontoDebe", 0) - COALESCE(s."MontoHaber", 0) AS "Saldo"
   FROM "BSCL.Contabilidad.BD::ITZ.InterfazCuenta" i
   LEFT JOIN "BSCL.Contabilidad.BD::ITZ.HomologaCuenta" h ON i."CuentaContabilidad" = h."CuentaContabilidad"   
   LEFT JOIN "BSCL.Normativa.BD::ILS.p_SaldoCuentaContable" s ON h."CuentaContabilidad" = s."IdCodigoContabilidad"
  WHERE i."C08Imprime" IN ('S', 'Z') 
  ORDER BY 1