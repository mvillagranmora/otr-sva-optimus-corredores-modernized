UPDATE "BSCL.Custodia.BD::CUS.Configuracion"
SET "Valor" = :p_Valor
WHERE	"IdEmpresa" 			= :p_IdEmpresa
		AND "CodigoParametro"	= :p_CodigoParametro