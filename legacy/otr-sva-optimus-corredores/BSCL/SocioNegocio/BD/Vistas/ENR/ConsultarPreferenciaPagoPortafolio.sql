SET SCHEMA "BSCL";

DROP VIEW "BSCL.SocioNegocio.BD.Vistas::ENR.ConsultarPreferenciaPagoPortafolio";

CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::ENR.ConsultarPreferenciaPagoPortafolio" AS 
SELECT 
PPP."IdPreferenciaPagoPortafolio"
,PPP."IdEnrolamiento"
,PPP."IdPreferenciaPago"
,PPP."IdCuentaBancaria"
,CASE PPP."IdCuentaBancaria" WHEN 0 THEN 0  
	ELSE (
		CASE TIPOP."RecibePagaPortafolio" 
			WHEN 'R' THEN COALESCE(CBANC."IdBanco",0)
			WHEN 'P' THEN 
				CASE WHEN TIPOP."IdTipoProducto"  IN ('MEME','MERF','MEFW')
				THEN COALESCE(CBANC."IdBanco",0) ELSE COALESCE(CUENTABANCO."IdBanco",0) END
			ELSE 0 END) 
END AS "IdBancoCuentaBancaria"
,CASE PPP."IdCuentaBancaria" WHEN 0 THEN '' 
	ELSE (
		CASE TIPOP."RecibePagaPortafolio" 
			WHEN 'R' THEN COALESCE(BANCBANC."NombreBanco",'')
			WHEN 'P' THEN 
				CASE WHEN TIPOP."IdTipoProducto"   IN ('MEME','MERF','MEFW')
				THEN COALESCE(BANCBANC."NombreBanco",'') ELSE COALESCE(BANCO."NombreBanco",'') END 					
			ELSE '' END) 
END AS "NombreBancoCuentaBancaria"
,CASE PPP."IdCuentaBancaria" WHEN 0 THEN '' 
	ELSE (
		CASE TIPOP."RecibePagaPortafolio" 
			WHEN 'R' THEN COALESCE(CBANC."NumeroCuentaBancaria",'')  	
			WHEN 'P' THEN 
				CASE WHEN TIPOP."IdTipoProducto" IN ('MEME','MERF','MEFW')
				THEN COALESCE(CBANC."NumeroCuentaBancaria",'') ELSE COALESCE(CUENTABANCO."NumeroCuentaBancaria",'') END			
			ELSE '' END) 
END AS "NumeroCuentaBancaria"
,CASE PPP."IdCuentaBancaria" WHEN 0 THEN '' 
	ELSE (
		CASE TIPOP."RecibePagaPortafolio" 
			WHEN 'R' THEN COALESCE(TCBANC."IdTipoCuentaBancaria",'')
			WHEN 'P' THEN 
				CASE WHEN TIPOP."IdTipoProducto"  IN ('MEME','MERF','MEFW')
				THEN COALESCE(TCBANC."IdTipoCuentaBancaria",'') ELSE COALESCE(CUENTABANCO."IdTipoCuentaBancaria",'') END			
			ELSE '' END) 
END AS "IdTipoCuentaBancaria"
,CASE PPP."IdCuentaBancaria" WHEN 0 THEN '' 
	ELSE (
		CASE TIPOP."RecibePagaPortafolio" 
			WHEN 'R' THEN COALESCE(TCBANC."NombreTipoCuentaBancaria",'') 
			WHEN 'P' THEN 
				CASE WHEN TIPOP."IdTipoProducto" IN ('MEME','MERF','MEFW')
				THEN COALESCE(TCBANC."NombreTipoCuentaBancaria",'') ELSE COALESCE(TIPOCUENTABANCARIA."NombreTipoCuentaBancaria",'') END  	
			ELSE '' END) 
END AS "NombreTipoCuentaBancaria"
,PPP."IdTarjetaBancaria"
,CASE PPP."IdTarjetaBancaria" 
	WHEN 0 THEN 0 
	ELSE COALESCE(TBANC."IdBanco",0) 
