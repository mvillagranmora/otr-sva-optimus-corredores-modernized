/**************************************************************
Proposito	: Script consulta cobros de la orden para notificar
autor		: RU
fecha		: 01/02/2017
***********************************************************/

SELECT
	"IdOrden",
	MAX( CASE WHEN "CodigoCobroClasificacion" = 'COMISION' THEN "Valor" END ) AS "COMISION",
	MAX( CASE WHEN "CodigoCobroClasificacion" = 'IMPUESTO' THEN "Valor" END ) AS "IMPUESTO",
	MAX( CASE WHEN "CodigoCobroClasificacion" = 'GASTO' THEN "Valor" END ) AS "GASTO",
	MAX( CASE WHEN "CodigoCobroClasificacion" = 'DERECHO' THEN "Valor" END ) AS "DERECHO"
FROM
	(
		SELECT
			co."IdOrden",
			co."IdClaseCobroBase" AS "CodigoCobroClasificacion",
			SUM( co."MontoCobroCalculado" ) AS "Valor"
		FROM "BSCL.Orden.BD.Vistas::ConsultarCobrosOrden" co
		WHERE co."IdOrden" IN ( :ListaIdOrden )
		GROUP BY
			co."IdOrden",
			co."IdClaseCobroBase"
	)
GROUP BY
	"IdOrden"
