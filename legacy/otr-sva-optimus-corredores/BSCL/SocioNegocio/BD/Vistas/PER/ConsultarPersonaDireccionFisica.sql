SET SCHEMA "BSCL";

--  Proposito: Se incluye campo CodigoCompuesto, requerido proy Perú.
--	Perú: UBIGEO / Chile: Codigo Unico Territorial.
--  Autor: JGarces - BCS
--  Fecha Creacion: 14/03/2017
-- 
DROP VIEW "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica";

CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica"
AS SELECT
	 "DIR"."IdDireccionFisica" AS "IdDireccionFisica" ,
	 "DIR"."Calle" AS "Calle" ,
	 "DIR"."IdPersona" ,
	 "DIR"."Numero" AS "Numero" ,
	 "DIR"."Departamento" AS "Departamento" ,
	 "DIR"."Sector" AS "Sector" ,
	 "DIR"."CodigoPostal" AS "CodigoPostal" ,
	 "DIR"."IdCiudad" AS "IdCiudad" ,
	 "CIU"."NombreCiudad",
	 "DIR"."CodigoProposito",
	 COALESCE("PROPMC"."Descripcion",'') AS  "DescripcionProposito" , 
	 "DIR"."Comentario",
	 "PAI"."IdPais",
	 "PAI"."NombrePais",
	 "PAI"."UsaDivisionPais",
	 CASE "PAI"."UsaDivisionPais" WHEN 'S' THEN COALESCE("PAI"."NombreDivisionPais",'') ELSE '-' END as "PaisNombreDivisionPais",
	 CASE "PAI"."UsaDivisionPais" WHEN 'S' THEN COALESCE("DIVPAI"."IdDivisionPais",0) ELSE 0 END AS "IdDivisionPais",
	 CASE "PAI"."UsaDivisionPais" WHEN 'S' THEN COALESCE("DIVPAI"."NombreDivisionPais",'-') ELSE '-' END AS "NombreDivisionPais",
	 COALESCE("DIVPAI"."Codigo",'') as "CodigoDivisionPais",
	 --"DIR"."IdEstado" AS "IdEstado" ,
	 "BSCL.SocioNegocio.BD.Utilitarios::SOC.ObtenerEstadoAutorizacion"('DIRECCIONFISICA',"DIR"."IdEstado","DIR"."IdEstadoAutorizacion") AS "IdEstado",
	 "EST"."Nombre" AS "NombreEstado",
	 "DIR"."IdEstadoAutorizacion",
	 "DIR"."FechaIngreso" AS "FechaIngreso" ,
	 "DIR"."FechaCreacion" AS "FechaCreacion" ,
	 "DIR"."FechaModificacion" AS "FechaModificacion",
	  CONCAT(CONCAT(CONCAT(CONCAT(COALESCE("DIR"."Calle",''),' '),COALESCE(CAST("DIR"."Numero" AS VARCHAR),'')),' - '),COALESCE("CIU"."NombreCiudad",'DEFINIR CIUDAD'))  AS "NombreLargo",
	  CASE WHEN "DIR"."Predeterminado" = 'S' THEN 'UI_SI' ELSE 'UI_NO' END AS "Predeterminado",
	 "DIR"."CodigoCompuesto" AS "CodigoCompuesto"	
	FROM "BSCL.SocioNegocio.BD::PER.DireccionFisica" AS "DIR"
		LEFT JOIN "BSCL.SocioNegocio.BD::PER.PropositoMedioContacto" as "PROPMC" on "PROPMC"."IdPropositoMedioContacto" = "DIR"."CodigoProposito"
		LEFT JOIN "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio" as "ESTSOC" 
			on "BSCL.SocioNegocio.BD.Utilitarios::SOC.ObtenerEstadoAutorizacion"('DIRECCIONFISICA',"DIR"."IdEstado","DIR"."IdEstadoAutorizacion") = "ESTSOC"."IdEstado" AND "NombreEntidad" = 'DIRECCIONFISICA'
		LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" as "EST" 
			on "ESTSOC"."IdEstado" = "EST"."IdEstado"
		LEFT JOIN "BSCL.Plataforma.BD::REG.Ciudad" as "CIU" 
			on "CIU"."IdCiudad" = "DIR"."IdCiudad"
		LEFT JOIN "BSCL.Plataforma.BD::REG.Pais" as "PAI" 
			on "CIU"."IdPais" = "PAI"."IdPais"
		LEFT JOIN "BSCL.Plataforma.BD::REG.DivisionPais" as "DIVPAI" 
			on ("DIR"."IdDivisionPais" = "DIVPAI"."IdDivisionPais")
		WHERE "DIR"."Borrado"='N'
;

GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 

 