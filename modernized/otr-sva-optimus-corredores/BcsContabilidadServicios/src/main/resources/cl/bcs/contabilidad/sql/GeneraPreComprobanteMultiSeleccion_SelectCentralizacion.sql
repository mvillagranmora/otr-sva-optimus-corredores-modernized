SELECT 
"IdTipoCentralizacion"
,"IdCentralizacion"
,"IdTipoContabilidad"
,"Nombre"
FROM (
SELECT 
row_number() OVER (partition by CTB_itzContabilidadTipoCentralizacionTipo."IdTipoContabilidad" order BY CTB_itzCentralizacion."IdCentralizacion" DESC) AS "Row"
,CTB_itzCentralizacion."IdTipoCentralizacion"
,CTB_itzCentralizacion."IdCentralizacion"
,CTB_itzContabilidadTipoCentralizacionTipo."IdTipoContabilidad"
,CT."Nombre"
FROM 
"BSCL.Contabilidad.BD::ITZ.Centralizacion" AS CTB_itzCentralizacion
INNER JOIN "BSCL.Contabilidad.BD::ITZ.CentralizacionTipo" CT ON CTB_itzCentralizacion."IdTipoCentralizacion" = CT."IdTipoCentralizacion"
INNER JOIN "BSCL.Contabilidad.BD::ITZ.ContabilidadTipoCentralizacionTipo" AS CTB_itzContabilidadTipoCentralizacionTipo
ON  CTB_itzCentralizacion."IdTipoCentralizacion" 					= CTB_itzContabilidadTipoCentralizacionTipo."IdTipoCentralizacion" 
AND CTB_itzContabilidadTipoCentralizacionTipo."IdTipoContabilidad" 	IN (:IdTipoContabilidad) 
AND CTB_itzContabilidadTipoCentralizacionTipo."Estado" 				= 'V' 
AND CTB_itzCentralizacion."FechaContable" 							= :FechaContable
AND CTB_itzCentralizacion."IdEmpresa"								= :IdEmpresa
AND CTB_itzCentralizacion."Estado" 									= 'V'
) WHERE "Row" = 1;