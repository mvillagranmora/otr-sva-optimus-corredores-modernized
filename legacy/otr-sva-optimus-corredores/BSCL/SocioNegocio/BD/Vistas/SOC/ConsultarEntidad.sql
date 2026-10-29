SET SCHEMA "BSCL";
GO

DROP VIEW "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarEntidad";
GO

CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarEntidad" 
AS 
SELECT --+ Persona 
	'PER'										AS "TipoEntidad"
	,"SocioDeNegocio"."IdPersona"				AS "IdEntidad"
	,"Persona"."IdEstado"						AS "Estado"
	,"DocumentoIdentidad"."DocumentoIdentidad" 	AS "DocumentoIdentidad"
FROM 
	"BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS "SocioDeNegocio"
		INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS "Persona"
		ON "SocioDeNegocio"."IdPersona" = "Persona"."IdPersona"
			LEFT OUTER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS "DocumentoIdentidad"
			ON "Persona"."IdDocumentoIdentidad" = "DocumentoIdentidad"."IdDocumentoIdentidad"
WHERE
	"SocioDeNegocio"."Borrado" = 'N'
UNION ALL --+ Cliente
SELECT 
	'CLI'										AS "TipoEntidad"
	,"Cliente"."IdCliente"						AS "IdEntidad"
	,"Cliente"."IdEstado"						AS "Estado"
	,"DocumentoIdentidad"."DocumentoIdentidad" 	AS "DocumentoIdentidad"
FROM 
	"BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS "SocioDeNegocio"
		INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS "Cliente"
		ON "Cliente"."IdCliente" = "SocioDeNegocio"."IdCliente"
		INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS "Persona"
		ON "SocioDeNegocio"."IdPersona" = "Persona"."IdPersona"
			LEFT OUTER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS "DocumentoIdentidad"
			ON "Persona"."IdDocumentoIdentidad" = "DocumentoIdentidad"."IdDocumentoIdentidad"
WHERE
	"SocioDeNegocio"."Borrado" = 'N'
UNION ALL --+ Proveedor
SELECT 
	'PRO'										AS "TipoEntidad"
	,"Proveedor"."IdProveedor"					AS "IdEntidad"
	,"Proveedor"."IdEstado"						AS "Estado"
	,"DocumentoIdentidad"."DocumentoIdentidad" 	AS "DocumentoIdentidad"
FROM 
	"BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS "SocioDeNegocio"
		INNER JOIN "BSCL.SocioNegocio.BD::PRO.Proveedor" AS "Proveedor"
		ON "Proveedor"."IdProveedor" = "SocioDeNegocio"."IdProveedor"
		INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS "Persona"
		ON "SocioDeNegocio"."IdPersona" = "Persona"."IdPersona"
			LEFT OUTER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS "DocumentoIdentidad"
			ON "Persona"."IdDocumentoIdentidad" = "DocumentoIdentidad"."IdDocumentoIdentidad"
WHERE
	"SocioDeNegocio"."Borrado" = 'N'
UNION ALL --+ Empleado
SELECT 
	'EMP'										AS "TipoEntidad"
	,"Empleado"."IdEmpleado"					AS "IdEntidad"
	,"Empleado"."IdEstado"						AS "Estado"
	,"DocumentoIdentidad"."DocumentoIdentidad" 	AS "DocumentoIdentidad"
FROM 
	"BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS "SocioDeNegocio"
		INNER JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" "Empleado"
		ON "Empleado"."IdEmpleado" = "SocioDeNegocio"."IdEmpleado"
		INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS "Persona"
		ON "SocioDeNegocio"."IdPersona" = "Persona"."IdPersona"
			LEFT OUTER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS "DocumentoIdentidad"
			ON "Persona"."IdDocumentoIdentidad" = "DocumentoIdentidad"."IdDocumentoIdentidad"
WHERE
	"SocioDeNegocio"."Borrado" = 'N'
UNION ALL --+ Contraparte
SELECT 
	'CNT'										AS "TipoEntidad"
	,"Contraparte"."IdContraparte"				AS "IdEntidad"
	,"Contraparte"."IdEstado"					AS "Estado"
	,"DocumentoIdentidad"."DocumentoIdentidad" 	AS "DocumentoIdentidad"
FROM 
	"BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS "SocioDeNegocio"
		INNER JOIN "BSCL.SocioNegocio.BD::CNT.Contraparte" AS "Contraparte"
		ON "Contraparte"."IdContraparte" = "SocioDeNegocio"."IdContraparte"
		INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS "Persona"
		ON "SocioDeNegocio"."IdPersona" = "Persona"."IdPersona"
			LEFT OUTER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS "DocumentoIdentidad"
			ON "Persona"."IdDocumentoIdentidad" = "DocumentoIdentidad"."IdDocumentoIdentidad"
WHERE
	"SocioDeNegocio"."Borrado" = 'N';
GO

GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarEntidad" TO BCS_DESARROLLADOR WITH GRANT OPTION;
GO	