END AS "IdBancoTarjetaBancaria"
,CASE PPP."IdTarjetaBancaria" 
	WHEN 0 THEN '' 
	ELSE COALESCE(BANTBANC."NombreBanco",'') 
END AS "NombreBancoTarjetaBancaria"
,CASE PPP."IdTarjetaBancaria" 
	WHEN 0 THEN '' 
	ELSE COALESCE(TBANC."NumeroTarjetaBancaria",'') 
END AS "NumeroTarjetaBancaria"
,CASE PPP."IdTarjetaBancaria" 
	WHEN 0 THEN '' 
	ELSE COALESCE(TTBANC."IdTipoTarjetaBancaria",'') 
END AS "IdTipoTarjetaBancaria"
,CASE PPP."IdTarjetaBancaria" 
	WHEN 0 THEN '' 
	ELSE COALESCE(TTBANC."NombreTipoTarjetaBancaria",'') 
END AS "NombreTipoTarjetaBancaria"
,PPP."IdEmpresa"
,PP."IdTipoOperacion"
,TIPOP."Nombre" AS "TipoOperacion"
,PP."IdMedioDePago"
,MP."Nombre" AS "MedioDePago"
,CASE WHEN MP."UsaCuentaBancaria" = 'S' AND BANCBANC."NombreBanco" IS NOT NULL AND CBANC."NumeroCuentaBancaria" IS NOT NULL
	THEN (COALESCE(MP."Nombre",'')||'-'||COALESCE(BANCBANC."NombreBanco",'')||'-'||COALESCE(CBANC."NumeroCuentaBancaria",'')) 
	ELSE MP."Nombre" 
END AS "Descripcion"
,PP."IdMoneda"
,PP."UsaPersonaRelacionada"
,CASE PP."UsaPersonaRelacionada" 
	WHEN 'S' THEN 'Si' 
	WHEN 'N' THEN 'No' 
	ELSE '-' 
END AS "TextoUsaPersonaRelacionada"
,PPP."IdPersonaRelacionada" AS "IdPersonaRelacionada"
,CASE PERSREL."Tipo" 
		WHEN 'N' THEN PERSREL."Nombres" ||' '|| PERSREL."ApellidoPaterno" || ' ' ||PERSREL."ApellidoMaterno" 
		ELSE PERSREL."RazonSocial"
END AS "NombrePersonaRelacionada"
,COALESCE(IDENT."DocumentoIdentidad",'') AS "DocumentoIdentidadPersonaRelacionada"
,CASE PPP."IdPersonaRelacionada" WHEN 0 THEN '' 
	ELSE 
	CASE PERSREL."Tipo" 
			WHEN 'N' THEN PERSREL."Nombres" ||' '|| PERSREL."ApellidoPaterno" || ' ' ||PERSREL."ApellidoMaterno" ||' - '|| COALESCE(IDENT."DocumentoIdentidad",'')
			ELSE PERSREL."RazonSocial"||' - '|| COALESCE(IDENT."DocumentoIdentidad",'')
	END
END AS "TextoPersonaRelacionada"
,MP."UsaCuentaBancaria"
,CASE MP."UsaCuentaBancaria" 
	WHEN 'S' THEN 'SI' WHEN 'N' THEN 'NO' 
	ELSE '-' 
END AS "TextoUsaCuentaBancaria"
,MP."UsaTarjetaBancaria"
,CASE MP."UsaTarjetaBancaria" 
	WHEN 'S' THEN 'SI' WHEN 'N' 
	THEN 'NO' ELSE '-'
END AS "TextoUsaTarjetaBancaria"
,TIPOP."RecibePagaPortafolio"
,CASE TIPOP."RecibePagaPortafolio" 
	WHEN 'R' THEN 'Recibe' WHEN 'P' THEN 'Paga' 
	ELSE '-' 
END AS "TextoRecibePagaPortafolio"
,PPP."PorDefecto"
,CASE PPP."PorDefecto" 
	WHEN 'S' THEN 'SI' 
	ELSE 'NO'
