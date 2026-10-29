SET SCHEMA "BSCL"; 
GO 
DROP VIEW "BSCL.Portafolio.BD.Vistas::ConsultarPreferenciaPagoPortafolioCorredora"; 
GO

CREATE VIEW "BSCL.Portafolio.BD.Vistas::ConsultarPreferenciaPagoPortafolioCorredora" AS 

SELECT 
	PPP."IdPreferenciaPagoPortafolio",
	PPP."IdPortafolio", 
	PPP."IdPreferenciaPago",
	CUENTABANCO."IdCuentaBanco"AS "IdCuentaBancaria",
	CASE PPP."IdCuentaBancaria" WHEN 0 THEN 0  
		ELSE (
			CASE TIPOP."RecibePagaPortafolio" 
				WHEN 'R' THEN COALESCE(CBANC."IdBanco",0)
				WHEN 'P' THEN 
					CASE WHEN TIPOP."IdTipoProducto"  IN ('MEME','MERF','MEFW')
					THEN COALESCE(CBANC."IdBanco",0) ELSE COALESCE(CUENTABANCO."IdBanco",0) END
				ELSE 0 END) 
		END as "IdBancoCuentaBancaria",
	CASE PPP."IdCuentaBancaria" WHEN 0 THEN '' 
		ELSE (
			CASE TIPOP."RecibePagaPortafolio" 
				WHEN 'R' THEN COALESCE(BANCBANC."NombreBanco",'')
				WHEN 'P' THEN 
					CASE WHEN TIPOP."IdTipoProducto"   IN ('MEME','MERF','MEFW')
					THEN COALESCE(BANCBANC."NombreBanco",'') ELSE COALESCE(BANCO."NombreBanco",'') END 					
				ELSE '' END) 
		END as "NombreBancoCuentaBancaria",
	CASE PPP."IdCuentaBancaria" WHEN 0 THEN '' 
		ELSE (
			CASE TIPOP."RecibePagaPortafolio" 
				WHEN 'R' THEN COALESCE(CBANC."NumeroCuentaBancaria",'')  	
				WHEN 'P' THEN 
					CASE WHEN TIPOP."IdTipoProducto" IN ('MEME','MERF','MEFW')
					THEN COALESCE(CBANC."NumeroCuentaBancaria",'') ELSE COALESCE(CUENTABANCO."NumeroCuentaBancaria",'') END			
				ELSE '' END) 
		END as "NumeroCuentaBancaria",
	CASE PPP."IdCuentaBancaria" WHEN 0 THEN '' 
		ELSE (
			CASE TIPOP."RecibePagaPortafolio" 
				WHEN 'R' THEN COALESCE(TCBANC."IdTipoCuentaBancaria",'')
				WHEN 'P' THEN 
					CASE WHEN TIPOP."IdTipoProducto"  IN ('MEME','MERF','MEFW')
					THEN COALESCE(TCBANC."IdTipoCuentaBancaria",'') ELSE COALESCE(CUENTABANCO."IdTipoCuentaBancaria",'') END			
				ELSE '' END) 
		END as "IdTipoCuentaBancaria",
	CASE PPP."IdCuentaBancaria" WHEN 0 THEN '' 
		ELSE (
			CASE TIPOP."RecibePagaPortafolio" 
				WHEN 'R' THEN COALESCE(TCBANC."NombreTipoCuentaBancaria",'') 
				WHEN 'P' THEN 
					CASE WHEN TIPOP."IdTipoProducto" IN ('MEME','MERF','MEFW')
					THEN COALESCE(TCBANC."NombreTipoCuentaBancaria",'') ELSE COALESCE(TIPOCUENTABANCARIA."NombreTipoCuentaBancaria",'') END  	
				ELSE '' END) 
		END as "NombreTipoCuentaBancaria",
	PPP."IdTarjetaBancaria",
	CASE PPP."IdTarjetaBancaria" WHEN 0 THEN 0 ELSE COALESCE(TBANC."IdBanco",0) END as "IdBancoTarjetaBancaria",
	CASE PPP."IdTarjetaBancaria" WHEN 0 THEN '' ELSE COALESCE(BANTBANC."NombreBanco",'') END as "NombreBancoTarjetaBancaria",
	CASE PPP."IdTarjetaBancaria" WHEN 0 THEN '' ELSE COALESCE(TBANC."NumeroTarjetaBancaria",'') END as "NumeroTarjetaBancaria",
	CASE PPP."IdTarjetaBancaria" WHEN 0 THEN '' ELSE COALESCE(TTBANC."IdTipoTarjetaBancaria",'') END as "IdTipoTarjetaBancaria",
	CASE PPP."IdTarjetaBancaria" WHEN 0 THEN '' ELSE COALESCE(TTBANC."NombreTipoTarjetaBancaria",'') END as "NombreTipoTarjetaBancaria",
   	PPP."FechaCreacion",
	PPP."FechaModificacion",
	PPP."IdUsuarioCreador",
	PPP."IdEstado",
	ESTPPP."Nombre" as "Estado",
	PPP."IdEmpresa",
	PP."IdTipoOperacion", 	-- (X)
	TIPOP."Nombre" as "TipoOperacion",
	PP."IdMedioDePago",
	MP."Nombre" as "MedioDePago",
	CASE WHEN MP."UsaCuentaBancaria" = 'S' AND BANCBANC."NombreBanco" IS NOT NULL AND CBANC."NumeroCuentaBancaria" IS NOT NULL
		THEN (COALESCE(MP."Nombre",'')||'-'||COALESCE(BANCBANC."NombreBanco",'')||'-'||COALESCE(CBANC."NumeroCuentaBancaria",'')) 
		ELSE MP."Nombre" END AS "Descripcion",
	PP."IdMoneda",			-- (X)
	PP."UsaPersonaRelacionada",
	CASE PP."UsaPersonaRelacionada" WHEN 'S' THEN 'Si' WHEN 'N' THEN 'No' ELSE '-' END AS "TextoUsaPersonaRelacionada",
	PPP."IdPersonaRelacionada" as "IdPersonaRelacionada",
	CASE PERSREL."Tipo" 
			WHEN 'N' THEN PERSREL."Nombres" ||' '|| PERSREL."ApellidoPaterno" || ' ' ||PERSREL."ApellidoMaterno" 
			ELSE PERSREL."RazonSocial"
	END  as "NombrePersonaRelacionada",
	COALESCE(IDENT."DocumentoIdentidad",'') as "DocumentoIdentidadPersonaRelacionada",
	CASE PPP."IdPersonaRelacionada" WHEN 0 THEN '' 
		ELSE 
		CASE PERSREL."Tipo" 
				WHEN 'N' THEN PERSREL."Nombres" ||' '|| PERSREL."ApellidoPaterno" || ' ' ||PERSREL."ApellidoMaterno" ||' - '|| COALESCE(IDENT."DocumentoIdentidad",'')
				ELSE PERSREL."RazonSocial"||' - '|| COALESCE(IDENT."DocumentoIdentidad",'')
		END
		END  as "TextoPersonaRelacionada",
	MP."UsaCuentaBancaria",
	CASE MP."UsaCuentaBancaria" WHEN 'S' THEN 'SI' WHEN 'N' THEN 'NO' ELSE '-' END AS "TextoUsaCuentaBancaria",
	MP."UsaTarjetaBancaria",
	CASE MP."UsaTarjetaBancaria" WHEN 'S' THEN 'SI' WHEN 'N' THEN 'NO' ELSE '-' END AS "TextoUsaTarjetaBancaria",
	TIPOP."RecibePagaPortafolio",
	CASE TIPOP."RecibePagaPortafolio" WHEN 'R' THEN 'Recibe' WHEN 'P' THEN 'Paga' ELSE '-' END AS "TextoRecibePagaPortafolio",
	PPP."PorDefecto",
	CASE PPP."PorDefecto" WHEN 'S' THEN 'SI' ELSE 'NO' END as "TextoPorDefecto",
	CASE 
		WHEN MP."UsaCuentaBancaria" = 'S' AND TIPOP."RecibePagaPortafolio" = 'R' THEN MP."Nombre" || ', ' || COALESCE(BANCBANC."NombreBanco",'') || ' - ' || COALESCE(CBANC."NumeroCuentaBancaria",'')
		WHEN MP."UsaCuentaBancaria" = 'S' AND TIPOP."RecibePagaPortafolio" = 'P' THEN MP."Nombre" || ', ' || COALESCE(BANCO."NombreBanco",'') || ' - ' || COALESCE(CUENTABANCO."NumeroCuentaBancaria",'')
		WHEN MP."UsaTarjetaBancaria" = 'S' THEN MP."Nombre" || ', ' || COALESCE(BANTBANC."NombreBanco",'') || ' - ' || COALESCE(TBANC."NumeroTarjetaBancaria",'')
		ELSE MP."Nombre"
		END as "TextoPreferenciaPago",
	BANTBANC."IdBanco" AS "IdBancoTarjetaBancariaBancaria",
	TIPOP."IdTipoProducto",
	POR."IdCliente",
	POR."IdMatrizAgrupadorAPV"
