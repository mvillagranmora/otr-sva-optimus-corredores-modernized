WITH RelacionadosCuentaPropia ("IdPersonaDestino") AS (
	SELECT 
		PER."IdPersonaDestino"
		FROM "BSCL.CuentaInversion.BD.Vistas::ConsultarRelacionCorredora" AS PER
),
ClientesConsultar ("IdPersona", "IdCliente", "Secuencia") AS (
	SELECT SOC."IdPersona" , CI."IdCliente",
	 ROW_NUMBER() OVER (
        PARTITION BY SOC."IdPersona", CI."IdCliente" 
        ORDER BY CI."IdCuentaInversion"
    ) AS "Secuencia"
	FROM "BSCL.CuentaInversion.BD.Vistas::ConsultarCuentaInversion" 				     AS CI
	INNER JOIN "BSCL.CuentaInversion.BD::GBL.GenerarArchivoCuentasPorCobrarAnexo16_Paso" AS GCP
		ON GCP."IdCuentaInversion" = CI."IdCuentaInversion" AND GCP."IdTrabajo" = (:idTrabajo)
	INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio"				 AS SOC
		ON CI."IdCliente" = SOC."IdCliente" 
),
Diccionario ("Traduccion", "Tipo") AS (
	SELECT
		"Descripcion" AS "Traduccion"
		,CASE 
			WHEN "CodDiccionario" = 'UI_CLAS_CONT_SI'
			THEN 'SI'
			ELSE 'NO'
		END AS "Tipo"
		
	FROM "BSCL.Plataforma.BD::PLA.Diccionario"
	WHERE "CodDiccionario" IN ('UI_CLAS_CONT_SI', 'UI_CLAS_CONT_NO')
)
SELECT 
	 CLI."IdCliente" 
	,CASE 
	  WHEN REL."IdPersonaDestino" IS NULL
	  THEN 'NO' 
	  ELSE 'SI' 
	END AS "Relacionado"
	, DICC."Traduccion"
	FROM  "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente"  AS CLI
	INNER JOIN ClientesConsultar 							   AS CCON 
		ON CLI."IdCliente" = CCON."IdCliente" AND CCON."Secuencia" = 1
	LEFT JOIN RelacionadosCuentaPropia						   AS REL
		ON CLI."IdPersona"  = REL."IdPersonaDestino"
	LEFT JOIN Diccionario 									   AS DICC
		ON  DICC."Tipo" = CASE  WHEN REL."IdPersonaDestino" IS NULL THEN 'NO' ELSE 'SI' END; 