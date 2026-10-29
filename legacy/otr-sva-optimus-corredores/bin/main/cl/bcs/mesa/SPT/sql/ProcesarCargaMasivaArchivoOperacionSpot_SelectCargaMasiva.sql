SELECT 
cms."IdCargaMasiva"
, cms."FechaCarga"
, TO_NVARCHAR(cms."FechaProceso") AS "FechaProceso"
, cms."CantidadRegistros"
, cms."IdEmpresa"
, cms."IdUsuario"
, cms."IdEstado"
, cms."NombreArchivo"
, cms."IdTipoCargaMasiva" 
, cmt."CodigoTipoCargaMasiva"
FROM "BSCL.Mesa.BD::CAR.CargaMasivaSpot" cms
left join "BSCL.Mesa.BD::CAR.TipoCargaMasivaSpot" cmt on (cms."IdTipoCargaMasiva" = cmt."IdTipoCargaMasiva")
WHERE cms."IdCargaMasiva" IN (:IdCargaMasiva);