SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Normativa.BD.Servicios::CAR.InfoCarteraMaster";
GO
CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::CAR.InfoCarteraMaster" (
    IN p_IdModulo NVARCHAR(40),
    IN p_IdUsuario BIGINT,
    IN p_IdEmpresa BIGINT,
    IN p_FechaDesde DATE,
    IN p_FechaHasta DATE,
    IN p_Datos TABLE ("IdCliente" BIGINT, "IdPortafolio" BIGINT)
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA "BSCL"
--READS SQL DATA
AS
BEGIN
	v_InformacionBovedaCavali = SELECT '' as "IdBoveda", '' as "DocumentoIdentidadCavali" FROM dummy WHERE 1 = 2;	

	v_ConfiguracionDelSistema =
	SELECT  "IdEmpresa",
			"PLA.LocalizacionSistema",
			"PLA.MonedaSistema",
			CASE WHEN "PLA.MonedaAdicional" IS NULL OR "PLA.MonedaAdicional" = '' THEN 'USD' ELSE "PLA.MonedaAdicional" END "PLA.MonedaAdicional" 
	  FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConfiguracionPivot"
	 WHERE "IdEmpresa" = :p_IdEmpresa;

	--Obtener informacion de los parámetros financieros
	INSERT INTO "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Entrada_Entrada"
	SELECT 0,1,:p_FechaHasta,'UF',"PLA.MonedaSistema",0,1,'CARTOLA' FROM :v_ConfiguracionDelSistema UNION ALL
	SELECT 0,2,:p_FechaHasta,'USD',"PLA.MonedaSistema",0,1,'CARTOLA' FROM :v_ConfiguracionDelSistema UNION ALL
	SELECT 0,3,:p_FechaHasta,'EUR',"PLA.MonedaSistema",0,1,'CARTOLA' FROM :v_ConfiguracionDelSistema UNION ALL
	SELECT 0,4,:p_FechaHasta,"PLA.MonedaAdicional","PLA.MonedaSistema",0,1,'CARTOLA' FROM :v_ConfiguracionDelSistema;
	
	CALL "BSCL.Plataforma.BD.Servicios::MER.CalcularParidadYTipoCambio"(
		p_IdEmpresa => :p_IdEmpresa,
		p_IdTrabajo => 0
	);
	

	v_InfoInformacionFinanciera = 
	SELECT
		COALESCE(MAX("ValorUF"),0) AS "Valor_UF",
		COALESCE(MAX("ValorUSD"),0) AS "Valor_USD",
		COALESCE(MAX("ValorEUR"),0) AS "Valor_EUR",
		COALESCE(MAX("ValorMonedaAdicional"),0) AS "ValorMonedaAdicional"
	FROM
	(
		SELECT 	CASE WHEN "IdMonedaOrigen"='UF' THEN "MontoDestino" ELSE null END AS "ValorUF",
				CASE WHEN "IdMonedaOrigen"='USD' THEN "MontoDestino" ELSE null END AS "ValorUSD",
				CASE WHEN "IdMonedaOrigen"='EUR' THEN "MontoDestino" ELSE null END AS "ValorEUR",
				CASE WHEN "IdMonedaOrigen"= "PLA.MonedaAdicional" THEN "MontoDestino" ELSE null END AS "ValorMonedaAdicional"
		  FROM (SELECT "IdMonedaOrigen", "MontoDestino" FROM "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Salida_Salida"),
		  		:v_ConfiguracionDelSistema
		  
	);
	
	
	v_InformacionCliente = 
	SELECT
	DATOS."IdCliente"															AS "IdCliente",
	DATOS."IdPortafolio" 														AS "IdPortafolio",
	--Informacionn Cliente
	COALESCE(CLI."NombreCompleto",'')											AS "NombreCompletoCliente",
	COALESCE(CLI."DocumentoIdentidad",PDI."DocumentoIdentidad",'')				AS "DocumentoIdentidadCliente",
	CASE WHEN CLI."Tipo" = 'M' THEN 'Mancomuna' ELSE 'Individual' END			AS "TipoCliente",
	--Portafolio
	COALESCE(POR."CodigoPortafolio",'')											AS "CodigoPortafolio",
	COALESCE(PROPPOR."Descripcion",'')											AS "PropositoPortafolio",
	COALESCE(EJE."NombreCompleto",'')											AS "EjecutivoPortafolio",
	CAST(CASE COALESCE(POR."NumeroFicha",'0') WHEN '' THEN '0' ELSE COALESCE(POR."NumeroFicha",'0') END  AS INTEGER) AS "NumeroFicha", --PREGUNTAR
	--Uso Direccion Principal por Cliente
	COALESCE(DIRC."Calle",'')													AS "CalleDireccionPrincipal",
	CAST(COALESCE(DIRC."Numero",'') as NVARCHAR(100))							AS "NumeroDireccionPrincipal",
	COALESCE(DIRC."Departamento", '')											AS "DepartamentoDireccionPrincipal",
	COALESCE(DIRC."NombreDivisionPais", '')										AS "NombreDivisionPaisPrincipal",	
	COALESCE(DIRC."DescripcionProposito", '')									AS "ClasificacionDireccionPrincipal", --PREGUNTAR
	COALESCE(DIRC."NombrePais", '')												AS "PaisDireccionPrincipal",
	COALESCE(DIRC."CodigoPostal", '')											AS "CodigoPostalPrincipal",
	
	--Uso Direccion Portafolio
	COALESCE(DIRFPOR."Calle", DIRF."Calle",'')									AS "CalleDireccion",
	CAST(COALESCE(DIRFPOR."Numero", DIRF."Numero",'') as NVARCHAR(100))			AS "NumeroDireccion",
	COALESCE(DIRFPOR."Departamento", DIRF."Departamento",'')					AS "DepartamentoDireccion",
	COALESCE(DIRFPOR."NombreDivisionPais", DIRF."NombreDivisionPais",'')		AS "NombreDivisionPais",	
	COALESCE(DIRFPOR."DescripcionProposito", DIRF."DescripcionProposito",'')	AS "ClasificacionDireccion", --PREGUNTAR
	COALESCE(DIRFPOR."NombrePais", DIRF."NombrePais",'')						AS "PaisDireccion",
	COALESCE(DIRFPOR."CodigoPostal", DIRF."CodigoPostal",'')					AS "CodigoPostal",
	--Uso Telefono  Portafolio
	COALESCE(TEL."Numero",'')													AS "NumeroTelefono",
	COALESCE(TEL."Anexo",'')													AS "AnexoTelefono",
	
	--Informacion Adicional		
	CASE WHEN COALESCE(INDP."ValorIndicador",'') = 'N' THEN 'No' WHEN COALESCE(INDP."ValorIndicador",'') = 'S' THEN 'Si' ELSE '' END AS "AdministraCartera",
	CASE WHEN COALESCE(INDC."ValorIndicador",'') = 'N' THEN 'No' WHEN COALESCE(INDC."ValorIndicador",'') = 'S' THEN 'Si' ELSE '' END AS "ContratoCustodia",
	COALESCE(DocInd."DocumentoIdentidad",'') 									AS "DocumentoIdentidadCavali"
	
	FROM		:p_Datos DATOS
	LEFT JOIN 	"BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" 					CLI		ON CLI."IdCliente" = DATOS."IdCliente"
	LEFT JOIN 	"BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" 						POR 	ON DATOS."IdPortafolio" = POR."IdPortafolio" AND DATOS."IdCliente" = POR."IdCliente" 
	LEFT JOIN	"BSCL.Portafolio.BD.Vistas::ConsultarPropositoPortafolio" 				PROPPOR ON PROPPOR."IdPropositoPortafolio" = POR."Proposito"
	LEFT JOIN 	"BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" 					EJE 	ON EJE."IdEmpleado" = POR."IdEmpleadoEjecutivo"
	LEFT JOIN 	"BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" 		DIRC 	ON DIRC."IdDireccionFisica" = CLI."IdDireccionFisicaPrincipal"
	LEFT JOIN 	"BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" 		DIRF 	ON DIRF."IdDireccionFisica" = POR."IdDireccionEnvioCorrespondencia"
	
	LEFT JOIN 	"BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio"			INDPOR	ON INDPOR."IdPortafolio" = POR."IdPortafolio" AND INDPOR."CodigoTipoIndicador" = 'CAR_DIRFIS'
	LEFT JOIN 	"BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" 		DIRFPOR ON DIRFPOR."IdDireccionFisica" = CASE WHEN INDPOR."ValorIndicador" = '' THEN DIRC."IdDireccionFisica" ELSE INDPOR."ValorIndicador" END
	
	LEFT JOIN	"BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDocumentoIdentidad" 	PDI		ON PDI."IdPersona" = CLI."IdPersona" AND PDI."Predeterminado" = 'UI_SI'
	LEFT JOIN 	"BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefono" 			TEL		ON TEL."IdTelefono" = POR."IdTelefonoContacto"
	LEFT JOIN 	"BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" 		INDP	ON INDP."IdCliente" = CLI."IdCliente" AND INDP."IdPortafolio" = POR."IdPortafolio" and INDP."CodigoTipoIndicador" = 'ADMCART'
	LEFT JOIN 	"BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" 			INDC	ON INDC."IdCliente" = CLI."IdCliente" AND INDC."CodigoTipoIndicador" = 'CC'
	LEFT OUTER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" 						DocInd  ON DocInd."IdPersona" = cli."IdPersona" AND DocInd."IdTipoDocumentoIdentidad" = 'RUT';
	
	
	--SELECT 'v_InformacionCliente',* FROM :v_InformacionCliente;   --Borrar
	
	
	--Obtener información del cliente
	SELECT   "IdCliente"
			,"IdPortafolio"			
			,"BSCL.Plataforma.BD.Utilitarios::CamelCase"("NombreCompletoCliente") AS "NombreCliente"
			,"BSCL.Plataforma.BD.Utilitarios::CamelCase"("CodigoPortafolio") AS "TipoPortafolio"
			,"BSCL.Plataforma.BD.Utilitarios::CamelCase"("CalleDireccionPrincipal" ||' '|| "NumeroDireccionPrincipal" ||' '|| "DepartamentoDireccionPrincipal") AS "DireccionPrincipal"
			,"BSCL.Plataforma.BD.Utilitarios::CamelCase"("CalleDireccion" ||' '|| "NumeroDireccion" ||' '|| "DepartamentoDireccion") AS "Direccion"
			,"BSCL.Plataforma.BD.Utilitarios::CamelCase"("NombreDivisionPaisPrincipal") AS "ComunaPrincipal"
			,"BSCL.Plataforma.BD.Utilitarios::CamelCase"("NombreDivisionPais") AS "Comuna"
			,"BSCL.Plataforma.BD.Utilitarios::CamelCase"("PaisDireccionPrincipal") AS "PaisPrincipal"
			,"BSCL.Plataforma.BD.Utilitarios::CamelCase"("PaisDireccion") AS "Pais"
			,"DocumentoIdentidadCliente" AS "DocumentoIdentidad"
			,"TipoCliente"			
			,CAST(:p_FechaDesde as NVARCHAR(10)) AS "FechaDesde"
			,CAST(:p_FechaHasta as NVARCHAR(10)) AS "FechaHasta"
			,CAST(to_varchar(current_date,'YYYY-MM-DD') as NVARCHAR(10)) AS "FechaEmision"
			,"DocumentoIdentidadCavali"
			
			------Proximanente eliminar
			,"NombreCompletoCliente" AS "nombre"						
			,"DocumentoIdentidadCliente" AS "rut"
			,"CodigoPortafolio" ||'_ ' AS "cuenta"
			,CAST("CalleDireccionPrincipal" ||' '|| "NumeroDireccionPrincipal" ||' '|| "DepartamentoDireccionPrincipal"  as NVARCHAR(1024)) AS "direccionPrincipal"
			,CAST("CalleDireccion" ||' '|| "NumeroDireccion" ||' '|| "DepartamentoDireccion"  as NVARCHAR(1024)) AS "direccion"
			,"NombreDivisionPaisPrincipal" AS "comunaPrincipal"
			,"NombreDivisionPais" AS "comuna"
			,"PaisDireccionPrincipal" AS "paisPrincipal"
			,"PaisDireccion" AS "pais"
			,"EjecutivoPortafolio" AS "agente"
			,"PropositoPortafolio" AS "Tipo_Cuenta"
			,"CodigoPostal" AS "codigoPostal"
			,CAST(:p_FechaDesde as TIMESTAMP) AS "desde"
    		,CAST(:p_FechaHasta as TIMESTAMP) AS "hasta"
    		,CAST(to_varchar(current_date,'YYYY-MM-DD') as TIMESTAMP) AS "fecha_emision"    		
    		,"NumeroTelefono" AS "tel"
    		,CAST(:p_FechaHasta as TIMESTAMP) AS "fecha"
    		,"Valor_UF"
    		,"Valor_USD"
    		,"Valor_EUR"
    		,"ValorMonedaAdicional"    		
    		,"AdministraCartera" AS "tieneAdministracionCartera"
    		,"NumeroFicha" AS "numeroFicha"    		
	  FROM :v_InformacionCliente
  	  FULL OUTER JOIN :v_InfoInformacionFinanciera ON 1=1;
	  
END;
GO