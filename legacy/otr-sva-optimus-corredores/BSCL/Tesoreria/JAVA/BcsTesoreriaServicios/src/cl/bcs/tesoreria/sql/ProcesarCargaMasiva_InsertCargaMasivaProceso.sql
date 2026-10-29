INSERT INTO "BSCL.Tesoreria.BD::GBL.ProcesarCargaMasiva_Proceso" 
(
	"IdCargaMasiva",
	"CodigoTipoCargaMasiva",
	"IdMoneda",
	"IdConcepto",
	"IdMedioDePago",
	"IdCuentaBanco"
)
VALUES
(
	:IdCargaMasiva,
	:CodigoTipoCargaMasiva,
	:IdMoneda,
	:IdConcepto,
	:IdMedioDePago,
	:IdCuentaBanco
);
