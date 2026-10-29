/********************************************************** 
Proposito	: Script para obtener las carteras para vencer
autor		: JG
fecha		: 01/02/2017
***********************************************************/
select
	c."FolioEmprestito" as "FolioEmprestito"
FROM "BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenEmprestitoVigente" c
INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" s on s."IdCliente" = c."IdCliente"
LEFT JOIN  "BSCL.Custodia.BD::PRT.TipoPrestamo" AS TipPre ON (TipPre."IdTipoPrestamo" = c."IdTipoPrestamo")
WHERE c."IdEmpresa" = :p_IdEmpresa AND c."FolioPrestamo"  in (:listaFolioPrestamo);