FROM "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio" as PPP 
  INNER JOIN "BSCL.Plataforma.BD::PAG.PreferenciaPago" as PP ON PPP."IdPreferenciaPago" = PP."IdPreferenciaPago"
  INNER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" as TIPOP ON TIPOP."IdTipoOperacion" = PP."IdTipoOperacion"
  INNER JOIN "BSCL.Plataforma.BD::PAG.MedioDePago" as MP ON PP."IdMedioDePago" = MP."IdMedioDePago"
  INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" as POR ON PPP."IdPortafolio" = POR."IdPortafolio"
  INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" as CLI ON POR."IdCliente" = CLI."IdCliente"
  LEFT JOIN "BSCL.SocioNegocio.BD::PER.CuentaBancaria" as CBANC ON CLI."IdPersona" = CBANC."IdPersona" AND PPP."IdCuentaBancaria" = CBANC."IdCuentaBancaria"
  INNER JOIN "BSCL.Tesoreria.BD::TES.CuentaBanco" AS CUENTABANCO  ON CUENTABANCO."IdBanco" = CBANC."IdBanco" 
  																AND	CUENTABANCO."IdMoneda" = CBANC."IdMoneda"
  																AND	CUENTABANCO."NumeroCuentaBancaria" = CBANC."NumeroCuentaBancaria" 
  LEFT JOIN "BSCL.Plataforma.BD::BAN.Banco" AS BANCO ON (BANCO."IdBanco" = CUENTABANCO."IdBanco")
  LEFT JOIN "BSCL.Plataforma.BD::BAN.TipoCuentaBancaria" AS TIPOCUENTABANCARIA ON (TIPOCUENTABANCARIA."IdTipoCuentaBancaria" = CUENTABANCO."IdTipoCuentaBancaria")
  LEFT JOIN "BSCL.SocioNegocio.BD::PER.TarjetaBancaria" as TBANC ON CLI."IdPersona" = TBANC."IdPersona" AND PPP."IdTarjetaBancaria" = TBANC."IdTarjetaBancaria"
  LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PERSREL ON PERSREL."IdPersona" = PPP."IdPersonaRelacionada"
  LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as IDENT ON PERSREL."IdDocumentoIdentidad" = IDENT."IdDocumentoIdentidad"
  LEFT JOIN "BSCL.Plataforma.BD::BAN.Banco" as BANCBANC ON BANCBANC."IdBanco" = CBANC."IdBanco"
  LEFT JOIN "BSCL.Plataforma.BD::BAN.Banco" as BANTBANC ON BANTBANC."IdBanco" = TBANC."IdBanco"
  LEFT JOIN "BSCL.Plataforma.BD::BAN.TipoCuentaBancaria" as TCBANC ON CBANC."IdTipoCuentaBancaria" = TCBANC."IdTipoCuentaBancaria"
  LEFT JOIN "BSCL.Plataforma.BD::BAN.TipoTarjetaBancaria" as TTBANC ON TBANC."IdTipoTarjetaBancaria" = TTBANC."IdTipoTarjetaBancaria"
  LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" as ESTPPP ON ESTPPP."IdEstado" = PPP."IdEstado"
