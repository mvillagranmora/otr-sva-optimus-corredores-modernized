SET SCHEMA "BSCL";
GO

DROP VIEW "BSCL.APV.BD.Vistas::ConsultarDatosGeneralesSAAPV";
GO

CREATE VIEW "BSCL.APV.BD.Vistas::ConsultarDatosGeneralesSAAPV" AS 
SELECT
	"IdDatosGenerales"
   ,"Descripcion"
   ,"Tipo"
   ,"Estado"
FROM
(
	SELECT
		TO_NVARCHAR("IdPlanTipoInversion")	AS "IdDatosGenerales"
	   ,"NombrePlanTipoInversion"			AS "Descripcion"
	   ,'PlanAhorro'						AS "Tipo"
	   ,"IdEstado"							AS "Estado"
	FROM "BSCL.APV.BD::SCS.PlanTipoInversion"
	UNION
	SELECT
		TO_NVARCHAR("IdProductoAgrupador")	AS "IdDatosGenerales"
	   ,"NombreProductoAgrupador"			AS "Descripcion"
	   ,'Producto'							AS "Tipo"
	   ,"IdEstado"							AS "Estado"
	FROM "BSCL.APV.BD::APV.ProductoAgrupador"
	UNION
	SELECT
		'1'									AS "IdDatosGenerales"
	   ,'IPS-EX INP'						AS "Descripcion"
	   ,'MedioRecaudacion'					AS "Tipo"
	   ,'VIG'								AS "Estado"
	FROM DUMMY
	UNION
	SELECT
		'2'									AS "IdDatosGenerales"
	   ,'AFP'								AS "Descripcion"
	   ,'MedioRecaudacion'					AS "Tipo"
	   ,'VIG'								AS "Estado"
	FROM DUMMY
	UNION
	SELECT
		TO_NVARCHAR("IdEntidadRecaudadora")	AS "IdDatosGenerales"
	   ,"Nombre"							AS "Descripcion"
	   ,'EntidadRecaudadora'				AS "Tipo"
	   ,"Estado"							AS "Estado"
	FROM "BSCL.APV.BD.Vistas::SCS.ConsultarEntidadRecaudadora"
	UNION
	SELECT
		'SI'								AS "IdDatosGenerales"
	   ,'SI'								AS "Descripcion"
	   ,'EsPensionado'						AS "Tipo"
	   ,'VIG'								AS "Estado"
	FROM DUMMY
	UNION
	SELECT
		'NO'								AS "IdDatosGenerales"
	   ,'NO'								AS "Descripcion"
	   ,'EsPensionado'						AS "Tipo"
	   ,'VIG'								AS "Estado"
	FROM DUMMY
	UNION
	SELECT
		'DEP'								AS "IdDatosGenerales"
	   ,'DEPENDIENTE'						AS "Descripcion"
	   ,'TipoTrabajador'					AS "Tipo"
	   ,'VIG'								AS "Estado"
	FROM DUMMY
	UNION
	SELECT
		'IND'								AS "IdDatosGenerales"
	   ,'INDEPENDIENTE'						AS "Descripcion"
	   ,'TipoTrabajador'					AS "Tipo"
	   ,'VIG'								AS "Estado"
	FROM DUMMY
	UNION
	SELECT
		'DIRECTA'							AS "IdDatosGenerales"
	   ,'DIRECTA'							AS "Descripcion"
	   ,'FormaPagoAPV'						AS "Tipo"
	   ,'VIG'								AS "Estado"
	FROM DUMMY
	UNION
	SELECT
		'INDIRECTA'							AS "IdDatosGenerales"
	   ,'INDIRECTA'							AS "Descripcion"
	   ,'FormaPagoAPV'						AS "Tipo"
	   ,'VIG'								AS "Estado"
	FROM DUMMY
	UNION
	SELECT
		"DiaDescuento"						AS "IdDatosGenerales"
	   ,"DiaDescuento"						AS "Descripcion"
	   ,'DiasDescuento'						AS "Tipo"
	   ,'VIG'								AS "Estado"
	FROM
	(
		SELECT
			TO_NVARCHAR("DiaDescuento") AS "DiaDescuento"
		FROM "BSCL.APV.BD::SCS.ContratoSAAPV"
		WHERE
			"DiaDescuento" IS NOT NULL
		GROUP BY
			"DiaDescuento"
	)
	UNION
	SELECT
		TO_NVARCHAR("IdRegimenTributario")	AS "IdDatosGenerales"
	   ,"Codigo"							AS "Descripcion"
	   ,'RegimenesTributarios'				AS "Tipo"
	   ,'VIG'								AS "Estado"
	FROM "BSCL.APV.BD::REC.RegimenTributario"
	UNION
	SELECT
		'IND'								AS "IdDatosGenerales"
	   ,'INDEFINIDO'						AS "Descripcion"
	   ,'FormaPagoAhorroAPV'				AS "Tipo"
	   ,'VIG'								AS "Estado"
	FROM DUMMY
	UNION
	SELECT
		'TEM'								AS "IdDatosGenerales"
	   ,'TEMPORAL'							AS "Descripcion"
	   ,'FormaPagoAhorroAPV'				AS "Tipo"
	   ,'VIG'								AS "Estado"
	FROM DUMMY
	UNION
	SELECT
		'UNI'								AS "IdDatosGenerales"
	   ,'UNICO'								AS "Descripcion"
	   ,'FormaPagoAhorroAPV'				AS "Tipo"
	   ,'VIG'								AS "Estado"
	FROM DUMMY
	UNION
	SELECT
		'1'									AS "IdDatosGenerales"
	   ,'DIRECTAMENTE AL TRABAJADOR'		AS "Descripcion"
	   ,'FormaPagoDirecta'					AS "Tipo"
	   ,'VIG'								AS "Estado"
	FROM DUMMY
	UNION
	SELECT
		'2'									AS "IdDatosGenerales"
	   ,'A TRAVES DEL EMPLEADOR'			AS "Descripcion"
	   ,'FormaPagoDirecta'					AS "Tipo"
	   ,'VIG'								AS "Estado"
	FROM DUMMY
	UNION
	SELECT
		'3'									AS "IdDatosGenerales"
	   ,'PAC'								AS "Descripcion"
	   ,'FormaPagoDirecta'					AS "Tipo"
	   ,'VIG'								AS "Estado"
	FROM DUMMY
	UNION
	SELECT
		TO_NVARCHAR("DocumentoIdentidad")      AS "IdDatosGenerales"
	   ,"Nombre"                			   AS "Descripcion"
	   ,'DocumentoIdentidadEntidadRecaudadora' AS "Tipo"
	   ,"Estado"							   AS "Estado"
	FROM "BSCL.APV.BD.Vistas::SCS.ConsultarEntidadRecaudadora"
) ORDER BY
	"Tipo"
   ,"IdDatosGenerales";
GO

GRANT SELECT ON "BSCL.APV.BD.Vistas::ConsultarDatosGeneralesSAAPV" TO BCS_DESARROLLADOR WITH GRANT OPTION;
GO