END AS "TextoPorDefecto"
,CASE 
	WHEN MP."UsaCuentaBancaria" = 'S' AND TIPOP."RecibePagaPortafolio" = 'R' THEN MP."Nombre" || ', ' || COALESCE(BANCBANC."NombreBanco",'') || ' - ' || COALESCE(CBANC."NumeroCuentaBancaria",'')
	WHEN MP."UsaCuentaBancaria" = 'S' AND TIPOP."RecibePagaPortafolio" = 'P' THEN MP."Nombre" || ', ' || COALESCE(BANCO."NombreBanco",'') || ' - ' || COALESCE(CUENTABANCO."NumeroCuentaBancaria",'')
	WHEN MP."UsaTarjetaBancaria" = 'S' THEN MP."Nombre" || ', ' || COALESCE(BANTBANC."NombreBanco",'') || ' - ' || COALESCE(TBANC."NumeroTarjetaBancaria",'')
	ELSE MP."Nombre"
END AS "TextoPreferenciaPago"
,BANTBANC."IdBanco" AS "IdBancoTarjetaBancariaBancaria"
,TIPOP."IdTipoProducto"
FROM "BSCL.SocioNegocio.BD::ENR.PreferenciaPagoPortafolio" AS PPP 
LEFT JOIN "BSCL.Plataforma.BD::PAG.PreferenciaPago" AS PP ON PPP."IdPreferenciaPago" = PP."IdPreferenciaPago"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS TIPOP ON TIPOP."IdTipoOperacion" = PP."IdTipoOperacion"
LEFT JOIN "BSCL.Plataforma.BD::PAG.MedioDePago" AS MP ON PP."IdMedioDePago" = MP."IdMedioDePago"
LEFT JOIN "BSCL.SocioNegocio.BD::ENR.CuentaBancaria" AS CBANC ON PPP."IdEnrolamiento" = CBANC."IdEnrolamiento" AND PPP."IdCuentaBancaria" = CBANC."IdCuentaBancaria"
LEFT JOIN "BSCL.Tesoreria.BD::TES.CuentaBanco" AS CUENTABANCO  ON PPP."IdCuentaBancaria" = CUENTABANCO."IdCuentaBanco"
LEFT JOIN "BSCL.Plataforma.BD::BAN.Banco" AS BANCO ON (BANCO."IdBanco" = CUENTABANCO."IdBanco")
LEFT JOIN "BSCL.Plataforma.BD::BAN.TipoCuentaBancaria" AS TIPOCUENTABANCARIA ON (TIPOCUENTABANCARIA."IdTipoCuentaBancaria" = CUENTABANCO."IdTipoCuentaBancaria")
LEFT JOIN "BSCL.SocioNegocio.BD::ENR.TarjetaBancaria" AS TBANC ON PPP."IdEnrolamiento" = TBANC."IdEnrolamiento" AND PPP."IdTarjetaBancaria" = TBANC."IdTarjetaBancaria"
LEFT JOIN "BSCL.SocioNegocio.BD::ENR.Enrolamiento" AS PERSREL ON PERSREL."IdEnrolamiento" = PPP."IdEnrolamiento"
LEFT JOIN "BSCL.SocioNegocio.BD::ENR.DocumentoIdentidad" AS IDENT ON PERSREL."IdDocumentoIdentidad" = IDENT."IdDocumentoIdentidad"
LEFT JOIN "BSCL.Plataforma.BD::BAN.Banco" AS BANCBANC ON BANCBANC."IdBanco" = CBANC."IdBanco"
LEFT JOIN "BSCL.Plataforma.BD::BAN.Banco" AS BANTBANC ON BANTBANC."IdBanco" = TBANC."IdBanco"
LEFT JOIN "BSCL.Plataforma.BD::BAN.TipoCuentaBancaria" AS TCBANC ON CBANC."IdTipoCuentaBancaria" = TCBANC."IdTipoCuentaBancaria"
LEFT JOIN "BSCL.Plataforma.BD::BAN.TipoTarjetaBancaria" AS TTBANC ON TBANC."IdTipoTarjetaBancaria" = TTBANC."IdTipoTarjetaBancaria";

GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::ENR.ConsultarPreferenciaPagoPortafolio" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 

