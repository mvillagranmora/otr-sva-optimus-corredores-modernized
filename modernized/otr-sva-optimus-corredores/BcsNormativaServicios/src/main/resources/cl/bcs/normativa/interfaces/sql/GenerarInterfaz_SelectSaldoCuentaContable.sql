   SELECT "IdCodigoContabilidad" AS "CUENTA",
		  COALESCE("MontoDebe", 0) -  COALESCE("MontoHaber", 0) AS "SALDO"   
   FROM "BSCL.Normativa.BD::ILS.p_SaldoCuentaContable"
   WHERE "IdCodigoContabilidad" IN (:Cuentas); 
  