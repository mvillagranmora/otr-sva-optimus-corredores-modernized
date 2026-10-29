SET SCHEMA "BSCL";
DROP table #ValoresCuota;

create local temporary table #ValoresCuota(
	"IdInterno"			   BIGINT,
	"Fecha"				   DATE,
	"IdSerieFondo"		   NVARCHAR(40),
 	"CalculoPorMontoCuota" NVARCHAR(1),
 	"Monto"				   DECIMAL(26,8),
 	"NumeroCuotas"		   DECIMAL(26,8),
 	"IdMonedaFondo"	   NVARCHAR(3),
 	"IdMonedaInversion"	   NVARCHAR(3),
 	"TipoSolicitud"		   NVARCHAR(3),
 	"TipoFactorAjuste"	   NVARCHAR(3),
 	"IdEmpresa"			   BIGINT
);


INSERT INTO #ValoresCuota
SELECT  0 AS "IdInterno"
		,'2016-11-30' AS "Fecha"
	  	,'FMLIQUIDEZ_A' AS "IdSerieFondo"
	  	,'C' AS "CalculoPorMontoCuota"
	  	,120.88 AS "Monto"
	  	,22 AS "NumeroCuotas"
	  	,'USD' AS "IdMonedaFondo"
	  	,'USD' AS "IdMonedaInversion"
	  	,'RES' AS "TipoSolicitud"
	  	,'UF' AS "TipoFactorAjuste"
	  	,1 AS "IdEmpresa"
FROM DUMMY;

CALL "BSCL.FondoMutuo.BD.Utilitarios::CalcularMontoNumeroCuota"(p_IdEmpresa=>1,p_Series=>#ValoresCuota,p_out_Resultado=>?);
