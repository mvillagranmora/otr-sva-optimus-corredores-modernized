WITH ConsultaComisionesNN AS (
	SELECT CLI."DocumentoIdentidad",
	       CLI."NombreCompleto",
	       Portafolio."CodigoPortafolio",
	       CAR."Nemotecnico" AS "IdSerieFondo",
	       CAR."FolioCartera",
	       CAR."IdCartera",
	       :FechaConsultaHasta AS "FechaHasta",
	       CAR."FechaLiquidacion" AS "FechaCompra",
	       days_between((CASE WHEN :FechaConsultaDesde > CAR."FechaLiquidacion" THEN :FechaConsultaDesde ELSE CAR."FechaLiquidacion" END) , :FechaConsultaHasta)  AS "DiasPermanencia",
	       TramoComision."PorcentajeComision" AS "ComisionAnual",
	       ((TramoComision."PorcentajeComision") / 365 ) AS "ComisionDiaria",
	       CAR."INGCantidad" AS "CuotasCliente",
	       ( days_between((CASE WHEN :FechaConsultaDesde > CAR."FechaLiquidacion" THEN :FechaConsultaDesde ELSE CAR."FechaLiquidacion" END) , :FechaConsultaHasta)  * ((TramoComision."PorcentajeComision" / 100) / 365) * 
	       		(1 + (CB."ValorCobro" / 100)) * CAR."INGCantidad" ) AS "ComisionNN",
	       CAR."INGCantidad" - ( days_between((CASE WHEN :FechaConsultaDesde > CAR."FechaLiquidacion" THEN :FechaConsultaDesde ELSE CAR."FechaLiquidacion" END) , :FechaConsultaHasta)  * ((TramoComision."PorcentajeComision" / 100) / 365) * 
				(1 + (CB."ValorCobro" / 100)) * CAR."INGCantidad") AS "CuotasFinalesCliente",
	       CAR."IdCliente" AS "IdCliente",
	       CAR."IdPortafolio" AS "IdPortafolio",
	       INS."IdMoneda" AS "IdMoneda",
	       CAR."IdRegimenFondoMutuo" AS "IdRegimenTributario",
	       CAR."INGMontoATasaMercadoActual" AS "MontoOriginal"
	FROM "BSCL.Custodia.BD::HIS.Cartera" AS CAR
	LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" as INS on CAR."IdInstrumento" = INS."IdInstrumento" 
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLI ON (CLI."IdCliente" = CAR."IdCliente")
	LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = CAR."IdPortafolio" AND Portafolio."IdCliente" = CAR."IdCliente"
	LEFT JOIN "BSCL.FondoMutuo.BD::FMM.Comision" AS Comision ON Comision."IdSerieFondo" = CAR."Nemotecnico"
	LEFT JOIN "BSCL.FondoMutuo.BD::FMM.TramoComision" AS TramoComision ON TramoComision."IdComision" = Comision."IdComision" AND TramoComision."Estado" = 'ACT'
	LEFT JOIN "BSCL.Plataforma.BD::COM.Cobro" AS CB ON CB."IdCobro" = 'IVA' AND CB."IdEmpresa" = :IdEmpresa
	LEFT JOIN "BSCL.Custodia.BD::CUS.ComisionNNProcesada" AS ComisionProcesada ON ComisionProcesada."IdCartera" = CAR."IdCartera" AND ComisionProcesada."FolioCartera" = CAR."FolioCartera" AND ComisionProcesada."IdEmpresa" = :IdEmpresa AND ComisionProcesada."FechaHasta" = :FechaConsultaHasta AND ComisionProcesada."FechaDesde" = :FechaConsultaDesde
	WHERE CAR."IdCartera" IN (:IdCartera) AND CAR."IdEmpresa" = :IdEmpresa
	AND CAR."FechaRespaldo" = :FechaHastaUltimoDiaHabil
	AND CAR."IdTipoInstrumento" = 'FFM' 
	AND (CAR."FechaLiquidacion" <= :FechaHastaUltimoDiaHabil)
	AND CAR."IdEstado" = 'VIG'
	AND CAR."IdClasificacionTipoInstrumento" = 'FM'
	AND Comision."TipoComision" = 'PER'
	AND TramoComision."PorcentajeComision"  > 0
	AND COALESCE(ComisionProcesada."FechaProcesamiento", '0001-01-01') = '0001-01-01'
	AND COALESCE(ComisionProcesada."Estado", 'ANU') <> 'PRO'
)
SELECT 
	"DocumentoIdentidad",
	"NombreCompleto",
	"CodigoPortafolio",
	"IdSerieFondo",
	"FolioCartera",
	"IdCartera",
	"FechaHasta",
	"FechaCompra",
	"DiasPermanencia",
	"ComisionAnual",
	"ComisionDiaria",
	"CuotasCliente",
	"ComisionNN",
	"CuotasFinalesCliente",
	"IdCliente",
	"IdPortafolio",
	"IdMoneda",
	"IdRegimenTributario",
	"MontoOriginal"
FROM ConsultaComisionesNN
WHERE "ComisionNN" >= 0.001;

