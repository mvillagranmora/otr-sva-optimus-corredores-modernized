SELECT 
"IdCliente"
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
,CAST(to_varchar(current_date,'YYYY-MM-DD') as TIMESTAMP) AS "fecha_emision"    		
,"NumeroTelefono" AS "tel"	
,"AdministraCartera" AS "tieneAdministracionCartera"
,"NumeroFicha" AS "numeroFicha"
,"PaisNombreDivisionPaisPrincipal" AS "PaisNombreDivisionPaisPrincipal"
FROM (
SELECT
	CLI."IdCliente"																AS "IdCliente",
	POR."IdPortafolio" 															AS "IdPortafolio",
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
	COALESCE(DIRC."PaisNombreDivisionPais", '')									AS "PaisNombreDivisionPaisPrincipal",
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
	
	FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" 							CLI		
	LEFT JOIN 	"BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" 						POR 	ON CLI."IdCliente" = POR."IdCliente" 
	INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" P ON POR."IdPortafolio" = P."IdPortafolio"
	LEFT JOIN	"BSCL.Portafolio.BD.Vistas::ConsultarPropositoPortafolio" 				PROPPOR ON PROPPOR."IdPropositoPortafolio" = POR."Proposito"
	LEFT JOIN 	"BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" 					EJE 	ON EJE."IdEmpleado" = POR."IdEmpleadoEjecutivo"
	LEFT JOIN 	"BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" 		DIRC 	ON DIRC."IdDireccionFisica" = CLI."IdDireccionFisicaPrincipal"
	LEFT JOIN 	"BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" 		DIRF 	ON DIRF."IdDireccionFisica" = POR."IdDireccionEnvioCorrespondencia"
	
	LEFT JOIN 	"BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio"			INDPOR	ON INDPOR."IdPortafolio" = POR."IdPortafolio" AND INDPOR."CodigoTipoIndicador" = 'CAR_DIRFIS'
	LEFT JOIN 	"BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" 		DIRFPOR ON DIRFPOR."IdDireccionFisica" = CASE WHEN INDPOR."ValorIndicador" != '' AND LENGTH(LTRIM(INDPOR."ValorIndicador",'+-.0123456789')) = 0 THEN INDPOR."ValorIndicador" ELSE DIRC."IdDireccionFisica" END
	
	LEFT JOIN	"BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDocumentoIdentidad" 	PDI		ON PDI."IdPersona" = CLI."IdPersona" AND PDI."Predeterminado" = 'UI_SI'
	LEFT JOIN 	"BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefono" 			TEL		ON TEL."IdTelefono" = POR."IdTelefonoContacto"
	LEFT JOIN 	"BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" 		INDP	ON INDP."IdCliente" = CLI."IdCliente" AND INDP."IdPortafolio" = POR."IdPortafolio" and INDP."CodigoTipoIndicador" = 'ADMCART'
	LEFT JOIN 	"BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" 			INDC	ON INDC."IdCliente" = CLI."IdCliente" AND INDC."CodigoTipoIndicador" = 'CC'
	LEFT OUTER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" 						DocInd  ON DocInd."IdPersona" = cli."IdPersona" AND DocInd."IdTipoDocumentoIdentidad" = 'RUT'
	
)