SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::ILS.Consultap_SaldosCuentascontables";

CREATE VIEW "BSCL.Normativa.BD.Vistas::ILS.Consultap_SaldosCuentascontables" 
AS

SELECT    "IdCodigoContabilidad"
    	 ,"MontoDebe" 	 
		 ,"MontoHaber"  
FROM "BSCL.Normativa.BD::ILS.p_SaldoCuentaContable";

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::ILS.Consultap_SaldosCuentascontables" TO BCS_DESARROLLADOR WITH GRANT OPTION;
