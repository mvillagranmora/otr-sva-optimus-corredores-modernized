SET SCHEMA "BSCL";

GO

DROP VIEW "BSCL.Plataforma.BD.Vistas::BAN.ConsultarBanco";

GO

CREATE VIEW "BSCL.Plataforma.BD.Vistas::BAN.ConsultarBanco" AS

	SELECT 
		 BAN."IdBanco"
		,BAN."NombreBanco"
		,BAN."CodigoBanco"
		,BAN."IdTipoBanco"                 -- NAC / EXT
        ,TB."Nombre" AS "NombreTipoCampo"  -- NACIONAL / EXTRANJERO
		,BAN."IdEstado"
		,BAN."IdEmpresa"
		,BAN."IdPaisOperacion"    -- Ej.: Santander(Chile)  Santander(Brasil)  > Pais operación Chile y Brazil (1 registro por cada uno)
		,PAI."NombrePais" AS "NombrePais"		
	    ,(( BAN."NombreBanco" || ' (' )  || UPPER (PAI."NombrePais" ) || ')')  AS "NombreBancoPais"
		,BAN."IdPersona"
		,(CASE
			WHEN (PER."Tipo" = 'N') THEN PER."Nombres" || ' ' || PER."ApellidoPaterno" || ' ' || PER."ApellidoMaterno"
			WHEN (PER."Tipo" = 'J') THEN PER."RazonSocial"
		  END) AS "NombrePersona"
		,COALESCE(SOC."IdSocioDeNegocio",0) AS "IdSocioDeNegocio"
		,COALESCE(CON."IdContraparte",0) AS "IdContraparte"
		,BAN."CodigoIso"
		,BAN."BancoReferenciaForward"
	FROM  "BSCL.Plataforma.BD::BAN.Banco" AS BAN	
	LEFT OUTER JOIN "BSCL.Plataforma.BD::REG.Pais" AS PAI ON  (PAI."IdPais" = BAN."IdPaisOperacion")
	LEFT OUTER JOIN "BSCL.SocioNegocio.BD::PER.Persona"  AS PER ON  (PER."IdPersona" = BAN."IdPersona")
	LEFT OUTER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS SOC ON (SOC."IdPersona" = BAN."IdPersona" AND SOC."IdEmpresa" = BAN."IdEmpresa")	
	LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::CNT.ConsultarContraparte" AS CON ON  (CON."IdPersona" = BAN."IdPersona" AND CON."IdEmpresa" = BAN."IdEmpresa")	
	LEFT OUTER JOIN "BSCL.Plataforma.BD::BAN.TipoBanco" AS TB ON (TB."IdTipoBanco" = BAN."IdTipoBanco");
GO

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::BAN.ConsultarBanco" TO  BCS_DESARROLLADOR WITH GRANT OPTION;

GO
