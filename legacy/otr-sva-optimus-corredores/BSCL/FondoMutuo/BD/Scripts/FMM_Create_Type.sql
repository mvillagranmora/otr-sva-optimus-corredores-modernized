SET SCHEMA "BSCL";
	

drop type "BSCL.FondoMutuo.BD.Tipos::SalidaCostoFactorAjuste" cascade;
drop type "BSCL.FondoMutuo.BD.Tipos::EntradaCostoFactorAjuste" cascade;


/*==============================================================*/
/* Create Type:                                                 */
/*==============================================================*/
CREATE TYPE "BSCL.FondoMutuo.BD.Tipos::SalidaCostoFactorAjuste" AS TABLE (
		"NumeroCalculoCostoFactorAjuste"	BIGINT,
		"CantidadRescate"					DECIMAL(26,8),
		"CantidadTotalCartera"				DECIMAL(26,8),
		"CostoFactorAjusteTotalCartera"		DECIMAL(26,8),
		"CostoFactorAjusteRescate"			DECIMAL(26,8),
		"CostoFactorAjusteRestante"			DECIMAL(26,8),
		"Resultado"							NVARCHAR(3),
		"Codigo"							NVARCHAR(100)
   
   );
CREATE TYPE "BSCL.FondoMutuo.BD.Tipos::EntradaCostoFactorAjuste" AS TABLE (
		"NumeroCalculoCostoFactorAjuste"	BIGINT,
		"CantidadRescate"					DECIMAL(26,8),
		"CantidadTotalCartera"				DECIMAL(26,8),
		"CostoFactorAjusteTotalCartera"		DECIMAL(26,8)
   
   );
/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
COMMIT;
