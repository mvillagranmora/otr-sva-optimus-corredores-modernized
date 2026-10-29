SELECT
	 CON."IdContratoSAAPV"
	,DETTRA."IdDetalleOrigenTraspaso"
	,DETTRA."IdTraspaso"
	,DETTRA."CodigoTipoAhorro"
	,DETTRA."IdEntidadRecaudadora"
	,DETTRA."NombreEntidad"
	,DETTRA."DocumentoEntidad"
	,DETTRA."Fondo"
	,DETTRA."MontoTraspaso"
	,DETTRA."UFTraspaso"
	,DETTRA."PorcentajeTraspaso"
	,DETTRA."IdEstado"
	,DETTRA."IdInstrumento"
	,DETTRA."CodigoRegimenTributario"
	,DETTRA."IdPortafolio"
FROM "BSCL.APV.BD::SCS.ContratoSAAPV" CON
INNER JOIN "BSCL.APV.BD::TRA.Traspaso" TRA
	ON (CON."IdContratoSAAPV" = TRA."IdContratoSAAPV" AND
		CON."IdEmpresa" = TRA."IdEmpresa"
		--AND TRA."IdEstado"
		)
INNER JOIN "BSCL.APV.BD::TRA.DetalleOrigenTraspaso" DETTRA
	ON (TRA."IdTraspaso" = DETTRA."IdTraspaso"
		--AND DETTRA."IdEstado"
		)
WHERE CON."IdEmpresa" = :IdEmpresa
  AND CON."IdPlan" = :IdPlan
  AND CON."IdContratoSAAPV" IN (:IdContratoSAAPV)
  AND CON."IdEstado" IN ('VIG')
;
