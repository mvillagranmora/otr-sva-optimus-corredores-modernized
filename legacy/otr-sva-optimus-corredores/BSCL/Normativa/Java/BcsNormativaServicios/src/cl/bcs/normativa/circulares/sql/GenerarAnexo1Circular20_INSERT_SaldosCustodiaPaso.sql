INSERT INTO "BSCL.Normativa.BD::CIR.GenerarAnexo1Circular20_SaldosCustodia_Paso"
( 
	"CantidadCustodia",
	"CodigoPortafolio",
	"FechaNacimiento",
	"IdCliente",
	"IdMoneda",
	"IdPlanTipoInversion",
	"IdPortafolio",
	"PrecioMercadoCustodia",
	"SecuenciaSolicitud",
	"Sexo",
	"Nemotecnico"
)
SELECT CUS."TotalPosicion" AS "CantidadCustodia",CUS."CodigoPortafolio",PER."FechaNacimiento",CUS."IdCliente",
	   CASE WHEN CUS."IdMonedaReajuste" != 'CLP' THEN 'DO'
	   		ELSE CUS."IdMonedaReajuste" END AS "IdMoneda",
	   	to_bigint(CMA."IdPlanTipoInversion") AS "IdPlanTipoInversion",CUS."IdPortafolio",
	   	CASE WHEN CUS."IdClasificacionTipoInstrumento" = 'RV' THEN CUS."PrecioMercadoCustodia"
	   		ELSE COALESCE(VC."ValorCuota",CUS."PrecioMercadoCustodia") END AS "PrecioMercadoCustodia",
	   		:SecuenciaSolicitud,PER."Sexo",CUS."Nemotecnico"
FROM "BSCL.Custodia.BD.Vistas::ConsultarCustodiaPortafolioAnaliticaHis"(PLACEHOLDER."$$p_idempresa$$"=> :IdEmpresa,
																		PLACEHOLDER."$$p_fechaconsulta$$"=> :Fecha,
																		PLACEHOLDER."$$p_tipofechaconsulta$$"=> :TipoFechaConsulta) CUS
INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarMatrizAgrupador" 	CMA ON (CMA."IdMatrizAgrupadorAPV" = CUS."IdMatrizAgrupador")
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI   ON ( CUS."IdCliente" = CLI."IdCliente" )
INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON ( PER."IdPersona" = CLI."IdPersona" AND PER."IdPersona" NOT IN (:IdPersonaInstitucional) )
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" SERIE ON (SERIE."IdInstrumento" = CUS."IdInstrumento")
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.ValorCuotaHistorico" VC ON (VC."FechaCierre"= :FechaFin AND SERIE."IdSerieFondo" = VC."IdSerieFondo" AND VC."EsUltimaCargaDiaria" = 'S')
WHERE 
	CUS."CodigoPortafolio" IN ('APV-A','APV-AN','APV-B','BF','DC-R','DC-DL','DC-AL') AND 
	CUS."IdClasificacionTipoInstrumento" IN ('FM','RV') AND
	CUS."TotalPosicion" > 0;