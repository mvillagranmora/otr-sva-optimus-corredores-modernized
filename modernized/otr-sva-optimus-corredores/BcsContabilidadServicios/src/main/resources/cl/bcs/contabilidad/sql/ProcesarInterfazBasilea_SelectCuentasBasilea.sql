SELECT
	"Concepto" AS "ConceptoAInformar",
	"TipoCuentaContable" ,
	"CuentaContable" ,
	"NombreCuentaContable" ,
	"IdEstado" ,
	"Producto" ,
	"Grupo" ,
	"IdTipoInstrumento" ,
	"IdClasificacionTipoInstrumento" ,
	"RelacionadoCorredora" ,
	"IdBolsa" ,
	"EsVentaCorta" ,
	"IdEmisor" ,
	"Nemotecnico" ,
	"IdTipoOperacion"
FROM
	"BSCL.Contabilidad.BD::ITZ.InterfazCuentasBasilea"
WHERE "IdEstado" = 'ACT';