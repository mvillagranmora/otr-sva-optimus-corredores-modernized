SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Normativa.BD.Servicios::CAR.ObtenerInformacionCliente";
GO
CREATE PROCEDURE
-- 
--  Proposito: Recupera la información de Cliente para la Cartola
--  Autor: <author> - BCS
--  Fecha Creacion: 17/08/2016
--  Fecha Modificacion: 17/08/2016
"BSCL.Normativa.BD.Servicios::CAR.ObtenerInformacionCliente" (
	 IN p_IdModulo					NVARCHAR(40) , -- Obligatorio
	 IN p_IdUsuario					BIGINT       , -- Obligatorio
	 IN p_IdEmpresa					BIGINT       , -- Obligatorio
	 IN p_FechaDesde				DATE,
	 IN p_FechaHasta				DATE,	 
	 IN p_Datos						TABLE ("IdCliente"	BIGINT,"IdPortafolio" BIGINT)	
 ) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL" READS SQL DATA 
AS
BEGIN 
	
	SELECT
	DATOS."IdCliente"															AS "IdCliente",
	DATOS."IdPortafolio" 														AS "IdPortafolio",
	--Informacionn Cliente
	COALESCE(CLI."NombreCompleto",'')											AS "NombreCompletoCliente",
	COALESCE(CLI."DocumentoIdentidad",'')										AS "DocumentoIdentidadCliente",
	--Portafolio
	COALESCE(POR."CodigoPortafolio",0)											AS "CodigoPortafolio",
	COALESCE(POR."CodigoPortafolio",0)											AS "Cuenta",
	COALESCE(PROPPOR."Descripcion",'')											AS "PropositoPortafolio",
	COALESCE(EJE."NombreCompleto",'')											AS "EjecutivoPortafolio",
	CAST(COALESCE(POR."NumeroFicha",'0') AS INT)									AS "NumeroFicha", --PREGUNTAR
	--Uso Direccion Portafolio
	COALESCE(DIRFPOR."Calle", DIRF."Calle",'')									AS "CalleDireccion",
	COALESCE(DIRFPOR."Numero", DIRF."Numero",0)									AS "NumeroDireccion",
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
	CASE WHEN COALESCE(INDC."ValorIndicador",'') = 'N' THEN 'No' WHEN COALESCE(INDC."ValorIndicador",'') = 'S' THEN 'Si' ELSE '' END AS "ContratoCustodia"
	
	FROM		:p_Datos DATOS
	LEFT JOIN 	"BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" 				CLI		ON CLI."IdCliente" = DATOS."IdCliente"
	LEFT JOIN 	"BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" 					POR 	ON DATOS."IdPortafolio" = POR."IdPortafolio" AND DATOS."IdCliente" = POR."IdCliente" 
	LEFT JOIN	"BSCL.Portafolio.BD.Vistas::ConsultarPropositoPortafolio" 			PROPPOR ON PROPPOR."IdPropositoPortafolio" = POR."Proposito"
	LEFT JOIN 	"BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" 				EJE 	ON EJE."IdEmpleado" = POR."IdEmpleadoEjecutivo"
	LEFT JOIN 	"BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" 	DIRF 	ON DIRF."IdDireccionFisica" = POR."IdDireccionEnvioCorrespondencia"
	
	LEFT JOIN 	"BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio"		INDPOR	ON INDPOR."IdPortafolio" = POR."IdPortafolio" AND INDPOR."CodigoTipoIndicador" = 'CAR_DIRFIS'
	LEFT JOIN 	"BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" 	DIRFPOR ON DIRFPOR."IdDireccionFisica" = CASE WHEN INDPOR."ValorIndicador" = '' THEN DIRFPOR."IdDireccionFisica" ELSE INDPOR."ValorIndicador" END
	
	LEFT JOIN 	"BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefono" 		TEL		ON TEL."IdTelefono" = POR."IdTelefonoContacto"
	LEFT JOIN 	"BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" 	INDP	ON INDP."IdCliente" = CLI."IdCliente" AND INDP."IdPortafolio" = POR."IdPortafolio" and INDP."CodigoTipoIndicador" = 'ADMCART'
	LEFT JOIN 	"BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" 		INDC	ON INDC."IdCliente" = CLI."IdCliente" AND INDC."CodigoTipoIndicador" = 'CC';
	
END;
GO