WHERE PPP."IdEstado" = 'ACT'

UNION ALL 

SELECT 
	PPP."IdPreferenciaPagoPortafolio",
	PPP."IdPortafolio", 
	PPP."IdPreferenciaPago",
	PPP."IdCuentaBancaria",
	CASE PPP."IdCuentaBancaria" WHEN 0 THEN 0  
		ELSE (
			CASE TIPOP."RecibePagaPortafolio" 
				WHEN 'R' THEN COALESCE(CBANC."IdBanco",0)
				WHEN 'P' THEN 
					CASE WHEN TIPOP."IdTipoProducto"  IN ('MEME','MERF','MEFW')
					THEN COALESCE(CBANC."IdBanco",0) ELSE COALESCE(CUENTABANCO."IdBanco",0) END
				ELSE 0 END) 
		END as "IdBancoCuentaBancaria",
	CASE PPP."IdCuentaBancaria" WHEN 0 THEN '' 
		ELSE (
			CASE TIPOP."RecibePagaPortafolio" 
				WHEN 'R' THEN COALESCE(BANCBANC."NombreBanco",'')
				WHEN 'P' THEN 
					CASE WHEN TIPOP."IdTipoProducto"   IN ('MEME','MERF','MEFW')
					THEN COALESCE(BANCBANC."NombreBanco",'') ELSE COALESCE(BANCO."NombreBanco",'') END 					
				ELSE '' END) 
		END as "NombreBancoCuentaBancaria",
	CASE PPP."IdCuentaBancaria" WHEN 0 THEN '' 
		ELSE (
			CASE TIPOP."RecibePagaPortafolio" 
				WHEN 'R' THEN COALESCE(CBANC."NumeroCuentaBancaria",'')  	
				WHEN 'P' THEN 
					CASE WHEN TIPOP."IdTipoProducto" IN ('MEME','MERF','MEFW')
					THEN COALESCE(CBANC."NumeroCuentaBancaria",'') ELSE COALESCE(CUENTABANCO."NumeroCuentaBancaria",'') END			
				ELSE '' END) 
		END as "NumeroCuentaBancaria",
	CASE PPP."IdCuentaBancaria" WHEN 0 THEN '' 
		ELSE (
			CASE TIPOP."RecibePagaPortafolio" 
				WHEN 'R' THEN COALESCE(TCBANC."IdTipoCuentaBancaria",'')
				WHEN 'P' THEN 
					CASE WHEN TIPOP."IdTipoProducto"  IN ('MEME','MERF','MEFW')
					THEN COALESCE(TCBANC."IdTipoCuentaBancaria",'') ELSE COALESCE(CUENTABANCO."IdTipoCuentaBancaria",'') END			
				ELSE '' END) 
		END as "IdTipoCuentaBancaria",
	CASE PPP."IdCuentaBancaria" WHEN 0 THEN '' 
		ELSE (
			CASE TIPOP."RecibePagaPortafolio" 
				WHEN 'R' THEN COALESCE(TCBANC."NombreTipoCuentaBancaria",'') 
				WHEN 'P' THEN 
					CASE WHEN TIPOP."IdTipoProducto" IN ('MEME','MERF','MEFW')
					THEN COALESCE(TCBANC."NombreTipoCuentaBancaria",'') ELSE COALESCE(TIPOCUENTABANCARIA."NombreTipoCuentaBancaria",'') END  	
				ELSE '' END) 
		END as "NombreTipoCuentaBancaria",
	PPP."IdTarjetaBancaria",
	CASE PPP."IdTarjetaBancaria" WHEN 0 THEN 0 ELSE COALESCE(TBANC."IdBanco",0) END as "IdBancoTarjetaBancaria",
	CASE PPP."IdTarjetaBancaria" WHEN 0 THEN '' ELSE COALESCE(BANTBANC."NombreBanco",'') END as "NombreBancoTarjetaBancaria",
	CASE PPP."IdTarjetaBancaria" WHEN 0 THEN '' ELSE COALESCE(TBANC."NumeroTarjetaBancaria",'') END as "NumeroTarjetaBancaria",
	CASE PPP."IdTarjetaBancaria" WHEN 0 THEN '' ELSE COALESCE(TTBANC."IdTipoTarjetaBancaria",'') END as "IdTipoTarjetaBancaria",
	CASE PPP."IdTarjetaBancaria" WHEN 0 THEN '' ELSE COALESCE(TTBANC."NombreTipoTarjetaBancaria",'') END as "NombreTipoTarjetaBancaria",
   	PPP."FechaCreacion",
	PPP."FechaModificacion",
	PPP."IdUsuarioCreador",
	PPP."IdEstado",
	ESTPPP."Nombre" as "Estado",
	PPP."IdEmpresa",
	PP."IdTipoOperacion", 	-- (X)
	TIPOP."Nombre" as "TipoOperacion",
	PP."IdMedioDePago",
	MP."Nombre" as "MedioDePago",
	CASE WHEN MP."UsaCuentaBancaria" = 'S' AND BANCBANC."NombreBanco" IS NOT NULL AND CBANC."NumeroCuentaBancaria" IS NOT NULL
		THEN (COALESCE(MP."Nombre",'')||'-'||COALESCE(BANCBANC."NombreBanco",'')||'-'||COALESCE(CBANC."NumeroCuentaBancaria",'')) 
		ELSE MP."Nombre" END AS "Descripcion",
	PP."IdMoneda",			-- (X)
	PP."UsaPersonaRelacionada",
	CASE PP."UsaPersonaRelacionada" WHEN 'S' THEN 'Si' WHEN 'N' THEN 'No' ELSE '-' END AS "TextoUsaPersonaRelacionada",
	PPP."IdPersonaRelacionada" as "IdPersonaRelacionada",
	CASE PERSREL."Tipo" 
			WHEN 'N' THEN PERSREL."Nombres" ||' '|| PERSREL."ApellidoPaterno" || ' ' ||PERSREL."ApellidoMaterno" 
			ELSE PERSREL."RazonSocial"
	END  as "NombrePersonaRelacionada",
	COALESCE(IDENT."DocumentoIdentidad",'') as "DocumentoIdentidadPersonaRelacionada",
	CASE PPP."IdPersonaRelacionada" WHEN 0 THEN '' 
		ELSE 
		CASE PERSREL."Tipo" 
				WHEN 'N' THEN PERSREL."Nombres" ||' '|| PERSREL."ApellidoPaterno" || ' ' ||PERSREL."ApellidoMaterno" ||' - '|| COALESCE(IDENT."DocumentoIdentidad",'')
				ELSE PERSREL."RazonSocial"||' - '|| COALESCE(IDENT."DocumentoIdentidad",'')
		END
		END  as "TextoPersonaRelacionada",
	MP."UsaCuentaBancaria",
	CASE MP."UsaCuentaBancaria" WHEN 'S' THEN 'SI' WHEN 'N' THEN 'NO' ELSE '-' END AS "TextoUsaCuentaBancaria",
	MP."UsaTarjetaBancaria",
	CASE MP."UsaTarjetaBancaria" WHEN 'S' THEN 'SI' WHEN 'N' THEN 'NO' ELSE '-' END AS "TextoUsaTarjetaBancaria",
	TIPOP."RecibePagaPortafolio",
	CASE TIPOP."RecibePagaPortafolio" WHEN 'R' THEN 'Recibe' WHEN 'P' THEN 'Paga' ELSE '-' END AS "TextoRecibePagaPortafolio",
	PPP."PorDefecto",
	CASE PPP."PorDefecto" WHEN 'S' THEN 'SI' ELSE 'NO' END as "TextoPorDefecto",
	CASE 
		WHEN MP."UsaCuentaBancaria" = 'S' AND TIPOP."RecibePagaPortafolio" = 'R' THEN MP."Nombre" || ', ' || COALESCE(BANCBANC."NombreBanco",'') || ' - ' || COALESCE(CBANC."NumeroCuentaBancaria",'')
		WHEN MP."UsaCuentaBancaria" = 'S' AND TIPOP."RecibePagaPortafolio" = 'P' THEN MP."Nombre" || ', ' || COALESCE(BANCO."NombreBanco",'') || ' - ' || COALESCE(CUENTABANCO."NumeroCuentaBancaria",'')
		WHEN MP."UsaTarjetaBancaria" = 'S' THEN MP."Nombre" || ', ' || COALESCE(BANTBANC."NombreBanco",'') || ' - ' || COALESCE(TBANC."NumeroTarjetaBancaria",'')
		ELSE MP."Nombre"
		END as "TextoPreferenciaPago",
	BANTBANC."IdBanco" AS "IdBancoTarjetaBancariaBancaria",
	TIPOP."IdTipoProducto",
	POR."IdCliente",
	POR."IdMatrizAgrupadorAPV"
