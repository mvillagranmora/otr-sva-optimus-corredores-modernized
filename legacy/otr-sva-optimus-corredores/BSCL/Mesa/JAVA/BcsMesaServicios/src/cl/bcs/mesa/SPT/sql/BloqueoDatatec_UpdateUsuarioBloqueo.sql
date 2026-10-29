UPDATE "BSCL.Mesa.BD::MES.ConfiguracionMesa"
SET
	"Valor" = :Valor,
	"ModificadorParametro" = :ModificadorParametro
WHERE "CodigoParametro" = 'USUARIOBLOQUEODATATEC'
AND "IdEmpresa" = :IdEmpresa
--AND "Valor" <> 'S'
;