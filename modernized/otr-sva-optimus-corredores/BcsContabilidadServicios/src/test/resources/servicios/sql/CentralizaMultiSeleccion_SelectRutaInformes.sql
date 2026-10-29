SELECT
	"IdTipoCentralizacion" , "InformeAsociado" , "LlaveInformeAsociado"
FROM
	BSCL."BSCL.Contabilidad.BD::ITZ.CentralizacionTipo"
WHERE "RequiereFechaConsulta"  = 'S';