FROM "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio" as PPP 
  INNER JOIN "BSCL.Plataforma.BD::PAG.PreferenciaPago" as PP ON PPP."IdPreferenciaPago" = PP."IdPreferenciaPago"
  INNER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" as TIPOP ON TIPOP."IdTipoOperacion" = PP."IdTipoOperacion"
  INNER JOIN "BSCL.Plataforma.BD::PAG.MedioDePago" as MP ON PP."IdMedioDePago" = MP."IdMedioDePago"
  INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" as POR ON PPP."IdPortafolio" = POR."IdPortafolio"
  INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" as CLI ON POR."IdCliente" = CLI."IdCliente"
  LEFT JOIN "BSCL.SocioNegocio.BD::PER.CuentaBancaria" as CBANC ON CLI."IdPersona" = CBANC."IdPersona" AND PPP."IdCuentaBancaria" = CBANC."IdCuentaBancaria"
  LEFT JOIN "BSCL.Tesoreria.BD::TES.CuentaBanco" AS CUENTABANCO  ON CUENTABANCO."IdBanco" = CBANC."IdBanco" 
  																AND	CUENTABANCO."IdMoneda" = CBANC."IdMoneda"
  																AND	CUENTABANCO."NumeroCuentaBancaria" = CBANC."NumeroCuentaBancaria" 
  LEFT JOIN "BSCL.Plataforma.BD::BAN.Banco" AS BANCO ON (BANCO."IdBanco" = CUENTABANCO."IdBanco")
  LEFT JOIN "BSCL.Plataforma.BD::BAN.TipoCuentaBancaria" AS TIPOCUENTABANCARIA ON (TIPOCUENTABANCARIA."IdTipoCuentaBancaria" = CUENTABANCO."IdTipoCuentaBancaria")
  LEFT JOIN "BSCL.SocioNegocio.BD::PER.TarjetaBancaria" as TBANC ON CLI."IdPersona" = TBANC."IdPersona" AND PPP."IdTarjetaBancaria" = TBANC."IdTarjetaBancaria"
  LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PERSREL ON PERSREL."IdPersona" = PPP."IdPersonaRelacionada"
  LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as IDENT ON PERSREL."IdDocumentoIdentidad" = IDENT."IdDocumentoIdentidad"
  LEFT JOIN "BSCL.Plataforma.BD::BAN.Banco" as BANCBANC ON BANCBANC."IdBanco" = CBANC."IdBanco"
  LEFT JOIN "BSCL.Plataforma.BD::BAN.Banco" as BANTBANC ON BANTBANC."IdBanco" = TBANC."IdBanco"
  LEFT JOIN "BSCL.Plataforma.BD::BAN.TipoCuentaBancaria" as TCBANC ON CBANC."IdTipoCuentaBancaria" = TCBANC."IdTipoCuentaBancaria"
  LEFT JOIN "BSCL.Plataforma.BD::BAN.TipoTarjetaBancaria" as TTBANC ON TBANC."IdTipoTarjetaBancaria" = TTBANC."IdTipoTarjetaBancaria"
  LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" as ESTPPP ON ESTPPP."IdEstado" = PPP."IdEstado"
WHERE PPP."IdEstado" = 'ACT'
AND PPP."IdCuentaBancaria" = 0

GO	 	
GRANT SELECT ON "BSCL.Portafolio.BD.Vistas::ConsultarPreferenciaPagoPortafolioCorredora" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
GO
