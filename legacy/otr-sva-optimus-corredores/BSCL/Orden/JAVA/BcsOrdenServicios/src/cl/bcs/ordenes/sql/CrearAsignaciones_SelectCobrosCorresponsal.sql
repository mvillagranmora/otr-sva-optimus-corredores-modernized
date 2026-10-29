/********************************************************** 
Proposito	: consulta de cobros de corresponsales
autor		: RU
fecha		: 01/02/2017
***********************************************************/
SELECT "IdCorredorCorresponsal" as "IdCorredoraCorresponsal","BSCL.Orden.BD.Secuencias::CobroAsignacion".NEXTVAL as "IdCobroAsignacion",
corr."IdCobro",corr."ValorCobro" AS "ValorCobroAplicado",
cob."ValorTopeMinimoOrden" AS "MontoMinimo","IdMoneda","FormaCobro","CodigoCobroClasificacion","CodigoCobroClasificacion" AS "CodigoCobroClasificacionBase",
'' AS "IdPerfilCobro"

FROM "BSCL.Orden.BD::ORD.CorredorCorresponsal" corr
INNER JOIN "BSCL.Plataforma.BD.Vistas::COM.ConsultarCobro" cob on cob."IdCobro" = corr."IdCobro"
WHERE corr."IdCorredorCorresponsal" in (:idsCorredorCorresponsal)
