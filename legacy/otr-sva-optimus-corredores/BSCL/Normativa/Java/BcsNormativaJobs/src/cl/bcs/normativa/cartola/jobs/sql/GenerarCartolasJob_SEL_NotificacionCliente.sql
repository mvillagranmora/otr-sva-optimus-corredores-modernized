/********************************************************** 
Proposito	: 
autor		: NRM
fecha		: 00/03/2018
***********************************************************/

WITH 
InformacionCliente AS (
	SELECT 	 CCL."IdCliente"	
			,CCL."NombreCompleto"
			,CCL."IdTipoCliente"
			,DI."DocumentoIdentidad" AS "DI" 
			,DI."IdTipoDocumentoIdentidad" AS "TipoDoc"
			,CPT."Numero" AS "TelCliente"
			,CCL."IdPersona"
			,CCL."IdEmpresa"
			,CPDE."IdDireccionElectronica"
			,CPDE."Direccion"
	FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CCL
	INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DI ON DI."IdDocumentoIdentidad" = CCL."IdDocumentoIdentidad"
	INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefono" CPT ON CPT."IdTelefono"= CCL."IdTelefonoPrincipal"
	INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronica" CPDE ON CPDE."IdDireccionElectronica" = CCL."IdDireccionElectronicaPrincipal"  
	WHERE CCL."IdCliente" IN (:IdCliente)
),
PreferenciasNotificacionCliente AS (
	SELECT	"IdPortafolio",
			COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'CAR_ENVCEM' THEN "ValorIndicador" END),'N') AS "EnviaNotificacionCartola",
			COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'CAR_DIRELE' THEN "ValorIndicador" END),'') AS "IdDireccionElectronica"
	FROM (
	
		SELECT "IdPortafolio","CodigoTipoIndicador","IdIndicador","ValorIndicador"
		FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio"
	) 
	WHERE "IdPortafolio" IN (:IdPortafolio)	
	GROUP BY "IdPortafolio"	
),
InformacionCorredora AS (
	SELECT
		 "IdEmpresa"
        ,"RazonSocial" 	AS "RazonCorredora"
        ,"Direccion" 	AS "DireccionCorredora"
        ,"RUT" 			AS "RutCorredora"
        ,"Telefono" 	AS "TelefonoCorredora"
	FROM "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales"
	WHERE "IdEmpresa" = :IdEmpresa
)
SELECT	 (IC."IdCliente" + PNC."IdPortafolio") AS "IdSecuencia"
		,IC."IdCliente"
		,IC."IdPersona"
		,PNC."IdPortafolio"
		,IC."NombreCompleto"
		,IC."IdTipoCliente"
		,IC."DI" 
		,IC."TipoDoc"
		,IC."TelCliente"
		,IC."Direccion" AS "Correo"
		,IC."IdDireccionElectronica"
		,ICOR."RazonCorredora"
        ,ICOR."DireccionCorredora"
        ,ICOR."RutCorredora"
        ,ICOR."TelefonoCorredora"
        ,AGE."NombreCompleto" AS "Agente"
FROM "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS CP
INNER JOIN InformacionCliente AS IC ON IC."IdCliente" = CP."IdCliente"
INNER JOIN PreferenciasNotificacionCliente AS PNC ON PNC."IdPortafolio" = CP."IdPortafolio" AND PNC."EnviaNotificacionCartola" = 'S'
INNER JOIN InformacionCorredora AS ICOR ON ICOR."IdEmpresa" = IC."IdEmpresa"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" AGE ON CP."IdEmpleadoEjecutivo" = AGE."IdEmpleado";