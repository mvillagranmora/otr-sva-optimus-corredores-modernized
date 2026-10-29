 SELECT i."CuentaContabilidad",
        COALESCE(h."NombreCuenta", 'NO HOMOLOGADO') AS "NombreCuenta",
        COALESCE(h."TipoCuenta", 'X') AS "TipoCuenta",        
		i."C40CodigoSBIF" AS "CodigoSBIF",
		i."C40Imprime" AS "C40Imprime",
		i."C40Codigo" AS "C40Codigo",
		i."C40Banda" AS "C40Banda",
		CASE WHEN COALESCE(s."MontoHaber", 0) - COALESCE(s."MontoDebe", 0) > 0 THEN COALESCE(s."MontoHaber", 0) - COALESCE(s."MontoDebe", 0)
		ELSE 0 END AS "Saldo"   FROM "BSCL.Contabilidad.BD::ITZ.InterfazCuenta" i
   LEFT JOIN "BSCL.Contabilidad.BD::ITZ.HomologaCuenta" h ON i."CuentaContabilidad" = h."CuentaContabilidad"   
   LEFT JOIN "BSCL.Normativa.BD::ILS.p_SaldoCuentaContable" s ON h."CuentaContabilidad" = s."IdCodigoContabilidad"
  WHERE i."C40Imprime" IN ('S', 'Z') 
  ORDER BY 1
  
  
