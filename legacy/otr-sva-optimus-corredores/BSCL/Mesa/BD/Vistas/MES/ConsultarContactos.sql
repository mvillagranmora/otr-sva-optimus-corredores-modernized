SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Mesa.BD.Vistas::MES.ConsultarContactos";

CREATE VIEW "BSCL.Mesa.BD.Vistas::MES.ConsultarContactos"
AS 
SELECT
	CLI."IdCliente" AS "IdCliente"
	,RP."IdTipoRelacion"
	,(CASE WHEN PER."Tipo" ='N' THEN CONCAT(PER."Nombres",CONCAT(' ',CONCAT(PER."ApellidoPaterno",CONCAT(' ',PER."ApellidoMaterno"))))
			WHEN PER."Tipo" ='J' THEN PER."RazonSocial" END) AS "Nombre"
	,TEL."Numero"
	,COR."Direccion" AS "CorreoElectronico"
	,DIR."Calle"
	,DIR."Numero" AS "NumeroCalle"
	,DIR."NombreDivisionPais" AS "Comuna"
	,DIR."NombreCiudad"
	,REG."NombreClasificacionDivisionPais" AS "Region"
	,PS."NombrePais"
	,PER."IdPersona"
FROM 		"BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI
	INNER JOIN  "BSCL.SocioNegocio.BD::REL.RelacionPersona" AS RP ON RP."IdPersonaOrigen" = CLI."IdPersona"
	INNER JOIN 	"BSCL.SocioNegocio.BD::PER.Persona" AS PER ON PER."IdPersona" = RP."IdPersonaDestino"
	INNER JOIN  "BSCL.Plataforma.BD::REG.Pais" AS PS ON PER."IdPaisResidencia" = PS."IdPais"
	INNER JOIN 	"BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" AS DIR ON PER."IdDireccionFisicaPrincipal" = DIR."IdDireccionFisica"
	INNER JOIN 	"BSCL.SocioNegocio.BD::PER.Telefono" AS TEL ON PER."IdTelefonoPrincipal" = TEL."IdTelefono"
	INNER JOIN  "BSCL.SocioNegocio.BD::PER.DireccionElectronica" AS COR ON PER."IdDireccionElectronicaPrincipal" = COR."IdDireccionElectronica"
LEFT JOIN 	"BSCL.Plataforma.BD::REG.DivisionPais" AS COM ON COM."IdDivisionPais" = DIR."IdDivisionPais"
LEFT JOIN 	"BSCL.Plataforma.BD::REG.ClasificacionDivisionPais" AS REG ON REG."IdClasificacionDivisionPais" = COM."IdClasificacion1"
WHERE  RP."IdEstado" = 'ACT' AND  RP."IdTipoRelacion" = 'CONTAC';

GRANT SELECT ON "BSCL.Mesa.BD.Vistas::MES.ConsultarContactos" TO BCS_DESARROLLADOR WITH GRANT OPTION;