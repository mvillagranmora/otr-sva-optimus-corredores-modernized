SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.APV.BD.Vistas::AFI.ConsultarClientePortafolioSAAPV";
GO
CREATE VIEW "BSCL.APV.BD.Vistas::AFI.ConsultarClientePortafolioSAAPV"  AS 

SELECT
	 CLI."IdEmpresa"
	,CLI."IdPersona"
	,CLI."IdCliente"
	,DOC."DocumentoIdentidad" AS "RutCliente"
	,PER."Nombres"||' '||PER."ApellidoPaterno"||' '||PER."ApellidoMaterno" AS "NombrePersona"
	,EVT."IdEstadoAutorizacion"
	,EVT."EstadoAutorizacion"
FROM "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER
	ON (PER."IdPersona" = CLI."IdPersona")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOC
 	ON (PER."IdDocumentoIdentidad" = DOC."IdDocumentoIdentidad")
LEFT JOIN "BSCL.APV.BD.Vistas::AFI.ConsultarEventoCambioPlanActivoSAAPV" AS EVT
	ON	EVT."IdCliente" = CLI."IdCliente"
WHERE
	DOC."IdDocumentoIdentidad" IS NOT NULL
AND EXISTS (
	SELECT 1 FROM "BSCL.Portafolio.BD::POR.Portafolio" AS POR
  		LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MAT
   			ON (MAT."IdMatrizAgrupadorAPV" = POR."IdMatrizAgrupadorAPV")
  		LEFT JOIN "BSCL.APV.BD::REC.RegimenTributario" AS REG
   			ON (REG."IdRegimenTributario" = MAT."IdRegimenTributario")
  		LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS PRD
   			ON (PRD."IdProductoAgrupador" = MAT."IdProductoAgrupador")
  		LEFT JOIN "BSCL.APV.BD::SCS.PlanTipoInversion" AS PTI
   			ON (PTI."IdPlanTipoInversion" = MAT."IdPlanTipoInversion")
  	WHERE 
    	CLI."IdCliente" = POR."IdCliente" 
    	AND POR."Proposito" = 'PROPOSITOAPV'
   	 	AND PTI."IdPlanTipoInversion" = 1
		AND POR."IdEstado" <> 'INA'
);
GO
GRANT SELECT ON "BSCL.APV.BD.Vistas::AFI.ConsultarClientePortafolioSAAPV" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
GO